import java.util.*;

/**
 * Breadth-First Search (BFS) for the Manuscript Sorting Problem.
 */
public class BFSSearch {

    public static void main(String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";

        final List<int[][]> inputData = PuzzleState.readInputMultipleLines(inputFile);
        if (inputData.isEmpty()) {
            return;
        }

        for (final int[][] input : inputData) {
            processSearch(input);
            System.out.println("#".repeat(60));
        }
    }

    private static void processSearch(int[][] input) {
        final int[] initial = input[0];
        final int[] goal = input[1];

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        final long startTime = System.currentTimeMillis();
        int statesExplored = 0;
        boolean success = false;
        List<int[]> solutionPath = null;

        // Frontier
        final Queue<int[]> frontier = new LinkedList<>();
        // Parent map for path reconstruction
        final Map<String, String> parent = new HashMap<>();
        // State map: key  - state array
        final Map<String, int[]> stateMap = new HashMap<>();
        // Visited set
        final Set<String> visited = new HashSet<>();

        final String initKey = Arrays.toString(initial);
        frontier.add(initial);
        visited.add(initKey);
        parent.put(initKey, null);
        stateMap.put(initKey, initial);

        while (!frontier.isEmpty()) {
            final int[] current = frontier.poll();
            String currentKey = Arrays.toString(current);
            statesExplored++;

            // Goal test
            if (Arrays.equals(current, goal)) {
                success = true;
                solutionPath = PuzzleState.reconstructPath(parent, stateMap, current);
                break;
            }
            // Expand neighbors
            for (final int[] neighbor : PuzzleState.getNeighbors(current)) {
                String neighborKey = Arrays.toString(neighbor);
                if (!visited.contains(neighborKey)) {
                    visited.add(neighborKey);
                    parent.put(neighborKey, currentKey);
                    stateMap.put(neighborKey, neighbor);
                    frontier.add(neighbor);
                }
            }
        }

        long timeMs = System.currentTimeMillis() - startTime;

        // Print results
        PuzzleState.printResult("Breadth-First Search (BFS)", "",
                success, solutionPath, statesExplored, timeMs);
    }
}
