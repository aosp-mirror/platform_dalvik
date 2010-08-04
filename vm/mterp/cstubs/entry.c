/*
 * Handler function table, one entry per opcode.
 */
#undef H
#define H(_op) dvmMterp_##_op
DEFINE_GOTO_TABLE(gDvmMterpHandlers)

#undef H
#define H(_op) #_op
DEFINE_GOTO_TABLE(gDvmMterpHandlerNames)

#include <setjmp.h>

/*
 * C mterp entry point.  This just calls the various C fallbacks, making
 * this a slow but portable interpeter.
 *
 * This is only used for the "allstubs" variant.
 */
bool dvmMterpStdRun(MterpGlue* glue)
{
    jmp_buf jmpBuf;
    int changeInterp;

    glue->bailPtr = &jmpBuf;

    /*
     * We want to return "changeInterp" as a boolean, but we can't return
     * zero through longjmp, so we return (boolean+1).
     */
    changeInterp = setjmp(jmpBuf) -1;
    if (changeInterp >= 0) {
        Thread* threadSelf = dvmThreadSelf();
        LOGVV("mterp threadid=%d returning %d\n",
            threadSelf->threadId, changeInterp);
        return changeInterp;
    }

    /*
     * We may not be starting at a point where we're executing instructions.
     * We need to pick up where the other interpreter left off.
     *
     * In some cases we need to call into a throw/return handler which
     * will do some processing and then either return to us (updating "glue")
     * or longjmp back out.
     */
    switch (glue->entryPoint) {
    case kInterpEntryInstr:
        /* just start at the start */
        break;
    case kInterpEntryReturn:
        dvmMterp_returnFromMethod(glue);
        break;
    case kInterpEntryThrow:
        dvmMterp_exceptionThrown(glue);
        break;
    default:
        dvmAbort();
    }

    /* run until somebody longjmp()s out */
    while (true) {
        typedef void (*Handler)(MterpGlue* glue);

        u2 inst = /*glue->*/pc[0];
        Handler handler = (Handler) gDvmMterpHandlers[inst & 0xff];
        LOGVV("handler %p %s\n",
            handler, (const char*) gDvmMterpHandlerNames[inst & 0xff]);
        (*handler)(glue);
    }
}

/*
 * C mterp exit point.  Call here to bail out of the interpreter.
 */
void dvmMterpStdBail(MterpGlue* glue, bool changeInterp)
{
    jmp_buf* pJmpBuf = glue->bailPtr;
    longjmp(*pJmpBuf, ((int)changeInterp)+1);
}
