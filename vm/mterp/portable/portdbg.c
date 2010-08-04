#define INTERP_FUNC_NAME dvmInterpretDbg
#define INTERP_TYPE INTERP_DBG

#define CHECK_DEBUG_AND_PROF() \
    checkDebugAndProf(pc, fp, self, curMethod, &debugIsMethodEntry)

#if defined(WITH_JIT)
#define CHECK_JIT_BOOL() (dvmCheckJit(pc, self, interpState, callsiteClass,\
                          methodToCall))
#define CHECK_JIT_VOID() (dvmCheckJit(pc, self, interpState, callsiteClass,\
                          methodToCall))
#define ABORT_JIT_TSELECT() (dvmJitAbortTraceSelect(interpState))
#else
#define CHECK_JIT_BOOL() (false)
#define CHECK_JIT_VOID()
#define ABORT_JIT_TSELECT(x) ((void)0)
#endif
