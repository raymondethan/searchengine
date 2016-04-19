package searchengine.searcher;

import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 */
public class TokenizerTest extends TestCase {
    @Test
    public void testTokenizer() {
        Tokenizer t = new Tokenizer("the quick brown fox jumps over the lazy dog", null);
        assertEquals(9, t.getTokens().size());

        t = new Tokenizer("the \"quick brown\" fox \"jumps over the\" lazy \"dog\"", null);
        assertEquals(6, t.getTokens().size());

        assertEquals(2, t.getTokens().get(1).getWords().size());
        assertEquals(1, t.getTokens().get(5).getWords().size());
    }
}