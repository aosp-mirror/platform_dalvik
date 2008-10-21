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

package java.nio.channels;


import java.io.IOException;

/**
 * Channels that implement this interface are both asynchronously closable and
 * interruptible.
 * <p>
 * A channel that is asynchronously closable permits a thread blocked on an IO
 * operation (the IO thread) to be released by another thread calling the
 * channel's <code>close()</code> method. The IO thread will throw an
 * <code>AsynchronousCloseException</code> and the channel will be closed.
 * </p>
 * <p>
 * A channel that is interruptible permits a thread blocked on an IO operation
 * (the IO thread) to be interrupted by another thread (by invoking
 * <code>interrupt()</code> on the IO thread). When the IO thread is
 * interrupted it will throw a <code>ClosedByInterruptException</code>
 * exception, it will have its interrupted status set, and the channel will be
 * closed. If the IO thread attempts to make an IO call with the interrupt
 * status set the call will immediately fail with a
 * <code>ClosedByInterruptException</code>.
 * 
 */
public interface InterruptibleChannel extends Channel {

    /**
     * Closes an InterruptibleChannel. This method is precisely the same as the
     * super-interface <code>close()</code>.
     * <p>
     * Any threads that are blocked on IO operations on this channel will be
     * interrupted with an <code>AsynchronousCloseException
     * </code>.
     * </p>
     * 
     * @throws IOException
     *             if an IO problem occurs closing the channel.
     */
    public void close() throws IOException;

}
