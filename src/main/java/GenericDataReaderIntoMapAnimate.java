import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import com.zybooks.dsaj.map.SortedMap;
//import com.zybooks.dsaj.searchtree.RBTreeMapAnimate; // Use RBTreeMapAnimate
import edu.desu.utilities.NumericStringComparator;

public class GenericDataReaderIntoMapAnimate {
    //public RBTreeMapAnimate<String, String> keyValuePairs =
    //        new RBTreeMapAnimate<>(new NumericStringComparator()); // Use RBTreeMapAnimate
    // Use RBTreeMapAnimate
    public SortedMap<String, String> keyValuePairs;
    public String keyTitle;
    public String valueTitle;
    public int animationDelayMs = -1;

    public GenericDataReaderIntoMapAnimate(String fileName, String treeType) {
        this(fileName, treeType, -1);
    }

    public GenericDataReaderIntoMapAnimate(String fileName, String treeType, double animationSpeedSeconds) {
        this(fileName, treeType, (int) Math.round(animationSpeedSeconds * 1000));
    }

    public GenericDataReaderIntoMapAnimate(String fileName, String treeType, int animationDelayMs) {
        this.animationDelayMs = animationDelayMs;
        // Initialize the appropriate tree type
        switch (treeType) {
            case "Red-Black Tree":
                keyValuePairs = (animationDelayMs >= 0)
                        ? new RBTreeMapAnimate<>(new NumericStringComparator(), animationDelayMs)
                        : new RBTreeMapAnimate<>(new NumericStringComparator());
                break;
            case "Splay Tree":
                keyValuePairs = (animationDelayMs >= 0)
                        ? new SplayTreeMapAnimate<>(new NumericStringComparator(), animationDelayMs)
                        : new SplayTreeMapAnimate<>(new NumericStringComparator());
                break;
            case "AVL Tree":
                keyValuePairs = (animationDelayMs >= 0)
                        ? new AVLTreeMapAnimate<>(new NumericStringComparator(), animationDelayMs)
                        : new AVLTreeMapAnimate<>(new NumericStringComparator());
                break;
            case "Unbalanced Tree":
            default:
                keyValuePairs = (animationDelayMs >= 0)
                        ? new TreeMapAnimate<>(new NumericStringComparator(), animationDelayMs)
                        : new TreeMapAnimate<>(new NumericStringComparator());
                break;
        }
        if (animationDelayMs >= 0 && keyValuePairs instanceof TreeAnimation) {
            ((TreeAnimation<String, String>) keyValuePairs).setAnimationDelay(animationDelayMs);
        }
        // Read the CSV file data into the map
        String line;
        int lineno = 0;
        String data[];
        boolean isActionCSV = false;
        try (BufferedReader br =
                     new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                if (lineno == 0) {
                    // Save the header
                    data = line.split(",");
                    if (data.length >= 2) {
                        if (data[0].trim().equalsIgnoreCase("Action")) {
                            isActionCSV = true;
                            keyTitle = data[1].trim();
                            valueTitle = (data.length >= 3) ? data[2].trim() : "Value";
                        } else {
                            keyTitle = data[0].trim();
                            valueTitle = data[1].trim();
                        }
                    }
                }
                else if (lineno > 0) {
                    if (isActionCSV) {
                        data = line.split(",", 3);
                        if (data.length >= 2) {
                            String action = data[0].trim();
                            String keyInput = data[1].trim();
                            if (action.equalsIgnoreCase("A")) {
                                String valueInput = (data.length >= 3) ? data[2].trim() : "";
                                String fullValueInput = "Line " + lineno + ": " + valueInput;
                                String curValue = keyValuePairs.get(keyInput);
                                if (curValue != null) {
                                    fullValueInput = curValue + '\n' + fullValueInput;
                                }
                                keyValuePairs.put(keyInput, fullValueInput);
                            } else if (action.equalsIgnoreCase("D")) {
                                keyValuePairs.remove(keyInput);
                            }
                        }
                    } else {
                        data = line.split(",", 2);
                        if (data.length == 2) {
                            String keyInput = data[0].trim();
                            String valueInput = "Line " + lineno + ": " + data[1].trim();
                            String curValue = keyValuePairs.get(keyInput);
                            if (curValue != null) {
                                valueInput = curValue + '\n' + valueInput;
                            }
                            keyValuePairs.put(keyInput, valueInput);
                        }
                    }
                }
                lineno++;
            }
            // Signal that tree building is complete
            if (keyValuePairs instanceof TreeAnimation) {
                ((TreeAnimation<String, String>) keyValuePairs).finishAnimation();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Static method for key-value pair lookups
    public void keyValueLookupCommandLine() {
        // Test operation with user input
        Scanner symbolscanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter a key (" +
                    this.keyTitle +
                    ") or 'quit' to exit: ");
            String key = symbolscanner.nextLine().trim();
            if (key.equalsIgnoreCase("quit")) {
                break;
            }
            String value = (String) this.keyValuePairs.get(key);
            if (value != null) {
                System.out.println("Value (" +
                        this.valueTitle +
                        "): " + value);
            } else {
                System.out.println("Key not found.");
            }
        }
        System.out.println("Thank you for using the Key-Value Lookup program");
    }

    // Static method for key-value pair lookups
    public String keyValueLookup(String keyString) {
        // Test - Create the loaded Map object and
        // Interact with user
        String keyStringTrim = keyString.trim();
        String value = (String) keyValuePairs.get(keyStringTrim);
        return value;
    }

    public String keyValueNearby(String keyString) {
        // TODO: get the lower and higher key for a given input key
        // we will use this to clue the user into nearby keys if they
        // submit something that missed (no key)
        // produce the output shown in the slides (see before/after)
        // and expected by the unit tests
        //String lower = this.keyValuePairs.lowerKey(keyString);
        //String higher = this.keyValuePairs.higherKey(keyString);
        String returnString;
        //returnString = new String
        //        ("The entered key " + keyString + " was not in the map."
        //                + "\n" + "Lower key is: " + lower + "\n"
        //                + "Higher key is: " + higher);
        returnString = "Return this later";
        return returnString;  // or construct a response with both nearby keys
    }

    public static void test() {
        String fileName = new File("DataFiles/TopSongs5000Edited.csv").exists()
                ? "DataFiles/TopSongs5000Edited.csv"
                : "DataFiles/ThirdPartyDataCSVFiles/TopSongs5000Edited.csv";
        GenericDataReaderIntoMapAnimate ourGenericDataReaderIntoMap = // Use Animate version
                new GenericDataReaderIntoMapAnimate(fileName, "Red-Black Tree");
        ourGenericDataReaderIntoMap.keyValueLookupCommandLine();
    }

    public static void main(String[] args) {
        test();
    }
}