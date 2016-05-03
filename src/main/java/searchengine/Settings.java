package searchengine;

/**
 *
 */
public class Settings {
    public int port = 8000;
    public int crawlCount = 600;
    public boolean shouldCrawl = true;
    public String startUrl = "https://www.cse.ust.hk/";
    public int maxTermsPrinted = 5;
    public int maxSearchResults = 50;
    public int similarityWeight = 90;
    public int pageRankWeight = 10;
    public int pageRankIterations = 40;
}
