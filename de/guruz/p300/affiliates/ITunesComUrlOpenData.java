package de.guruz.p300.affiliates;

import de.guruz.p300.utils.URL;

/**
 * 
 * Siehe erhaltene Mail
 * 
 */
public class ITunesComUrlOpenData extends UrlOpenData {

	public ITunesComUrlOpenData(String sw) {
		super(sw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getURL() {
		String phobosUrl = "http://phobos.apple.com/WebObjects/MZSearch.woa/wa/search?term="
				+ m_searchWordsEncoded;
		return "http://click.linksynergy.com/fs-bin/stat?id=3FT6AidkwsQ&offerid=146261&type=3&subid=0&tmpid=1826&RD_PARM1="
				+ URL.encode(phobosUrl);
	}

	@Override
	public String toString() {
		return "iTunes Store (USA)";
	}

}
