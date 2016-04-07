package searchengine.indexer;

import javafx.geometry.Pos;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import searchengine.crawler.WebPage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 */
public class Index {
    private static final String BODY_INDEX_NAME = "body_index";
    private static final String TITLE_INDEX_NAME = "title_index";

    private static final String DOCIDINDEX_NAME = "docIdIndex";
    private static final int MAX_TERMS_PRINTED = 5;

    private InvertedIndex bodyIndex;
    private InvertedIndex titleIndex;

    private WordIndex wordIndex;
    private LinkIndex linkIndex;
    private DocumentWordCounts wordCountIndex;
    private BasicPersistentMap docIdIndex;

    private RecordManager recman;

    public Index(String recordmanager) throws IOException
    {
        recman = RecordManagerFactory.createRecordManager(recordmanager);

        bodyIndex = new InvertedIndex(recman, BODY_INDEX_NAME);
        titleIndex = new InvertedIndex(recman, TITLE_INDEX_NAME);

        wordIndex = new WordIndex(recman);
        linkIndex = new LinkIndex(recman);
        docIdIndex = new BasicPersistentMap<>(DOCIDINDEX_NAME,recman);
        wordCountIndex = new DocumentWordCounts(recman);
    }

    public void addChildLinks(String link, List<String> children) throws IOException {
        linkIndex.addChildren(link, children);
    }

    public void addParentLink(String link, String parent) throws IOException {
        linkIndex.addParent(link, parent);
    }

    public void addEntry(String word, String link, int pos) throws IOException {

        int wordId = wordIndex.getId(word);
        int docId = linkIndex.getId(link);

        //We add the word not the word id because we only ever use this printing.
        wordCountIndex.addWord(docId, word);

        //Add the word to the inverted index
        bodyIndex.addEntry(wordId, docId);
    }

    public void addTitleEntry(String title, String url) throws IOException {
        int docId = linkIndex.getId(url);
        int wordId = wordIndex.getId(title);

        titleIndex.addEntry(wordId,docId);
    }

    public void delEntry(String word) throws IOException {
        int wordId = wordIndex.getId(word);

        bodyIndex.delEntry(wordId);
    }

    //Get the docId of a given link so we can pass it into insertIntoDocInex
    public int getDocId(String link) throws IOException {
        return linkIndex.getId(link);
    }

    //Insert a doc into the docIndex, we use this when printing out information
    public void insertIntoDocIndex(int docId, String url, Date lastModified, String size, String title) throws IOException {
        WebPage page = new WebPage(docId, url, lastModified, size, title);
        docIdIndex.put(docId, page);
    }

    public WebPage getWebPage(int docId) throws IOException {
        return (WebPage) docIdIndex.get(docId);
    }

    public void printAll(PrintStream stream) throws IOException {
        // Print all the data in the hashtable
        FastIterator iter = linkIndex.getIds();

        Integer key;
        while ((key = (Integer) iter.next()) != null) {
            //Don't do anything for the value that keeps track of the auto increment index
            if (key == -1) continue;
            /**
             * The format of output as specified on the project. Args are:
             * Page title
             * Page url
             * Last modified data, Page size
             * Keyword1 freq1; ...; KeywordM fredM
             * Child Link 1
             * .
             * .
             * .
             * Child Link n
             * -------------------------------------------------------------------------------------------
             */
            String outputFormatter = "%s\n%s\n%s, %s\n%s\n%s\n%s\n%s\n-------------------------------------------------------------------------------------------";

            //Assumes get returns a valid webpage
            WebPage currPage = (WebPage) docIdIndex.get(key);

            //Added this due to a null pointer exception. I think we add links to the link index,
            //But don't index them all because of our maxLink limit on pages we index.
            if (null == currPage) continue;

            String url = linkIndex.get(key);
            String title = currPage.title;
            String lastModified = currPage.lastModified.toString();
            String size = currPage.size;

            Map<String, Integer> wordCountsMap = wordCountIndex.getWordCounts(key);

            //Possible for links we haven't scraped but have assigned an id to
            if (wordCountsMap == null) continue;

            //Print out the top 5 most frequent terms
            String wordCounts = "";
            Set<Map.Entry<String, Integer>> set = wordCountsMap.entrySet();
            List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
                    set);
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1,
                                   Map.Entry<String, Integer> o2) {

                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            for (int i = 0; i < Math.min(MAX_TERMS_PRINTED, list.size()); ++i) {
                wordCounts += list.get(i).toString().replace("="," ") + "; ";
            }

//            String wordCounts = wordCountsMap
//                    .keySet()
//                    .stream()
//                    .map(word -> word + " " + wordCountsMap.get(word))
//                    .collect(Collectors.joining("; "));


            //Print parent links and then child links
            List<String> parentLinksList = linkIndex.getParents(url);
            String parentLinks = parentLinksList
                    .stream()
                    .collect(Collectors.joining("\n"));

            String delimeter = "..............";

            List<String> childLinksList = linkIndex.getChildren(url);
            String childLinks = childLinksList
                    .stream()
                    .collect(Collectors.joining("\n"));

            String result = String.format(outputFormatter, title, url, lastModified, size, wordCounts, parentLinks, delimeter, childLinks);
            stream.println(result);
        }

        FastIterator iterator = titleIndex.getIterator();
        Integer current;
        while ((current = (Integer)iterator.next()) != null) {
            String word = wordIndex.get(current);
            ArrayList<Posting> postings = titleIndex.getDocuments(current);

            String output = word + ": " + postings.stream().map(p -> p.doc + "").collect(Collectors.joining(", "));
            System.out.println(output);
        }
    }

    public void printAll() throws IOException {
        printAll(System.out);
    }

    public void finalize() throws IOException
    {
        recman.commit();
        recman.close();
    }
}
