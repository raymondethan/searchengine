package searchengine.crawler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
            index = new InvertedIndex("inverted_index", "ht1");
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

            for (int i = 0; i < words.size(); ++i) {
                if (!stopStem.isStopWord(words.get(i))) {
                    index.addEntry(stopStem.stem(words.get(i)), visited.size(), i);
                }

            }

            System.out.println(current);
        }

        PrintStream stream = new PrintStream(new FileOutputStream("output.txt"));
        index.printAll(stream);
        index.finalize();
    }
}
