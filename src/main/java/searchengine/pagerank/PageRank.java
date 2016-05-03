package searchengine.pagerank;

import jdbm.helper.FastIterator;
import searchengine.crawler.WebPage;
import searchengine.indexer.Index;

import java.io.IOException;
import java.util.*;
import searchengine.Settings;

/**
 * Created by ethanraymond on 4/24/16.
 */
public class PageRank {

    private final float DAMPING_FACTOR;
    private Index index;
    private Set<Integer> visited = new HashSet<>();
    private LinkedList<Integer> frontier = new LinkedList<>();

    public PageRank(Index index) throws IOException {
        this.index=index;
        Settings settings = new Settings();
        frontier.add(index.getDocId(settings.startUrl));
        DAMPING_FACTOR = settings.damping_factor;
    }


    //TODO: doesn't work if the first page doesn't have any parents
    public void rankPages() throws IOException {
        while (frontier.size() > 0 && visited.size() < index.getDocumentCount()) {
            Integer current = frontier.removeFirst();
            if (visited.contains(current)) continue;
            List<String> parents = index.getParents(current);
            float pagerank = 0;
            for (int i = 0; i < parents.size(); ++i) {
                WebPage parent = index.getWebPage(parents.get(i));
                if (null != parent) {
                    pagerank += parent.pagerank / index.getNumChildren(parent.docId);
                    if (!visited.contains(parent.docId)) {
                        frontier.addLast(parent.docId);
                    }
                }
            }
            pagerank *= DAMPING_FACTOR;
            pagerank += 1-DAMPING_FACTOR;
            index.updatePageRank(current,pagerank);
            visited.add(current);
        }
    }

    public HashMap<Integer,Float> getPageRanks() throws IOException {
        HashMap<Integer,Float> pageranks = new HashMap<Integer, Float>();
        FastIterator iter = index.getDocIds();

        Integer key;
        while ((key = (Integer) iter.next()) != null) {
            WebPage currPage = (WebPage) index.getWebPage(key);
            pageranks.put(currPage.docId, (float) currPage.pagerank);
        }
        return pageranks;
    }

    public void printPageRanks() throws IOException {
        getPageRanks().forEach((k, v) -> {
            try {
                System.out.println("DocId: " + k + ", PageRank: " + v + ", num parents " + index.getParents(k).size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
