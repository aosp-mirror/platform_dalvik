/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 * Implements caching for {@code InetAddress}. We use a unified cache for both positive and negative
 * cache entries.
 */
class AddressCache {
    /**
     * When the cache contains more entries than this, we start dropping the oldest ones.
     * This should be a power of two to avoid wasted space in our custom map.
     */
    private static final int MAX_ENTRIES = 512;
    
    // This isn't used by our HashMap implementation, but the API demands it.
    private static final float DEFAULT_LOAD_FACTOR = .75F;
    
    // Default time-to-live for positive cache entries. 600 seconds (10 minutes).
    private static final long DEFAULT_POSITIVE_TTL_NANOS = 600 * 1000000000L;
    // Default time-to-live for negative cache entries. 10 seconds.
    private static final long DEFAULT_NEGATIVE_TTL_NANOS = 10 * 1000000000L;
    
    // Failed lookups are represented in the cache my mappings to this empty array.
    private static final InetAddress[] NO_ADDRESSES = new InetAddress[0];
    
    // The actual cache.
    private final Map<String, AddressCacheEntry> map;
    
    class AddressCacheEntry {
        // The addresses. May be the empty array for a negative cache entry.
        InetAddress[] addresses;
        
        /**
         * The absolute expiry time in nanoseconds. Nanoseconds from System.nanoTime is ideal
         * because -- unlike System.currentTimeMillis -- it can never go backwards.
         * 
         * Unless we need to cope with DNS TTLs of 292 years, we don't need to worry about overflow.
         */
        long expiryNanos;
        
        AddressCacheEntry(InetAddress[] addresses, long expiryNanos) {
            this.addresses = addresses;
            this.expiryNanos = expiryNanos;
        }
    }
    
    public AddressCache() {
        // We pass 'true' so removeEldestEntry removes the least-recently accessed entry, rather
        // than the least-recently inserted.
        map = new LinkedHashMap<String, AddressCacheEntry>(0, DEFAULT_LOAD_FACTOR, true) {
            @Override protected boolean removeEldestEntry(Entry<String, AddressCacheEntry> eldest) {
                // By the time this method is called, the new entry has already been inserted and
                // the map will have grown to accommodate it. Using == lets us prevent resizing.
                return size() == MAX_ENTRIES;
            }
        };
    }
    
    /**
     * Returns the cached addresses associated with 'hostname'. Returns null if nothing is known
     * about 'hostname'. Returns an empty array if 'hostname' is known not to exist.
     */
    public InetAddress[] get(String hostname) {
        AddressCacheEntry entry;
        synchronized (map) {
            entry = map.get(hostname);
        }
        // Do we have a valid cache entry?
        if (entry != null && entry.expiryNanos >= System.nanoTime()) {
            return entry.addresses;
        }
        // Either we didn't find anything, or it had expired.
        // No need to remove expired entries: the caller will provide a replacement shortly.
        return null;
    }
    
    /**
     * Associates the given 'addresses' with 'hostname'. The association will expire after a
     * certain length of time.
     */
    public void put(String hostname, InetAddress[] addresses) {
        // Calculate the expiry time.
        boolean isPositive = (addresses.length > 0);
        String propertyName = isPositive ? "networkaddress.cache.ttl" : "networkaddress.cache.negative.ttl";
        long defaultTtlNanos = isPositive ? DEFAULT_POSITIVE_TTL_NANOS : DEFAULT_NEGATIVE_TTL_NANOS;
        // Fast-path the default case...
        long expiryNanos = System.nanoTime() + defaultTtlNanos;
        if (System.getSecurityManager() != null || System.getProperty(propertyName, null) != null) {
            // ...and let those using a SecurityManager or custom properties pay full price.
            expiryNanos = customTtl(propertyName, defaultTtlNanos);
            if (expiryNanos == Long.MIN_VALUE) {
                return;
            }
        }
        // Update the cache.
        synchronized (map) {
            map.put(hostname, new AddressCacheEntry(addresses, expiryNanos));
        }
    }
    
    /**
     * Records that 'hostname' is known not to have any associated addresses. (I.e. insert a
     * negative cache entry.)
     */
    public void putUnknownHost(String hostname) {
        put(hostname, NO_ADDRESSES);
    }
    
    private long customTtl(String propertyName, long defaultTtlNanos) {
        String ttlString = AccessController.doPrivileged(new PriviAction<String>(propertyName, null));
        if (ttlString == null) {
            return System.nanoTime() + defaultTtlNanos;
        }
        try {
            long ttlS = Long.parseLong(ttlString);
            // For the system properties, -1 means "cache forever" and 0 means "don't cache".
            if (ttlS == -1) {
                return Long.MAX_VALUE;
            } else if (ttlS == 0) {
                return Long.MIN_VALUE;
            } else {
                return System.nanoTime() + ttlS * 1000000000L;
            }
        } catch (NumberFormatException ex) {
            return System.nanoTime() + defaultTtlNanos;
        }
    }
}
