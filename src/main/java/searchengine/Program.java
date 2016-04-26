package searchengine;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
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
        LocalDateTime start = LocalDateTime.now();
        Crawler crawler = new Crawler("http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm", 300);

        try {
            crawler.begin();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.printf("Crawling time: %s%n", Duration.between(start, LocalDateTime.now()));

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
        List<SearchResult> result = search.search("hkust \"pg admission\"");
        result.forEach(searchResult -> System.out.println(searchResult.getLink()));
        System.out.println("finished search");
    }
}
