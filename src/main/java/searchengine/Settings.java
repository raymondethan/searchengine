package searchengine;

/**
 *
 */
public class Settings {
    public int port = 8000;
    public int crawlCount = 300;
    public final int maxInsertionsBeforeMerge = 75;
    public boolean shouldCrawl = true;
    public String startUrl = "http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm";
    public int maxTermsPrinted = 5;
    public int maxSearchResults = 75;
    public double alpha = .9;
    public int pageRankIterations = 40;
    public final float damping_factor = (float) .85;
}
