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

    /** Pointer to native state. */
    int pointer = 0;

    /** The thread that collects samples. */
    Thread samplingThread;

    /** Whether or not the profiler is running. */
    boolean running = false;
    int delayPerThread; // ms

    /** Number of samples taken. */
    int sampleCount = 0;

    /** Total time spent collecting samples. */
    long totalSampleTime = 0;

    private SamplingProfiler() {}

    /**
     * Returns true if the profiler is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Starts collecting samples.
     *
     * @param threadsPerSecond number of threads to sample per second
     */
    public synchronized void start(int threadsPerSecond) {
        if (threadsPerSecond < 1) {
            throw new IllegalArgumentException("threadsPerSecond < 1");
        }
        if (!running) {
            logger.info("Starting profiler.");
            running = true;
            if (samplingThread == null) {
                // TODO: Priority?
                samplingThread = new Thread(new Sampler());
                samplingThread.setDaemon(true);
                samplingThread.start();
            } else {
                notifyAll();
            }
        }
        delayPerThread = 1000 / threadsPerSecond;
    }

    /**
     * Stops sample collection.
     */
    public synchronized void stop() {
        if (running) {
            logger.info("Stopping profiler.");
            running = false;
        }
    }

    /**
     * Captures collected samples and clears the sample set. Returns null
     * if no data has been captured.
     *
     * <p>Note: The exact format is not documented because it's not set in
     * stone yet.
     */
    public synchronized byte[] snapshot() {
        if (pointer == 0 || sampleCount == 0) {
            return null;
        }

        int size = size(pointer);
        int collisions = collisions(pointer);

        long start = System.nanoTime();
        byte[] bytes = snapshot(pointer);
        long elapsed = System.nanoTime() - start;

        logger.info("Grabbed snapshot in " + (elapsed / 1000) + "us."
                + " Samples collected: " + sampleCount
                + ", Average sample time (per thread): "
                    + (((totalSampleTime / sampleCount) << 10) / 1000) + "us"
                + ", Set size: " + size
                + ", Collisions: " + collisions);
        sampleCount = 0;
        totalSampleTime = 0;

        return bytes;
    }

    /**
     * Identifies the "event thread". For a user-facing application, this
     * might be the UI thread. For a background process, this might be the
     * thread that processes incoming requests.
     */
    public synchronized void setEventThread(Thread eventThread) {
        if (pointer == 0) {
            pointer = allocate();
        }
        setEventThread(pointer, eventThread);
    }

    /** Collects some data. Returns number of threads sampled. */
    private static native int sample(int pointer);

    /** Allocates native state. */
    private static native int allocate();

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
                int threadsSampled;
                synchronized (SamplingProfiler.this) {
                    if (!running) {
                        logger.info("Stopped profiler.");
                        while (!running) {
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
                        logger.info("Started profiler.");
                        firstSample = false;
                    }

                    long start = System.nanoTime();
                    threadsSampled = sample(pointer);
                    long elapsed = System.nanoTime() - start;

                    sampleCount += threadsSampled;
                    totalSampleTime += elapsed >> 10; // shift avoids overflow.
                }

                try {
                    Thread.sleep(delayPerThread * threadsSampled);
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
        sb.append("      running: ").append(in.readShort()).append('\n');
        sb.append("      native: ").append(in.readShort()).append('\n');
        sb.append("      suspended: ").append(in.readShort()).append('\n');
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
