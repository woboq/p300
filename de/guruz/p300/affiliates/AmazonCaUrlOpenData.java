package de.guruz.p300.affiliates;

/**
 * http://www.amazon.com/s/url=search-alias%3Daps&field-keywords=XYZ?tag=p3000d-20
 * 
 */
public class AmazonCaUrlOpenData extends UrlOpenData {

	public AmazonCaUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		return "http://www.amazon.ca/s/url=search-alias%3Daps&field-keywords="
				+ m_searchWordsEncoded + "?tag=p300-20";
	}

	@Override
	public String toString() {
		return "Amazon.ca";
	}

}
