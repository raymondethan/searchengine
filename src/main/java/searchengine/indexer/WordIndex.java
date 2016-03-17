package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;

/**
 * An index of words
 */
public class WordIndex extends BasicIndex<String> {
    private static final String WORD_MAP_NAME = "wordindex";

    public WordIndex(RecordManager recman) throws IOException {
        super(WORD_MAP_NAME, recman);
    }
}
