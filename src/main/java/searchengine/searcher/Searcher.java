package searchengine.searcher;

import com.sun.tools.javac.parser.Tokens;
import searchengine.indexer.Index;

import java.util.ArrayList;
import java.util.List;

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
