package searchengine.crawler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import org.htmlparser.util.ParserException;

/**
 *
 */
public class Crawler {
    private Set<String> visited = new HashSet<String>();
    private LinkedList<String> frontier = new LinkedList<String>();

    public Crawler(String startingUrl) {
        frontier.addLast(startingUrl);
    }

    public void begin() {
        String current;
        while ((current = frontier.removeFirst()) != null) {
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
                        .filter(visited::contains)
                        .forEach(frontier::addLast);
            } catch (ParserException e) {
                e.printStackTrace();
            }

            //TODO for each word save in db
        }
    }
}
