// Copyright 2008 Google Inc. All Rights Reserved.

/**
 * Public class that can't access its interface.
 */
public class Inaccessible3 implements InaccessibleInterface {
    public Inaccessible3() {
        System.out.println("--- inaccessible3");
    }
}

