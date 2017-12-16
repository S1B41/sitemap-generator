package com.sibaihm.sitemapgenerator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class HTTPConnection {
	
	private final String USER_AGENT 		= "Mozilla/48.0";
	
	private final String CONTENT_TYPE		= "application/x-www-form-urlencoded";
	
	private final int CONNECTION_TIMEOUT 	= 2000;
	
	public InputStream makeRequest(String url) {
		
		try {
			URL targetURL = new URL(url);
			
			HttpURLConnection httpConn;
			
			/*
			 * 	Create the connection object
			 */
			
			switch (targetURL.getProtocol()) {
			
			case "http":
				
				httpConn = (HttpURLConnection) targetURL.openConnection();
				
				break;
				
			case "https":
				
				httpConn = (HttpsURLConnection) targetURL.openConnection();
				
				break;
				
			default:
				
				return null;
				
			}
			
			httpConn.setRequestProperty("Content-Type", CONTENT_TYPE);
			
			httpConn.setRequestProperty("User-Agent", USER_AGENT);
			
			httpConn.setConnectTimeout(CONNECTION_TIMEOUT);
			
			int responseCode = httpConn.getResponseCode();
			
			if(responseCode == HttpURLConnection.HTTP_OK) {
				
				return httpConn.getInputStream();
				
			}
			
			/*
			 * 	If the requested URL has moved,
			 * 	or there are multiple choices
			 * 	re-invoke the method with the new location
			 */
			
			else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
					|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_MULT_CHOICE) {
				
				if(httpConn.getHeaderField("Location") != null) {
					
					makeRequest(httpConn.getHeaderField("Location"));
					
				}
			    
			    return null;
			    
			}
			
			/*
			 * 	For any other response
			 */
			
			else return null;
			
			
		} catch (Exception e) {

			return null;
			
		}
	}
}
