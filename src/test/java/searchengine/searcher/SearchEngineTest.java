package searchengine.searcher;

import junit.framework.TestCase;
import org.junit.Test;
import searchengine.Settings;
import searchengine.indexer.Index;
import searchengine.pagerank.PageRank;

import java.io.IOException;
import java.util.List;

/**
 * Created by ethanraymond on 5/2/16.
 */
public class SearchEngineTest extends TestCase {

    @Test
    public void SearchEngineTest() throws IOException {
        final Settings settings = new Settings();
        Index index = new Index("inverted_index");

        PageRank pageRanker = new PageRank(index);
        pageRanker.rankPages();

        Searcher search = new Searcher(index);

        //Test we only return a maximum of 50 results
        List<SearchResult> result = search.search("playing");
        assertEquals(settings.maxSearchResults, result.size());

        //Test the ranking
        List<SearchResult> result2 = search.search("\"Episode Title Search for \"Dinosaur Planet\"\"");
        assertEquals(result.get(0).getTitle(),"Dinosaur Planet (2003)");

        List<SearchResult> result3 = search.search("\"Episode Title Search for Dinosaur Planet\"");
        assertEquals(result.get(0).getTitle(),"Dinosaur Planet (2003)");
    }

}
