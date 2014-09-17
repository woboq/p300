/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.guruz.p300.eventstream.BadRSSException;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.DOMUtils;

/**
 * Tests for {@link de.guruz.p300.eventstream.Event}
 * @author tomcat
 *
 */
public class EventTest extends TestCase {

	/**
	 * Test method for {@link de.guruz.p300.eventstream.Event#Event(de.guruz.p300.hosts.Host, java.util.Date)}.
	 */
	@Test
	public void testEventHostDate() {
		Host h = new Host("TestHost");
		Date d = new Date();
		
		EventSubclass e = new EventSubclass(h, d);
		
		assertSame(h, e.getSourceHost());
		assertSame(d, e.getTimestamp());
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.Event#Event(org.w3c.dom.Element, de.guruz.p300.hosts.Host)}.
	 */
	@Test
	public void testEventElementHost() {
		// Invalid RSS
		EventSubclass e = testEventElementHost("<bla />");
		assertNull(e);
		
		// Empty item
		e = testEventElementHost("<item />");
		assertNull(e);
		
		// Empty author, pubDate, source
		e = testEventElementHost("<item><author /><pubDate /><source /></item>");
		assertNull(e);

		// Invalid date
		e = testEventElementHost("<item><author>TestHost</author><pubDate>Maeh</pubDate><source url=\"http://localhost:4337/\">TestUUID</source></item>");
		assertNull(e);
		
		// Source has no url
		e = testEventElementHost("<item><author>TestHost</author><pubDate>02 Oct 08 15:30:00 +0200</pubDate><source>TestHost</source></item>");
		assertNull(e);
		
		// invalid year
		e = testEventElementHost("<item><author>TestHost</author><pubDate>02 Oct 08 15:30:00 +0200</pubDate><source url=\"http://localhost:4337/\">TestUUID</source></item>");
		assertNull(e);
		
		// Valid RSS
		e = testEventElementHost("<item><author>TestHost</author><pubDate>02 Oct 2008 15:30:00 +0200</pubDate><source url=\"http://localhost:4337/\">TestUUID</source></item>");
		assertNotNull(e);
		assertNotNull(e.getSourceHost());
		assertEquals("TestUUID", e.getSourceHost().getUUID());
		assertEquals("TestHost", e.getSourceHost().getDisplayName());
		assertNotNull(e.getSourceHost().getBestHostLocation());
		assertEquals("localhost", e.getSourceHost().getBestHostLocation().getIp());
		assertEquals(4337, e.getSourceHost().getBestHostLocation().getPort());
		Date d = e.getTimestamp();
		assertNotNull(d);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(d);
		assertEquals(2, gc.get(GregorianCalendar.DAY_OF_MONTH));
		// Remember: 9 is October in 0-based months
		assertEquals(9, gc.get(GregorianCalendar.MONTH));
		assertEquals(2008, gc.get(GregorianCalendar.YEAR));
		assertEquals(15, gc.get(GregorianCalendar.HOUR_OF_DAY));
		assertEquals(30, gc.get(GregorianCalendar.MINUTE));
		assertEquals(0, gc.get(GregorianCalendar.SECOND));
		assertEquals(1 * 60 * 60 * 1000, gc.get(GregorianCalendar.ZONE_OFFSET));
		assertEquals(1 * 60 * 60 * 1000, gc.get(GregorianCalendar.DST_OFFSET));
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.Event#Event(org.w3c.dom.Element, de.guruz.p300.hosts.Host)}. 
	 * @param rss The String that contains XML to use as Element
	 */
	private EventSubclass testEventElementHost(String rss) {
		Document d = null;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(rss)));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		if (d == null) {
			fail("Error");
		}

		EventSubclass e = null;
		try {
			Host h = new Host("TestUUID");
			h.setDisplayName("TestHost");
			h.addIPAndPort("localhost", 4337, 0);
			e = new EventSubclass((Element)d.getFirstChild(), h);
		} catch (BadRSSException badRSS) {
			e = null;
		}
		return e;
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.Event#toRSS(org.w3c.dom.Element)}.
	 */
	@Test
	public void testToRSS() {
		EventSubclass e = testEventElementHost("<item><author>TestHost</author><pubDate>02 Oct 2008 15:30:00 +0200</pubDate><source url=\"http://localhost:4337/\">TestUUID</source></item>");
		assertNotNull(e);
		Document d = null;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		if (d == null) {
			fail("Error");
		}
		Element item = d.createElement("item");
		e.toRSS(item);
		assertEquals("item", item.getNodeName());
		Node authorNode = DOMUtils.getFirstNamedChild(item, "author");
		Node pubDateNode = DOMUtils.getFirstNamedChild(item, "pubDate");
		Node sourceNode = DOMUtils.getFirstNamedChild(item, "source");
		assertNotNull(authorNode);
		assertNotNull(pubDateNode);
		assertNotNull(sourceNode);
		Node authorTextNode = DOMUtils.getFirstTextChild(authorNode);
		Node pubDateTextNode = DOMUtils.getFirstTextChild(pubDateNode);
		Node sourceTextNode = DOMUtils.getFirstTextChild(sourceNode);
		assertNotNull(authorTextNode);
		assertNotNull(pubDateTextNode);
		assertNotNull(sourceTextNode);
		assertEquals("TestHost", authorTextNode.getNodeValue());
		assertEquals("02 Oct 2008 15:30:00 +0200", pubDateTextNode.getNodeValue());
		assertEquals("TestUUID", sourceTextNode.getNodeValue());
		assertNotNull(sourceNode.getAttributes());
		assertNotNull(sourceNode.getAttributes().getNamedItem("url"));
		assertEquals("http://localhost:4337/", sourceNode.getAttributes().getNamedItem("url").getNodeValue());
	}

}