
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        try {
            int[] array = new Random().ints(1_000_000, 1, 100).toArray();

            long startTime = System.currentTimeMillis();
            long sequentialSum = getSequencialSum(array);
            long endTime = System.currentTimeMillis();
            long sequentialTime = endTime - startTime;
            System.out.println("Sequential sum: " + sequentialSum + " in " + sequentialTime + " ms");

            startTime = System.currentTimeMillis();
            long concurrentSum10 = concurrentSum(array, 10);
            endTime = System.currentTimeMillis();
            long concurrentTime10 = endTime - startTime;
            System.out.println("Concurrent sum with 10 threads: " + concurrentSum10 + " in " + concurrentTime10 + " ms");


            startTime = System.currentTimeMillis();
            long concurrentSum100 = concurrentSum(array, 100);
            endTime = System.currentTimeMillis();
            long concurrentTime100 = endTime - startTime;
            System.out.println("Concurrent sum with 100 threads: " + concurrentSum100 + " in " + concurrentTime100 + " ms");

            double speedup10 = (double) sequentialTime / concurrentTime10;
            double speedup100 = (double) sequentialTime / concurrentTime100;
            System.out.println("Speedup with 10 threads: " + speedup10);
            System.out.println("Speedup with 100 threads: " + speedup100);

            startTime = System.currentTimeMillis();
            long virtualSum10 = virtualSum(array, 10);
            endTime = System.currentTimeMillis();
            long virtualTime10 = endTime - startTime;
            System.out.println("Virtual sum with 10 threads: " + virtualSum10 + " in " + virtualTime10 + " ms");

            startTime = System.currentTimeMillis();
            long virtualSum100 = virtualSum(array, 100);
            endTime = System.currentTimeMillis();
            long virtualTime100 = endTime - startTime;
            System.out.println("Virtual sum with 100 threads: " + virtualSum100 + " in " + virtualTime100 + " ms");

            double virtualSpeedup10 = (double) sequentialTime / virtualTime10;
            double virtualSpeedup100 = (double) sequentialTime / virtualTime100;
            System.out.println("Virtual speedup with 10 threads: " + virtualSpeedup10);
            System.out.println("Virtual speedup with 100 threads: " + virtualSpeedup100);
        } catch (InterruptedException e) {
            System.out.println("Interrompida.");
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            System.out.println("Erro na execução.");
            throw new RuntimeException(e);
        }
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