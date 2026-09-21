import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * SkyShow Collision Alert - Assignment 1.
 * This is the only file you change.
 */
public class Main {

    static final String BARCODE = String.valueOf(250245);   // <-- put your student barcode here

    public static void main(String[] args) {
        // Runs your methods, checks them and prints the results.
        Checker.run(BARCODE, Main::findClosestPairBruteForce, Main::findClosestPairDivideAndConquer, Main::findFirstUnsafeN);
    }

    // ======================== Part A: Brute Force ========================

    /**
     * Returns the two closest drones as new Drone[] {a, b}.
     */
    static Drone[] findClosestPairBruteForce(Drone[] drones) {
        // TODO: look at every pair exactly once and keep the closest one.
        //       Compare two drones with Drone.distSq(a, b) and store the best value in a variable.
        Drone A = null;
        Drone B = null;

        long bestDistance = Long.MAX_VALUE;

        for (int i = 0; i < drones.length; i++) {

            for (int j = i + 1; j < drones.length; j++) {

                long distance = Drone.distSq(drones[i], drones[j]);

                if (distance < bestDistance) {
                    bestDistance = distance;

                    A = drones[i];
                    B = drones[j];
                }
            }
        }
        return new Drone[]{A, B};

    }

    // ====================== Part B: Divide & Conquer ======================

    /**
     * Returns the two closest drones as new Drone[] {a, b}.
     */
    static Drone[] findClosestPairDivideAndConquer(Drone[] drones) {
        // TODO: copy the array (drones.clone()), sort the copy by x, then call closest(...).
        //       Sorting by x:  Arrays.sort(copy, Comparator.comparingInt(d -> d.x));

        Drone[] Sorted = drones.clone();
        Arrays.sort(Sorted, Comparator.comparingInt(d -> d.x));
        return closest(Sorted, 0, Sorted.length);
    }

    /**
     * Closest pair among sorted[from] .. sorted[to - 1], where sorted is already sorted by x.
     * Everything below is squared: delta is a squared distance, so compare dx*dx and dy*dy with it.
     */
    private static Drone[] closest(Drone[] sorted, int from, int to) {
        int n = to - from;

        // BASE CASE: 3 drones or fewer - just check all pairs
        if (n <= 3) {
            Drone A = null;
            Drone B = null;

            long bestDistance = Long.MAX_VALUE;

            for (int i = from; i < to; i++) {
                for (int j = i + 1; j < to; j++) {
                    long distance = Drone.distSq(sorted[i], sorted[j]);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        A = sorted[i];
                        B = sorted[j];
                    }
                }
            }
            return new Drone[]{A, B};
        }
        // DIVIDE: split in the middle BY INDEX; midX is the x of the middle drone
        int mid = from + n / 2;
        int midX = sorted[mid].x;

        // RECURSIVE CASE: solve the left half and the right half, keep the better pair.
        //                 delta = the smaller of the two squared distances.
        Drone[] Left = closest(sorted, from, mid);
        Drone[] Right = closest(sorted, mid, to);

        long leftDistance = Drone.distSq(Left[0], Left[1]);
        long rightDistance = Drone.distSq(Right[0], Right[1]);

        Drone[] bestPair;
        long delta;

        if (leftDistance < rightDistance) {
            bestPair = Left;
            delta = leftDistance;
        } else {
            bestPair = Right;
            delta = rightDistance;
        }

        // COMBINE: the closest pair may have one drone on each side.
        //          1) strip = the drones with (x - midX) * (x - midX) < delta
        //          2) sort the strip by y
        //          3) for each drone in the strip, compare it with the next ones and
        //             stop as soon as (y difference) * (y difference) >= delta
        ArrayList<Drone> strip = new ArrayList<>();

        for (int i = from; i < to; i++) {
            long dx = (long) sorted[i].x - midX;

            if (dx * dx < delta) {
                strip.add(sorted[i]);
            }
        }

        strip.sort(Comparator.comparingInt(d -> d.y));

        for (int i = 0; i < strip.size(); i++) {

            for (int j = i + 1; j < strip.size(); j++) {

                long dy = (long) strip.get(j).y - strip.get(i).y;

                if (dy * dy >= delta) {
                    break;
                }

                long distance =
                        Drone.distSq(strip.get(i), strip.get(j));

                if (distance < delta) {
                    delta = distance;

                    bestPair = new Drone[]{
                            strip.get(i), strip.get(j)};
                }
            }
        }

        return bestPair;
    }


    // ======================== Part C: Safety limit ========================

    /**
     * Drones join the show one by one in order of id: #1, #2, #3, ...
     * Returns the smallest N such that the first N drones already have a pair
     * closer than Drone.SAFE_DISTANCE_CM.
     */
    static int findFirstUnsafeN(Drone[] drones) {

        int low = 2;
        int high = drones.length;

        long safeDistance =
                (long) Drone.SAFE_DISTANCE_CM
                        * Drone.SAFE_DISTANCE_CM;

        while (low < high) {

            int mid = low + (high - low) / 2;

            Drone[] firstDrones =
                    Arrays.copyOfRange(drones, 0, mid);

            Drone[] pair =
                    findClosestPairDivideAndConquer(firstDrones);

            long distance =
                    Drone.distSq(pair[0], pair[1]);

            if (distance < safeDistance) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }
    // TODO: binary search on N, exactly like binary search in an array.
    //       low = 2, high = drones.length.
    //       For a middle N: take Arrays.copyOfRange(drones, 0, mid), run your Part B on it,
    //       and ask whether that pair is closer than SAFE_DISTANCE_CM (compare SQUARED values).
    //       Unsafe -> the answer is mid or smaller. Safe -> the answer is bigger.
}
