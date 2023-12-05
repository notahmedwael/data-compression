import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

// JFrame subclass for Image Compression and Decompression GUI
class Quantization extends JFrame {

    // Constructor for the Quantization class
    public Quantization() {
        super("Vector Quantization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create buttons for compression and decompression
        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");

        // Attach action listeners to the buttons
        compressButton.addActionListener(e -> compressImage());
        decompressButton.addActionListener(e -> decompressImage());

        // Create a panel and add the buttons to it
        JPanel panel = new JPanel();
        panel.add(compressButton);
        panel.add(decompressButton);

        // Add the panel to the frame
        add(panel);

        // Pack the components and center the frame on the screen
        pack();
        setLocationRelativeTo(null); // Center the frame on the screen
    }

    // Method to choose the save path for compressed/decompressed images
    private String chooseSavePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Set the default directory to the desktop
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        fileChooser.setCurrentDirectory(new File(desktopPath));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    // Method to handle image compression
    private void compressImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));

        // Set the default directory to the desktop
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        fileChooser.setCurrentDirectory(new File(desktopPath));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String inputImagePath = selectedFile.getAbsolutePath();

            String outputCompressedImagePath = chooseSavePath();
            if (outputCompressedImagePath != null) {
                // Set parameters for compression
                int vectorHeight = 8;
                int vectorWidth = 8;
                int nVectors = 64;

                try {
                    // Read the original image
                    Compress.originalImage = Compress.readImage(inputImagePath);

                    // Set compression parameters
                    Compress.vectorHeight = vectorHeight;
                    Compress.vectorWidth = vectorWidth;
                    Compress.nVectors = nVectors;

                    // Perform compression
                    Compress.compression();

                    // Write the compressed data to a binary file
                    Compress.writeCompressedToFile(outputCompressedImagePath);

                    // Display compression completion message
                    JOptionPane.showMessageDialog(this, "Compression complete!");
                } catch (IOException e) {
                    // Handle compression exception and show an error message
                    System.out.println("Exception: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "Compression failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Method to handle image decompression
    private void decompressImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Binary Files", "bin"));

        // Set the default directory to the desktop
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        fileChooser.setCurrentDirectory(new File(desktopPath));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String inputCompressedImagePath = selectedFile.getAbsolutePath();

            String outputDecompressedImagePath = chooseSavePath();
            if (outputDecompressedImagePath != null) {
                try {
                    // Read compressed data from the binary file
                    Compress.readCompressedFromFile(inputCompressedImagePath);

                    // Perform decompression
                    Compress.decompression();

                    // Write the decompressed image
                    Compress.writeImage(Compress.reconstructedImage, outputDecompressedImagePath);

                    // Display decompression completion message
                    JOptionPane.showMessageDialog(this, "Decompression complete!");
                } catch (IOException e) {
                    // Handle decompression exception and show an error message
                    System.out.println("Exception: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "Decompression failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Main method to create and display the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Quantization().setVisible(true));
    }
}