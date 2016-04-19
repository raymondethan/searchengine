package searchengine.searcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing information about a word
 */
public class Token {
    public List<String> words = new ArrayList<>();

    public List<String> getWords() {
        return words;
    }
}
