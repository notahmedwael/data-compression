import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        String input = "ABCEEFR";
        int windowSize = 12;

        ArrayList<LZ77.Tag> compressed = LZ77.compress(input, windowSize);

        String decompressed = LZ77.decompress(compressed);

        System.out.println("Original: " + input);
        System.out.println("Compressed: " + compressed);
        System.out.println("Decompressed: " + decompressed);
    }
}