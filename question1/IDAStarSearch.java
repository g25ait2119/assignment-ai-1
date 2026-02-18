import java.util.*;

/**
 * Iterative Deepening A* (IDA*) for the Manuscript Sorting Problem.
 */
public class IDAStarSearch {

    private static int nodesExplored;

    private static final int SOLUTION_FOUND = -1;

    /**
     * Recursive depth-limited search with f-value threshold.
     * Returns SOLUTION_FOUND (-1) if goal reached, otherwise the minimum f exceeding the threshold.
     */
    static int depthLimitedSearch(final List<int[]> currentPath, final int[] goal,
                                  final int[][] goalPositions, final Set<String> onCurrentPath,
                                  final int pathCost, final int threshold,
                                  final boolean useMisplacedTiles) {
        final int[] current = currentPath.get(currentPath.size() - 1);
        final int heuristic = useMisplacedTiles
                ? PuzzleState.h1(current, goal)
                : PuzzleState.h2(current, goalPositions);
        final int estimatedTotal = pathCost + heuristic;

        if (estimatedTotal > threshold) {
            return estimatedTotal;
        }

        nodesExplored++;

        if (PuzzleState.isGoal(current, goal)) {
            return SOLUTION_FOUND;
        }

        int nextThreshold = Integer.MAX_VALUE;
        for (final int[] successor : PuzzleState.getNeighbors(current)) {
            final String successorKey = Arrays.toString(successor);
            if (onCurrentPath.contains(successorKey)) continue;

            currentPath.add(successor);
            onCurrentPath.add(successorKey);

            final int searchResult = depthLimitedSearch(currentPath, goal, goalPositions,
                    onCurrentPath, pathCost + 1, threshold, useMisplacedTiles);

            if (searchResult == SOLUTION_FOUND) return SOLUTION_FOUND;
            nextThreshold = Math.min(nextThreshold, searchResult);

            currentPath.remove(currentPath.size() - 1);
            onCurrentPath.remove(successorKey);
        }

        return nextThreshold;
    }

    /**
     * Solve the puzzle using IDA* with the specified heuristic.
     *
     * @param useMisplacedTiles true = h1 (misplaced tiles), false = h2 (Manhattan distance)
     */
    static void solve(final int[] initial, final int[] goal, final int[][] goalPositions,
                      final boolean useMisplacedTiles) {
        final String heuristicName = useMisplacedTiles ? "h1 - Misplaced Tiles" : "h2 - Manhattan Distance";
        final long startTime = System.currentTimeMillis();
        nodesExplored = 0;
        boolean solved = false;
        List<int[]> solutionPath = null;

        int threshold = useMisplacedTiles
                ? PuzzleState.h1(initial, goal)
                : PuzzleState.h2(initial, goalPositions);

        final List<int[]> currentPath = new ArrayList<>();
        currentPath.add(initial);

        final Set<String> onCurrentPath = new HashSet<>();
        onCurrentPath.add(Arrays.toString(initial));

        int iterationCount = 0;
        while (true) {
            iterationCount++;
            final int searchResult = depthLimitedSearch(currentPath, goal, goalPositions,
                    onCurrentPath, 0, threshold, useMisplacedTiles);

            if (searchResult == SOLUTION_FOUND) {
                solved = true;
                solutionPath = new ArrayList<>(currentPath);
                break;
            }

            if (searchResult == Integer.MAX_VALUE) {
                break;
            }

            System.out.println("  IDA* iteration " + iterationCount
                    + ": threshold:" + threshold + " , next=" + searchResult
                    + " (states explored so far: " + nodesExplored + ")");
            threshold = searchResult;
        }

        final long elapsedMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("Iterative Deepening A* (IDA*)", heuristicName,
                solved, solutionPath, nodesExplored, elapsedMs);
        System.out.println("Total IDA* iterations: " + iterationCount + "\n");
    }

    /**
     * Solve each puzzle with both heuristics and print results.
     */
    private static void solveWithBothHeuristics(final int[][] puzzle) {
        final int[] initial = puzzle[0];
        final int[] goal = puzzle[1];
        final int[][] goalPositions = PuzzleState.goalPosition(goal);

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        solve(initial, goal, goalPositions, true);
        solve(initial, goal, goalPositions, false);
        System.out.println("#".repeat(60));
    }

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        if (puzzles.isEmpty()) return;

        puzzles.forEach(IDAStarSearch::solveWithBothHeuristics);
    }
}