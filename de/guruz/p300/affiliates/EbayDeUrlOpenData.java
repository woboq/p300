package de.guruz.p300.affiliates;

/**
 * http://www.amazon.com/s/url=search-alias%3Daps&field-keywords=XYZ?tag=p3000d-20
 * 
 */
public class EbayDeUrlOpenData extends UrlOpenData {

	public EbayDeUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		// http://partners.webmasterplan.com/click.asp?ref=423143&site=1382&type=text&tnb=23&prd=yes&srchdesc=Y&itf=0&category0=&minprice=&maxprice=&query=suchbegriff
		return "http://partners.webmasterplan.com/click.asp?ref=423143&site=1382&type=text&tnb=23&prd=yes&srchdesc=Y&itf=0&category0=&minprice=&maxprice=&query="
				+ m_searchWordsEncoded;
	}

	@Override
	public String toString() {
		return "Ebay.de";
	}

}
