#define INTERP_FUNC_NAME dvmInterpretDbg
#define INTERP_TYPE INTERP_DBG

#define CHECK_DEBUG_AND_PROF() \
    checkDebugAndProf(pc, fp, self, curMethod, &debugIsMethodEntry)

#if defined(WITH_JIT)
#define CHECK_JIT() (dvmCheckJit(pc, self, interpState))
#else
#define CHECK_JIT() (0)
#endif
