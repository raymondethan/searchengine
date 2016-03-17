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
import java.util.stream.Collectors;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

class Posting implements Serializable
{
	public String doc;
	public int freq;

	Posting(String doc, int freq)
	{
		this.doc = doc;
		this.freq = freq;
	}
}

public class InvertedIndex
{
    private static final String INVERTED_INDEX_NAME = "ht1";
    private static final String WORD_MAP_NAME = "wordmap";
    private static final String WORD_ID_MAP_NAME = "wordidmap";

    //We need to use something for an index that's a word, so we don't ever overwrite it..
    private static final int HIGHEST_WORD_NAME = -1;
    private int highestWord = 0;

	private RecordManager recman;

	private HTree hashtable;
    private HTree wordMap;
    private HTree wordIdMap;

	public InvertedIndex(String recordmanager) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);

        //Create our inverted index
		long recid = recman.getNamedObject(INVERTED_INDEX_NAME);
		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(INVERTED_INDEX_NAME, hashtable.getRecid() );
		}

        recid = recman.getNamedObject(WORD_MAP_NAME);
        if (recid != 0) {
            wordMap = HTree.load(recman, recid);
            highestWord = (int)wordMap.get(HIGHEST_WORD_NAME);
        } else {
            wordMap = HTree.createInstance(recman);
            recman.setNamedObject(WORD_MAP_NAME, wordMap.getRecid());
            wordMap.put(HIGHEST_WORD_NAME, 0);
        }

        recid = recman.getNamedObject(WORD_ID_MAP_NAME);
        if (recid != 0) {
            wordIdMap = HTree.load(recman, recid);
        } else {
            wordIdMap = HTree.createInstance(recman);
            recman.setNamedObject(WORD_ID_MAP_NAME, wordIdMap.getRecid());
        }
	}


	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	} 

	public void addEntry(String word, int x, int y) throws IOException
	{
        Integer wordId = (Integer)wordMap.get(word);
        if (wordId == null) {
            wordId = highestWord;

            //Make sure we update the highest word id
            highestWord++;
            wordMap.put(HIGHEST_WORD_NAME, highestWord);
            wordIdMap.put(wordId, word);
            wordMap.put(word, wordId);
        }

		// Add a "docX Y" entry for the key "word" into hashtable
        Posting entry = new Posting("doc" + x, y);

		List<Posting> entries = (List<Posting>) hashtable.get(wordId);
		if (entries == null) {
			entries = new ArrayList<>();
		}

        entries.add(entry);
		hashtable.put(wordId, entries);
		
	}

	public void delEntry(String word) throws IOException
	{
		// Delete the word and its list from the hashtable
		hashtable.remove(word);
	
	}

	public void printAll(PrintStream stream) throws IOException {
        // Print all the data in the hashtable
        FastIterator iter = hashtable.keys();

        Integer key;
        while ((key = (Integer) iter.next()) != null) {
            //Get the word that corresponds to this id
            String word = (String) wordIdMap.get(key);

            // get and print the content of each key
            List<Posting> entries = (List<Posting>) hashtable.get(key);
            String entriesString = entries.stream().map(p -> (p.doc + " " + p.freq)).collect(Collectors.joining(" "));
            stream.println(word + " : " + entriesString);
        }
    }

    public void printAll() throws IOException {
        printAll(System.out);
    }
}