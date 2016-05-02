package searchengine;

/**
 *
 */
public class Settings {
    public int port = 8000;
    public int crawlCount = 300;
    public boolean shouldCrawl = true;
    public String startUrl = "http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm";
    public int maxTermsPrinted = 5;
    public int maxSearchResults = 50;
    public int similarityWeight = 90;
    public int pageRankWeight = 10;
    public int pageRankIterations = 40;
}
