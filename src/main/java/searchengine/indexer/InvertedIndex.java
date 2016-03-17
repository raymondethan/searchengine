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

	private RecordManager recman;
	private HTree hashtable;

	public InvertedIndex(String recordmanager) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(INVERTED_INDEX_NAME);
			
		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject( "ht1", hashtable.getRecid() );
		}
	}


	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	} 

	public void addEntry(String word, int x, int y) throws IOException
	{
		// Add a "docX Y" entry for the key "word" into hashtable
        Posting entry = new Posting("doc" + x, y);

		List<Posting> entries = (List<Posting>) hashtable.get(word);
		if (entries == null) {
			entries = new ArrayList<>();
		}

        entries.add(entry);
		hashtable.put(word, entries);
		
	}

	public void delEntry(String word) throws IOException
	{
		// Delete the word and its list from the hashtable
		hashtable.remove(word);
	
	}

	public void printAll(PrintStream stream) throws IOException
	{
		// Print all the data in the hashtable
		FastIterator iter = hashtable.keys();

        String key;
        while( (key = (String)iter.next())!=null)
        {
                // get and print the content of each key
                List<Posting> entries = (List<Posting>) hashtable.get(key);
                String entriesString = entries.stream().map(p -> (p.doc + " " + p.freq)).collect(Collectors.joining(" "));
                stream.println(key + " : " + entriesString);
        }
	
	}

    public void printAll() throws IOException {
        printAll(System.out);
    }
}