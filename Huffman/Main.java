import java.io.*;
import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

// Represents a node in the Huffman tree
class HuffmanNode implements Comparable<HuffmanNode> {
    char data;
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
    public HuffmanNode buildTree(HashMap<Character, Integer> frequencies) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        // Create leaf nodes for each character and add them to the priority queue
        for (char c : frequencies.keySet()) {
            HuffmanNode node = new HuffmanNode();
            node.data = c;
            node.frequency = frequencies.get(c);
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
    public void generateCodes(HuffmanNode root, String code, HashMap<Character, String> codes) {
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
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            // Step 1: Calculate character frequencies
            HashMap<Character, Integer> frequencies = new HashMap<>();
            int data;
            while ((data = fis.read()) != -1) {
                char character = (char) data;
                frequencies.put(character, frequencies.getOrDefault(character, 0) + 1);
            }

            // Step 2: Build the Huffman tree
            HuffmanTree huffmanTree = new HuffmanTree();
            HuffmanNode root = huffmanTree.buildTree(frequencies);

            // Step 3: Generate Huffman codes
            HashMap<Character, String> codes = new HashMap<>();
            huffmanTree.generateCodes(root, "", codes);

            // Step 4: Write Huffman codes to the output file
            oos.writeObject(codes);

            // Step 5: Write compressed data to the output file
            try {
                fis.getChannel().position(0); // Reset the position of the FileInputStream to the beginning
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An error occurred while resetting file position", e);
            }

            StringBuilder compressedData = new StringBuilder();
            while ((data = fis.read()) != -1) {
                char character = (char) data;
                compressedData.append(codes.get(character));
            }
            fos.write(toByteArray(compressedData.toString()));

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred during compression", e);
        }
    }

    // Decompress a file using Huffman coding
    public void decompress(String inputFile, String outputFile) {
        try (FileInputStream fis = new FileInputStream(inputFile);
             ObjectInputStream ois = new ObjectInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Step 1: Read Huffman codes from the input file
            @SuppressWarnings("unchecked")
            HashMap<Character, String> codes = (HashMap<Character, String>) ois.readObject();

            // Step 2: Read compressed data from the input file
            StringBuilder compressedData = new StringBuilder();
            int data;
            while ((data = fis.read()) != -1) {
                compressedData.append(byteToBinaryString((byte) data));
            }

            // Step 3: Decode the compressed data
            StringBuilder decodedData = new StringBuilder();
            int start = 0;
            for (int end = 1; end <= compressedData.length(); end++) {
                String substring = compressedData.substring(start, end);
                for (char c : codes.keySet()) {
                    if (codes.get(c).equals(substring)) {
                        decodedData.append(c);
                        start = end;
                        break;
                    }
                }
            }

            // Step 4: Write the decoded data to the output file
            fos.write(decodedData.toString().getBytes());

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "An error occurred during decompression", e);
        }
    }

    // Convert a binary string to a byte array
    private byte[] toByteArray(String binaryString) {
        int len = binaryString.length();
        byte[] data = new byte[(len + 7) / 8];
        for (int i = 0; i < len; i++) {
            if (binaryString.charAt(i) == '1') {
                data[i / 8] |= (byte) (128 >> (i % 8));
            }
        }
        return data;
    }

    // Convert a byte to a binary string
    private String byteToBinaryString(byte b) {
        StringBuilder binaryString = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            binaryString.append((b & (1 << i)) == 0 ? '0' : '1');
        }
        return binaryString.toString();
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