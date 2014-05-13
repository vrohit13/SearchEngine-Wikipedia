package edu.sjsu.cs286.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class Step3_MergeIntoLists {


	public static void main(String[] args) {
		
		merge(Common.inboundDir);
		merge(Common.outboundDir);

	}

	public static void merge(String fileName) {


		HashMap<String, HashSet<String>> entries = new HashMap<String, HashSet<String>>();

		HashMap<Character, PrintWriter> writers = new HashMap<Character, PrintWriter>();

		String out_filenameDir = fileName + "\\merged";

		new File(out_filenameDir).mkdirs();


		File f = new File(fileName);

		File[] files = f.listFiles();

		for(File file : files) {

			if(file.isDirectory()) {
				continue;
			}

			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String line;


			try {
				while((line = br.readLine()) != null) {

					String[] token = line.split("\t");

					HashSet<String> set = null;


					if(entries.containsKey(token[0])) {

						set = entries.get(token[0]);

					} else {

						set = new HashSet<String>();
						entries.put(token[0], set);

					}

					set.add(token[1]);			

				}

			}catch(Exception e) {
				e.printStackTrace();
			}

			// Write the values to file.

			for(String key : entries.keySet() ) {

				StringBuilder st = new StringBuilder();
				HashSet<String> set = entries.get(key);

				for(String setEntry : set) {

					st.append(setEntry);
					st.append("\t");
				}

				PrintWriter pw = null;

				if(writers.containsKey(key.charAt(0))) {
					pw = writers.get(key.charAt(0));
				} else {
					try {
						pw = new PrintWriter(out_filenameDir + "\\" + key.charAt(0) + ".txt");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					writers.put(key.charAt(0), pw);
				}

				pw.println(key + "\t" + st.toString());
				pw.flush();

			}	

			entries.clear();
		}


	}

}
