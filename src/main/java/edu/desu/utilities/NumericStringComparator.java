package edu.desu.utilities;

import java.util.Comparator;

/**
 * A comparator that attempts to compare strings as numbers first,
 * falling back to lexicographical comparison if parsing fails.
 */
public class NumericStringComparator implements Comparator<String> {

    @Override
    public int compare(String str1, String str2) {
        try {
            // Attempt to parse the strings as integers
            int num1 = Integer.parseInt(str1);
            int num2 = Integer.parseInt(str2);

            // Compare the parsed integer values using Integer.compare
            return Integer.compare(num1, num2);

        } catch (NumberFormatException e) {
            // If parsing fails for either string, fall back to string comparison
            return str1.compareTo(str2);
        }
    }
}