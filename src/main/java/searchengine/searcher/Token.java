package searchengine.searcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing information about a word
 */
public class Token {
    private List<String> words = new ArrayList<>();
    private List<Integer> wordIds = new ArrayList<>();

    public void addWord(String word, int id) {
        words.add(word);
        wordIds.add(id);
    }

    public boolean isPhrase() {
        return words.size() > 1;
    }

    public List<String> getWords() {
        return words;
    }

    public List<Integer> getWordIds() {
        return wordIds;
    }

}
