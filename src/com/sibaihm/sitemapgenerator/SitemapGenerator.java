package com.sibaihm.sitemapgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SitemapGenerator {

	/*
	 * The unique URLs of the web site
	 */
	
	private Set<String> uniqueURLs = new TreeSet<String>();
	
	/*
	 * This set stores all the extracted URLs
	 * used in if statement to prevent requesting the same URL again if the URL is found in other pages
	 */
	
	private Set<String> extURLs = new HashSet<>();
	
	/*
	 * The scheme and the host of the URL
	 */
	
	private final String ROOT_URL;
	
	/*
	 * The current URL that are being processed 
	 */
	
	private String currentURL;
	
	/*
	 * The object that stores the HTML document and performs the extraction of the URLs from it
	 */
	
	private HTMLDocument document;
	
	/*
	 * The constructor
	 */
	
	public SitemapGenerator(String URL_CURRENT) throws MalformedURLException {
		
		/*
		 * Remove the trailing slash if found and convert the string to lower case 
		 */
		
		this.currentURL = ROOT_URL = URL_CURRENT.endsWith("/") ? URL_CURRENT.substring(0,URL_CURRENT.length()-1).toLowerCase() : URL_CURRENT.toLowerCase();
	}
	
	/*
	 * Create the site map
	 */
	
	public Set<String> create() throws IOException {
		
		/*
		 * Create the connection object
		 * which is responsible for making the HTTP request to the specified URL and getting the response
		 */
		
		HTTPConnection httpConn = new HTTPConnection();
		
		/*
		 * Get the response
		 */
		
		InputStream inputStream = httpConn.makeRequest(currentURL);
		
		/*
		 * If there is an error
		 */
		
		if(inputStream == null) return null;
		
		/*
		 * If there is no error add the URL into the set
		 * If it is already in the set return
		 */
		
		if(!uniqueURLs.add(currentURL)) return null;
		
		/*
		 * Then read the response
		 */
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		String inputLine;
		
		StringBuffer HTMLResponse = new StringBuffer();
		
		while((inputLine = reader.readLine()) != null) HTMLResponse.append(inputLine);
		
		reader.close();
		
		/*
		 * Create HTMLDoc object
		 * which is responsible for storing the HTML content, extracting URLs from it,
		 * filtering the URLs and returning a set of valid unique URLs
		 */
		
		document = new HTMLDocument(HTMLResponse.toString(), currentURL);
		
		/*
		 * Extract the URLs in the page recursively using extractURLs method
		 */
		
		for(String extractedURL : document.extractURLs()) {
			
			/*
			 * Ignore not HTML files like .css, .js, images, or anything else
			 */
			
			if(!isHTMLFile(extractedURL)) continue;
			
			/*
			 * Ignore the URL if it is already added to extURLs set
			 */
			
			if(extURLs.contains(extractedURL)) continue;
			
			/*
			 * Or add it if it is not yet added
			 */
			
			else extURLs.add(extractedURL);
			
			/*
			 * Change the current URL to the new extracted one
			 */
			
			currentURL = extractedURL;
			
			/*
			 * Repeat the process
			 */
			
			create();
		}
		
		return uniqueURLs;
	}
	
	/*
	 * Print the found URLs 
	 */
	
	public void print() {
		
		for(String url : uniqueURLs) System.out.println(url);
	}
	
	/*
	 * Save the URLs into a file on desktop
	 */
	
	public void saveToFile() {
		
		try {
			
			final String DESKTOP_PATH = System.getProperty("user.home") + "\\Desktop\\";
			
			final String HOST_NAME = ROOT_URL.replace("http://", "").replace("https://", "");
			
			final String FILE_NAME = DESKTOP_PATH + HOST_NAME + ".txt";
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
			
			for(String url : uniqueURLs) {
				writer.write(url);
				writer.newLine();
			}
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Return the number of found URLs
	 */
	
	public int getNumOfURLs() {
		
		return uniqueURLs.size();
	}
	
	/*
	 * Check whether the path of the URL leads to a web page file
	 */
	
	private boolean isHTMLFile(String url) {
		
		Pattern patternWebPageExt = Pattern.compile("(?i)\\.(?!html|htm|php|php3|php4|php5|asp)[a-z]{2,4}$");
		
		Matcher matcherWebPageExt = patternWebPageExt.matcher(url);
		
		if(matcherWebPageExt.find()) return false;
		
		return true;
	}
	
	/*
	 * Check whether the path of the URL leads to a directory
	 * assumed that the path could be either a web page or directory
	 */
	
	private boolean isDirectory(String url) {
		
		Pattern patternWebPageExt = Pattern.compile("(?i)\\.(html|htm|php|php3|asp)$");
		
		Matcher matcherWebPageExt = patternWebPageExt.matcher(url);
		
		if(matcherWebPageExt.find()) return false;
		
		return true;
	}
}







