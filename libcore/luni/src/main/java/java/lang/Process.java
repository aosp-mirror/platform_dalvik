/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;


import java.io.InputStream;
import java.io.OutputStream;

/**
 * Instances of class Process provide control of and access to platform
 * processes.
 */
public abstract class Process {

    /**
     * Terimates the receiver and closes any associated streams.
     */
    abstract public void destroy();

    /**
     * Returns the exit value of the receiving Process. It is available only
     * when the OS subprocess is finished.
     * 
     * @return The exit value of the receiver.
     * 
     * @throws IllegalThreadStateException
     *             If the receiver has not terminated.
     */
    abstract public int exitValue();

    /**
     * Returns the receiver's error output stream.
     * <p>
     * Note: This is an InputStream which allows reading of the other threads
     * "stderr".
     * 
     * @return The error stream associated with the receiver
     */
    abstract public InputStream getErrorStream();

    /**
     * Returns the receiver's standard input stream
     * <p>
     * Note: This is an InputStream which allows reading from the other process'
     * "stdout".
     * 
     * @return The receiver's process' stdin.
     */
    abstract public InputStream getInputStream();

    /**
     * Returns the receiver's standard output stream
     * <p>
     * Note: This is an OutputStream which allows writing to the other process'
     * "stdin".
     * 
     * @return The receiver's process' stdout.
     */
    abstract public OutputStream getOutputStream();

    /**
     * Causes the calling thread to wait for the process associated with the
     * receiver to finish executing.
     * 
     * @return The exit value of the Process being waited on
     * 
     * @throws InterruptedException
     *             If the calling thread is interrupted
     */
    abstract public int waitFor() throws InterruptedException;
}
