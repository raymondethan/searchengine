package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;
import jdbm.helper.FastIterator;
import searchengine.crawler.WebPage;

/**
 *
 */
public class DocumentIndex<T,K> extends BasicPersistentMap<T,K> {
    private int documentCount = 0;

    public DocumentIndex(String recordName, RecordManager recman) throws IOException {
        super(recordName, recman);

        loadDocCount();
    }

    @Override
    public void put(T key, K value) throws IOException {
        documentCount++;
        super.put(key, value);
    }

    private void loadDocCount() throws IOException {
        FastIterator iterator = getIds();
        while (iterator.next() != null) documentCount++;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void updatePageRank(T key, float pagerank) throws IOException {
        WebPage doc = (WebPage) get(key);
        doc.pagerank = pagerank;
        super.put(key, (K) doc);
    }
}
