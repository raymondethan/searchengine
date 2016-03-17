package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;
import jdbm.htree.HTree;

/**
 *
 */
public class WordIndex {
    private static final String WORD_MAP_NAME = "wordmap";
    private static final String WORD_ID_MAP_NAME = "wordidmap";
    //We need to use something for an index that's a word, so we don't ever overwrite it..
    private static final int HIGHEST_WORD_NAME = -1;
    private RecordManager recman;
    //The highest word id we've reached
    private int highestWord = 0;

    private HTree wordMap;
    private HTree wordIdMap;

    public WordIndex(RecordManager recman) throws IOException {
        this.recman = recman;

        long recid = recman.getNamedObject(WORD_MAP_NAME);
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

    public int getWordId(String word) throws IOException {
        Integer wordId = (Integer)wordMap.get(word);
        if (wordId == null) {
            wordId = highestWord;

            //Make sure we update the highest word id
            highestWord++;
            wordMap.put(HIGHEST_WORD_NAME, highestWord);
            wordIdMap.put(wordId, word);
            wordMap.put(word, wordId);
        }

        return wordId;
    }

    public String getWord(int id) throws IOException {
        return (String)wordIdMap.get(id);
    }
}
