package searchengine.indexer;/* --
COMP336 Lab1 Exercise
Student Name:
Student ID:
Section:
Email:
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import searchengine.Settings;

public class InvertedIndex
{
	private final int maxInsertionsBeforeMerge;
	private RecordManager recman;
	private HTree hashtable;
	private HTree tmpHashtable;
	private int insertionsSinceLastMerge = 0;
    private int lastDocIdInserted = -1;

	public InvertedIndex(RecordManager recordmanager, String indexName) throws IOException
	{
		this.recman = recordmanager;
        Settings settings = new Settings();
        maxInsertionsBeforeMerge = settings.maxInsertionsBeforeMerge;

        //Create our inverted index
		long recid = recman.getNamedObject(indexName);
		long tmpRecid = recman.getNamedObject("tmp"+indexName);
		if (recid != 0) {
			hashtable = HTree.load(recman, recid);
		}
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(indexName, hashtable.getRecid());
		}
		if (tmpRecid != 0) {
			tmpHashtable = HTree.load(recman, tmpRecid);
		}
		else {
			tmpHashtable = HTree.createInstance(recman);
			recman.setNamedObject("tmp"+indexName, tmpHashtable.getRecid());
		}
	}


	public void addEntry(int wordId, int docId, int pos) throws IOException
	{
        if (docId != lastDocIdInserted) {
            ++insertionsSinceLastMerge;
            lastDocIdInserted = docId;
        }

        if (insertionsSinceLastMerge >= maxInsertionsBeforeMerge) {
            merge(tmpHashtable,hashtable);
            insertionsSinceLastMerge = 0;
        }

		List<Posting> entries = (List<Posting>) tmpHashtable.get(wordId);
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

		tmpHashtable.put(wordId, entries);
		
	}

    private void merge(HTree tmpHashtable, HTree hashtable) throws IOException {

        FastIterator iter = tmpHashtable.keys();
        Integer key;

        while ((key = (Integer) iter.next()) != null) {
            List<Posting> tmpPosting = (List<Posting>) tmpHashtable.get(key);
            List<Posting> permanentPosting = (List<Posting>) hashtable.get(key);
            if (permanentPosting == null) {
                permanentPosting = new ArrayList<>();
                permanentPosting.addAll(tmpPosting);
            }
            else {
                permanentPosting.addAll(tmpPosting);
            }
            hashtable.put(key, permanentPosting);
        }

        clearHashTable(tmpHashtable, hashtable);

    }

    private void clearHashTable(HTree tmpHastable, HTree permHashtable) throws IOException {
        FastIterator iter = tmpHashtable.keys();
        Integer key;
        List<Integer> keys = new ArrayList<Integer>();

        while ((key = (Integer) iter.next()) != null) {
            keys.add(key);
        }
        for (Integer wordId : keys) {
            tmpHastable.remove(wordId);
        }
    }

    public void remove(Integer docId) throws IOException {
        removeDocFromHashtable(docId,tmpHashtable);
        removeDocFromHashtable(docId,hashtable);
	}

    private void removeDocFromHashtable(Integer docId, HTree hastable) throws IOException {
        FastIterator iter = hastable.keys();
        Integer key;

        while ((key = (Integer) iter.next()) != null) {
            List<Posting> entries = (List<Posting>) hastable.get(key);
            for (int i = 0; i < entries.size(); ++i) {
                Posting element = entries.get(i);
                if (element.doc == docId) {
                    entries.remove(i);
                    break;
                }
            }
        }
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

	public void finalize() throws IOException {
		merge(tmpHashtable, hashtable);
		insertionsSinceLastMerge = 0;
	}
}