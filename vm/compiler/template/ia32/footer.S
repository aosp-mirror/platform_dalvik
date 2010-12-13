/*
 * ===========================================================================
 *  Common subroutines and data
 * ===========================================================================
 */

    .text
    .align  4
/*
 * FIXME - verify that we don't need an explicit cache flush
 * for x86.
 */
    .global cacheflush
cacheflush:
    ret


    .global dmvCompilerTemplateEnd
dmvCompilerTemplateEnd:

#endif /* WITH_JIT */
