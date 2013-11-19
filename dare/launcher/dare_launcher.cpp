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
 * dare_launcher.cpp
 */

#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <unistd.h>

#include <algorithm>
#include <fstream>
#include <set>
#include <sstream>
#include <string>
#include <vector>

#include "dare_launcher.h"

using std::ifstream;
using std::ofstream;
using std::ostringstream;
using std::vector;
using std::set;
using std::string;


/*static*/ const char* DedLauncher::kVersion = "1.1.0";
/*static*/ pid_t DedLauncher::ch_pid_;


/**
 * Process a dex or apk file.
 *
 * @param argc Command line argument count.
 * @param argv Command line arguments.
 */
void DedLauncher::Process(int argc, char** argv) {
  bool want_usage = false;
  int ic;
  char* class_list = NULL;

  while (1) {
    ic = getopt(argc, argv, "d:s:a:ocj:m:p:evr:bx:kl:");
    if (ic < 0)
      break;

    switch (ic) {
    case 'd':
      output_dir_ = optarg;
      break;
    case 's':
      soot_ = optarg;
      break;
    case 'a':
      libraries_ = optarg;
      break;
    case 'o':
      optimize_ = true;
      break;
    case 'c':
      decompile_ = true;
      break;
    case 'j':
      jasmin_ = optarg;
      break;
    case 'm':
      maxine_ = optarg;
      break;
    case 'p':
      preverify_ = optarg;
      break;
    case 'e':
      no_split_tables_ = true;
      break;
    case 'v':
      printf("dare-launcher version %s\n", DedLauncher::version());
      return;
    case 'r':
      dare_ = optarg;
      break;
    case 'b':
      generate_stubs_ = true;
      break;
    case 'x':
      vm_options_.push_back(optarg);
      break;
    case 'k':
      keep_jasmin_files_ = true;
      break;
    case 'l':
      offset_limit_ = optarg;
      break;
    default:
      want_usage = true;
      break;
    }
  }

  if (optind == argc) {
    fprintf(stderr, "dare: no file specified\n");
    want_usage = true;
  } else {
    i_file_name_ = argv[optind];
  }

  if (want_usage) {
    Usage();
    return;
  }

  ProcessClasses();
}

/**
 * Display usage information.
 */
void DedLauncher::Usage() const {
  fprintf(stderr, "Copyright (C) 2012-2013 The Pennsylvania State University\n"
      "Systems and Internet Infrastructure Security Laboratory\n\n");
  fprintf(stderr,
    "Usage: dare-launcher-%s [-d <output dir>] [-s <Soot classes>] "
    "[-a <library classes>] [-o] [-c] [-j <jasmin jar>] [-m <maxine script>] "
    "[-p <Dalvik verifier>] [-e] <dex or apk file>\n", DedLauncher::version());
  fprintf(stderr, " -d <output dir>: set output directory\n");
  fprintf(stderr, " -s <Soot classes> : define Soot/Polyglot/Jasmin class "
      " locations (separated by a : character)\n");
  fprintf(stderr, " -a <library classes> : define location of .jar libraries "
      "to be used by Soot (separated by a : character)\n");
  fprintf(stderr, " -o : optimize classes using Soot\n");
  fprintf(stderr, " -c : optimize and decompile classes using Soot\n");
  fprintf(stderr, " -j <jasmin jar> : set the path to the Jasmin jar\n");
  fprintf(stderr, " -m <maxine script> : set the path to the Maxine max or mx "
      "script\n");
  fprintf(stderr, " -p <Dalvik verifier>: set the path to the Dalvik verifier, "
      "if pre-verification is desired\n");
  fprintf(stderr, " -e : prevent exception table splitting\n");
  fprintf(stderr, " -v : version number\n");
  fprintf(stderr, " -b : generate stubs\n");
  fprintf(stderr, " -x <VM option>: set a VM option to run Soot (use option "
      "several times to set multiple VM options)\n");
  fprintf(stderr, " -k : keep Jasmin files (they are deleted by default).\n");
  fprintf(stderr, "\n");
}

/**
 * Calculate the time elapsed between two timestamps.
 *
 * @param t1 First timestamp.
 * @param t2 Second timestamp.
 * @return The time elapsed between the two timestamps (in microseconds).
 */
int DedLauncher::CalcTime(struct timeval t1, struct timeval t2) const {
  return (t2.tv_sec * 1000000 - t1.tv_sec * 1000000 + t2.tv_usec
      - t1.tv_usec);
}

/**
 * Handle an alarm signal by killing the child process.
 *
 * @param sig The signal number.
 */
/*static*/ void DedLauncher::Handler(int sig) {
  fprintf(stderr, "Child process has taken too long and will be terminated\n");
  kill(ch_pid_, SIGKILL);
}

/**
 * Split a command string into an array of C strings.
 *
 * @param cmd The input command.
 * @return An array of C strings.
 */
char** DedLauncher::GetCmd(string cmd) const {
  vector<string> arguments;
  size_t cut;

  while ((cut = cmd.find_first_of(' ', 0)) != string::npos) {
    if(cut != 0)
      arguments.push_back(cmd.substr(0, cut));
    cmd = cmd.substr(cut + 1, string::npos);
  }

  arguments.push_back(cmd);
  char** args = (char**) malloc((arguments.size() + 1) * sizeof(char*));

  for(int i = 0; i < (int) arguments.size(); ++i) {
    args[i] = (char*) malloc(arguments[i].length() + 1);
    strncpy(args[i], arguments[i].c_str(), arguments[i].length());
    args[i][arguments[i].length()] = '\0';
  }
  args[arguments.size()] = NULL;

  return args;
}

/**
 * Fork a new process and kill it if it takes more than a certain amount of
 * time. Optionally, send the child process's stdout and stderr to file.
 *
 * @param cmd The command line used to fork the new process.
 * @param max_time The maximum amount of time the child process is allowed to
 *        run.
 * @param filename The optional name of the file to which the child process's
 *        stdout and stderr should be diverted.
 * @return The amount of time the child process ran.
 */
int DedLauncher::StartProcess(const string& cmd, int max_time,
    const char* filename /*= NULL*/, int* stat /*= NULL*/) const {
  pid_t pid;
//  printf("Command: %s\n", cmd.c_str());

  switch(pid = fork()) {
    case -1: {
      fprintf(stderr, "Problem with fork()\n");
      break;
    }
    case 0: {
      if (filename != NULL) {
        int fd = open(filename, O_RDWR | O_CREAT, 0666);

        // Divert stdout and stderr to file.
        dup2(fd, STDOUT_FILENO);
        dup2(fd, STDERR_FILENO);
        close(fd);
      }
      char** args = GetCmd(cmd);
      if(execvp(args[0], args) < 0)
        fprintf(stderr, "Error when calling exec: %s\n", strerror(errno));
      exit(0);
    }
    default: {
      ch_pid_ = pid;
      struct timeval time1, time2;
      signal(SIGALRM, Handler);
      alarm(max_time);
      gettimeofday(&time1, NULL);
      waitpid(ch_pid_, stat, 0);
      gettimeofday(&time2, NULL);
      signal(SIGALRM, SIG_IGN);

      return CalcTime(time1, time2);
    }
  }
  return 0;
}

/**
 * Execute the Dalvik retargeting process.
 *
 * @param options Extra options.
 * @return The time taken by retargeting.
 */
int DedLauncher::ExecuteDare(const string& options, int* dare_status) const {
  struct timeval time1;
  struct timeval time2;
  string cmd = dare_;
  cmd += " -d " + dclass_ + "/ -l " + offset_limit_ + options +
      (no_split_tables_ ? " -e " : " ") + i_file_name_;

  return StartProcess(cmd, 3000, NULL, dare_status);
}

/**
 * Execute Jasmin for bytecode assembly.
 *
 * @param class_list The list of classes which should be assembled.
 * @param directory The directory where the Jasmin files are located.
 * @return The time taken by bytecode assembly.
 */
int DedLauncher::ExecuteJasmin(const std::vector<std::string>& class_list,
    const std::string& directory) const {
  int jasmin_time = 0;
  // We divide ARG_MAX by 2 to give a largely sufficient margin for envp.
  // This is not very precise, but it is good enough for most cases.
  int arg_max = sysconf(_SC_ARG_MAX) / 2;
  int class_count = class_list.size();
  int i = 0;
  int current_limit = 0;
  while (i < class_count) {
    string class_list_concat;
    while (i < class_count && (int) class_list_concat.size() < arg_max)
      class_list_concat += " " + directory + "/" + class_list[i++] + ".jasmin";

    string cmd = string("java -jar ") + jasmin_ + " -d " + directory
        + class_list_concat;

    jasmin_time += StartProcess(cmd, 10 * class_list.size());
  }

  return jasmin_time;
}

/**
 * Remove Jasmin files.
 * 
 * @param class_list The list of classes which should be removed.
 * @param directory The directory where the Jasmin file are located.
 */
void DedLauncher::RemoveJasminFiles(const std::vector<std::string>& class_list, 
    const std::string& directory) const {
  if (!keep_jasmin_files_)
    for (int i = 0; i < (int) class_list.size(); ++i)
      remove((directory + "/" + class_list[i] + ".jasmin").c_str());
}

/**
 * Execute Maxine for Java bytecode verification.
 *
 * @param class_list The list of classes to verify.
 * @param classpath The classpath for Maxine.
 */
void DedLauncher::ExecuteMaxine(const std::vector<std::string>& class_list,
    std::string classpath) {
  for (int i = 0; i < (int) class_list.size(); ++i) {
    string cmd(maxine_);
    string filename = dclass_ + "/" + class_list[i] + ".max";
    cmd += " -cp/p:" + classpath + " verify -verbose=false ^" + class_list[i] +
        "^ > " + filename + " 2>&1";

    for (int j = 0; j < (int) cmd.length(); ++j) {
      if (cmd[j] == '$') {
        cmd.insert(j, "\\");
        ++j;
      }
    }

    system(cmd.c_str());

    for (int j = 0; j < (int) filename.length(); ++j) {
      if (filename[j] == '$') {
        filename.insert(j, "\\");
        ++j;
      }
    }

    int methods = MakePipe(string("grep -c ") + "\"^VerifyError:\" "
        + filename);
    method_verify_stats_ += methods;
    if (methods != 0)
      ++class_verify_stats_;
    method_count_ += MakePipe(string("grep -c ") + "\"^Verifying\" "
        + filename);
  }
}

/**
 * Execute Maxine per application.
 *
 * @param class_list The list of classes to verify.
 * @param classpath The Maxine classpath.
 */
void DedLauncher::ExecuteMaxineGlobal(
    const std::vector<std::string>& class_list,
    std::string classpath) {
  string class_list_global;
  for (int i = 0; i < (int) class_list.size(); ++i) {
    class_list_global += " ^" + class_list[i] + "^";
  }

  string cmd(maxine_);
  string filename = dclass_ + "/maxine_verif_global.max";
  cmd += " -cp/p:" + classpath + " verify -verbose=false" + class_list_global;
//  printf("*****Command: %s\n", cmd.c_str());

  StartProcess(cmd, 3000, filename.c_str());

  int app_method_count = MakePipe(string("grep -c ") + "\"^Verifying\" "
      + filename);
  if (app_method_count > 0) {
    method_count_ += app_method_count;
    int methods = MakePipe(string("grep -c ") + "\"^VerifyError:\" " + filename);
    method_verify_stats_ += methods;
    if (methods != 0)
        ++class_verify_stats_;
  } else {
    remove(filename.c_str());
    ExecuteMaxine(class_list, classpath);
  }
}

/**
 * Execute the Dalvik verifier on the file we are retargeting.
 *
 * Executing the Dalvik verifier allows us to modify the code to make it
 * Dalvik-verifiable
 *
 * @param file_name Name of the file which should be verified.
 * @return The time taken by the Dalvik verifier.
 */
int DedLauncher::ExecuteDalvikVerifier(const std::string& file_name) {
  string cmd("bash ");
  cmd += preverify_;
  cmd += " " + i_file_name_ + " " + dclass_ + "/" + file_name
      + ".odex";
//  printf("Command: %s\n", cmd.c_str());

  int time = StartProcess(cmd, 3000);
  remove((dclass_ + "/" + file_name + ".odex").c_str());
  return time;
}

/**
 * Parse and record Dalvik annotations.
 *
 * @param file_name The name of the file containing the Dalvik annotations.
 */
void DedLauncher::ProcessDalvikVerifierAnnotations(const char* file_name) {
  int code_location_count = 0;
  set<string> classes;
  set<string> methods;

  std::ifstream in(file_name);

  while (in) {
    string method_descriptor, temp;
    // First part: class descriptor.
    in >> method_descriptor;
    if (method_descriptor == "")
      break;
    ++code_location_count;
    classes.insert(method_descriptor);
    // Second part: method name.
    in >> temp;
    method_descriptor += temp;
    // Third part: method signature. This fully qualified name is unique.
    in >> temp;
    method_descriptor += temp;
    methods.insert(method_descriptor);
  }

  in.close();

  bad_input_code_locations_ = code_location_count;
  bad_input_methods_ = methods.size();
  bad_input_classes_ = classes.size();
}

/**
 * Create a directory and all upper-level directories as necessary.
 *
 * @param path The directory to be created.
 */
void DedLauncher::CreateDir(const char* path) {
  char temp_path[1024];
  memset(temp_path, 0, 1024);
  size_t len = strlen(path);

  strncpy(temp_path, path, len);
  for (char* p = temp_path; *p != '\0'; ++p) {
    if (*p == '/') {
      *p = '\0';
      if (access(temp_path, F_OK))
        mkdir(temp_path, S_IRWXU);
      *p = '/';
    }
  }
  // If path is not terminated with /.
  if (access(temp_path, F_OK))
    mkdir(temp_path, S_IRWXU);
}

/**
 * Get a class list from file.
 *
 * @param list_file The file containing the class list.
 * @param result The output class list.
 */
void DedLauncher::GetClassList(const string& list_file,
    vector<string>& result) const {
  ifstream classes(list_file.c_str());
  while (!classes.eof()) {
    string current_class;
    classes >> current_class;
    if (current_class != "")
      result.push_back(current_class);
  }
  classes.close();
}

/**
 * Make a command to launch Soot.
 *
 * @param in_dir Input directory.
 * @param in_file File considered.
 * @param out_dir Output directory.
 * @param library Classpath.
 * @param options Extra options.
 * @return A command.
 */
string DedLauncher::MakeSootCommand(const string& in_dir,
    const string& in_file, const string& out_dir, const string& library,
    const string& options) const {
  string soot = "java ";
  
  for (int i = 0; i < (int) vm_options_.size(); ++i) {
    soot += vm_options_[i];
    soot += " ";
  }
  
  soot += "-jar ";
  soot += soot_;
//  const string soot_main = " soot.Main ";
  const string extra_options = " -pp -allow-phantom-refs -p bb.lso sll:false"
      " -p db.transformations off -p db.force-recompile off ";
  string result;

  result = soot + " -cp " + dclass_ + ":" + library + " -d "
      + out_dir + extra_options + options + in_file;
  return result;
}

/**
 * Make a pipe to get a numerical result from a shell command.
 *
 * @cmd A shell command.
 * @return A numerical result.
 */
int DedLauncher::MakePipe(const string& cmd) const {
  FILE * pfd;
  char buf[10];
  int result = 0;

  pfd = popen(cmd.c_str(), "r");
  if (pfd != NULL && fgets(buf, 10, pfd) != NULL)
    result = atoi(buf);
  pclose(pfd);
  return result;
}

/**
 * Test if a file is valid (i.e., if it exists and has non-zero size).
 *
 * @param path The path to the file to be tested.
 * @return True if the file is valid.
 */
bool DedLauncher::IsFileValid(const string& path) const {
  struct stat file_info;
  int int_stat = stat(path.c_str(), &file_info);
  return (int_stat == 0 && file_info.st_size != 0);
}

/**
 * Execute Soot on a list of classes, one at a time.
 *
 * @param classes A list of classes.
 * @param options Processing options.
 * @param directory Input directory.
 * @param library Classpath.
 * @param extension Extension of the files we are generating.
 * @param process_time The Soot processing time.
 * @return A list of classes for which processing failed.
 */
vector<string> DedLauncher::SootPerClass(const vector<string>& classes,
    const string& options, const string& directory, const string& library,
    const string& extension, int& process_time) {
  vector<string> missing_classes;

  for (int i = 0; i < (int) classes.size(); ++i) {
    string class_path = classes[i];
    replace(class_path.begin(), class_path.end(), '.', '/');
    string cmd2 = MakeSootCommand(dclass_, classes[i], directory,
        library, options);

    // Create file.
    process_time += StartProcess(cmd2, 3000);

    if (!IsFileValid(directory + "/" + class_path + extension))
      missing_classes.push_back("L" + class_path + ";");
  }

  return missing_classes;
}

/**
 * Write missing classes to text file.
 *
 * @param missing_classes A list of missing classes.
 */
void DedLauncher::WriteMissingClasses(
    const std::vector<std::string>& missing_classes) {
  ofstream out((sclass_ + "/missing_classes_0").c_str());

  for (int i = 0; i < (int) missing_classes.size(); ++i)
    out << missing_classes[i] << "\n";

  out.close();
}

/**
 * Process the classes in an Android application.
 */
void DedLauncher::ProcessClasses() {
  string output_dir0;
  output_dir0.assign(i_file_name_, 0, i_file_name_.size() - 4);
  size_t ind = output_dir0.find_last_of('/', output_dir0.npos);
  if (ind != output_dir0.npos)
    output_dir0 = output_dir0.substr(ind + 1);

  dclass_ = output_dir_ + "/retargeted/" + output_dir0;
  sclass_ = output_dir_ + "/optimized/" + output_dir0;
  dojava_ = output_dir_ + "/optimized-decompiled/" + output_dir0;
  string stubs_dir = (generate_stubs_ ?
      output_dir_ + "/stubs/" + output_dir0 : "");

  CreateDir(dclass_.c_str());
  CreateDir(sclass_.c_str());
  CreateDir(dojava_.c_str());
  if (generate_stubs_)
    CreateDir(stubs_dir.c_str());

  int verif_time = 0;
  string preverify = " ";

  if (preverify_ != NULL) {
    verif_time = ExecuteDalvikVerifier(output_dir0);
    ProcessDalvikVerifierAnnotations(
        (dclass_ + "/" + output_dir0 + ".txt").c_str());
    preverify = " -p " + dclass_ + "/" + output_dir0 + ".txt ";
  }

  string stubs_string = (generate_stubs_ ? " -s " + stubs_dir : "");
  int jasmin_time = 0;
  int opt_classes = 0;
  int dec_classes = 0;
  int opt_time = 0;
  int dec_time = 0;
  int dare_status;
  int dare_time = ExecuteDare(stubs_string + preverify, &dare_status);
  if (dare_status != 0) goto bail;
  GetClassList(dclass_ + "/classes.txt", original_class_names_);
  if (generate_stubs_)
    GetClassList(stubs_dir + "/stubs.txt", stubs_);

  if (jasmin_ != NULL) {
    printf("Assembling classes\n");
    jasmin_time = ExecuteJasmin(original_class_names_, dclass_);
    RemoveJasminFiles(original_class_names_, dclass_);
    if (generate_stubs_) {
      ExecuteJasmin(stubs_, stubs_dir);
      RemoveJasminFiles(stubs_, stubs_dir);
    }
  }

  if (maxine_ != NULL) {
    string classpath = dclass_ + ":" + libraries_ + ":" + stubs_dir;
    ExecuteMaxineGlobal(original_class_names_, classpath);
  }

  if (optimize_ && soot_ != NULL) {
    // We initiate optimizations to the files we have just generated.
    string cmd1 = MakeSootCommand(dclass_, dclass_, sclass_, libraries_  + ":" 
        + stubs_dir, " -p jb.uce remove-unreachable-traps -O -process-dir ");

    // Create optimized files.
    opt_time += StartProcess(cmd1, original_class_names_.size() * 4);
    opt_classes = MakePipe("ls -1R " + sclass_ + " | grep .*.class$ | wc -l");

    string options = " -omit-excepting-unit-edges -throw-analysis unit "
        "-dex-mode -synchronous-only ";

    if (opt_classes < (int) original_class_names_.size()) {
      vector<string> missing_classes = SootPerClass(original_class_names_,
          options + " -O ", sclass_, libraries_ + ":" + stubs_dir,
          ".class", opt_time);
      WriteMissingClasses(missing_classes);
      opt_classes = original_class_names_.size() - missing_classes.size();
    }
  }

  if (decompile_ && soot_ != NULL) {
    string cmd4 = MakeSootCommand(dclass_, dclass_, dojava_,
        libraries_ + ":" + stubs_dir, " -omit-excepting-unit-edges "
            "-throw-analysis unit -p jb.uce remove-unreachable-traps -O "
            "-f dava -process-dir ");

    // Optimize and decompile files.
    dec_time += StartProcess(cmd4, original_class_names_.size() * 4);
    dec_classes = MakePipe("ls -1R " + dojava_ + " | grep .*.java$ | wc -l");

    if (dec_classes < (int) original_class_names_.size())
      SootPerClass(original_class_names_, " -O -f dava ", dojava_,
          libraries_ + ":" + stubs_dir, ".java", dec_time);
  }

  // Figure out how many classes got decompiled.
  dec_classes = MakePipe("ls -1R " + dojava_ + " | grep .*.java$ | wc -l");

bail:
  // Write statistics to file.
  ofstream stats((output_dir_ + "/stats.csv").c_str(), ofstream::app);
  if (dare_status == 0) {
    stats << i_file_name_ << " " << original_class_names_.size() << " "
        << bad_input_code_locations_ << " " << bad_input_methods_ << " "
        << bad_input_classes_ << " " << opt_classes << " "
        << dec_classes << " "
        << method_count_ << " " << method_verify_stats_ << " "
        << class_verify_stats_ << " "
        << verif_time << " " << dare_time << " " << jasmin_time << " "
        << opt_time << " " << dec_time << "\n";
  } else {
    stats << i_file_name_ << " " << -1 << " "
        << -1 << " " << -1 << " "
        << -1 << " " << -1 << " "
        << -1 << " "
        << -1 << " " << -1 << " "
        << -1 << " "
        << -1 << " " << dare_time << " " << -1 << " "
        << -1 << " " << -1 << "\n";
  }
  stats.close();
}
