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

package org.apache.harmony.luni.util;

import java.text.NumberFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Manages a locale-specific thread-local cache of expensive locale-specific
 * format objects.  The objects are discarded if the locale requested changes,
 * or if heap space is exhausted.
 */
public class LocaleCache {

    private static final ThreadLocalCache<LocaleCache> cache = new ThreadLocalCache<LocaleCache>();

    private NumberFormat numberFormat = null;

    private final Locale locale;

    private LocaleCache(Locale locale) {
        this.locale = locale;
    }

    /**
     * Re-uses or creates a LocaleCache object for the specified Locale.
     * LocaleCache objects are reused within a thread as long as they have
     * the same Locale (which must not be null).
     */
    private static LocaleCache getLocaleCache(Locale locale) {
        LocaleCache lc = cache.get();
        if (lc == null || !lc.locale.equals(locale)) {
            lc = new LocaleCache(locale);
            cache.set(lc);
        }
        return lc;
    }

    /**
     * Returns a NumberFormat object for the specified Locale.
     */
    public static NumberFormat getNumberFormat(Locale locale) {
        LocaleCache lc = getLocaleCache(locale);
        if (lc.numberFormat == null) {
            lc.numberFormat = NumberFormat.getInstance(locale);
        }

        // NumberFormat is mutable, so return a new clone each time.
        return (NumberFormat) lc.numberFormat.clone();
    }
}
