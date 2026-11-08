package exp01;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Runs the experiment: measures the execution time of the three
 * bubble sort implementations and writes the raw data to a CSV file.
 */
public final class ExperimentRunner {

    /** Number of discarded runs per algorithm before measuring. */
    private static final int WARMUP_RUNS = 20;
    /** Array sizes tested. */
    private static final int[] SIZES = {1000, 5000, 10000, 20000};
    /** Number of measurements per condition. */
    private static final int REPETITIONS = 30;
    /** Seed for input generation: repetition r uses BASE_SEED + r. */
    private static final long BASE_SEED = 42;
    /** Seed used to randomize the execution order of the measurements. */
    private static final long ORDER_SEED = 2026;

    /** One single measurement to perform. */
    record Task(Sorter<Integer> sorter, InputGenerator.Arrangement arrangement,
                int size, int repetition) {}

    /**
     * Measures the time taken by one call to sorter.sort().
     * The original array is never modified: a copy is sorted instead.
     *
     * @param sorter   the algorithm to measure
     * @param original the input array (left untouched)
     * @return elapsed time in nanoseconds
     */
    static long measureOnce(Sorter<Integer> sorter, Integer[] original) {
        Integer[] copy = original.clone();
        long start = System.nanoTime();
        sorter.sort(copy);
        long end = System.nanoTime();
        if (!isSorted(copy)) {
            throw new IllegalStateException("Array not sorted");
        }
        return end - start;
    }

    /** Returns true if the array is sorted in ascending order. */
    static boolean isSorted(Integer[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i].compareTo(array[i + 1]) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs every algorithm WARMUP_RUNS times on a random array and discards
     * the timings, so that the JIT compiler has optimized the code before
     * the actual measurements start.
     */
    static void warmUp(List<Sorter<Integer>> sorters) {
        Integer[] array = InputGenerator.generate(InputGenerator.Arrangement.RANDOM, 1000, 0);
        for (Sorter<Integer> sorter : sorters) {
            for (int i = 0; i < WARMUP_RUNS; i++) {
                measureOnce(sorter, array);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        List<Sorter<Integer>> sorters = List.of(
                new BubbleSortPassPerItem<>(),
                new BubbleSortUntilNoChange<>(),
                new BubbleSortWhileNeeded<>());

        List<Task> tasks = new ArrayList<>();
        for (Sorter<Integer> sorter : sorters) {
            for (InputGenerator.Arrangement arrangement : InputGenerator.Arrangement.values()) {
                for (int size : SIZES) {
                    for (int r = 0; r < REPETITIONS; r++) {
                        tasks.add(new Task(sorter, arrangement, size, r));
                    }
                }
            }
        }

        Collections.shuffle(tasks, new Random(ORDER_SEED));

        warmUp(sorters);

        Files.createDirectories(Path.of("../data"));
        try (PrintWriter out = new PrintWriter(new FileWriter("../data/results.csv"))) {
            out.println("algorithm,arrangement,size,repetition,time_ns");
            int done = 0;
            for (Task t : tasks) {
                Integer[] input = InputGenerator.generate(
                        t.arrangement(), t.size(), BASE_SEED + t.repetition());
                long time = measureOnce(t.sorter(), input);
                out.println(t.sorter().getClass().getSimpleName() + ","
                        + t.arrangement() + "," + t.size() + ","
                        + t.repetition() + "," + time);
                done++;
                if (done % 100 == 0) {
                    System.out.println(done + " / " + tasks.size());
                }
            }
        }
        System.out.println("Done");
    }
}