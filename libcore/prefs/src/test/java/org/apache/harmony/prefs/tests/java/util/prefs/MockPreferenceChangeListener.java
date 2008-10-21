package org.apache.harmony.prefs.tests.java.util.prefs;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class MockPreferenceChangeListener implements PreferenceChangeListener {
	private Object lock = new Object();

	private int changed = 0;

	private boolean addDispatched = false;
	
	public static final int TEST_GET_KEY = 1;

	public static final int TEST_GET_NEW_VALUE = 2;
	
	public static final int TEST_GET_NODE = 3;
	
	boolean result = false;
	
	int testNum = 0;
	
	
	public MockPreferenceChangeListener() {
		
	}
	
	public MockPreferenceChangeListener(int test) {
		testNum = test;
	}

	// private Object lock = new Object();

	public void preferenceChange(PreferenceChangeEvent pce) {
		synchronized (lock) {
			switch(testNum) {
				case TEST_GET_KEY:
					if(pce != null) {
						if(pce.getKey().equals("key_int")) {
							result = true;
						}
					} 
					break;
				case TEST_GET_NEW_VALUE:
					if(pce != null) {
						if(pce.getNewValue().equals(new Integer(Integer.MAX_VALUE).toString())) {
							result = true;
						}
					} 					
					break;
				case TEST_GET_NODE:
					if(pce != null) {
						if("mock".equals(pce.getNode().name())) {
							result = true;
						}
					} 					
	
					break;
			}
			changed++;
			addDispatched = true;
			lock.notifyAll();
		}
	}

	public boolean getResult() {
		synchronized (lock) {

			if (!addDispatched) {
				try {
					// TODO: don't know why must add limitation
					lock.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			addDispatched = false;
			return result;
		}
	}
	
	public int getChanged() {
		synchronized (lock) {

			if (!addDispatched) {
				try {
					// TODO: don't know why must add limitation
					lock.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			addDispatched = false;
			return changed;
		}
	}

	public void reset() {
		changed = 0;
		result = false;
	}
}
