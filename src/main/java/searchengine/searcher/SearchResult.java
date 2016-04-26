package searchengine.searcher;

/**
 * A class containing pertinent information about a search result
 */
public class SearchResult {
    private float pageRank;
    private double similarity;

    private Integer id;
    private String title;
    private String description;
    private String link;

    public SearchResult(Integer id, String title, String description, String link, double similarity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.similarity = similarity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public float getPageRank() {
        return pageRank;
    }

    public void setPageRank(float pageRank) {
        this.pageRank = pageRank;
    }

    public Integer getId() {
        return id;
    }
}
