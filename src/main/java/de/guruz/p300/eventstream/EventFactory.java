/**
 * 
 */
package de.guruz.p300.eventstream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.DOMUtils;

/**
 * Creates event object from XML by determining its type and then invoking the XML constructor
 * @author tomcat
 *
 */
public class EventFactory {

	/**
	 * Read the RSS item (XML element), determine the event type and run the event constructor
	 * @param item RSS item (XML element) of the event
	 * @return An Event child object
	 */
	public static Event createEvent(Element item, Host host) {
		if (!validRSS(item)) {
			return null;
		}
		Node titleNode = DOMUtils.getFirstNamedChild(item, "title");
		Node titleTextNode = DOMUtils.getFirstTextChild(titleNode);
		String title = titleTextNode.getNodeValue();
		try {
			if (title.equals("MicroblogEvent")) {
				return new MicroblogEvent(item, host); 
			}
		} catch (BadRSSException exception) {
			exception.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * Check if the XML element (RSS item) has enough information for the EventFactory
	 * @param item The RSS item (XML element) to be examined
	 * @return True if there is enough information; False otherwise
	 */
	private static boolean validRSS(Element item) {
		if (item == null || !item.getNodeName().equals("item")) {
			return false;
		}
		Node titleNode = DOMUtils.getFirstNamedChild(item, "title");
		if (titleNode == null) {
			return false;
		}
		Node titleTextNode = DOMUtils.getFirstTextChild(titleNode);
		if (titleTextNode == null) {
			return false;
		}
		return true;
	}
	
}
