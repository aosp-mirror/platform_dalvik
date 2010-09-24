/*
 * ===========================================================================
 *  Common subroutines and data
 * ===========================================================================
 */

    .text
    .align  4
/*
 * FIXME - need a cacheflush for x86
 */
    .global cacheflush
cacheflush:
    movl  $$0xdeadf0f0, %eax
    call *%eax


    .global dmvCompilerTemplateEnd
dmvCompilerTemplateEnd:

#endif /* WITH_JIT */
