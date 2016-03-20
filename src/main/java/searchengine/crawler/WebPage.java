package searchengine.crawler;

/**
 * Created by ethanraymond on 3/18/16.
 */
public class WebPage implements java.io.Serializable {

    public Integer docId;
    public String url;
    public String lastModified;
    public String size;
    public String title;

    public WebPage(int docId, String url, String lastModified, String size, String title) {
        this.docId = docId;
        this.url = url;
        this.lastModified = lastModified;
        this.size = size;
        this.title = title;
    }

    @Override
    public int hashCode() {
        return docId.hashCode();
    }

}
