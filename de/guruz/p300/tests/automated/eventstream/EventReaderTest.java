/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import de.guruz.p300.eventstream.Event;
import de.guruz.p300.eventstream.EventReader;
import de.guruz.p300.eventstream.EventWriter;
import de.guruz.p300.eventstream.MicroblogEvent;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostLocation;

/**
 * Tests for {@link de.guruz.p300.eventstream.EventReader}
 * @author tomcat
 *
 */
public class EventReaderTest extends TestCase {

	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventReader#EventReader(java.io.Reader)}.
	 */
	@Test
	public void testEventReader() {
		Host host = new Host("TestUUID");
		host.setDisplayName("TestHost");
		host.addIPAndPort("localhost", 4337, 0);
		MicroblogEvent event1 = new MicroblogEvent(host, new Date(), "Test1 on Microblog");
		MicroblogEvent event2 = new MicroblogEvent(host, new Date(), "Test2 on Microblog");
		MicroblogEvent event3 = new MicroblogEvent(host, new Date(), null);
		
		StringWriter stringWriter = new StringWriter();
		EventWriter eventWriter = new EventWriter(stringWriter, host);
		eventWriter.writeEvents(new Event[] { event1, event2, event3 });
		String output = stringWriter.toString();
		
		StringReader stringReader = new StringReader(output);
		EventReader eventReader = new EventReader(stringReader, host);
		assertNotNull(eventReader);

		Event[] events = eventReader.readEvents();
		assertNotNull(events);
		assertEquals(3, events.length);
		
		for (Event event: events) {
			assertNotNull(event);
			assertTrue(event instanceof MicroblogEvent);
			MicroblogEvent microblogEvent = (MicroblogEvent)event;
			host = microblogEvent.getSourceHost();
			assertNotNull(host);
			assertEquals("TestUUID", host.getUUID());
			assertEquals("TestHost", host.getDisplayName());
			HostLocation location = host.getBestHostLocation();
			assertNotNull(location);
			assertEquals("localhost", location.getIp());
			assertEquals(4337, location.getPort());
			
			Date date = microblogEvent.getTimestamp();
			assertNotNull(date);
			// Write & read shouldn't take more than 10s = 10.000 ms
			assertTrue(new Date().getTime() - date.getTime() < 10000);
			
			String text = microblogEvent.getBlogText();
			assertNotNull(text);
		}
		assertEquals("Test1 on Microblog", ((MicroblogEvent)events[0]).getBlogText());
		assertEquals("Test2 on Microblog", ((MicroblogEvent)events[1]).getBlogText());
		assertEquals("", ((MicroblogEvent)events[2]).getBlogText());
	}

}
