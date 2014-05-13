package edu.sjsu.cs286.assignment3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PageRanksStep2 {

	static int pageTitleIndex = -1;
	static String[] pageTitles = new String[14128976];

	static int[] outlinksCount = new int[14128976];
	static int[] inlinksCount = new int[14128976];

	static long[] outlinkPointers = new long[14128976];
	static long[] inlinkPointers = new long[14128976];
	static double[] pageRanks;
	static double[] newPageRanks;

	static int[][] inlinks = new int[14128976][];

	static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Complete";
	//static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Sample";
	static String inLinkDir = baseDir + "\\inlinks";
	static String outLinkDir = baseDir + "\\outlinks";
	static String inLinkMergedDir = inLinkDir + "\\merged"; 

	static String inlinksCountFileName = baseDir + "\\inlinksCount.txt";
	static String outlinksCountFileName = baseDir + "\\outlinksCount.txt";

	static String inlinksPointersFileName = baseDir + "\\inlinkPointers.txt";
	static String outlinksPointersFileName = baseDir + "\\outlinkPointers.txt";

	private static void loadPageTitles() throws IOException {

		long count = 0;

		String line;

		// Load PageTitles
		BufferedReader br = new BufferedReader(new FileReader(new File(baseDir + "\\pageTitles.txt")));

		while( (line = br.readLine()) != null) {

			pageTitleIndex++;
			pageTitles[pageTitleIndex] = line;

			count ++;

			if(count % 100000 == 0) {
				System.out.println("PageTitles:" + count);
			}

		}

		br.close();

		System.out.println("PageTitles Total:" + count);

	}

	private static void loadOutlinkCount() throws NumberFormatException, IOException {

		long count = 0;
		String line;

		int tempIndex = -1;

		// Load outlinks count
		BufferedReader br = new BufferedReader(new FileReader( outlinksCountFileName ));

		while( (line = br.readLine()) != null) {

			tempIndex++;
			outlinksCount[tempIndex] = Integer.parseInt(line);

			count ++;

			if(count % 100000 == 0) {
				System.out.println("Outlinks:" + count);
			}

		}

		br.close();

		System.out.println("Outlinks Total:" + count);

	}

	private static void loadInlinkCount() throws NumberFormatException, IOException {

		int count = 0;

		int tempIndex = -1;
		String line;

		// Load inlinks count
		BufferedReader br = new BufferedReader(new FileReader( inlinksCountFileName ));

		while( (line = br.readLine()) != null) {

			tempIndex++;
			inlinksCount[tempIndex] = Integer.parseInt(line);

			count ++;

			if(count % 100000 == 0) {
				System.out.println("Inlinks:" + count);
			}

		}

		br.close();

		System.out.println("Inlink Total:" + count);

	}

	private static void createAndWriteOutlinkPointers() throws IOException {

		int count = 0;

		// Storing outlink pointers
		File outlinkFileDir = new File(outLinkDir);

		File[] outlinkFiles = outlinkFileDir.listFiles();

		for( File outlinkFile : outlinkFiles ) {

			count = 0;
			System.out.println(outlinkFile.getAbsolutePath());

			RandomAccessFile raf = new RandomAccessFile(outlinkFile, "r");

			long lastFilePointerPosition = raf.getFilePointer();
			String outlinkLine;

			while ( (outlinkLine = raf.readLine()) != null) {

				String[] list = outlinkLine.split("\t");

				Integer index = Integer.parseInt( list[0] );
				outlinkPointers[index] = lastFilePointerPosition;		

				lastFilePointerPosition = raf.getFilePointer();

				count++;
				if(count % 10000 == 0){
					System.out.println(count);
				}
			}

			raf.close();

		}

		System.out.println("Writing outlink pointers");
		// Write the outlink pointers to file

		PrintWriter pw = new PrintWriter( outlinksPointersFileName );

		for(int i=0;i<=pageTitleIndex;i++) {

			pw.println(outlinkPointers[i]);
			pw.flush();

		}

		pw.close();

	}

	private static void createAndWriteInlinkPointers() throws IOException {

		int count =0;

		// Storing inlink pointers
		File inlinkFileDir = new File(inLinkMergedDir);

		File[] inlinkFiles = inlinkFileDir.listFiles();

		for( File inlinkFile : inlinkFiles ) {

			count = 0;
			System.out.println(inlinkFile.getAbsolutePath());

			RandomAccessFile raf = new RandomAccessFile(inlinkFile, "r");

			long lastFilePointerPosition = raf.getFilePointer();
			String inlinkLine;

			while ( (inlinkLine = raf.readLine()) != null) {

				String[] list = inlinkLine.split("\t");

				Integer index = Integer.parseInt( list[0] );
				inlinkPointers[index] = lastFilePointerPosition;		

				lastFilePointerPosition = raf.getFilePointer();

				count++;
				if(count % 10000 == 0){
					System.out.println(count);
				}
			}

			raf.close();

		}

		// Write the inlink pointers to file
		System.out.println("Writing inlink pointers");

		PrintWriter pw = new PrintWriter( inlinksPointersFileName );

		for(int i=0;i<=pageTitleIndex;i++) {

			pw.println(inlinkPointers[i]);
			pw.flush();

		}

		pw.close();

	}

	private static void loadOutlinkPointers() throws IOException {

		System.out.println("Loading Outlink pointers");
		int count = 0;

		BufferedReader br = new BufferedReader(new FileReader(outlinksPointersFileName));
		String line;

		int tempIndex = -1;

		while( (line = br.readLine()) != null) {
			tempIndex++;
			outlinkPointers[tempIndex] = Long.parseLong(line);

			count++;
			if(count % 100000 == 0) {
				System.out.println("OutlinkPointer load:" + count);
			}
		}

		br.close();

		System.out.println("OutlinkPointer Total:" + count);

	}

	private static void loadInlinksIntoMemory() throws IOException {
		System.out.println("Loads inlinks into memory");



		File file = new File(inLinkMergedDir);

		File[] files = file.listFiles();

		for(File file1 : files) {

			if(file1.isDirectory()) {
				continue;
			}

			System.out.println(file1.getAbsolutePath());
			long count = 0;

			BufferedReader br = new BufferedReader(new FileReader(file1));
			String line;

			while( (line = br.readLine()) != null) {

				String[] elems = line.split("\t");

				Integer pid = Integer.parseInt(elems[0]);

				inlinks[pid] = new int[(inlinksCount[pid])];

				int index = 0;
				for(int i =1; i<elems.length;i++) {
					inlinks[pid][index] = Integer.parseInt(elems[i]);
					index++;
				}

				count++;
				if(count % 10000 ==0) {
					System.out.println(count);
				}
			}

			br.close();

		}
	}

	private static void loadInlinkPointers() throws IOException {

		System.out.println("Loading Inlink pointers");
		int count = 0;

		BufferedReader br = new BufferedReader(new FileReader(inlinksPointersFileName));
		String line;

		int tempIndex = -1;

		while( (line = br.readLine()) != null) {
			tempIndex++;
			inlinkPointers[tempIndex] = Long.parseLong(line);

			count++;
			if(count % 100000 == 0) {
				System.out.println("InlinkPointer load:" + count);
			}
		}

		br.close();

		System.out.println("InlinkPointer Total:" + count);

	}

	public static void main(String[] args) throws IOException {

		Arrays.fill(outlinkPointers, -1);
		Arrays.fill(inlinkPointers, -1);

		loadPageTitles();
		loadOutlinkCount();
		loadInlinkCount();

		// Create pointers - one time. Next time load
		//createAndWriteOutlinkPointers();
		//createAndWriteInlinkPointers();

		//	loadOutlinkPointers();
		//loadInlinkPointers();
		loadInlinksIntoMemory();

		System.out.println("Done");

		//System.exit(-1);

		pageRanks = new double[14128976];
		newPageRanks = new double[14128976];


		initPageRanks();

		System.out.println("Computing PageRank");
		int count = 0;
		double alpha = 0.01;
		
		double OneByN = 1/(double)pageTitleIndex;
		double alphaByN = (double)alpha/(double)pageTitleIndex;

		// First iteration of PageRanks
		for(int i=0;i<=pageTitleIndex;i++) {

			boolean[] pgVisited = new boolean[ pageTitleIndex + 1 ];
			Arrays.fill(pgVisited, false);

			double computedPg = 0D;

			// Fetch inlinks for this page


			int[] inlinksArray = inlinks[i];
			
			

			if(inlinksArray != null) {

				// For inlinks [(alpha/N) + (1 - alpha)/out(Page)] * PageRank(Page)
				for(int j=0;j<inlinksArray.length;j++) {

					Integer inlink = inlinksArray[j];//Integer.parseInt(inlinks[j]);

					//computedPg += ((((double)alpha/(double)pageTitleIndex) + ((double)(1 - alpha)/(double)outlinksCount[inlink])) * (double)pageRanks[inlink]);
					computedPg += ((alphaByN + ((double)(1 - alpha)/(double)outlinksCount[inlink])) * (double)pageRanks[inlink]);

					// Visit the page
					pgVisited[inlink] = true;

				}
			}

			// For the remaining pages
			for(int j=0;j<pgVisited.length;j++) {

				if(pgVisited[j]) {
					continue;
				}

				if( outlinksCount[j] > 0 ) {	// For pages that have atleast 1 outlink [ alpha/N ]
					//computedPg += ((alpha/(double)pageTitleIndex) * pageRanks[j]);
					computedPg += (alphaByN * pageRanks[j]);
				} else if( outlinksCount[j] == 0 ) { // For pages with no outlinks [ 1/N ]
					//computedPg += ((1/(double)pageTitleIndex) * pageRanks[j]);
					computedPg += (OneByN * pageRanks[j]);
				}	
			}

			// Ok now we have the PageRank for this page. Store in the new PageRanks array
			newPageRanks[i] = computedPg ;


			count ++;

			//if(count % 500 == 0) {
				System.out.println("PageRank Computation:" + count);
			//}
		}

		System.out.println("PageRank computation completed. Calculatiing summation.");
		double sum1 = 0D;
		double sum2 = 0D;

		for(int i=0; i<=pageTitleIndex;i++) {
			sum1 += pageRanks[i];
			sum2 += newPageRanks[i];
		}

		System.out.println("Difference = " + (double)(sum2-sum1));

	}

	private static void initPageRanks() {

		double defaultPageRank = ((double)1/(double)pageTitleIndex);
		Arrays.fill(pageRanks, defaultPageRank);

	}


}
