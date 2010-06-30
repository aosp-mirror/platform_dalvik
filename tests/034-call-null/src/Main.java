// Copyright 2008 The Android Open Source Project

public class Main {
    int mFoo = 27;

    private void doStuff() {
        System.out.println("mFoo is " + mFoo);
    }

    public static void main(String[] args) {
        Main instance = null;
        instance.doStuff();
    }
}
