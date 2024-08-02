import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBufferSolution {
    private int[] buffer;
    private int n;
    private Lock mutex = new ReentrantLock();
    private int producerCount = 0;
    private int consumerCount = 0;
    private boolean raceConditionDetected = false;

    /**
     * Constructor for the BoundedBufferSolution class.
     * 
     * @param size The size of the buffer.
     */
    public BoundedBufferSolution(int size) {
        buffer = new int[size];
        n = size;
    }

    /**
     * Producer thread method.
     * Adds elements to the buffer in bursts of size k, with a small delay between each addition.
     * Continues until a race condition is detected or the producer count reaches 10.
     * 
     * @param k The number of slots the producer adds in each burst.
     * @param t The time in milliseconds between each slot addition.
     */
    public void producerThread(int k, int t) {
        while (!raceConditionDetected && producerCount < 10) {
            for (int i = 0; i < k; i++) {
                mutex.lock();
                try {
                    int nextIn = buffer[n - 1];
                    sleepForTime(t); // Add a small delay before incrementing
                    if (nextIn == buffer[n - 1]) {
                        buffer[n - 1] = (nextIn + 1) % n;
                        System.out.println("So far producer " + producerCount);
                        producerCount++;
                    }
                } finally {
                    mutex.unlock();
                }
            }
            sleepForRandomTime();
        }
    }

    /**
     * Consumer thread method.
     * Reads elements from the buffer and checks for race conditions.
     * Continues until a race condition is detected or the consumer count reaches 10.
     */
    public void consumerThread() {
        while (!raceConditionDetected && consumerCount < 10) {
            mutex.lock();
            try {
                int nextOut = buffer[0];
                if (nextOut > 1) {
                    reportRaceCondition("Consumer too slow");
                    raceConditionDetected = true;
                } else {
                    // Shift all elements in the buffer to the left
                    for (int j = 0; j < n - 1; j++) {
                        buffer[j] = buffer[j + 1];
                    }
                    buffer[n - 1] = 0;
                    System.out.println("So far consumer " + consumerCount);
                    consumerCount++;
                }
            } finally {
                mutex.unlock();
            }
            sleepForRandomTime();
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
     */
    private void sleepForRandomTime() {
        try {
            Random random = new Random();
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
     * Main method to run the bounded buffer solution.
     * 
     * @param args Command line arguments (not used in this program).
     */
    public static void main(String[] args) {
        int n = 10; // Buffer size
        int k = 1; // Number of slots producer adds in each burst
        int t = 10; // Time in milliseconds between each slot addition

        BoundedBufferSolution buffer = new BoundedBufferSolution(n);

        // Create and start the producer and consumer threads
        Thread producerThread = new Thread(() -> buffer.producerThread(k, t));
        Thread consumerThread = new Thread(buffer::consumerThread);
        producerThread.start();
        consumerThread.start();

        // Wait for both threads to complete
        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Display a message if no race condition was found
        if (!buffer.raceConditionDetected) {
            System.out.println("No race condition found.");
        }
    }
}