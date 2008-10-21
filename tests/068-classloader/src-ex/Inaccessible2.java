// Copyright 2008 Google Inc. All Rights Reserved.

/**
 * Public class that can't access its base.
 */
public class Inaccessible2 extends InaccessibleBase {
    public Inaccessible2() {
        System.out.println("--- inaccessible2");
    }
}

