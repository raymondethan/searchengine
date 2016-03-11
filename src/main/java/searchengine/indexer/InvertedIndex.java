package searchengine.indexer;/* --
COMP336 Lab1 Exercise
Student Name:
Student ID:
Section:
Email:
*/

import java.io.PrintStream;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.io.IOException;
import java.io.Serializable;

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
	private RecordManager recman;
	private HTree hashtable;

	public InvertedIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
			
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
		String newEntry = "doc" + Integer.toString(x) + " " + Integer.toString(y);
		Object oldEntry = hashtable.get(word);
		if (oldEntry != null) {
			newEntry = (String) oldEntry + " " + newEntry;
		}
		hashtable.put(word, newEntry);
		
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
                stream.println(key + " : " + hashtable.get(key));
        }
	
	}

    public void printAll() throws IOException {
        printAll(System.out);
    }
}