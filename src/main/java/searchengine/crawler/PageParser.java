/* --
COMP4321 Lab2 Exercise
Student Name:
Student ID:
Section:
Email:
*/
package searchengine.crawler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.http.ConnectionMonitor;
import org.htmlparser.http.HttpHeader;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


//TODO: double check pageparse does not make multiple http requests to the same url

public class PageParser
{
	private String url;
    private Parser parser = new Parser();
    HttpURLConnection connection;
    String[] responseHeader;
    public String lastModified;
    public String size = "No content length";

    private final String LAST_MODIFIED = "Last-Modified";
    private final String DATE = "Date";
    private final String CONTENT_LENGTH = "Content-Length";

	PageParser(String _url)
	{
        this.url = _url;
        try {
            parser.setResource(url);
        } catch (ParserException e) {
            e.printStackTrace();
        }
        ConnectionManager manager = Parser.getConnectionManager ();
        connection = (HttpURLConnection) parser.getConnection();
        try {
            responseHeader = connection.getResponseMessage().split("\n");
        } catch (IOException e) {
            e.printStackTrace();
            responseHeader = new String[0];
        }
        this.lastModified = null;
        extractResponseInfo();
    };

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

    private void extractResponseInfo() {
        String date = "No Date";
        for (int i = 0; i < responseHeader.length; ++i) {
            String[] element = responseHeader[i].split(": ");
            switch (element[0]) {
                case DATE:
                    date = element[1];
                case LAST_MODIFIED:
                    this.lastModified = element[1];
                case CONTENT_LENGTH:
                    this.size = element[1];
                default:
                    break;
            }
        }
        if (null == this.lastModified) {
            this.lastModified = date;
        }
    }

    public String extractTitle() throws ParserException {
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
	