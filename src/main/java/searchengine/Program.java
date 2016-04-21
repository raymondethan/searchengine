package searchengine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;

import searchengine.crawler.Crawler;
import searchengine.indexer.Index;
import searchengine.indexer.InvertedIndex;
import searchengine.searcher.Searcher;
import searchengine.searcher.Token;
import searchengine.searcher.Tokenizer;

/**
 * The main entry point for the program
 */
public class Program {
    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler("http://www.cse.ust.hk/", 30);

        try {
            crawler.begin();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Have a look at what we got
        Index index = new Index("inverted_index");

//        PrintStream stream = new PrintStream(new FileOutputStream("spider_result.txt"));
//        index.printAll(stream);
//        index.printAll();

        Searcher search = new Searcher(index);
        search.search("Brahim \"introduction to the quality assurance\"");
        System.out.println("finished search");
    }
}
