import java.util.*;
import java.io.*;

/**
 * PuzzleState - Shared utility class for the Manuscript Sorting Problem.
 * Provides state representation, neighbor generation, heuristics, and I/O.
 *
 * Student : Suresh Babu Gandla
 * Roll No : 123245
 * IIT Jodhpur - M.Tech Programme
 */
public class PuzzleState {

    public static final int SIZE = 3;
    public static final int[] GOAL = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    // Direction vectors: Up, Down, Left, Right
    public static final int[] DR = {-1, 1, 0, 0};
    public static final int[] DC = {0, 0, -1, 1};
    public static final String[] DIR_NAMES = {"Up", "Down", "Left", "Right"};

    // Goal positions for Manhattan distance: GOAL_POS[tile] = {row, col}
    public static final int[][] GOAL_POS = new int[9][2];

    static {
        for (int i = 0; i < 9; i++) {
            int val = GOAL[i];
            GOAL_POS[val] = new int[]{i / SIZE, i % SIZE};
        }
    }

    // -------------------- State Parsing --------------------

    /**
     * Parse state string like "123;B46;758" or "123 456 78B" into int array.
     * 'B' or '0' represents blank.
     */
    public static int[] parseState(String s) {
        s = s.replace(";", "").replace("B", "0").replace(" ", "");
        int[] state = new int[9];
        for (int i = 0; i < 9; i++) {
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

    // -------------------- State Display --------------------

    /** Display state as "1 2 3 / B 4 6 / 7 5 8" */
    public static String stateToString(int[] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(state[i] == 0 ? "B" : state[i]);
            if (i % 3 == 2 && i < 8) sb.append(" / ");
            else if (i < 8) sb.append(" ");
        }
        return sb.toString();
    }

    /** Display state as a 3x3 grid */
    public static String stateToGrid(int[] state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(state[i] == 0 ? "B" : state[i]);
            sb.append(i % 3 == 2 ? "\n" : " ");
        }
        return sb.toString();
    }

    // -------------------- Core Operations --------------------

    /** Find the index of the blank (0) in the state array. */
    public static int findBlank(int[] state) {
        for (int i = 0; i < 9; i++) {
            if (state[i] == 0) return i;
        }
        return -1;
    }

    /** Check if state matches the goal. */
    public static boolean isGoal(int[] state) {
        return Arrays.equals(state, GOAL);
    }

    /** Create a new state by swapping positions i and j. */
    public static int[] swap(int[] state, int i, int j) {
        int[] next = state.clone();
        int tmp = next[i];
        next[i] = next[j];
        next[j] = tmp;
        return next;
    }

    /** Generate all valid neighbor states by moving the blank. */
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

    /** Get the action name (Up/Down/Left/Right) that transforms 'from' to 'to'. */
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

    // -------------------- Heuristic Functions --------------------

    /** h1: Number of misplaced manuscripts (excluding blank). */
    public static int h1(int[] state) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (state[i] != 0 && state[i] != GOAL[i]) count++;
        }
        return count;
    }

    /** h2: Total Manhattan Distance of all tiles from goal positions. */
    public static int h2(int[] state) {
        int dist = 0;
        for (int i = 0; i < 9; i++) {
            int val = state[i];
            if (val != 0) {
                int currentRow = i / SIZE, currentCol = i % SIZE;
                dist += Math.abs(currentRow - GOAL_POS[val][0])
                      + Math.abs(currentCol - GOAL_POS[val][1]);
            }
        }
        return dist;
    }

    // -------------------- Path Reconstruction --------------------

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



    /** Print search results in the required output format. */
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

            /* System.out.println("\nStep-by-step:");
            for (int i = 1; i < path.size(); i++) {
                System.out.println("  | Move " + i + ": "
                        + getAction(path.get(i - 1), path.get(i)));
                System.out.println("  v");
                System.out.print(stateToGrid(path.get(i)));
            }*/
            System.out.println("Goal State:");
            System.out.print(stateToGrid(path.get((path.size() - 1))));

        }

    }
}
