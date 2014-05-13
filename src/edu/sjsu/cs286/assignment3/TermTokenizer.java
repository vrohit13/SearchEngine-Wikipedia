package edu.sjsu.cs286.assignment3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class TermTokenizer {
	
	HashSet<String> stopWordsList;
	String stopWordsFilePath;
	Integer wc = 0;
	
	public TermTokenizer(String stopWordsFilePath) {
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
	
	public HashMap<String, Integer> tokenizeAndComputeTF(String inputString) {
		
		wc = 0;
		
		HashMap<String, Integer> generatedStrings = new HashMap<String, Integer>();
		
		inputString = inputString.replaceAll("[^A-Za-z0-9]", " ");
		
		String[] tokens = inputString.split(" ");
		
		for(String token : tokens) {
			
			token = token.trim();
			token = token.toLowerCase();
			
			if( Pattern.matches("^[^a-zA-Z0-9]{1,}$", token) ) {	// Ignore all tokens having only special characters
				
			} else if(token.length() > 2) {	// Ignore tokens of size less than 3

				if( !token.isEmpty() && !stopWordsList.contains(token) ) {	// Ignore StopWords
									
					if( generatedStrings.containsKey(token)) {
						generatedStrings.put(token, generatedStrings.get(token) + 1);
					}
					else {
						generatedStrings.put(token, 1);
					}
					
					wc++;
					
				}	
			}
			
		}
		
		return generatedStrings ;		
	}
	
	public Integer getWC() {
		return wc;
	}

}
