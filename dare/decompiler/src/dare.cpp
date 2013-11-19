/*
 * Copyright (C) 2012 The Pennsylvania State University
 * Systems and Internet Infrastructure Security Laboratory
 *
 * Author: Damien Octeau <octeau@cse.psu.edu>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

/**
 * dare.cpp
 *
 * Main application.
 */

#include "dare.h"

#include <stdlib.h>

#include <fstream>

#include "libdex/SysUtil.h"
#include "libdex/CmdUtils.h"
#include "libdex/DexFile.h"

#include "class_file/class_file.h"
#include "timer.h"

using std::string;


/*static*/ const char* Dare::kVersion = "1.1.0";
/*static*/ bool Dare::split_exception_tables_ = true;
/*static*/ int Dare::offset_limit_ = 5000;
///*static*/ std::set<std::string> Dare::conflicted_classes_;


/**
 * Start Dare processing. To be called from the main() function.
 *
 * @param argc Argument count.
 * @param argv[] Arguments.
 * @return 0 on success, non-0 on failure.
 */
int Dare::Start(int argc, char* const argv[]) {
  bool wantUsage = false;
  int ic;
  char* class_list = NULL;
  const char* verif = NULL;

  while (true) {
    ic = getopt(argc, argv, "id:t:c:es:p:vl:");
    if (ic < 0)
      break;

    switch (ic) {
      case 'i':       // continue even if checksum is bad
        ignore_bad_checksum_ = true;
        break;
      case 'd':
        output_dir_ = optarg;
        break;
      case 't':       // temp file, used when opening compressed Jar
        temp_file_name_ = optarg;
        break;
      case 'c':
        class_list = optarg;
        break;
      case 'e':
        split_exception_tables_ = false;
        break;
      case 's':
        stubs_dir_ = optarg;
        break;
      case 'p':
        verif = optarg;
        break;
      case 'v':
        printf("dare version %s\n", Dare::version());
        return 0;
      case 'l':
        offset_limit_ = atoi(optarg);
        break;
      default:
        wantUsage = true;
        break;
    }
  }

  if (optind == argc) {
    fprintf(stderr, "dare: no file specified\n");
    wantUsage = true;
  }

  if (class_list != NULL)
    ProcessClassList(class_list);

  if (verif != NULL)
    ClassFile::ProcessVerifierOutput(verif);

  if (wantUsage) {
    Usage();
    return 2;
  }

  return Process(argv[optind]);
}

/**
 * Turn class list argument into set.
 *
 * @param A list of :-separated classes.
 */
void Dare::ProcessClassList(char* class_list) {
  char* current = strtok(class_list, ":");
  while (current != NULL) {
    class_list_.insert(current);
    current = strtok(NULL, ":");
  }
}

/**
 * Process a .dex file.
 *
 * @param file File name.
 * @return 0 on success, non-0 on failure.
 */
int Dare::Process(const char* file) {
  DexFile* dex_file = NULL;
  MemMapping map;
  bool mapped = false;
  int result = 1;
  ClassFile* classFile;
  string file_name(output_dir_);
  int counter = 0;
  string current_file_name;
  std::ofstream conflicted_classes;
  string times_file_name = file_name + "/times.csv";
  std::ifstream times_in(times_file_name.c_str());
  std::ofstream times_out;

  Timer::getInstance().Init(times_in);
  times_in.close();
  Timer::getInstance().Start(Timer::kParsing);

  // Open and map file
  if (dexOpenAndMap(file, temp_file_name_, &map, false) != 0)
    goto bail;
  mapped = true;

  {
    int flags = kDexParseVerifyChecksum;
    if (ignore_bad_checksum_)
      flags |= kDexParseContinueOnError;

    dex_file = dexFileParse((const u1*) map.addr, map.length, flags);
  }

  if (dex_file == NULL) {
    fprintf(stderr, "ERROR: DEX parse failed\n");
    goto bail;
  }

  if (class_list_.empty()) {
    for (int i = 0; i < (int) dex_file->pHeader->classDefsSize; ++i) {
      ClassFile classFile(dex_file);
      classFile.GenerateClass(i, output_dir_);
    }
  } else {
    for (int i = 0; i < (int) dex_file->pHeader->classDefsSize; ++i) {
      const DexClassDef* pClassDef = dexGetClassDef(dex_file, i);
      const char* classDescriptor = dexStringByTypeIdx(dex_file,
          pClassDef->classIdx);
      if (class_list_.find(classDescriptor) != class_list_.end()) {
        ClassFile classFile(dex_file);
        classFile.GenerateClass(i, output_dir_);
      }
    }
  }

  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kOther);

  if (stubs_dir_ != NULL)
    ClassFile::GenerateStubs(stubs_dir_);

  ClassFile::ClearStaticRefs();

//  file_name += "/conflicted_classes_";
//  file_name += ((char) ('0' + throw_analysis_));

//  conflicted_classes.open(file_name.c_str());
//  Ded::WriteConflictedClasses(conflicted_classes);
//  conflicted_classes.close();

  result = 0;

bail:
  if (mapped)
    sysReleaseShmem(&map);
  if (dex_file != NULL)
    dexFileFree(dex_file);
  Timer::getInstance().End();
  times_out.open(times_file_name.c_str());
  Timer::getInstance().WriteToFile(times_out, file);
  times_out.close();
  return result;
}


/**
 * Display usage information.
 */
void Dare::Usage() const {
  fprintf(stderr, "Copyright (C) 2012-2013 The Pennsylvania State University\n"
      "Systems and Internet Infrastructure Security Laboratory\n\n");
  fprintf(stderr,
    "Usage: dare-%s [-d <output dir>] [-i] [-t <tempfile>] [-c <class-list>]"
    " [-e] [-s <stub dir>] [-p <verifier annotations>] <dex or apk file>\n",
    Dare::version());
  fprintf(stderr, " -d <output dir>: set output directory\n");
  fprintf(stderr, " -i : ignore checksum failures\n");
  fprintf(stderr, " -t <tempfile>: temp file name is tempfile\n");
  fprintf(stderr, " -c <class-list> : only process classes from class-list"
      "(separated by a : character)\n");
  fprintf(stderr, " -e : prevent exception table splitting\n");
  fprintf(stderr, " -s <stub dir>: output class stubs to stub dir\n");
  fprintf(stderr, " -p <verifier annotations> : use verifier annotations\n");
  fprintf(stderr, " -v : version number\n");
  fprintf(stderr, "\n");
}

///*static*/ void Dare::WriteConflictedClasses(std::ostream& out) {
//  for (std::set<std::string>::iterator it = conflicted_classes_.begin();
//      it != conflicted_classes_.end(); ++it) {
//    out << *it << "\n";
//  }
//}
