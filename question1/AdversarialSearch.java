
import java.util.*;

/**
 * Adversarial Search for the Manuscript Sorting Problem.
 * Implements both Minimax and Alpha-Beta Pruning.
 */
public class AdversarialSearch {

    private static int minimaxCalls = 0;
    private static int alphaBetaCalls = 0;

    /**
     * Utility = negative Manhattan distance.
     * MAX wants this high (close to 0 = near goal).
     * MIN wants this low (far from goal).
     */
    static int getNVeManhattanDistance(int[] state) {
        return -PuzzleState.h2(state);
    }

    /**
     * Plain Minimax search.
     * @param state   current game state
     * @param depth   remaining depth to search
     * @param isMax   true if MAX's turn, false if MIN's turn
     * @param visited  visited states
     * @return minimax value
     */
    static int minimax(int[] state, int depth, boolean isMax, Set<String> visited) {
        minimaxCalls++;

        // check if max depth reached or goal reached
        if (depth == 0 || PuzzleState.isGoal(state)) {
            return getNVeManhattanDistance(state);
        }

        final List<int[]> neighbors = PuzzleState.getNeighbors(state);

        if (isMax) {
            // choose move that maximizes utility -- MAX player
            int best = Integer.MIN_VALUE;
            for (int[] next : neighbors) {
                String key = Arrays.toString(next);
                if (!visited.contains(key)) {
                    visited.add(key);
                    int val = minimax(next, depth - 1, false, visited);
                    best = Math.max(best, val);
                    visited.remove(key);
                }
            }
            return best == Integer.MIN_VALUE ? getNVeManhattanDistance(state) : best;
        } else {
            //  choose move that minimizes utility --MIN player
            int worst = Integer.MAX_VALUE;
            for (int[] next : neighbors) {
                String key = Arrays.toString(next);
                if (!visited.contains(key)) {
                    visited.add(key);
                    int val = minimax(next, depth - 1, true, visited);
                    worst = Math.min(worst, val);
                    visited.remove(key);
                }
            }
            return worst == Integer.MAX_VALUE ? getNVeManhattanDistance(state) : worst;
        }
    }

    /**
     * Minimax with Alpha-Beta pruning.
     * @param alpha best value MAX can guarantee (lower bound)
     * @param beta  best value MIN can guarantee (upper bound)
     * Prunes when beta <= alpha (remaining branches cannot affect decision).
     */
    static int alphaBeta(int[] state, int depth, int alpha, int beta,
                         boolean isMax, Set<String> visited) {
        alphaBetaCalls++;

        if (depth == 0 || PuzzleState.isGoal(state)) {
            return getNVeManhattanDistance(state);
        }

       final List<int[]> neighbors = PuzzleState.getNeighbors(state);

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int[] next : neighbors) {
                final String key = Arrays.toString(next);
                if (!visited.contains(key)) {
                    visited.add(key);
                    int val = alphaBeta(next, depth - 1, alpha, beta, false, visited);
                    best = Math.max(best, val);
                    visited.remove(key);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break;  // Beta cutoff - prune
                }
            }
            return best == Integer.MIN_VALUE ? getNVeManhattanDistance(state) : best;
        } else {
            int worst = Integer.MAX_VALUE;
            for (int[] next : neighbors) {
                final String key = Arrays.toString(next);
                if (!visited.contains(key)) {
                    visited.add(key);
                    final int val = alphaBeta(next, depth - 1, alpha, beta, true, visited);
                    worst = Math.min(worst, val);
                    visited.remove(key);
                    beta = Math.min(beta, worst);
                    if (beta <= alpha) break;  // Alpha cutoff - prune
                }
            }
            return worst == Integer.MAX_VALUE ? getNVeManhattanDistance(state) : worst;
        }
    }

    /**
     * Find best move for MAX using Minimax.
     */
    static int[] runMinimax(int[] initial, int depth) {
        minimaxCalls = 0;
        final Set<String> visited = new HashSet<>();
        visited.add(Arrays.toString(initial));
        int bestVal = Integer.MIN_VALUE;
        int[] bestMove = null;
        String bestAction = "";

        for (int[] next : PuzzleState.getNeighbors(initial)) {
            final String key = Arrays.toString(next);
            visited.add(key);
            int val = minimax(next, depth - 1, false, visited);
            visited.remove(key);
            if (val > bestVal) {
                bestVal = val;
                bestMove = next;
                bestAction = PuzzleState.getAction(initial, next);
            }
        }

        System.out.println("Best move: " + bestAction + " (utility=" + bestVal + ")");
        return bestMove;
    }

    /**
     * Find best move for MAX using Alpha-Beta.
     */
    static int[] runAlphaBeta(int[] initial, int depth) {
        alphaBetaCalls = 0;
        final Set<String> visited = new HashSet<>();
        visited.add(Arrays.toString(initial));
        int bestVal = Integer.MIN_VALUE;
        int[] bestMove = null;
        String bestAction = "";

        for (int[] next : PuzzleState.getNeighbors(initial)) {
            String key = Arrays.toString(next);
            visited.add(key);
            int val = alphaBeta(next, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    false, visited);
            visited.remove(key);
            if (val > bestVal) {
                bestVal = val;
                bestMove = next;
                bestAction = PuzzleState.getAction(initial, next);
            }
        }

        System.out.println("  Best move: " + bestAction + " (utility=" + bestVal + ")");
        return bestMove;
    }

    public static void main(String[] args) throws Exception {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        int[][] input = PuzzleState.readInput(inputFile);
        int[] initial = input[0];

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(PuzzleState.GOAL));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));

        final int searchDepth = 6;
        System.out.println("Adversarial Search Depth: " + searchDepth);
        System.out.println("Utility function: u(s) = -ManhattanDistance(s)");
        System.out.println();

        // ---- Minimax ----
        System.out.println("--- Plain MINI - MAX ---");
        final long t1 = System.currentTimeMillis();
        final int[] mmMove = runMinimax(initial, searchDepth);
        final long mmTime = System.currentTimeMillis() - t1;
        final int mmStates = minimaxCalls;
        System.out.println("  States evaluated: " + mmStates);
        System.out.println("  Time: " + mmTime + " ms");
        if (mmMove != null) {
            System.out.println("  Resulting state:");
            System.out.print(PuzzleState.stateToGrid(mmMove));
        }
        System.out.println();

        // ---- Alpha-Beta ----
        System.out.println("--- Alpha-Beta Pruning ---");
        final long t2 = System.currentTimeMillis();
        final int[] alphaBeta = runAlphaBeta(initial, searchDepth);
        final long alphaBetaTime = System.currentTimeMillis() - t2;

        System.out.println("  States evaluated: " + alphaBetaCalls);
        System.out.println("  Time: " + alphaBetaTime + " ms");
        if (alphaBeta != null) {
            System.out.println("  Resulting state:");
            System.out.print(PuzzleState.stateToGrid(alphaBeta));
        }
        System.out.println();

        // ---- Comparison ----
        System.out.println("=".repeat(60));
        System.out.println("Minimax vs Alpha-Beta (depth=" + searchDepth + ")");
        System.out.println("=".repeat(60));
        System.out.println("                    Minimax    Alpha-Beta");
        System.out.println("States evaluated:   " + String.format("%-11d%d", mmStates, alphaBetaCalls));
        System.out.println("Time (ms):          " + String.format("%-11d%d", mmTime, alphaBetaTime));
        System.out.println("Same best move?     "
                + (Arrays.equals(mmMove, alphaBeta) ? "YES (pruning is lossless)" : "NO (unexpected)"));
        if (mmStates > 0) {
            final double savings = (1.0 - (double) alphaBetaCalls / mmStates) * 100;
            System.out.printf("Pruning saved:      %.1f%% of state evaluations%n", savings);
        }
        System.out.println();
    }
}
