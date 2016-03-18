package searchengine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import searchengine.crawler.Crawler;
import searchengine.indexer.InvertedIndex;

/**
 * The main entry point for the program
 */
public class Program {
    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler("http://www.cse.ust.hk/", 10);

        try {
            crawler.begin();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Have a look at what we got
        InvertedIndex index = new InvertedIndex("inverted_index");

        PrintStream stream = new PrintStream(new FileOutputStream("output.txt"));
        index.printAll(stream);
        index.printAll();
    }
}
