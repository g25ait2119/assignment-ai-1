import java.util.*;

/**
 * A* Search for the Manuscript Sorting Problem.
 */
public class AStarSearch {

    // Node for priority queue
    final static class Node implements Comparable<Node> {
        int[] state;
        int pathCost;
        int huresticValue;
        int totalEstimatedCost;
        String key;

        Node(int[] state, int pathCost, int huresticValue) {
            this.state = state;
            this.pathCost = pathCost;
            this.huresticValue = huresticValue;
            this.totalEstimatedCost = pathCost + huresticValue;
            this.key = Arrays.toString(state);
        }

        public int compareTo(Node o) {
            if (this.totalEstimatedCost != o.totalEstimatedCost) {
                return Integer.compare(this.totalEstimatedCost, o.totalEstimatedCost);
            }
            return Integer.compare(this.huresticValue, o.huresticValue); // tie-break: prefer lower h
        }
    }

    /**
     * Run A* with specified heuristic.
     *
     * @param useH1 true = h1 (misplaced tiles), false = h2 (Manhattan distance)
     */
    static void runAStar(int[] initial, int[] goal, int[][] goalPositions, boolean useH1) {
        final String hName = useH1 ? "h1 - Misplaced Tiles" : "h2 - Manhattan Distance";
        final long startTime = System.currentTimeMillis();
        int statesExplored = 0;
        boolean success = false;
        List<int[]> solutionPath = null;

        final PriorityQueue<Node> frontier = new PriorityQueue<>();
        Map<String, Integer> bestPathCost = new HashMap<>(); // best g-value found for each state
        Map<String, String> parent = new HashMap<>();
        Map<String, int[]> stateMap = new HashMap<>();

        String initKey = Arrays.toString(initial);
        final int huresticVal = useH1 ? PuzzleState.h1(initial, goal) : PuzzleState.h2(initial, goalPositions);
        frontier.add(new Node(initial, 0, huresticVal));
        bestPathCost.put(initKey, 0);
        parent.put(initKey, null);
        stateMap.put(initKey, initial);

        while (!frontier.isEmpty()) {
            Node node = frontier.poll();
            statesExplored++;

            // Goal test
            if (PuzzleState.isGoal(node.state, goal)) {
                success = true;
                solutionPath = PuzzleState.reconstructPath(parent, stateMap, node.state);
                break;
            }

            // Skip if we already found a better path to this state
            if (node.pathCost > bestPathCost.getOrDefault(node.key, Integer.MAX_VALUE)) {
                continue;
            }

            // Expand neighbors
            for (int[] neighbor : PuzzleState.getNeighbors(node.state)) {
                String nKey = Arrays.toString(neighbor);
                int newG = node.pathCost + 1; // each move costs 1 unit of System Energy

                if (newG < bestPathCost.getOrDefault(nKey, Integer.MAX_VALUE)) {
                    bestPathCost.put(nKey, newG);
                    parent.put(nKey, node.key);
                    stateMap.put(nKey, neighbor);
                    int nh = useH1 ? PuzzleState.h1(neighbor, goal) : PuzzleState.h2(neighbor, goalPositions);
                    frontier.add(new Node(neighbor, newG, nh));
                }
            }
        }

        final long timeMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("A* Search", hName,
                success, solutionPath, statesExplored, timeMs);
    }

    public static void main(String[] args) {

        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";

        final List<int[][]> inputData = PuzzleState.readInputMultipleLines(inputFile);
        if (inputData.isEmpty()) {
            return;
        }

        for (final int[][] input : inputData) {
            final int[] initial = input[0];
            final int[] goal = input[1];
            final int[][] goalPositions = PuzzleState.goalPosition(goal);

            System.out.println("Start State: " + PuzzleState.stateToString(initial));
            System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
            System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

            // Run A* with h1 (Misplaced Tiles)
            runAStar(initial, goal, goalPositions, true);

            // Run A* with h2 (Manhattan Distance)
            runAStar(initial, goal, goalPositions, false);
            System.out.println("#".repeat(60));
        }

    }
}
