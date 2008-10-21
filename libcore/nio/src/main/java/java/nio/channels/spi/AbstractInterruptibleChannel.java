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

package java.nio.channels.spi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.InterruptibleChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * This class roots the implementation of interruptible channels.
 * <p>
 * The basic usage pattern for an interruptible channel is to invoke
 * <code>begin()</code> before any IO operations, then
 * <code>end(boolean)</code> after completing the operation. The argument to
 * the end method shows whether there has been any change to the java
 * environment that is visible to the API user.
 * </p>
 * 
 */
public abstract class AbstractInterruptibleChannel implements Channel,
        InterruptibleChannel {

    static Method setInterruptAction = null;

    static {
        try {
            setInterruptAction = AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Method>() {
                        public Method run() throws Exception {
                            return Thread.class.getDeclaredMethod(
                                    "setInterruptAction", //$NON-NLS-1$
                                    new Class[] { Runnable.class });

                        }
                    });
            setInterruptAction.setAccessible(true);
        } catch (Exception e) {
            // FIXME: be accommodate before VM actually provides
            // setInterruptAction method
            // throw new Error(e);
        }
    }

    private volatile boolean closed = false;

    volatile boolean interrupted = false;

    /**
     * Default constructor.
     */
    protected AbstractInterruptibleChannel() {
        super();
    }

    /**
     * Returns whether the channel is open.
     * 
     * @return true if the channel is open, and false if it is closed.
     * @see java.nio.channels.Channel#isOpen()
     */
    public synchronized final boolean isOpen() {
        return !closed;
    }

    /**
     * Closes the channel.
     * <p>
     * If the channel is already closed then this method has no effect,
     * otherwise it closes the receiver via the implCloseChannel method.
     * </p>
     * 
     * @see java.nio.channels.Channel#close()
     */
    public final void close() throws IOException {
        if (!closed) {
            synchronized (this) {
                if (!closed) {
                    closed = true;
                    implCloseChannel();
                }
            }
        }
    }

    /**
     * Start an IO operation that is potentially blocking.
     * <p>
     * Once the operation is completed the application should invoke a
     * corresponding <code>end(boolean)</code>.
     */
    protected final void begin() {
        // FIXME: be accommodate before VM actually provides
        // setInterruptAction method
        if (setInterruptAction != null) {
            try {
                setInterruptAction.invoke(Thread.currentThread(),
                        new Object[] { new Runnable() {
                            public void run() {
                                try {
                                    interrupted = true;
                                    AbstractInterruptibleChannel.this.close();
                                } catch (IOException e) {
                                    // ignore
                                }
                            }
                        } });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * End an IO operation that was previously started with <code>begin()</code>.
     * 
     * @param success
     *            pass true if the operation succeeded and had a side effect on
     *            the Java system, or false if not.
     * @throws AsynchronousCloseException
     *             the channel was closed while the IO operation was in
     *             progress.
     * @throws java.nio.channels.ClosedByInterruptException
     *             the thread conducting the IO operation was interrupted.
     */
    protected final void end(boolean success) throws AsynchronousCloseException {
        // FIXME: be accommodate before VM actually provides
        // setInterruptAction method
        if (setInterruptAction != null) {
            try {
                setInterruptAction.invoke(Thread.currentThread(),
                        new Object[] { null });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (interrupted) {
                interrupted = false;
                throw new ClosedByInterruptException();
            }
        }
        if (!success && closed) {
            throw new AsynchronousCloseException();
        }
    }

    /**
     * Implements the close channel behavior.
     * <p>
     * Closes the channel with a guarantee that the channel is not currently
     * closed via <code>close()</code> and that the method is thread-safe.
     * </p>
     * <p>
     * any outstanding threads blocked on IO operations on this channel must be
     * released with either a normal return code, or an
     * <code>AsynchronousCloseException</code>.
     * 
     * @throws IOException
     *             if a problem occurs closing the channel.
     */
    protected abstract void implCloseChannel() throws IOException;
}
