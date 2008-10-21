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

import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * 
 */
public class NodeChangeListenerTest extends TestCase {

	NodeChangeListener l;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		l = new NodeChangeListenerImpl();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testChildAdded() {
		l.childAdded(new NodeChangeEvent(Preferences.userRoot(), Preferences
				.userRoot()));
	}

	public void testChildRemoved() {
		l.childRemoved(new NodeChangeEvent(Preferences.userRoot(), Preferences
				.userRoot()));
	}

	public static class NodeChangeListenerImpl implements NodeChangeListener {

		public void childAdded(NodeChangeEvent e) {
		}

		public void childRemoved(NodeChangeEvent e) {
		}

	}

}
