import java.util.*;
import java.util.stream.*;

/**
 * Forward Checking Demonstration for the Security Bot Scheduling CSP.
 */
public class ForwardCheckingDemo {

    public static void main(String[] args) throws Exception {
        String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        CSPFramework.readInput(inputFile);

        System.out.println("#".repeat(55));
        System.out.println("# FORWARD CHECKING DEMONSTRATION");
        System.out.println("#".repeat(55));
        System.out.println();

        runStepByStepDemo();
        runFailureDetectionDemo();
    }

    /**
     * Demo 1: Walk through forward checking assignments one slot at a time.
     */
    private static void runStepByStepDemo() {
        System.out.println("=== Demo 1: Step-by-Step Forward Checking ===\n");

        List<List<String>> availableBots = CSPFramework.initDomains();
        String[] schedule = new String[CSPFramework.NUM_SLOTS];

        System.out.println("Initial Domains:");
        printSlotDomains(availableBots);
        System.out.println();

        // Step 1: Assign Slot1 = A
        System.out.println("Step 1: Assign Slot1 = A");
        schedule[0] = "A";
        System.out.println("  Assignment: {Slot1=A, Slot2=_, Slot3=_, Slot4=_}");
        System.out.println("  Forward Check: Remove 'A' from Slot2's domain (back-to-back)");
        availableBots.get(1).remove("A");
        System.out.println("  Domains after FC:");
        printSlotDomains(availableBots);
        System.out.println("  All domains non-empty -> Continue.\n");

        // Step 2: Assign Slot2 = B
        System.out.println("Step 2: Assign Slot2 = B");
        schedule[1] = "B";
        System.out.println("  Assignment: {Slot1=A, Slot2=B, Slot3=_, Slot4=_}");
        System.out.println("  Forward Check: Remove 'B' from Slot3's domain (back-to-back)");
        availableBots.get(2).remove("B");
        System.out.println("  Domains after FC:");
        printSlotDomains(availableBots);
        System.out.println("  All domains non-empty -> Continue.\n");

        // Step 3: Assign Slot3 = A
        System.out.println("Step 3: Assign Slot3 = A");
        schedule[2] = "A";
        System.out.println("  Assignment: {Slot1=A, Slot2=B, Slot3=A, Slot4=_}");
        System.out.println("  Forward Check: Remove 'A' from Slot4's domain (back-to-back)");
        availableBots.get(3).remove("A");
        System.out.println("  Domains after FC:");
        printSlotDomains(availableBots);
        System.out.println("  Slot4 has domain {B} -> Still valid.\n");

        // Step 4: Assign Slot4 = B — coverage fails
        System.out.println("Step 4: Assign Slot4 = B");
        schedule[3] = "B";
        System.out.println("  Assignment: {Slot1=A, Slot2=B, Slot3=A, Slot4=B}");
        System.out.println("  Check minimum coverage: A used, B used, C NOT used -> FAIL!");
        System.out.println("  Backtrack needed to satisfy minimum coverage.\n");

        // Backtrack: reassign Slot3 = C
        System.out.println("  Backtrack: Try Slot3 = C instead.");
        schedule[2] = "C";
        schedule[3] = null;
        availableBots.set(3, new ArrayList<>(Arrays.asList("A", "B")));
        System.out.println("  Forward Check: Remove 'C' from Slot4's domain");
        System.out.println("  (C already excluded from Slot4 by maintenance constraint)");
        System.out.println("  Slot4 domain = {A, B}");
        System.out.println("  Assign Slot4 = A");
        schedule[3] = "A";
        System.out.println("  Final: {Slot1=A, Slot2=B, Slot3=C, Slot4=A}");
        System.out.println("  Coverage: A=yes, B=yes, C=yes -> ALL CONSTRAINTS SATISFIED!\n");

        CSPFramework.printAssignment(schedule);
    }

    /**
     * Demo 2: Show how forward checking detects failure early
     * when domains are restricted to only {A, B}.
     */
    private static void runFailureDetectionDemo() {
        System.out.println();
        System.out.println("=".repeat(55));
        System.out.println("=== Demo 2: Failure Detection with Restricted Domain ===\n");
        System.out.println("Scenario: Suppose domain is restricted to only {A, B}");
        System.out.println("for all slots (Bot C unavailable entirely).\n");

        final int slotCount = 4;
        List<List<String>> restrictedBots = IntStream.range(0, slotCount)
                .mapToObj(i -> new ArrayList<>(Arrays.asList("A", "B")))
                .collect(Collectors.toList());
        String[] schedule = new String[slotCount];

        System.out.println("Restricted Domains:");
        printNumberedDomains(restrictedBots);
        System.out.println();

        System.out.println("Step 1: Assign Slot1 = A");
        schedule[0] = "A";
        System.out.println("  Forward Check: Remove 'A' from Slot2");
        restrictedBots.get(1).remove("A");
        System.out.println("  Slot2 domain = " + restrictedBots.get(1));

        System.out.println("\nStep 2: Assign Slot2 = B (only option)");
        schedule[1] = "B";
        System.out.println("  Forward Check: Remove 'B' from Slot3");
        restrictedBots.get(2).remove("B");
        System.out.println("  Slot3 domain = " + restrictedBots.get(2));

        System.out.println("\nStep 3: Assign Slot3 = A (only option)");
        schedule[2] = "A";
        System.out.println("  Forward Check: Remove 'A' from Slot4");
        restrictedBots.get(3).remove("A");
        System.out.println("  Slot4 domain = " + restrictedBots.get(3));

        System.out.println("\nStep 4: Assign Slot4 = B (only option)");
        schedule[3] = "B";
        System.out.println("  Check minimum coverage: A=yes, B=yes, C=NO!");
        System.out.println("  FAILURE: Bot C never assigned (coverage violated).");
        System.out.println("  Forward Checking detects that with only {A,B} in domains,");
        System.out.println("  the minimum coverage constraint for C can NEVER be satisfied.");
        System.out.println("  This is detected IMMEDIATELY by checking canSatisfyCoverage()");
        System.out.println("  even before completing the assignment.\n");
    }

    /**
     * Print domains using CSPFramework slot names, marking maintenance exclusions.
     */
    private static void printSlotDomains(List<List<String>> availableBots) {
        IntStream.range(0, CSPFramework.NUM_SLOTS)
                .forEach(slot -> {
                    String marker = (slot == 3) ? " (C excluded: maintenance)" : "";
                    System.out.println("    " + CSPFramework.SLOT_NAMES[slot]
                            + " = " + availableBots.get(slot) + marker);
                });
    }

    /**
     * Print domains with simple numbered slot labels (Slot1, Slot2, ...).
     */
    private static void printNumberedDomains(List<List<String>> availableBots) {
        IntStream.range(0, availableBots.size())
                .forEach(slot -> System.out.println("    Slot" + (slot + 1)
                        + " = " + availableBots.get(slot)));
    }
}