/**
 *
 */
package de.guruz.p300.tests.automated;

import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.DOMUtils;
import de.guruz.p300.webdav.search.client.SearchResultCollector;
import de.guruz.p300.webdav.search.client.SingleHostSearchWorker;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test for {@link de.guruz.p300.webdav.search.client.SingleHostSearchWorker}.
 * @author tomcat
 *
 */
public class SingleHostSearchWorkerTest {

	SearchResultCollector myResultCollector = new TestResultCollector();

	private static ResultType expectedResult;

	/**
	 * Get the expected result of the current test
	 * @return the expectedResult
	 */
	public static ResultType getExpectedResult() {
		return expectedResult;
	}

	/**
	 * Set up the worker
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MainDialog.main(new String[] { "--headless" });
	}

	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.SingleHostSearchWorker#run()}.
	 * Try to search a nonexisting IP, creating an error
	 */
        @Ignore(value = "should read the file fromresource folder")
	@Test
	public void testRunWithError() {
		Host mySearchHost = new Host("Testhost");
		mySearchHost.addIPAndPort("256.256.256.256", 0, 0);
		Document query = null;
		try {
			query = DOMUtils.documentFromInputStream(new FileInputStream("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery"));
		} catch (FileNotFoundException fileNotFound) {
			fail("File not found");
		}
		SingleHostSearchWorker searchWorker = new SingleHostSearchWorker(Collections.singletonList(myResultCollector), mySearchHost, query);
		expectedResult = ResultType.ERROR;
		searchWorker.run();
		assertTrue(TestResultCollector.isCalled());
		TestResultCollector.resetCalled();
	}

	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.SingleHostSearchWorker#run()}.
	 * Regular search, should return the TestFile
	 */
        @Ignore(value = "should read the file fromresource folder")
	@Test
	public void testRunWithResult() {
		Host mySearchHost = new Host("Testhost");
		int myPort = MainDialog.getCurrentHTTPPort();
//		int myPort = 3333;
		mySearchHost.addIPAndPort("127.0.0.1", myPort, 0);
		expectedResult = ResultType.RESULT;
		Document query = null;
		try {
			query = DOMUtils.documentFromInputStream(new FileInputStream("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery2"));
		} catch (FileNotFoundException fileNotFound) {
			fail("File not found");
		}
		SingleHostSearchWorker searchWorker = new SingleHostSearchWorker(Collections.singletonList(myResultCollector), mySearchHost, query);
		searchWorker.run();
		assertTrue(TestResultCollector.isCalled());
		TestResultCollector.resetCalled();
	}

	/**
	 * Test method for {@link de.guruz.p300.webdav.search.client.SingleHostSearchWorker#run()}.
	 * Regular search, should return no result
	 */
        @Ignore(value = "should read the file fromresource folder")
	@Test
	public void testRunWithNoResult() {
		Host mySearchHost = new Host("Testhost");
		int myPort = MainDialog.getCurrentHTTPPort();
		mySearchHost.addIPAndPort("127.0.0.1", myPort, 0);
		expectedResult = ResultType.NORESULT;
		Document query = null;
		try {
			query = DOMUtils.documentFromInputStream(new FileInputStream("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery5"));
		} catch (FileNotFoundException fileNotFound) {
			fail("File not found");
		}
		SingleHostSearchWorker searchWorker = new SingleHostSearchWorker(Collections.singletonList(myResultCollector), mySearchHost, query);
		searchWorker.run();
		assertTrue(TestResultCollector.isCalled());
		TestResultCollector.resetCalled();
	}

}

/**
 * A collector for search results that can check against expected results
 * @author tomcat
 *
 */
class TestResultCollector implements SearchResultCollector {

	private static boolean called = false;

	/**
	 * Set the collector call state
	 * @param called the called to set
	 */
	public static void resetCalled() {
		TestResultCollector.called = false;
	}

	/**
	 * Check if the collector has been called by the search worker
	 * @return the called
	 */
	public static boolean isCalled() {
		return called;
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#hasError(de.guruz.p300.hosts.Host, java.lang.String)
	 */
	public void hasError(Host host, String msg) {
		assertEquals(SingleHostSearchWorkerTest.getExpectedResult(), ResultType.ERROR);
		called = true;
		assertTrue(msg.startsWith("Could not connect to host"));
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#newResult(de.guruz.p300.hosts.Host, java.util.List)
	 */
	public void newResult(Host host, List<RemoteEntity> resultList) {
		assertEquals(SingleHostSearchWorkerTest.getExpectedResult(), ResultType.RESULT);
		called = true;
		assertNotNull(resultList);
		assertEquals(resultList.size(), 1);
		RemoteEntity remoteEntity = resultList.get(0);
		assertNotNull(remoteEntity);
		assertEquals("TestFile", remoteEntity.getName());
		assertEquals(42, remoteEntity.getSize());
		assertTrue(remoteEntity.getPath().endsWith("/TestFile"));
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.webdav.search.client.SearchResultCollector#noResult(de.guruz.p300.hosts.Host)
	 */
	public void noResult(Host host) {
		assertEquals(SingleHostSearchWorkerTest.getExpectedResult(), ResultType.NORESULT);
		called = true;
	}
}