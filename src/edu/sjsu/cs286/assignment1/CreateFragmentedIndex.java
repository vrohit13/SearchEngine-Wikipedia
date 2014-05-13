package edu.sjsu.cs286.assignment1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;


public class CreateFragmentedIndex {

	static long count = 0;
	public static void main(String args[]) {
		
		final String stopWordsFilePath = "stopwords.txt";
		final String baseDir = "RawIndex";
		final String pageId_PageTitle_Path = baseDir + "\\PageId_PageTitle";
		String wikipediaDump = ""; //"C:\\SJSU\\Sem2\\CS286-IR\\sample-enwiki-4.xml";
		
		if(args.length > 0) {
			if(args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println("Create Fragmented Index Help:");
				System.out.println("Arguments as:");
				System.out.println("arg1: <Wikipedia dump file path>");
				System.exit(-1);
			} else {
				wikipediaDump = args[0];
			}
		} else {
			System.out.println("Create Fragmented Index Help:");
			System.out.println("Arguments as:");
			System.out.println("arg1: <Wikipedia dump file path>");
			System.exit(-1);
		}
		
		
		
		if( !new File(wikipediaDump).exists()) {
			System.out.println("Wikipedia dump file not found. " + wikipediaDump);
			System.exit(-1);
		}
		
		File stopWordsFile = new File(stopWordsFilePath);
		if(!stopWordsFile.exists()) {
			System.out.println("File 'stopwords.txt' not found in current directory.");
			System.exit(-1);
		}
		
		File f = new File(baseDir);
		f.mkdirs();
		
		File f1 = new File(pageId_PageTitle_Path);
		f1.mkdir();
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("Building Index. " + startTime);
		
		final HashMap<Character, PrintWriter> writers = new HashMap<Character, PrintWriter>();
		final HashMap<String, String> pageId_title = new HashMap<String, String>();

		
		final Tokenizer t = new Tokenizer(stopWordsFilePath);
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				boolean inPage = false;
				boolean inRevision = false ;
				private boolean fixed;
				private StringBuffer lastStr = new StringBuffer();
				private String lastPageTitle = null;
				private String lastPageId = null;


				public void startElement(String uri, String localName,String qName, 
						Attributes attributes) throws SAXException {

					lastStr.setLength(0);

					if (qName.equalsIgnoreCase("page")) {
						
						inPage = true;
						
						if(CreateFragmentedIndex.count % 5000 ==0) {
							System.out.println("Page Count:" + CreateFragmentedIndex.count);
						}

					} else if (qName.equalsIgnoreCase("revision")) {
						inRevision = true;
					}
					
					fixed = false;

				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {


					if(qName.equals("page")) {
						inPage = false;
						lastPageTitle = null;
						lastPageId = null;
					}  else if(qName.equals("revision")) {
						inRevision = false;
					} else if (qName.equals("title") && inPage) {
						lastPageTitle = lastString();
					}  else if(qName.equals("id") && inPage && !inRevision) {
						lastPageId = lastString();
					} else if (qName.equals("text") && inPage) {				
						
						TreeSet<String> tokenStrings = t.tokenize2(lastString());
						
						for(String token : tokenStrings) {
							
							PrintWriter pw = null;
							
							if( Pattern.matches("^[A-Za-z0-9].*", token)) {	// Perform only for tokens containing alphabets and/or digits
								

								if(writers.containsKey(token.charAt(0))) {
									pw = writers.get(token.charAt(0));
								} else {
									try {
										pw = new PrintWriter( baseDir + "\\" + Character.toString(token.charAt(0)) + ".txt" );

										writers.put( token.charAt(0) , pw);
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								pw.println(token + "\t" + lastPageId);
								pw.flush();
								
								pageId_title.put(lastPageId, lastPageTitle);

							}
							
							
						}
						
						CreateFragmentedIndex.count++;

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

			};

			
			File xmlFile = new File(wikipediaDump);
			InputStream inputStream= new FileInputStream(xmlFile);
			InputStreamReader inputReader = new InputStreamReader(inputStream,"UTF-8");

			InputSource inputSource = new InputSource(inputReader);

			inputSource.setEncoding("UTF-8");
			
			//saxParser.parse("C:\\SJSU\\Sem2\\CS286-IR\\enwiki-latest-pages-articles.xml", handler);
			saxParser.parse(inputSource, handler);


		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long pageProcessed = System.currentTimeMillis();
		
		System.out.println("Processed all pages. Time taken:" + (pageProcessed - startTime));
		
		System.out.println("Writing PageId - PageTitle into file.");
		
		//Write the pageId - title into file.
		
		try {
			PrintWriter pw = new PrintWriter(pageId_PageTitle_Path + "\\PageId_PageTitle.txt");
			
			for(String pageId : pageId_title.keySet()) {
				
				pw.println(pageId + "\t" + pageId_title.get(pageId) );
				pw.flush();
				
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Written to file. Total time taken:" + (System.currentTimeMillis() - startTime) + " ms");
		System.out.println("Fragmented Index created at: " + f.getAbsolutePath());
		System.out.println("PageId-PageTitle created at: " + f1.getAbsolutePath());
		System.out.println("Total Pages/Documents:" +  count);
		System.out.println("Total tokens encountered:" + t.getTokenCount());
	}

}
