/**
 * 
 */
package de.guruz.p300.tests.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.webdav.search.client.SearchResultCollector;
import de.guruz.p300.webdav.search.client.WebDAVSearchClient;

/**
 * @author tomcat
 * Test the WebDAV SEARCH Client
 * The client takes a search string, creates an XML query from it, send it to all given hosts, and calls a collector on completion with the result of the remote host
 * The result can be a list of files, "no result", or an error message
 *
 */
public class WebDAVSearchClientTest implements SearchResultCollector {

	private ResultType expectedResult;
	private boolean called = false;
	private boolean searchForTestFile = false;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MainDialog.main(new String[] {"--headless"});
		called = false;
	}

	/**
	 * Search on a specified host for a specified search term
	 * Wait for notification from the collector thread, or a timeout of 3s
	 * Check for a specific outcome (expectation)
	 * @param host
	 * @param search
	 */
	private void searchFor(String host, String search, ResultType expectation) {
		Host target = new Host("TestHost");
		target.addIPAndPort(host, MainDialog.getCurrentHTTPPort(), 0);
		WebDAVSearchClient.searchSpecificHosts(search, Collections.singletonList((SearchResultCollector)this), Collections.singletonList(target));
		expectedResult = expectation;
		try {
			synchronized (this) {
				while (!called) {
					wait(3000);
				}
			}
		} catch (InterruptedException interrupt) {
			System.err.println("Wait interrupted: " + interrupt.getLocalizedMessage());
		}
		assertTrue(called);
	}
	
	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.WebDAVSearchClient#searchSpecificHosts(java.lang.String, java.util.List, java.util.List)}.
	 * Search for the TestFile on localhost
	 */
	@Test
	public void testSearchSpecificHostsWithResult1() {
		searchForTestFile = true;
		searchFor("localhost", "TestFile", ResultType.RESULT);
	}
	
	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.WebDAVSearchClient#searchSpecificHosts(java.lang.String, java.util.List, java.util.List)}.
	 * Search for the TestDir on localhost
	 */
	@Test
	public void testSearchSpecificHostsWithResult2() {
		searchForTestFile = false;
		searchFor("localhost", "TestDirectory", ResultType.RESULT);
	}


	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.WebDAVSearchClient#searchSpecificHosts(java.lang.String, java.util.List, java.util.List)}.
	 * Search for something that does not exist on localhost
	 */
	@Test
	public void testSearchSpecificHostsNoResult() {
		searchFor("localhost", "File that does not exist", ResultType.NORESULT);
	}

	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.WebDAVSearchClient#searchSpecificHosts(java.lang.String, java.util.List, java.util.List)}.
	 * Create a "can't connect" error in search
	 */
	@Test
	public void testSearchSpecificHostsError() {
		searchFor("256.256.256.256", "", ResultType.ERROR);
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#hasError(de.guruz.p300.hosts.Host, java.lang.String)
	 */
	public void hasError(Host host, String msg) {
		System.err.println("hasError()");
		called = true;
		synchronized (this) {
			notify();
		}
		assertEquals(expectedResult, ResultType.ERROR);
		assertGoodHost(host, "256.256.256.256");
		assertNotNull(msg);
		assertTrue(msg.startsWith("Could not connect to host"));
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#newResult(de.guruz.p300.hosts.Host, java.util.List)
	 */
	public void newResult(Host host, List<RemoteEntity> resultList) {
		called = true;
		synchronized (this) {
			notify();
		}
		assertEquals(expectedResult, ResultType.RESULT);
		assertGoodHost(host, "localhost");
		assertNotNull(resultList);
		assertEquals(1, resultList.size());
		if (searchForTestFile) {
			System.err.println("Searched for file");
			RemoteEntity testFile = resultList.get(0);
			assertTrue(testFile.getPath().endsWith("/TestFile"));
			assertEquals(42, testFile.getSize());
			assertTrue(testFile instanceof RemoteFile);
		} else {
			System.err.println("Searched for dir");
			RemoteEntity testDir = resultList.get(0);
			assertTrue(testDir.getPath().endsWith("/TestDirectory"));
			assertEquals(0, testDir.getSize());
			assertTrue(testDir instanceof RemoteDir);
		}
	}

	/**
	 * Check if the received host has good data
	 * @param host
	 */
	private void assertGoodHost(Host host, String ip) {
		assertNotNull(host);
		assertNotNull(host.getBestHostLocation());
		assertEquals("TestHost", host.getUUID());
		assertEquals(ip, host.getBestHostLocation().getIp());
		assertEquals(MainDialog.getCurrentHTTPPort(), host.getBestHostLocation().getPort());
	}
	
	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#noResult(de.guruz.p300.hosts.Host)
	 */
	public void noResult(Host host) {
		called = true;
		synchronized (this) {
			notify();
		}
		assertEquals(expectedResult, ResultType.NORESULT);
		assertGoodHost(host, "localhost");
	}

}