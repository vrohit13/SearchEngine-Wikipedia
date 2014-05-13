package edu.sjsu.cs286.assignment2;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkTokenizer {

	public HashSet<String> tokenize( String text ) {
		
		HashSet<String> outlinks = new HashSet<String>();

		String[] lines = text.split("\\n");

		for(String line : lines) {

			// Process [[Page Titles]]

		//	String pattern = "\\[\\[([A-Za-z0-9\\_\\.\\-\\s]{1,})(\\|{1}[A-Za-z0-9\\_\\.\\-]{1,}){0,1}\\]\\]";
			String pattern = "\\[\\[([^\\]\\]]{1,})\\]\\]";
			Matcher m = Pattern.compile(pattern/*Common.linkPattern*/).matcher(line);

			while(m.find()) {
				// Collect the match.
				outlinks.add(m.group(1));
			}

//			// Process other links http://en.wikipedia.org/
//			String urlPattern = "http:////en.wikipedia.org//.*\\s{0,1}/{0,1}";
//
//			m = Pattern.compile(urlPattern).matcher(line);
//
//			while(m.find()) {
//				// Collect the match
//			}
			
			

		}
		
		return outlinks;

	}

}
