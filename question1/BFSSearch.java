import java.util.*;

/**
 * Breadth-First Search (BFS) for the Manuscript Sorting Problem.
 */
public class BFSSearch {

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
     * Solve a single puzzle using BFS and print the results.
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

        final Queue<int[]> frontier = new LinkedList<>();
        final Map<String, String> parentOf = new HashMap<>();
        final Map<String, int[]> stateByKey = new HashMap<>();
        final Set<String> seen = new HashSet<>();

        final String initialKey = Arrays.toString(initial);
        frontier.add(initial);
        seen.add(initialKey);
        parentOf.put(initialKey, null);
        stateByKey.put(initialKey, initial);

        while (!frontier.isEmpty()) {
            final int[] current = frontier.poll();
            final String currentKey = Arrays.toString(current);
            nodesExplored++;

            if (Arrays.equals(current, goal)) {
                solved = true;
                solutionPath = PuzzleState.reconstructPath(parentOf, stateByKey, current);
                break;
            }

            for (final int[] successor : PuzzleState.getNeighbors(current)) {
                final String successorKey = Arrays.toString(successor);
                if (!seen.contains(successorKey)) {
                    seen.add(successorKey);
                    parentOf.put(successorKey, currentKey);
                    stateByKey.put(successorKey, successor);
                    frontier.add(successor);
                }
            }
        }

        final long elapsedMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("Breadth-First Search (BFS)", "",
                solved, solutionPath, nodesExplored, elapsedMs);
    }
}