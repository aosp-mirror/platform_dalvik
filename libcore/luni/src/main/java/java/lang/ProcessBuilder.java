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

package java.lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A builder for creating OS-specific processes.
 * </p>
 * 
 * @since 1.5
 */
public final class ProcessBuilder {

    private List<String> command;

    private File directory;

    private Map<String, String> environment;

    private boolean redirectErrorStream;

    /**
     * <p>
     * Constructs an instance with the given command.
     * </p>
     * 
     * @param command
     *            The program and arguments.
     */
    public ProcessBuilder(String... command) {
        this(toList(command));
    }

    /**
     * <p>
     * Constructs an instance with the given command.
     * </p>
     * 
     * @param command
     *            The program and arguments.
     * @throws NullPointerException
     *             if <code>command</code> is <code>null</code>.
     */
    public ProcessBuilder(List<String> command) {
        super();
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
        this.environment = System.getenv();
    }

    /**
     * <p>
     * The builder's current program and arguments. The returned value is
     * considered live and modifications to it will change the state of the
     * instance.
     * </p>
     * 
     * @return The program and arguments currently set.
     */
    public List<String> command() {
        return command;
    }

    /**
     * <p>
     * Changes the program and arguments to the command given.
     * </p>
     * 
     * @param command
     *            The program and arguments.
     * @return A reference to this instance.
     */
    public ProcessBuilder command(String... command) {
        return command(toList(command));
    }

    /**
     * <p>
     * Changes the program and arguments to the command given. The list passed
     * is not copied, so any subsequent updates to it are reflected in this
     * instance's state.
     * </p>
     * 
     * @param command
     *            The program and arguments.
     * @return A reference to this instance.
     * @throws NullPointerException
     *             if <code>command</code> is <code>null</code>.
     */
    public ProcessBuilder command(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
        return this;
    }

    /**
     * <p>
     * The working directory that's currently set. If this value is
     * <code>null</code>, then the working directory of the Java process is
     * used.
     * </p>
     * 
     * @return The current working directory, which may be <code>null</code>.
     */
    public File directory() {
        return directory;
    }

    /**
     * <p>
     * Changes the working directory to the directory given. If the given
     * directory is <code>null</code>, then the working directory of the Java
     * process is used when a process is started.
     * </p>
     * 
     * @param directory
     *            The working directory to set.
     * @return A reference to this instance.
     */
    public ProcessBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }

    /**
     * <p>
     * The builder's current environment. When an instance is created, the
     * environment is populated with a copy of the environment, as returned by
     * {@link System#getenv()}. The Map returned is live and any changes made
     * to it are reflected in this instance's state.
     * </p>
     * 
     * @return The Map of the current environment variables.
     */
    public Map<String, String> environment() {
        return environment;
    }

    /**
     * <p>
     * Indicates whether or not the standard error should be redirected to
     * standard output. If redirected, the {@link Process#getErrorStream()} will
     * always return end of stream and standard error is written to
     * {@link Process#getInputStream()}.
     * </p>
     * 
     * @return Indicates whether or not standard error is redirected.
     */
    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }

    /**
     * <p>
     * Changes the state of whether or not standard error is redirected.
     * </p>
     * 
     * @param redirectErrorStream
     *            <code>true</code> to redirect standard error,
     *            <code>false</code> if not.
     * @return A reference to this instance.
     */
    public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * <p>
     * Starts a new process based on the current state of the builder.
     * </p>
     * 
     * @return The new process that was started.
     * @throws NullPointerException
     *             if any of the elements of {@link #command()} are
     *             <code>null</code>.
     * @throws IndexOutOfBoundsException
     *             if {@link #command()} is empty.
     * @throws SecurityException
     *             if {@link SecurityManager#checkExec(String)} doesn't allow
     *             process creation.
     * @throws IOException
     *             if an I/O error happens.
     */
    public Process start() throws IOException {
        if (command.isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        String[] cmdArray = new String[command.size()];
        for (int i = 0; i < cmdArray.length; i++) {
            if ((cmdArray[i] = command.get(i)) == null) {
                throw new NullPointerException();
            }
        }
        String[] envArray = new String[environment.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            envArray[i++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
        }
        Process process = Runtime.getRuntime().exec(cmdArray, envArray,
                directory);
        // TODO implement support for redirectErrorStream
        return process;
    }

    private static List<String> toList(String[] strings) {
        ArrayList<String> arrayList = new ArrayList<String>(strings.length);
        for (String string : strings) {
            arrayList.add(string);
        }
        return arrayList;
    }
}
