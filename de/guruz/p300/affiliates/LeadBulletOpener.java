package de.guruz.p300.affiliates;

import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.utils.Mime;

public class LeadBulletOpener {
	private static final String m_baseUrl = "http://match.sharemonkey.com/?cid=31";

	public static void openPossibleProductsInBrowser (RemoteEntity f)
	{
		String url = m_baseUrl ;
		String filename = f.getName();
		String parentFilename = (f.getPath() != null ? f.getParent().getName() : "");
		String queryFilename = filename;
		
		// heuristic: the filename is very short or it is an audio without a "-" so it is probably in a directory with the album title
		if (queryFilename.length() < 10 || (Mime.isAudioFileName(filename) && !filename.contains("-")))
			queryFilename = parentFilename + " " + queryFilename;
		
		url += "&n=" + de.guruz.p300.utils.URL.encode(queryFilename);
		
		if (f.getSize() > 0)
			url += "&s=" + f.getSize();
		
		de.guruz.p300.utils.launchers.BareBonesBrowserLaunch.openURL(url);
	}
	
	public static String createMatchUrlForSearchwords (String sw)
	{
		return m_baseUrl + "&n=" + de.guruz.p300.utils.URL.encode(sw);
	}
	
	public static String createMatchUrlForEncodedSearchwords (String sw)
	{
		return m_baseUrl + "&n=" + sw;
	}
	
	
}
