// Copyright 2008 Google Inc. All Rights Reserved.

/**
 * Common base class.
 */
public class Base {
    public Base() {}

    public DoubledExtend getExtended() {
        return new DoubledExtend();
    }

    public static String doStuff(DoubledExtend dt) {
        return dt.getStr();
    }
}

