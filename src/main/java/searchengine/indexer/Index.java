package searchengine.indexer;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import searchengine.crawler.WebPage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Index {
    private static final String BODY_IDF_NAME = "body_idfs";
    private static final String TITLE_IDF_NAME = "title_idfs";
    private static final String BODY_INDEX_NAME = "body_index";
    private static final String TITLE_INDEX_NAME = "title_index";
    private static final String DOC_CONTENT_INDEX_NAME = "content_index";

    private static final String DOCIDINDEX_NAME = "docIdIndex";
    private static final int MAX_TERMS_PRINTED = 5;

    private InvertedIndex bodyIndex;
    private InverseDocumentFrequencies bodyInverseDocumentFrequencies;
    private InvertedIndex titleIndex;
    private InverseDocumentFrequencies titleInverseDocumentFrequencies;

    private WordIndex wordIndex;
    private LinkIndex linkIndex;
    private DocumentWordCounts wordCountIndex;
    private DocumentIndex docIdIndex;
    private DocContentIndex docContentIndex;

    private RecordManager recman;

    public Index(String recordmanager) throws IOException
    {
        recman = RecordManagerFactory.createRecordManager(recordmanager);

        bodyIndex = new InvertedIndex(recman, BODY_INDEX_NAME);
        titleIndex = new InvertedIndex(recman, TITLE_INDEX_NAME);

        wordIndex = new WordIndex(recman);
        linkIndex = new LinkIndex(recman);
        docIdIndex = new DocumentIndex(DOCIDINDEX_NAME,recman);
        wordCountIndex = new DocumentWordCounts(recman);
        bodyInverseDocumentFrequencies = new InverseDocumentFrequencies(BODY_IDF_NAME, recman);
        titleInverseDocumentFrequencies = new InverseDocumentFrequencies(TITLE_IDF_NAME, recman);

        docContentIndex = new DocContentIndex(DOC_CONTENT_INDEX_NAME, recman);
    }

    public List<Posting> getDoc(String word) throws IOException {
        Integer wordId = wordIndex.tryGetId(word);
        if (wordId == null) return new ArrayList<>();

        return getDoc(wordId);
    }

    public List<Posting> getDoc(int wordId) throws IOException {
        return bodyIndex.getDocuments(wordId);
    }

    public List<Posting> getTitleDoc(String word) throws IOException {
        Integer wordId = wordIndex.tryGetId(word);
        if (wordId == null) return new ArrayList<>();

        return getTitleDoc(wordId);
    }

    public List<Posting> getTitleDoc(int wordId) throws IOException {
        return titleIndex.getDocuments(wordId);
    }

    public void addChildLinks(Integer linkId, List<String> children) throws IOException {
        linkIndex.addChildren(linkId, children);
    }

    public void addParentLink(String link, String parent) throws IOException {
        Integer linkId = getDocId(link);
        linkIndex.addParent(linkId, parent);
    }

    public void addEntry(String word, String link, int pos) throws IOException {

        int wordId = wordIndex.getId(word);
        int docId = linkIndex.getId(link);

        //We add the word not the word id because we only ever use this printing.
        wordCountIndex.addWord(docId, word);

        //Add the word to the inverted index
        bodyIndex.addEntry(wordId, docId, pos);
        bodyInverseDocumentFrequencies.addDocument(wordId, docId);
    }

    public void removeDocument(Integer docId) throws IOException {
        bodyIndex.remove(docId);
        titleIndex.remove(docId);
    }

    public void addWordToDocContent(int docId, String word) throws IOException {
        int wordId = wordIndex.getId(word);
        docContentIndex.addWord(docId, wordId);
    }

    public ArrayList<Integer> getDescription(int docId, int position) throws IOException {
        return docContentIndex.getDescription(docId, position);
    }

    public void addTitleEntry(String title, String url, int pos) throws IOException {
        int docId = linkIndex.getId(url);
        int wordId = wordIndex.getId(title);

        titleIndex.addEntry(wordId,docId, pos);
        titleInverseDocumentFrequencies.addDocument(wordId, docId);
    }

    public void delEntry(String word) throws IOException {
        int wordId = wordIndex.getId(word);

        bodyIndex.delEntry(wordId);
    }

    public Integer tryGetWordId(String word) throws IOException {
        return wordIndex.tryGetId(word);
    }

    public String getWord(Integer wordId) throws IOException {
        return wordIndex.get(wordId);
    }

    //Get the docId of a given link so we can pass it into insertIntoDocInex
    public int getDocId(String link) throws IOException {
        return linkIndex.getId(link);
    }

    //Insert a doc into the docIndex, we use this when printing out information
    public void insertIntoDocIndex(int docId, String url, Date lastModified, Integer size, String title) throws IOException {
        WebPage page = new WebPage(docId, url, lastModified, size, title);
        docIdIndex.put(docId, page);
    }

    public double idf(String word) throws IOException {
        Integer id = wordIndex.tryGetId(word);
        if (id == null) return 0;
        return idf(id);
    }

    public double idf(Integer wordId) throws IOException {
        int contain = bodyInverseDocumentFrequencies.containingDocuments(wordId);
        if (contain == 0) return 0;

        return Math.log(docIdIndex.getDocumentCount()/contain);
    }

    public double titleIdf(String word) throws IOException {
        Integer id = wordIndex.tryGetId(word);
        if (id == null) return 0;
        return titleIdf(id);
    }

    public double titleIdf(Integer wordId) throws IOException {
        int contain = titleInverseDocumentFrequencies.containingDocuments(wordId);
        if (contain == 0) return 0;

        return Math.log(docIdIndex.getDocumentCount()/contain);
    }

    public int getDocumentCount() {
        return docIdIndex.getDocumentCount();
    }

    public WebPage getWebPage(int docId) throws IOException {
        return (WebPage) docIdIndex.get(docId);
    }

    public int getNumChildren(int docId) throws IOException {
        return linkIndex.getNumChildren(docId);
    }

    public WebPage getWebPage(String url) throws IOException {
        return getWebPage(getDocId(url));
    }

    public FastIterator getDocIds() throws IOException {
        return docIdIndex.getIds();
    }

    public void updatePageRank(int docId, float pagerank) throws IOException {
        docIdIndex.updatePageRank(docId,pagerank);
    }

    public List<String> getParents(Integer docId) throws IOException {
        return linkIndex.getParents(docId);
    }

    public List<String> getChildLinks(Integer docId) throws IOException {
        return linkIndex.getChildLinks(docId);
    }

    public double getPageRank(Integer docId) throws IOException {
        return docIdIndex.getPageRank(docId);
    }

    public Map getWordCounts(int docId) throws IOException {
        return wordCountIndex.getWordCounts(docId);
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
            String outputFormatter = "%s\n%s\n%s, %s\n%s\n%s\n-------------------------------------------------------------------------------------------";

            //Assumes get returns a valid webpage
            WebPage currPage = (WebPage) docIdIndex.get(key);

            //Added this due to a null pointer exception. I think we add links to the link index,
            //But don't index them all because of our maxLink limit on pages we index.
            if (null == currPage) continue;

            String url = linkIndex.get(key);
            String title = currPage.title;
            String lastModified = currPage.lastModified.toString();
            Integer size = currPage.size;

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

            List<String> childLinksList = linkIndex.getChildren(url);
            String childLinks = childLinksList
                    .stream()
                    .collect(Collectors.joining("\n"));

            String result = String.format(outputFormatter, title, url, lastModified, size, wordCounts, childLinks);
            stream.println(result);
        }

    }

    public void printAll() throws IOException {
        printAll(System.out);
    }

    public void finalize() throws IOException
    {
        bodyIndex.finalize();
        titleIndex.finalize();

        recman.commit();
        recman.close();
    }
}
