package searchengine.searcher;

import java.util.List;
import searchengine.indexer.Index;

/**
 *
 */
public class Searcher {

    private Index index;

    public Searcher(Index index) {
        this.index = index;
    }

    public void search(String query) {
        List<Token> tokens = new Tokenizer(query, index).getTokens();

    }
}
