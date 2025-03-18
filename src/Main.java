
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int[] array = new Random().ints(1_000_000, 1, 100).toArray();

        long startTime = System.nanoTime();
        long sequentialSum = getSequencialSum(array);
        long endTime = System.nanoTime();
        long sequentialTime = endTime - startTime;
        System.out.println("Sequential sum: " + sequentialSum + " in " + sequentialTime + " ns");

        startTime = System.nanoTime();
        long concurrentSum10 = concurrentSum(array, 10);
        endTime = System.nanoTime();
        long concurrentTime10 = endTime - startTime;
        System.out.println("Concurrent sum with 10 threads: " + concurrentSum10 + " in " + concurrentTime10 + " ns");

        // Concurrent addition with 100 threads
        startTime = System.nanoTime();
        long concurrentSum100 = concurrentSum(array, 100);
        endTime = System.nanoTime();
        long concurrentTime100 = endTime - startTime;
        System.out.println("Concurrent sum with 100 threads: " + concurrentSum100 + " in " + concurrentTime100 + " ns");

        // Speedup calculations
        double speedup10 = (double) sequentialTime / concurrentTime10;
        double speedup100 = (double) sequentialTime / concurrentTime100;
        System.out.println("Speedup with 10 threads: " + speedup10);
        System.out.println("Speedup with 100 threads: " + speedup100);

        // Testes com threads virtuais
        startTime = System.nanoTime();
        long virtualSum10 = virtualSum(array, 10);
        endTime = System.nanoTime();
        long virtualTime10 = endTime - startTime;
        System.out.println("Virtual sum with 10 threads: " + virtualSum10 + " in " + virtualTime10 + " ns");

        startTime = System.nanoTime();
        long virtualSum100 = virtualSum(array, 100);
        endTime = System.nanoTime();
        long virtualTime100 = endTime - startTime;
        System.out.println("Virtual sum with 100 threads: " + virtualSum100 + " in " + virtualTime100 + " ns");

        double virtualSpeedup10 = (double) sequentialTime / virtualTime10;
        double virtualSpeedup100 = (double) sequentialTime / virtualTime100;
        System.out.println("Virtual speedup with 10 threads: " + virtualSpeedup10);
        System.out.println("Virtual speedup with 100 threads: " + virtualSpeedup100);
    }

    private static long getSequencialSum(int[] array) {
        long sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    private static long concurrentSum(int[] array, int numThreads) throws InterruptedException {
        SumThread[] threads = new SumThread[numThreads];
        int chunkSize = array.length / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? array.length : start + chunkSize;
            threads[i] = new SumThread(array, start, end);
            threads[i].start();
        }

        long totalSum = 0;
        for (SumThread thread : threads) {
            thread.join();
            totalSum += thread.getSum();
        }

        return totalSum;
    }

    private static long virtualSum(int[] array, int numThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        int chunkSize = array.length / numThreads;
        Future<Long>[] futures = new Future[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = (i == numThreads - 1) ? array.length : start + chunkSize;
            futures[i] = executor.submit(() -> {
                long sum = 0;
                for (int j = start; j < end; j++) {
                    sum += array[j];
                }
                return sum;
            });
        }

        long totalSum = 0;
        for (Future<Long> future : futures) {
            totalSum += future.get();
        }

        executor.shutdown();
        return totalSum;
    }
}