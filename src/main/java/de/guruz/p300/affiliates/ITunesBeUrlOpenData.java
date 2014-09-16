package de.guruz.p300.affiliates;

import de.guruz.p300.utils.URL;

/**
 * 
 * http://clk.tradedoubler.com/click?p=$$$$$$&a=********&url=PHOBOS_URL&partnerId=2003
 * 
 * Replace $$$$$$ with Programme code Replace ******** with affiliate ID
 * 
 */
public class ITunesBeUrlOpenData extends UrlOpenData {

	public ITunesBeUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		String phobosUrl = "http://phobos.apple.com/WebObjects/MZSearch.woa/wa/search?term="
				+ m_searchWordsEncoded + "&partnerId=2003";
		return "http://clk.tradedoubler.com/click?p=24379&a=1570938&url="
				+ URL.encode(phobosUrl) + "&partnerId=2003";
	}

	@Override
	public String toString() {
		return "iTunes Store (Belgium)";
	}

}
