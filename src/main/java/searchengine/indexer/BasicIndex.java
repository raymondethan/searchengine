package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

/**
 *
 */
public class BasicIndex<T> {
    //We need to use something for an index that's a word, so we don't ever overwrite it..
    private static final int HIGHEST_ID_NAME = -1;
    private RecordManager recman;

    private String recordName;
    private String inverseRecordName;

    //The id we've reached
    private int highestIndex = 0;

    private HTree map;
    private HTree inverseMap;

    public BasicIndex(String recordName, RecordManager recman) throws IOException {
        this.recman = recman;
        this.recordName = recordName;
        this.inverseRecordName = recordName + "_inverse";

        long recid = recman.getNamedObject(recordName);
        if (recid != 0) {
            map = HTree.load(recman, recid);
            highestIndex = (int) map.get(HIGHEST_ID_NAME);
        } else {
            map = HTree.createInstance(recman);
            recman.setNamedObject(recordName, map.getRecid());
            map.put(HIGHEST_ID_NAME, 0);
        }

        recid = recman.getNamedObject(inverseRecordName);
        if (recid != 0) {
            inverseMap = HTree.load(recman, recid);
        } else {
            inverseMap = HTree.createInstance(recman);
            recman.setNamedObject(inverseRecordName, inverseMap.getRecid());
        }
    }

    public int getId(T item) throws IOException {
        Integer wordId = (Integer) map.get(item);
        if (wordId == null) {
            wordId = highestIndex;

            //Make sure we update the highest word id
            highestIndex++;
            map.put(HIGHEST_ID_NAME, highestIndex);
            inverseMap.put(wordId, item);
            map.put(item, wordId);
        }

        return wordId;
    }

    public T get(int id) throws IOException {
        return (T) inverseMap.get(id);
    }

    public FastIterator getIds() throws IOException {
        return inverseMap.keys();
    }
}
