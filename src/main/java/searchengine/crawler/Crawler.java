package searchengine.crawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

            PageParser pageParser = new PageParser(current);
            if (!pageParser.urlIsValid) {
                System.out.println("page parser invalid url---------------------------");
                continue;
            }
            Date lastModified = pageParser.lastModified;
            Integer size = pageParser.size;

            Integer currDocId = index.getDocId(current);
            WebPage pageInIndex = index.getWebPage(currDocId);

            //If the doc exists in our index, and the webpage retrieved does not have a last modified field
            //Or the webpage's last modified field is after the last modification date of the document in the index
            //Then we do not want to add the page to the index and visit its children
            if (null != currDocId && null != pageInIndex && (null == lastModified || !pageInIndex.lastModified.after(lastModified))) {
                continue;
            }
            //TODO: We are supposed to ignore urls if we have already visited them and the last modification date has not been updated
            //TODO: This means we do not add any urls to the frontier when we start from a root that has already been indexed - What should we do in this situation

            if (null != currDocId) {
                index.removeDocument(currDocId);
            }

            visited.add(current);

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

            Vector<String> words = null;
            try {
                //Get the words from the page
                words = pageParser.extractWords();
            } catch (ParserException e) {
                e.printStackTrace();
                continue;
            }

            if (title.equals("Led Zeppelin: The Song Remains the Same (1976)")) {
                int a = 1;
            }

            //todo: convert the words to lower case?????
            for (int i = 0; i < words.size(); ++i) {
                if (!stopStem.isStopWord(words.get(i))) {
                    String stemmed = stopStem.stem(words.get(i));

                    //Stop getting those empty entries
                    if (stemmed.isEmpty()) {index.addWordToDocContent(currDocId, words.get(i)); continue;}

                    index.addEntry(stemmed, current, i);
                }
                index.addWordToDocContent(currDocId, words.get(i));
            }

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

                //TODO: check that the links are getting stored correctly - there is a page that only 4 child links are getting printed out for, which does not match online
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
        }
        System.out.println();

        index.finalize();
    }
}
