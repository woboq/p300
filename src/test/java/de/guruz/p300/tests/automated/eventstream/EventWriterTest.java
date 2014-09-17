/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import de.guruz.p300.eventstream.EventWriter;
import de.guruz.p300.eventstream.MicroblogEvent;
import de.guruz.p300.hosts.Host;

/**
 * Tests for {@link de.guruz.p300.eventstream.EventWriter}
 * @author tomcat
 *
 */
public class EventWriterTest extends TestCase {

	/**
	 * Test method for {@link de.guruz.p300.eventstream.EventWriter#EventWriter(java.io.Writer)}.
	 */
	@Test
	public void testEventWriter() {
		StringWriter stringWriter = new StringWriter();
		Host host = new Host("TestUUID");
		host.setDisplayName("TestHost");
		host.addIPAndPort("localhost", 4337, 0);
		EventWriter eventWriter = new EventWriter(stringWriter, host);
		MicroblogEvent event1 = new MicroblogEvent(host, new Date(), "Test on Microblog");
		MicroblogEvent event2 = new MicroblogEvent(host, new Date(), "Test2 on Microblog");
		
		eventWriter.writeEvents(new MicroblogEvent[] { event1, event2 });
		
		String output = stringWriter.toString();
		String[] required = new String[] {
				"<channel>", "</channel>", "<title>", "</title>", "<link>", "</link>", "<description>", "</description>",
				"<item>", "</item>", "<author>", "</author>", "Test on Microblog", "Test2 on Microblog", "<title>", "</title>",
				"TestUUID", "TestHost", "http://localhost:4337", "<rss version=\"2.0\">", "</rss>"
		};

		for (String word: required) {
			assertTrue(word + " doesn't exist.", output.contains(word));
		}
	}

}
