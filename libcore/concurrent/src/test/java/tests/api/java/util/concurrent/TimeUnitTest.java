/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import junit.framework.*;
import java.util.concurrent.*;
import java.io.*;

public class TimeUnitTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(TimeUnitTest.class);
    }

    // (loops to 88888 check increments at all time divisions.)

    /**
     * convert correctly converts sample values across the units
     */
    public void testConvert() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*60*60*24,
                         TimeUnit.SECONDS.convert(t,
                                                  TimeUnit.DAYS));
            assertEquals(t*60*60,
                         TimeUnit.SECONDS.convert(t,
                                                  TimeUnit.HOURS));
            assertEquals(t*60,
                         TimeUnit.SECONDS.convert(t,
                                                  TimeUnit.MINUTES));
            assertEquals(t,
                         TimeUnit.SECONDS.convert(t,
                                                  TimeUnit.SECONDS));
            assertEquals(t,
                         TimeUnit.SECONDS.convert(1000L*t,
                                                  TimeUnit.MILLISECONDS));
            assertEquals(t,
                         TimeUnit.SECONDS.convert(1000000L*t,
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t,
                         TimeUnit.SECONDS.convert(1000000000L*t,
                                                  TimeUnit.NANOSECONDS));


            assertEquals(1000L*t*60*60*24,
                         TimeUnit.MILLISECONDS.convert(t,
                                                  TimeUnit.DAYS));
            assertEquals(1000L*t*60*60,
                         TimeUnit.MILLISECONDS.convert(t,
                                                  TimeUnit.HOURS));
            assertEquals(1000L*t*60,
                         TimeUnit.MILLISECONDS.convert(t,
                                                  TimeUnit.MINUTES));
            assertEquals(1000L*t,
                         TimeUnit.MILLISECONDS.convert(t,
                                                  TimeUnit.SECONDS));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.convert(t,
                                                  TimeUnit.MILLISECONDS));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.convert(1000L*t,
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.convert(1000000L*t,
                                                  TimeUnit.NANOSECONDS));

            assertEquals(1000000L*t*60*60*24,
                         TimeUnit.MICROSECONDS.convert(t,
                                                  TimeUnit.DAYS));
            assertEquals(1000000L*t*60*60,
                         TimeUnit.MICROSECONDS.convert(t,
                                                  TimeUnit.HOURS));
            assertEquals(1000000L*t*60,
                         TimeUnit.MICROSECONDS.convert(t,
                                                  TimeUnit.MINUTES));
            assertEquals(1000000L*t,
                         TimeUnit.MICROSECONDS.convert(t,
                                                  TimeUnit.SECONDS));
            assertEquals(1000L*t,
                         TimeUnit.MICROSECONDS.convert(t,
                                                  TimeUnit.MILLISECONDS));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.convert(t,
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.convert(1000L*t,
                                                  TimeUnit.NANOSECONDS));

            assertEquals(1000000000L*t*60*60*24,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.DAYS));
            assertEquals(1000000000L*t*60*60,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.HOURS));
            assertEquals(1000000000L*t*60,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.MINUTES));
            assertEquals(1000000000L*t,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.SECONDS));
            assertEquals(1000000L*t,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.MILLISECONDS));
            assertEquals(1000L*t,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.convert(t,
                                                  TimeUnit.NANOSECONDS));
        }
    }

    /**
     * toNanos correctly converts sample values in different units to
     * nanoseconds
     */
    public void testToNanos() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*1000000000L*60*60*24,
                         TimeUnit.DAYS.toNanos(t));
            assertEquals(t*1000000000L*60*60,
                         TimeUnit.HOURS.toNanos(t));
            assertEquals(t*1000000000L*60,
                         TimeUnit.MINUTES.toNanos(t));
            assertEquals(1000000000L*t,
                         TimeUnit.SECONDS.toNanos(t));
            assertEquals(1000000L*t,
                         TimeUnit.MILLISECONDS.toNanos(t));
            assertEquals(1000L*t,
                         TimeUnit.MICROSECONDS.toNanos(t));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toNanos(t));
        }
    }

    /**
     * toMicros correctly converts sample values in different units to
     * microseconds
     */
    public void testToMicros() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*1000000L*60*60*24,
                         TimeUnit.DAYS.toMicros(t));
            assertEquals(t*1000000L*60*60,
                         TimeUnit.HOURS.toMicros(t));
            assertEquals(t*1000000L*60,
                         TimeUnit.MINUTES.toMicros(t));
            assertEquals(1000000L*t,
                         TimeUnit.SECONDS.toMicros(t));
            assertEquals(1000L*t,
                         TimeUnit.MILLISECONDS.toMicros(t));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.toMicros(t));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toMicros(t*1000L));
        }
    }

    /**
     * toMillis correctly converts sample values in different units to
     * milliseconds
     */
    public void testToMillis() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*1000L*60*60*24,
                         TimeUnit.DAYS.toMillis(t));
            assertEquals(t*1000L*60*60,
                         TimeUnit.HOURS.toMillis(t));
            assertEquals(t*1000L*60,
                         TimeUnit.MINUTES.toMillis(t));
            assertEquals(1000L*t,
                         TimeUnit.SECONDS.toMillis(t));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.toMillis(t));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.toMillis(t*1000L));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toMillis(t*1000000L));
        }
    }

    /**
     * toSeconds correctly converts sample values in different units to
     * seconds
     */
    public void testToSeconds() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*60*60*24,
                         TimeUnit.DAYS.toSeconds(t));
            assertEquals(t*60*60,
                         TimeUnit.HOURS.toSeconds(t));
            assertEquals(t*60,
                         TimeUnit.MINUTES.toSeconds(t));
            assertEquals(t,
                         TimeUnit.SECONDS.toSeconds(t));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.toSeconds(t*1000L));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.toSeconds(t*1000000L));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toSeconds(t*1000000000L));
        }
    }

    /**
     * toMinutes correctly converts sample values in different units to
     * minutes
     */
    public void testToMinutes() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*60*24,
                         TimeUnit.DAYS.toMinutes(t));
            assertEquals(t*60,
                         TimeUnit.HOURS.toMinutes(t));
            assertEquals(t,
                         TimeUnit.MINUTES.toMinutes(t));
            assertEquals(t,
                         TimeUnit.SECONDS.toMinutes(t*60));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.toMinutes(t*1000L*60));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.toMinutes(t*1000000L*60));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toMinutes(t*1000000000L*60));
        }
    }

    /**
     * toHours correctly converts sample values in different units to
     * hours
     */
    public void testToHours() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t*24,
                         TimeUnit.DAYS.toHours(t));
            assertEquals(t,
                         TimeUnit.HOURS.toHours(t));
            assertEquals(t,
                         TimeUnit.MINUTES.toHours(t*60));
            assertEquals(t,
                         TimeUnit.SECONDS.toHours(t*60*60));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.toHours(t*1000L*60*60));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.toHours(t*1000000L*60*60));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toHours(t*1000000000L*60*60));
        }
    }

    /**
     * toDays correctly converts sample values in different units to
     * days
     */
    public void testToDays() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t,
                         TimeUnit.DAYS.toDays(t));
            assertEquals(t,
                         TimeUnit.HOURS.toDays(t*24));
            assertEquals(t,
                         TimeUnit.MINUTES.toDays(t*60*24));
            assertEquals(t,
                         TimeUnit.SECONDS.toDays(t*60*60*24));
            assertEquals(t,
                         TimeUnit.MILLISECONDS.toDays(t*1000L*60*60*24));
            assertEquals(t,
                         TimeUnit.MICROSECONDS.toDays(t*1000000L*60*60*24));
            assertEquals(t,
                         TimeUnit.NANOSECONDS.toDays(t*1000000000L*60*60*24));
        }
    }


    /**
     * convert saturates positive too-large values to Long.MAX_VALUE
     * and negative to LONG.MIN_VALUE
     */
    public void testConvertSaturate() {
        assertEquals(Long.MAX_VALUE,
                     TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE / 2,
                                                  TimeUnit.SECONDS));
        assertEquals(Long.MIN_VALUE,
                     TimeUnit.NANOSECONDS.convert(-Long.MAX_VALUE / 4,
                                                  TimeUnit.SECONDS));
        assertEquals(Long.MAX_VALUE,
                     TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE / 2,
                                                  TimeUnit.MINUTES));
        assertEquals(Long.MIN_VALUE,
                     TimeUnit.NANOSECONDS.convert(-Long.MAX_VALUE / 4,
                                                  TimeUnit.MINUTES));
        assertEquals(Long.MAX_VALUE,
                     TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE / 2,
                                                  TimeUnit.HOURS));
        assertEquals(Long.MIN_VALUE,
                     TimeUnit.NANOSECONDS.convert(-Long.MAX_VALUE / 4,
                                                  TimeUnit.HOURS));
        assertEquals(Long.MAX_VALUE,
                     TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE / 2,
                                                  TimeUnit.DAYS));
        assertEquals(Long.MIN_VALUE,
                     TimeUnit.NANOSECONDS.convert(-Long.MAX_VALUE / 4,
                                                  TimeUnit.DAYS));
    }

    /**
     * toNanos saturates positive too-large values to Long.MAX_VALUE
     * and negative to LONG.MIN_VALUE
     */
    public void testToNanosSaturate() {
        assertEquals(Long.MAX_VALUE,
                     TimeUnit.MILLISECONDS.toNanos(Long.MAX_VALUE / 2));
        assertEquals(Long.MIN_VALUE,
                     TimeUnit.MILLISECONDS.toNanos(-Long.MAX_VALUE / 3));
    }


    /**
     * toString returns string containing common name of unit
     */
    public void testToString() {
        String s = TimeUnit.SECONDS.toString();
        assertTrue(s.indexOf("ECOND") >= 0);
    }


    /**
     *  Timed wait without holding lock throws
     *  IllegalMonitorStateException
     */
    public void testTimedWait_IllegalMonitorException() throws Exception {
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                Object o = new Object();
                TimeUnit tu = TimeUnit.MILLISECONDS;
                try {
                    tu.timedWait(o,LONG_DELAY_MS);
                    threadShouldThrow();
                } catch (IllegalMonitorStateException success) {}}});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }

    /**
     * timedWait throws InterruptedException when interrupted
     */
    public void testTimedWait() throws InterruptedException {
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                Object o = new Object();

                TimeUnit tu = TimeUnit.MILLISECONDS;
                synchronized(o) {
                    tu.timedWait(o,MEDIUM_DELAY_MS);
                }
            }});
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }


    /**
     * timedJoin throws InterruptedException when interrupted
     */
    public void testTimedJoin() throws InterruptedException {
        final Thread s = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                Thread.sleep(MEDIUM_DELAY_MS);
            }});
        final Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                TimeUnit tu = TimeUnit.MILLISECONDS;
                tu.timedJoin(s, MEDIUM_DELAY_MS);
            }});;
        s.start();
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
        s.interrupt();
        s.join();
    }

    /**
     *  timedSleep throws InterruptedException when interrupted
     */
    public void testTimedSleep() throws InterruptedException {
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                TimeUnit tu = TimeUnit.MILLISECONDS;
                tu.sleep(MEDIUM_DELAY_MS);
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }

    /**
     * a deserialized serialized unit is the same instance
     */
    public void testSerialization() throws Exception {
        TimeUnit q = TimeUnit.MILLISECONDS;

        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
        out.writeObject(q);
        out.close();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
        TimeUnit r = (TimeUnit)in.readObject();
        assertSame(q, r);
    }

}
