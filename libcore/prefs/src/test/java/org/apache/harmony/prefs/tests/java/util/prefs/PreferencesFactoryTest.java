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

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import junit.framework.TestCase;

/**
 * 
 */
public class PreferencesFactoryTest extends TestCase {

	PreferencesFactory f;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		f = new PreferencesFactoryImpl();
	}

	public void testUserRoot() {
		f.userRoot();
	}

	public void testSystemRoot() {
		f.systemRoot();
	}

	public static class PreferencesFactoryImpl implements PreferencesFactory {

		public Preferences userRoot() {
			return null;
		}

		public Preferences systemRoot() {
			return null;
		}

	}

}
