package de.guruz.p300.onetoonechat;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.XML;

public class Message {
	
	protected Host m_from;
	
	protected Host m_to;
	

	
	protected String m_text;

	public Message(Host from, Host to, String text) {
		super();
		m_from = from;
		m_to = to;
		m_text = text;
	}

	public Host getFrom() {
		return m_from;
	}

	public Host getTo() {
		return m_to;
	}


	public String getText() {
		return m_text;
	}
	
	public String serializeToXML ()
	{
		return "<p300><message-v1><text>" + XML.encode (getText ()) + "</text></message-v1></p300>";
	}
}
