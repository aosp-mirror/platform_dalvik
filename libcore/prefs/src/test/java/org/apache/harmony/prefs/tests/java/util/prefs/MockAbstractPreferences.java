/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.prefs.tests.java.util.prefs;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class MockAbstractPreferences extends AbstractPreferences {
	static final int NORMAL = 0;

	static final int backingException = 1;

	static final int runtimeException = 2;

	static final int returnNull = 3;

	int result = NORMAL;

	Properties attr = new Properties();

	Map<String, MockAbstractPreferences> childs = new HashMap<String, MockAbstractPreferences>();

	private int flushedTimes;

	private int syncTimes;

	protected MockAbstractPreferences(AbstractPreferences parent, String name) {
		this(parent, name, false);

	}

	protected MockAbstractPreferences(AbstractPreferences parent, String name,
			boolean newNode) {
		super(parent, name);
		super.newNode = newNode;
		if (parent instanceof MockAbstractPreferences) {
			((MockAbstractPreferences) parent).addChild(this);
		}
	}

	public int getFlushedTimes() {
		return flushedTimes;
	}

	public void resetFlushedTimes() {
		flushedTimes = 0;
	}

	public int getSyncTimes() {
		return syncTimes;
	}

	public void resetSyncTimes() {
		syncTimes = 0;
	}

	private void addChild(MockAbstractPreferences c) {
		childs.put(c.name(), c);
	}

	public void setResult(int r) {
		result = r;
	}

	public Object lock() {
		return lock;
	}

	public String[] childrenNamesSpi() throws BackingStoreException {
		checkException();
		if (result == returnNull)
			return null;
		String[] r = new String[childs.size()];
		childs.keySet().toArray(r);
		return r;
	}

	private void checkException() throws BackingStoreException {
		switch (result) {
		case NORMAL:
			return;
		case backingException:
			throw new BackingStoreException("test");
		case runtimeException:
			throw new MockRuntimeException("test");
		}
	}

	public AbstractPreferences publicChildSpi(String name) {
		return childSpi(name);
	}

	public AbstractPreferences childSpi(String name) {
		try {
			checkException();
		} catch (BackingStoreException e) {
		}
		if (result == returnNull)
			return null;
		AbstractPreferences r = childs.get(name);
		if (r == null) {
			r = new MockAbstractPreferences(this, name, true);

		}
		return r;
	}

	public void flushSpi() throws BackingStoreException {
		checkException();
		flushedTimes++;
	}

	public String getSpi(String key) {
		try {
			checkException();
		} catch (BackingStoreException e) {
		}
		if (null == key) {
			return null;
		}
		return result == returnNull ? null : attr.getProperty(key);
	}

	public String[] keysSpi() throws BackingStoreException {
		checkException();
		Set<Object> keys = attr.keySet();
		String[] results = new String[keys.size()];
		keys.toArray(results);
		return result == returnNull ? null : results;
	}

	public void putSpi(String name, String value) {
		try {
			checkException();
		} catch (BackingStoreException e) {
		}
		if (name == null || value == null) {
			return;
		}
		attr.put(name, value);
	}

	protected void removeNodeSpi() throws BackingStoreException {
		checkException();
		((MockAbstractPreferences) parent()).childs.remove(name());
	}

	public void removeSpi(String key) {
		try {
			checkException();
		} catch (BackingStoreException e) {
		}
		if (null == key) {
			return;
		}
		attr.remove(key);
	}

	public void syncSpi() throws BackingStoreException {
		checkException();
		syncTimes++;
	}

	public boolean getNewNode() {
		return newNode;
	}

	public Object getLock() {
		return lock;
	}

	public void protectedAbstractMethod() {
		try {
			childrenNamesSpi();
		} catch (BackingStoreException e) {
		}
		childSpi("mock");
		try {
			flushSpi();
		} catch (BackingStoreException e1) {
		}
		getSpi(null);
		isRemoved();
		try {
			keysSpi();
		} catch (BackingStoreException e2) {
		}
		putSpi(null, null);
		try {
			removeNodeSpi();
		} catch (BackingStoreException e3) {
		}
		removeSpi(null);
		try {
			syncSpi();
		} catch (BackingStoreException e4) {
		}
	}

	public boolean isRemovedImpl() {
		return super.isRemoved();
	}

	public AbstractPreferences getChildImpl(String name)
			throws BackingStoreException {
		return super.getChild(name);
	}

	public AbstractPreferences[] cachedChildrenImpl() {
		return super.cachedChildren();
	}

}

class MockRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MockRuntimeException(String s) {
		super(s);
	}

	public MockRuntimeException() {
		super();
	}
}

