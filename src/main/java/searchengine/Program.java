package searchengine;

import searchengine.crawler.Crawler;

/**
 * The main entry point for the program
 */
public class Program {
    public static void main(String[] args) {
        Crawler crawler = new Crawler("http://www.cse.ust.hk/", 10);

        crawler.begin();
    }
}
