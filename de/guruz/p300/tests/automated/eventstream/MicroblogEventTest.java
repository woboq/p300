/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.io.StringReader;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import de.guruz.p300.eventstream.BadRSSException;
import de.guruz.p300.eventstream.MicroblogEvent;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.DOMUtils;

/**
 * Tests for {@link de.guruz.p300.eventstream.MicroblogEvent}
 * @author tomcat
 *
 */
public class MicroblogEventTest extends TestCase {

	/**
	 * Test method for {@link de.guruz.p300.eventstream.MicroblogEvent#toRSS(org.w3c.dom.Element)}.
	 */
	@Test
	public void testToRSS() {
		Host h = new Host("");
		Date d = new Date();
		MicroblogEvent e = new MicroblogEvent(h, d, "Test on Microblog");
		
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
			fail(ex.getLocalizedMessage());
		}
		
		Element el = doc.createElement("item");
		e.toRSS(el);
		
		assertEquals("item", el.getNodeName());
		Node titleNode = DOMUtils.getFirstNamedChild(el, "title");
		Node descriptionNode = DOMUtils.getFirstNamedChild(el, "description");
		assertNotNull(titleNode);
		assertNotNull(descriptionNode);
		Node titleTextNode = DOMUtils.getFirstTextChild(titleNode);
		Node descriptionTextNode = DOMUtils.getFirstTextChild(descriptionNode);
		assertNotNull(titleTextNode);
		assertNotNull(descriptionNode);
		String title = titleTextNode.getNodeValue();
		String description = descriptionTextNode.getNodeValue();
		assertNotNull(title);
		assertNotNull(description);
		assertEquals("MicroblogEvent", title);
		assertEquals("Test on Microblog", description);
		
		// Tests for stuff coming from Event base class
		Node sourceNode = DOMUtils.getFirstNamedChild(el, "source");
		assertNotNull(sourceNode);
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.MicroblogEvent#MicroblogEvent(de.guruz.p300.hosts.Host, java.util.Date, java.lang.String)}.
	 */
	@Test
	public void testMicroblogEventHostDateStringGoodString() {
		Host host = new Host("");
		Date date = new Date();
		MicroblogEvent event = new MicroblogEvent(host, date, "Test on Microblog");
		
		assertNotNull(event);
		assertEquals("Test on Microblog", event.getBlogText());
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.MicroblogEvent#MicroblogEvent(de.guruz.p300.hosts.Host, java.util.Date, java.lang.String)}.
	 */
	@Test
	public void testMicroblogEventHostDateStringNullString() {
		Host host = new Host("");
		Date date = new Date();
		MicroblogEvent event = new MicroblogEvent(host, date, null);
		
		assertNotNull(event);
		assertNotNull(event.getBlogText());
		assertEquals("", event.getBlogText());
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.MicroblogEvent#MicroblogEvent(org.w3c.dom.Element, de.guruz.p300.hosts.Host)}.
	 */
	@Test
	public void testMicroblogEventElementHostError() {
		Host host = new Host("");
		String invalidRSS = "<item />";
		Document doc = DOMUtils.documentFromInputSource(new InputSource(new StringReader(invalidRSS)));
//		System.err.println(doc.getDocumentElement());
		MicroblogEvent event = null;
		try {
			event = new MicroblogEvent(doc.getDocumentElement(), host);
		} catch (BadRSSException exception) {
//			System.err.println(exception.getLocalizedMessage());
			exception.printStackTrace();
		}
		assertNull(event);
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.MicroblogEvent#MicroblogEvent(org.w3c.dom.Element, de.guruz.p300.hosts.Host)}.
	 */
	@Test
	public void testMicroblogEventElementHostNoError() {
		Host host = new Host("TestUUID");
		host.setDisplayName("TestHost");
		host.addIPAndPort("localhost", 4337, 0);
		Date date = new Date();
		MicroblogEvent event = new MicroblogEvent(host, date, "Test on Microblog");
		assertNotNull(event);
		
		Element item = null;
		try {
			item = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("item");
		} catch (ParserConfigurationException exception) {
			exception.printStackTrace();
			fail(exception.getLocalizedMessage());
		}
		event.toRSS(item);
//		System.err.println(DOMUtils.prettyPrintXMLDocument(item));
		
		MicroblogEvent event2 = null;
		try {
			event2 = new MicroblogEvent(item, host);
		} catch (BadRSSException exception) {
			exception.printStackTrace();
			fail(exception.getLocalizedMessage());
		}
		assertNotNull(event2);
		assertNotNull(event2.getBlogText());
		assertEquals("Test on Microblog", event2.getBlogText());
		
		// Tests for stuff in Event base class
		assertEquals(host, event2.getSourceHost());
	}
	
	/**
	 * Test for {@link de.guruz.p300.eventstream.MicroblogEvent#isOld()}
	 */
	public void testIsOld() {
		Host host = new Host("TestUUID");
		host.setDisplayName("TestHost");
		host.addIPAndPort("localhost", 4337, 0);
		MicroblogEvent newEvent = new MicroblogEvent(host, new Date(), "Test");
		assertFalse(newEvent.isOld());
		
		Date oldDate = new Date(new Date().getTime() - 864000000);
		MicroblogEvent oldEvent = new MicroblogEvent(host, oldDate, "Test");
		assertTrue(oldEvent.isOld());
	}

}
