/**
 * 
 */
package de.guruz.p300.tests.automated;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;

import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.webdav.search.client.SimpleSearchResultCollector;

/**
 * Tests for de.guruz.p300.webdav.search.client.SimpleSearchResultCollector
 * @author tomcat
 *
 */
public class SimpleSearchResultCollectorTest extends TestCase {

	private SimpleSearchResultCollector mySSRC = new SimpleSearchResultCollector();
	private String expectedString = "";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		mySSRC.setOutput(new PrintStream(System.err) {

			/* (non-Javadoc)
			 * @see java.io.PrintStream#println(java.lang.String)
			 */
			@Override
			public void println(String x) {
				SimpleSearchResultCollectorTest.assertEquals(x, expectedString);
			}
			
		});
	}
		
	public void testHasError() {
		Host h = new Host("Testhost");
		String msg = "Testmessage";
		expectedString = "Host " + h.toString() + " returned an error when searching.";
		mySSRC.hasError(h, msg);
	}
	
	public void testNewResult() {
		Host h = new Host("Testhost");
		List<RemoteEntity> r = new ArrayList<RemoteEntity>();
		expectedString = "Host " + h.toString() + " returned new results: " + r.toString();
		mySSRC.newResult(h, r);
	}
	
	public void testNoResult() {
		Host h = new Host("Testhost");
		expectedString = "Host " + h.toString() + " had no results.";
		mySSRC.noResult(h);
	}

}
