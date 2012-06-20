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

/*
 * timer.h
 *
 * A timer class.
 */

#ifndef TIMER_H_
#define TIMER_H_


#include <sys/time.h>

#include <fstream>


class Timer {
 public:
  enum Phase {
    kParsing = 0,
    kCfg,
    kTypeInferenceInitialization,
    kConstraintGeneration,
    kConstraintSolution,
    kTranslationPreprocessing,
    kTranslationToJasmin,
    kCodeTranslationToJasmin,
    kOutputToFile,
    kOther,
    kPhaseCount,
  };

  static Timer& getInstance() {
    static Timer instance;
    return instance;
  }
  void Init(std::ifstream& file);
  void Start(Phase phase);
  void End();
  Phase phase() { return phase_; }
  void WriteToFile(std::ofstream& out, const char* file_name);
 private:
  Timer() {}
  Timer(const Timer& t);
  void operator=(const Timer& t);

  timeval start_;
  int times_[kPhaseCount];
  Phase phase_;
};

#endif /* TIMER_H_ */
