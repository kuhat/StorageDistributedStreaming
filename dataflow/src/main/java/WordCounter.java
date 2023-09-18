import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;

public class WordCounter {
    private static String filePath;
    public static String[] split(String line) {
        // remove 's/'ve/'re/'d/'ll/'m
        line = line.replaceAll("'s", "").replaceAll("'ve", "").replaceAll("'re", "").replaceAll("'d", "").replaceAll("'ll", "").replaceAll("'m", "");
        // remove punctuation
        line = line.replaceAll("[^a-zA-Z ]", " ").toLowerCase();
        // remove multiple spaces
        line = line.replaceAll(" +", " ");
        // split
        return line.split(" ");
    }

    public static HashMap<String, Integer> countWord() throws FileNotFoundException {
        HashMap<String, Integer> counter = new HashMap<>();
        Scanner in = new Scanner(new File(filePath));
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] words = split(line);
            for (String word : words) {
                counter.merge(word, 1, (i, j) -> i + j);
            }
        }
        return counter;
    }

    public static void main(String[] args) {
        filePath = "TheCompleteWorksOfWilliamShakespearebyWilliamShakespeare.txt";
        try {
            HashMap<String, Integer> result = countWord();
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(new File("count.txt"), result);
            }catch (IOException e) {

            }
        }catch (FileNotFoundException e) {
            System.out.println("File Not Found!");
        }
    }
}
