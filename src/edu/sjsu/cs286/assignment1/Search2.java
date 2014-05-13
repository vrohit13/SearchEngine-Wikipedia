package edu.sjsu.cs286.assignment1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Search2 {
	
	public static HashMap<String, ArrayList<String>> readIntoMemory( String fileName ) {
		
		System.out.println("Loading index into memory.");
		
		HashMap<String, ArrayList<String>> invertedIndex = new HashMap<String, ArrayList<String>>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String line;int cnt =0;
		try {
			while ((line = br.readLine()) != null ) {
				// process the line.
				
				String[] lineStrs = line.split("\t");
				
				String word = lineStrs[0];
				
				ArrayList<String> postingList = new ArrayList<String>();
				
				for(int i=1;i<lineStrs.length;i++) {
					
					postingList.add( lineStrs[i] );
					
				}
				
				invertedIndex.put( word , postingList );
				cnt++;
				
				if(cnt%1000 == 0){
					System.out.println("Loaded:" + cnt);
				}
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Loaded index into memory.");
		
		return invertedIndex;
		
		
	}
	
	
	
	public static void main(String[] args) {

		String fileName = "MergeIndex\\invertedIndex.txt";
		String searchQuery = "james halstead the";
		
		HashMap<String, ArrayList<String>> invertedIndex = readIntoMemory( fileName );
		
		searchQuery = searchQuery.toLowerCase();
		
		HashMap<String, Integer> resultDocs = new HashMap<String, Integer>();
		
		Tokenizer t = new Tokenizer("stopwords.txt");
		TreeSet<String> tokSet = t.tokenize2(searchQuery);
		
		int size = tokSet.size();

		for(String token : tokSet) {
			
			if(invertedIndex.containsKey(token)){
				
				// Fetch the posting list
				ArrayList<String> postingList = invertedIndex.get(token);
				
				for(String postingListItem : postingList){

					if(resultDocs.containsKey(postingListItem)) {

						resultDocs.put(postingListItem, resultDocs.get(postingListItem)+1);

					} else {

						resultDocs.put(postingListItem, 1);

					}
				}

			} else {
				System.out.println("Search term not found in index. " + token);
			}
			
		}
		
		System.out.println("Search results for the query '" + searchQuery + "':");
		
		for(String doc : resultDocs.keySet()) {
			
			if(resultDocs.get(doc) >= size) {
				
				System.out.println(makeURL(doc));
				
			}
			
		}
//		
//		for(String doc : resultDocs.keySet()) {
//			
//			if(resultDocs.get(doc) == 1) {
//				
//				System.out.println(doc);
//				
//			}
//			
//		}



	}
	
	private static String makeURL( String pageTitle ) {
		
		String url = "http://en.wikipedia.org/wiki/" + pageTitle.replaceAll(" ", "_") ;
		
		return url;
		
	}
	
	

}
