/* --
COMP4321 Lab2 Exercise
Student Name:
Student ID:
Section:
Email:
*/
package searchengine.crawler;

import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;


public class PageParser
{
	private String url;
	PageParser(String _url)
	{
		url = _url;
	}
	public Vector<String> extractWords() throws ParserException

	{
		// extract words in url and return them
		// use StringTokenizer to tokenize the result from StringBean
		StringBean sb;

        sb = new StringBean ();
        sb.setLinks (true);
        sb.setURL (url);
        
        StringTokenizer st = new StringTokenizer(sb.getStrings());
        Vector<String> vec = new Vector<String>();
        while (st.hasMoreTokens()) {
        	vec.addElement(st.nextToken());
        }

        return (vec);
			
	}
	public Vector<String> extractLinks() throws ParserException

	{
		// extract links in url and return them
		LinkBean lb = new LinkBean();
		lb.setURL(url);
		Vector<String> links = new Vector<String>();
		URL[] URL_array = lb.getLinks();
	    for(int i=0; i<URL_array.length; i++){
	    	links.addElement(URL_array[i].toString());
	    }
		return links;
	}
	
	public static void main (String[] args)
	{
		try
		{
			PageParser crawler = new PageParser("http://www.cs.ust.hk/~dlee/4321/");


			Vector<String> words = crawler.extractWords();		
			
			System.out.println("Words in "+crawler.url+":");
			for(int i = 0; i < words.size(); i++)
				System.out.print(words.get(i)+" ");
			System.out.println("\n\n");
			

	
			Vector<String> links = crawler.extractLinks();
			System.out.println("Links in "+crawler.url+":");
			for(int i = 0; i < links.size(); i++)		
				System.out.println(links.get(i));
			System.out.println("");
			
		}
		catch (ParserException e)
            	{
                	e.printStackTrace ();
            	}

	}
}
	