package edu.sjsu.cs286.assignment3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.sjsu.cs286.assignment2.LinkTokenizer;
import edu.sjsu.cs286.assignment2.RedirectTokenizer;

public class PageRanksStep1 {

	static int count = 0;
	static int redirectCount = 0;

	static int pageTitleIndex = -1;
	static String[] pageTitles = new String[14128976];

	static int redirectsIndex = -1;
	static String[] redirectsKey = new String[14128976];
	static String[] redirectsValue = new String[14128976];

	static int[] outlinksCount = new int[14128976];
	static int[] inlinksCount = new int[14128976];

	static HashMap<Character, PrintWriter> inLinkWriters = new HashMap<Character, PrintWriter>();
	static HashMap<Character, PrintWriter> outLinkWriters = new HashMap<Character, PrintWriter>();
	static HashMap<Character, PrintWriter> inLinkMergedWriters = new HashMap<Character, PrintWriter>();

	//static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Complete";
	static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Sample";
	static String inLinkDir = baseDir + "\\inlinks";
	static String outLinkDir = baseDir + "\\outlinks";
	static String inLinkMergedDir = inLinkDir + "\\merged"; 

	public static void main(String[] args) throws FileNotFoundException {

		Arrays.fill(outlinksCount, 0);
		Arrays.fill(inlinksCount, 0);
		
		new File(baseDir).mkdirs();
		new File(inLinkDir).mkdirs();
		new File(outLinkDir).mkdirs();
		new File(inLinkMergedDir).mkdirs();

		String dumpFileName = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\sample-enwiki-1.xml";
		//String dumpFileName = "C:\\SJSU\\Sem2\\CS286-IR\\enwiki-latest-pages-articles.xml";

		try{

			MyHandler handler1 = new MyHandler("step1");
			System.out.println("Starting Step 1");

			File xmlFile = new File( dumpFileName);

			InputStream inputStream= new FileInputStream(xmlFile);
			InputStreamReader inputReader = new InputStreamReader(inputStream,"UTF-8");
			InputSource inputSource = new InputSource(inputReader);
			inputSource.setEncoding("UTF-8");

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(inputSource, handler1);

			System.out.println("Done Step 1");
			System.out.println(count + " " + redirectCount);
			System.out.println("Starting Post Step 1");

			postStep1();

			System.out.println("Done Post Step 1");
			
			count = 0;

			System.out.println("Starting Step 2");
			xmlFile = new File( dumpFileName);

			inputStream= new FileInputStream(xmlFile);
			inputReader = new InputStreamReader(inputStream,"UTF-8");
			inputSource = new InputSource(inputReader);
			inputSource.setEncoding("UTF-8");

			factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();

			MyHandler handler2 = new MyHandler("step2");
			saxParser.parse(inputSource, handler2);

			System.out.println("Done Step 2");
			
			// FIXME - Merge Inlinks
			
			File inlinksDir = new File(inLinkDir);
			
			File[] inlinkFiles = inlinksDir.listFiles();
			
			for(File inlinkFile : inlinkFiles) {
				
				HashMap<Integer, HashSet<Integer>> inlinks = new HashMap<Integer, HashSet<Integer>>(); 
				
				if(inlinkFile.isDirectory()) {
					continue;
				}
				
				BufferedReader br = new BufferedReader(new FileReader(inlinkFile));
				
				String line;
				
				// Read each line
				while( (line = br.readLine()) != null) {
					
					String[] elems = line.split("\t");
					
					Integer field1 = Integer.parseInt(elems[0]);
					Integer field2 = Integer.parseInt(elems[1]);
					
					HashSet<Integer> postingList = null;
					
					if(inlinks.containsKey(field1)) {
						postingList = inlinks.get(field1);
					} else {
						postingList = new HashSet<Integer>();
						inlinks.put(field1, postingList);
					}
					
					postingList.add(field2);
	
				}
				
				// At this point the inlinks and associated posting list is complete for this file.
				// Write to file.
				//outLinkMergedWriters
				for(Integer key : inlinks.keySet()) {
					
					HashSet<Integer> list = inlinks.get(key);
					
					PrintWriter pw = null;
					
					if(inLinkMergedWriters.containsKey(Integer.toString(key).charAt(0))) {
						pw = inLinkMergedWriters.get( Integer.toString(key).charAt(0) );
					} else {
						try {
							pw = new PrintWriter(inLinkMergedDir + "\\" + Integer.toString(key).charAt(0) + ".txt");
							inLinkMergedWriters.put( Integer.toString(key).charAt(0), pw );
						} catch (FileNotFoundException e) {

							e.printStackTrace();
						}
					}
					
					StringBuilder sb = new StringBuilder();
					
					sb.append(key);
					sb.append("\t");
					
					for(Integer listItem : list) {
						sb.append(listItem);
						sb.append("\t");
					}
					
					pw.println(sb.toString());
					pw.flush();	
				}
				
				br.close();
				inlinks.clear();
			}
			

			// Inlinks and outlinks ready
			// FIXME - PageRank computation

			// P(K+1) = Xj * P(K)

			// Counting max inlinks and outlinks
			int maxOutlink = 0;String outPage = "";
			for(int i=0;i<=pageTitleIndex;i++){

				if(outlinksCount[i] > maxOutlink) {
					maxOutlink = outlinksCount[i];
					outPage = pageTitles[i];
				}
			}

			int maxInlink = 0;String inPage = "";
			for(int i=0;i<=pageTitleIndex;i++){

				if(inlinksCount[i] > maxInlink) {
					maxInlink = inlinksCount[i];
					inPage = pageTitles[i];
				}
			}

			System.out.println("MaxInlink:" + maxInlink + "|page:" + inPage + ", Maxoutlink:" + maxOutlink + "|page:" + outPage);

			writeToFile();
			closeWriters(inLinkWriters);
			closeWriters(outLinkWriters);
			closeWriters(inLinkMergedWriters);


		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void closeWriters(HashMap<Character, PrintWriter> writers) {
		
		for(Character c : writers.keySet()) {
			writers.get(c).close();
		}
		
	}

	private static void writeToFile() throws FileNotFoundException {

		System.out.println("Writing to file.");

		// Ok sorting is done, write PageTitles to file.	
		PrintWriter pw = new PrintWriter(baseDir + "\\pageTitles.txt");
		PrintWriter pw2 = new PrintWriter(baseDir + "\\redirects.txt");
		PrintWriter pw3 = new PrintWriter(baseDir + "\\outlinksCount.txt");
		PrintWriter pw4 = new PrintWriter(baseDir + "\\inlinksCount.txt");

		for(int i=0;i<=pageTitleIndex;i++) {

			if(i % 10000 == 0) {
				System.out.println("WriteToFile: " + i);
			}

			pw.println(pageTitles[i]);
			pw.flush();
			
			pw3.println(outlinksCount[i]);
			pw3.flush();
			
			pw4.println(inlinksCount[i]);
			pw4.flush();

		}

		for(int i=0;i<=redirectsIndex;i++) {
			pw2.println(redirectsKey[i] + "\t" + redirectsValue[i]);
			pw2.flush();
		}

		pw.close();
		pw2.close();
		pw3.close();
		pw4.close();
	}

	private static void step1( String lastPageId, String lastPageTitle, String pageText ) {

		// Collect all PageTitles ( This will be sorted later )
		pageTitleIndex++;
		pageTitles[pageTitleIndex] = lastPageTitle;

		final RedirectTokenizer redirectTokenizer = new RedirectTokenizer();
		HashSet<String> redLinks = redirectTokenizer.tokenize(pageText);

		// Populate Redirects
		if(redLinks.size() > 0) {
			redirectCount++;

			redirectsIndex++;
			redirectsKey[redirectsIndex] = lastPageTitle;

			// Concerned with only 1st redirect
			String redirect = (String)redLinks.iterator().next();

			// Omit page sections from the redirect
			if(redirect.indexOf("#") >= 0) {
				redirect = redirect.substring(0, redirect.indexOf("#") );
			}

			// Populating the redirect associated with the current Page
			redirectsValue[redirectsIndex] = redirect;

		}

	}

	private static void postStep1() {

		System.out.println("Sorting PageTitles");

		Arrays.sort(pageTitles, 0, pageTitleIndex+1);

		System.out.println("Sorting Redirects");

		// Sort RedirectsKey and rearrange redirectsValue according to redirectsKey
		QuickSort(redirectsKey, redirectsValue, 0, redirectsIndex);

		System.out.println("Sorting Done");


	}

	public static void QuickSort(String[] arr, String[] arr2, int left, int right) {

		int pivot = QuickSortSplit(arr, arr2, left, right);

		if(pivot == -1)return;

		QuickSort(arr, arr2, left, pivot - 1);
		QuickSort(arr, arr2, pivot + 1, right);


	}

	public static int QuickSortSplit(String[] arr, String[] arr2, int left, int right) {

		if((right - left) <1 ) { 
			return -1;
		} else if ( (right - left) == 1) { // Base Case

			if(arr[left].compareTo(arr[right]) > 0) {
				// Swap
				String temp = arr[left];
				arr[left] = arr[right];
				arr[right] = temp;

				temp = arr2[left];
				arr2[left] = arr2[right];
				arr2[right] = temp;

				return -1;
			}

		}

		int r = (left+right)/2;

		String X = arr[r];
		String X2 = arr2[r];

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

	private static void step2( String currentPageTitle, String pageText ) {

		// Rescanning the entire dump.
		final LinkTokenizer linkTokenizer = new LinkTokenizer();
		final RedirectTokenizer redirectTokenizer = new RedirectTokenizer();

		// Check for redirects
		HashSet<String> redLinks = redirectTokenizer.tokenize(pageText);

		if(redLinks.size() > 0) {

			// Do nothing with redirects

			return;


		} else {

			// Check for out outlinks 

			HashSet<String> outlinks = linkTokenizer.tokenize(pageText);

			if(outlinks.size() > 0){

				StringBuilder outLinkString = new StringBuilder();	// Meant for outlinks

				int currentPageTitleIndex = getPageIndex(currentPageTitle);

				// Write to file

				for(String outlink : outlinks) {

					// Skip link that point to the same page
					if(outlink.startsWith("#")) {
						continue;
					}


					// Omit page sections from the page title
					if(outlink.indexOf("#") >= 0) {

						outlink = outlink.substring(0, outlink.indexOf("#") );

					}

					// Identify if the Link has a redirect using redirects captured from step 1. If yes, get the target
					// Resolve redirect using the outlink
					String resolvedRedirect = resolveRedirect(outlink, new HashSet<String>()); // This will be used for inlinks also

					// Is a cycle
					if(currentPageTitle.equals(resolvedRedirect)) {
						continue;
					}


					int redirectIndex = getPageIndex(resolvedRedirect);					

					// Is pageId valid ? Yes it is
					if( redirectIndex >=0 ) {

						// Append to outlinks String
						outLinkString.append(redirectIndex);
						outLinkString.append("\t");

						// Increase outlink count
						outlinksCount[currentPageTitleIndex] = outlinksCount[currentPageTitleIndex]+1;

						// Write to inlinks

						// Get the writer
						PrintWriter pw = null;

						if(inLinkWriters.containsKey(Integer.toString(redirectIndex).charAt(0))) {
							pw = inLinkWriters.get( Integer.toString(redirectIndex).charAt(0) );
						} else {
							try {
								pw = new PrintWriter(inLinkDir + "\\" + Integer.toString(redirectIndex).charAt(0) + ".txt");
								inLinkWriters.put( Integer.toString(redirectIndex).charAt(0), pw );
							} catch (FileNotFoundException e) {

								e.printStackTrace();
							}
						}

						pw.println(redirectIndex + "\t" + currentPageTitleIndex);
						pw.flush();

						// Increase inlinks count
						inlinksCount[redirectIndex] = inlinksCount[redirectIndex]+1;
					}
				}

				// Write the outlinks
				if(outLinkString.length() > 0) {

					PrintWriter pw = null;

					if(outLinkWriters.containsKey(Integer.toString(currentPageTitleIndex).charAt(0))) {
						pw = outLinkWriters.get( Integer.toString(currentPageTitleIndex).charAt(0) );
					} else {
						try {
							pw = new PrintWriter(outLinkDir + "\\" + Integer.toString(currentPageTitleIndex).charAt(0) + ".txt");
							outLinkWriters.put( Integer.toString(currentPageTitleIndex).charAt(0), pw );
						} catch (FileNotFoundException e) {

							e.printStackTrace();
						}
					}

					pw.println(currentPageTitleIndex + "\t" + outLinkString.toString());
					pw.flush();

				}
			}		

		}

	}

	private static int getPageIndex(String pageTitle) {
		int idx = Arrays.binarySearch(pageTitles, 0, pageTitleIndex + 1, pageTitle);
		//int idx =  searchInPageTitles(pageTitle, 0, pageTitleIndex);

		if(idx < 0) {
			//System.out.println("PageTitle found. " + pageTitle);
		}

		return idx;
	}

	// Resolves the redirect ( transitive dependency )
	// What to do in case of an infinite loop
	private static String resolveRedirect(String pageTitle, HashSet<String> encountered) {

		if(encountered.contains(pageTitle)) {
			return pageTitle;
		}

		encountered.add(pageTitle);
		int index = Arrays.binarySearch(redirectsKey, 0, redirectsIndex + 1, pageTitle);//searchInRedirects(pageTitle, 0, redirectsIndex);

		if(index >= 0) {

			return resolveRedirect(redirectsValue[index], encountered);
		}

		return pageTitle;	
	}

	private static int searchInPageTitles(String pageText, int start, int end) {

		int mid = (start + end) / 2;

		if(pageTitles[mid].compareTo(pageText) == 0) {
			return mid;
		} else if( start >= end) {
			return -1;
		} else if(pageTitles[mid].compareTo(pageText) > 0) {
			return searchInPageTitles(pageText, mid+1, end);
		} else {
			return searchInPageTitles(pageText, start, mid-1);
		}

	}

	private static int searchInRedirects(String pageText, int start, int end) {

		int mid = (start + end) / 2;

		if(redirectsKey[mid].compareTo(pageText) == 0) {
			return mid;
		} else if( start >= end) {
			return -1;
		} else if(redirectsKey[mid].compareTo(pageText) > 0) {
			return searchInRedirects(pageText, mid+1, end);
		} else {
			return searchInRedirects(pageText, start, mid-1);
		}

	}

	static class MyHandler extends DefaultHandler {

		boolean inPage = false;
		boolean inRevision = false ;
		private boolean fixed;
		private StringBuffer lastStr = new StringBuffer();
		private String lastPageTitle = null;
		private String lastPageId = null;
		private String lastPageNs = null;

		private String step = null ;

		public MyHandler(String step) {
			this.step = step;
		}


		public void startElement(String uri, String localName,String qName, 
				Attributes attributes) throws SAXException {

			lastStr.setLength(0);

			if (qName.equalsIgnoreCase("page")) {

				inPage = true;

			} else if (qName.equalsIgnoreCase("revision")) {
				inRevision = true;
			}

			fixed = false;

		}

		public void endElement(String uri, String localName,
				String qName) throws SAXException {

			if(qName.equals("page")) {
				count++;

				if(count % 10000 == 0) {
					System.out.println(step + ":" + count);
				}

				inPage = false;
				lastPageTitle = null;
				lastPageId = null;
				lastPageNs = null;


			}  else if(qName.equals("revision")) {
				inRevision = false;

				if(inPage){
					//revCount++;
				}
			} else if (qName.equals("title") && inPage) {
				lastPageTitle = lastString();
			}  else if(qName.equals("id") && inPage && !inRevision) {
				lastPageId = lastString();
			} else if(qName.equals("ns") && inPage ) {
				lastPageNs = lastString();
			} else if (qName.equals("text") && inPage && lastPageNs != null && lastPageNs.equals("0")) {

				if(step != null && step.equalsIgnoreCase("step1")) {
					step1(lastPageId, lastPageTitle, lastString());
				} else if(step != null && step.equalsIgnoreCase("step2")) {
					step2(lastPageTitle, lastString());
				}

			}
		}


		public void characters(char ch[], int start, int length) throws SAXException {

			lastStr.append(ch, start, length);

		}

		public String lastString()
		{
			if (!fixed)
			{
				lastStr.trimToSize();

				fixed = true;
			}

			return lastStr.toString();
		}

	}
}