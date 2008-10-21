// Copyright 2007 The Android Open Source Project

/**
 * Abstract base class.
 */
public abstract class AbstractBase {
    public void doStuff() {
        System.out.println("In AbstractBase.doStuff (src2)");
        redefineMe();
    }

    public abstract void redefineMe();
}

