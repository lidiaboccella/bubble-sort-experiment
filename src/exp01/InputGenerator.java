package exp01;
import java.util.Arrays;
import java.util.Random;

/**
 * Generates the input arrays used in the experiment.
 * All arrangements are permutations of the integers 1..n,
 * so only the order of the elements differs between them.
 */
public final class InputGenerator {

    /**
     * The possible initial arrangements of the array to sort.
     */
    public enum Arrangement {SORTED, REVERSED, RANDOM, NEARLY_SORTED}

    /**
     * Creates an array of size n with the given arrangement.
     *
     * @param arrangement the initial order of the elements
     * @param n           the size of the array
     * @param seed        seed for the random generator (used by RANDOM and NEARLY_SORTED),
     *                    so that the same array can be recreated exactly
     * @return a new array containing a permutation of 1..n
     */
    public static Integer[] generate(Arrangement arrangement, int n, long seed) {
        Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            array[i] = i + 1;
        }
        Random random = new Random(seed);

        switch (arrangement) {
            case SORTED:
                break;
            case REVERSED:
                for (int i = 0; i < n; i++) {
                    array[i] = n - i;
                }
                break;
            case RANDOM:
                for (int i = n - 1; i > 0; i--) {
                    swap(array, i, random.nextInt(i + 1));
                }
                break;
            case NEARLY_SORTED:
                int swaps = n / 100;
                for (int k = 0; k < swaps; k++) {
                    swap(array, random.nextInt(n), random.nextInt(n));
                }
                break;
        }
        return array;
    }

    /**
     * Swaps the elements at positions i and j.
     */
    private static void swap(Integer[] array, int i, int j) {
        Integer temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

}