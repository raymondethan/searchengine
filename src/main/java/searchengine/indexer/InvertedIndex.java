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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

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

	private RecordManager recman;

	private HTree hashtable;

    private WordIndex wordIndex;
    private LinkIndex linkIndex;
	private DocumentWordCounts wordCountIndex;

	public InvertedIndex(String recordmanager) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
        wordIndex = new WordIndex(recman);
        linkIndex = new LinkIndex(recman);
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
		
	}

	public void delEntry(String word) throws IOException
	{
		//TODO remove from documents that have the word count?

		// Delete the word and its list from the hashtable
		hashtable.remove(word);
	
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

			String url = linkIndex.get(key);
			String title = "unknown"; //TODO load from somewhere
			String lastModified = "01/01/0001"; //TODO load from somewhere
			String size = "0"; //TODO load from somewhere

			Map<String, Integer> wordCountsMap = wordCountIndex.get(key);
			String wordCounts = wordCountsMap
					.keySet()
					.stream()
					.map(word -> word + " " + wordCountsMap.get(word))
					.collect(Collectors.joining("; "));

			List<String> linksList = linkIndex.getChildren(url);
			String links = linksList
					.stream()
					.collect(Collectors.joining("\n"));

			String result = String.format(outputFormatter, url, title, lastModified, size, wordCounts, links);
			stream.println(result);
		}
    }

    public void printAll() throws IOException {
        printAll(System.out);
    }
}