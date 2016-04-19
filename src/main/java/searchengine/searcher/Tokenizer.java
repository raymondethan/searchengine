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

    private StringBuilder currentWord = new StringBuilder();
    private Token currentToken = new Token();
    private boolean inPhrase = false;

    private Index index;

    public Tokenizer(String query, Index index) {
        this.query = query;
        this.index = index;

        tokenize();
    }

    private void tokenize() {
        for (int i = 0; i < query.length(); ++i) {
            if (query.charAt(i) == '"') {
                if (inPhrase) {
                    addWord();
                    endToken();
                    inPhrase = false;
                } else {
                    inPhrase = true;
                }
            }
            else if (query.charAt(i) == ' ') {
                addWord();
            }else  {
                currentWord.append(query.charAt(i));
            }
        }

        if (currentWord.length() > 0)
        {
            addWord();
            if (inPhrase)
                endToken();
        }
    }

    private void addWord() {
        if (currentWord.toString().isEmpty()) return;

        //Add the current word to the token
        currentToken.addWord(currentWord.toString(), 0);
        currentWord = new StringBuilder();

        //If we're not in a phrase, end the token
        if (!inPhrase) {
            endToken();
        }
    }

    private void endToken() {
        tokens.add(currentToken);

        currentToken = new Token();
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
