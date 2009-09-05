package dalvik.system;

import java.util.logging.Logger;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A sampling profiler.
 *
 * @hide
 */
public class SamplingProfiler {

    private static final Logger logger = Logger.getLogger(
            SamplingProfiler.class.getName());

    static final boolean DEBUG = false;

    enum State {
        /** The sampling thread hasn't started or is waiting to resume. */
        PAUSED,
        /** The sampling thread is collecting samples. */
        RUNNING,
        /** The sampling thread is shutting down. */
        SHUTTING_DOWN
    }

    /** Pointer to native state. */
    int pointer = 0;

    /** The thread that collects samples. */
    Thread samplingThread;

    /** Time between samples. */
    volatile int delay; // ms

    /** Number of samples taken (~samples per second * number of threads). */
    int totalThreadsSampled = 0;

    /** Total time spent collecting samples. */
    long totalSampleTime = 0;

    /** The state of the profiler. */
    volatile State state = State.PAUSED;

    private SamplingProfiler() {}

    /**
     * Returns true if the profiler is running.
     */
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    /**
     * Starts collecting samples.
     *
     * @param samplesPerSecond number of times to sample each thread per second
     * @throws IllegalStateException if the profiler is
     *  {@linkplain #shutDown()} shutting down}
     */
    public synchronized void start(int samplesPerSecond) {
        if (samplesPerSecond < 1) {
            throw new IllegalArgumentException("samplesPerSecond < 1");
        }
        ensureNotShuttingDown();
        delay = 1000 / samplesPerSecond;
        if (!isRunning()) {
            if (DEBUG) logger.info("Starting profiler...");
            state = State.RUNNING;
            if (samplingThread == null) {
                // TODO: Priority?
                samplingThread = new Thread(new Sampler(), "SamplingProfiler");
                samplingThread.setDaemon(true);
                samplingThread.start();
            } else {
                notifyAll();
            }
        }
    }

    /**
     * Pauses sample collection.
     */
    public synchronized void pause() {
        if (isRunning()) {
            if (DEBUG) logger.info("Pausing profiler...");
            state = State.PAUSED;
        }
    }

    /**
     * Captures collected samples and clears the sample set. Returns null
     * if no data has been captured.
     *
     * <p>Note: The exact format is not documented because it's not set in
     * stone yet.
     *
     * @throws IllegalStateException if the profiler is
     *  {@linkplain #shutDown()} shutting down}
     */
    public synchronized byte[] snapshot() {
        ensureNotShuttingDown();
        if (pointer == 0 || totalThreadsSampled == 0) {
            return null;
        }

        if (DEBUG) {
            int size = size(pointer);
            int collisions = collisions(pointer);

            long start = System.nanoTime();
            byte[] bytes = snapshot(pointer);
            long elapsed = System.nanoTime() - start;

            long averageSampleTime = ((totalSampleTime / totalThreadsSampled)
                    << 10) / 1000;
            logger.info("Grabbed snapshot in " + (elapsed / 1000) + "us."
                    + " Samples collected: " + totalThreadsSampled
                    + ", Average sample time (per thread): "
                            + averageSampleTime + "us"
                    + ", Set size: " + size
                    + ", Collisions: " + collisions);
            totalThreadsSampled = 0;
            totalSampleTime = 0;

            return bytes;
        } else {
            totalThreadsSampled = 0;
            return snapshot(pointer);
        }
    }

    /**
     * Identifies the "event thread". For a user-facing application, this
     * might be the UI thread. For a background process, this might be the
     * thread that processes incoming requests.
     *
     * @throws IllegalStateException if the profiler is
     *  {@linkplain #shutDown()} shutting down}
     */
    public synchronized void setEventThread(Thread eventThread) {
        ensureNotShuttingDown();
        if (pointer == 0) {
            pointer = allocate();
        }
        setEventThread(pointer, eventThread);
    }

    private void ensureNotShuttingDown() {
        if (state == State.SHUTTING_DOWN) {
            throw new IllegalStateException("Profiler is shutting down.");
        }
    }

    /**
     * Shuts down the profiler thread and frees native memory. The profiler
     * will recreate the thread the next time {@link #start(int)} is called.
     *
     * @throws IllegalStateException if the profiler is already shutting down
     */
    public void shutDown() {
        Thread toStop;
        synchronized (this) {
            ensureNotShuttingDown();

            toStop = samplingThread;
            if (toStop == null) {
                // The profiler hasn't started yet.
                return;
            }

            state = State.SHUTTING_DOWN;
            samplingThread = null;
            notifyAll();
        }

        // Release lock to 'this' so background thread can grab it and stop.
        boolean successful = false;
        while (!successful) {
            try {
                toStop.join();
                successful = true;
            } catch (InterruptedException e) { /* ignore */ }
        }

        synchronized (this) {
            if (pointer != 0) {
                free(pointer);
                pointer = 0;
            }

            totalThreadsSampled = 0;
            totalSampleTime = 0;
            state = State.PAUSED;
        }
    }

    /** Collects some data. Returns number of threads sampled. */
    private static native int sample(int pointer);

    /** Allocates native state. */
    private static native int allocate();

    /** Frees native state. */
    private static native void free(int pointer);

    /** Gets the number of methods in the sample set. */
    private static native int size(int pointer);

    /** Gets the number of collisions in the sample set. */
    private static native int collisions(int pointer);

    /** Captures data. */
    private static native byte[] snapshot(int pointer);

    /** Identifies the "event thread". */
    private static native void setEventThread(int pointer, Thread thread);

    /**
     * Background thread that collects samples.
     */
    class Sampler implements Runnable {
        public void run() {
            boolean firstSample = true;
            while (true) {
                synchronized (SamplingProfiler.this) {
                    if (!isRunning()) {
                        if (DEBUG) logger.info("Paused profiler.");
                        while (!isRunning()) {
                            if (state == State.SHUTTING_DOWN) {
                                // Stop thread.
                                return;
                            }

                            try {
                                SamplingProfiler.this.wait();
                            } catch (InterruptedException e) { /* ignore */ }
                        }
                        firstSample = true;
                    }

                    if (pointer == 0) {
                        pointer = allocate();
                    }

                    if (firstSample) {
                        if (DEBUG) logger.info("Started profiler.");
                        firstSample = false;
                    }

                    if (DEBUG) {
                        long start = System.nanoTime();
                        int threadsSampled = sample(pointer);
                        long elapsed = System.nanoTime() - start;

                        totalThreadsSampled += threadsSampled;
                        totalSampleTime += elapsed >> 10; // avoids overflow.
                    } else {
                        totalThreadsSampled += sample(pointer);
                    }
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) { /* ignore */ }
            }
        }
    }

    /**
     * Dumps a snapshot to the log. Useful for debugging.
     */
    public static void logSnapshot(byte[] snapshot) {
        DataInputStream in = new DataInputStream(
                new ByteArrayInputStream(snapshot));
        try {
            int version = in.readUnsignedShort();
            int classCount = in.readUnsignedShort();
            StringBuilder sb = new StringBuilder();
            sb.append("version=").append(version).append(' ')
                    .append("classes=").append(classCount).append('\n');
            logger.info(sb.toString());
            for (int i = 0; i < classCount; i++) {
                sb = new StringBuilder();
                sb.append("class ").append(in.readUTF()).append('\n');
                int methodCount = in.readUnsignedShort();
                for (int m = 0; m < methodCount; m++) {
                    sb.append("  ").append(in.readUTF()).append(":\n");
                    sb.append("    event:\n");
                    appendCounts(in, sb);
                    sb.append("    other:\n");
                    appendCounts(in, sb);
                }
                logger.info(sb.toString());
            }
        } catch (IOException e) {
            logger.warning(e.toString());
        }
    }

    private static void appendCounts(DataInputStream in, StringBuilder sb)
            throws IOException {
        sb.append("      running:\n");
        sb.append("        caller: ").append(in.readShort()).append('\n');
        sb.append("        leaf: ").append(in.readShort()).append('\n');
        sb.append("      suspended:\n");
        sb.append("        caller: ").append(in.readShort()).append('\n');
        sb.append("        leaf: ").append(in.readShort()).append('\n');
    }

    /** This will be allocated when the user calls getInstance(). */
    private static final SamplingProfiler instance = new SamplingProfiler();

    /**
     * Gets the profiler. The profiler is not running by default. Start it
     * with {@link #start(int)}.
     */
    public static synchronized SamplingProfiler getInstance() {
        return instance;
    }
}
