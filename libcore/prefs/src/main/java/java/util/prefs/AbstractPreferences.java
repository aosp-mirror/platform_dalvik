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

package java.util.prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.harmony.luni.util.Base64;
import org.apache.harmony.prefs.internal.nls.Messages;

/**
 * This abstract class is a partial implementation of the abstract class
 * Preferences, which can be used to simplify {@code Preferences} provider's
 * implementation. This class defines nine abstract SPI methods, which must be
 * implemented by a preference provider.
 * 
 * @since Android 1.0
 */
public abstract class AbstractPreferences extends Preferences {
    /*
     * -----------------------------------------------------------
     * Class fields
     * -----------------------------------------------------------
     */
    /**
     * The unhandled events collection.
     */
    private static final List<EventObject> events = new LinkedList<EventObject>();

    /**
     * The event dispatcher thread.
     */
    private static final EventDispatcher dispatcher = new EventDispatcher("Preference Event Dispatcher"); //$NON-NLS-1$

    /*
     * -----------------------------------------------------------
     * Class initializer
     * -----------------------------------------------------------
     */
    static {
        dispatcher.setDaemon(true);
        dispatcher.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Preferences uroot = Preferences.userRoot();
                Preferences sroot = Preferences.systemRoot();
                try {
                    uroot.flush();
                } catch (BackingStoreException e) {//ignore
                }
                try {
                    sroot.flush();
                } catch (BackingStoreException e) {//ignore
                }
            }
        });
    }
    
    /*
     * -----------------------------------------------------------
     * Instance fields (package-private)
     * -----------------------------------------------------------
     */
    /**
     * True, if this node is in user preference hierarchy.
     */
    boolean userNode;

    /*
     * -----------------------------------------------------------
     * Instance fields (private)
     * -----------------------------------------------------------
     */
    /**
     * Marker class for 'lock' field.
     */
    private static class Lock {
    }

    /**
     * The object used to lock this node.
     * 
     * @since Android 1.0
     */
    protected final Object lock;
    
    /**
     * This field is true if this node is created while it doesn't exist in the
     * backing store. This field's default value is false, and it is checked
     * when the node creation is completed, and if it is true, the node change
     * event will be fired for this node's parent.
     * 
     * @since Android 1.0
     */
    protected boolean newNode;

    /**
     * Cached child nodes
     */
    private Map<String, AbstractPreferences> cachedNode;

    //the collections of listeners
    private List<EventListener> nodeChangeListeners;
    private List<EventListener> preferenceChangeListeners;

    //this node's name
    private String nodeName;

    //handler to this node's parent
    private AbstractPreferences parentPref;

    //true if this node has been removed
    private boolean isRemoved;

    //handler to this node's root node
    private AbstractPreferences root;

    /*
     * -----------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------
     */
    /**
     * Constructs a new {@code AbstractPreferences} instance using the given parent node
     * and node name.
     * 
     * @param parent
     *            the parent node of the new node or {@code null} to indicate
     *            that the new node is a root node.
     * @param name
     *            the name of the new node or an empty string to indicate that
     *            this node is called "root".
     * @throws IllegalArgumentException
     *             if the name contains a slash character or is empty if {@code
     *             parent} is not {@code null}.
     * @since Android 1.0
     */
    protected AbstractPreferences(AbstractPreferences parent, String name) {
        if ((null == parent ^ name.length() == 0) || name.indexOf("/") >= 0) { //$NON-NLS-1$
            throw new IllegalArgumentException();
        }
        root = null == parent ? this : parent.root;
        nodeChangeListeners = new LinkedList<EventListener>();
        preferenceChangeListeners = new LinkedList<EventListener>();
        isRemoved = false;
        cachedNode = new HashMap<String, AbstractPreferences>();
        nodeName = name;
        parentPref = parent;
        lock = new Lock();
        userNode = root.userNode;
    }

    /*
     * -----------------------------------------------------------
     * Methods
     * -----------------------------------------------------------
     */
    /**
     * Returns an array of all cached child nodes.
     * 
     * @return the array of cached child nodes.
     * @since Android 1.0
     */
    protected final AbstractPreferences[] cachedChildren() {
        return cachedNode.values().toArray(new AbstractPreferences[cachedNode.size()]);
    }

    /**
     * Returns the child node with the specified name or {@code null} if it
     * doesn't exist. Implementers can assume that the name supplied to this method 
     * will be a valid node name string (conforming to the node naming format) and 
     * will not correspond to a node that has been cached or removed.
     * 
     * @param name
     *            the name of the desired child node.
     * @return the child node with the given name or {@code null} if it doesn't
     *         exist.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    protected AbstractPreferences getChild(String name)
            throws BackingStoreException {
        synchronized (lock) {
            checkState();
            AbstractPreferences result = null;
            String[] childrenNames = childrenNames();
            for (int i = 0; i < childrenNames.length; i++) {
                if (childrenNames[i].equals(name)) {
                    result = childSpi(name);
                    break;
                }
            }
            return result;
        }

    }

    /**
     * Returns whether this node has been removed by invoking the method {@code
     * removeNode()}.
     * 
     * @return {@code true}, if this node has been removed, {@code false}
     *         otherwise.
     * @since Android 1.0
     */
    protected boolean isRemoved() {
        synchronized (lock) {
            return isRemoved;
        }
    }

    /**
     * Flushes changes of this node to the backing store. This method should
     * only flush this node and should not include the descendant nodes. Any
     * implementation that wants to provide functionality to flush all nodes 
     * at once should override the method {@link #flush() flush()}.
     * 
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    protected abstract void flushSpi() throws BackingStoreException;
    
    /**
     * Returns the names of all of the child nodes of this node or an empty array if
     * this node has no children. The names of cached children are not required to be
     * returned.
     * 
     * @return the names of this node's children.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    protected abstract String[] childrenNamesSpi() throws BackingStoreException;

    /**
     * Returns the child preference node with the given name, creating it
     * if it does not exist. The caller of this method should ensure that the
     * given name is valid and that this node has not been removed or cached. 
     * If the named node has just been removed, the implementation
     * of this method must create a new one instead of reactivating the removed
     * one.
     * <p>
     * The new creation is not required to be persisted immediately until the
     * flush method will be invoked.
     * </p>
     * 
     * @param name
     *            the name of the child preference to be returned.
     * @return the child preference node.
     * @since Android 1.0
     */
    protected abstract AbstractPreferences childSpi(String name);


    /**
     * Puts the given key-value pair into this node. Caller of this method
     * should ensure that both of the given values are valid and that this
     * node has not been removed.
     * 
     * @param name
     *            the given preference key.
     * @param value
     *            the given preference value.
     * @since Android 1.0
     */
    protected abstract void putSpi(String name, String value);

    /**
     * Gets the preference value mapped to the given key. The caller of this method
     * should ensure that the given key is valid and that this node has not been
     * removed. This method should not throw any exceptions but if it does, the
     * caller will ignore the exception, regarding it as a {@code null} return value.
     * 
     * @param key
     *            the given key to be searched for.
     * @return the preference value mapped to the given key.
     * @since Android 1.0
     */
    protected abstract String getSpi(String key);


    /**
     * Returns an array of all preference keys of this node or an empty array if
     * no preferences have been found. The caller of this method should ensure that
     * this node has not been removed.
     * 
     * @return the array of all preference keys.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    protected abstract String[] keysSpi() throws BackingStoreException;

    /**
     * Removes this node from the preference hierarchy tree. The caller of this
     * method should ensure that this node has no child nodes, which means the
     * method {@link Preferences#removeNode() Preferences.removeNode()} should
     * invoke this method multiple-times in bottom-up pattern. The removal is
     * not required to be persisted until after it is flushed.
     * 
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    protected abstract void removeNodeSpi() throws BackingStoreException;

    /**
     * Removes the preference with the specified key. The caller of this method
     * should ensure that the given key is valid and that this node has not been
     * removed.
     * 
     * @param key
     *            the key of the preference that is to be removed.
     * @since Android 1.0
     */
    protected abstract void removeSpi(String key);

    /**
     * Synchronizes this node with the backing store. This method should only
     * synchronize this node and should not include the descendant nodes. An
     * implementation that wants to provide functionality to synchronize all nodes at once should
     * override the method {@link #sync() sync()}.
     * 
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    protected abstract void syncSpi() throws BackingStoreException;

    /*
     * -----------------------------------------------------------
     * Methods inherited from Preferences
     * -----------------------------------------------------------
     */
    @Override
    public String absolutePath() {
        if (parentPref == null) {
            return "/"; //$NON-NLS-1$
        } else if (parentPref == root) {
            return "/" + nodeName; //$NON-NLS-1$
        }
        return parentPref.absolutePath() + "/" + nodeName; //$NON-NLS-1$
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            TreeSet<String> result = new TreeSet<String>(cachedNode.keySet());
            String[] names = childrenNamesSpi();
            for (int i = 0; i < names.length; i++) {
                result.add(names[i]);
            }
            return result.toArray(new String[0]);
        }
    }

    @Override
    public void clear() throws BackingStoreException {
        synchronized (lock) {
            String[] keyList = keys();
            for (int i = 0; i < keyList.length; i++) {
                remove(keyList[i]);
            }
        }
    }

    @Override
    public void exportNode(OutputStream ostream) throws IOException,
            BackingStoreException {
        if(ostream == null) {
            // prefs.5=Stream is null
            throw new NullPointerException(Messages.getString("prefs.5"));  //$NON-NLS-1$
        }
        checkState();
        XMLParser.exportPrefs(this, ostream, false);

    }

    @Override
    public void exportSubtree(OutputStream ostream) throws IOException,
            BackingStoreException {
        if(ostream == null) {
            // prefs.5=Stream is null
            throw new NullPointerException(Messages.getString("prefs.5"));  //$NON-NLS-1$
        }
        checkState();
        XMLParser.exportPrefs(this, ostream, true);
    }

    @Override
    public void flush() throws BackingStoreException {
        synchronized (lock) {
            flushSpi();
        }
        AbstractPreferences[] cc = cachedChildren();
        int i;
        for (i = 0; i < cc.length; i++) {
            cc[i].flush();
        }
    }

    @Override
    public String get(String key, String deflt) {
        if (key == null) {
            throw new NullPointerException();
        }
        String result;
        synchronized (lock) {
            checkState();
            try {
                result = getSpi(key);
            } catch (Exception e) {
                result = null;
            }
        }
        return (result == null ? deflt : result);
    }

    @Override
    public boolean getBoolean(String key, boolean deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        } else if (result.equalsIgnoreCase("true")) { //$NON-NLS-1$
            return true;
        } else if (result.equalsIgnoreCase("false")) { //$NON-NLS-1$
            return false;
        } else {
            return deflt;
        }
    }

    @Override
    public byte[] getByteArray(String key, byte[] deflt) {
        String svalue = get(key, null);
        if (svalue == null) {
            return deflt;
        }
        if (svalue.length() == 0) { 
            return new byte[0];
        }
        byte[] dres;
        try {
            byte[] bavalue = svalue.getBytes("US-ASCII"); //$NON-NLS-1$
            if (bavalue.length % 4 != 0) {
                return deflt;
            }
            dres = Base64.decode(bavalue);
        } catch (Exception e) {
            dres = deflt;
        }
        return dres;
    }

    @Override
    public double getDouble(String key, double deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        double dres;
        try {
            dres = Double.parseDouble(result);
        } catch (NumberFormatException e) {
            dres = deflt;
        }
        return dres;
    }

    @Override
    public float getFloat(String key, float deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        float fres;
        try {
            fres = Float.parseFloat(result);
        } catch (NumberFormatException e) {
            fres = deflt;
        }
        return fres;
    }

    @Override
    public int getInt(String key, int deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        int ires;
        try {
            ires = Integer.parseInt(result);
        } catch (NumberFormatException e) {
            ires = deflt;
        }
        return ires;
    }

    @Override
    public long getLong(String key, long deflt) {
        String result = get(key, null);
        if (result == null) {
            return deflt;
        }
        long lres;
        try {
            lres = Long.parseLong(result);
        } catch (NumberFormatException e) {
            lres = deflt;
        }
        return lres;
    }

    @Override
    public boolean isUserNode() {
        return root == Preferences.userRoot();
    }

    @Override
    public String[] keys() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            return keysSpi();
        }
    }

    @Override
    public String name() {
        return nodeName;
    }

    @Override
    public Preferences node(String name) {
        AbstractPreferences startNode = null;
        synchronized (lock) {
            checkState();
            validateName(name);
            if ("".equals(name)) { //$NON-NLS-1$
                return this;
            } else if ("/".equals(name)) { //$NON-NLS-1$
                return root;
            }
            if (name.startsWith("/")) { //$NON-NLS-1$
                startNode = root;
                name = name.substring(1);
            } else {
                startNode = this;
            }
        }
        Preferences result = null;
        try {
            result = startNode.nodeImpl(name, true);
        } catch (BackingStoreException e) {
            //should not happen
        }
        return result;
    }

    private void validateName(String name) {
        if (name.endsWith("/") && name.length() > 1) { //$NON-NLS-1$
            // prefs.6=Name cannot end with '/'\!
            throw new IllegalArgumentException(Messages.getString("prefs.6"));  //$NON-NLS-1$
        }
        if (name.indexOf("//") >= 0) { //$NON-NLS-1$
            // prefs.7=Name cannot contains consecutive '/'\!
            throw new IllegalArgumentException(
                    Messages.getString("prefs.7"));  //$NON-NLS-1$
        }
    }

    private AbstractPreferences nodeImpl(String path, boolean createNew)
            throws BackingStoreException {
        StringTokenizer st = new StringTokenizer(path, "/"); //$NON-NLS-1$
        AbstractPreferences currentNode = this;
        AbstractPreferences temp = null;
        while (st.hasMoreTokens() && null != currentNode) {
            String name = st.nextToken();
            synchronized (currentNode.lock) {
                temp = currentNode.cachedNode.get(name);
                if (temp == null) {
                    temp = getNodeFromBackend(createNew, currentNode, name);
                }
            }

            currentNode = temp;
        }
        return currentNode;
    }

    private AbstractPreferences getNodeFromBackend(boolean createNew,
            AbstractPreferences currentNode, String name)
            throws BackingStoreException {
        AbstractPreferences temp;
        if (name.length() > MAX_NAME_LENGTH) {
            // prefs.8=Name length is too long: {0}
            throw new IllegalArgumentException(Messages.getString("prefs.8",  //$NON-NLS-1$
                    name));
        }
        if (createNew) {
            temp = currentNode.childSpi(name);
            currentNode.cachedNode.put(name, temp);
            if (temp.newNode && currentNode.nodeChangeListeners.size() > 0) {
                currentNode.notifyChildAdded(temp);
            }
        } else {
            temp = currentNode.getChild(name);
        }
        return temp;
    }

    @Override
    public boolean nodeExists(String name) throws BackingStoreException {
        AbstractPreferences startNode = null;
        synchronized (lock) {
            if (isRemoved()) {
                if ("".equals(name)) { //$NON-NLS-1$
                    return false;
                }
                // prefs.9=This node has been removed\!
                throw new IllegalStateException(Messages.getString("prefs.9"));  //$NON-NLS-1$
            }
            validateName(name);
            if ("".equals(name) || "/".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                return true;
            }
            if (name.startsWith("/")) { //$NON-NLS-1$
                startNode = root;
                name = name.substring(1);
            } else {
                startNode = this;
            }
        }
        try {
            Preferences result = startNode.nodeImpl(name, false);
            return null == result ? false : true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Preferences parent() {
        checkState();
        return parentPref;
    }

    private void checkState() {
        if (isRemoved()) {
            // prefs.9=This node has been removed\!
            throw new IllegalStateException(Messages.getString("prefs.9"));  //$NON-NLS-1$
        }
    }

    @Override
    public void put(String key, String value) {
        if (null == key || null == value) {
            throw new NullPointerException();
        }
        if (key.length() > MAX_KEY_LENGTH || value.length() > MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            checkState();
            putSpi(key, value);
        }
        notifyPreferenceChange(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        String sval = String.valueOf(value);
        put(key, sval);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        try {
            put(key, Base64.encode(value, "US-ASCII")); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void putDouble(String key, double value) {
        String sval = Double.toString(value);
        put(key, sval);
    }

    @Override
    public void putFloat(String key, float value) {
        String sval = Float.toString(value);
        put(key, sval);
    }

    @Override
    public void putInt(String key, int value) {
        String sval = Integer.toString(value);
        put(key, sval);
    }

    @Override
    public void putLong(String key, long value) {
        String sval = Long.toString(value);
        put(key, sval);
    }

    @Override
    public void remove(String key) {
        synchronized (lock) {
            checkState();
            removeSpi(key);
        }
        notifyPreferenceChange(key, null);
    }

    @Override
    public void removeNode() throws BackingStoreException {
        if (root == this) {
            // prefs.A=Cannot remove root node\!
            throw new UnsupportedOperationException(Messages.getString("prefs.A"));  //$NON-NLS-1$
        }
        synchronized (parentPref.lock) {
            removeNodeImpl();
        }
    }

    private void removeNodeImpl() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            String[] childrenNames = childrenNamesSpi();
            for (int i = 0; i < childrenNames.length; i++) {
                if (null == cachedNode.get(childrenNames[i])) {
                    AbstractPreferences child = childSpi(childrenNames[i]);
                    cachedNode.put(childrenNames[i], child);
                }
            }
            AbstractPreferences[] children = cachedNode
                    .values().toArray(new AbstractPreferences[0]);
            for (int i = 0; i < children.length; i++) {
                children[i].removeNodeImpl();
            }
            removeNodeSpi();
            isRemoved = true;
            parentPref.cachedNode.remove(nodeName);
        }
        if (parentPref.nodeChangeListeners.size() > 0) {
            parentPref.notifyChildRemoved(this);
        }
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (null == ncl) {
            throw new NullPointerException();
        }
        checkState();
        synchronized (nodeChangeListeners) {
            nodeChangeListeners.add(ncl);
        }
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        if (null == pcl) {
            throw new NullPointerException();
        }
        checkState();
        synchronized (preferenceChangeListeners) {
            preferenceChangeListeners.add(pcl);
        }
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        checkState();
        synchronized (nodeChangeListeners) {
            int pos;
            if ((pos = nodeChangeListeners.indexOf(ncl)) == -1) {
                throw new IllegalArgumentException();
            }
            nodeChangeListeners.remove(pos);
        }
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        checkState();
        synchronized (preferenceChangeListeners) {
            int pos;
            if ((pos = preferenceChangeListeners.indexOf(pcl)) == -1) {
                throw new IllegalArgumentException();
            }
            preferenceChangeListeners.remove(pos);
        }
    }

    @Override
    public void sync() throws BackingStoreException {
        synchronized (lock) {
            checkState();
            syncSpi();
        }
        AbstractPreferences[] cc = cachedChildren();
        int i;
        for (i = 0; i < cc.length; i++) {
            cc[i].sync();
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(isUserNode() ? "User" : "System"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(" Preference Node: "); //$NON-NLS-1$
        sb.append(absolutePath());
        return sb.toString();
    }

    private void notifyChildAdded(Preferences child) {
        NodeChangeEvent nce = new NodeAddEvent(this, child);
        synchronized (events) {
            events.add(nce);
            events.notifyAll();
        }
    }

    private void notifyChildRemoved(Preferences child) {
        NodeChangeEvent nce = new NodeRemoveEvent(this, child);
        synchronized (events) {
            events.add(nce);
            events.notifyAll();
        }
    }

    private void notifyPreferenceChange(String key, String newValue) {
        PreferenceChangeEvent pce = new PreferenceChangeEvent(this, key,
                newValue);
        synchronized (events) {
            events.add(pce);
            events.notifyAll();
        }
    }

    private static class EventDispatcher extends Thread {
        EventDispatcher(String name){
            super(name);
        }
        
        @Override
        public void run() {
            while (true) {
                EventObject event = null;
                try {
                    event = getEventObject();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                AbstractPreferences pref = (AbstractPreferences) event
                        .getSource();
                if (event instanceof NodeAddEvent) {
                    dispatchNodeAdd((NodeChangeEvent) event,
                            pref.nodeChangeListeners);
                } else if (event instanceof NodeRemoveEvent) {
                    dispatchNodeRemove((NodeChangeEvent) event,
                            pref.nodeChangeListeners);
                } else if (event instanceof PreferenceChangeEvent) {
                    dispatchPrefChange((PreferenceChangeEvent) event,
                            pref.preferenceChangeListeners);
                }
            }
        }

        private EventObject getEventObject() throws InterruptedException {
            synchronized (events) {
                if (events.isEmpty()) {
                    events.wait();
                }
                EventObject event = events.get(0);
                events.remove(0);
                return event;
            }
        }

        private void dispatchPrefChange(PreferenceChangeEvent event,
                List<EventListener> preferenceChangeListeners) {
            synchronized (preferenceChangeListeners) {
                Iterator<EventListener> i = preferenceChangeListeners.iterator();
                while (i.hasNext()) {
                    PreferenceChangeListener pcl = (PreferenceChangeListener) i
                            .next();
                    pcl.preferenceChange(event);
                }
            }
        }

        private void dispatchNodeRemove(NodeChangeEvent event,
                List<EventListener> nodeChangeListeners) {
            synchronized (nodeChangeListeners) {
                Iterator<EventListener> i = nodeChangeListeners.iterator();
                while (i.hasNext()) {
                    NodeChangeListener ncl = (NodeChangeListener) i.next();
                    ncl.childRemoved(event);
                }
            }
        }

        private void dispatchNodeAdd(NodeChangeEvent event,
                List<EventListener> nodeChangeListeners) {
            synchronized (nodeChangeListeners) {
                Iterator<EventListener> i = nodeChangeListeners.iterator();
                while (i.hasNext()) {
                    NodeChangeListener ncl = (NodeChangeListener) i.next();
                    ncl.childAdded(event);
                }
            }
        }
    }

    private static class NodeAddEvent extends NodeChangeEvent {
        //The base class is NOT serializable, so this class isn't either.
        private static final long serialVersionUID = 1L;

        public NodeAddEvent(Preferences p, Preferences c) {
            super(p, c);
        }
    }

    private static class NodeRemoveEvent extends NodeChangeEvent {
        //The base class is NOT serializable, so this class isn't either.
        private static final long serialVersionUID = 1L;
        
        public NodeRemoveEvent(Preferences p, Preferences c) {
            super(p, c);
        }
    }
}
