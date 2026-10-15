import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BinarySearchTreesTest {

    private static final String[][] EXPECTED_DATA_BOOK_FIGURE_11_6_6 = {
        {"Action", "IntegerKey", "IntegerValue"},
        {"A", "4", "4"},
        {"A", "7", "7"},
        {"A", "12", "12"},
        {"A", "15", "15"},
        {"A", "3", "3"},
        {"A", "5", "5"},
        {"A", "14", "14"},
        {"A", "18", "18"},
        {"A", "16", "16"},
        {"A", "17", "17"}
    };

    private static final String[][] EXPECTED_DATA_BOOK_FIGURE_11_6_10 = {
        {"Action", "IntegerKey", "IntegerValue"},
        {"A", "4", "4"},
        {"A", "7", "7"},
        {"A", "12", "12"},
        {"A", "15", "15"},
        {"A", "3", "3"},
        {"A", "5", "5"},
        {"A", "14", "14"},
        {"A", "18", "18"},
        {"A", "16", "16"},
        {"A", "17", "17"},
        {"D", "3", "3"},
        {"D", "12", "12"},
        {"D", "17", "17"},
        {"D", "18", "18"},
        {"D", "15", "15"},
        {"D", "16", "16"}
    };

    @Test
    void testBookFigure11_6_6_CSVExistsAndContent() {
        File file = getDataFile("BookFigure11-6-6.csv");
        assertTrue(file.exists(), "CSV file BookFigure11-6-6.csv should exist in directory datafiles");
        verifyCSVContent(file, EXPECTED_DATA_BOOK_FIGURE_11_6_6);
    }

    @Test
    void testBookFigure11_6_10_CSVExistsAndContent() {
        File file = getDataFile("BookFigure11-6-10.csv");
        assertTrue(file.exists(), "CSV file BookFigure11-6-10.csv should exist in directory datafiles");
        verifyCSVContent(file, EXPECTED_DATA_BOOK_FIGURE_11_6_10);
    }

    private static File getDataFile(String filename) {
        File file = new File("datafiles", filename);
        if (file.exists()) {
            return file;
        }
        File altDir = new File("DataFiles", filename);
        if (altDir.exists()) {
            return altDir;
        }
        File cwdFile = new File(filename);
        if (cwdFile.exists()) {
            return cwdFile;
        }
        // Fallback search case-insensitively for the datafiles directory
        File cwd = new File(".");
        File[] matches = cwd.listFiles((dir, name) -> name.equalsIgnoreCase("datafiles"));
        if (matches != null) {
            for (File dir : matches) {
                if (dir.isDirectory()) {
                    File candidate = new File(dir, filename);
                    if (candidate.exists()) {
                        return candidate;
                    }
                    File[] fileMatches = dir.listFiles((d, name) -> name.equalsIgnoreCase(filename));
                    if (fileMatches != null && fileMatches.length > 0) {
                        return fileMatches[0];
                    }
                }
            }
        }
        return file;
    }

    private void verifyCSVContent(File file, String[][] expectedData) {
        assertTrue(file.exists(), "CSV file should exist in directory datafiles: " + file.getPath());

        List<String[]> actualRows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                // Strip UTF-8 BOM if present on the first line
                if (firstLine) {
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    firstLine = false;
                }

                // Ignore completely blank lines or lines containing only whitespace
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split by comma preserving all tokens
                String[] rawCols = line.split(",", -1);
                // Trim each column value to be forgiving about extra spaces
                String[] trimmedCols = new String[rawCols.length];
                for (int i = 0; i < rawCols.length; i++) {
                    trimmedCols[i] = rawCols[i].trim();
                }
                actualRows.add(trimmedCols);
            }
        } catch (IOException e) {
            fail("IOException reading CSV file " + file.getName() + ": " + e.getMessage());
        }

        assertEquals(expectedData.length, actualRows.size(),
            String.format("Row count mismatch in %s: expected %d non-empty rows, but found %d.",
                file.getName(), expectedData.length, actualRows.size()));

        for (int r = 0; r < expectedData.length; r++) {
            String[] expectedCols = expectedData[r];
            String[] actualCols = actualRows.get(r);

            assertEquals(expectedCols.length, actualCols.length,
                String.format("Row %d in %s has %d columns, expected %d columns.",
                    r + 1, file.getName(), actualCols.length, expectedCols.length));

            for (int c = 0; c < expectedCols.length; c++) {
                assertEquals(expectedCols[c].trim(), actualCols[c],
                    String.format("Row %d, Column %d (%s) mismatch in %s",
                        r + 1, c + 1, expectedData[0][c].trim(), file.getName()));
            }
        }
    }
}
