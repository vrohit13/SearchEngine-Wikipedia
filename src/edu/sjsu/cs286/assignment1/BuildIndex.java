package edu.sjsu.cs286.assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeSet;

public class BuildIndex {
	
	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		long count = 0;
		
		HashMap<String, TreeSet<String>> invertedIndex = new HashMap<String, TreeSet<String>>();
		
		String indexDir = "RawIndex";
		String outDir = "MergeIndex";
		
		
		if(args.length > 0) {
			if(args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println("Build Index using the given Fragmented Index Help");
				System.out.println("The directory 'RawIndex' shoudl already be present within the current directory.");
				System.exit(-1);
			}
		} 	
		
		if(!new File(indexDir).exists()) {
			System.out.println("Fragmented Index directory not found. " + indexDir );
			System.exit(-1);
		}
		
		File outFile = new File(outDir);
		outFile.mkdirs();
		
		System.out.println("Reading RawIndex. " + startTime);
		
		PrintWriter pw = null ;
		
		try {
			pw = new PrintWriter( outDir + "\\invertedIndex.txt" );
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		File f = new File(indexDir);
		
		File[] files = f.listFiles();
		
		for(File file : files) {
			
			long fileStartFile = System.currentTimeMillis();
			
			if(file.isDirectory()) {
				continue;
			}
			
			System.out.println("Processing file '" + file.getAbsolutePath() + "'.");
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String line;
			
			try {
				while ((line = br.readLine()) != null) {
					
					String[] lineTokens = line.split("\t");
					
					String word = lineTokens[0];
					String pageID = lineTokens[1];
					
					TreeSet<String> postingList = null ;
					
					if(invertedIndex.containsKey( word )) {
						postingList = invertedIndex.get(word);
					} else {
						postingList = new TreeSet<String>();
						invertedIndex.put( word ,  postingList );
					}
					
					postingList.add( pageID );
		
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
			
			// At this point the inverted index has been built for this file. Write to the new file.
			count += invertedIndex.size();
			
			for(String word: invertedIndex.keySet()) {
				
				TreeSet<String> postingList = invertedIndex.get(word);
				
				StringBuilder st = new StringBuilder();
				
				st.append(word);
				st.append("\t");
				
				for(String listItem : postingList) {
					
					st.append(listItem);
					st.append("\t");
					
				}
				
				pw.println(st);
				pw.flush();
				
				st.setLength(0);
				
				
			}
			
			//Inverted Index written to file.
			invertedIndex.clear();
			long end = System.currentTimeMillis();
			System.out.println("Done processing file '" + file.getAbsolutePath() + "'. " + (end - fileStartFile) + " ms");
		} 
		
		pw.close();
		System.out.println("Created Merged Index. Time:" + (System.currentTimeMillis() - startTime) + " ms");
		System.out.println("Total no. of unique tokens:" + count);		
	}

}
