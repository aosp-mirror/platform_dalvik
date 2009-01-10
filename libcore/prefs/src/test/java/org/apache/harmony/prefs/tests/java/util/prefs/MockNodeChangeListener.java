package org.apache.harmony.prefs.tests.java.util.prefs;

import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

public class MockNodeChangeListener implements NodeChangeListener {
    private Object addLock = new Object();

    private Object removeLock = new Object();

    private int added = 0;

    private int removed = 0;

    private int testNum = 0;

    public static final int TEST_GET_CHILD = 1;

    public static final int TEST_GET_PARENT = 2;

    boolean addResult = false;

    boolean removeResult = false;

    public MockNodeChangeListener(int test) {
        testNum = test;
    }

    public MockNodeChangeListener() {

    }
    
    public void waitForEvent() {
        try {
            synchronized (addLock) {
                addLock.wait(500);
            }
        } catch (InterruptedException e) {
        }
    }

    public void childAdded(NodeChangeEvent e) {

        synchronized (addLock) {
            switch (testNum) {
            case TEST_GET_CHILD:
                Preferences child = e.getChild();
                if (child == null) {
                    addResult = false;
                } else {
                    if (child.name() == "mock1") {
                        addResult = true;
                    }
                }
                break;
            case TEST_GET_PARENT:
                Preferences parent = e.getParent();
                if (parent == null) {
                    addResult = false;
                } else {
                    if (parent.name() == "mock") {
                        addResult = true;
                    }
                }

                break;
            }
            ++added;
            addLock.notifyAll();
        }
    }

    public void childRemoved(NodeChangeEvent e) {
        synchronized (removeLock) {
            switch (testNum) {
            case TEST_GET_CHILD:
                Preferences child = e.getChild();
                if (child == null) {
                    removeResult = false;
                } else {
                    if (child.name() == "mock1") {
                        removeResult = true;
                    }
                }
                break;
            case TEST_GET_PARENT:
                Preferences parent = e.getParent();
                if (parent == null) {
                    addResult = false;
                } else {
                    if (parent.name() == "mock") {
                        addResult = true;
                    }
                }

                break;
            }
            removed++;
            removeLock.notifyAll();
        }
    }

    public boolean getAddResult() {
        synchronized (addLock) {
            return addResult;
        }
    }

    public boolean getRemoveResult() {
        synchronized (removeLock) {
            return removeResult;
        }
    }

    public int getAdded() {
        synchronized (addLock) {
            return added;
        }
    }

    public int getRemoved() {
        synchronized (removeLock) {
            return removed;
        }
    }

    public void reset() {
        added = 0;
        removed = 0;
    }
}
