#define INTERP_FUNC_NAME dvmInterpretDbg
#define INTERP_TYPE INTERP_DBG

#define CHECK_DEBUG_AND_PROF() \
    checkDebugAndProf(pc, fp, self, curMethod, &debugIsMethodEntry)

#if defined(WITH_JIT)
#define CHECK_JIT() \
    if (dvmCheckJit(pc, self, interpState)) GOTO_bail_switch()
#else
#define CHECK_JIT() \
    ((void)0)
#endif
