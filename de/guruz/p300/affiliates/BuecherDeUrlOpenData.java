package de.guruz.p300.affiliates;

/**
 * http://www.amazon.com/s/url=search-alias%3Daps&field-keywords=XYZ?tag=p3000d-20
 * 
 */
public class BuecherDeUrlOpenData extends UrlOpenData {

	public BuecherDeUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		return "http://partners.webmasterplan.com/click.asp?ref=423143&site=3780&type=text&tnb=18&prd=yes&suchwert="
				+ m_searchWordsEncoded;
	}

	@Override
	public String toString() {
		return "Buecher.de";
	}

}
