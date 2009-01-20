package de.guruz.p300.requests;

import org.w3c.dom.Document;

import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.utils.DOMUtils;
import de.guruz.p300.webdav.search.host.WebDAVSearch;
import de.guruz.p300.webdav.search.host.WebDAVSearchException;

public class WebDAVSearchRequest extends Request {

	@Override
	public void handle() throws Exception {
		Document xmlRequest = extractXMLRequest();
		if (xmlRequest == null) {
			error500();
		}
//		System.err.println("xmlRequest:");
//		System.err.println(DOMUtils.prettyPrintXMLDocument(xmlRequest));
		Document xmlReply = null;
		try {
			xmlReply = WebDAVSearch.search(xmlRequest);
		} catch (WebDAVSearchException exception) {
			System.err.println("Error 500! Stack trace follows:");
			exception.printStackTrace();
			error500();
		}
		if (xmlReply != null) {
//			System.err.println("xmlReply:");
//			System.err.println(DOMUtils.prettyPrintXMLDocument(xmlReply));

			requestThread.httpStatus(207, "Multi-status");
			requestThread.httpContentType("text/xml", "utf-8");
			requestThread.httpContents();
			requestThread.write(DOMUtils.prettyPrintXMLDocument(xmlReply));
			requestThread.close();
		}
	}

	private Document extractXMLRequest() {
		if (requestThread == null) {
			return null;
		}
		byte[] clientContent = requestThread.getClientContent();
		if (clientContent == null) {
			return null;
		}
		return DOMUtils.documentFromByteArray(clientContent);
	}

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (rt == HTTPVerb.SEARCH) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Report an error 500 back to the user
	 * @throws Exception
	 */
	private void error500() throws Exception {
		requestThread.httpStatus(500, "Internal Server Error");
//		requestThread.close();
	}
}
