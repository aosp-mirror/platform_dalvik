// Copyright 2008 Google Inc. All Rights Reserved.

/**
 * Non-public class, inaccessible from Main.  Note the constructor is
 * public.
 */
class Inaccessible1 extends Base {
    public Inaccessible1() {
        System.out.println("--- inaccessible1");
    }
}

