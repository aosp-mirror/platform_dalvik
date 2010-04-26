/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.SQLite;

import SQLite.Shell;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import java.io.PrintStream;
import java.io.PrintWriter;

@TestTargetClass(Shell.class)
public class ShellTest extends TestCase {

    /**
     * Test method for {@link SQLite.Shell#Shell(java.io.PrintWriter, java.io.PrintWriter)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "Shell",
      args = {PrintWriter.class, PrintWriter.class}
    )
    public void testShellPrintWriterPrintWriter() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.Shell#Shell(java.io.PrintStream, java.io.PrintStream)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "Shell",
      args = {PrintStream.class, PrintStream.class}
    )
    public void testShellPrintStreamPrintStream() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.Shell#clone()}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "clone",
      args = {}
    )
    public void testClone() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.Shell#sql_quote_dbl(java.lang.String)}.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
                method = "sql_quote_dbl",
                args = {String.class}
          )
    public void testSql_quote_dbl() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.Shell#sql_quote(java.lang.String)}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "sql_quote",
          args = {String.class}
    )
    public void testSql_quote() {
        fail("Not yet implemented");
    }


    /**
     * Test method for {@link SQLite.Shell#columns(java.lang.String[])}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "columns",
      args = {String[].class}
    )
    public void testColumns() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.Shell#types(java.lang.String[])}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "types",
      args = {String[].class}
    )
    public void testTypes() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link SQLite.Shell#newrow(java.lang.String[])}.
     */
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "newrow",
      args = {String[].class}
    )
    public void testNewrow() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "main",
      args = {String[].class}
    )
    public void testMain() {
        
    }

}
