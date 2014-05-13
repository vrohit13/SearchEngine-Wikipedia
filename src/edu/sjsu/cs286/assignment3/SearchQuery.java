package edu.sjsu.cs286.assignment3;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class SearchQuery {

	static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Complete";	

	static final String fragmentedDir = baseDir + "\\FragmentedIndex";
	static String mergedDirPath = fragmentedDir + "\\merged";
	static String mergeFilePointerDirPath = mergedDirPath + "\\" + "mergepointers";
	static final String stopWordsFilePath = "stopwords.txt";

	static int pageTitleIndex = -1;
	static String[] pageTitles = new String[14128976];

	static int termIndex = -1;
	static String[] terms = new String[10000000];
	static long[] filePointers = new long[10000000];

	static RandomAccessFile[] rafs = new RandomAccessFile[10000000];

	public static void main(String[] args) throws IOException {

		loadPageTitles();
			createMetaData();
			System.exit(-1);

		loadMetaDataIntoMemory();

		// Now search

		System.out.println("Welcome to Hunter's search engine, the next good thing:");
		
		while(true){


			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter Search term:");
			String searchQuery = br.readLine();

			if(searchQuery.equalsIgnoreCase("q")) {
				System.out.println("Bye Bye");
				break;
			}

			performSearch(searchQuery);
		}
	}

	private static void performSearch(String searchQuery) throws IOException {
		
		boolean success = true;


		HashMap<Integer, Integer> resultDocs = new HashMap<Integer, Integer>();
		TreeMap<Integer, Double> tf_idf = new TreeMap<Integer, Double>();

		TreeSet<ResultItem> results = new TreeSet<ResultItem>(new ResultComparator());


		TermTokenizer t = new TermTokenizer(stopWordsFilePath);

		HashMap<String, Integer> map = t.tokenizeAndComputeTF(searchQuery);

		for(String term : map.keySet()) {

			// Fetch the posting list for the term

			HashMap<Integer, Double> postingList = getPostingList(term);

			if(postingList == null) {
				System.out.println("Term not found in the index. " + term);
				success = false;
				continue;
				//System.exit(-1);
			}

			for(Integer postingListItem : postingList.keySet()){

				if(resultDocs.containsKey(postingListItem)) {

					resultDocs.put(postingListItem, resultDocs.get(postingListItem)+1);

					double tfidf = (double)pageTitleIndex/(double)postingList.size();

					tfidf = tfidf * postingList.get(postingListItem);			
					tf_idf.put(postingListItem, tfidf + tf_idf.get(postingListItem) );


				} else {

					resultDocs.put(postingListItem, 1);

					double tfidf = (double)pageTitleIndex/(double)postingList.size();
					tfidf = tfidf * postingList.get(postingListItem);
					tf_idf.put(postingListItem, tfidf);

				}
			}		
		}
		
		if(!success) {
			System.out.println("Unable to search, as the input term(s) were not found in the index.");
			System.exit(-1);
		}

	/*	System.out.println("Search results for the query without tf-idf:'" + searchQuery + "':");

		for(Integer doc : resultDocs.keySet()) {

			if(resultDocs.get(doc) >= size) {

				System.out.println(makeURL(pageTitles[doc]));

				// Put this into the results.

				ResultItem r = new ResultItem();
				r.docId = doc;
				r.tfidf = tf_idf.get(doc);

				results.add(r);
			}
		}*/

		//System.out.println("Tf-idf included:");

		// Displays results in descending order of Tf-Idf
		for(ResultItem ri : results) {

			int pageId = ri.docId;
		//	System.out.println(ri.tfidf + "\t" + makeURL(pageTitles[pageId]));
			System.out.println( makeURL(pageTitles[pageId]));

		}
	}

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

	private static int getTermIndex( String term ) {
		return Arrays.binarySearch(terms, 0, termIndex, term);
	}

	public static HashMap<Integer, Double> getPostingList(String term) throws IOException {

		HashMap<Integer, Double> postingList = new HashMap<Integer, Double>();
		
		// The no. of terms may be huge in number.

		int index = getTermIndex(term);

		if(index < 0) {
			return null;
		} else {

			RandomAccessFile raf = rafs[index];

			if(raf == null) {
				raf = new RandomAccessFile(mergedDirPath + "\\" + term.substring(0, 2) + ".txt", "r");
				rafs[index] = raf;
			}

			raf.seek(filePointers[index]);

			String line = raf.readLine();

			String[] elems = line.split("\t");

			for(int i=1;i<elems.length;i++) {

				String[] subs = elems[i].split("\\|");

				postingList.put(Integer.parseInt(subs[0]), Double.parseDouble(subs[1]));
			}

		}

		return postingList;
	}

	public static void loadMetaDataIntoMemory() throws IOException {

		File mergePointerFile = new File(mergeFilePointerDirPath);

		File[] mergePointerFiles = mergePointerFile.listFiles();

		for(File file : mergePointerFiles) {

			if(file.isDirectory()) {
				continue;
			}

			BufferedReader br = new BufferedReader(new FileReader(file));

			String line;

			while( (line = br.readLine()) != null) {

				String[] elems = line.split("\t");

				termIndex++;

				terms[termIndex] = elems[0];
				filePointers[termIndex] = Long.parseLong(elems[1]);		
			}	

			br.close();
		}

		System.out.println("Sorting");
		QuickSort(terms, filePointers, 0, termIndex);
		System.out.println("Sorted");

	}

	public static void createMetaData() throws IOException {
		
		long termCount = 0;

		File mergePointerFile = new File(mergeFilePointerDirPath);
		mergePointerFile.mkdirs();

		File mergedDirFile = new File(mergedDirPath);

		File[] mergedFiles = mergedDirFile.listFiles();

		for(File mergeFile : mergedFiles) {

			if(mergeFile.isDirectory()) {
				continue;
			}
			
			System.out.println("Processing " + mergeFile.getAbsolutePath());

			// Each line would contain the posting list, i.e. Term - PageId|TF ...
			// Record the file pointers for each term and store in new file.			
			RandomAccessFile raf = new RandomAccessFile(mergeFile, "r");

			PrintWriter pw = new PrintWriter(mergeFilePointerDirPath + "\\" + mergeFile.getName());

			long lastFilePointer = raf.getFilePointer();

			String line;

			while( (line = raf.readLine()) != null) {

				String[] elems = line.split("\t");

				String term = elems[0];

				pw.println(term + "\t" + lastFilePointer);
				pw.flush();

				lastFilePointer = raf.getFilePointer();		
				
				termCount++;
			}	

			raf.close();
			pw.close();
		}	
		
		System.out.println("Total terms: " + termCount);
	}

	public static void QuickSort(String[] arr, long[] arr2, int left, int right) {

		int pivot = QuickSortSplit(arr, arr2, left, right);

		if(pivot == -1)return;

		QuickSort(arr, arr2, left, pivot - 1);
		QuickSort(arr, arr2, pivot + 1, right);


	}

	public static int QuickSortSplit(String[] arr, long[] arr2, int left, int right) {

		if((right - left) <1 ) { 
			return -1;
		} else if ( (right - left) == 1) { // Base Case

			if(arr[left].compareTo(arr[right]) > 0) {
				// Swap
				String temp = arr[left];
				arr[left] = arr[right];
				arr[right] = temp;

				long temp2 = arr2[left];
				arr2[left] = arr2[right];
				arr2[right] = temp2;

				return -1;
			}

		}

		int r = (left+right)/2;

		String X = arr[r];
		long X2 = arr2[r];

		int i = left;
		int j = right;

		arr[r] = arr[left];
		arr2[r] = arr2[left];

		while(true) {

			// Decrease j until ar[j] violates with x
			while(i<j && arr[j].compareTo(X) >= 0) {

				j = j-1;
			}

			// OK, there were no violations
			if(i==j || i > j) {
				break;
			}

			arr[i] = arr[j];
			arr2[i] = arr2[j];
			i = i + 1;

			while(i<j && arr[i].compareTo(X) <=0) {

				i = i + 1;

			}

			// OK, there were no violations
			if(i==j || i>j) {
				break;
			}

			arr[j] = arr[i];
			arr2[j] = arr2[i];
			j = j - 1;


		}

		arr[i] = X;
		arr2[i] = X2;

		return i;	
	}

	private static String makeURL( String pageTitle ) {

		String url = "http://en.wikipedia.org/wiki/" + pageTitle.replaceAll(" ", "_") ;

		return url;

	}
	
	static class ResultItem {

		Integer docId;
		Double tfidf;

	}

	static class ResultComparator implements Comparator {

		@Override
		public int compare(Object o1, Object o2) {

			ResultItem r1 = (ResultItem)o1;
			ResultItem r2 = (ResultItem)o2;

			int cmp =  r1.tfidf.compareTo(r2.tfidf);

			return cmp * -1;
		}


	}

}


