package de.guruz.p300.affiliates;

public abstract class UrlOpenData {
	final String m_searchWordsEncoded;
	
	public UrlOpenData (String sw)
	{
		m_searchWordsEncoded = de.guruz.p300.utils.URL.encode(sw);
	}
	
	abstract public  String getURL ();
	
	abstract public String toString ();
	
	
	
}
