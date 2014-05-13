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
import java.util.HashSet;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CreateTermPostingListUsingFilePointers {

	static int pageTitleIndex = -1;
	static String[] pageTitles = new String[14128976];

	static int[] inlinksCount = new int[14128976];

	static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Complete";	

	//static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\TestingArea";	
	//static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\Sample";
	static String inlinksCountFileName = baseDir + "\\inlinksCount.txt";

	static String dumpFileName = "C:\\SJSU\\Sem2\\CS286-IR\\enwiki-latest-pages-articles.xml";
	//static String dumpFileName = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 3\\sample-enwiki-1.xml";

	static final String stopWordsFilePath = "stopwords.txt";

	static final String fragmentedDir = baseDir + "\\FragmentedIndex";
	static TermTokenizer t = null;

	static final HashMap<String, PrintWriter> fragmentedWriters = new HashMap<String, PrintWriter>();

	static ArrayList<String> reserved = new ArrayList<String>();
	
	static HashSet<String> notProcess = new HashSet<String>();

	static{
		reserved.add("CON");
		reserved.add("PRN");
		reserved.add("AUX");
		reserved.add("CLOCK$");
		reserved.add("NUL");
		reserved.add("COM1");
		reserved.add("COM2");
		reserved.add("COM3");
		reserved.add("COM4");
		reserved.add("COM5");
		reserved.add("COM6");
		reserved.add("COM7");
		reserved.add("COM8");
		reserved.add("COM9");

		reserved.add("LPT1");
		reserved.add("LPT2");
		reserved.add("LPT3");
		reserved.add("LPT4");
		reserved.add("LPT5");
		reserved.add("LPT6");
		reserved.add("LPT7");
		reserved.add("LPT8");
		reserved.add("LPT9");
		
		notProcess.add("co.txt");
		notProcess.add("re.txt");
		notProcess.add("in.txt");
		notProcess.add("de.txt");
		notProcess.add("pr.txt");
		notProcess.add("st.txt");
		notProcess.add("ca.txt");
		notProcess.add("ma.txt");
		notProcess.add("19.txt");
		notProcess.add("se.txt");
		notProcess.add("20.txt");
		notProcess.add("pa.txt");
		notProcess.add("di.txt");
		notProcess.add("la.txt");
		notProcess.add("li.txt");
		
		
	}

	public static void main(String[] args) throws Exception {

		// Create the fragmented index directory
		new File(fragmentedDir).mkdirs();

		// Load the page titles
		loadPageTitles();

		loadInlinkCount();

		// Check for stop words
		/*File stopWordsFile = new File(stopWordsFilePath);
		if(!stopWordsFile.exists()) {
			System.out.println("File 'stopwords.txt' not found in current directory.");
			System.exit(-1);
		}

		t = new TermTokenizer(stopWordsFilePath);


		// Read the dump file and created the fragmented posting list associated with the term
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		MyTermHandler handler = new MyTermHandler();

		File xmlFile = new File(dumpFileName);
		InputStream inputStream= new FileInputStream(xmlFile);
		InputStreamReader inputReader = new InputStreamReader(inputStream,"UTF-8");

		InputSource inputSource = new InputSource(inputReader);

		inputSource.setEncoding("UTF-8");

		saxParser.parse(inputSource, handler);*/

		//merge();
		//mergeN2();
		//mergeUsingFilePointers();
		mergeUsingFilePointersAferCreation();
		//calcIdf();
		cleanup();


	}
	
	private static boolean shouldProcess(String fileName) {
		
		if(notProcess.contains(fileName)) {
			return false;
		}
		
		return true;
	}

	private static void cleanup() {

		// Cleanup Fragmented Writers
		for(String c : fragmentedWriters.keySet()) {

			PrintWriter pw = fragmentedWriters.get(c);
			pw.close();
		}

	}

	private static void calcIdf() throws Exception {


		System.out.println("Calculating idf");
		File mergedIndex = new File(baseDir + "\\FragmentedIndex\\merged");

		File idfDir = new File(baseDir + "\\FragmentedIndex\\merged\\idf");
		idfDir.mkdirs();

		File[] mergedFiles = mergedIndex.listFiles();

		for(File mergeFile : mergedFiles) {

			if(mergeFile.isDirectory()) {
				continue;
			}

			System.out.println("Processing file " + mergeFile.getAbsolutePath());

			PrintWriter pw = new PrintWriter(idfDir.getAbsolutePath() + "\\" + mergeFile.getName());

			BufferedReader br = new BufferedReader(new FileReader(mergeFile));
			String line;


			while( (line = br.readLine()) != null) {


				String[] elems = line.split("\t");

				double idf = Math.log((double)pageTitleIndex/(double)(elems.length -1));

				pw.println(elems[0] + "\t" + idf);
				pw.flush();

			}

			br.close();
			pw.close();					
		}
	}

	private static void mergeUsingFilePointersAferCreation() throws IOException {

		File fragmentedDirFile = new File( fragmentedDir );

		File mergedIndex = new File(fragmentedDir + "\\merged");
		mergedIndex.mkdirs();

		File filepointers = new File(fragmentedDir + "\\filepointers");

		File[] pointerFileList = filepointers.listFiles();

		for(File pointerFile : pointerFileList) {

			if(pointerFile.isDirectory()) {
				continue;


			}

			System.out.println("Processing pointer file:" + pointerFile.getAbsolutePath());

			BufferedReader br = new BufferedReader(new FileReader(pointerFile));
			RandomAccessFile raf = new RandomAccessFile(fragmentedDirFile.getAbsolutePath() + "\\" + pointerFile.getName(), "r");
			PrintWriter pw = new PrintWriter(mergedIndex.getAbsolutePath() + "\\" + pointerFile.getName());

			String pointerLine;

			long count = 0;

			while( (pointerLine = br.readLine()) != null) {

				HashSet<String> finalPostingList = new HashSet<String>();
				String[] elems = pointerLine.split("\t");

				String term = elems[0];

				for(int i=1; i< elems.length; i++) {

					// Get the file offset
					long pointer = Long.parseLong(elems[i]);

					// Seek to the offset in fragment file
					raf.seek(pointer);

					// Read the line at this offset
					String fragLine = raf.readLine();

					String[] frags = fragLine.split("\t");
					finalPostingList.add(frags[1]);		
				}

				// Write the posting list to merged file.
				StringBuilder sb = new StringBuilder();
				sb.append(term);
				sb.append("\t");

				for(String pList : finalPostingList) {
					sb.append(pList);
					sb.append("\t");
				}

				pw.println(sb.toString());
				pw.flush();

				count++;
				if(count % 10000 == 0) {
					System.out.println(pointerFile.getName() + " " + count);
				}

			}

			pw.close();
			raf.close();
			br.close();

		}

	}

	private static void mergeUsingFilePointers() throws IOException {

		System.out.println("Merging the fragmented index for each file using file pointers approach.");
		File fragmentedDirFile = new File( fragmentedDir );

		File mergedIndex = new File(fragmentedDir + "\\merged");
		mergedIndex.mkdirs();

		File filepointers = new File(fragmentedDir + "\\filepointers");
		filepointers.mkdirs();

		File[] fragmentedFiles = fragmentedDirFile.listFiles();

		for(File file : fragmentedFiles) {

			if(file.isDirectory()) {
				continue;
			}
			
			if(!shouldProcess(file.getName())) {
				continue;
			}

			System.out.println("Processing file. " + file.getAbsolutePath());

			// Process the file and store the file pointers for each term
			HashMap<String, HashSet<Long>> pointerLists = new HashMap<String, HashSet<Long>>();
			RandomAccessFile raf = new RandomAccessFile(file, "r");

			String line;
			long lastPointerLocation = raf.getFilePointer();


			long count = 0;

			while( (line = raf.readLine()) != null) {

				String[] elems = line.split("\t");

				HashSet<Long> pointerList = null;
				if(pointerLists.containsKey(elems[0])) {
					pointerList = pointerLists.get(elems[0]);
				} else {
					pointerList = new HashSet<Long>();
					pointerLists.put(elems[0], pointerList);
				}

				pointerList.add(lastPointerLocation);

				lastPointerLocation = raf.getFilePointer();

				count++;
				if(count % 10000 == 0) {
					System.out.println("Pointer collection:" + count);
				}

			}	

			raf.close();
			count = 0;
			System.out.println("Writing to file.");
			// Write the pointer lists to file
			PrintWriter pw = new PrintWriter(filepointers.getAbsolutePath() + "\\" + file.getName());

			for(String term : pointerLists.keySet()) {

				StringBuilder sb = new StringBuilder();
				sb.append(term);
				sb.append("\t");			

				HashSet<Long> pointerList = pointerLists.get(term);

				for(Long pointerItem : pointerList) {
					sb.append(pointerItem);
					sb.append("\t");					
				}

				pw.println(sb.toString());
				pw.flush();			

			}

			pw.close();

		}

	}

	private static void mergeN2() throws IOException {

		System.out.println("Merging the fragmented index for each file using N^2 approach.");
		File fragmentedDirFile = new File( fragmentedDir );

		File mergedIndex = new File(fragmentedDir + "\\merged");
		mergedIndex.mkdirs();

		File[] fragmentedFiles = fragmentedDirFile.listFiles();

		for(File file : fragmentedFiles) {

			if(file.isDirectory()) {
				continue;
			}

			System.out.println("Merge: Processing file " + file.getAbsolutePath());

			// Read all terms into memory
			HashSet<String> terms = new HashSet<String>();

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			System.out.println(file.getName() + " " + "Collecting terms.");
			while( (line = br.readLine()) != null) {

				String[] elems = line.split("\t");

				if( !terms.contains(elems[0])) {
					terms.add(elems[0]);
				}

			}

			br.close();
			System.out.println(file.getName() + " " +"Collected terms.");

			PrintWriter pw = new PrintWriter( mergedIndex.getAbsolutePath() + "\\" + file.getName());

			// For each term, process the entire file and collect associated page ids. Finally write to merged directory
			for( String term : terms ) {

				//System.out.println("Processing file for term. " + term);

				br = new BufferedReader(new FileReader(file));

				HashSet<String> postingList = new HashSet<String>();

				line = null;

				while( (line = br.readLine()) != null) {

					String[] elems = line.split("\t");

					if(term.equals(elems[0])) {
						postingList.add(elems[1]);		
					}

				}

				//System.out.println("Created posting list for term. " + term);
				//System.out.println("Writing posting list for term. " + term);
				// Write to merged file

				StringBuilder sb = new StringBuilder();
				sb.append(term);
				sb.append("\t");

				for(String listItem : postingList) {
					sb.append(listItem);
					sb.append("\t");
				}

				pw.println(sb.toString());
				pw.flush();

				br.close();

				postingList.clear();

				//System.out.println("Written posting list for term. " + term);
			}

			pw.close();

			System.out.println(file.getName() + " " + "Merge: Processed file " + file.getAbsolutePath());

		}


	}

	private static void merge() throws Exception{

		// Now process each file in 'FragmentedIndex' at a time and merge the contents into together, i.e. create a posting list for each term encountered
		System.out.println("Merging the fragmented index for each file.");
		File fragmentedDirFile = new File( fragmentedDir );

		File mergedIndex = new File(baseDir + "\\FragmentedIndex\\merged");
		mergedIndex.mkdirs();
		HashMap<String, PrintWriter> mergeWriters = new HashMap<String, PrintWriter>();

		File[] fragmentedFiles = fragmentedDirFile.listFiles();

		for(File file : fragmentedFiles) {

			if(file.isDirectory()) {
				continue;
			}

			System.out.println("Merge: Processing file " + file.getAbsolutePath());

			// For each file, collect the terms and the posting list associated with each term
			HashMap<String, HashSet<String>> postingLists = new HashMap<String, HashSet<String>>();

			BufferedReader br = new BufferedReader(new FileReader(file));

			String line;

			long count = 0;

			while ( (line = br.readLine()) != null ) {

				HashSet<String> postingList = null;

				// Each line will contain the term and the page id
				String[] elems = line.split("\t");

				if(postingLists.containsKey(elems[0])) {
					postingList = postingLists.get(elems[0]);

				} else {
					postingList = new HashSet<String>();
					postingLists.put(elems[0], postingList);
				}

				postingList.add(elems[1]);

				count ++;
				if(count % 10000 == 0) {
					System.out.println("Processed. " + count);
				}

			}

			br.close();

			System.out.println("Done creating posting list for this file.");

			// Now write the merged to another file.
			count = 0;

			System.out.println("Writing merged lists to file.");

			for(String term : postingLists.keySet()) {

				HashSet<String> postingList = postingLists.get(term);

				StringBuilder sb = new StringBuilder();

				sb.append(term);
				sb.append("\t");

				for(String item : postingList) {

					sb.append(item);
					sb.append("\t");
				}

				PrintWriter pw = null;

				String fileId = getValidFilename( term.substring(0, 2) );

				if(mergeWriters.containsKey( fileId )) {
					pw = mergeWriters.get( fileId );
				} else {
					pw = new PrintWriter(mergedIndex + "\\" +fileId + ".txt");
					mergeWriters.put(fileId, pw);
				}

				pw.println(sb.toString());
				pw.flush();

			}

			System.out.println("Done writing merged lists to file(s)");	

		}

		// Cleanup MergeWriters
		for(String c : mergeWriters.keySet()) {

			PrintWriter pw = mergeWriters.get(c);
			pw.close();					
		}

	}

	private static String getValidFilename( String name ) {

		/*String alt = null;

		if(reserved.contains(name.toUpperCase())) {
			alt = "_" + name;
		} else {
			alt = name;
		}*/

		return name;
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

	private static int getPageIndex( String pageTitle ) {
		return Arrays.binarySearch(pageTitles, 0, pageTitleIndex, pageTitle);
	}

	static class MyTermHandler extends DefaultHandler {

		int count = 0;
		boolean inPage = false;
		boolean inRevision = false ;
		private boolean fixed;
		private StringBuffer lastStr = new StringBuffer();
		private String lastPageTitle = null;
		private String lastPageId = null;
		private String lastPageNs = null;

		private String step = null ;

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

				if(count % 5000 == 0) {
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

				int pageTitleIndex = getPageIndex(lastPageTitle);

				if(pageTitleIndex < 0 ) {	// Invalid page title
					return;
				} else if(inlinksCount[pageTitleIndex] < 1) {	// Ignore pages with no inlinks
					return;
				}

				// FIXME  filter out pages that have smaller inlinks


				HashMap<String, Integer> tokenStrings = t.tokenizeAndComputeTF(lastString());

				for(String token : tokenStrings.keySet()) {

					Integer tCount = tokenStrings.get(token);
					double tf = (double)tCount / (double)t.getWC();

					PrintWriter pw = null;

					if( Pattern.matches("^[A-Za-z0-9].*", token)) {	// Perform only for tokens containing alphabets and/or digits

						String fileId = getValidFilename( token.substring(0, 2) );

						if(fragmentedWriters.containsKey( fileId )) {
							pw = fragmentedWriters.get( fileId );
						} else {
							try {
								pw = new PrintWriter( baseDir + "\\FragmentedIndex\\" + fileId + ".txt" );

								fragmentedWriters.put( fileId , pw);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						// Write Token, PageTitleIndex and TermFrequency
						pw.println(token + "\t" + pageTitleIndex + "|" + tf);
						pw.flush();

					}			
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
