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
 * occurs.
 */
public class Observable {
    
    Vector<Observer> observers = new Vector<Observer>();

    boolean changed = false;

    /**
     * Constructs a new Observable object.
     */
    public Observable() {
        super();
    }

    /**
     * Adds the specified Observer to the list of observers.
     * 
     * @param observer
     *            the Observer to add
     */
    public synchronized void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!observers.contains(observer))
            observers.addElement(observer);
    }

    /**
     * Clears the changed flag for this Observable.  After calling <code>clearChanged()</code>, <code>hasChanged()</code> will return false.
     */
    protected synchronized void clearChanged() {
        changed = false;
    }

    /**
     * Returns the number of Observers in the list of observers.
     * 
     * @return the number of observers
     */
    public synchronized int countObservers() {
        return observers.size();
    }

    /**
     * Removes the specified Observer from the list of observers.
     * 
     * @param observer
     *            the Observer to remove
     */
    public synchronized void deleteObserver(Observer observer) {
        observers.removeElement(observer);
    }

    /**
     * Removes all Observers from the list of observers.
     */
    public synchronized void deleteObservers() {
        observers.setSize(0);
    }

    /**
     * Returns the changed flag for this Observable.
     * 
     * @return true when the changed flag for this Observable is set, false
     *         otherwise
     */
    public synchronized boolean hasChanged() {
        return changed;
    }

    /**
     * If <code>hasChanged()</code> returns true, calls the <code>update()</code> method for
     * every Observer in the list of observers using null as the argument.
     * Afterwards calls <code>clearChanged()</code>.
     * 
     * Equivalent to calling <code>notifyObservers(null)</code>
     */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * If <code>hasChanged()</code> returns true, calls the <code>update()</code> method for
     * every Observer in the list of observers using the specified argument.
     * Afterwards calls <code>clearChanged()</code>.
     * 
     * @param data
     *            the argument passed to update()
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
     * Sets the changed flag for this Observable. After calling <code>setChanged()</code>, <code>hasChanged()</code> will return true.
     */
    protected synchronized void setChanged() {
        changed = true;
    }
}
