
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class UserQueryCSVDialog {

    private JFrame mainFrame;
    private GenericDataReaderIntoMap ourGenericDataReaderIntoMap;
    private String userInput;
    private JTextArea resultsArea;
    private JTextField inputField;
    private JLabel queryLabel;
    public static final String DEFAULT_DIRECTORY = ".\\DataFiles";
    public static final String DEFAULT_FILE = "TopSongs5000Edited.csv";

    public UserQueryCSVDialog() {
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

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Choose tree type
            String[] options = {"Red-Black Tree", "Splay Tree", "AVL Tree", "Unbalanced Tree"};
            String treeType = (String) JOptionPane.showInputDialog(
                null,
                "Choose tree type for animation:",
                "Tree Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (treeType != null) {
                openUserQueryDialog(selectedFile.getAbsolutePath(), treeType);
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0); // Exit if cancelled
        }
    }

    private void openUserQueryDialog(String filename, String treeType) {
        // Prepare the main dialog window, but keep it hidden initially
        mainFrame = new JFrame("User Query CSV Dialog - Loading (" + treeType + ")...");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(700, 400);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        // Loading panel
        JPanel loadingPanel = new JPanel(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading data, please wait...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);
        mainFrame.add(loadingPanel, BorderLayout.CENTER); 
        mainFrame.setVisible(true);

        // Use SwingWorker to load data in background
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Instantiate the GenericDataReaderIntoMap with the selected file and tree type
                ourGenericDataReaderIntoMap = new GenericDataReaderIntoMap(filename, treeType);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); 
                    mainFrame.remove(loadingPanel);

                    // Rebuild the main dialog window content after data is loaded
                    JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

                    String labelText = ourGenericDataReaderIntoMap.keyTitle + ": <Enter key>";
                    queryLabel = new JLabel(labelText);
                    queryLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

                    inputField = new JTextField();
                    inputField.setFont(new Font("SansSerif", Font.PLAIN, 16));

                    JButton submitButton = new JButton("Submit");
                    submitButton.setFont(new Font("SansSerif", Font.BOLD, 16));

                    submitButton.addActionListener(e -> handleUserInput());
                    inputField.addActionListener(e -> handleUserInput());

                    inputPanel.add(queryLabel, BorderLayout.WEST);
                    inputPanel.add(inputField, BorderLayout.CENTER);
                    inputPanel.add(submitButton, BorderLayout.EAST);

                    resultsArea = new JTextArea();
                    resultsArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    resultsArea.setLineWrap(true);
                    resultsArea.setWrapStyleWord(true);
                    resultsArea.setEditable(false);
                    JScrollPane resultsScroll = new JScrollPane(resultsArea);

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

                    mainFrame.setTitle("User Query CSV Dialog - " + treeType);
                    mainFrame.revalidate();
                    mainFrame.repaint();
                    inputField.requestFocusInWindow();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        }.execute();
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
        SwingUtilities.invokeLater(UserQueryCSVDialog::new);
    }
}
