package searchengine.indexer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ethanraymond on 4/19/16.
 */
public class Posting implements Serializable
{
    public Integer doc;
    public int freq;
    public ArrayList<Integer> positions;

    private static long serialVersionUID = 8987159344547339369L;

    Posting(int doc, int freq)
    {
        positions = new ArrayList<>();
        this.doc = doc;
        this.freq = freq;
    }

    @Override
    public int hashCode() {
        return doc.hashCode();
    }
}
