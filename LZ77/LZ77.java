import java.util.ArrayList;

public class LZ77 {

    // Tag class to represent data
    public static class Tag {
        int offset;
        int length;
        Character nextChar;

        public Tag(int offset, int length, Character nextChar) {
            this.offset = offset; // how many indices to come back
            this.length = length; //length of the elements to take from the string
            this.nextChar = nextChar; // next character to be put
        }

        // Overriding the toString method to return the tags
        @Override
        public String toString() {
            String nextCharString = (nextChar != null) ? "'" + nextChar + "'" : "null";
            return "Tag(" + offset + ", " + length + ", " + nextCharString + ")";
        }
    }

    // Compress text into tags
    public static ArrayList<Tag> compress(String input, int windowSize) {
        ArrayList<Tag> tags = new ArrayList<>();
        int bufferSize = 5;
        int currentIndex = 0;

        while (currentIndex < input.length()) {
            int bestMatchOffset = -1;
            int bestMatchLength = 0;

            // Search for the best match in the search buffer
            for (int i = Math.max(0, currentIndex - windowSize); i < currentIndex; i++) {
                int j = i; // Position of sliding window
                int k = currentIndex; // Position of the lookahead buffer
                int length = 0;

                // Compare characters in search buffer and lookahead buffer
                while (k < input.length() && input.charAt(j) == input.charAt(k) && length < bufferSize) {
                    j++;
                    k++;
                    length++;
                }

                // Update if a better match is found
                if (length > bestMatchLength) {
                    bestMatchLength = length;
                    bestMatchOffset = currentIndex - i;
                }
            }

            if (bestMatchLength > 0) {
                // Create a tag when we find a match
                Character nextChar = currentIndex + bestMatchLength < input.length() ?
                        input.charAt(currentIndex + bestMatchLength) : null;
                Tag tag = new Tag(bestMatchOffset, bestMatchLength, nextChar);
                tags.add(tag);
                currentIndex += bestMatchLength + 1;  // Move past the matched part
            } else {
                // No match, create a tag with length 0
                Tag tag = new Tag(0, 0, input.charAt(currentIndex));
                tags.add(tag);
                currentIndex++;
            }
        }

        return tags;
    }

    // Decompress a list of tags to the original string
    public static String decompress(ArrayList<Tag> tags) {
        StringBuilder decompressed = new StringBuilder();

        for (Tag tag : tags) {
            if (tag.length > 0) {
                int startIndex = decompressed.length() - tag.offset;
                int endIndex = startIndex + tag.length;

                // Copy characters from the search buffer
                for (int i = startIndex; i < endIndex; i++) {
                    decompressed.append(decompressed.charAt(i));
                }
            }

            // Append the next character
            if (tag.nextChar != null) {
                decompressed.append(tag.nextChar);
            }
        }

        return decompressed.toString();
    }
}
