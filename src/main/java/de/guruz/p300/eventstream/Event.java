/**
 * 
 */
package de.guruz.p300.eventstream;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostLocation;
import de.guruz.p300.utils.DOMUtils;

/**
 * Encapsulates one event, identified by Host, Timestamp<br />
 * Abstract, because we don't know what data is needed for any event
 * @author tomcat
 *
 */
public abstract class Event {

	/**
	 * The host that generated the event 
	 */
	private Host sourceHost;
	
	/**
	 * The timestamp of the event aka "when it happened"
	 */
	private Date timestamp;
	
	/**
	 * RFC 822 date format for parsing from RSS and writing to RSS
	 */
	private DateFormat rfc822Date = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US);
	
	/**
	 * Create a new Event object from a specific source which happened at a specific point in time (timestamp)<br />
	 * Protected, because only more specialized classes should be able to use this
	 * @param sourceHost The event source
	 * @param timestamp The point in time of event happening
	 */
	protected Event(Host sourceHost, Date timestamp) {
		this.sourceHost = sourceHost;
		this.timestamp = timestamp;
	}
	
	/**
	 * Return the source of the event
	 * @return The source host
	 */
	public Host getSourceHost() {
		return sourceHost;
	}
	
	/**
	 * Return the point in time of the event
	 * @return The timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Create a new Event object from an RSS item<br />
	 * Protected, because generic events are useless, but specialized classes need this to set host and timestamp
	 * @param item XML Element (RSS item)
	 * @param host The Host object of the server that generated the item
	 * @throws BadRSSException In case bad RSS data has been used
	 */
	protected Event(Element item, Host host) throws BadRSSException {
		if (!validPreconditions(item, host)) {
			throw new BadRSSException();
		}
		sourceHost = host;
		Node pubDateNode = DOMUtils.getFirstNamedChild(item, "pubDate");
		Node pubDateTextNode = DOMUtils.getFirstTextChild(pubDateNode);
		String date = pubDateTextNode.getNodeValue();
		Date d = parseRFC22Date(date);
		timestamp = d;
	}
	
	/**
	 * Check for valid preconditions on the XML
	 * @param item The XML element (RSS item) that represents an event
	 * @param h The host that produced the event. Has to match the XML
	 * @return True if the XML is okay; False otherwise
	 */
	private boolean validPreconditions(Element item, Host h) {
//		System.err.println("validPreconditions(...)");
//		System.err.println("item: " + item);
		if (item == null) {
			return false;
		}
//		System.err.println("item.getNodeName(): " + item.getNodeName());
		if (item.getNodeName() == null || !item.getNodeName().equals("item")) {
			return false;
		}
		Node authorNode = DOMUtils.getFirstNamedChild(item, "author");
		Node pubDateNode = DOMUtils.getFirstNamedChild(item, "pubDate");
		Node sourceNode = DOMUtils.getFirstNamedChild(item, "source");
//		System.err.println("authorNode: " + authorNode);
//		System.err.println("pubDateNode: " + pubDateNode);
//		System.err.println("sourceNode: " + sourceNode);
//		System.err.println(item.getNamespaceURI());
		if (authorNode == null || pubDateNode == null || sourceNode == null) {
			return false;
		}
		Node authorTextNode = DOMUtils.getFirstTextChild(authorNode);
		Node pubDateTextNode = DOMUtils.getFirstTextChild(pubDateNode);
		Node sourceTextNode = DOMUtils.getFirstTextChild(sourceNode);
//		System.err.println("authorTextNode: " + authorTextNode);
//		System.err.println("pubDateTextNode: " + pubDateTextNode);
//		System.err.println("sourceTextNode: " + sourceTextNode);
		if (authorTextNode == null || pubDateTextNode == null || sourceTextNode == null) {
			return false;
		}
		String author = authorTextNode.getNodeValue();
		String pubDate = pubDateTextNode.getNodeValue();
		String source = sourceTextNode.getNodeValue();
//		System.err.println("author: " + author);
//		System.err.println("pubDate: " + pubDate);
//		System.err.println("source: " + source);
		if (author == null || pubDate == null || source == null) {
			return false;
		}
//		System.err.println("h.getDisplayName(): " + h.getDisplayName());
		if (!author.equals(h.getDisplayName())) {
			return false;
		}
//		System.err.println("h.getUUID(): " + h.getUUID());
		if (!source.equals(h.getUUID())) {
			return false;
		}
//		System.err.println("sourceNode.getAttributes(): " + sourceNode.getAttributes());
//		System.err.println("sourceNode.getAttributes().getNamedItem(\"url\"): " + sourceNode.getAttributes().getNamedItem("url"));
//		System.err.println("sourceNode.getAttributes().getNamedItem(\"url\").getNodeValue(): " + sourceNode.getAttributes().getNamedItem("url").getNodeValue());
		if (sourceNode.getAttributes() == null || sourceNode.getAttributes().getNamedItem("url") == null || sourceNode.getAttributes().getNamedItem("url").getNodeValue() == null) {
			return false;
		}
//		System.err.println("h.getBestHostLocation(): " + h.getBestHostLocation());
//		System.err.println("h.getBestHostLocation().getIP(): " + h.getBestHostLocation().getIp());
		if (h.getBestHostLocation() == null || h.getBestHostLocation().getIp() == null) {
			return false;
		}
//		System.err.println("h.getUUID(): " + h.getUUID());
		if (!sourceNode.getAttributes().getNamedItem("url").getNodeValue().equals("http://" + h.getBestHostLocation().getIp() + ":" + h.getBestHostLocation().getPort() + "/")) {
			return false;
		}
		Date d = parseRFC22Date(pubDate);
//		System.err.println("d: " + d);
		if (d == null || d.getTime() < 946681200) {
			return false;
		}
//		System.err.println("-> true");
		return true;
	}

	/**
	 * Parse a date from a String that is in RFC 822 (mail) format
	 * @param pubDate The String containing the date as text
	 * @return A date if it could be parsed, or null if there was an error
	 */
	private Date parseRFC22Date(String pubDate) {
		Date d;
		try {
			d = rfc822Date.parse(pubDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return d;
	}

	/**
	 * Add the data of this event to an XML element (RSS item).<br />
	 * This should be called from the subclass's toRSS() method to add more generic fields.
	 * @param item The RSS item (XML element) denoting this event
	 */
	protected void toRSS(Element item) {
		if (item == null) {
			return;
		}
		Document d = item.getOwnerDocument();
		Element authorElement = d.createElement("author");
		Element pubDateElement = d.createElement("pubDate");
		Element sourceElement = d.createElement("source");
		HostLocation loc = sourceHost.getBestHostLocation();
		if (loc != null) {
			sourceElement.setAttribute("url", "http://" + loc.getIp() + ":" + loc.getPort() + "/");
		}
		Text authorTextNode = d.createTextNode(sourceHost.getDisplayName());
		String dateString = createRFC822Date(timestamp);
		Text pubDateTextNode = d.createTextNode(dateString);
		Text sourceTextNode = d.createTextNode(sourceHost.getUUID());
		authorElement.appendChild(authorTextNode);
		pubDateElement.appendChild(pubDateTextNode);
		sourceElement.appendChild(sourceTextNode);
		
		item.appendChild(authorElement);
		item.appendChild(pubDateElement);
		item.appendChild(sourceElement);
	}

	/**
	 * Create an RFC 822-compliant date from a timestamp
	 * @param timestamp2
	 * @return
	 */
	private String createRFC822Date(Date timestamp2) {
		if (timestamp2 == null) {
			return "";
		}
		String s = rfc822Date.format(timestamp2);
		if (s == null) {
			return "";
		}
		return s;
	}
	
}
