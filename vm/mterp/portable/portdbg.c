#define INTERP_FUNC_NAME dvmInterpretDbg
#define INTERP_TYPE INTERP_DBG

#define CHECK_DEBUG_AND_PROF() \
    checkDebugAndProf(pc, fp, self, curMethod, &debugIsMethodEntry)
