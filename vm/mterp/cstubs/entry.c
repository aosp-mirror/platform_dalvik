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
bool dvmMterpStdRun(Thread* self)
{
    jmp_buf jmpBuf;
    int changeInterp;

    self->bailPtr = &jmpBuf;

    /*
     * We want to return "changeInterp" as a boolean, but we can't return
     * zero through longjmp, so we return (boolean+1).
     */
    changeInterp = setjmp(jmpBuf) -1;
    if (changeInterp >= 0) {
        LOGVV("mterp threadid=%d returning %d\n",
            dvmThreadSelf()->threadId, changeInterp);
        return changeInterp;
    }

    /*
     * We may not be starting at a point where we're executing instructions.
     * We need to pick up where the other interpreter left off.
     *
     * In some cases we need to call into a throw/return handler which
     * will do some processing and then either return to us (updating "self")
     * or longjmp back out.
     */
    switch (self->entryPoint) {
    case kInterpEntryInstr:
        /* just start at the start */
        break;
    case kInterpEntryReturn:
        dvmMterp_returnFromMethod(self);
        break;
    case kInterpEntryThrow:
        dvmMterp_exceptionThrown(self);
        break;
    default:
        dvmAbort();
    }

    /* run until somebody longjmp()s out */
    while (true) {
        typedef void (*Handler)(Thread* self);

        u2 inst = /*self->*/pc[0];
        Handler handler = (Handler) gDvmMterpHandlers[inst & 0xff];
        (void) gDvmMterpHandlerNames;   /* avoid gcc "defined but not used" */
        LOGVV("handler %p %s\n",
            handler, (const char*) gDvmMterpHandlerNames[inst & 0xff]);
        (*handler)(self);
    }
}

/*
 * C mterp exit point.  Call here to bail out of the interpreter.
 */
void dvmMterpStdBail(Thread* self, bool changeInterp)
{
    jmp_buf* pJmpBuf = self->bailPtr;
    longjmp(*pJmpBuf, ((int)changeInterp)+1);
}
