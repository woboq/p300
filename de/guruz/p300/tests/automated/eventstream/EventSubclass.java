/**
 * 
 */
package de.guruz.p300.tests.automated.eventstream;

import java.util.Date;

import org.w3c.dom.Element;

import de.guruz.p300.eventstream.BadRSSException;
import de.guruz.p300.eventstream.Event;
import de.guruz.p300.hosts.Host;

/**
 * Class that opens up the protected methods of {@link de.guruz.p300.eventstream.Event} for testing.
 * @author tomcat
 *
 */
public class EventSubclass extends Event {

	/**
	 * Create a new Event
	 * @param sourceHost The host that generated the event
	 * @param timestamp Place in time of event
	 */
	public EventSubclass(Host sourceHost, Date timestamp) {
		super(sourceHost, timestamp);
	}

	/**
	 * Create a new Event from RSS item (XML element)
	 * @param item The XML element to read as RSS item
	 * @param host The host that is the source of these events
	 * @throws BadRSSException In case bad RSS data has been given
	 */
	public EventSubclass(Element item, Host host) throws BadRSSException {
		super(item, host);
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.eventstream.Event#toRSS(org.w3c.dom.Element)
	 */
	@Override
	protected void toRSS(Element item) {
		super.toRSS(item);
	}

}
