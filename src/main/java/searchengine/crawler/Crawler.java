package searchengine.crawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public void begin() throws IOException, ParseException {
        System.out.println("Beginning...");

        DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");

        while (frontier.size() > 0 && visited.size() < maxLinks) {
            String current = frontier.removeFirst();
            if (visited.contains(current)) continue;

            PageParser pageParser = new PageParser(current);
            Date lastModified = pageParser.lastModified;
            String size = pageParser.size;

            Integer currDocId = index.getDocId(current);
            WebPage pageInIndex = index.getWebPage(currDocId);

            //If the doc exists in our index, and the webpage retrieved does not have a last modified field
            //Or the webpage's last modified field is after the last modification date of the document in the index
            //Then we do not want to add the page to the index and visit its children
            if (null != currDocId && null != pageInIndex && (null == lastModified || pageInIndex.lastModified.before(lastModified))) continue;
            //TODO: We are supposed to ignore urls if we have already visited them and the last modification date has not been updated
            //TODO: This means we do not add any urls to the frontier when we start from a root that has already been indexed - What should we do in this situation

            System.out.println("visiting " + current);
            System.out.println(visited.size());
            visited.add(current);

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
