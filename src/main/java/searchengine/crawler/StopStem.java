package searchengine.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Stream;

public class StopStem
{
    private Porter porter;
    private java.util.HashSet stopWords;
    public boolean isStopWord(String str)
    {
        return stopWords.contains(str);
    }
    public StopStem(String str)
    {
        super();
        porter = new Porter();
        stopWords = new java.util.HashSet();

        try {
            Stream<String> lines = Files.lines(new File(str).toPath());
            lines.forEach(stopWords::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String stem(String str)
    {
        return porter.stripAffixes(str);
    }
    public static void main(String[] arg)
    {
        StopStem stopStem = new StopStem("stopwords.txt");
        String input="";
        try{
            do
            {
                System.out.print("Please enter a single English word: ");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                input = in.readLine();
                if(input.length()>0)
                {
                    if (stopStem.isStopWord(input))
                        System.out.println("It should be stopped");
                    else
                        System.out.println("The stem of it is \"" + stopStem.stem(input)+"\"");
                }
            }
            while(input.length()>0);
        }
        catch(IOException ioe)
        {
            System.err.println(ioe.toString());
        }
    }
}
