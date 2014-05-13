package edu.sjsu.cs286.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class Step5_PageRank {

	static String baseDir = Common.baseDir;//"C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\Sample";

	static String firstFileDir = baseDir + "\\PageRank\\First";
	static String secondFileDir = baseDir + "\\PageRank\\Second";
	static String inboundDir = baseDir + "\\inbound\\merged";
	static String outboundDir = baseDir + "\\outbound\\merged";
	static String pageId_PageTitle = baseDir + "\\" + "PageId_PageTitle";

	public static Double main(String[] args) {
		
		HashMap<Character, PrintWriter> secondWriters = new HashMap<Character, PrintWriter>();

		Double diff = 0D;

		Double sumA = 0D;
		Double sumB = 0D;

		Double alpha = 0.15D;
		Integer N = 110962;	

		HashSet<String> pageIds = loadPageIds();
		HashMap<Integer, Double> firstFileMap = loadPageFirstFile();
		HashMap<Integer, HashSet<Integer>> inboundFile = loadInboundFile();
		HashMap<Integer, HashSet<Integer>> outboundFile = loadOutboundFile();

		long processed = 0;
		
		new File(secondFileDir).mkdirs();

		// For each entry in first.txt:

		File firstFile = new File(firstFileDir);

		File[] firstFiles = firstFile.listFiles();

		for(File f : firstFiles) {

			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String line = null;

			try {
				while( (line = br.readLine()) != null) {

					String[] tokens = line.split("\t");

					String pageId = tokens[0];
					String pageRank = tokens[1];

					sumA += Double.parseDouble(pageRank);

					// Collect list of inbound id's
					HashSet<Integer> inboundList = inboundFile.get(Integer.parseInt(pageId));//getInboundList(pageId);
					
					if(inboundList == null) {
						inboundList = new HashSet<Integer>();
					}

					Double partA = 0D;

					// Calculate Part A
					// For each InLink for current node, 
					// alpha/N + (1-alpha)/OutDegree(Node)
					//System.out.println("Computing PartA");
					for(Integer inboundEntry : inboundList) {

						Double inBoundPageRank = firstFileMap.get(inboundEntry);//firstFileMap.get(Integer.parseInt(inboundEntry));//getPageRank(inboundEntry);

						partA += inBoundPageRank * ( alpha/N + (1- alpha)/outboundFile.get(inboundEntry).size() );//inBoundPageRank * ( alpha/N + (1- alpha)/getOutLinksCount(inboundEntry) ); 

					}

					//System.out.println("Computed PartA");

					//System.out.println("Computing PartB");
					// Calculate PartB
					Double partB = 0D;

					//System.out.println("Cloning");
					HashSet<String> pageIdsClone = (HashSet<String>)pageIds.clone();
					//System.out.println("Cloned");

					//System.out.println("Diffing");
					pageIdsClone.removeAll(inboundList);
					//System.out.println("Diffed");

					//System.out.println("Summing up. " + pageIdsClone.size());
					for(String pageIdNotInbound : pageIdsClone) {
						partB += firstFileMap.get(Integer.parseInt(pageIdNotInbound))/*getPageRank(pageIdNotInbound)*/ * alpha/N;
					}
					//System.out.println("Computed PartB");

					//					File pageId_PageTitleFile  = new File(pageId_PageTitle);
					//
					//					File[] files = pageId_PageTitleFile.listFiles();
					//
					//					for(File file : files) {
					//
					//						BufferedReader br1 = null;
					//						try {
					//							br1 = new BufferedReader(new FileReader(file));
					//						} catch (FileNotFoundException e) {
					//							// TODO Auto-generated catch block
					//							e.printStackTrace();
					//						}
					//
					//
					//						String line1;
					//
					//						while( (line1 = br1.readLine()) != null) {
					//
					//							String token[] = line.split("\t");
					//
					//							// All nodes that are not inlinks to the current node
					//							if( !inboundList.contains(token[0])) {
					//
					//								partB += getPageRank(token[0]) * alpha/N;
					//
					//							}
					//
					//						}
					//
					//						br1.close();
					//					}

					Double newPageRank = partA + partB;
					
					sumB += newPageRank;
					
					PrintWriter pw = null;
					
					if( secondWriters.containsKey(pageId.charAt(0))) {
						pw = secondWriters.get(pageId.charAt(0));
					} else {
						pw = new PrintWriter(secondFileDir + "\\" + pageId.charAt(0) + ".txt");
						secondWriters.put(pageId.charAt(0), pw);
					}
					
					pw.println(pageId + "\t" + newPageRank );
					pw.flush();

					processed++;
					//System.out.println("Processed");

					if(processed % 100 == 0) {
						System.out.println("Processed:" + processed);
					}

				}

				br.close();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		

		new File(firstFileDir).delete();
		new File(secondFileDir).renameTo(new File(firstFileDir));

		diff = sumA - sumB;

		System.out.println("Processed:" + processed);
		System.out.println("Diff:" + diff + " " + Math.abs(diff));

		
		return diff;
	}

	public static HashSet<String> getInboundList(String pageId) {



		HashSet<String> inboundList = new HashSet<String>();

		String fileName = inboundDir + "\\" + pageId.charAt(0) + ".txt" ;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

			String line;

			while( (line = br.readLine()) != null) {

				String tokens[] = line.split("\t");

				if(tokens[0].equals(pageId)) {

					// Collect remaining entries and put into set

					for(int i=1;i<tokens.length;i++) {
						inboundList.add(tokens[i]);
					}

					break;
				}
			}

			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		return inboundList;

	}

	public static Double getPageRank(String pageId) {

		Double pageRank = 0D;

		String fileName = firstFileDir + "\\" + pageId.charAt(0) + ".txt" ;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

			String line;

			while( (line = br.readLine()) != null) {

				String tokens[] = line.split("\t");

				if(tokens[0].equals(pageId)) {

					pageRank = Double.parseDouble(tokens[1]);

					break;
				}
			}

			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pageRank;
	}

	public static Integer getOutLinksCount(String pageId) {

		Integer outboundLinks = 0;
		//HashSet<String> outLinkList = new HashSet<String>();

		String fileName = outboundDir + "\\" + pageId.charAt(0) + ".txt" ;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

			String line;

			while( (line = br.readLine()) != null) {

				String tokens[] = line.split("\t");

				if(tokens[0].equals(pageId)) {

					outboundLinks = tokens.length - 1;

					break;
				}
			}

			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outboundLinks;
	}
	
	public static HashMap<Integer, HashSet<Integer>> loadOutboundFile() {
		
		System.out.println("Loading OutboundFile");
		HashMap<Integer, HashSet<Integer>> outboundFile = new HashMap<Integer, HashSet<Integer>>();
		
		File f = new File(outboundDir);
		
		File[] files = f.listFiles();
		
		for(File file : files) {
		
			System.out.println(file.getAbsolutePath());
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));

				String line;

				while( (line = br.readLine()) != null) {

					String tokens[] = line.split("\t");
					
					HashSet<Integer> list = new HashSet<Integer>();
					
					for(int i=1;i<tokens.length;i++) {
						list.add(Integer.parseInt(tokens[i]));
					}
					
					outboundFile.put(Integer.parseInt(tokens[0]), list);
				}

				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		System.out.println("Loaded OutboundFile");
		
		return outboundFile;
		
	}
	
	public static HashMap<Integer, HashSet<Integer>> loadInboundFile() {
		
		System.out.println("Loading Inbound file");
		
		HashMap<Integer, HashSet<Integer>> inboundFile = new HashMap<Integer, HashSet<Integer>>();
		
		File f = new File(inboundDir);
		
		File[] files = f.listFiles();
		
		for(File file : files) {
			
			System.out.println(file.getAbsolutePath());

			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));

				String line;

				while( (line = br.readLine()) != null) {

					String tokens[] = line.split("\t");
					
					HashSet<Integer> list = new HashSet<Integer>();
					
					for(int i=1;i<tokens.length;i++) {
						list.add(Integer.parseInt(tokens[i]));
					}
					
					inboundFile.put(Integer.parseInt(tokens[0]), list);
				}

				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
		System.out.println("Loaded Inbound file");
				
		return inboundFile;
	}
	
	public static HashMap<Integer, Double> loadPageFirstFile() {
		
		System.out.println("Loading FirstFile");
		HashMap<Integer, Double> firstFile = new HashMap<Integer, Double>();
		
		File f = new File(firstFileDir);
		
		File[] files = f.listFiles();
		
		for(File file: files) {

			System.out.println(file.getAbsolutePath());
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));

				String line;

				while( (line = br.readLine()) != null) {

					String tokens[] = line.split("\t");

					firstFile.put(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[1]));

				}

				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		System.out.println("Loaded FirstFile. " + firstFile.size());
		
		return firstFile;
		
	}

	public static HashSet<String> loadPageIds() {

		System.out.println("Loading PageIds");
		HashSet<String> pageIds = new HashSet<String>();

		File pageId_PageTitleFile  = new File(pageId_PageTitle);

		File[] files = pageId_PageTitleFile.listFiles();

		for(File file : files) {

			BufferedReader br1 = null;
			try {
				br1 = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			String line;

			try {
				while( (line = br1.readLine()) != null) {

					String token[] = line.split("\t");

					pageIds.add(token[0]);				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				br1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Loaded PageIds");

		return pageIds;
	}

}
