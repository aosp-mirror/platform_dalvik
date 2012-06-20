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
 * timer.cpp
 *
 * A timer class.
 */

#include "timer.h"

#include <string>

using std::string;


void Timer::Init(std::ifstream& file) {
  if (!file.fail() && !file.eof()) {
    string current;
    file >> current;

    if (current != "") {
      for (int i = 0; !file.eof(); ++i) {
        int current;
        file >> current;
        times_[i] = current;
      }
      return;
    }
  }

  for (int i = 0; i < kPhaseCount; ++i)
    times_[i] = 0;
}

void Timer::Start(Phase phase) {
  phase_ = phase;
  gettimeofday(&start_, NULL);
}

void Timer::End() {
  timeval end;
  gettimeofday(&end, NULL);
  int difference = 1000000 * (end.tv_sec - start_.tv_sec) + (end.tv_usec - start_.tv_usec);
  times_[phase_] += difference;
}

void Timer::WriteToFile(std::ofstream& out, const char* file_name) {
  string file_name_string(file_name);
  file_name_string = file_name_string.substr(file_name_string.rfind('/') + 1);
  out << file_name_string;

  for (int i = 0; i < kPhaseCount; ++i)
    out << " " << times_[i];
}
