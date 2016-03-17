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

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
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
        sb.setLinks (false);
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

    public String extractTitle() throws ParserException {
        Parser parser = new Parser();
        parser.setResource(url);
        NodeFilter nf = new NodeFilter() {
            @Override
            public boolean accept(Node node) {
                return node instanceof TitleTag;
            }
        };
        NodeList list = parser.parse(nf);
        Node title = list.elementAt(0);
        if (title != null) {
            return title.toString();
        }
        return "No title";
    }

}
	