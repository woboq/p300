package de.guruz.p300.affiliates;

/**
 * http://www.amazon.com/s/url=search-alias%3Daps&field-keywords=XYZ?tag=p3000d-20
 * 
 */
public class AmazonDeUrlOpenData extends UrlOpenData {

	public AmazonDeUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		return "http://www.amazon.de/s/url=search-alias%3Daps&field-keywords="
				+ m_searchWordsEncoded + "?tag=p30003-21";
	}

	@Override
	public String toString() {
		return "Amazon.de";
	}

}
