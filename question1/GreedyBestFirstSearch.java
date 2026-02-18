import java.util.*;

/**
 * Greedy Best-First Search for the Manuscript Sorting Problem.
 */
public class GreedyBestFirstSearch {

    // Node for priority queue
    static class Node implements Comparable<Node> {
        int[] state;
        int h;
        String key;

        Node(int[] state, int h) {
            this.state = state;
            this.h = h;
            this.key = Arrays.toString(state);
        }

        public int compareTo(Node o) {
            return Integer.compare(this.h, o.h);
        }
    }

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
        final int[][] goalPositions = PuzzleState.goalPosition(goal);

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        // Run Greedy Best-First Search
        final long startTime = System.currentTimeMillis();
        int statesExplored = 0;
        boolean success = false;
        List<int[]> solutionPath = null;

        final PriorityQueue<Node> frontier = new PriorityQueue<>();
        final Map<String, String> parent = new HashMap<>();
        final Map<String, int[]> stateMap = new HashMap<>();
        final Set<String> visited = new HashSet<>();

        final String initKey = Arrays.toString(initial);
        frontier.add(new Node(initial, PuzzleState.h2(initial, goalPositions)));
        parent.put(initKey, null);
        stateMap.put(initKey, initial);

        while (!frontier.isEmpty()) {
            final Node node = frontier.poll();

            if (visited.contains(node.key)) continue;
            visited.add(node.key);
            statesExplored++;

            // Goal test
            if (PuzzleState.isGoal(node.state, goal)) {
                success = true;
                solutionPath = PuzzleState.reconstructPath(parent, stateMap, node.state);
                break;
            }

            // Expand: prioritize by h(n) only
            for (int[] neighbor : PuzzleState.getNeighbors(node.state)) {
                final String nKey = Arrays.toString(neighbor);
                if (!visited.contains(nKey)) {
                    if (!parent.containsKey(nKey)) {
                        parent.put(nKey, node.key);
                        stateMap.put(nKey, neighbor);
                    }
                    frontier.add(new Node(neighbor, PuzzleState.h2(neighbor, goalPositions)));
                }
            }
        }

        long timeMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult("Greedy Best-First Search",
                "h2 - Manhattan Distance",
                success, solutionPath, statesExplored, timeMs);
    }
}
