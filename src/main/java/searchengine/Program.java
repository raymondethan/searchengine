package searchengine;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import searchengine.crawler.Crawler;
import searchengine.indexer.Index;
import searchengine.pagerank.PageRank;
import searchengine.searcher.SearchResult;
import searchengine.searcher.Searcher;

/**
 * The main entry point for the program
 */
public class Program {
    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler("http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm", 30);

        try {
            crawler.begin();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Have a look at what we got
        Index index = new Index("inverted_index");

        System.out.println("Running page rank");
        PageRank pageRanker = new PageRank(index);
        pageRanker.rankPages();
        pageRanker.printPageRanks();

//        PrintStream stream = new PrintStream(new FileOutputStream("spider_result.txt"));
//        index.printAll(stream);
//        index.printAll();

        Searcher search = new Searcher(index);
        List<SearchResult> result = search.search("search");
        result.forEach(searchResult -> System.out.println(searchResult.getLink()));
        System.out.println("finished search");
    }
}
