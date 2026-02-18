import java.util.*;

/**
 * Depth-First Search (DFS) for the Manuscript Sorting Problem.
 */
public class DFSSearch {

    private static final int MAX_DEPTH = 50;

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        if (puzzles.isEmpty()) return;

        puzzles.forEach(puzzle -> {
            solveAndPrint(puzzle);
            System.out.println("#".repeat(60));
        });
    }

    /**
     * Solve a single puzzle using depth-limited DFS and print the results.
     */
    private static void solveAndPrint(final int[][] puzzle) {
        final int[] initial = puzzle[0];
        final int[] goal = puzzle[1];

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        final long startTime = System.currentTimeMillis();
        int nodesExplored = 0;
        boolean solved = false;
        List<int[]> solutionPath = null;

        final Deque<int[]> frontier = new ArrayDeque<>();
        final Deque<Integer> depthTracker = new ArrayDeque<>();
        final Map<String, String> parentOf = new HashMap<>();
        final Map<String, int[]> stateByKey = new HashMap<>();
        final Set<String> seen = new HashSet<>();

        final String initialKey = Arrays.toString(initial);
        frontier.push(initial);
        depthTracker.push(0);
        parentOf.put(initialKey, null);
        stateByKey.put(initialKey, initial);

        while (!frontier.isEmpty()) {
            final int[] current = frontier.pop();
            final int currentDepth = depthTracker.pop();
            final String currentKey = Arrays.toString(current);

            if (seen.contains(currentKey)) continue;
            seen.add(currentKey);
            nodesExplored++;

            if (Arrays.equals(current, goal)) {
                solved = true;
                solutionPath = PuzzleState.reconstructPath(parentOf, stateByKey, current);
                break;
            }

            if (currentDepth >= MAX_DEPTH) continue;

            for (final int[] successor : PuzzleState.getNeighbors(current)) {
                final String successorKey = Arrays.toString(successor);
                if (!seen.contains(successorKey)) {
                    parentOf.put(successorKey, currentKey);
                    stateByKey.put(successorKey, successor);
                    frontier.push(successor);
                    depthTracker.push(currentDepth + 1);
                }
            }
        }

        final long elapsedMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("Depth-First Search (DFS)",
                "Depth Limit = " + MAX_DEPTH,
                solved, solutionPath, nodesExplored, elapsedMs);
    }
}