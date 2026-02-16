import java.util.*;

/**
 * Depth-First Search (DFS) for the Manuscript Sorting Problem.
 */
public class DFSSearch {

    private static final int DEPTH_LIMIT = 50;

    public static void main(String[] args) throws Exception {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        final int[][] input = PuzzleState.readInput(inputFile);
        final int[] initial = input[0];
        final int[] goal = input[1];

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        // Run DFS
        final long startTime = System.currentTimeMillis();
        int statesExplored = 0;
        boolean success = false;
        List<int[]> solutionPath = null;

        // Frontier
        Deque<int[]> frontier = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();
        // Parent map for path reconstruction
        final Map<String, String> parent = new HashMap<>();
        final Map<String, int[]> stateMap = new HashMap<>();
        // Visited set
        final Set<String> visited = new HashSet<>();

        final String initKey = Arrays.toString(initial);
        frontier.push(initial);
        depths.push(0);
        parent.put(initKey, null);
        stateMap.put(initKey, initial);

        while (!frontier.isEmpty()) {
            final int[] current = frontier.pop();
            final int depth = depths.pop();
            final String curKey = Arrays.toString(current);

            // Skip if already visited
            if (visited.contains(curKey)) continue;
            visited.add(curKey);
            statesExplored++;

            // Goal test
            if (Arrays.equals(current, goal)) {
                success = true;
                solutionPath = PuzzleState.reconstructPath(parent, stateMap, current);
                break;
            }

            // Depth bound: prevent infinite paths
            if (depth >= DEPTH_LIMIT) continue;

            // Expand neighbors
            for (final int[] neighbor : PuzzleState.getNeighbors(current)) {
                String neighborKey = Arrays.toString(neighbor);
                if (!visited.contains(neighborKey)) {
                    parent.put(neighborKey, curKey);
                    stateMap.put(neighborKey, neighbor);
                    frontier.push(neighbor);
                    depths.push(depth + 1);
                }
            }
        }

       final long timeTakenInMS = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("Depth-First Search (DFS)",
                "Depth Limit = " + DEPTH_LIMIT,
                success, solutionPath, statesExplored, timeTakenInMS);
    }
}
