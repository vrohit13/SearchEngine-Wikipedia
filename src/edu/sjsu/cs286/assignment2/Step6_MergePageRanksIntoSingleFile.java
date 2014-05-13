package edu.sjsu.cs286.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class Step6_MergePageRanksIntoSingleFile {

	public static void main(String[] args) {
		
		String baseDir = Common.baseDir;//"C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\Sample";
		
		String dir = baseDir + "\\PageRank\\First" ;
		String mergedPageRanksFileName = baseDir + "\\PageRank\\PageRanks.txt";
		PrintWriter outPw = null;
		try {
			outPw = new PrintWriter(mergedPageRanksFileName);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		File f = new File(dir);
		
		File[] files = f.listFiles();
		
		for(File file : files) {
	
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));

				String line;

				while( (line = br.readLine()) != null) {

					outPw.println(line);
					outPw.flush();

				}

				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		outPw.close();
	}
}
