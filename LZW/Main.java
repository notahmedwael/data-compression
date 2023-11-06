import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static JLabel messageLabel; // Label for displaying success/failure messages

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Create the main JFrame for the application.
        JFrame frame = new JFrame("LZW Compression");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setLayout(new BorderLayout());

        // Create a top panel to display success/failure messages.
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        messageLabel = new JLabel(""); // Message label
        topPanel.add(messageLabel); // Add message label to the top panel
        frame.add(topPanel, BorderLayout.NORTH);

        // Create a panel to hold the compression and decompression buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 100, 200)); // Grid layout with 3 rows and 1 column

        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");
        JButton exitButton = new JButton("Exit");

        // ActionListener for the Compress button
        compressButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File inputFile = fileChooser.getSelectedFile();

                // Choose an output file for the compressed data.
                fileChooser.setSelectedFile(null);
                result = fileChooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File outputFile = fileChooser.getSelectedFile();

                    try {
                        compress(inputFile, outputFile); // Perform compression.
                    } catch (IOException ex) {
                        showMessage("Compression failed: " + ex.getMessage(), Color.RED);
                    }
                }
            }
        });

        // ActionListener for the Decompress button
        decompressButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File inputFile = fileChooser.getSelectedFile();

                // Choose an output file for the decompressed data.
                fileChooser.setSelectedFile(null);
                result = fileChooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File outputFile = fileChooser.getSelectedFile();

                    try {
                        decompress(inputFile, outputFile); // Perform decompression.
                    } catch (IOException ex) {
                        showMessage("Decompression failed: " + ex.getMessage(), Color.RED);
                    }
                }
            }
        });

        // ActionListener for the Exit button
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to the buttonPanel.
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        // Create a bottom panel for the Exit button.
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(exitButton);

        // Add panels to the main frame and set the background color.
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        frame.setVisible(true);
    }

    // Methods for file compression and decompression

    private static void compress(File inputFile, File outputFile) throws IOException {
        // Read text from the input file.
        String textToCompress = readTextFromFile(inputFile);

        // Perform LZW compression on the text.
        List<Integer> compressedData = LZW.compress(textToCompress);

        // Write the compressed data to the output file.
        writeCompressedDataToFile(outputFile, compressedData);

        // Display a success message in green.
        showMessage("Compression succeeded", Color.GREEN);
    }

    private static void decompress(File inputFile, File outputFile) throws IOException {
        // Read compressed data from the input file.
        List<Integer> compressedData = readCompressedDataFromFile(inputFile);

        // Perform LZW decompression on the data.
        String decompressedText = LZW.decompress(compressedData);

        // Write the decompressed text to the output file.
        writeDecompressedTextToFile(outputFile, decompressedText);

        // Display a success message in green.
        showMessage("Decompression succeeded", Color.GREEN);
    }

    // File I/O methods

    private static String readTextFromFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void writeCompressedDataToFile(File file, List<Integer> compressedData) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int code : compressedData) {
                writer.write(String.valueOf(code));
                writer.write(" ");
            }
        }
    }

    private static List<Integer> readCompressedDataFromFile(File file) throws IOException {
        List<Integer> compressedData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            String[] tokens = line.split(" ");
            for (String token : tokens) {
                compressedData.add(Integer.parseInt(token));
            }
        }
        return compressedData;
    }

    private static void writeDecompressedTextToFile(File file, String decompressedText) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(decompressedText);
        }
    }

    private static void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }
}