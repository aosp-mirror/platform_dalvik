/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

/**
 * This class must be implemented by the vm vendor. Object is the root of the
 * java class hierarchy. All non-base types respond to the messages defined in
 * this class.
 * 
 */
public class Object {

    /**
     * Constructs a new instance of this class.
     */
    public Object() {
    }

    /**
     * Returns a new instance of the same class as the receiver, whose slots
     * have been filled in with the values in the slots of the receiver.
     * <p>
     * Classes which wish to support cloning must specify that they implement
     * the Cloneable interface, since the implementation checks for this.
     * 
     * @return Object a shallow copy of this object.
     * @throws CloneNotSupportedException if the receiver's class does not
     *         implement the interface Cloneable.
     */
    protected Object clone() throws CloneNotSupportedException {
        if (!(this instanceof Cloneable)) {
            throw new CloneNotSupportedException("Class doesn't implement Cloneable");
        }
        
        return internalClone((Cloneable) this);
    }

    /*
     * Native helper method for cloning.
     */
    private native Object internalClone(Cloneable o);
    
    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison. The
     * implementation in Object returns true only if the argument is the exact
     * same object as the receiver (==).
     * 
     * @param o Object the object to compare with this object.
     * @return boolean <code>true</code> if the object is the same as this
     *         object <code>false</code> if it is different from this object.
     * @see #hashCode
     */
    public boolean equals(Object o) {
        return this == o;
    }

    /**
     * Called by the virtual machine when there are no longer any (non-weak)
     * references to the receiver. Subclasses can use this facility to guarantee
     * that any associated resources are cleaned up before the receiver is
     * garbage collected. Uncaught exceptions which are thrown during the
     * running of the method cause it to terminate immediately, but are
     * otherwise ignored.
     * <p>
     * Note: The virtual machine assumes that the implementation in class Object
     * is empty.
     * 
     * @throws Throwable The virtual machine ignores any exceptions which are
     *         thrown during finalization.
     */
    protected void finalize() throws Throwable {
    }

    /**
     * Returns the unique instance of java.lang.Class which represents the class
     * of the receiver.
     * 
     * @return Class the receiver's Class
     */
    public final native Class<? extends Object> getClass();

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * answer <code>true</code> when passed to <code>.equals</code> must
     * answer the same value for this method.
     * 
     * @return int the receiver's hash.
     * @see #equals
     */
    public native int hashCode();

    /**
     * Causes one thread which is <code>wait</code> ing on the receiver to be
     * made ready to run. This does not guarantee that the thread will
     * immediately run. The method can only be invoked by a thread which owns
     * the receiver's monitor.
     * 
     * @see #notifyAll
     * @see #wait()
     * @see #wait(long)
     * @see #wait(long,int)
     * @see java.lang.Thread
     */
    public final native void notify();

    /**
     * Causes all threads which are <code>wait</code> ing on the receiver to
     * be made ready to run. The threads are scheduled according to their
     * priorities as specified in class Thread. Between any two threads of the
     * same priority the one which waited first will be the first thread that
     * runs after being notified. The method can only be invoked by a thread
     * which owns the receiver's monitor.
     * 
     * @see #notify
     * @see #wait()
     * @see #wait(long)
     * @see #wait(long,int)
     * @see java.lang.Thread
     */
    public final native void notifyAll();

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return String a printable representation for the receiver.
     */
    public String toString() {
        return getClass().getName() + '@' + Integer.toHexString(hashCode());
    }

    /**
     * Causes the thread which sent this message to be made not ready to run
     * pending some change in the receiver (as indicated by <code>notify</code>
     * or <code>notifyAll</code>). The method can only be invoked by a thread
     * which owns the receiver's monitor. A waiting thread can be sent
     * <code>interrupt()</code> to cause it to prematurely stop waiting, so
     * senders of wait should check that the condition they were waiting for has
     * been met.
     * <p>
     * When the thread waits, it gives up ownership of the receiver's monitor.
     * When it is notified (or interrupted) it re-acquires the monitor before it
     * starts running.
     * 
     * @throws InterruptedException to interrupt the wait.
     * @see Thread#interrupt
     * @see #notify
     * @see #notifyAll
     * @see #wait(long)
     * @see #wait(long,int)
     * @see java.lang.Thread
     */
    public final void wait() throws InterruptedException {
        wait(0 ,0);
    }

    /**
     * Causes the thread which sent this message to be made not ready to run
     * either pending some change in the receiver (as indicated by
     * <code>notify</code> or <code>notifyAll</code>) or the expiration of
     * the timeout. The method can only be invoked by a thread which owns the
     * receiver's monitor. A waiting thread can be sent <code>interrupt()</code>
     * to cause it to prematurely stop waiting, so senders of wait should check
     * that the condition they were waiting for has been met.
     * <p>
     * When the thread waits, it gives up ownership of the receiver's monitor.
     * When it is notified (or interrupted) it re-acquires the monitor before it
     * starts running.
     * 
     * @param time long The maximum time to wait in milliseconds.
     * @throws InterruptedException to interrupt the wait.
     * @see #notify
     * @see #notifyAll
     * @see #wait()
     * @see #wait(long,int)
     * @see java.lang.Thread
     */
    public final void wait(long time) throws InterruptedException {
        wait(time, 0);
    }

    /**
     * Causes the thread which sent this message to be made not ready to run
     * either pending some change in the receiver (as indicated by
     * <code>notify</code> or <code>notifyAll</code>) or the expiration of
     * the timeout. The method can only be invoked by a thread which owns the
     * receiver's monitor. A waiting thread can be sent <code>interrupt()</code>
     * to cause it to prematurely stop waiting, so senders of wait should check
     * that the condition they were waiting for has been met.
     * <p>
     * When the thread waits, it gives up ownership of the receiver's monitor.
     * When it is notified (or interrupted) it re-acquires the monitor before it
     * starts running.
     * 
     * @param time long The maximum time to wait in milliseconds.
     * @param frac int The fraction of a mSec to wait, specified in nanoseconds.
     * @throws InterruptedException to interrupt the wait.
     * @see #notify
     * @see #notifyAll
     * @see #wait()
     * @see #wait(long)
     * @see java.lang.Thread
     */
    public final native void wait(long time, int frac) throws InterruptedException;
}
