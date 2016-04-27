package searchengine.indexer;

import jdbm.RecordManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ethanraymond on 4/27/16.
 */
public class DocContentIndex extends BasicPersistentMap {

    //number of words retreived from each side of the given position for the description
    private final int DESCRIPTION_OFFSET = 10;

    public DocContentIndex(String recordName, RecordManager recman) throws IOException {
        super(recordName, recman);
    }

    public void addWord(int docId, int wordId) throws IOException {
        ArrayList<Integer> content = (ArrayList<Integer>) get(docId);
        if (null == content) {
            content = new ArrayList<Integer>();
        }
        content.add(wordId);
        put(docId,content);
    }

    public ArrayList<Integer> getDescription(int docId, int position) throws IOException {
        ArrayList<Integer> positions = (ArrayList<Integer>) get(docId);
        return new ArrayList<Integer>(positions.subList(Math.max(0,position - DESCRIPTION_OFFSET), Math.min(positions.size(), position + DESCRIPTION_OFFSET)));
    }
}
