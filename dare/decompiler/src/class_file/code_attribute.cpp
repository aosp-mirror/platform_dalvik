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
 * code_attribute.cpp
 *
 * CodeAttribute contains a code attribute.
 */

#include "class_file/code_attribute.h"

#include "class_file/constant_pool.h"
#include "tyde/cfg_builder.h"
#include "tyde/translator.h"
#include "typing/type_solver.h"
#include "timer.h"


CodeAttribute::~CodeAttribute() {
  for (int i = 0; i < (int) tyde_body().size(); ++i)
    delete tyde_body()[i];
  for (int i = 0; i < (int) attributes_.size(); ++i)
    delete attributes_[i];
}


void CodeAttribute::Tydify(ConstantPool& cp) {
  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kCfg);
  CFGBuilder::BuildCFG(this, cp);
  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kTypeInferenceInitialization);

  // Type registers.
  TypeSolver type_solver(tyde_body(), dex_code_->registersSize);
  type_solver.SolveTypes(method_);
  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kTranslationPreprocessing);

  // Create floats, ints, doubles, longs.
  cp.GetNumericalConstantsUsed(tyde_body());
}

void CodeAttribute::Dump(std::ostream& out) const {
  tyde_body().Dump(out);
  out << "\n\n";
}

void CodeAttribute::WriteToJasmin(std::ostream& out) {
  Translator converter(this, out);
  Timer::getInstance().End();
  Timer::getInstance().Start(Timer::kCodeTranslationToJasmin);
  converter.Convert();
}
