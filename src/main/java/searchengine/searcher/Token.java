package searchengine.searcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing information about a word
 */
public class Token {
    private List<String> words = new ArrayList<>();
    private ArrayList<Integer> positions = new ArrayList<Integer>();

    public void addWord(String word, int query_word_index) {
        words.add(word);
        positions.add(query_word_index);
    }

    public boolean isPhrase() {
        return words.size() > 1;
    }

    public List<String> getWords() {
        return words;
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

}
