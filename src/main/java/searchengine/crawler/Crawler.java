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
    private Set<String> visited = new HashSet<>();
    private LinkedList<String> frontier = new LinkedList<>();
    private StopStem stopStem = new StopStem("stopwords.txt");

    public Crawler(String startingUrl, int maxLinks) {
        this.maxLinks = maxLinks;

        try {
            index = new InvertedIndex("inverted_index");
        } catch (IOException e) {
            e.printStackTrace();
        }

        frontier.addLast(startingUrl);
    }

    public void begin() throws IOException {
        System.out.println("Beginning...");

        while (frontier.size() > 0 && visited.size() < maxLinks) {
            String current = frontier.removeFirst();
            if (visited.contains(current)) continue;

            visited.add(current);

            PageParser pageParser = new PageParser(current);
            String lastModified = pageParser.lastModified;
            String size = pageParser.size;

            Vector<String> words = null;
            try {
                //Get the words from the page
                words = pageParser.extractWords();
            } catch (ParserException e) {
                e.printStackTrace();
                continue;
            }
            String title = "No Title";
            try {
                title = pageParser.extractTitle();
            } catch (ParserException e) {
                e.printStackTrace();
            }

            //Add the page to the docIndex
            //The method checks for duplicate links so we don't have to here
            int currDocId = index.getDocId(current);
            //System.out.println("Inserting doc id: " + currDocId);
            index.insertIntoDocIndex(currDocId, current, lastModified, size, title);

            try {
                //Add all the links to the frontier that we haven't seen already
                Vector<String> links = pageParser.extractLinks();

                //Save the child links
                index.addChildLinks(current, links);

                for (String link : links) {
                    //Add the links to the frontier
                    frontier.addLast(link);

                    //Remember the parent for this link
                    index.addParentLink(link, current);
                }

            } catch (ParserException e) {
                e.printStackTrace();
                continue;
            }

            for (int i = 0; i < words.size(); ++i) {
                if (!stopStem.isStopWord(words.get(i))) {
                    String stemmed = stopStem.stem(words.get(i));

                    //Stop getting those empty entries
                    if (stemmed.isEmpty()) continue;

                    index.addEntry(stemmed, current, i);
                }
            }

            System.out.println(current);
        }

        index.finalize();
    }
}
