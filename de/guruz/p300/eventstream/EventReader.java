/**
 * 
 */
package de.guruz.p300.eventstream;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostLocation;
import de.guruz.p300.utils.DOMUtils;

/**
 * Read events from an RSS
 * @author tomcat
 *
 */
public class EventReader extends BufferedReader {

	/**
	 * The source host of the events to be read
	 */
	private Host sourceHost = null;
	
	/**
	 * Create a new RSS event reader, using the given Reader as backend
	 * @param in The reader to read the RSS from
	 * @param host The host to validate against & set as source host
	 */
	public EventReader(Reader in, Host host) {
		super(in);
		if (host == null) {
			host = new Host("");
		}
		sourceHost = host;
	}
	
	/**
	 * Read an array of events from the backend Reader
	 * @return An array of events
	 */
	public Event[] readEvents() {
		InputSource source = new InputSource(this);
		Document doc = DOMUtils.documentFromInputSource(source);
		if (!validRSS(doc)) {
			return new Event[0];
		}
		List<Event> events = new ArrayList<Event>();
		NodeList channelChildren = DOMUtils.getFirstNamedChild(doc.getDocumentElement(), "channel").getChildNodes();
		for (int i = 0; i < channelChildren.getLength(); i++) {
			Node node = channelChildren.item(i);
			if (node instanceof Element && node.getNodeName().equals("item")) {
				Event event = EventFactory.createEvent((Element)node, sourceHost);
				if (event != null) {
					events.add(event);
				}
			}
		}
		return events.toArray(new Event[0]);
	}

	/**
	 * Check if the read XML document is a valid event RSS
	 * @param doc The XML document
	 * @return True if it is valid RSS; False otherwise
	 */
	private boolean validRSS(Document doc) {
//		System.err.println("validRSS(Document)");
		HostLocation location = sourceHost.getBestHostLocation();
//		System.err.println("Check location");
		if (location == null) {
			return false;
		}
//		System.err.println("Check document");
		if (doc == null) {
			return false;
		}
		Node rss = doc.getDocumentElement();
		if (rss == null) {
			return false;
		}
		Node channel = DOMUtils.getFirstNamedChild(rss, "channel");
//		System.err.println("Check channel");
		if (channel == null) {
			return false;
		}
		NodeList channelChildren = channel.getChildNodes();
//		System.err.println("Check channel children");
		if (channelChildren == null) {
			return false;
		}
		for (int i = 0; i < channelChildren.getLength(); i++) {
			Node node = channelChildren.item(i);
			if (node.getNodeName() != null && node.getNodeName().equals("link")) {
				Node linkText = DOMUtils.getFirstTextChild(node);
				String link = linkText.getNodeValue();
//				System.err.println("Check channel child #" + i);
				if (link == null || !link.contains(location.getIp()) || !link.contains(Integer.toString(location.getPort()))) {
					return false;
				}
			}
		}
//		System.err.println("RSS good");
		return true;
	}
	
}
