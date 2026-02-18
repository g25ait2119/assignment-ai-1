import java.util.*;
import java.util.stream.*;

/**
 * AC-3 (Arc Consistency Algorithm 3) for the Security Bot Scheduling CSP.
 */
public class ArcConsistency {

    /**
     * Enforce arc consistency across all slot domains.
     * Binary constraint: No Back-to-Back (Slot[i] != Slot[i+1]).
     *
     * @param availableBots mutable list of domains for each slot
     * @return true if arc-consistent (no empty domains), false if inconsistent
     */
    static boolean enforceArcConsistency(final List<List<String>> availableBots) {
        final Queue<int[]> arcQueue = buildInitialArcs();
        int revisionCount = 0;

        while (!arcQueue.isEmpty()) {
            final int[] arc = arcQueue.poll();
            final int target = arc[0];
            final int constraint = arc[1];

            if (pruneUnsupportedValues(availableBots, target, constraint)) {
                revisionCount++;
                System.out.println("  Revised " + CSPFramework.SLOT_NAMES[target]
                        + " -> " + CSPFramework.SLOT_NAMES[constraint]
                        + " : " + CSPFramework.SLOT_NAMES[target]
                        + " domain = " + availableBots.get(target));

                if (availableBots.get(target).isEmpty()) {
                    System.out.println("  FAILURE: " + CSPFramework.SLOT_NAMES[target]
                            + " has empty domain!");
                    return false;
                }

                // Re-enqueue arcs from other neighbors of target (excluding constraint)
                IntStream.range(0, CSPFramework.NUM_SLOTS)
                        .filter(neighbor -> neighbor != constraint && Math.abs(neighbor - target) == 1)
                        .forEach(neighbor -> arcQueue.add(new int[]{neighbor, target}));
            }
        }

        System.out.println("  Total revisions performed: " + revisionCount);
        return true;
    }

    /**
     * Build the initial queue of all directed arcs between consecutive slots.
     */
    private static Queue<int[]> buildInitialArcs() {
        final Queue<int[]> arcQueue = new LinkedList<>();
        IntStream.range(0, CSPFramework.NUM_SLOTS - 1).forEach(i -> {
            arcQueue.add(new int[]{i, i + 1});
            arcQueue.add(new int[]{i + 1, i});
        });
        return arcQueue;
    }

    /**
     * Remove values from target slot's domain that have no valid pairing
     * in the constraint slot's domain.
     * Constraint: target != constraint (No Back-to-Back).
     *
     * @return true if any value was removed from target's domain
     */
    static boolean pruneUnsupportedValues(final List<List<String>> availableBots,
                                          final int target, final int constraint) {
        final List<String> unsupported = availableBots.get(target).stream()
                .filter(bot -> availableBots.get(constraint).stream().noneMatch(other -> !other.equals(bot)))
                .collect(Collectors.toList());

        availableBots.get(target).removeAll(unsupported);
        return !unsupported.isEmpty();
    }

    /**
     * Solve the CSP using backtracking with MRV heuristic on reduced domains.
     */
    static boolean solveWithBacktracking(final String[] schedule, final List<List<String>> availableBots,
                                         final int[] countHolder) {
        if (Arrays.stream(schedule).noneMatch(Objects::isNull)) {
            return CSPFramework.checkMinimumCoverage(schedule);
        }

        final int slot = selectSlotWithFewestOptions(schedule, availableBots);
        if (slot == -1) return false;

        for (final String bot : new ArrayList<>(availableBots.get(slot))) {
            if (!CSPFramework.isConsistent(schedule, slot, bot)) continue;

            countHolder[0]++;
            schedule[slot] = bot;

            if (CSPFramework.canSatisfyCoverage(schedule, availableBots)) {
                if (solveWithBacktracking(schedule, availableBots, countHolder)) return true;
            }

            schedule[slot] = null;
        }
        return false;
    }

    /**
     * Select the unassigned slot with the fewest remaining bot options (MRV heuristic).
     * Returns -1 if all slots are assigned.
     */
    private static int selectSlotWithFewestOptions(final String[] schedule,
                                                   final List<List<String>> availableBots) {
        return IntStream.range(0, CSPFramework.NUM_SLOTS)
                .filter(i -> schedule[i] == null)
                .boxed()
                .min(Comparator.comparingInt(i -> availableBots.get(i).size()))
                .orElse(-1);
    }

    /**
     * Print domains for all slots.
     */
    private static void printAllDomains(final List<List<String>> availableBots) {
        IntStream.range(0, CSPFramework.NUM_SLOTS)
                .forEach(i -> System.out.println("  " + CSPFramework.SLOT_NAMES[i]
                        + " = " + availableBots.get(i)));
    }

    public static void main(final String[] args) throws Exception {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        CSPFramework.readInput(inputFile);

        System.out.println("#".repeat(55));
        System.out.println("# SECURITY BOT SCHEDULING - CSP");
        System.out.println("# AC-3 Arc Consistency + Backtracking");
        System.out.println("#".repeat(55));
        System.out.println();

        CSPFramework.printConstraintGraph();

        final List<List<String>> availableBots = CSPFramework.initDomains();
        System.out.println("Initial Domains (after unary constraint):");
        printAllDomains(availableBots);
        System.out.println();

        System.out.println("=== Running AC-3 Arc Consistency ===");
        System.out.println("Arcs to process: (Slot1,Slot2), (Slot2,Slot1), "
                + "(Slot2,Slot3), (Slot3,Slot2), (Slot3,Slot4), (Slot4,Slot3)");
        System.out.println();

        final long startTime = System.currentTimeMillis();
        final boolean isConsistent = enforceArcConsistency(availableBots);

        System.out.println();
        System.out.println("Domains after AC-3:");
        printAllDomains(availableBots);
        System.out.println("Arc Consistent: " + (isConsistent ? "YES" : "NO"));
        System.out.println();

        if (isConsistent) {
            System.out.println("=== Running Backtracking on AC-3 Reduced Domains ===\n");
            final String[] schedule = new String[CSPFramework.NUM_SLOTS];
            final int[] countHolder = {0};

            final boolean solved = solveWithBacktracking(schedule, availableBots, countHolder);
            final long elapsedMs = System.currentTimeMillis() - startTime;

            CSPFramework.printResult(
                    "AC-3 + Backtracking",
                    "MRV (Minimum Remaining Values)",
                    "AC-3 Arc Consistency Preprocessing",
                    solved, schedule, countHolder[0], elapsedMs);
        } else {
            final long elapsedMs = System.currentTimeMillis() - startTime;
            System.out.println("CSP is INCONSISTENT - no solution exists.");
            System.out.println("Time: " + elapsedMs + " ms");
        }
    }
}