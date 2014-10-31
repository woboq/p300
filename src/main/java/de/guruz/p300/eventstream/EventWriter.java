/**
 * 
 */
package de.guruz.p300.eventstream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.DOMUtils;

/**
 * Writer that can write an event (or many) to RSS
 * @author tomcat
 *
 */
public class EventWriter extends BufferedWriter {

	/**
	 * The RSS channel's host
	 */
	private Host channelHost;
	
	/**
	 * Create a new RSS event writer, using the given writer as backend
	 * @param out The backend writer, usually to a file or stream, or something
	 * @param host The host to use for the RSS channel (title, link, desc)
	 */
	public EventWriter(Writer out, Host host) {
		super(out);
		channelHost = host;
	}
	
	/**
	 * Write the given events to the backend.
	 * @param list The events to write
	 */
	public void writeEvents(Event[] list) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException exception) {
			exception.printStackTrace();
			return;
		}
			
		Element rss = doc.createElement("rss");
		Element channel = doc.createElement("channel");
		Element channelTitle = doc.createElement("title");
		Element channelLink = doc.createElement("link");
		Element channelDescription = doc.createElement("description");

		rss.setAttribute("version", "2.0");
		
		doc.appendChild(rss);
		rss.appendChild(channel);
		channel.appendChild(channelTitle);
		channel.appendChild(channelLink);
		channel.appendChild(channelDescription);
		
		Text channelTitleText = doc.createTextNode("EventStream of " + channelHost.getDisplayName());
		Text channelLinkText = doc.createTextNode("http://" + channelHost.getBestHostLocation().getIp() + ":" + channelHost.getBestHostLocation().getPort() + "/events");
		Text channelDescText = doc.createTextNode("EventStream of " + channelHost.getDisplayName());
		
		channelTitle.appendChild(channelTitleText);
		channelLink.appendChild(channelLinkText);
		channelDescription.appendChild(channelDescText);
		
		for (Event event: list) {
			Element item = doc.createElement("item");
			event.toRSS(item);
			channel.appendChild(item);
		}
		
		String rssString = DOMUtils.prettyPrintXMLDocument(doc);
		try {
			write(rssString);
			this.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
