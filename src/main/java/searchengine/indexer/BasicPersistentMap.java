package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

/**
 *
 */
public class BasicPersistentMap<T, K> {
    protected HTree map;
    private RecordManager recman;
    private String recordName;

    public BasicPersistentMap(String recordName, RecordManager recman) throws IOException {
        this.recman = recman;
        this.recordName = recordName;

        long recid = recman.getNamedObject(recordName);
        if (recid != 0) {
            map = HTree.load(recman, recid);
        } else {
            map = HTree.createInstance(recman);
            recman.setNamedObject(recordName, map.getRecid());
        }
    }

    public K get(T index) throws IOException {
        return (K) map.get(index);
    }

    public void put(T index, K value) throws IOException {
        map.put(index, value);
    }

    public FastIterator getIds() throws IOException {
        return map.keys();
    }
}
