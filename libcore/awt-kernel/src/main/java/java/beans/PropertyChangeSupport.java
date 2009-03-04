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

package java.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This utility class  
 *
 */
public class PropertyChangeSupport implements Serializable {

    private static final long serialVersionUID = 6401253773779951803l;

    private transient Object sourceBean;

    private transient List<PropertyChangeListener> allPropertiesChangeListeners =
            new ArrayList<PropertyChangeListener>();

    private transient Map<String, List<PropertyChangeListener>>
            selectedPropertiesChangeListeners =
            new HashMap<String, List<PropertyChangeListener>>();

    // fields for serialization compatibility
    private Hashtable<String, List<PropertyChangeListener>> children;

    private Object source;

    private int propertyChangeSupportSerializedDataVersion = 1;

    /**
     * Creates a new instance that uses the source bean as source for any event.
     * 
     * @param sourceBean
     *            the bean used as source for all events.
     */
    public PropertyChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.sourceBean = sourceBean;
    }

    /**
     * Fires a {@link PropertyChangeEvent} with the given name, old value and
     * new value. As source the bean used to initialize this instance is used.
     * If the old value and the new value are not null and equal the event will
     * not be fired.
     * 
     * @param propertyName
     *            the name of the property
     * @param oldValue
     *            the old value of the property
     * @param newValue
     *            the new value of the property
     */
    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    /**
     * Fires an {@link IndexedPropertyChangeEvent} with the given name, old
     * value, new value and index. As source the bean used to initialize this
     * instance is used. If the old value and the new value are not null and
     * equal the event will not be fired.
     * 
     * @param propertyName
     *            the name of the property
     * @param index
     *            the index
     * @param oldValue
     *            the old value of the property
     * @param newValue
     *            the new value of the property
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
            Object oldValue, Object newValue) {

        // nulls and equals check done in doFire...
        doFirePropertyChange(new IndexedPropertyChangeEvent(sourceBean,
                propertyName, oldValue, newValue, index));
    }

    /**
     * Removes the listener from the specific property. This only happens if it
     * was registered to this property. Nothing happens if it was not
     * registered with this property or if the property name or the listener is
     * null.
     * 
     * @param propertyName
     *            the property name the listener is listening to
     * @param listener
     *            the listener to remove
     */
    public synchronized void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        if ((propertyName != null) && (listener != null)) {
            List<PropertyChangeListener> listeners =
                    selectedPropertiesChangeListeners.get(propertyName);

            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Adds a listener to a specific property. Nothing happens if the property
     * name or the listener is null.
     * 
     * @param propertyName
     *            the name of the property
     * @param listener
     *            the listener to register for the property with the given name
     */
    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        if ((listener != null) && (propertyName != null)) {
            List<PropertyChangeListener> listeners =
                    selectedPropertiesChangeListeners.get(propertyName);

            if (listeners == null) {
                listeners = new ArrayList<PropertyChangeListener>();
                selectedPropertiesChangeListeners.put(propertyName, listeners);
            }

            // RI compatibility
            if (listener instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy =
                        (PropertyChangeListenerProxy) listener;

                listeners.add(new PropertyChangeListenerProxy(
                        proxy.getPropertyName(),
                        (PropertyChangeListener) proxy.getListener()));
            } else {
                listeners.add(listener);
            }
        }
    }

    /**
     * Returns an array of listeners that registered to the property with the
     * given name. If the property name is null an empty array is returned.
     * 
     * @param propertyName
     *            the name of the property whose listeners should be returned
     * @return the array of listeners to the property with the given name.
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        List<PropertyChangeListener> listeners = null;

        if (propertyName != null) {
            listeners = selectedPropertiesChangeListeners.get(propertyName);
        }

        return (listeners == null) ? new PropertyChangeListener[] {}
                : listeners.toArray(
                        new PropertyChangeListener[listeners.size()]);
    }

    /**
     * Fires a property change of a boolean property with the given name. If the
     * old value and the new value are not null and equal the event will not be
     * fired.
     * 
     * @param propertyName
     *            the property name
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    /**
     * Fires a property change of a boolean property with the given name. If the
     * old value and the new value are not null and equal the event will not be
     * fired.
     * 
     * @param propertyName
     *            the property name
     * @param index
     *            the index of the changed property
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
            boolean oldValue, boolean newValue) {

        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Boolean
                    .valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    /**
     * Fires a property change of an integer property with the given name. If
     * the old value and the new value are not null and equal the event will not
     * be fired.
     * 
     * @param propertyName
     *            the property name
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    /**
     * Fires a property change of an integer property with the given name. If
     * the old value and the new value are not null and equal the event will not
     * be fired.
     * 
     * @param propertyName
     *            the property name
     * @param index
     *            the index of the changed property
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
            int oldValue, int newValue) {

        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index,
                    new Integer(oldValue), new Integer(newValue));
        }
    }

    /**
     * Returns true if there are listeners registered to the property with the
     * given name.
     * 
     * @param propertyName
     *            the name of the property
     * @return true if there are listeners registered to that property, false
     *         otherwise.
     */
    public synchronized boolean hasListeners(String propertyName) {
        boolean result = allPropertiesChangeListeners.size() > 0;
        if (!result && (propertyName != null)) {
            List<PropertyChangeListener> listeners =
                    selectedPropertiesChangeListeners.get(propertyName);
            if (listeners != null) {
                result = listeners.size() > 0;
            }
        }
        return result;
    }

    /**
     * removes a property change listener that was registered to all properties.
     * 
     * @param listener
     *            the listener to remove
     */
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener != null) {
            if (listener instanceof PropertyChangeListenerProxy) {
                String name = ((PropertyChangeListenerProxy) listener)
                        .getPropertyName();
                PropertyChangeListener lst = (PropertyChangeListener)
                        ((PropertyChangeListenerProxy) listener).getListener();

                removePropertyChangeListener(name, lst);
            } else {
                allPropertiesChangeListeners.remove(listener);
            }
        }
    }

    /**
     * Registers a listener with all properties.
     * 
     * @param listener
     *            the listener to register
     */
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener != null) {
            if (listener instanceof PropertyChangeListenerProxy) {
                String name = ((PropertyChangeListenerProxy) listener)
                        .getPropertyName();
                PropertyChangeListener lst = (PropertyChangeListener)
                        ((PropertyChangeListenerProxy) listener).getListener();
                addPropertyChangeListener(name, lst);
            } else {
                allPropertiesChangeListeners.add(listener);
            }
        }
    }

    /**
     * Returns an array with the listeners that registered to all properties.
     * 
     * @return the array of listeners
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        ArrayList<PropertyChangeListener> result =
                new ArrayList<PropertyChangeListener>(
                        allPropertiesChangeListeners);

        for (String propertyName : selectedPropertiesChangeListeners.keySet()) {
            List<PropertyChangeListener> selectedListeners =
                    selectedPropertiesChangeListeners.get(propertyName);

            if (selectedListeners != null) {

                for (PropertyChangeListener listener : selectedListeners) {
                    result.add(new PropertyChangeListenerProxy(propertyName,
                            listener));
                }
            }
        }

        return result.toArray(new PropertyChangeListener[result.size()]);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        List<PropertyChangeListener> allSerializedPropertiesChangeListeners =
                new ArrayList<PropertyChangeListener>();

        for (PropertyChangeListener pcl : allPropertiesChangeListeners) {
            if (pcl instanceof Serializable) {
                allSerializedPropertiesChangeListeners.add(pcl);
            }
        }

        Map<String, List<PropertyChangeListener>>
                selectedSerializedPropertiesChangeListeners =
                        new HashMap<String, List<PropertyChangeListener>>();

        for (String propertyName : selectedPropertiesChangeListeners.keySet()) {
            List<PropertyChangeListener> keyValues =
                    selectedPropertiesChangeListeners.get(propertyName);

            if (keyValues != null) {
                List<PropertyChangeListener> serializedPropertiesChangeListeners
                        = new ArrayList<PropertyChangeListener>();

                for (PropertyChangeListener pcl : keyValues) {
                    if (pcl instanceof Serializable) {
                        serializedPropertiesChangeListeners.add(pcl);
                    }
                }

                if (!serializedPropertiesChangeListeners.isEmpty()) {
                    selectedSerializedPropertiesChangeListeners.put(
                            propertyName, serializedPropertiesChangeListeners);
                }
            }
        }

        children = new Hashtable<String, List<PropertyChangeListener>>(
                selectedSerializedPropertiesChangeListeners);
        children.put("", allSerializedPropertiesChangeListeners); //$NON-NLS-1$
        oos.writeObject(children);

        Object source = null;
        if (sourceBean instanceof Serializable) {
            source = sourceBean;
        }
        oos.writeObject(source);

        oos.writeInt(propertyChangeSupportSerializedDataVersion);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        children = (Hashtable<String, List<PropertyChangeListener>>) ois
                .readObject();

        selectedPropertiesChangeListeners = new HashMap<String, List<PropertyChangeListener>>(
                children);
        allPropertiesChangeListeners = selectedPropertiesChangeListeners
                .remove(""); //$NON-NLS-1$
        if (allPropertiesChangeListeners == null) {
            allPropertiesChangeListeners = new ArrayList<PropertyChangeListener>();
        }

        sourceBean = ois.readObject();
        propertyChangeSupportSerializedDataVersion = ois.readInt();
    }

    /**
     * Fires a property change event to all listeners of that property.
     * 
     * @param event
     *            the event to fire
     */
    public void firePropertyChange(PropertyChangeEvent event) {
        doFirePropertyChange(event);
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            Object oldValue, Object newValue) {
        return new PropertyChangeEvent(sourceBean, propertyName, oldValue,
                newValue);
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            boolean oldValue, boolean newValue) {
        return new PropertyChangeEvent(sourceBean, propertyName, oldValue,
                newValue);
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            int oldValue, int newValue) {
        return new PropertyChangeEvent(sourceBean, propertyName, oldValue,
                newValue);
    }

    private void doFirePropertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if ((newValue != null) && (oldValue != null)
                && newValue.equals(oldValue)) {
            return;
        }

        /*
         * Copy the listeners collections so they can be modified while we fire
         * events.
         */

        // Listeners to all property change events
        PropertyChangeListener[] listensToAll;
        // Listens to a given property change
        PropertyChangeListener[] listensToOne = null;
        synchronized (this) {
            listensToAll = allPropertiesChangeListeners
                    .toArray(new PropertyChangeListener[allPropertiesChangeListeners
                            .size()]);

            List<PropertyChangeListener> listeners = selectedPropertiesChangeListeners
                    .get(propertyName);
            if (listeners != null) {
                listensToOne = listeners
                        .toArray(new PropertyChangeListener[listeners.size()]);
            }
        }

        // Fire the listeners
        for (PropertyChangeListener listener : listensToAll) {
            listener.propertyChange(event);
        }
        if (listensToOne != null) {
            for (PropertyChangeListener listener : listensToOne) {
                listener.propertyChange(event);
            }
        }
    }

}
