package edu.sjsu.cs286.assignment2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class Step1_CollectRedirectsAndPageId_PageTitle {

	static int count = 0;
	static PrintWriter pageId_PageTitleOther = null;

	static PrintWriter pageTitle_PageIdOther = null;
	static PrintWriter redirectsOther = null;
	
	public static void main(String[] args) {

		String dumpFileName = Common.dumpFileName;//"C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\sample-enwiki-1.xml";
		//String dumpFileName = "C:\\SJSU\\Sem2\\CS286-IR\\sample-enwiki-4.xml";
		
		//final String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\Sample";
		final String redirectsDir = Common.redirectsDir /*baseDir + "\\Redirects";*/;
		final String pageId_PageTitleDir = Common.pageId_PageTitleDir;//baseDir + "\\PageId_PageTitle";
		final String pageTitle_PageIdDir = Common.pageTitle_PageIdDir;//baseDir + "\\PageTitle_PageId";
		
		new File(Common.baseDir).mkdirs();
		new File(redirectsDir).mkdirs();
		new File(pageId_PageTitleDir).mkdirs();
		new File(pageTitle_PageIdDir).mkdirs();
		
		final HashMap<Character, PrintWriter> redirectWriters = new HashMap<Character, PrintWriter>();
		final HashMap<Character, PrintWriter> pageId_PageTitleWriters = new HashMap<Character, PrintWriter>();
		final HashMap<Character, PrintWriter> pageTitle_PageIdWriters = new HashMap<Character, PrintWriter>();
		
		try {
			pageId_PageTitleOther = new PrintWriter(pageId_PageTitleDir + "\\other.txt");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			pageTitle_PageIdOther = new PrintWriter(pageTitle_PageIdDir + "\\other.txt");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			redirectsOther = new PrintWriter(redirectsDir + "\\other.txt");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		try{

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

						if(count % 10000 == 0) {
							System.out.println(count);
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

						// Check for redirects
						HashSet<String> redLinks = redirectTokenizer.tokenize(lastString());

						if(redLinks.size() > 0) {

							// Write to file
							for(String redirect : redLinks) {
								
								PrintWriter pw = null;
								
								if( !Pattern.matches("^[A-Za-z0-9]{1}.*", lastPageTitle )) {
									
									pw = redirectsOther;
									
								} else if( redirectWriters.containsKey(lastPageTitle.charAt(0))) {
									pw = redirectWriters.get(lastPageTitle.charAt(0));
								} else {
									try {
										pw = new PrintWriter(redirectsDir + "\\" + lastPageTitle.charAt(0) + ".txt" );
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									redirectWriters.put(lastPageTitle.charAt(0), pw);
								}
								
								// Omit page sections from the redirect
								if(redirect.indexOf("#") >= 0) {
									
									redirect = redirect.substring(0, redirect.indexOf("#") );
									
								}

								pw.println( lastPageTitle + "\t" + redirect );
								pw.flush();
							}


						} else {

							// Not a redirect, store PageId and PageTitle (Fragmented)
							// And the inverse list ( May be needed later on )
							
							PrintWriter pw = null;
							
							if( pageId_PageTitleWriters.containsKey(lastPageId.charAt(0))) {
								pw = pageId_PageTitleWriters.get(lastPageId.charAt(0));
							} else {
								try {
									pw = new PrintWriter(pageId_PageTitleDir + "\\" + lastPageId.charAt(0) + ".txt");
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								pageId_PageTitleWriters.put(lastPageId.charAt(0), pw);
							}
							
							pw.println(lastPageId + "\t" + lastPageTitle);
							pw.flush();
							
							// PageTitle - PageId
							pw = null;
							
							if( !Pattern.matches("^[A-Za-z0-9]{1}.*", lastPageTitle )) {
								
								pw = pageTitle_PageIdOther;
								
							} else if( pageTitle_PageIdWriters.containsKey(lastPageTitle.charAt(0))) {
								pw = pageTitle_PageIdWriters.get(lastPageTitle.charAt(0));
							} else {
								try {
									pw = new PrintWriter(pageTitle_PageIdDir + "\\" + lastPageTitle.charAt(0) + ".txt");
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.out.println(lastPageId + ""  + lastPageTitle);
								}
								pageTitle_PageIdWriters.put(lastPageTitle.charAt(0), pw);
							}
							
							pw.println(lastPageTitle + "\t" + lastPageId);
							pw.flush();

							
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

			File xmlFile = new File( dumpFileName);

			InputStream inputStream= new FileInputStream(xmlFile);
			InputStreamReader inputReader = new InputStreamReader(inputStream,"UTF-8");

			InputSource inputSource = new InputSource(inputReader);

			inputSource.setEncoding("UTF-8");

			saxParser.parse(inputSource, handler);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
