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
 * dare_launcher.h
 */

#ifndef DARE_LAUNCHER_H_
#define DARE_LAUNCHER_H_


#include <sys/time.h>

#include <set>
#include <string>
#include <vector>


class DedLauncher {
 public:
  DedLauncher()
      : soot_(NULL),
        jas_time_(0),
        optimize_(false),
        decompile_(false),
        jasmin_(NULL),
        maxine_(NULL),
        preverify_(NULL),
        no_split_tables_(false),
        class_verify_stats_(0),
        method_verify_stats_(0),
        method_count_(0),
        bad_input_code_locations_(0),
        bad_input_methods_(0),
        bad_input_classes_(0),
        dare_(std::string("./dare-") + kVersion),
        generate_stubs_(false) {}

  /**
   * Process a dex or apk file.
   *
   * @param argc Command line argument count.
   * @param argv Command line arguments.
   */
  void Process(int argc, char** argv);

 private:
  static const char* kVersion;

  static const char* version() { return kVersion; }
  /**
   * Display usage information.
   */
  void Usage() const;
  /**
   * Calculate the time elapsed between two timestamps.
   *
   * @param t1 First timestamp.
   * @param t2 Second timestamp.
   * @return The time elapsed between the two timestamps (in microseconds).
   */
  int CalcTime(struct timeval t1, struct timeval t2) const;
  /**
   * Handle an alarm signal by killing the child process.
   *
   * @param sig The signal number.
   */
  static void Handler(int sig);
  /**
   * Split a command string into an array of C strings.
   *
   * @param cmd The input command.
   * @return An array of C strings.
   */
  char** GetCmd(std::string cmd) const;
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
  int StartProcess(const std::string& cmd, int max_time,
      const char* filename = NULL) const;
  /**
   * Execute the Dalvik retargeting process.
   *
   * @param options Extra options.
   * @return The time taken by retargeting.
   */
  int ExecuteDare(const std::string& options) const;
  /**
   * Execute Jasmin for bytecode assembly.
   *
   * @param class_list The list of classes which should be assembled.
   * @param directory The directory where the Jasmin files are located.
   * @return The time taken by bytecode assembly.
   */
  int ExecuteJasmin(const std::vector<std::string>& class_list,
      const std::string& directory) const;
  /**
   * Execute Maxine for Java bytecode verification.
   *
   * @param class_list The list of classes to verify.
   * @param classpath The classpath for Maxine.
   */
  void ExecuteMaxine(const std::vector<std::string>& class_list,
      std::string classpath);
  /**
   * Execute Maxine per application.
   *
   * @param class_list The list of classes to verify.
   * @param classpath The Maxine classpath.
   */
  void ExecuteMaxineGlobal(const std::vector<std::string>& class_list,
      std::string classpath);
  /**
   * Execute the Dalvik verifier on the file we are retargeting.
   *
   * Executing the Dalvik verifier allows us to modify the code to make it
   * Dalvik-verifiable
   *
   * @param file_name Name of the file which should be verified.
   * @return The time taken by the Dalvik verifier.
   */
  int ExecuteDalvikVerifier(const std::string& file_name);
  /**
   * Parse and record Dalvik annotations.
   *
   * @param file_name The name of the file containing the Dalvik annotations.
   */
  void ProcessDalvikVerifierAnnotations(const char* file_name);
  /**
   * Create a directory and all upper-level directories as necessary.
   *
   * @param path The directory to be created.
   */
  void CreateDir(const char* path);
  /**
   * Get a class list from file.
   *
   * @param list_file The file containing the class list.
   * @param result The output class list.
   */
  void GetClassList(const std::string& list_file,
      std::vector<std::string>& result) const;
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
  std::string MakeSootCommand(const std::string& in_dir,
      const std::string& in_file, const std::string& out_dir,
      const std::string& library, const std::string& options) const;
  /**
   * Make a pipe to get a numerical result from a shell command.
   *
   * @cmd A shell command.
   * @return A numerical result.
   */
  int MakePipe(const std::string& cmd) const;
  /**
   * Test if a file is valid (i.e., if it exists and has non-zero size).
   *
   * @param path The path to the file to be tested.
   * @return True if the file is valid.
   */
  bool IsFileValid(const std::string& path) const;
  /**
   * Execute Soot on a list of class, one at a time.
   *
   * @param classes A list of classes.
   * @param options Processing options.
   * @param directory Input directory.
   * @param library Classpath.
   * @param extension Extension of the files we are generating.
   * @param process_time The Soot processing time.
   * @return A list of classes for which processing failed.
   */
  std::vector<std::string> SootPerClass(
      const std::vector<std::string>& classes, const std::string& options,
      const std::string& directory, const std::string& library,
      const std::string& extension, int& process_time);
  /**
   * Write missing classes to text file.
   *
   * @param missing_classes A list of missing classes.
   */
  void WriteMissingClasses(
      const std::vector<std::string>& missing_classes);
  /**
   * Process the classes in an Android application.
   */
  void ProcessClasses();

  std::string i_file_name_;
  std::string output_dir_;
  std::string dclass_;
  std::string sclass_;
  std::string dojava_;
  static pid_t ch_pid_;
  const char* soot_;
  int jas_time_;
  bool optimize_;
  bool decompile_;
  const char* jasmin_;
  const char* maxine_;
  const char* preverify_;
  bool no_split_tables_;
  std::string libraries_;
  int class_verify_stats_;
  int method_verify_stats_;
  int method_count_;
  std::vector<std::string> original_class_names_;
  std::vector<std::string> stubs_;
  int bad_input_code_locations_;
  int bad_input_methods_;
  int bad_input_classes_;
  std::string dare_;
  bool generate_stubs_;
};

#endif /* DARE_LAUNCHER_H_ */
