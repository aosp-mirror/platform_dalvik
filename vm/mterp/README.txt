Dalvik "mterp" README

NOTE: Find rebuilding instructions at the bottom of this file.


==== Overview ====

This is the source code for the Dalvik interpreter.  The core of the
original version was implemented as a single C function, but to improve
performance we rewrote it in assembly.  To make this and future assembly
ports easier and less error-prone, we used a modular approach that allows
development of platform-specific code one opcode at a time.

The original all-in-one-function C version still exists as the "portable"
interpreter, and is generated using the same sources and tools that
generate the platform-specific versions.  One form of the portable
interpreter includes support for profiling and debugging features, and
is included even if we have a platform-optimized implementation.

Every configuration has a "config-*" file that controls how the sources
are generated.  The sources are written into the "out" directory, where
they are picked up by the Android build system.

The best way to become familiar with the interpreter is to look at the
generated files in the "out" directory, such as out/InterpC-portstd.c,
rather than trying to look at the various component pieces in (say)
armv5te.


==== Platform-specific source generation ====

The architecture-specific config files determine what goes into two
generated output files (InterpC-<arch>.c, InterpAsm-<arch>.S).  The goal is
to make it easy to swap C and assembly sources during initial development
and testing, and to provide a way to use architecture-specific versions of
some operations (e.g. making use of PLD instructions on ARMv6 or avoiding
CLZ on ARMv4T).

Two basic assumptions are made about the operation of the interpreter:

 - The assembly version uses fixed-size areas for each instruction
   (e.g. 64 bytes).  "Overflow" code is tacked on to the end.
 - When a C implementation is desired, the assembly version packs all
   local state into a "glue" struct, and passes that into the C function.
   Updates to the state are pulled out of the "glue" on return.

The "arch" value should indicate an architecture family with common
programming characteristics, so "armv5te" would work for all ARMv5TE CPUs,
but might not be backward- or forward-compatible.  (We *might* want to
specify the ABI model as well, e.g. "armv5te-eabi", but currently that adds
verbosity without value.)


==== Config file format ====

The config files are parsed from top to bottom.  Each line in the file
may be blank, hold a comment (line starts with '#'), or be a command.

The commands are:

  handler-size <bytes>

    Specify the size of the assembly region, in bytes.  On most platforms
    this will need to be a power of 2.

  import <filename>

    The specified file is included immediately, in its entirety.  No
    substitutions are performed.  ".c" and ".h" files are copied to the
    C output, ".S" files are copied to the asm output.

  asm-stub <filename>

    The named file will be included whenever an assembly "stub" is needed.
    Text substitution is performed on the opcode name.

  op-start <directory>

    Indicates the start of the opcode list.  Must precede any "op"
    commands.  The specified directory is the default location to pull
    instruction files from.

  op <opcode> <directory>

    Can only appear after "op-start" and before "op-end".  Overrides the
    default source file location of the specified opcode.  The opcode
    definition will come from the specified file, e.g. "op OP_NOP armv5te"
    will load from "armv5te/OP_NOP.S".  A substitution dictionary will be
    applied (see below).

  op-end

    Indicates the end of the opcode list.  All 256 opcodes are emitted
    when this is seen, followed by any code that didn't fit inside the
    fixed-size instruction handler space.


The order of "op" directives is not significant; the generation tool will
extract ordering info from the VM sources.

Typically the form in which most opcodes currently exist is used in
the "op-start" directive.  For a new port you would start with "c",
and add architecture-specific "op" entries as you write instructions.
When complete it will default to the target architecture, and you insert
"c" ops to stub out platform-specific code.

For the <directory> specified in the "op" command, the "c" directory
is special in two ways: (1) the sources are assumed to be C code, and
will be inserted into the generated C file; (2) when a C implementation
is emitted, a "glue stub" is emitted in the assembly source file.
(The generator script always emits 256 assembly instructions, unless
"asm-stub" was left blank, in which case it only emits some labels.)


==== Instruction file format ====

The assembly instruction files are simply fragments of assembly sources.
The starting label will be provided by the generation tool, as will
declarations for the segment type and alignment.  The expected target
assembler is GNU "as", but others will work (may require fiddling with
some of the pseudo-ops emitted by the generation tool).

The C files do a bunch of fancy things with macros in an attempt to share
code with the portable interpreter.  (This is expected to be reduced in
the future.)

A substitution dictionary is applied to all opcode fragments as they are
appended to the output.  Substitutions can look like "$value" or "${value}".

The dictionary always includes:

  $opcode - opcode name, e.g. "OP_NOP"
  $opnum - opcode number, e.g. 0 for OP_NOP
  $handler_size_bytes - max size of an instruction handler, in bytes
  $handler_size_bits - max size of an instruction handler, log 2

Both C and assembly sources will be passed through the C pre-processor,
so you can take advantage of C-style comments and preprocessor directives
like "#define".

Some generator operations are available.

  %include "filename" [subst-dict]

    Includes the file, which should look like "armv5te/OP_NOP.S".  You can
    specify values for the substitution dictionary, using standard Python
    syntax.  For example, this:
      %include "armv5te/unop.S" {"result":"r1"}
    would insert "armv5te/unop.S" at the current file position, replacing
    occurrences of "$result" with "r1".

  %default <subst-dict>

    Specify default substitution dictionary values, using standard Python
    syntax.  Useful if you want to have a "base" version and variants.

  %break

    Identifies the split between the main portion of the instruction
    handler (which must fit in "handler-size" bytes) and the "sister"
    code, which is appended to the end of the instruction handler block.

  %verify "message"

    Leave a note to yourself about what needs to be tested.  (This may
    turn into something more interesting someday; for now, it just gets
    stripped out before the output is generated.)

The generation tool does *not* print a warning if your instructions
exceed "handler-size", but the VM will abort on startup if it detects an
oversized handler.  On architectures with fixed-width instructions this
is easy to work with, on others this you will need to count bytes.


==== Using C constants from assembly sources ====

The file "common/asm-constants.h" has some definitions for constant
values, structure sizes, and struct member offsets.  The format is fairly
restricted, as simple macros are used to massage it for use with both C
(where it is verified) and assembly (where the definitions are used).

If a constant in the file becomes out of sync, the VM will log an error
message and abort during startup.


==== Development tips ====

If you need to debug the initial piece of an opcode handler, and your
debug code expands it beyond the handler size limit, you can insert a
generic header at the top:

    b       ${opcode}_start
%break
${opcode}_start:

If you already have a %break, it's okay to leave it in place -- the second
%break is ignored.


==== Rebuilding ====

If you change any of the source file fragments, you need to rebuild the
combined source files in the "out" directory.  Make sure the files in
"out" are editable, then:

    $ cd mterp
    $ ./rebuild.sh

As of this writing, this requires Python 2.5. You may see inscrutible
error messages or just general failure if you have a different version
of Python installed.

The ultimate goal is to have the build system generate the necessary
output files without requiring this separate step, but we're not yet
ready to require Python in the build.
