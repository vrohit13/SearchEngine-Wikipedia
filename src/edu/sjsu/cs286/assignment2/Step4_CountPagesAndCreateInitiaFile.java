package edu.sjsu.cs286.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Step4_CountPagesAndCreateInitiaFile {

	public static void main(String[] args) {


		String baseDir = Common.baseDir;//"C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\Sample";
		String fileName = baseDir + "\\" + "PageId_PageTitle";

		String initilPGDir = baseDir + "\\" + "PageRank\\First";

		new File(initilPGDir).mkdirs();
		
//		PrintWriter pw1 = null;
//		try {
//			pw1 = new PrintWriter(initilPGDir + "\\first.txt");
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		HashMap<Character, PrintWriter> writers = new HashMap<Character, PrintWriter>();

		File f = new File(fileName);

		File[] files = f.listFiles();

		long pages = 0;

		// Count pages
		for(File file : files) {
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String line;

			try {
				while( (line = br.readLine()) != null) {

					pages++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}

		System.out.println(pages + " " + (double)((double)1/(double)pages)) ;


		files = f.listFiles();
		long counter = 0;

		// For each page encountered
		for(File file : files) {
			
			System.out.println(file.getAbsolutePath());

			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			String line;
			
			

			try {
				while( (line = br.readLine()) != null) {

					String[] tokens = line.split("\t");
					
					PrintWriter pw = null;
					
					if(writers.containsKey(tokens[0].charAt(0))) {
						pw = writers.get(tokens[0].charAt(0));
					} else {
						pw = new PrintWriter(initilPGDir + "\\" + tokens[0].charAt(0) + ".txt");
						writers.put(tokens[0].charAt(0), pw);
						
					}
					
					// Writing the probability or page rank for each page
					pw.println(tokens[0] + "\t" + (double) ((double)1/(double)pages));
					pw.flush();
					
					//pageRanks.put(Integer.parseInt(tokens[0]), (double) ((double)1/(double)pages));
					counter ++;
					
//					pw1.println(tokens[0] + "\t" + ((double)1/(double)pages));
//					pw1.flush();

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
			
		}
		
		//pw1.close();
		
		
		System.out.println(counter);

	}
}
