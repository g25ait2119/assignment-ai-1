
/**
 * ManuscriptSorter - Master runner for all search algorithms.
 */
public class ManuscriptSorter {

    public static void main(String[] args) throws Exception {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";

        System.out.println("#".repeat(60));
        System.out.println("#  MANUSCRIPT SORTING PROBLEM - COMPLETE ANALYSIS");
        System.out.println("#".repeat(60)+"\n");

        // Execute each algorithm's main method
        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2A: UNINFORMED SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        BFSSearch.main(new String[]{inputFile});
        DFSSearch.main(new String[]{inputFile});

        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2B: INFORMED SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        GreedyBestFirstSearch.main(new String[]{inputFile});
        AStarSearch.main(new String[]{inputFile});

        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2C: MEMORY-BOUNDED & LOCAL SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        IDAStarSearch.main(new String[]{inputFile});

    }
}
