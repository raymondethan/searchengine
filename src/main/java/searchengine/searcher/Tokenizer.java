package searchengine.searcher;

import searchengine.indexer.Index;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Tokenizer {
    private String query;
    private List<Token> tokens = new ArrayList<>();

    public Tokenizer(String query, Index index) {
        tokenize();
    }

    private void tokenize() {
        boolean seenQuote = false;
    }




    public List<Token> getTokens() {
        return tokens;
    }
}
