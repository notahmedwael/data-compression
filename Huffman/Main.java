import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Represents a node in the Huffman tree
class HuffmanNode implements Comparable<HuffmanNode> {
    byte data;
    int frequency;
    HuffmanNode left, right;

    // Compare nodes based on their frequencies
    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}

// Represents the Huffman tree and provides methods to build and generate codes
class HuffmanTree {
    // Build the Huffman tree based on character frequencies
    public HuffmanNode buildTree(HashMap<Byte, Integer> frequencies) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        // Create leaf nodes for each character and add them to the priority queue
        for (byte b : frequencies.keySet()) {
            HuffmanNode node = new HuffmanNode();
            node.data = b;
            node.frequency = frequencies.get(b);
            priorityQueue.add(node);
        }

        // Build the Huffman tree by combining nodes until only one node remains
        while (priorityQueue.size() > 1) {
            HuffmanNode x = priorityQueue.poll();
            HuffmanNode y = priorityQueue.poll();

            HuffmanNode sum = new HuffmanNode();
            if (y == null) {
                // Handle the case when y is null (only one node remaining)
                sum.frequency = x.frequency;
                sum.left = x;
            } else {
                sum.frequency = x.frequency + y.frequency;
                sum.left = x;
                sum.right = y;
            }
            priorityQueue.add(sum);
        }

        return priorityQueue.poll();
    }

    // Generate Huffman codes for each character in the tree
    public void generateCodes(HuffmanNode root, String code, HashMap<Byte, String> codes) {
        if (root != null) {
            if (root.left == null && root.right == null) {
                // Leaf node, add the character and its code to the map
                codes.put(root.data, code);
            }
            // Recursively generate codes for the left and right subtrees
            generateCodes(root.left, code + "0", codes);
            generateCodes(root.right, code + "1", codes);
        }
    }
}

// Handles the compression and decompression of files using Huffman coding
class HuffmanCoding {
    private static final Logger LOGGER = Logger.getLogger(HuffmanCoding.class.getName());

    // Compress a file using Huffman coding
    public void compress(String inputFile, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Step 1: Calculate character frequencies
            HashMap<Byte, Integer> frequencies = new HashMap<>();
            byte[] inputData = Files.readAllBytes(Path.of(inputFile));

            for (byte data : inputData) {
                frequencies.put(data, frequencies.getOrDefault(data, 0) + 1);
            }

            // Step 2: Build the Huffman tree
            HuffmanTree huffmanTree = new HuffmanTree();
            HuffmanNode root = huffmanTree.buildTree(frequencies);

            // Step 3: Generate Huffman codes
            HashMap<Byte, String> codes = new HashMap<>();
            huffmanTree.generateCodes(root, "", codes);

            // Step 4: Write Huffman codes to the output file
            for (byte data : codes.keySet()) {
                writer.write(data + ":" + codes.get(data) + "\n");
            }

            // Step 5: Separate codes from compressed data using a special character
            writer.write("#####\n");

            // Step 6: Write compressed data to the output file
            StringBuilder compressedData = new StringBuilder();
            for (byte data : inputData) {
                compressedData.append(codes.get(data));
            }
            writer.write(compressedData.toString());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred during compression", e);
        }
    }

    // Decompress a file using Huffman coding
    public void decompress(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            // Step 1: Read Huffman codes from the input file
            List<String> lines = Files.readAllLines(Path.of(inputFile));
            int separatorIndex = lines.indexOf("#####");

            if (separatorIndex == -1) {
                LOGGER.log(Level.SEVERE, "Separator not found in compressed data. Invalid format.");
                return;
            }

            List<String> codesLines = lines.subList(0, separatorIndex);
            String compressedDataLine = lines.get(separatorIndex + 1);

            // Step 2: Parse Huffman codes
            HashMap<Byte, String> codes = new HashMap<>();
            for (String codeLine : codesLines) {
                String[] parts = codeLine.split(":");
                if (parts.length == 2) {
                    byte data = Byte.parseByte(parts[0]);
                    String code = parts[1];
                    codes.put(data, code);
                }
            }

            // Step 3: Decode the compressed data
            StringBuilder compressedDataStringBuilder = new StringBuilder(compressedDataLine);
            List<Byte> decodedData = new ArrayList<>();
            int start = 0;
            while (start < compressedDataStringBuilder.length()) {
                for (byte data : codes.keySet()) {
                    String code = codes.get(data);
                    if (compressedDataStringBuilder.substring(start).startsWith(code)) {
                        decodedData.add(data);
                        start += code.length();
                        break;
                    }
                }
            }

            // Step 4: Write the decoded data to the output file
            byte[] decodedBytes = new byte[decodedData.size()];
            for (int i = 0; i < decodedData.size(); i++) {
                decodedBytes[i] = decodedData.get(i);
            }
            Files.write(Path.of(outputFile), decodedBytes, StandardOpenOption.CREATE);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred during decompression", e);
        }
    }
}

// Main class that contains the GUI for Huffman coding
public class Main {
    public static class Huffman extends JFrame {
        private final HuffmanCoding huffmanCoding;

        public Huffman() {
            huffmanCoding = new HuffmanCoding();

            // Create GUI components
            JButton compressButton = new JButton("Compress");
            JButton decompressButton = new JButton("Decompress");

            // Add action listeners
            compressButton.addActionListener(e -> handleCompressButton());
            decompressButton.addActionListener(e -> handleDecompressButton());

            // Set up layout
            GroupLayout layout = new GroupLayout(getContentPane());
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addComponent(compressButton)
                    .addComponent(decompressButton));
            layout.setVerticalGroup(layout.createParallelGroup()
                    .addComponent(compressButton)
                    .addComponent(decompressButton));
            setLayout(layout);

            setTitle("Huffman Coding GUI");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pack();
            setLocationRelativeTo(null); // Center the frame
            setVisible(true);
        }

        // Handle the compress button action
        private void handleCompressButton() {
            JFileChooser fileChooser = new JFileChooser();

            // Set the default directory to the desktop
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File inputFile = fileChooser.getSelectedFile();

                // Choose where to save the compressed file
                fileChooser.setDialogTitle("Save Compressed File");
                result = fileChooser.showSaveDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File outputFile = fileChooser.getSelectedFile();
                    huffmanCoding.compress(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Compression complete!");
                }
            }
        }

        // Handle the decompress button action
        private void handleDecompressButton() {
            JFileChooser fileChooser = new JFileChooser();

            // Set the default directory to the desktop
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File inputFile = fileChooser.getSelectedFile();

                // Choose where to save the decompressed file
                fileChooser.setDialogTitle("Save Decompressed File");
                result = fileChooser.showSaveDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File outputFile = fileChooser.getSelectedFile();
                    huffmanCoding.decompress(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Decompression complete!");
                }
            }
        }

        // Main method to launch the GUI
        public static void main(String[] args) {
            SwingUtilities.invokeLater(Huffman::new);
        }
    }
}
