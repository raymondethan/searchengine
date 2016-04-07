package searchengine.crawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.htmlparser.util.ParserException;
import searchengine.indexer.Index;
import searchengine.indexer.InvertedIndex;

/**
 *
 */

    //count characters if size field null
public class Crawler {
    private int maxLinks;

    private Index index;
    private Set<String> visited = new HashSet<>();
    private LinkedList<String> frontier = new LinkedList<>();
    private StopStem stopStem = new StopStem("stopwords.txt");

    public Crawler(String startingUrl, int maxLinks) {
        this.maxLinks = maxLinks;

        try {
            index = new Index("inverted_index");
        } catch (IOException e) {
            e.printStackTrace();
        }

        frontier.addLast(startingUrl);
    }

    public void begin() throws IOException, ParseException {
        System.out.println("Beginning...");

        //Store the last modified section of the response as a Date so we can easily make date comparisons
        DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");

        while (frontier.size() > 0 && visited.size() < maxLinks) {
            String current = frontier.removeFirst();
            if (visited.contains(current)) continue;

            //GET RID OF THIS
            Long startTime = System.currentTimeMillis();

            PageParser pageParser = new PageParser(current);
            Date lastModified = pageParser.lastModified;
            String size = pageParser.size;

            System.out.println("Parser initialization time: " + (System.currentTimeMillis() - startTime) );

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

            String title = "No Title";
            try {
                Long titlestart = System.currentTimeMillis();
                title = pageParser.extractTitle();
                String [] titleArray = title.split(" ");
                for (int i = 0; i < titleArray.length; ++i) {
                    if (!stopStem.isStopWord(titleArray[i])) {
                        String stemmed = stopStem.stem(titleArray[i]);

                        //Stop getting those empty entries
                        if (stemmed.isEmpty()) continue;

                        index.addTitleEntry(stemmed, current, i);
                    }
                }
                System.out.println("Extract title time: " + (System.currentTimeMillis() - titlestart));
            } catch (ParserException e) {
                e.printStackTrace();
            }

            Vector<String> words = null;
            try {
                //Get the words from the page
                Long sbstart = System.currentTimeMillis();
                words = pageParser.extractWords();
                System.out.println(words.size());
                System.out.println("String Bean time: " + (System.currentTimeMillis() - sbstart) );
            } catch (ParserException e) {
                e.printStackTrace();
                continue;
            }

            //Add the page to the docIndex
            index.insertIntoDocIndex(currDocId, current, lastModified, size, title);

            try {
                //Add all the links to the frontier that we haven't seen already
                Long linkstart = System.currentTimeMillis();
                Vector<String> links = pageParser.extractLinks();

                System.out.println("Extract Links time: " + (System.currentTimeMillis() - linkstart) );
                //TODO: check that the links are getting stored correctly - there is a page that only 4 child links are getting printed out for, which does not match online
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

            System.out.println("Total execution time: " + (System.currentTimeMillis() - startTime) );
            System.out.println(current);
        }

        index.finalize();
    }
}
