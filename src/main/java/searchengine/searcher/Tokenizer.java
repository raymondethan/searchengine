package searchengine.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import searchengine.crawler.StopStem;
import searchengine.indexer.Index;

/**
 *
 */
public class Tokenizer {
    private StopStem stopStem = new StopStem("stopwords.txt");

    private String query;
    private List<Token> tokens = new ArrayList<>();

    private StringBuilder currentWord = new StringBuilder();
    private Token currentToken = new Token();
    private boolean inPhrase = false;

    private Index index;

    public Tokenizer(String query, Index index) {
        this.query = query;
        this.index = index;

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

        if (currentWord.length() > 0)
        {
            addWord();
            if (inPhrase)
                endToken();
        }
    }

    private void addWord() throws IOException {
        String word = currentWord.toString();

        if (word.isEmpty() || stopStem.isStopWord(word)){
            return;
        }

        word = stopStem.stem(word);
        //If the word isn't in the index, don't add it to the query vector
        Integer id = index.tryGetWordId(word);
        if (id == null) {
            return;
        }

        //Add the current word to the token
        currentToken.addWord(currentWord.toString(), id);
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

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Integer> allWords() {
        return tokens.stream().flatMap(t -> t.getWordIds().stream()).collect(Collectors.toList());
    }
}
