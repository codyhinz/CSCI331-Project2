import java.util.Random;

public class BoundedBufferExperiment {
    private int[] buffer;
    private int n;
    private int count = 0;
    private boolean raceConditionDetected = false;

    /**
     * Constructor for the BoundedBufferExperiment class.
     * 
     * @param size The size of the buffer.
     */
    public BoundedBufferExperiment(int size) {
        buffer = new int[size];
        n = size;
    }

    /**
     * Producer thread method.
     * Adds elements to the buffer in bursts of size k, with a small delay between each addition.
     * Continues until a race condition is detected.
     * 
     * @param k The number of slots the producer adds in each burst.
     * @param t The time in milliseconds between each slot addition.
     */
    public void producerThread(int k, int t) {
        Random random = new Random();
        while (!raceConditionDetected) {
            for (int i = 0; i < k; i++) {
                int nextIn = buffer[n - 1];
                sleepForTime(t); // Add a small delay before incrementing
                if (nextIn == buffer[n - 1]) {
                    buffer[n - 1] = (nextIn + 1) % n;
                    System.out.println("So far okay " + count);
                    count++;
                }
            }
            sleepForRandomTime(random);
        }
    }

    /**
     * Consumer thread method.
     * Reads elements from the buffer and checks for race conditions.
     * Continues until a race condition is detected.
     */
    public void consumerThread() {
        Random random = new Random();
        while (!raceConditionDetected) {
            int nextOut = buffer[0];
            if (nextOut > 1) {
                reportRaceCondition("Race condition detected!");
                raceConditionDetected = true;
            } else {
                // Shift all elements in the buffer to the left
                for (int j = 0; j < n - 1; j++) {
                    buffer[j] = buffer[j + 1];
                }
                buffer[n - 1] = 0;
            }
            sleepForRandomTime(random);
        }
    }

    /**
     * Helper method to make the thread sleep for a specified time.
     * 
     * @param time The time in milliseconds for the thread to sleep.
     */
    private void sleepForTime(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to make the thread sleep for a random time interval.
     * 
     * @param random The Random object used to generate the random sleep time.
     */
    private void sleepForRandomTime(Random random) {
        try {
            Thread.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to report a race condition.
     * 
     * @param message The message to be displayed when a race condition is detected.
     */
    private void reportRaceCondition(String message) {
        System.out.println(message);
    }

    /**
     * Main method to run the bounded buffer experiment.
     * 
     * @param args Command line arguments (not used in this program).
     */
    public static void main(String[] args) {
        int n = 10; // Buffer size
        int k = 2; // Number of slots producer adds in each burst
        int t = 10; // Time in milliseconds between each slot addition

        BoundedBufferExperiment buffer = new BoundedBufferExperiment(n);

        // Create and start the producer and consumer threads
        Thread producerThread = new Thread(() -> buffer.producerThread(k, t));
        Thread consumerThread = new Thread(buffer::consumerThread);
        producerThread.start();
        consumerThread.start();
    }
}