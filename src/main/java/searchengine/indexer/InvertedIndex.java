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

public class InvertedIndex
{
	private RecordManager recman;
	private HTree hashtable;

	public InvertedIndex(RecordManager recordmanager, String indexName) throws IOException
	{
		this.recman = recordmanager;

        //Create our inverted index
		long recid = recman.getNamedObject(indexName);
		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(indexName, hashtable.getRecid() );
		}
	}


	public void addEntry(int wordId, int docId, int pos) throws IOException
	{
		// Add a "docX Y" entry for the key "word" into hashtable

		List<Posting> entries = (List<Posting>) hashtable.get(wordId);
		if (entries == null) {
			Posting entry = new Posting(docId, 1);
            entry.positions.add(pos);

			entries = new ArrayList<>();
			entries.add(entry);
		} else {
			boolean docFound = false;
			for (int i = 0; i < entries.size(); ++i) {
				Posting element = entries.get(i);
				if (element.doc == docId) {
					element.positions.add(pos);
					element.freq += 1;
					docFound = true;
					break;
				}
			}
			if (!docFound) {
				Posting entry = new Posting(docId, 1);
                entry.positions.add(pos);
				entries.add(entry);
			}
		}

		hashtable.put(wordId, entries);

		//Add docId to docIdIndex if it
		
	}

    public ArrayList<Posting> getDocuments(int wordIndex) throws IOException {
        return (ArrayList<Posting>) hashtable.get(wordIndex);
    }

	public FastIterator getIterator() throws IOException {
        return hashtable.keys();
    }

	public void delEntry(int wordId) throws IOException
	{
		//TODO remove from documents that have the word count?

		// Delete the word and its list from the hashtable
		hashtable.remove(wordId);
	
	}
}