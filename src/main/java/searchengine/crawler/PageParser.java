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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.HTMLLinkBean;
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
    private HttpURLConnection connection;
    private String[] responseHeader;
    public Date lastModified = null;
    public Integer size = null;
    //We use this variable to count the total number of chars on the page if size is not included in the response header
    public int size_default = 0;
    public Boolean urlIsValid = true;

	PageParser(String _url) throws ParseException {
        this.url = _url;
        try {
            parser.setResource(url);
        } catch (ParserException e) {
            urlIsValid = false;
            //e.printStackTrace();
        }
        if (urlIsValid) {
            ConnectionManager manager = Parser.getConnectionManager();
            connection = (HttpURLConnection) parser.getConnection();
            try {
                responseHeader = HttpHeader.getResponseHeader(connection).split("\n");
            } catch (Exception e) {
                e.printStackTrace();
                responseHeader = new String[0];
            }
            extractResponseInfo();
        }
    };

	public Vector<String> extractWords() throws ParserException

	{
		// extract words in url and return them
		// use StringTokenizer to tokenize the result from StringBean
        parser.reset();
        StringBean sb = new StringBean ();
        sb.setLinks (false);

        parser.visitAllNodesWith(sb);
        Vector<String> vec = new Vector<String>();
        String strings = sb.getStrings();
        if (null == strings) return vec;
        StringTokenizer st = new StringTokenizer(strings);
        while (st.hasMoreTokens()) {
            String element = st.nextToken();
        	vec.addElement(element);
            this.size_default += element.length();
        }

        return (vec);
			
	}
	public Vector<String> extractLinks() throws ParserException

	{
		// extract links in url and return them
		LinkBean lb = new LinkBean();
		//lb.setConnection(connection);
        lb.setURL(url);
		Vector<String> links = new Vector<String>();
		URL[] URL_array = lb.getLinks();
	    for(int i=0; i<URL_array.length; i++){
	    	links.addElement(URL_array[i].toString());
            this.size_default += URL_array[i].toString().length();
	    }
		return links;
	}

    private void extractResponseInfo() throws ParseException {
        long lm = connection.getLastModified();
        if (-1 == lm) {
            lm = connection.getDate();
        }
        this.lastModified = new Date(lm);
        this.size = connection.getContentLength();
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
            return ((TitleTag)title).getTitle();
        }
        return "No title";
    }

}
	