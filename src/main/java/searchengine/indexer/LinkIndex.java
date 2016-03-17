package searchengine.indexer;

import java.io.IOException;
import jdbm.RecordManager;

/**
 *
 */
public class LinkIndex extends BasicIndex<String> {
    public static final String LINK_INDEX_NAME = "linkindex";

    public LinkIndex(RecordManager recman) throws IOException {
        super(LINK_INDEX_NAME, recman);
    }
}
