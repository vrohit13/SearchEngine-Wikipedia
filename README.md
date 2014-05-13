SearchEngine-Wikipedia
======================

A search engine for Wikipedia implemented on Core Java.

Contributors: 

Rohit Vobbilisetty
Tanuvir Singh
Bhavna Gurnani


Assignment 1:

Create Inverted index for the Wikipedia Dump.


Assignment 2:

Compute inlinks and outlinks for each page within the Wikipedia dump. Finally use this information to compute the PageRank for each page. 
Algorithm used from the "Introduction to Information Retrieval' by Christopher D. Manning, Prabhakar Raghavan and Hinrich Schütze.


Assignment 3:

Similar to assignment 1, where an inverted index is created using the information from each page. Also calculates the tf-idf associated for the term and page.
The page rank implementation was rewritten as part of this assignment, using a more optimized approach.

This implementation also includes optimized file handle management, data structures and making sure that the program does not run out of memory during execution.

The Search module is a CLI, which accepts a query, fetches the posting list associated with each query term followed by an intersection of the Page Id's.

The results are displayed in the decreasing order of the tf-idf values. (Page Ranks to be included soon)
