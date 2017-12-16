package com.sibaihm.sitemapgenerator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*	
 * The Class:
 *  Extracts all URLs from href attributes inside <a> tags in a HTML document
 *  Performs operations on the extracted URL to ensure that the URL is correct and valid, otherwise it drops the URL
 * 	
 * 
 * The Class:
 *  Doesn't support keeping URL parameters or traversing pages through them
 */

class HTMLDocument {
	
	/*
	 * The scheme and the host components of the URL 
	 */
	
	private final String URL_ROOT;
	
	/*
	 * The current URL that is being extracted
	 */
	
	private final String CURRENT_URL;
	
	/*
	 * The path of the URL
	 */
	
	private final String URL_PATH;
	
	/*
	 * The HTML document content
	 */
	
	private final String DOCUMENT;
	
	/*
	 * The default index page names on the Internet
	 */
	
	private final String[] DEFAULT_INDEX_PAGE_NAMES = {
			"index.html",
			"index.htm",
			"index.shtml",
			"index.php",
			"index.php5",
			"index.php4",
			"index.php3",
			"index.asp",
			"index.cgi",
			"Index.html",
			"Index.htm",
			"Index.shtml",
			"Index.php",
			"Index.asp",
			"Index.cgi",
			"default.html",
			"default.htm",
			"Default.htm",
			"Default.htm",
			"home.html",
			"home.htm",
			"Home.htm",
			"Home.htm",
			"placeholder.html"
	};
	
	/*
	 * THe default extensions of HTML document
	 */
	
	private final String[] DEFAULT_HTML_EXTENSIONS = {
			".html",
			".htm",
			".shtml",
			".php",
			".php5",
			".php4",
			".php3",
			".asp",
			".cgi"
	};
	
	/*
	 * The unique URLs that found in the page
	 */
	
	private Set<String> URLsSet = new HashSet<String>();
	
	/*
	 * The constructor
	 */
	
	public HTMLDocument(String DOCUMENT, String CURRENT_URL) throws MalformedURLException {
		
		this.DOCUMENT 		= DOCUMENT;
		
		this.CURRENT_URL 	= CURRENT_URL;
		
		this.URL_ROOT 		= new URL(CURRENT_URL).getProtocol() + "://" + new URL(CURRENT_URL).getHost();
		
		this.URL_PATH 		= removeFile(new URL(CURRENT_URL).getPath());
		
	}

	public Set<String> extractURLs() {
		
		/*
		 * The matched string
		 * Used to trim and manipulate the matched string, also used for comparing
		 */
		
		String match;
		
		/*
		 * Pattern matches the HREF attribute
		 */
			
		Pattern patternHref = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
		
		Matcher matcherHref = patternHref.matcher(DOCUMENT.toString());
						
		while(matcherHref.find()) {
			
			/*
			 * Store the matched string "the extracted href attribute" in match to make it easier to work on it
			 */
			
			match = matcherHref.group().toLowerCase();
			
			/*
			 * Remove href= and the single or double quotes around the URL as well white spaces 
			 */
			
			match = match.replace("href=", "").replaceAll("'", "").replaceAll("\"", "").trim();
			
			/*
			 * Don't add URL starting with double slashes
			 * Don't add a link for sending email
			 */
			
			if(match.startsWith("//") || match.contains("mailto:")) continue;
			
			/*
			 * Don't add any URL that directs to external websites
			 */
			
			if(match.contains("http") && !match.startsWith(CURRENT_URL)) continue;
			
			/*
			 * Don't add URL directs to the home page 
			 */
			
			if(match.equals("/")) continue;
			
			/*
			 * Remove dot-segments
			 */
			
			while(match.contains("../")) match = match.substring(0, match.indexOf("../")) + match.substring(match.indexOf("../")+3, match.length());
			
			while(match.contains("./")) match = match.substring(0, match.indexOf("./")) + match.substring(match.indexOf("./")+2, match.length());
			
			/*
			 * Remove any URL parameters
			 */
			
			if(match.contains("?"))  match = match.substring(0, match.indexOf("?"));
			
			/*
			 * Remove tags from URL
			 */
			
			if(match.contains("#")) match = match.substring(0, match.indexOf("#"));
			
			/* 
			 * Remove default index page file from URL
			 */
			
			for(int i=0; i<DEFAULT_INDEX_PAGE_NAMES.length; i++) {
				
				if(match.endsWith(DEFAULT_INDEX_PAGE_NAMES[i])) {
					
					match = match.substring(0, match.indexOf(DEFAULT_INDEX_PAGE_NAMES[i]));
					
					break;
					
				}
				
			}
			
			/*
			 *	Remove trailing slash to avoid adding two pages directing to the same page
			 *	For example: http://example.com/about
			 *				 http://example.com/about/
			 *	will direct to the same page but will be stored in the Set separately because they don't match, thus duplicate URL
			 */
			
			if(match.endsWith("/")) match = match.substring(0, match.length()-1);
			
			/*
			 *  Remove URL that directs to telephone number or JavaScript function or whatever else
			 *  If the URL doesn't have any slash that could mean also it is a relative URL,
			 *  thus is the check if the URL has one of the following special characters,
			 *  which are not usual for a URL directs to a HTML document
			 */
			
			if(!match.contains("/")){
				
				if(match.contains(":") || match.contains(";") || match.contains("(") || match.contains(")") || match.matches(" ")) continue;
				
			}
			
			/*
			 * 	If now there are nothing left in the URL, ignore it,
			 * 	otherwise we will get a URL to the home page in the following lines 
			 */
			
			if(match.equals("")) continue;
			
			/*
			 * 	If the URL is on the form of: http://example.com/path just add it to the set
			 */
			
			if(match.startsWith(URL_ROOT)) {
				
				URLsSet.add(match);
				
			}
			
			/*
			 *  If not, ensure that it will be on the form of: http://example.com/path
			 *  by converting the relative URL to an absolute one
			 */
			
			else {
				
				if(match.startsWith("/")) match = URL_ROOT + match;
				
				else match = URL_ROOT + URL_PATH + "/" + match;
				
				URLsSet.add(match);
			}
			
		}

		return URLsSet;
		
	}
	
	/*
	 * 	Remove the file name from the URL
	 */
	
	public String removeFile(String url) {
		
		for(int i=0; i<DEFAULT_HTML_EXTENSIONS.length; i++) {
			
			if(url.endsWith(DEFAULT_HTML_EXTENSIONS[i])) {
				
				String[] splittedURL = url.split("/");
				
				url = "";
				
				for(int j=0; j<splittedURL.length-1; j++) {
					if(!splittedURL[j].equals("")) url += "/" + splittedURL[j];
				}
				
				return url;
				
			}
			
		}
		
		return url;
		
	}
	
	public int getNumOfURLs() {
		return URLsSet.size();
	}
}

