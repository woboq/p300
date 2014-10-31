/**
 * 
 */
package de.guruz.p300.eventstream;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.DOMUtils;

/**
 * Microblog event
 * @author tomcat
 *
 */
public class MicroblogEvent extends Event implements PurgableByAge {

	/**
	 * The blog text
	 */
	private String blogText;
	
	/**
	 * Create a new MicroblogEvent.<br />
	 * @param sourceHost The source of the event
	 * @param timestamp The point in time of the event
	 * @param text The text to microblog
	 */
	public MicroblogEvent(Host sourceHost, Date timestamp, String text) {
		super(sourceHost, timestamp);
		blogText = text;
		if (blogText == null) {
			blogText = "";
		}
	}
	
	/**
	 * Return the text of this microblog event
	 * @return The microblog text
	 */
	public String getBlogText() {
		return blogText;
	}

	/**
	 * Create the event from an XML element (RSS item) 
	 * @param item The RSS item to read from
	 * @param host The Host that generated the event
	 * @throws BadRSSException In case bad RSS data has been given
	 */
	public MicroblogEvent(Element item, Host host) throws BadRSSException {
		super(item, host);
		if (!validRSS(item)) {
			throw new BadRSSException();
		}
		Node descriptionNode = DOMUtils.getFirstNamedChild(item, "description");
		Node descriptionTextNode = DOMUtils.getFirstTextChild(descriptionNode);
		String description = "";
		if (descriptionTextNode != null) {
			description = descriptionTextNode.getNodeValue();
		}
		blogText = description;
	}

	/**
	 * Check if the RSS item data defined in the given XML element is valid concerning MicroblogEvents.<br />
	 * Checks for validity of the XML tree for title and description elements.
	 * @param item
	 * @return
	 */
	private boolean validRSS(Element item) {
		if (item == null) {
			return false;
		}
		if (!item.getNodeName().equals("item")) {
			return false;
		}
		Node titleNode = DOMUtils.getFirstNamedChild(item, "title");
		Node descriptionNode = DOMUtils.getFirstNamedChild(item, "description");
		if (titleNode == null || descriptionNode == null) {
			return false;
		}
		Node titleTextNode = DOMUtils.getFirstTextChild(titleNode);
		if (titleTextNode == null) {
			return false;
		}
		String title = titleTextNode.getNodeValue();
		if (title == null) {
			return false;
		}
		if (!title.equals("MicroblogEvent")) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see de.guruz.p300.eventstream.Event#toRSS(org.w3c.dom.Element)
	 */
	@Override
	public void toRSS(Element item) {
		super.toRSS(item);
		Document doc = item.getOwnerDocument();
		Element titleElement = doc.createElement("title");
		Element descriptionElement = doc.createElement("description");
		String title = "MicroblogEvent";
		String description = blogText;
		Node titleText = doc.createTextNode(title);
		Node descriptionText = doc.createTextNode(description);
		titleElement.appendChild(titleText);
		descriptionElement.appendChild(descriptionText);
		item.appendChild(titleElement);
		item.appendChild(descriptionElement);
	}
	
	/* (non-Javadoc)
	 * @see de.guruz.p300.eventstream.PurgableByAge#isOld()
	 */
	public boolean isOld() {
		Date now = new Date();
		long age = now.getTime() - getTimestamp().getTime();
		if (age > 604800000) {
			return true;
		} else {
			return false;
		}
	}
	
}
