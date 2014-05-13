package edu.sjsu.cs286.assignment2;

public class Common {

	public static String basePatternGen = "(.*)";
	public static String linkPatternGen =  "\\[\\[" + basePatternGen + "\\]\\]";
	public static String redirectPatternGen = "(#REDIRECT|#redirect) " + "\\[\\[" + basePatternGen + "\\]\\]";

	public static String basePattern = "([A-Za-z0-9\\_\\.\\-\\s\\#\\(\\)]{1,})(\\|{1}[A-Za-z0-9\\_\\.\\-\\,]{1,}){0,1}";
	public static String linkPattern = "\\[\\[" + basePattern + "\\]\\]";
	public static String redirectPattern = "(#REDIRECT|#redirect) " + "\\[\\[" + basePatternGen + "\\]\\]";

	public static String dumpFileName = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\sample-enwiki-1.xml";
	public static String baseDir = "C:\\SJSU\\Sem2\\CS286-IR\\Assignment 2\\Sample";

	public static String pageId_PageTitleDir = baseDir + "\\PageId_PageTitle";
	public static String pageTitle_PageIdDir = baseDir + "\\PageTitle_PageId";
	public static String redirectsDir = baseDir + "\\Redirects";

	public static String outboundDir = baseDir + "\\outbound";
	public static String inboundDir = baseDir + "\\inbound";

	public static String mergedOutboundDir = outboundDir + "\\merged";
	public static String mergedInboundDir = inboundDir + "\\merged";

	public static String pageRankDir = baseDir + "\\PageRank";

	public static String finalPageRanksFile = baseDir + "\\PageRank\\PageRanks.txt";
	
	public static Double convergenceThreshold = 0.003D;

	public static void main(String[] args) {

		if(args.length <2) {
			System.out.println("Missing Dump and Base Dir.");
			System.out.println("Prepare Data: <Dump> <Base Dir>");
			System.out.println("PageRank: <Dump> <BaseDir> [PageRank]");
			System.out.println("CombinePageRanks (Once convergence is achieved): <Dump> <BaseDir> [CombinePageRank]");
			System.exit(-1);
		}

		dumpFileName = args[0];
		baseDir = args[1];

		pageId_PageTitleDir = baseDir + "\\PageId_PageTitle";
		pageTitle_PageIdDir = baseDir + "\\PageTitle_PageId";
		redirectsDir = baseDir + "\\Redirects";

		outboundDir = baseDir + "\\outbound";
		inboundDir = baseDir + "\\inbound";

		mergedOutboundDir = outboundDir + "\\merged";
		mergedInboundDir = inboundDir + "\\merged";

		pageRankDir = baseDir + "\\PageRank";

		finalPageRanksFile = baseDir + "\\PageRank\\PageRanks.txt";

		if(args.length == 3) {

			if(args[2].equalsIgnoreCase("pagerank")){

				long count = 1;
				Double diff = Double.MAX_VALUE;

				do{

					System.out.println("Computing pageranks. Run " + count);
					long start = System.nanoTime();
					diff = Step5_PageRank.main(args);
					long end = System.nanoTime();

					System.out.println("Time taken:" + (end - start)/1000000 + " ms");
					
					if(diff>convergenceThreshold) {
						System.out.println("Difference not under threshold. Recomputing PageRanks.");
					}
					count ++;

				}while(diff>convergenceThreshold);

			} else if(args[2].equalsIgnoreCase("combinepageranks")){

				System.out.println("Combining pageranks");
				long start = System.nanoTime();
				Step6_MergePageRanksIntoSingleFile.main(args);
				long end = System.nanoTime();

				System.out.println("Time taken:" + (end - start)/1000000 + " ms");
			} else {
				System.out.println(args[2] + " unrecognized.");
			}

		} else {

			long start = System.nanoTime();

			System.out.println("Starting Step1");
			Step1_CollectRedirectsAndPageId_PageTitle.main(args);
			System.out.println("Starting Step2");
			Step2_ProcessPages.main(args);
			System.out.println("Starting Step3");
			Step3_MergeIntoLists.main(args);
			System.out.println("Starting Step4");
			Step4_CountPagesAndCreateInitiaFile.main(args);

			long end = System.nanoTime();

			System.out.println("Time taken:" + (end - start)/1000000 + " ms");
		}
	}

}
