package searchengine.crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import org.htmlparser.util.ParserException;
import searchengine.indexer.InvertedIndex;

/**
 *
 */
public class Crawler {
    private int maxLinks;

    private InvertedIndex index;
    private Set<String> visited = new HashSet<String>();
    private LinkedList<String> frontier = new LinkedList<String>();

    public Crawler(String startingUrl, int maxLinks) {
        this.maxLinks = maxLinks;

        try {
            index = new InvertedIndex("inverted_index.db", "ht1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        frontier.addLast(startingUrl);
    }

    public void begin() {
        System.out.println("Beginning...");

        while (frontier.size() > 0 && visited.size() < maxLinks) {
            String current = frontier.removeFirst();
            if (visited.contains(current)) continue;

            visited.add(current);

            PageParser pageParser = new PageParser(current);

            Vector<String> words = null;
            try {
                words = pageParser.extractWords();
            } catch (ParserException e) {
                e.printStackTrace();
            }

            try {
                //Add all the links to the frontier that we haven't seen already
                pageParser
                        .extractLinks()
                        .stream()
                        .forEach(frontier::addLast);
            } catch (ParserException e) {
                e.printStackTrace();
            }

            //TODO for each word save in db
            System.out.println("Crawled page: " + current);
        }
    }
}
