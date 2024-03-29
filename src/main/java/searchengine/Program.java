package searchengine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import searchengine.crawler.Crawler;
import searchengine.helpers.ArgumentsManager;
import searchengine.indexer.Index;
import searchengine.pagerank.PageRank;
import searchengine.site.Server;

/**
 * The main entry point for the program
 */
public class Program {
    public static void main(String[] args) throws IOException {
        final Settings settings = new Settings();

        ArgumentsManager.get()
                .registerArgument("c", "crawl", "tells the program it should crawl", opts -> {
                    if (opts.size() > 0){
                    settings.crawlCount = Integer.parseInt(opts.get(0));
                    }
                    settings.shouldCrawl = true;
                })
                .registerArgument("u", "url", "tells the crawler it's starting url", opts -> {
                    settings.startUrl = opts.get(0);
                })
                .registerArgument("s", "server", "tells the program it should start as a server", opts -> {
                    settings.shouldCrawl = false;
                })
                .registerArgument("p", "port", "tells the program what port is should use", opts -> {
                    settings.port = Integer.parseInt(opts.get(0));
                })
                .parseArguments(args);

        if (settings.shouldCrawl){
            crawl(settings);
        } else {
            startServer(settings);
        }
    }

    private static void startServer(Settings settings) throws IOException {
        Server.startServer(settings.port);
    }

    private static void crawl(Settings settings) throws IOException {
        LocalDateTime start = LocalDateTime.now();
        Crawler crawler = new Crawler(settings.startUrl, settings.crawlCount);

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
        int maxIterations = settings.pageRankIterations;
        for (int i = 0; i < maxIterations; ++i) {
            pageRanker.rankPages();
        }

        PrintStream stream = new PrintStream(new FileOutputStream("spider_result.txt"));
        index.printAll(stream);
    }
}
