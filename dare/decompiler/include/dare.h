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
 * dare.h
 *
 * The main application.
 */

#ifndef DARE_H_
#define DARE_H_


#include <string.h>

#include <set>


class Dare {
 public:
  Dare()
      : output_dir_(""),
        temp_file_name_(NULL),
        stubs_dir_(NULL),
        ignore_bad_checksum_(false) {}

  static bool split_exception_tables() { return split_exception_tables_; }
  static int offset_limit() { return offset_limit_; }
//  static void AddConflictedClass(std::string c) {
//    conflicted_classes_.insert(c);
//  }
//  static void WriteConflictedClasses(std::ostream& out);

  int Start(int argc, char* const argv[]);

 private:
  struct CStringOp {
    bool operator() (const char* lhs, const char* rhs) const {
      return strcmp(lhs, rhs) < 0;
    }
  };

  typedef std::set<const char*, CStringOp> CStringSet;

  static const char* kVersion;

  static const char* version() { return kVersion; }
  /**
   * Turn class list argument into set.
   *
   * @param A list of :-separated classes.
   */
  void ProcessClassList(char* class_list);
  /**
   * Process a .dex file.
   *
   * @param file File name.
   * @return 0 on success, non-0 on failure.
   */
  int Process(const char* fileName);
  /**
   * Display usage information.
   */
  void Usage() const;

  const char* output_dir_;
  const char* temp_file_name_;
  const char* stubs_dir_;

  static bool split_exception_tables_;
  static int offset_limit_;

  bool ignore_bad_checksum_;
//  static std::set<std::string> conflicted_classes_;

  CStringSet class_list_;
};


#endif /* DARE_H_ */
