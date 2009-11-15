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

package java.net;

import java.security.AccessController;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.harmony.luni.util.PriviAction;

/**
 * This class is used to maintain the negative name lookup cache, which caches
 * host names which could not be resolved, as a security feature.
 *
 * @see NegCacheElement
 */
class NegativeCache {
   // maximum number of entries in the cache
    private static final int MAX_NEGATIVE_ENTRIES = 5;

    // the loading for the cache
    private static final float LOAD_FACTOR = 0.75F;

    private static final Map<String, NegCacheElement> negCache
            = new LinkedHashMap<String, NegCacheElement>(
                2 * MAX_NEGATIVE_ENTRIES, LOAD_FACTOR, true) {

        /**
         * Returns whether the eldest entry should be removed. It is removed if
         * the size has grown beyond the maximum size allowed for the cache.
         *
         * @param eldest
         *            the LRU entry, which will be deleted if we return true.
         */
        @Override protected boolean removeEldestEntry(
                Entry<String, NegCacheElement> eldest) {
            return size() > MAX_NEGATIVE_ENTRIES;
        }
    };

    /** Ensures non-instantiability */
    private NegativeCache() {
    }

    /**
     * Adds the host name and the corresponding name lookup fail message to the
     * cache.
     *
     * @param hostName
     *            the name of the host for which the lookup failed.
     * @param failedMessage
     *            the message returned when the lookup fails.
     */
    static synchronized void put(String hostName, String failedMessage) {
        negCache.put(hostName, new NegCacheElement(failedMessage));
    }

    /**
     * Returns the message of the negative cache if the entry has not yet
     * expired.
     *
     * @param hostName
     *            the name of the host for which we look up the entry.
     * @return the message which was returned when the host lookup failed if the
     *         entry has not yet expired.
     */
    static synchronized String getFailedMessage(String hostName) {
        NegCacheElement element = negCache.get(hostName);
        if (element != null) {
            // check if element is still valid
            String ttlValue = AccessController
                    .doPrivileged(new PriviAction<String>(
                            "networkaddress.cache.negative.ttl")); //$NON-NLS-1$
            int ttl = 10;
            try {
                if (ttlValue != null) {
                    ttl = Integer.decode(ttlValue);
                }
            } catch (NumberFormatException e) {
                // If exception, go with ttl == 10
            }
            if (ttl == 0) {
                negCache.clear();
                element = null;
            } else if (ttl != -1) {
                // BEGIN android-changed
                long delta = System.nanoTime() - element.nanoTimeAdded;
                if (delta > secondsToNanos(ttl)) {
                    // remove the element from the cache and return null
                    negCache.remove(hostName);
                    element = null;
                }
                // END android-changed
            }
        }
        if (element != null) {
            return element.failedMessage;
        }
        return null;
    }

    // BEGIN android-added
    /**
     * Multiplies value by 1 billion.
     */
    private static int secondsToNanos(int ttl) {
        return ttl * 1000000000;
    }
    // END android-added
}
