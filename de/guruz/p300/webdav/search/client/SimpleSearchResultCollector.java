/**
 * 
 */
package de.guruz.p300.webdav.search.client;

import java.io.PrintStream;
import java.util.List;

import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.hosts.Host;

/**
 * Simple implementation of the SearchResultCollector interface
 * Prints out search results
 * @author tomcat
 *
 */
public class SimpleSearchResultCollector implements SearchResultCollector {

	/**
	 * The PrintStream to write the output to
	 */
	private PrintStream output = System.err;
	
	/**
	 * Set the PrintStream to use for output
	 * @param output the output to set
	 */
	public void setOutput(PrintStream output) {
		this.output = output;
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#hasError(de.guruz.p300.hosts.Host, java.lang.String)
	 */
	public void hasError(Host host, String msg) {
		output.println("Host " + host + " returned an error when searching.");
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#newResult(de.guruz.p300.hosts.Host, java.util.List)
	 */
	public void newResult(Host host, List<RemoteEntity> resultList) {
		output.println("Host " + host + " returned new results: " + resultList);
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#noResult(de.guruz.p300.hosts.Host)
	 */
	public void noResult(Host host) {
		output.println("Host " + host + " had no results.");
	}

}
