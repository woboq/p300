/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import de.guruz.p300.Configuration;
import de.guruz.p300.eventstream.Event;
import de.guruz.p300.eventstream.EventManager;
import de.guruz.p300.eventstream.MicroblogEvent;
import de.guruz.p300.hosts.Host;

/**
 * Tests for {@link de.guruz.p300.eventstream.EventManager}
 * @author tomcat
 *
 */
public class EventManagerTest extends TestCase {

	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventManager#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		EventManager manager = EventManager.getInstance();
		EventManager manager2 = EventManager.getInstance();
		assertEquals("EventManager should be singleton: Failed", manager, manager2);
	}

	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventManager#addEvent(de.guruz.p300.eventstream.Event)}.
	 */
	@Test
	public void testAddEvent() {
		Host host = new Host("TestUUID");
		host.setDisplayName("TestHost");
		host.addIPAndPort("localhost", 4337, 0);
		Date oldDate = new Date(new Date().getTime() - 777600000);
		MicroblogEvent oldEvent = new MicroblogEvent(host, oldDate, "Test");
		MicroblogEvent currentEvent = new MicroblogEvent(host, new Date(), "Test2");
		MicroblogEvent currentEvent2 = new MicroblogEvent(host, new Date(), "Test3");
		EventManager.getInstance().addEvent(oldEvent);
		EventManager.getInstance().addEvent(currentEvent);
		EventManager.getInstance().addEvent(currentEvent2);
		Event[] events = EventManager.getInstance().getEvents();
		assertEquals(2, events.length);
		assertEquals(currentEvent, events[0]);
		assertEquals(currentEvent2, events[1]);
		
		FileReader fileReader = null;
		try {
			 fileReader = new FileReader(Configuration.configDirFileName("events.rss"));
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
			fail("events.rss not found");
		}
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		StringWriter writer = new StringWriter();
		try {
			String line = bufferedReader.readLine();
			while (line != null) {
				writer.write(line);
				line = bufferedReader.readLine();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		String eventFileContents = writer.toString();

		String[] required = new String[] {
				"<channel>", "</channel>", "<title>", "</title>", "<link>", "</link>", "<description>", "</description>",
				"<item>", "</item>", "<author>", "</author>", "Test2", "Test3", "<title>", "</title>",
				"TestUUID", "TestHost", "http://localhost:4337", "<rss version=\"2.0\">", "</rss>"
		};

		for (String word: required) {
			assertTrue(word + " doesn't exist.", eventFileContents.contains(word));
		}

	}

}
