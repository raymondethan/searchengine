package searchengine.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import searchengine.crawler.StopStem;
import searchengine.indexer.Index;

/**
 * Parses tokens from words
 */
public class Tokenizer {
    private StopStem stopStem = new StopStem("stopwords.txt");

    private String query;
    private List<Token> tokens = new ArrayList<>();

    private StringBuilder currentWord = new StringBuilder();
    private Token currentToken = new Token();
    private boolean inPhrase = false;

    public Tokenizer(String query) {
        this.query = query;

        try {
            tokenize();
        }catch (IOException e) {
            System.err.println("Failed to load words :'(");
        }
    }

    private void tokenize() throws IOException {
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

        //If we didn't finish a word, add it
        if (currentWord.length() > 0)
        {
            addWord();
            if (inPhrase)
                endToken();
        }
    }

    private void addWord() throws IOException {
        String word = currentWord.toString();

        //Don't add empty words or stop words
        if (word.isEmpty() || stopStem.isStopWord(word)){
            return;
        }

        word = stopStem.stem(word);

        //Add the current word to the token
        currentToken.addWord(currentWord.toString());
        currentWord = new StringBuilder();

        //If we're not in a phrase, end the token
        if (!inPhrase) {
            endToken();
        }
    }

    private void endToken() {
        //Don't add tokens with no words
        if (currentToken.getWords().size() == 0) return;

        tokens.add(currentToken);

        currentToken = new Token();
    }

    /**
     * Gets a list of the tokens parsed by the tokenizer
     * @return The tokens
     */
    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * Gets a list of all the words from the query, whether they are phrases or not
     * @return The words
     */
    public List<String> allWords() {
        return tokens.stream().flatMap(t -> t.getWords().stream()).collect(Collectors.toList());
    }
}
