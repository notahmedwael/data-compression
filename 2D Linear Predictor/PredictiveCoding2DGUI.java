import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredictiveCoding2DGUI extends JFrame {

    private static final Logger logger = Logger.getLogger(PredictiveCoding2DGUI.class.getName());

    private JButton compressButton;
    private JButton decompressButton;

    public PredictiveCoding2DGUI() {
        super("2D Linear Predictor");
        initializeComponents();
        setupLayout();
        setupListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        compressButton = new JButton("Compress Image");
        decompressButton = new JButton("Decompress Image");
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Padding

        add(compressButton, gbc);

        gbc.gridy = 1;
        add(decompressButton, gbc);
    }

    private void setupListeners() {
        compressButton.addActionListener(e -> compressImage());
        decompressButton.addActionListener(e -> decompressImage());
    }

    private void compressImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Image to Compress");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String inputImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                int[][] compressedData = PredictiveCoding2D.compressImage(inputImagePath);

                fileChooser.setDialogTitle("Choose Location to Save Compressed Data");
                result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String outputPath = fileChooser.getSelectedFile().getAbsolutePath();
                    PredictiveCoding2D.saveCompressedData(compressedData, outputPath);
                    JOptionPane.showMessageDialog(this, "Image compressed and data saved successfully!");
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error compressing image.", ex);
                JOptionPane.showMessageDialog(this, "Error compressing image.");
            }
        }
    }

    private void decompressImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Compressed Data File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary files", "bin");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String inputPath = fileChooser.getSelectedFile().getAbsolutePath();
            int[][] compressedData = PredictiveCoding2D.loadCompressedData(inputPath);

            if (compressedData != null) {
                BufferedImage decompressedImage = PredictiveCoding2D.decompressImage(compressedData);

                fileChooser.setDialogTitle("Choose Location to Save Decompressed Image");
                result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String outputPath = fileChooser.getSelectedFile().getAbsolutePath();
                    try {
                        ImageIO.write(decompressedImage, "png", new File(outputPath));
                        JOptionPane.showMessageDialog(this, "Image decompressed and saved successfully!");
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error saving decompressed image.", ex);
                        JOptionPane.showMessageDialog(this, "Error saving decompressed image.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading compressed data.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PredictiveCoding2DGUI::new);
    }
}