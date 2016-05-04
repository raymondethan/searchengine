package searchengine.searcher;

import java.util.List;

/**
 * A class containing pertinent information about a search result
 */
public class SearchResult {
    private double similarity;

    private Integer id;
    private String title;
    private String description;
    private String link;
    private String lastModified;
    private int size;
    private String topWordCounts;
    private List<String> parentLinks;
    private List<String> childLinks;

    public SearchResult(Integer id, String title, String description, String link, double similarity, String lastModified, int size, String topWordCounts, List<String> parentLinksList, List<String> childLinksList) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.similarity = similarity;
        this.lastModified = lastModified;
        this.size = size;
        this.topWordCounts = topWordCounts;
        this.parentLinks = parentLinksList;
        this.childLinks = childLinksList;

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

    public Integer getId() {
        return id;
    }

    public String getLastModified() { return lastModified; }

    public int getSize() { return size; }

    public String getTopWordCounts() {
        return topWordCounts;
    }

    public void setTopWordCounts(String topWordCounts) {
        this.topWordCounts = topWordCounts;
    }

    public List<String> getParentLinks() {
        return parentLinks;
    }

    public void setParentLinks(List<String> parentLinks) {
        this.parentLinks = parentLinks;
    }

    public List<String> getChildLinks() {
        return childLinks;
    }

    public void setChildLinks(List<String> childLinks) {
        this.childLinks = childLinks;
    }
}
