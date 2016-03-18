package searchengine.indexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jdbm.RecordManager;

/**
 *
 */
public class DocumentWordCounts extends BasicIndex<Map<String, Integer>> {
    private static final String WORD_MAP_NAME = "document_word_counts";

    public DocumentWordCounts(RecordManager recman) throws IOException {
        super(WORD_MAP_NAME, recman);
    }

    public void addWord(int document, String word) throws IOException {
        Map<String, Integer> wordMap = get(document);

        if (wordMap == null) {
            wordMap = new HashMap<>();
        }

        if (!wordMap.containsKey(word)) {
            wordMap.put(word, 0);
        }

        //Increment the number of times the word has appeared
        wordMap.put(word, wordMap.get(word) + 1);
    }
}
