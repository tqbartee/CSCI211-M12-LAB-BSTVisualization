
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting RBTreeMapAnimate demonstration...");

        // Create an instance of RBTreeMapAnimate
        RBTreeMapAnimate<Integer, String> rbTree = new RBTreeMapAnimate<>();

        // Add some entries to the tree to trigger animation
        System.out.println("Adding (50, \"Fifty\")");
        rbTree.put(50, "Fifty");
        System.out.println("Adding (25, \"Twenty-Five\")");
        rbTree.put(25, "Twenty-Five");
        System.out.println("Adding (75, \"Seventy-Five\")");
        rbTree.put(75, "Seventy-Five");
        System.out.println("Adding (10, \"Ten\")");
        rbTree.put(10, "Ten");
        System.out.println("Adding (30, \"Thirty\")");
        rbTree.put(30, "Thirty");
        System.out.println("Adding (60, \"Sixty\")");
        rbTree.put(60, "Sixty");
        System.out.println("Adding (80, \"Eighty\")");
        rbTree.put(80, "Eighty");
        System.out.println("Adding (5, \"Five\")");
        rbTree.put(5, "Five");
        System.out.println("Adding (35, \"Thirty-Five\")");
        rbTree.put(35, "Thirty-Five");
        System.out.println("Adding (90, \"Ninety\")");
        rbTree.put(90, "Ninety");
        System.out.println("Adding (70, \"Seventy\")");
        rbTree.put(70, "Seventy");

        // Signal that tree building is complete
        rbTree.finishAnimation();

        System.out.println("\nDemonstration complete. Observe the console output and the Swing windows.");
        }
        }