package searchengine.crawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import org.htmlparser.util.ParserException;
import searchengine.Settings;
import searchengine.indexer.Index;

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

    private long totalDuration = 0;
    private long maxConnection = -1;
    private long minConnection = 100000;
    private long totalDurationT = 0;
    private long maxConnectionT = -1;
    private long minConnectionT = 100000;
    private long totalDurationW = 0;
    private long maxConnectionW = -1;
    private long minConnectionW = 100000;
    private long totalDurationL = 0;
    private long maxConnectionL = -1;
    private long minConnectionL = 100000;
    Boolean debug = true;

    public Crawler(String startingUrl, int maxLinks) {
        this.maxLinks = maxLinks;

        try {
            index = new Index("inverted_index");
        } catch (IOException e) {
            e.printStackTrace();
        }

        frontier.addLast(startingUrl);
    }

    public static void main(String[] args) throws IOException, ParseException {
        Settings settings = new Settings();
        Crawler crawler = new Crawler(settings.startUrl, settings.crawlCount);
        crawler.begin();
    }

    public void begin() throws IOException, ParseException {
        System.out.print("Crawling");

        while (frontier.size() > 0 && visited.size() < maxLinks) {
            //Print to console to indicate crawling of pages
            System.out.print(".");
            String current = frontier.removeFirst();
            if (visited.contains(current)) continue;

            LocalDateTime start = LocalDateTime.now();
            PageParser pageParser = new PageParser(current);
            long d = Duration.between(start, LocalDateTime.now()).getSeconds();
            totalDuration += d;
            if (d < minConnection) {
                minConnection = d;
            }
            else if (d > maxConnection) {
                maxConnection = d;
            }

            Date lastModified = pageParser.lastModified;
            Integer size = pageParser.size;

            Integer currDocId = index.getDocId(current);
            WebPage pageInIndex = index.getWebPage(currDocId);

            //If the doc exists in our index, and the webpage retrieved does not have a last modified field
            //Or the webpage's last modified field is after the last modification date of the document in the index
            //Then we do not want to add the page to the index and visit its children
            if (null != pageInIndex && (null == lastModified || !pageInIndex.lastModified.after(lastModified))) {
                continue;
            }
            //TODO: We are supposed to ignore urls if we have already visited them and the last modification date has not been updated
            //TODO: This means we do not add any urls to the frontier when we start from a root that has already been indexed - What should we do in this situation

            if (null != pageInIndex) {
                index.removeDocument(currDocId);
            }

            visited.add(current);

            LocalDateTime startT = LocalDateTime.now();
            String title = "No Title";
            try {
                title = pageParser.extractTitle();
                String[] titleArray = title.split(" ");
                for (int i = 0; i < titleArray.length; ++i) {
                    if (!stopStem.isStopWord(titleArray[i])) {
                        String stemmed = stopStem.stem(titleArray[i]);

                        //Stop getting those empty entries
                        if (stemmed.isEmpty()) continue;

                        index.addTitleEntry(stemmed, current, i);
                    }
                }
            } catch (ParserException e) {
                e.printStackTrace();
            }
            long dT = Duration.between(startT, LocalDateTime.now()).getSeconds();
            totalDurationT += dT;
            if (dT < minConnectionT) {
                minConnectionT = dT;
            }
            else if (dT > maxConnectionT) {
                maxConnectionT = dT;
            }

            LocalDateTime startW = LocalDateTime.now();
            Vector<String> words = null;
            try {
                //Get the words from the page
                words = pageParser.extractWords();
            } catch (ParserException e) {
                e.printStackTrace();
                continue;
            }

            for (int i = 0; i < words.size(); ++i) {
                //Make sure the word is lower case before we try to do anything with it.
                words.set(i, words.get(i).toLowerCase());

                if (!stopStem.isStopWord(words.get(i))) {
                    String stemmed = stopStem.stem(words.get(i));

                    //Stop getting those empty entries
                    if (stemmed.isEmpty()) {index.addWordToDocContent(currDocId, words.get(i)); continue;}

                    index.addEntry(stemmed, current, i);
                }
                index.addWordToDocContent(currDocId, words.get(i));
            }
            long dW = Duration.between(startW, LocalDateTime.now()).getSeconds();
            totalDurationW += dW;
            if (dW < minConnectionW) {
                minConnectionW = dW;
            }
            else if (dW > maxConnectionW) {
                maxConnectionW = dW;
            }


            LocalDateTime startL = LocalDateTime.now();
            try {
                //Add all the links to the frontier that we haven't seen already
                Vector<String> links = pageParser.extractLinks();
                if (size.equals("-1")) {

                }
                //Use the character count if default size is not included in the response header
                if (-1 == size) {
                    size = pageParser.size_default;
                }
                //Add the page to the docIndex
                index.insertIntoDocIndex(currDocId, current, lastModified, size, title);

                //Save the child links
                index.addChildLinks(currDocId, links);

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
            long dL = Duration.between(startL, LocalDateTime.now()).getSeconds();
            totalDurationL += dL;
            if (dL < minConnectionL) {
                minConnectionL = dL;
            }
            else if (dL > maxConnectionL) {
                maxConnectionL = dL;
            }

        }
        System.out.println();

        if (debug) {
            System.out.println("Total seconds spent connection: " + totalDuration);
            System.out.println("Average seconds spent connection: " + totalDuration / maxLinks);
            System.out.println("max connection time: " + maxConnection);
            System.out.println("min connection time: " + minConnection);

            System.out.println("Total seconds spent connection: " + totalDurationT);
            System.out.println("Average seconds spent connection: " + totalDurationT / maxLinks);
            System.out.println("max connection time: " + maxConnectionT);
            System.out.println("min connection time: " + minConnectionT);

            System.out.println("Total seconds spent connection: " + totalDurationW);
            System.out.println("Average seconds spent connection: " + totalDurationW / maxLinks);
            System.out.println("max connection time: " + maxConnectionW);
            System.out.println("min connection time: " + minConnectionW);

            System.out.println("Total seconds spent connection: " + totalDurationL);
            System.out.println("Average seconds spent connection: " + totalDurationL / maxLinks);
            System.out.println("max connection time: " + maxConnectionL);
            System.out.println("min connection time: " + minConnectionL);
        }

        index.finalize();
    }
}
