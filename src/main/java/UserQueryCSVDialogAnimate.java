
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class UserQueryCSVDialogAnimate {

    private JFrame mainFrame;
    private GenericDataReaderIntoMapAnimate ourGenericDataReaderIntoMap; // Use the animate version
    private String userInput;
    private JTextArea resultsArea;
    private JTextField inputField;
    private JLabel queryLabel;
    public static final String DEFAULT_DIRECTORY = ".\\DataFiles";
    public static final String DEFAULT_FILE = "TopSongs5000Edited.csv";
    public static final String[] SPEED_OPTIONS = {".25", ".5", ".75", "1", "1.25", "1.50", "1.75", "2", "3", "4", "5"};
    private JFrame dummyFrameForFileChooser; // Add a dummy frame for the file chooser

    public static int parseSpeedToDelayMs(String speedString) {
        if (speedString == null) {
            return 1000;
        }
        try {
            double speedSeconds = Double.parseDouble(speedString.trim());
            return (int) Math.round(speedSeconds * 1000);
        } catch (NumberFormatException e) {
            String cleaned = speedString.replaceAll("[^0-9.]", "").trim();
            if (!cleaned.isEmpty()) {
                try {
                    return (int) Math.round(Double.parseDouble(cleaned) * 1000);
                } catch (NumberFormatException ignored) {}
            }
            return 1000;
        }
    }

    public UserQueryCSVDialogAnimate() {
        // Initialize the dummy frame
        dummyFrameForFileChooser = new JFrame();
        dummyFrameForFileChooser.setUndecorated(true); // Make it invisible
        dummyFrameForFileChooser.setSize(0, 0);       // No size
        dummyFrameForFileChooser.setLocationRelativeTo(null); // Center if needed, but not visible
        dummyFrameForFileChooser.setVisible(false);
        showFileChooser();
    }

    private void showFileChooser() {
        // Set up the JFileChooser
        JFileChooser fileChooser = new JFileChooser(DEFAULT_DIRECTORY);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Optional: add a file filter for CSV
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        // Preselect the default file if it exists
        File defaultFile = new File(DEFAULT_DIRECTORY, DEFAULT_FILE);
        if (defaultFile.exists()) {
            fileChooser.setSelectedFile(defaultFile);
        }

        // Use the dummyFrameForFileChooser as the parent
        int result = fileChooser.showOpenDialog(dummyFrameForFileChooser);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Choose tree type
            String[] options = {"Red-Black Tree", "Splay Tree", "AVL Tree", "Unbalanced Tree"};
            String treeType = (String) JOptionPane.showInputDialog(
                dummyFrameForFileChooser,
                "Choose tree type for animation:",
                "Tree Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (treeType != null) {
                // Choose animation speed
                String speedString = (String) JOptionPane.showInputDialog(
                    dummyFrameForFileChooser,
                    "Choose animation speed (seconds):",
                    "Animation Speed Selection",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    SPEED_OPTIONS,
                    "1"
                );

                if (speedString != null) {
                    int delayMs = parseSpeedToDelayMs(speedString);
                    openUserQueryDialog(selectedFile.getAbsolutePath(), treeType, delayMs);
                } else {
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0); // Exit if cancelled
        }
        // Dispose the dummy frame after use
        dummyFrameForFileChooser.dispose();
    }

    private void openUserQueryDialog(String filename, String treeType) {
        openUserQueryDialog(filename, treeType, 1000);
    }

    private void openUserQueryDialog(String filename, String treeType, int animationDelayMs) {
        // Prepare the main dialog window, but keep it hidden initially
        mainFrame = new JFrame("User Query CSV Dialog - Loading (" + treeType + ")...");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(700, 400);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        // Loading panel
        JPanel loadingPanel = new JPanel(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading data, please wait... This may take a while as animations play.", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);
        mainFrame.add(loadingPanel, BorderLayout.CENTER); // Show loading panel initially
        mainFrame.setVisible(true);

        // Use SwingWorker to load data in background
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Instantiate the GenericDataReaderIntoMapAnimate with the selected file, tree type, and animation speed
                ourGenericDataReaderIntoMap = new GenericDataReaderIntoMapAnimate(filename, treeType, animationDelayMs);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions during doInBackground
                    // Remove loading panel
                    mainFrame.remove(loadingPanel);

                    // Rebuild the main dialog window content after data is loaded
                    // Panel at the top for input and submit button
                    JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

                    // Label using keyTitle
                    String labelText = ourGenericDataReaderIntoMap.keyTitle + ": <Enter key>";
                    queryLabel = new JLabel(labelText);
                    queryLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

                    inputField = new JTextField();
                    inputField.setFont(new Font("SansSerif", Font.PLAIN, 16));

                    JButton submitButton = new JButton("Submit");
                    submitButton.setFont(new Font("SansSerif", Font.BOLD, 16));

                    // Submit button action
                    submitButton.addActionListener(e -> handleUserInput());

                    // Allow Enter key to trigger submit
                    inputField.addActionListener(e -> handleUserInput());

                    inputPanel.add(queryLabel, BorderLayout.WEST);
                    inputPanel.add(inputField, BorderLayout.CENTER);
                    inputPanel.add(submitButton, BorderLayout.EAST);

                    // Results area in center
                    resultsArea = new JTextArea();
                    resultsArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    resultsArea.setLineWrap(true);
                    resultsArea.setWrapStyleWord(true);
                    resultsArea.setEditable(false);
                    JScrollPane resultsScroll = new JScrollPane(resultsArea);

                    // Panel at the bottom for Clear button
                    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JButton clearButton = new JButton("Clear");
                    clearButton.setFont(new Font("SansSerif", Font.PLAIN, 16));

                    clearButton.addActionListener(e -> {
                        inputField.setText("");
                        resultsArea.setText("");
                        inputField.requestFocusInWindow();
                    });

                    bottomPanel.add(clearButton);

                    mainFrame.add(inputPanel, BorderLayout.NORTH);
                    mainFrame.add(resultsScroll, BorderLayout.CENTER);
                    mainFrame.add(bottomPanel, BorderLayout.SOUTH);

                    mainFrame.setTitle("User Query CSV Dialog"); // Update title
                    mainFrame.revalidate(); // Revalidate the frame to show new components
                    mainFrame.repaint();    // Repaint
                    inputField.requestFocusInWindow(); // Give focus to input field
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        }.execute(); // Execute the SwingWorker
    }

    private void handleUserInput() {
        userInput = inputField.getText().trim();
        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Please enter a key to look up.", "Input Required", JOptionPane.WARNING_MESSAGE);
            inputField.requestFocusInWindow();
            return;
        }
        // Attempt to query the CSV using keyValueLookup
        String result = ourGenericDataReaderIntoMap.keyValueLookup(userInput);
        if (result != null) {
            appendResult("> " + userInput);
            appendResult(result);
        } else {
            // Try keyValueNearby if not found
            String nearby = ourGenericDataReaderIntoMap.keyValueNearby(userInput);
            appendResult("> " + userInput);
            appendResult(nearby != null ? nearby : "No result or close matches found.");
        }
        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    private void appendResult(String text) {
        resultsArea.append(text + "\n");
        resultsArea.setCaretPosition(resultsArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        // For better cross-platform appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(UserQueryCSVDialogAnimate::new);
    }
}
