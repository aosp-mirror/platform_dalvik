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

package java.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CalendarTest extends junit.framework.TestCase {
    // http://code.google.com/p/android/issues/detail?id=6184
    public void test_setTimeZone() {
        // The specific time zones don't matter; they just have to be different so we can see that
        // get(Calendar.ZONE_OFFSET) returns the zone offset of the time zone passed to setTimeZone.
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(0, cal.get(Calendar.ZONE_OFFSET));
        TimeZone tz = java.util.TimeZone.getTimeZone("GMT+7");
        cal.setTimeZone(tz);
        assertEquals(25200000, cal.get(Calendar.ZONE_OFFSET));
    }
}
