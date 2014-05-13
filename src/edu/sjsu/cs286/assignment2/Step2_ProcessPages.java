package edu.sjsu.cs286.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Step2_ProcessPages {

	static long count = 0;
	static long revCount = 0;
	static long textCount = 0;
	static long invalidPages = 0;
	static long redirectResolved = 0;

	final static String baseDir = Common.baseDir;//"C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\Sample";
	final static String redirectsDir = Common.redirectsDir;//baseDir + "\\Redirects";
	final static String pageTitle_PageIdDir = Common.pageTitle_PageIdDir;//baseDir + "\\PageTitle_PageId";
	
	public static HashMap<String, String> loadRedirects() {
		
		System.out.println("Loading redirects.");
		
		long redirectsCount = 0;
		
		HashMap<String, String> redirectsMap = new HashMap<String, String>();
		
		File f = new File(redirectsDir);
		
		File[] files = f.listFiles();
		
		for(File file : files ) {
			
			System.out.println(file.getAbsolutePath());
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String line = null;
			
			
			
			try {
				while((line = br.readLine()) != null) {
						
					String[] tokens = line.split("\t");
					
					if(tokens.length <2) {
						continue;
					}
					
					redirectsMap.put(tokens[0], tokens[1]);
					
					redirectsCount++;
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(line);
			}
			
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Loaded redirects. " + redirectsCount);
					
		return redirectsMap;
	}

	public static void main(String[] args) {

		String dumpFileName = Common.dumpFileName;//"C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\sample-enwiki-1.xml";

		new File(baseDir).mkdirs();

		final PageTitlePageId pg = new PageTitlePageId(pageTitle_PageIdDir);

		final String outboundDir = baseDir + "\\outbound";
		final String inboundDir = baseDir + "\\inbound";

		new File(outboundDir).mkdirs();
		new File(inboundDir).mkdirs();


		final HashMap<Character, PrintWriter> outboundWriters = new HashMap<Character, PrintWriter>();
		final HashMap<Character, PrintWriter> inboundWriters = new HashMap<Character, PrintWriter>();

		final HashMap<String, String> redirectsMap = loadRedirects();

		try {

			final LinkTokenizer linkTokenizer = new LinkTokenizer();
			final RedirectTokenizer redirectTokenizer = new RedirectTokenizer();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				boolean inPage = false;
				boolean inRevision = false ;
				private boolean fixed;
				private StringBuffer lastStr = new StringBuffer();
				private String lastPageTitle = null;
				private String lastPageId = null;
				private String lastPageNs = null;


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

						if(count % 1000 ==0) {
							System.out.println(count);
						}
						inPage = false;
						lastPageTitle = null;
						lastPageId = null;
						lastPageNs = null;
					}  else if(qName.equals("revision")) {
						inRevision = false;

						if(inPage){
							revCount++;
						}
					} else if (qName.equals("title") && inPage) {
						lastPageTitle = lastString();
					}  else if(qName.equals("id") && inPage && !inRevision) {
						lastPageId = lastString();
					} else if(qName.equals("ns") && inPage ) {
						lastPageNs = lastString();
					} else if (qName.equals("text") && inPage && lastPageNs != null && lastPageNs.equals("0")) {				

						// Check for redirects
						HashSet<String> redLinks = redirectTokenizer.tokenize(lastString());

						if(redLinks.size() > 0) {

							// Do nothing with redirects


						} else {

							// Check for out outlinks 

							HashSet<String> outlinks = linkTokenizer.tokenize(lastString());

							if(outlinks.size() > 0){

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

									boolean validOutlink = false;
									
									String outlinkPageId = pg.getPageId(outlink);
									
									if(outlinkPageId != null) {
										validOutlink = true;
										
									} else {
										validOutlink = false;
									}
									
									// Check if the outlink is a valid Wikipedia page to be considered (i.e. ns=0, etc etc). Use the data collected from Step 1.
//
//									File file = new File(pageTitle_PageIdDir + "\\" + outlink.charAt(0)+".txt");
//
//									// Redirects file exists
//									if(file.exists()) {
//
//										BufferedReader br = null;
//										try {
//											br = new BufferedReader(new FileReader(file));
//										} catch (FileNotFoundException e) {
//
//											e.printStackTrace();
//										}
//
//										String line;
//
//										//FIXME - should this be done in the memory ???????
//										try {
//											while( (line = br.readLine()) != null) {
//
//												String[] tokens = line.split("\t");
//
//												if( tokens[0].equals(outlink)) {
//
//													// Ok we got the required link that has a redirect
//
//													//outlink = tokens[1]; // Replace token with target of redirect
//													validOutlink = true;
//													break;
//												}
//											}
//											br.close();
//										} catch (IOException e) {
//
//											e.printStackTrace();
//										}
//
//
//									} else {
//										//System.out.println("Page file not found. " + outlink);
//										validOutlink = false;
//									}

									if(!validOutlink) {
										//System.out.println("Not a valid page. ");
										invalidPages++;
										// Not a valid page
										return;
									}


									textCount++;
									// Identify if the Link has a redirect using redirects captured from step 1. If yes, get the target
									
									if(redirectsMap.containsKey(outlink)) {
										
										outlink = redirectsMap.get(outlink); // Replace token with target of redirect
										System.out.println("Redirect resolved.");
										redirectResolved++;
										
									}

//									File file = new File(redirectsDir + "\\" + outlink.charAt(0)+".txt");
//
//									// Redirects file exists
//									if(file.exists()) {
//
//										BufferedReader br = null;
//										try {
//											br = new BufferedReader(new FileReader(file));
//										} catch (FileNotFoundException e) {
//
//											e.printStackTrace();
//										}
//
//										String line;
//
//										//FIXME - should this be done in the memory ???????
//										try {
//											while( (line = br.readLine()) != null) {
//
//												String[] tokens = line.split("\t");
//
//												if( tokens[0].equals(outlink)) {
//
//													// Ok we got the required link that has a redirect
//
//													outlink = tokens[1]; // Replace token with target of redirect
//													System.out.println("Redirect resolved.");
//													redirectResolved++;
//													break;
//												}
//											}
//											br.close();
//										} catch (IOException e) {
//
//											e.printStackTrace();
//										}
//
//
//									} else {
//										System.out.println("Redirect file not found. " + outlink);
//									}



									PrintWriter pw = null;

									if(outboundWriters.containsKey(lastPageId.charAt(0))) {
										pw = outboundWriters.get(lastPageId.charAt(0));
									} else {
										try {
											pw = new PrintWriter(outboundDir + "\\" + lastPageId.charAt(0) + ".txt");
											outboundWriters.put(lastPageId.charAt(0), pw);
										} catch (FileNotFoundException e) {

											e.printStackTrace();
										}
									}

									pw.println(lastPageId + "\t" + outlinkPageId/*outlink*/);
									pw.flush();

									// Inbound
									pw = null;

									if(inboundWriters.containsKey(outlinkPageId.charAt(0))) {
										pw = inboundWriters.get(outlinkPageId.charAt(0));
									} else {
										try {
											pw = new PrintWriter(inboundDir + "\\" + outlinkPageId.charAt(0) + ".txt");
											inboundWriters.put(outlinkPageId.charAt(0), pw);
										} catch (FileNotFoundException e) {

											e.printStackTrace();
										}
									}

									pw.println(outlinkPageId + "\t" + lastPageId);
									pw.flush();

								}

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

			};


			//File xmlFile = new File("C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\sample-enwiki-1.xml");

			File xmlFile = new File(dumpFileName);


			InputStream inputStream= new FileInputStream(xmlFile);
			InputStreamReader inputReader = new InputStreamReader(inputStream,"UTF-8");

			InputSource inputSource = new InputSource(inputReader);

			inputSource.setEncoding("UTF-8");

			//saxParser.parse("C:\\SJSU\\Sem2\\CS286-IR\\enwiki-latest-pages-articles.xml", handler);
			saxParser.parse(inputSource, handler);

			System.out.println(count + " " + revCount);
			System.out.println("Text Count:" + textCount);
			System.out.println("Invalid Pages:" + invalidPages);
			System.out.println("REdirects Resolved:" + redirectResolved);

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}

class PageTitlePageId {
	
	HashMap<String, String> pageTitleMap = new HashMap<String, String>();
	String lastTitle = null;
	
	String fileName;
	
	PageTitlePageId(String fileName) {
		this.fileName = fileName;
	}

	public String getPageId(String pageTitle) {
		
		//System.out.println("Getting PageId for " + pageTitle);

		String pageId = null;
		
		if(lastTitle != null && lastTitle.charAt(0) == pageTitle.charAt(0)) {
			//System.out.println("Reuse");
			if( pageTitleMap.containsKey(pageTitle)) {
				return pageId = (String)pageTitleMap.get(pageTitle);
			}
			
			lastTitle = pageTitle;
			
		} else {
			
			File f = null;
			
			if( !Pattern.matches("^[A-Za-z0-9]{1}.*", pageTitle ) ) {
				
				f = new File(fileName + "\\" + "other.txt");
				
			} else {
				 f = new File(fileName + "\\" + pageTitle.charAt(0) + ".txt");
			}
			
			if(f.exists()) {
				
			//	System.out.println("Loading into memory");
				
				pageTitleMap.clear();
				
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(f));
				} catch (FileNotFoundException e) {
					System.out.println("PageTitle:" + pageTitle);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String line;
				
				try {
					while( (line = br.readLine()) != null) {
						
						String[] tokens = line.split("\t");
						pageTitleMap.put(tokens[0], tokens[1]);

					}
					
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if( pageTitleMap.containsKey(pageTitle)) {
					pageId = (String)pageTitleMap.get(pageTitle);
				}
				
				lastTitle = pageTitle;
			//	System.out.println("Loaded into memory");
			}	
		}
		
		

		return pageId;

	}
}
