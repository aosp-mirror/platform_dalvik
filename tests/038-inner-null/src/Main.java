// Copyright 2008 The Android Open Source Project

public class Main {
    public static void main(String[] args) {
        Special special = new Special();
        special.callInner();
    }

    public static class Special {
        Blort mBlort = null;

        Special() {
            System.out.println("new Special()");
        }

        public void callInner() {
            mBlort.repaint();
        }
    }

    private class Blort {
        public void repaint() {
            System.out.println("shouldn't see this");
        }
    }

}
