import java.util.*;
import java.util.stream.*;

/**
 * Backtracking Search with MRV Heuristic and Forward Checking
 * for the Security Bot Scheduling CSP.
 */
public class BacktrackingSearch {

    private static int assignmentCount = 0;
    private static int stepNumber = 0;

    /**
     * Select the unassigned slot with the Minimum Remaining Values.
     * Returns -1 if all slots are assigned.
     */
    static int selectMRV(String[] schedule, List<List<String>> availableBots) {
        return IntStream.range(0, CSPFramework.NUM_SLOTS)
                .filter(slot -> schedule[slot] == null)
                .boxed()
                .min(Comparator.comparingInt(slot -> availableBots.get(slot).size()))
                .orElse(-1);
    }

    /**
     * Perform Forward Checking after assigning bot to slot.
     * Removes inconsistent values from neighboring unassigned variables.
     * Returns the list of pruned (slot, botIndex) pairs for undo, or empty Optional on wipeout.
     */
    static Optional<List<int[]>> forwardCheck(String[] schedule, List<List<String>> availableBots,
                                              int slot, String bot) {
        final List<int[]> removals = new ArrayList<>();
        final int botIndex = Arrays.asList(CSPFramework.BOTS).indexOf(bot);
        final int[] adjacentSlots = {slot - 1, slot + 1};

        for (int neighbor : adjacentSlots) {
            if (neighbor < 0 || neighbor >= CSPFramework.NUM_SLOTS) continue;
            if (schedule[neighbor] != null) continue;

            List<String> neighborBots = availableBots.get(neighbor);
            if (neighborBots.contains(bot)) {
                neighborBots.remove(bot);
                removals.add(new int[]{neighbor, botIndex});
                if (neighborBots.isEmpty()) {
                    return Optional.empty();
                }
            }
        }

        return Optional.of(removals);
    }

    /**
     * Undo Forward Checking by restoring pruned values.
     */
    static void restorePrunedDomains(List<int[]> removals, List<List<String>> availableBots) {
        removals.forEach(entry -> availableBots.get(entry[0]).add(CSPFramework.BOTS[entry[1]]));
    }

    /**
     * Deep copy all domain lists.
     */
    private static List<List<String>> snapshotDomains(List<List<String>> availableBots) {
        return availableBots.stream()
                .map(ArrayList::new)
                .collect(Collectors.toList());
    }

    /**
     * Check if every slot has been assigned.
     */
    private static boolean isFullyAssigned(String[] schedule) {
        return Arrays.stream(schedule).noneMatch(Objects::isNull);
    }

    /**
     * Format current schedule as a readable string.
     */
    private static String formatSchedule(String[] schedule) {
        return IntStream.range(0, CSPFramework.NUM_SLOTS)
                .mapToObj(i -> CSPFramework.SLOT_NAMES[i] + "="
                        + Optional.ofNullable(schedule[i]).orElse("_"))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Print remaining domains for all unassigned slots.
     */
    private static void printRemainingDomains(String[] schedule, List<List<String>> availableBots) {
        IntStream.range(0, CSPFramework.NUM_SLOTS)
                .filter(slot -> schedule[slot] == null)
                .forEach(slot -> System.out.println("    " + CSPFramework.SLOT_NAMES[slot]
                        + " = " + availableBots.get(slot)));
    }

    /**
     * Recursive backtracking with MRV + Forward Checking.
     *
     * @param traceEnabled if true, prints the first 3 steps in detail
     */
    static boolean backtrack(String[] schedule, List<List<String>> availableBots,
                             boolean traceEnabled) {
        if (isFullyAssigned(schedule)) {
            return CSPFramework.checkMinimumCoverage(schedule);
        }

        int slot = selectMRV(schedule, availableBots);
        if (slot == -1) return false;

        if (traceEnabled && stepNumber < 3) {
            stepNumber++;
            System.out.println("--- Step " + stepNumber + " ---");
            System.out.println("  MRV selects: " + CSPFramework.SLOT_NAMES[slot]
                    + " (domain size = " + availableBots.get(slot).size()
                    + ", values = " + availableBots.get(slot) + ")");
            System.out.println("  Current assignment: " + formatSchedule(schedule));
        }

        List<String> candidates = new ArrayList<>(availableBots.get(slot));
        for (String bot : candidates) {
            if (!CSPFramework.isConsistent(schedule, slot, bot)) continue;

            assignmentCount++;
            schedule[slot] = bot;

            if (traceEnabled && stepNumber <= 3) {
                System.out.println("  Try: " + CSPFramework.SLOT_NAMES[slot] + " = " + bot);
            }

            final List<List<String>> savedDomains = snapshotDomains(availableBots);

            Optional<List<int[]>> pruneResult = forwardCheck(schedule, availableBots, slot, bot);

            if (pruneResult.isPresent()) {
                List<int[]> removals = pruneResult.get();

                if (CSPFramework.canSatisfyCoverage(schedule, availableBots)) {
                    if (traceEnabled && stepNumber <= 3) {
                        System.out.println("  Forward Check: domains after pruning:");
                        printRemainingDomains(schedule, availableBots);
                    }

                    if (backtrack(schedule, availableBots, traceEnabled)) {
                        return true;
                    }
                }
                restorePrunedDomains(removals, availableBots);
            } else {
                if (traceEnabled && stepNumber <= 3) {
                    System.out.println("  Forward Check: DOMAIN WIPEOUT! Backtracking.");
                }
                IntStream.range(0, availableBots.size())
                        .forEach(i -> availableBots.set(i, savedDomains.get(i)));
            }

            schedule[slot] = null;
        }

        return false;
    }

    public static void main(String[] args) throws Exception {
        String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        CSPFramework.readInput(inputFile);

        System.out.println("#".repeat(55));
        System.out.println("# SECURITY BOT SCHEDULING - CSP");
        System.out.println("# Backtracking + MRV + Forward Checking");
        System.out.println("#".repeat(55));
        System.out.println();

        CSPFramework.printConstraintGraph();

        final List<List<String>> availableBots = CSPFramework.initDomains();
        System.out.println("Initial Domains:");
        IntStream.range(0, CSPFramework.NUM_SLOTS)
                .forEach(i -> System.out.println("  " + CSPFramework.SLOT_NAMES[i]
                        + " = " + availableBots.get(i)));
        System.out.println();

        System.out.println("=== First 3 Steps of Backtracking with MRV ===\n");

        final String[] schedule = new String[CSPFramework.NUM_SLOTS];
        final long startTime = System.currentTimeMillis();

        final boolean solved = backtrack(schedule, availableBots, true);
        final long elapsedMs = System.currentTimeMillis() - startTime;

        System.out.println();
        CSPFramework.printResult(
                "Backtracking Search",
                "MRV (Minimum Remaining Values)",
                "Forward Checking",
                solved, schedule, assignmentCount, elapsedMs);
    }
}