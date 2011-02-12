/*--- end of opcodes ---*/

#ifndef THREADED_INTERP
        } // end of "switch"
    } // end of "while"
#endif

bail:
    ILOGD("|-- Leaving interpreter loop");      // note "curMethod" may be NULL

    self->retval = retval;
    return false;

bail_switch:
    /*
     * The standard interpreter currently doesn't set or care about the
     * "debugIsMethodEntry" value, so setting this is only of use if we're
     * switching between two "debug" interpreters, which we never do.
     *
     * TODO: figure out if preserving this makes any sense.
     */
#if INTERP_TYPE == INTERP_DBG
    self->debugIsMethodEntry = debugIsMethodEntry;
#else
    self->debugIsMethodEntry = false;
#endif

    /* export state changes */
    self->interpSave.method = curMethod;
    self->interpSave.pc = pc;
    self->interpSave.fp = fp;
    /* debugTrackedRefStart doesn't change */
    self->retval = retval;   /* need for _entryPoint=ret */
    self->nextMode =
        (INTERP_TYPE == INTERP_STD) ? INTERP_DBG : INTERP_STD;
    LOGVV(" meth='%s.%s' pc=0x%x fp=%p\n",
        curMethod->clazz->descriptor, curMethod->name,
        pc - curMethod->insns, fp);
    return true;
}
