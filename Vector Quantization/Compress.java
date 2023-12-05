import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;

public class Compress {
    // Class variables to store image and compression-related data
    public static int height, width, vectorHeight, vectorWidth, nVectors, compressedHeight, compressedWidth;
    public static float[][] originalImage;
    public static String[][] compressedImage;
    public static float[][] reconstructedImage;
    public static ArrayList<float[][]> originalBlocks = new ArrayList<>();
    public static ArrayList<float[][]> blocks = new ArrayList<>();
    public static Map<float[][], ArrayList<float[][]>> nearestVectors = new HashMap<>();
    public static TreeMap<String, float[][]> codeBook = new TreeMap<>();

    // Method to read an image from a file
    public static float[][] readImage(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedImage image;
        image = ImageIO.read(file);
        width = image.getWidth();
        height = image.getHeight();
        float[][] pixels = new float[height][width];
        int rgb;
        // Iterate through each pixel in the image
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Extract RGB values from the pixel
                rgb = image.getRGB(i, j);
                int red = (rgb & 0x00ff0000) >> 16;
                int green = (rgb & 0x0000ff00) >> 8;
                int blue = (rgb & 0x000000ff);
                // Store the maximum color intensity in the pixel array
                pixels[j][i] = Math.max(Math.max(red, green), blue);
            }
        }
        return pixels;
    }

    // Method to write an image to a file
    public static void writeImage(float[][] pixels, String filePath) throws IOException {
        int h = pixels.length;
        int w = pixels[0].length;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        // Iterate through each pixel in the pixel array
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // Create a pixel value by repeating the intensity value for RGB
                int pixelValue = (int) pixels[i][j] << 16 | (int) pixels[i][j] << 8 | (int) pixels[i][j];
                // Set the pixel value in the image
                image.setRGB(j, i, pixelValue);
            }
        }

        // Write the BufferedImage to the specified file path with "jpg" format
        File outputFile = new File(filePath);
        ImageIO.write(image, "jpg", outputFile);
    }

    // Method to divide the original image into non-overlapping blocks
    public static void divideImage() {
        // Get the dimensions of the original image
        height = originalImage.length;
        width = originalImage[0].length;

        // Calculate the remaining height and width after division
        int h = height % vectorHeight;
        int w = width % vectorWidth;

        // Adjust the dimensions to make them divisible by vectorHeight and vectorWidth
        if ((h != 0) || (w != 0)) {
            height -= h;
            width -= w;
            float[][] newImage = new float[height][width];
            for (int i = 0; i < height; i++)
                if (width >= 0) System.arraycopy(originalImage[i], 0, newImage[i], 0, width);
            originalImage = newImage;
        }

        // Iterate through the image, extracting vectors of size vectorHeight x vectorWidth
        for (int i = 0; i < height; i += vectorHeight) {
            for (int j = 0; j < width; j += vectorWidth) {
                float[][] vector = new float[vectorHeight][vectorWidth];
                h = i;
                for (int x = 0; x < vectorHeight; x++, h++) {
                    w = j;
                    for (int y = 0; y < vectorWidth; y++, w++) {
                        vector[x][y] = originalImage[h][w];
                    }
                }
                // Add the extracted vector to the list of original blocks
                originalBlocks.add(vector);
            }
        }
    }

    // Method to calculate the average vector from a list of vectors
    public static float[][] getAverage(ArrayList<float[][]> vectors) {
        float[][] average = new float[vectorHeight][vectorWidth];
        for (int i = 0; i < vectorHeight; i++)
            for (int j = 0; j < vectorWidth; j++) {
                average[i][j] = 0;
                // Sum up the values for each position in the vectors
                for (float[][] vec : vectors) average[i][j] += vec[i][j];
                // Calculate the average by dividing by the number of vectors
                average[i][j] /= (float) vectors.size();
            }
        return average;
    }

    // Method to split each block into two vectors by rounding towards floor and ceiling
    public static void split() {
        int size = blocks.size();
        for (int i = 0; i < size; i++) {
            float[][] floor = new float[vectorHeight][vectorWidth];
            float[][] ceil = new float[vectorHeight][vectorWidth];
            for (int x = 0; x < vectorHeight; x++) {
                for (int y = 0; y < vectorWidth; y++) {
                    float f = (float) Math.floor(blocks.get(i)[x][y]);
                    float c = (float) Math.ceil(blocks.get(i)[x][y]);
                    if (f == c) {
                        f--;
                        c++;
                    }
                    floor[x][y] = f;
                    ceil[x][y] = c;
                }
            }
            // Add the two split vectors to the list of blocks
            blocks.add(floor);
            blocks.add(ceil);
        }
        // Clear the original blocks to make room for the split blocks
        while (size != 0) {
            blocks.remove(0);
            size--;
        }
    }

    // Method to find the index of the block with the nearest vector to a given original block
    public static void getNearestVectors() {
        for (float[][] originalBlock : originalBlocks) {
            // Get the index of the block with the nearest vector
            int index = getIndex(originalBlock);
            // Add the original block to the list associated with the nearest vector block
            if (nearestVectors.containsKey(blocks.get(index)))
                nearestVectors.get(blocks.get(index)).add(originalBlock);
            else {
                ArrayList<float[][]> temp = new ArrayList<>();
                temp.add(originalBlock);
                nearestVectors.put(blocks.get(index), temp);
            }
        }
    }

    // Method to calculate the index of the block with the nearest vector to a given original block
    private static int getIndex(float[][] originalBlock) {
        ArrayList<Double> distances = new ArrayList<>();
        // Calculate the Euclidean distance between the original block and each block vector
        for (float[][] block : blocks) {
            double distance = 0;
            for (int x = 0; x < vectorHeight; x++) {
                for (int y = 0; y < vectorWidth; y++) {
                    distance += Math.pow(((double) block[x][y] - (double) originalBlock[x][y]), 2);
                }
            }
            distances.add(distance);
        }
        // Find the index of the block with the minimum distance
        double min = Collections.min(distances);
        return distances.indexOf(min);
    }

    public static void compression() {
        // Divide the image into blocks
        divideImage();

        // Add the average of the original blocks to the blocks list
        blocks.add(getAverage(originalBlocks));

        // Initialize with the nearest vectors to the average block
        nearestVectors.put(blocks.get(0), originalBlocks);

        // Iteratively split and update the nearest vectors until the desired number of vectors is reached
        while (blocks.size() < nVectors) {
            split();
            nearestVectors.clear();
            getNearestVectors();
            blocks.clear();
            for (float[][] vec : nearestVectors.keySet()) {
                float[][] avg = getAverage(nearestVectors.get(vec));
                blocks.add(avg);
            }
        }

        // Refine the blocks until convergence
        while (true) {
            int counter = getCounter();
            if (counter == blocks.size())
                break;

            nearestVectors.clear();
            getNearestVectors();
            blocks.clear();
            for (float[][] vec : nearestVectors.keySet())
                blocks.add(getAverage(nearestVectors.get(vec)));
        }

        // Encode the blocks and generate the codebook
        encode();
    }

    private static int getCounter() {
        // Count the number of blocks that remain unchanged
        int counter = 0;
        for (float[][] block : blocks) {
            float[][] vec;
            vec = block;
            for (float[][] vector : nearestVectors.keySet()) {
                int c = 0;
                for (int x = 0; x < vectorHeight; x++) {
                    for (int y = 0; y < vectorWidth; y++) {
                        if (vector[x][y] == vec[x][y])
                            c++;
                    }
                }
                if (c == (vectorHeight * vectorWidth))
                    counter++;
            }
        }
        return counter;
    }

    public static void encode() {
        // Calculate the number of bits needed to represent the block indices
        int nBits = (int) Math.ceil((Math.log(nVectors) / Math.log(2)));

        // Create binary codes for each block index and build the codebook
        for (int i = 0; i < blocks.size(); i++) {
            StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
            while (binary.length() != nBits)
                binary.insert(0, "0");
            codeBook.put(binary.toString(), blocks.get(i));
        }

        // Calculate compressed image dimensions
        compressedHeight = height / vectorHeight;
        compressedWidth = width / vectorWidth;
        compressedImage = new String[compressedHeight][compressedWidth];

        // Map original blocks to their corresponding codes in the compressed image
        int index = 0;
        for (int i = 0; i < compressedHeight; i++) {
            for (int j = 0; j < compressedWidth; j++) {
                for (float[][] vec : nearestVectors.keySet()) {
                    if (nearestVectors.get(vec).contains(originalBlocks.get(index))) {
                        compressedImage[i][j] = getCode(vec);
                    }
                }
                index++;
            }
        }
    }

    public static String getCode(float[][] vector) {
        // Find the code for a given vector in the codebook
        for (String code : codeBook.keySet()) {
            int counter = 0;
            for (int i = 0; i < vectorHeight; i++) {
                for (int j = 0; j < vectorWidth; j++) {
                    if (vector[i][j] == codeBook.get(code)[i][j])
                        counter++;
                }
            }
            if (counter == (vectorHeight * vectorWidth))
                return code;
        }
        return null;
    }

    public static void decompression() {
        // Determine vector dimensions from the first code in the codebook
        for (String code : codeBook.keySet()) {
            vectorHeight = codeBook.get(code).length;
            vectorWidth = codeBook.get(code)[0].length;
            break;
        }

        // Calculate the dimensions of the reconstructed image
        height = compressedHeight * vectorHeight;
        width = compressedWidth * vectorWidth;
        reconstructedImage = new float[height][width];

        int h, w;
        // Reconstruct the image by mapping codes to vectors in the codebook
        for (int i = 0, a = 0; i < height; i += vectorHeight, a++) {
            for (int j = 0, b = 0; j < width; j += vectorWidth, b++) {
                h = i;
                float[][] vector = codeBook.get(compressedImage[a][b]);
                for (int x = 0; x < vectorHeight; x++, h++) {
                    w = j;
                    for (int y = 0; y < vectorWidth; y++, w++) {
                        reconstructedImage[h][w] = vector[x][y];
                    }
                }
            }
        }
    }

    public static void writeCompressedToFile(String fileName) throws IOException {
        // Write compressed data to a binary file
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName))) {
            dos.writeInt(compressedHeight);
            dos.writeInt(compressedWidth);

            for (int i = 0; i < compressedHeight; i++) {
                for (int j = 0; j < compressedWidth; j++) {
                    dos.writeUTF(compressedImage[i][j]);
                }
            }

            dos.writeInt(vectorHeight);
            dos.writeInt(vectorWidth);

            for (String code : codeBook.keySet()) {
                dos.writeUTF(code);
                float[][] vector = codeBook.get(code);
                for (int i = 0; i < vectorHeight; i++) {
                    for (int j = 0; j < vectorWidth; j++) {
                        dos.writeFloat(vector[i][j]);
                    }
                }
            }
        }
    }

    public static void readCompressedFromFile(String fileName) throws IOException {
        // Read compressed data from a binary file
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            compressedHeight = dis.readInt();
            compressedWidth = dis.readInt();

            compressedImage = new String[compressedHeight][compressedWidth];
            for (int i = 0; i < compressedHeight; i++) {
                for (int j = 0; j < compressedWidth; j++) {
                    compressedImage[i][j] = dis.readUTF();
                }
            }

            vectorHeight = dis.readInt();
            vectorWidth = dis.readInt();

            codeBook.clear();
            while (dis.available() > 0) {
                String code = dis.readUTF();
                float[][] vector = new float[vectorHeight][vectorWidth];
                for (int i = 0; i < vectorHeight; i++) {
                    for (int j = 0; j < vectorWidth; j++) {
                        vector[i][j] = dis.readFloat();
                    }
                }
                codeBook.put(code, vector);
            }
        }
    }

}