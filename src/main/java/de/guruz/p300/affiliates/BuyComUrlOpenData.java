package de.guruz.p300.affiliates;

import de.guruz.p300.utils.URL;

/**
 * http://www.amazon.com/s/url=search-alias%3Daps&field-keywords=XYZ?tag=p3000d-20
 * 
 */
public class BuyComUrlOpenData extends UrlOpenData {

	public BuyComUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		//return "http://clickfrom.buy.com/default.aspx?adid=17662&aid=10389713&pid=3043620&sid=&sURL=http%3A//www.buy.com%2Fretail%2FGlobalSearchAction.asp%3Fqu%3D"
		//		+ URL.encode (m_searchWordsEncoded);
		
		return "http://affiliate.buy.com/gateway.aspx?adid=17662&pid=3043620&aid=10391416&sURL=http%3A%2F%2Fwww%2Ebuy%2Ecom%2Fretail%2FGlobalSearchAction%2Easp%3FqueryType%3Dhome%26qu%3D"
		+ URL.encode (m_searchWordsEncoded);
	}

	@Override
	public String toString() {
		return "Buy.com";
	}

}
