import java.util.*;
import java.io.*;

/**
 * PuzzleState - Shared utility class for the Manuscript Sorting Problem.
 * Provides state representation, neighbor generation, heuristics, and I/O.
 */
public class PuzzleState {

    public static final int SIZE = 3;

    // Direction vectors: Up, Down, Left, Right
    public static final int[] DR = {-1, 1, 0, 0};
    public static final int[] DC = {0, 0, -1, 1};
    public static final String[] DIR_NAMES = {"Up", "Down", "Left", "Right"};

    public static int[][] goalPosition(final int[] goal) {
        final int[][] goalPosition = new int[9][2];
        for (int i = 0; i < 9; i++) {
            final int val = goal[i];
            goalPosition[val] = new int[]{i / SIZE, i % SIZE};
        }
        return goalPosition;
    }

    /**
     * Parse state string like "123;B46;758" or "123 456 78B" into int array.
     * 'B' or '0' represents blank.
     */
    public static int[] parseState(String s) {
        s = s.replace(";", "").replace("B", "0").replace(" ", "");
        final int[] state = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            state[i] = s.charAt(i) - '0';
        }
        return state;
    }

    /**
     * Read initial and goal states from input.txt.
     * Returns int[2][9]: [0] = initial, [1] = goal.
     */
    public static int[][] readInput(String filename) throws Exception {
        Scanner sc = new Scanner(new File(filename));
        String startLine = sc.nextLine().trim();
        String goalLine = sc.hasNextLine() ? sc.nextLine().trim() : "123 456 78B";
        sc.close();
        return new int[][]{parseState(startLine), parseState(goalLine)};
    }

    public static List<int[][]> readInputMultipleLines(String filename) {
        final List<int[][]> inputData = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());

        }

        if (lines.size() % 2 != 0) {
            System.out.println("Exception: Input file is incomplete. Please check the goal state or provided input.txt file");
            System.out.println("Total lines found: " + lines.size());
            return inputData;
        }

        final int totalSets = lines.size() / 2;
        for (int i = 0; i < totalSets; i++) {
            final int inputIndex = i * 2;
            final int outputIndex = i * 2 + 1;

            inputData.add(new int[][]{parseState(lines.get(inputIndex)), parseState(lines.get(outputIndex))});
        }

        return inputData;
    }

    /**
     * Display state as "1 2 3 / B 4 6 / 7 5 8"
     */
    public static String stateToString(int[] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(state[i] == 0 ? "B" : state[i]);
            if (i % 3 == 2 && i < 8) sb.append(" / ");
            else if (i < 8) sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Display state as a 3x3 grid
     */
    public static String stateToGrid(int[] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(state[i] == 0 ? "B" : state[i]);
            sb.append(i % 3 == 2 ? "\n" : " ");
        }
        return sb.toString();
    }

    /**
     * Find the index of the blank (0) in the state array.
     */
    public static int findBlank(int[] state) {
        for (int i = 0; i < 9; i++) {
            if (state[i] == 0) return i;
        }
        return -1;
    }

    /**
     * Check if state matches the goal.
     */
    public static boolean isGoal(int[] state, int[] goal) {
        return Arrays.equals(state, goal);
    }

    /**
     * Create a new state by swapping positions i and j.
     */
    public static int[] swap(int[] state, int i, int j) {
        int[] next = state.clone();
        int tmp = next[i];
        next[i] = next[j];
        next[j] = tmp;
        return next;
    }

    /**
     * Generate all valid neighbor states by moving the blank.
     */
    public static List<int[]> getNeighbors(int[] state) {
        List<int[]> neighbors = new ArrayList<>();
        int blank = findBlank(state);
        int r = blank / SIZE, c = blank % SIZE;
        for (int d = 0; d < 4; d++) {
            int nr = r + DR[d], nc = c + DC[d];
            if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {
                neighbors.add(swap(state, blank, nr * SIZE + nc));
            }
        }
        return neighbors;
    }

    /**
     * Get the action name (Up/Down/Left/Right) that transforms 'from' to 'to'.
     */
    public static String getAction(int[] from, int[] to) {
        int blankFrom = findBlank(from);
        int blankTo = findBlank(to);
        int dr = (blankTo / SIZE) - (blankFrom / SIZE);
        int dc = (blankTo % SIZE) - (blankFrom % SIZE);
        for (int d = 0; d < 4; d++) {
            if (DR[d] == dr && DC[d] == dc) return DIR_NAMES[d];
        }
        return "?";
    }

    /**
     * h1: Number of misplaced manuscripts (excluding blank).
     */
    public static int h1(int[] state, int[] goal) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (state[i] != 0 && state[i] != goal[i]) count++;
        }
        return count;
    }

    /**
     * h2: Total Manhattan Distance of all tiles from goal positions.
     */
    public static int h2(int[] state, int[][] goalPosition) {
        int dist = 0;
        for (int i = 0; i < 9; i++) {
            int val = state[i];
            if (val != 0) {
                int currentRow = i / SIZE, currentCol = i % SIZE;
                dist += Math.abs(currentRow - goalPosition[val][0])
                        + Math.abs(currentCol - goalPosition[val][1]);
            }
        }
        return dist;
    }

    /**
     * Reconstruct path from parent map.
     * parent maps state-key -> parent-state-key.
     * stateMap maps state-key -> int[] state.
     */
    public static List<int[]> reconstructPath(Map<String, String> parent,
                                              Map<String, int[]> stateMap,
                                              int[] goal) {
        List<int[]> path = new ArrayList<>();
        String key = Arrays.toString(goal);
        while (key != null) {
            path.add(stateMap.get(key));
            key = parent.get(key);
        }
        Collections.reverse(path);
        return path;
    }

    public static void printResult(final String algorithm, final String heuristic,
                                   final boolean success, final List<int[]> path,
                                   final int statesExplored, final long timeTakenInMS) {
        System.out.println("=".repeat(60));
        System.out.println("Algorithm    : " + algorithm
                + (heuristic.isEmpty() ? "" : " (" + heuristic + ")"));
        System.out.println("=".repeat(60));
        System.out.println("Status       : " + (success ? "SUCCESS" : "FAILURE"));
        System.out.println("States Explored: " + statesExplored);
        System.out.println("Time Taken   : " + timeTakenInMS + " ms");

        if (success && path != null && path.size() > 1) {
            System.out.println("Path Length  : " + (path.size() - 1) + " moves");
            System.out.print("Path         : ");
            for (int i = 1; i < path.size(); i++) {
                System.out.print(getAction(path.get(i - 1), path.get(i)));
                if (i < path.size() - 1) System.out.print(" -> ");
            }

            System.out.println("\nInitial State:");
            System.out.print(stateToGrid(path.get(0)));

            System.out.println("Goal State:");
            System.out.print(stateToGrid(path.get((path.size() - 1))));
        }
    }
}
