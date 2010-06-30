The codegen file for the ARM-based JIT is composed by files broken by
functionality hierarchies. The goal is to separate architectural dependent
and independent components to facilitate maintenance and future extension.

For example, the codegen file for armv7-a is assembled by the following
components:

--

/* Architectural independent building blocks */
#include "../CodegenCommon.c"

/* Thumb2-specific factory utilities */
#include "../Thumb2/Factory.c"
/* Factory utilities dependent on arch-specific features */
#include "../CodegenFactory.c"

/* Thumb2-specific codegen routines */
#include "../Thumb2/Gen.c"
/* Thumb2+VFP codegen routines */
#include "../FP/Thumb2VFP.c"

/* Thumb2-specific register allocation */
#include "../Thumb2/Ralloc.c"

/* MIR2LIR dispatcher and architectural independent codegen routines */
#include "../CodegenDriver.c"

/* Architecture manifest */
#include "ArchVariant.c"

--

For the Thumb/Thumb2 directories, each contain the followin three files:

- Factory.c (low-level routines for instruction selections)
- Gen.c     (invoke the ISA-specific instruction selection routines)
- Ralloc.c  (arch-dependent register pools)

The FP directory contains FP-specific codegen routines depending on
Thumb/Thumb2/VFP/PortableFP:

- Thumb2VFP.c
- ThumbVFP.c
- ThumbPortableFP.c

In this way the dependency between generic and specific code tied to
particular architectures can be explicitly represented.
