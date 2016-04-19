package searchengine.searcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing information about a word
 */
public class Token {
    private List<String> words = new ArrayList<>();

    public void addWord(String word) {
        words.add(word);
    }

    public boolean isPhrase() {
        return words.size() > 1;
    }

    public List<String> getWords() {
        return words;
    }
}
