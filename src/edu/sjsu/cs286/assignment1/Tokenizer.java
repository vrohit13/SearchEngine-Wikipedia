package edu.sjsu.cs286.assignment1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tokenizer {
	
	HashSet<String> stopWordsList;
	String stopWordsFilePath;
	
	long tokenCount = 0;	// Total no. of tokens encountered (This counts even duplicates)
	
	public Tokenizer(String stopWordsFilePath) {
		this.stopWordsFilePath = stopWordsFilePath;
		stopWordsList = new HashSet<String>();		
		loadStopWords();
		
	}
	
	public void loadStopWords() {
		
		System.out.println("Loading Stop Words.");
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(this.stopWordsFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String line;
		try {
			while ((line = br.readLine()) != null ) {
				// process the line.
				
				stopWordsList.add(line);
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Loaded Stop Words.");
		
	}
	
	public long getTokenCount() {
		return tokenCount;
	}
	
	public TreeSet<String> tokenize2(String inputString) {
		
		TreeSet<String> generatedStrings = new TreeSet<String>();
		
		inputString = inputString.replaceAll("[^A-Za-z0-9]", " ");
		
		String[] tokens = inputString.split(" ");
		
		for(String token : tokens) {
			
			token = token.trim();
			token = token.toLowerCase();
			
			if( Pattern.matches("^[^a-zA-Z0-9]{1,}$", token) ) {	// Ignore all tokens having only special characters
				
			} else /*if(token.length() > 2)*/ {	// Ignore tokens of size less than 3

				if( !stopWordsList.contains(token) && !token.isEmpty()) {	// Ignore StopWords
					
					generatedStrings.add(token);
					tokenCount ++;
					
				}	
			}
			
		}
		
		//System.out.println("Tokens: " + generatedStrings.toString());
		
		return generatedStrings ;
		
	}
	
	public TreeSet<String> tokenize(String inputString) {
		
		TreeSet<String> generatedStrings = new TreeSet<String>();
		
		// Split on new line		
		String[] lineStrings = inputString.split("\\n") ;
		
		for(String lineString : lineStrings) {
			
			// Replace [[ ]] {{ }} with space
			// Replace , with space
			lineString = lineString.replaceAll("\\[\\[", " ").replaceAll("\\]\\]", " ")
					.replaceAll("\\{\\{", " ").replaceAll("\\}\\}", " ")
					.replaceAll("\\,", " ").replaceAll("\\|", " ").replace("=", " ")//.replaceAll(":", " ") // Do not split on :
					.replaceAll("&nbsp;", " ").replaceAll("-->", " ").replaceAll("#", " ");
			
			// Replace . with space only if it matches the pattern \\.\s
			
			lineString = lineString.replaceAll("\\.\\s", " ") ;
			
			// Replace the pattern \\<[A-Za-z]{1,}\\> with empty space
			// Replace the pattern \\<\\/[A-Za-z]{1,}\\> with empty space
			lineString = lineString.replaceAll("\\<[A-Za-z]{1,}\\>{0,1}", " ") ;
			lineString = lineString.replaceAll("\\<\\/[A-Za-z]{1,}\\>{0,1}", " ") ;
			
			// Now split on space
			String[] strOnSpaces = lineString.split( "\\s" );
			
			for( String strOnSpace : strOnSpaces) {
				
				
				// Trim the string ( Remove leading and trailing white spaces)
				strOnSpace = strOnSpace.trim();
				
				
				// Trim the leading and trailing special characters out of each string
				Matcher m = Pattern.compile( "^^[^A-Za-z0-9]{0,}([A-Za-z0-9]{1}[A-Za-z0-9\\-\\'\\.]{0,}[A-Za-z0-9]{1})[^A-Za-z0-9\\-]{0,}$" ).matcher( strOnSpace );
				
				if(m.matches()) {
					strOnSpace = m.group(1);
				}
				
				
				// If string consists of only contiguous = signs or contiguous alphabets in the form ===Rail===, extract the word out of it. 
				// The word can contain spaces. Recurse
				if(Pattern.matches("^=+(.*)=+$", strOnSpace)) {
					
					generatedStrings.addAll( tokenize( strOnSpace.replaceAll("=", " ")));
					
					// Continue to the next iteration, since recursion would have already covered the subsequent steps 
					continue;
					
				} 
				
				
				// Match a URL
				String urlPattern = "^[^A-Za-z0-9]{0,}((http(s){0,1}:){0,1}//([A-Za-z0-9\\-\\_\\.\\%]{1,}\\/){0,}[A-Za-z0-9\\.\\-\\_\\?\\+\\%]{1,})[^A-Za-z0-9]{0,}$";
				
				Matcher urlMatcher = Pattern.compile(urlPattern).matcher(strOnSpace);
				
				if(urlMatcher.matches()) {
					/*
					 * //colleges.usnews.rankingsandreviews.com/best-colleges/rankings/national-universities/page+29
					 * //colleges.usnews.rankingsandreviews.com/best-colleges/rankings/national-universities/top-public/spp%2b50
					 * //commerce.alabama.gov/content/media/publications/transportation/transportationinalabama.pdf
					 */
					
					// If the string matches some kind of url (without http), replace // and / with whitespace characters
					generatedStrings.addAll( tokenize( urlMatcher.group(1).replaceAll("//", " ").replaceAll("\\/", " ").replaceAll("\\?", " ")
							.replaceAll("\\:", " ")
							.replaceAll("%[0-9A-Z]{1,}", " ") )); // Replace URL encoding
					
					// Continue to the next iteration, since recursion would have already covered the subsequent steps 
					continue;
					
				}
				
				
				// If the string consists of only integers - IGNORE
				if(Pattern.matches("^[0-9]{1,}$", strOnSpace)) {
					
				} else if( Pattern.matches("^[^A-Za-z0-9]{1,}$", strOnSpace) ) { // If the string consists of only special characters - IGNORE
					
				} else if( Pattern.matches("^[^a-zA-Z]{1,}$", strOnSpace) ) { // If the string consists of only special characters and numbers - IGNORE
					
				} else {
					
					if(strOnSpace.length() > 2) {	// Ignore tokens of size less than 3

						generatedStrings.add(strOnSpace.toLowerCase());
					}
				}
				
			}
			
			
		}
	
		
		return generatedStrings;
		
		
	}

}
