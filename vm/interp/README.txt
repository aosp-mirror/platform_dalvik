Dalvik interpreter, "portable" version.

The interpreter is built twice, once with debugging/profiling support,
once without.  The "standard" version is much smaller than the "debug"
version, and does less work per instruction, yielding a significant
performance improvement.

See the "mterp" directory for the non-portable version.

TODO: combine old and new interpreters into a single source base.

