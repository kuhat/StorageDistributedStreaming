package utils;

public class WordSplitter {
    // split word from sentence
    public static String[] split(String line) {
        // remove line break
        line = line.replaceAll("\n", " ");
        line = line.replaceAll("\r", " ");
        // remove 's/'ve/'re/'d/'ll/'m
        line = line.replaceAll("'s", " ").replaceAll("'ve", " ").
                replaceAll("'re", " ").replaceAll("'d", " ").replaceAll("'ll", " ").replaceAll("'m", " ");
        // remove punctuation
        line = line.replaceAll("[^a-zA-Z ]", " ").toLowerCase();
        // remove multiple spaces
        line = line.replaceAll(" +", " ");
        if (line.length() == 0) {
            return new String[0];
        }
        // remove space at the beginning and end
        if (line.charAt(0) == ' ') {
            line = line.substring(1);
            if (line.length() == 0) {
                return new String[0];
            }
        }
        if (line.charAt(line.length() - 1) == ' ') {
            line = line.substring(0, line.length() - 1);
            if (line.length() == 0) {
                return new String[0];
            }
        }
        return line.split(" ");
    }

    // Test
    public static void main(String[] args) {
        String line = "I'm sorry, but I've decided I'll be unable to attend the party. I'm just too busy with work right now and I'm afraid I won't be able to make it.";
        String[] words = split(line);
        System.out.println(words.length);
        for (String word : words) {
            System.out.println(word);
        }
    }
}
