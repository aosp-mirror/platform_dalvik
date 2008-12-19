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

package java.util;


/**
 * Observable is used to notify a group of Observer objects when a change
 * occurs. On creation, the set of observers is empty. After a change occurred,
 * the application can call the {@link #notifyObservers()} method. This will
 * cause the invocation of the {@code update()} method of all registered
 * Observers. The order of invocation is not specified. This implementation will
 * call the Observers in the order they registered. Subclasses are completely
 * free in what order they call the update methods.
 * 
 * @see Observer
 * 
 * @since Android 1.0
 */
public class Observable {
    
    Vector<Observer> observers = new Vector<Observer>();

    boolean changed = false;

    /**
     * Constructs a new {@code Observable} object.
     * 
     * @since Android 1.0
     */
    public Observable() {
        super();
    }

    /**
     * Adds the specified observer to the list of observers. If it is already
     * registered, it is not added a second time.
     * 
     * @param observer
     *            the Observer to add.
     * @since Android 1.0
     */
    public synchronized void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!observers.contains(observer))
            observers.addElement(observer);
    }

    /**
     * Clears the changed flag for this {@code Observable}. After calling
     * {@code clearChanged()}, {@code hasChanged()} will return {@code false}.
     * 
     * @since Android 1.0
     */
    protected synchronized void clearChanged() {
        changed = false;
    }

    /**
     * Returns the number of observers registered to this {@code Observable}.
     * 
     * @return the number of observers.
     * @since Android 1.0
     */
    public synchronized int countObservers() {
        return observers.size();
    }

    /**
     * Removes the specified observer from the list of observers. Passing null
     * won't do anything.
     * 
     * @param observer
     *            the observer to remove.
     * @since Android 1.0
     */
    public synchronized void deleteObserver(Observer observer) {
        observers.removeElement(observer);
    }

    /**
     * Removes all observers from the list of observers.
     * 
     * @since Android 1.0
     */
    public synchronized void deleteObservers() {
        observers.setSize(0);
    }

    /**
     * Returns the changed flag for this {@code Observable}.
     * 
     * @return {@code true} when the changed flag for this {@code Observable} is
     *         set, {@code false} otherwise.
     * @since Android 1.0
     */
    public synchronized boolean hasChanged() {
        return changed;
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every observer in the list of observers using null as the
     * argument. Afterwards, calls {@code clearChanged()}.
     * <p>
     * Equivalent to calling {@code notifyObservers(null)}.
     * </p>
     * 
     * @since Android 1.0
     */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every Observer in the list of observers using the specified
     * argument. Afterwards calls {@code clearChanged()}.
     * 
     * @param data
     *            the argument passed to {@code update()}.
     * @since Android 1.0
     */
    @SuppressWarnings("unchecked")
    public void notifyObservers(Object data) {
        if (hasChanged()) {
            // Must clone the vector in case deleteObserver is called
            Vector<Observer> clone = (Vector<Observer>)observers.clone();
            int size = clone.size();
            for (int i = 0; i < size; i++) {
                clone.elementAt(i).update(this, data);
            }
            clearChanged();
        }
    }

    /**
     * Sets the changed flag for this {@code Observable}. After calling
     * {@code setChanged()}, {@code hasChanged()} will return {@code true}.
     * 
     * @since Android 1.0
     */
    protected synchronized void setChanged() {
        changed = true;
    }
}
