#define INTERP_FUNC_NAME dvmInterpretDbg
#define INTERP_TYPE INTERP_DBG

#define CHECK_DEBUG_AND_PROF() \
    checkDebugAndProf(pc, fp, self, curMethod, &debugIsMethodEntry)

#if defined(WITH_JIT)
#define CHECK_JIT_BOOL() (dvmCheckJit(pc, self, callsiteClass,\
                          methodToCall))
#define CHECK_JIT_VOID() (dvmCheckJit(pc, self, callsiteClass,\
                          methodToCall))
#define END_JIT_TSELECT() (dvmJitEndTraceSelect(self))
#else
#define CHECK_JIT_BOOL() (false)
#define CHECK_JIT_VOID()
#define END_JIT_TSELECT(x) ((void)0)
#endif
