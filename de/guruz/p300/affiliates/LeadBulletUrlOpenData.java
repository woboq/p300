package de.guruz.p300.affiliates;

public class LeadBulletUrlOpenData extends UrlOpenData {
	
	
	public LeadBulletUrlOpenData(String sw) {
		super(sw);
	}

	public String getURL() {
		return LeadBulletOpener.createMatchUrlForEncodedSearchwords (m_searchWordsEncoded);
	}
	
	public String toString ()
	{
		return "Sharemonkey";
	}

}
