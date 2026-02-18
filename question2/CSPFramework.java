import java.util.*;
import java.io.*;
import java.util.stream.*;

/**
 * CSPFramework - Shared utility class for the Security Bot Scheduling CSP.
 *
 * Problem:
 *   Variables : {Slot1, Slot2, Slot3, Slot4}
 *   Domains   : Each slot can be assigned {A, B, C}
 *   Constraints:
 *     1. No Back-to-Back: A bot cannot work two consecutive slots
 *     2. Maintenance Break: Bot C cannot work in Slot 4
 *     3. Minimum Coverage: Every bot (A, B, C) must be used at least once
 */
public class CSPFramework {

    // Problem constants
    public static final String[] BOTS = {"A", "B", "C"};
    public static final int NUM_SLOTS = 4;
    public static final String[] SLOT_NAMES = {"Slot1", "Slot2", "Slot3", "Slot4"};

    private static final int MAINTENANCE_SLOT = 3;
    private static final String MAINTENANCE_BOT = "C";

    /**
     * Read CSP configuration from input file.
     * Parses bots, slots, and unary exclusion rules.
     */
    public static Map<String, Object> readInput(String filepath) throws Exception {
        Map<String, Object> config = new HashMap<>();
        List<String> botList = new ArrayList<>();
        List<Integer> slotList = new ArrayList<>();
        Map<String, Set<Integer>> exclusionRules = new HashMap<>();

        try (Scanner reader = new Scanner(new File(filepath))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("BOTS:")) {
                    Arrays.stream(line.substring(5).trim().split(","))
                            .map(String::trim)
                            .forEach(botList::add);
                } else if (line.startsWith("SLOTS:")) {
                    Arrays.stream(line.substring(6).trim().split(","))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .forEach(slotList::add);
                } else if (line.startsWith("BOT_C_NOT_IN:")) {
                    Set<Integer> excludedSlots = Arrays.stream(line.substring(13).trim().split(","))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .collect(Collectors.toSet());
                    exclusionRules.put(MAINTENANCE_BOT, excludedSlots);
                }
            }
        }

        config.put("bots", botList.isEmpty() ? Arrays.asList(BOTS) : botList);
        config.put("slots", slotList.isEmpty() ? Arrays.asList(1, 2, 3, 4) : slotList);
        config.put("unaryExclusions", exclusionRules);
        return config;
    }

    /**
     * Initialize domains for each slot variable.
     * Applies unary constraint: Bot C cannot be in Slot 4.
     */
    public static List<List<String>> initDomains() {
        return IntStream.range(0, NUM_SLOTS)
                .mapToObj(slot -> {
                    List<String> bots = new ArrayList<>(Arrays.asList(BOTS));
                    if (slot == MAINTENANCE_SLOT) {
                        bots.remove(MAINTENANCE_BOT);
                    }
                    return bots;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check No Back-to-Back constraint:
     * No bot can be assigned to two consecutive slots.
     */
    public static boolean checkNoBackToBack(String[] schedule) {
        return IntStream.range(0, schedule.length - 1)
                .noneMatch(i -> schedule[i] != null
                        && schedule[i + 1] != null
                        && schedule[i].equals(schedule[i + 1]));
    }

    /**
     * Check Maintenance Break constraint:
     * Bot C cannot be assigned to Slot 4 (index 3).
     */
    public static boolean checkMaintenanceBreak(String[] schedule) {
        return !MAINTENANCE_BOT.equals(schedule[MAINTENANCE_SLOT]);
    }

    /**
     * Check Minimum Coverage constraint:
     * Every bot {A, B, C} must appear at least once.
     * Only meaningful when the schedule is complete.
     */
    public static boolean checkMinimumCoverage(String[] schedule) {
        Set<String> usedBots = Arrays.stream(schedule)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return usedBots.containsAll(Arrays.asList(BOTS));
    }

    /**
     * Check if assigning a bot to a slot is consistent with current partial schedule.
     */
    public static boolean isConsistent(String[] schedule, int slot, String bot) {
        // Check No Back-to-Back with left neighbor
        if (slot > 0 && bot.equals(schedule[slot - 1])) {
            return false;
        }

        // Check No Back-to-Back with right neighbor
        if (slot < NUM_SLOTS - 1 && bot.equals(schedule[slot + 1])) {
            return false;
        }

        // Check Maintenance Break
        if (slot == MAINTENANCE_SLOT && MAINTENANCE_BOT.equals(bot)) {
            return false;
        }

        return true;
    }

    /**
     * Check if minimum coverage CAN still be satisfied given the current
     * partial schedule and remaining domains.
     * Returns false if any bot has no remaining slot where it could be placed.
     */
    public static boolean canSatisfyCoverage(String[] schedule, List<List<String>> availableBots) {
        Set<String> alreadyUsed = IntStream.range(0, NUM_SLOTS)
                .filter(i -> schedule[i] != null)
                .mapToObj(i -> schedule[i])
                .collect(Collectors.toSet());

        return Arrays.stream(BOTS)
                .filter(bot -> !alreadyUsed.contains(bot))
                .allMatch(bot -> IntStream.range(0, NUM_SLOTS)
                        .anyMatch(slot -> schedule[slot] == null
                                && availableBots.get(slot).contains(bot)));
    }

    /**
     * Print the schedule in a formatted table.
     */
    public static void printAssignment(String[] schedule) {
        System.out.println("\nFinal Assignment:");
        System.out.println("+--------+-----+");
        System.out.println("| Slot   | Bot |");
        System.out.println("+--------+-----+");
        IntStream.range(0, NUM_SLOTS)
                .forEach(i -> System.out.printf("| Slot %d |  %s  |%n", i + 1,
                        Optional.ofNullable(schedule[i]).orElse("?")));
        System.out.println("+--------+-----+");
    }

    /**
     * Print complete result summary with constraint verification.
     */
    public static void printResult(String algorithm, String heuristic,
                                   String inference, boolean solved,
                                   String[] schedule, int assignmentCount,
                                   long elapsedMs) {
        System.out.println("=".repeat(55));
        System.out.println("Algorithm : " + algorithm);
        System.out.println("Heuristic : " + heuristic);
        System.out.println("Inference : " + inference);
        System.out.println("=".repeat(55));
        System.out.println("Status           : " + (solved ? "SUCCESS" : "FAILURE"));
        System.out.println("Total Assignments: " + assignmentCount);
        System.out.println("Time Taken       : " + elapsedMs + " ms");

        System.out.println("\nConstraints Applied:");
        System.out.println("  1. No Back-to-Back  : Slot[i] != Slot[i+1]");
        System.out.println("  2. Maintenance Break: Bot C not in Slot 4");
        System.out.println("  3. Minimum Coverage : All bots {A,B,C} used");

        if (solved && schedule != null) {
            printAssignment(schedule);
            printConstraintVerification(schedule);
        }
        System.out.println();
    }

    /**
     * Verify and print whether each constraint is satisfied or violated.
     */
    private static void printConstraintVerification(String[] schedule) {
        System.out.println("\nConstraint Verification:");
        System.out.println("  No Back-to-Back  : "
                + (checkNoBackToBack(schedule) ? "SATISFIED" : "VIOLATED"));
        System.out.println("  Maintenance Break : "
                + (checkMaintenanceBreak(schedule) ? "SATISFIED" : "VIOLATED"));
        System.out.println("  Minimum Coverage  : "
                + (checkMinimumCoverage(schedule) ? "SATISFIED" : "VIOLATED"));
    }

    /**
     * Print constraint graph (adjacency representation).
     */
    public static void printConstraintGraph() {
        System.out.println("Constraint Graph (No Back-to-Back edges):");
        System.out.println("  Nodes: Slot1, Slot2, Slot3, Slot4");
        System.out.println("  Edges (binary constraints):");
        System.out.println("    Slot1 --- Slot2  (Slot1 != Slot2)");
        System.out.println("    Slot2 --- Slot3  (Slot2 != Slot3)");
        System.out.println("    Slot3 --- Slot4  (Slot3 != Slot4)");
        System.out.println();
        System.out.println("  Visual:");
        System.out.println("    [Slot1] ------- [Slot2] ------- [Slot3] ------- [Slot4]");
        System.out.println("    {A,B,C}         {A,B,C}         {A,B,C}         {A,B}");
        System.out.println("                                                  (C excluded)");
        System.out.println();
    }
}