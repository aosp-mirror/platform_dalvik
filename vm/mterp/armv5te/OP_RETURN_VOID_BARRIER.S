%verify "executed"
    SMP_DMB                             @ TODO: want "DMB ST"
    b       common_returnFromMethod
