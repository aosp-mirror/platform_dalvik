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
 * body.h
 *
 * Base class for Body objects.
 */

#ifndef TYDE_BODY_H_
#define TYDE_BODY_H_


#include <vector>

template <typename T>
class Body {
 public:
  T& operator[](int index) { return instructions_[index]; }
  const T& operator[](int index) const { return instructions_[index]; }
  const T& back() const { return instructions_.back(); }
  void push_back(const T& element) { instructions_.push_back(element); }
  void push_front(const T& element) {
    instructions_.insert(instructions_.begin(), element);
  }
  void pop_front() { instructions_.erase(instructions_.begin()); }
  void pop_back() { instructions_.pop_back(); }
  void insert(int index, const std::vector<T>& contents) {
    instructions_.insert(instructions_.begin() + index, contents.begin(),
        contents.end());
  }
  size_t size() const { return instructions_.size(); }

 private:
  std::vector<T> instructions_;
};

#endif /* TYDE_BODY_H_ */
