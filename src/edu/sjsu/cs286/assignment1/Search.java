package edu.sjsu.cs286.assignment1;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeSet;


public class Search {

	/**
	 * Loads the PageId - Page Title file into memory.
	 * 
	 * @param fileName The filename
	 * @return The in memory representation of the file.
	 */
	public static HashMap<String, String> readIntoMemory( String fileName ) {
		
		long start = System.currentTimeMillis();
		
		System.out.println("Loading PageId-PageTitle into memory.");

		HashMap<String, String> assoc = new HashMap<String, String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String line;
		try {
			while ((line = br.readLine()) != null ) {
				// process the line.

				String[] lineStrs = line.split("\t");

				assoc.put( lineStrs[0] , lineStrs[1] );
			}

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Loaded PageId-PageTitle into memory. Time:" + (System.currentTimeMillis() - start) + " ms");

		return assoc;		

	}


	public static void main(String[] args) {
		
		final String stopWordsFilePath = "stopwords.txt";
		final String pageId_PageTitlePath = "RawIndex\\PageId_PageTitle\\PageId_PageTitle.txt";
		
		File stopWordsFile = new File(stopWordsFilePath);
		if(!stopWordsFile.exists()) {
			System.out.println("File 'stopwords.txt' not found in current directory.");
			System.exit(-1);
		}
		
		File pageId_PageTitleFile = new File(pageId_PageTitlePath);
		if(!pageId_PageTitleFile.exists()) {
			System.out.println("File 'RawIndex\\PageId_PageTitle\\PageId_PageTitle.txt' not found in current directory.");
			System.exit(-1);
		}

		long startTime = System.currentTimeMillis();

		System.out.println("Started. " + startTime);
		HashMap<String, String> assoc = readIntoMemory("RawIndex\\PageId_PageTitle\\PageId_PageTitle.txt");

		System.out.println("Loaded. " + (System.currentTimeMillis() - startTime)/1000);
		
		Tokenizer t = new Tokenizer(stopWordsFilePath);

		String fileName = "MergeIndex\\invertedIndex.txt";
		String searchQuery = "";//"james halstead the";

		while( true ) {

			BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
			System.out.println();
			System.out.println("------------------------------------------------");
			System.out.print("Enter your search Query (Q to quit):");

			try {
				searchQuery = br2.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				continue;
			}
			
			long searchStartTime = System.currentTimeMillis();

			searchQuery = searchQuery.toLowerCase();

			if(searchQuery.equals("q")) {
				System.out.println("Bye !");
				break;
			} else if (searchQuery.isEmpty()) {
				System.out.println("Empty search quesry detected.");
				continue;
			}

			HashMap<String, Integer> resultDocs = new HashMap<String, Integer>();

			

			TreeSet<String> tokSet = t.tokenize2(searchQuery);

			int size = tokSet.size();


			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String line;
			try {
				while ((line = br.readLine()) != null && tokSet.size() > 0) {
					// process the line.

					String[] lineStrs = line.split("\t");

					if(tokSet.contains(lineStrs[0])) {

						for(int i = 1; i<lineStrs.length; i++) {

							if(resultDocs.containsKey(lineStrs[i])) {

								resultDocs.put(lineStrs[i], resultDocs.get(lineStrs[i])+1);

							} else {

								resultDocs.put(lineStrs[i], 1);

							}

						}

						tokSet.remove(lineStrs[0]);

					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			

			System.out.println("Search results for the query '" + searchQuery + "':");
			
			int count = 0;

			for(String doc : resultDocs.keySet()) {

				if(resultDocs.get(doc) >= size) {

					System.out.println(makeURL(assoc.get(doc)));
					count++;
				}

			}
			
			System.out.println("Search completed. Time:" + (System.currentTimeMillis() - searchStartTime) + " ms");
			System.out.println("No. of search results. " + count);

		}

	}

	/**
	 * Make URL for the given Page Title
	 * 
	 * @param pageTitle The page title
	 * @return The page URL
	 */
	private static String makeURL( String pageTitle ) {

		String url = "http://en.wikipedia.org/wiki/" + pageTitle.replaceAll(" ", "_") ;

		return url;

	}

}
