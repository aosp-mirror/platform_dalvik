/*
 * Copyright (C) 2007 The Android Open Source Project
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

package tests.api.java.lang;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ProcessManagerTest extends TestCase {

    public void testCat() throws IOException, InterruptedException {
        String[] commands = { "cat" };
        Process process = Runtime.getRuntime().exec(commands, null, null);

        OutputStream out = process.getOutputStream();
        String greeting = "Hello, World!";
        out.write(greeting.getBytes());
        out.write('\n');
        out.close();

        assertEquals(greeting, readLine(process));
    }

    public void testSleep() throws IOException, InterruptedException {
        String[] commands = { "sleep", "1" };
        Process process = Runtime.getRuntime().exec(commands, null, null);
        assertEquals(0, process.waitFor());
    }

    public void testPwd() throws IOException, InterruptedException {
        String[] commands = { "sh", "-c", "pwd" };
        Process process = Runtime.getRuntime().exec(
                commands, null, new File("/"));
        logErrors(process);
        assertEquals("/", readLine(process));
    }

    public void testEnvironment() throws IOException, InterruptedException {
        String[] commands = { "sh", "-c", "echo $FOO" };

        // Remember to set the path so we can find sh.
        String[] environment = { "FOO=foo", "PATH=" + System.getenv("PATH") };
        Process process = Runtime.getRuntime().exec(
                commands, environment, null);
        logErrors(process);
        assertEquals("foo", readLine(process));
    }

    String readLine(Process process) throws IOException {
        InputStream in = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.readLine();
    }

    void logErrors(final Process process) throws IOException {
        Thread thread = new Thread() {
            public void run() {
                InputStream in = process.getErrorStream();
                BufferedReader reader
                        = new BufferedReader(new InputStreamReader(in));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void testHeavyLoad() {
        int i;
        for (i = 0; i < 2000; i++)
            stuff(i);
    }

    private static void stuff(int iteration) {
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec("ls");
            int code = proc.waitFor();
            proc = null;
        } catch (Exception ex) {
            System.err.println("Failure: " + ex);
            throw new RuntimeException(ex);
        }
        rt.gc();
        rt = null;
    }
}
