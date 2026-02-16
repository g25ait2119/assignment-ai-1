import java.util.*;

/**
 * Iterative Deepening A* (IDA*) for the Manuscript Sorting Problem.
 */
public class IDAStarSearch {

    private static int statesExplored;

    /**
     * Recursive DFS with f-value threshold.
     * Returns: -1 if FOUND, otherwise the minimum f exceeding threshold.
     */
    static int idaSearch(List<int[]> path, Set<String> pathSet,
                         int g, int threshold, boolean useH1) {
        int[] current = path.get(path.size() - 1);
        int h = useH1 ? PuzzleState.h1(current) : PuzzleState.h2(current);
        int f = g + h;

        if (f > threshold) {
            return f; // Exceeded threshold
        }
        statesExplored++;
        if (PuzzleState.isGoal(current)) {
            return -1;  // FOUND
        }

       int min = Integer.MAX_VALUE;
        for (int[] neighbor : PuzzleState.getNeighbors(current)) {
            String nKey = Arrays.toString(neighbor);
            if (!pathSet.contains(nKey)) {  // Cycle detection on current path
                path.add(neighbor);
                pathSet.add(nKey);

                int result = idaSearch(path, pathSet, g + 1, threshold, useH1);

                if (result == -1) return -1;  // Found solution
                if (result < min) min = result;

                path.remove(path.size() - 1);
                pathSet.remove(nKey);
            }
        }
        return min;
    }

    /**
     * Run IDA* with specified heuristic.
     * @param useH1 true = h1 (misplaced tiles), false = h2 (Manhattan distance)
     */
    static void runIDAStar(int[] initial, boolean useH1) {
        String hName = useH1 ? "h1 - Misplaced Tiles" : "h2 - Manhattan Distance";
        long startTime = System.currentTimeMillis();
        statesExplored = 0;
        boolean success = false;
        List<int[]> solutionPath = null;

        int threshold = useH1 ? PuzzleState.h1(initial) : PuzzleState.h2(initial);
        final List<int[]> path = new ArrayList<>();
        path.add(initial);
        Set<String> pathSet = new HashSet<>();
        pathSet.add(Arrays.toString(initial));

        int iteration = 0;
        while (true) {
            iteration++;
            int result = idaSearch(path, pathSet, 0, threshold, useH1);

            if (result == -1) {
                // Solution found - path contains the solution
                success = true;
                solutionPath = new ArrayList<>(path);
                break;
            }
            if (result == Integer.MAX_VALUE) {
                // No solution exists from the loop
                break;
            }

            System.out.println("  IDA* iteration " + iteration
                    + ": threshold:" + threshold + " , next=" + result
                    + " (states explored so far: " + statesExplored + ")");
            threshold = result;  // Increase threshold to next smallest f
        }

        long timeMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("Iterative Deepening A* (IDA*)", hName,
                success, solutionPath, statesExplored, timeMs);
        System.out.println("Total IDA* iterations: " + iteration +"\n");
    }

    public static void main(String[] args) throws Exception {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        final int[][] input = PuzzleState.readInput(inputFile);
        final int[] initial = input[0];

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(PuzzleState.GOAL));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        // Run IDA* with h1 (Misplaced Tiles)
        runIDAStar(initial, true);

        // Run IDA* with h2 (Manhattan Distance)
        runIDAStar(initial, false);
    }
}
