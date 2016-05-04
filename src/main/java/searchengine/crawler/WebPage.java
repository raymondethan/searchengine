package searchengine.crawler;

import java.util.Date;

/**
 * Created by ethanraymond on 3/18/16.
 */
public class WebPage implements java.io.Serializable {

    public Integer docId;
    public String url;
    public Date lastModified;
    public Integer size;
    public String title;
    public double pagerank;

    public WebPage(int docId, String url, Date lastModified, Integer size, String title) {
        this.docId = docId;
        this.url = url;
        this.lastModified = lastModified;
        this.size = size;
        this.title = title;
        this.pagerank = 1;
    }

    @Override
    public int hashCode() {
        return docId.hashCode();
    }

}
