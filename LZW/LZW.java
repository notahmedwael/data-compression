import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZW {
    // LZW compression algorithm
    public static List<Integer> compress(String input) {
        // Create a dictionary to store character-to-code mappings.
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            // Initialize the dictionary with single characters (ASCII values 0 to 255).
            dictionary.put(String.valueOf((char) i), i);
        }

        // Initialize variables for encoding.
        int code = 256; // Starting code for new entries
        String p = "";  // Current string
        List<Integer> compressedData = new ArrayList<>();

        // Iterate through each character in the input string.
        for (char c : input.toCharArray()) {
            String pc = p + c; // Append the current character to the current string.

            // Check if the combination is already in the dictionary.
            if (dictionary.containsKey(pc)) {
                p = pc; // If yes, update the current string.
            } else {
                compressedData.add(dictionary.get(p)); // Add the code for the current string to the compressed data.
                dictionary.put(pc, code); // Add the new combination to the dictionary.
                code++; // Increment the code for future entries.
                p = String.valueOf(c); // Start a new current string with the current character.
            }
        }
        compressedData.add(dictionary.get(p)); // Add the code for the last current string.
        return compressedData; // Return the compressed data as a list of integers.
    }

    // LZW decompression algorithm
    public static String decompress(List<Integer> compressedData) {
        // Create a dictionary to store code-to-character mappings.
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            // Initialize the dictionary with single characters (ASCII values 0 to 255).
            dictionary.put(i, String.valueOf((char) i));
        }

        // Initialize variables for decoding.
        int code = 256; // Starting code for new entries
        StringBuilder decompressedText = new StringBuilder();
        int old = compressedData.get(0);
        String s = dictionary.get(old);
        decompressedText.append(s);

        // Iterate through the compressed data.
        for (int i = 1; i < compressedData.size(); i++) {
            int n = compressedData.get(i);

            // Check if the code is in the dictionary.
            if (!dictionary.containsKey(n)) {
                // If not, reconstruct the string using the previous code and its first character.
                s = dictionary.get(old) + s.charAt(0);
            } else {
                s = dictionary.get(n); // Get the string associated with the code.
            }

            decompressedText.append(s); // Append the string to the decompressed text.
            dictionary.put(code, dictionary.get(old) + s.charAt(0)); // Add the new entry to the dictionary.
            code++; // Increment the code for future entries.
            old = n; // Update the old code.
        }
        return decompressedText.toString(); // Return the decompressed text.
    }
}