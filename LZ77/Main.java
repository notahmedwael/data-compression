import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("LZ-77 Compression/Decompression Program");
        System.out.println("======================================");

        System.out.println("1) Compress");
        System.out.println("2) Decompress");
        int operationChoice = in.nextInt();

        in.nextLine(); // Consume newline character

        if (operationChoice == 1) {
            System.out.print("Enter the input string to compress: ");
            String input = in.nextLine();
            int windowSize = getInputWindowSize();

            System.out.println("1) Write to a File");
            System.out.println("2) Write to Console");
            int writeChoice = in.nextInt();

            if (writeChoice == 1) {
                // Construct the file path to the user's desktop
                String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
                String filePath = desktopPath + File.separator + "compressedFile.txt";

                ArrayList<LZ77.Tag> compressed = LZ77.compress(input, windowSize);
                if (writeTagsToFile(filePath, compressed)) {
                    System.out.println("File saved on the desktop at: " + filePath);
                } else {
                    System.out.println("File not saved successfully.");
                }
            } else if (writeChoice == 2) {
                ArrayList<LZ77.Tag> compressed = LZ77.compress(input, windowSize);
                System.out.println("Compressed: " + tagsToString(compressed));
            } else {
                System.out.println("Invalid choice");
            }
        } else if (operationChoice == 2) {
            System.out.print("Enter the number of tags: ");
            int numTags = in.nextInt();
            in.nextLine(); // Consume newline character

            ArrayList<LZ77.Tag> tags = new ArrayList<>();
            for (int i = 0; i < numTags; i++) {
                System.out.print("Enter tag (Offset Length NextCharacter, or 'null' for no character): ");
                String tagInput = in.nextLine();
                String[] tagParts = tagInput.split(" ");
                if (tagParts.length == 3) {
                    int offset = Integer.parseInt(tagParts[0]);
                    int length = Integer.parseInt(tagParts[1]);
                    char nextChar = tagParts[2].equals("null") ? '\0' : tagParts[2].charAt(0);
                    LZ77.Tag tag = new LZ77.Tag(offset, length, nextChar);
                    tags.add(tag);
                }
            }

            System.out.println("1) Write to a File");
            System.out.println("2) Write to Console");
            int writeChoice = in.nextInt();

            if (writeChoice == 1) {
                // Construct the file path to the user's desktop
                String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
                String outputFilePath = desktopPath + File.separator + "decompressedFile.txt";

                String decompressed = LZ77.decompress(tags);
                if (writeStringToFile(outputFilePath, decompressed)) {
                    System.out.println("File saved on the desktop at: " + outputFilePath);
                } else {
                    System.out.println("File not saved successfully.");
                }
            } else if (writeChoice == 2) {
                String decompressed = LZ77.decompress(tags);
                System.out.println("Decompressed: " + decompressed);
            } else {
                System.out.println("Invalid choice");
            }
        } else {
            System.out.println("Invalid operation choice");
        }
    }

    private static int getInputWindowSize() {
        System.out.print("Enter the window size: ");
        return in.nextInt();
    }

    private static boolean writeTagsToFile(String filePath, ArrayList<LZ77.Tag> tags) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (LZ77.Tag tag : tags) {
                writer.write(tag.toString());
                writer.newLine(); // Add a newline between tags
            }
            writer.close();
            return true; // File saved successfully
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
            return false; // File not saved successfully
        }
    }

    private static String tagsToString(ArrayList<LZ77.Tag> tags) {
        StringBuilder builder = new StringBuilder();
        for (LZ77.Tag tag : tags) {
            builder.append(tag.toString()).append(" ");
        }
        return builder.toString();
    }

    private static boolean writeStringToFile(String filePath, String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(data);
            writer.close();
            return true; // File saved successfully
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
            return false; // File not saved successfully
        }
    }
}
