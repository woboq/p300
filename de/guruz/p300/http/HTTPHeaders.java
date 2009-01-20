package de.guruz.p300.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HTTPHeaders {
	protected HashMap<String,String> m_entries = new HashMap<String, String>();
	
	public Map<String,String> getAllHeaders ()
	{
		return Collections.unmodifiableMap(m_entries);
	}
	
	public void setHeader (String k, String v)
	{
		m_entries.put(k.toLowerCase(), v);
	}
	
	public String getHeader (String k)
	{
		return m_entries.get(k.toLowerCase());
	}

	public void clearHeaders() {
		m_entries.clear();
		
	}
}
