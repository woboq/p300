/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.guruz.p300.eventstream.Event;
import de.guruz.p300.eventstream.EventFactory;
import de.guruz.p300.eventstream.MicroblogEvent;
import de.guruz.p300.hosts.Host;

/**
 * Tests for {@link de.guruz.p300.eventstream.EventFactory}
 * @author tomcat
 *
 */
public class EventFactoryTest extends TestCase {

	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventFactory#createEvent(org.w3c.dom.Element)}.
	 */
	@Test
	public void testCreateEventError() {
		Element item = createEmptyItem();
		Document doc = item.getOwnerDocument();
		Element title = doc.createElement("title");
		Node titleText = doc.createTextNode("MicroblogEvent");
		title.appendChild(titleText);
		item.appendChild(title);
		
		Event e = EventFactory.createEvent(item, new Host(""));
		assertNull(e);
	}
	
	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventFactory#createEvent(org.w3c.dom.Element)}.
	 */
	@Test
	public void testCreateEventNoClass() {
		Element item = createEmptyItem();
		Document doc = item.getOwnerDocument();
		Element title = doc.createElement("title");
		Node titleText = doc.createTextNode("TestEvent");
		title.appendChild(titleText);
		item.appendChild(title);
		
		Event e = EventFactory.createEvent(item, new Host(""));
		assertNull(e);
	}
	
	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventFactory#createEvent(org.w3c.dom.Element)}.
	 */
	@Test
	public void testCreateEventMicroblog() {
		Host host = new Host("TestUUID");
		host.setDisplayName("TestHost");
		host.addIPAndPort("localhost", 4337, 0);
		MicroblogEvent microblogEvent = new MicroblogEvent(host, new Date(), "Test on Microblog");
		Element item = createEmptyItem();
		microblogEvent.toRSS(item);
		
		Event event = EventFactory.createEvent(item, host);
		assertTrue(event instanceof MicroblogEvent);
		microblogEvent = (MicroblogEvent)event;
		assertEquals(host, microblogEvent.getSourceHost());
	}
	
	/**
	 * Create an "item" element
	 * @return An empty item element (XML)
	 */
	private Element createEmptyItem() {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException exception) {
			exception.printStackTrace();
			fail(exception.getLocalizedMessage());
			return null;
		}
		
		return doc.createElement("item");
	}

}
