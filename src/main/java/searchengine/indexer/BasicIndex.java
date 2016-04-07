package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;
import jdbm.helper.FastIterator;

/**
 *
 */
public class BasicIndex<T> {
    //We need to use something for an index that's a word, so we don't ever overwrite it..
    private static final int HIGHEST_ID_NAME = -1;

    private BasicPersistentMap<Integer, T> map;
    private BasicPersistentMap<Object, Integer> inverseMap;

    //The id we've reached
    private Integer highestIndex;

    private String tableName;
    private String inverseTableName;

    public BasicIndex(String tableName, RecordManager recordManager) throws IOException {
        this.tableName = tableName;
        this.inverseTableName = tableName + "_inverse";

        map = new BasicPersistentMap<>(this.tableName, recordManager);
        inverseMap = new BasicPersistentMap<>(inverseTableName, recordManager);

        highestIndex = inverseMap.get(HIGHEST_ID_NAME);
        if (highestIndex == null) {
            inverseMap.put(HIGHEST_ID_NAME, 0);
            highestIndex = 0;
        }
    }

    public int getId(T item) throws IOException {
        Integer id = inverseMap.get(item);
        if (id == null) {
            id = highestIndex;

            inverseMap.put(item, id);
            map.put(id, item);

            highestIndex++;
            inverseMap.put(HIGHEST_ID_NAME, highestIndex);
        }

        return id;
    }

    public T get(int id) throws IOException {
        return (T) map.get(id);
    }

    public FastIterator getIds() throws IOException {
        return map.getIds();
    }
}
