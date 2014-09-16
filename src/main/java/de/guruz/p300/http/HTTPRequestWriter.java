package de.guruz.p300.http;

import java.util.Map;

import de.guruz.p300.Configuration;
import de.guruz.p300.connections.SynchronousLogicalStreamConnection;

public class HTTPRequestWriter {
	final String userAgentHeader;
	final String keepAliveHeader;
	
	final SynchronousLogicalStreamConnection m_connection;
	final HTTPVerb m_verb;
	final String m_entity;
	final HTTPHeaders m_headers;
	final String m_hostHeader;
	
	public HTTPRequestWriter (SynchronousLogicalStreamConnection c, String hostHeader, HTTPVerb v, String e, HTTPHeaders h)
	{
		m_connection = c;
		m_verb = v;
		m_entity = e;

		if (h == null)
			m_headers = new HTTPHeaders ();
		else
			m_headers = h;
		
		m_hostHeader = hostHeader;
		
		userAgentHeader = "User-agent: p300 " + Configuration.getSVNRevision() + "\r\n";
		keepAliveHeader = "Connection: keep-alive\r\n";
	}
	
	public void write () throws Exception 
	{
//		m_connection.writeUTF8(m_verb.name());
//		m_connection.writeUTF8(" ");
//		m_connection.writeUTF8(m_entity);
//		m_connection.writeUTF8(" HTTP/1.0\r\n");
		
		m_connection.writeUTF8(m_verb.name() + " "+ m_entity +" HTTP/1.1\r\n");
		
		for (Map.Entry<String,String> header : m_headers.getAllHeaders().entrySet())
		{
			m_connection.writeUTF8 (header.getKey() + ": " + header.getValue() + "\r\n");
		}
		m_connection.writeUTF8 (keepAliveHeader);
		m_connection.writeUTF8 (userAgentHeader);
		
		if (m_hostHeader != null && m_hostHeader.trim().length() > 0 && m_headers.getHeader("Host") == null)
			m_connection.writeUTF8 ("Host: " + m_hostHeader + "\r\n");
		
		m_connection.writeUTF8 ("\r\n");
		//m_connection.flush();
	}
}
