package searchengine.indexer;/* --
COMP336 Lab1 Exercise
Student Name:
Student ID:
Section:
Email:
*/

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import searchengine.crawler.WebPage;

class Posting implements Serializable
{
	public int doc;
	public int freq;

	Posting(int doc, int freq)
	{
		this.doc = doc;
		this.freq = freq;
	}
}

public class InvertedIndex
{
    private static final String INVERTED_INDEX_NAME = "ht1";
	private static final String DOCIDINDEX_NAME = "docIdIndex";
    private static final int MAX_TERMS_PRINTED = 5;

	private RecordManager recman;

	private HTree hashtable;

    private WordIndex wordIndex;
    private LinkIndex linkIndex;
	private DocumentWordCounts wordCountIndex;
	private BasicPersistentMap docIdIndex;

	public InvertedIndex(String recordmanager) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
        wordIndex = new WordIndex(recman);
        linkIndex = new LinkIndex(recman);
		docIdIndex = new BasicPersistentMap<>(DOCIDINDEX_NAME,recman);
		wordCountIndex = new DocumentWordCounts(recman);

        //Create our inverted index
		long recid = recman.getNamedObject(INVERTED_INDEX_NAME);
		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(INVERTED_INDEX_NAME, hashtable.getRecid() );
		}
	}


	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	}

    public void addChildLinks(String link, List<String> children) throws IOException {
        linkIndex.addChildren(link, children);
    }

    public void addParentLink(String link, String parent) throws IOException {
        linkIndex.addParent(link, parent);
    }

	public void addEntry(String word, String link, int y) throws IOException
	{
        int wordId = wordIndex.getId(word);
        int docId = linkIndex.getId(link);

		//We add the word not the word id because we only ever use this printing.
		wordCountIndex.addWord(docId, word);

		// Add a "docX Y" entry for the key "word" into hashtable

		List<Posting> entries = (List<Posting>) hashtable.get(wordId);
		if (entries == null) {
			Posting entry = new Posting(docId, 1);
			entries = new ArrayList<>();
			entries.add(entry);
		} else {
			boolean docFound = false;
			for (int i = 0; i < entries.size(); ++i) {
				Posting element = entries.get(i);
				if (element.doc == docId) {
					element.freq += 1;
					docFound = true;
					break;
				}
			}
			if (!docFound) {
				Posting entry = new Posting(docId, 1);
				entries.add(entry);
			}
		}

		hashtable.put(wordId, entries);

		//Add docId to docIdIndex if it
		
	}

	public void delEntry(String word) throws IOException
	{
		//TODO remove from documents that have the word count?

		// Delete the word and its list from the hashtable
		hashtable.remove(word);
	
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
            String outputFormatter = "%s\n%s\n%s, %s\n%s\n%s\n-------------------------------------------------------------------------------------------";

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
    }

    public void printAll() throws IOException {
        printAll(System.out);
    }
}