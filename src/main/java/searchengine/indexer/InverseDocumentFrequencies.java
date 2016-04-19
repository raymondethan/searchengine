package searchengine.indexer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import jdbm.RecordManager;

/**
 *
 */
public class InverseDocumentFrequencies {
    private BasicPersistentMap<Integer, Set<Integer>> map;

    public InverseDocumentFrequencies(String name, RecordManager recman) throws IOException {
        map = new BasicPersistentMap<>(name, recman);
    }

    public void addDocument(Integer wordId, Integer documentId) throws IOException {
        Set<Integer> documents = map.get(wordId);
        if (documents == null) {
            documents = new HashSet<>();
        }

        documents.add(documentId);
        map.put(wordId, documents);
    }

    public int containingDocuments(Integer word) throws IOException {
        Set<Integer> documents = map.get(word);
        if (documents == null) return  0;

        return documents.size();
    }
}
