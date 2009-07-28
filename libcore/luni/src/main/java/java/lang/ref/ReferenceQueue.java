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

// BEGIN android-note
// This implementation is quite different from Harmony. Changes are not marked.
// END android-note

package java.lang.ref;

/**
 * The {@code ReferenceQueue} is the container on which reference objects are
 * enqueued when the garbage collector detects the reachability type specified
 * for the referent.
 *
 * @since 1.2
 */
public class ReferenceQueue<T> {

    private Reference<? extends T> head;

    /**
     * Constructs a new instance of this class.
     */
    public ReferenceQueue() {
        super();
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Does not wait for a reference to become available.
     *
     * @return the next available reference, or {@code null} if no reference is
     *         immediately available
     */
    @SuppressWarnings("unchecked")
    public synchronized Reference<? extends T> poll() {
        if (head == null) {
            return null;
        }

        Reference<? extends T> ret;

        ret = head;

        if (head == head.queueNext) {
            head = null;
        } else {
            head = head.queueNext;
        }

        ret.queueNext = null;

        return ret;
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Waits indefinitely for a reference to become available.
     *
     * @return the next available reference
     *
     * @throws InterruptedException
     *             if the blocking call was interrupted for some reason
     */
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0L);
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Waits for a reference to become available or the given timeout
     * period to elapse, whichever happens first.
     *
     * @param timeout
     *            maximum time (in ms) to spend waiting for a reference object
     *            to become available. A value of zero results in the method
     *            waiting indefinitely.
     * @return the next available reference, or {@code null} if no reference
     *         becomes available within the timeout period
     * @throws IllegalArgumentException
     *             if the wait period is negative.
     * @throws InterruptedException
     *             if the blocking call was interrupted for some reason
     */
    public synchronized Reference<? extends T> remove(long timeout) throws IllegalArgumentException,
            InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }

        if (timeout == 0L) {
            while (head == null) {
                wait(0L);
            }
        } else {
            long now = System.currentTimeMillis();
            long wakeupTime = now + timeout + 1L;
            while (head == null && now < wakeupTime) {
                wait(wakeupTime - now);
                now = System.currentTimeMillis();
            }
        }

        return poll();
    }

    /**
     * Enqueue the reference object on the receiver.
     *
     * @param reference
     *            reference object to be enqueued.
     * @return boolean true if reference is enqueued. false if reference failed
     *         to enqueue.
     */
    synchronized void enqueue(Reference<? extends T> reference) {
        if (head == null) {
            reference.queueNext = reference;
        } else {
            reference.queueNext = head;
        }
        head = reference;
        notify();
    }
}
