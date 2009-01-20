package de.guruz.p300.webdav.search.host;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.guruz.p300.search.Searcher;
import de.guruz.p300.search.SingleSearchResult;
import de.guruz.p300.utils.DOMUtils;

public class WebDAVSearch {

	private static final String DAV_NAMESPACE = "DAV:";
	private static final String DEFAULT_DAV_PREFIX = "d:";
	
	private static final String SHARE_LOCATION = "/shares/";

	/**
	 * Search the file database using the WebDAV SEARCH request contained in xmlRequest
	 * Returns a result document conforming to
	 *   http://greenbytes.de/tech/webdav/draft-reschke-webdav-search-latest.html
	 * @param xmlRequest
	 * @return
	 * @throws WebDAVSearchException
	 */
	public static Document search(Document xmlRequest) throws WebDAVSearchException {
		if (isValidRequest(xmlRequest)) {
			String searchString = getSearchString(xmlRequest);
			if (searchString == null) {
				searchString = "";
			}
			SingleSearchResult[] searchResult = Searcher.search(searchString);
			return transformSSRtoXML(searchResult);
		} else {
			throw new WebDAVSearchException();
		}
	}

	/**
	 * Transform an array of SingleSearchResult into an XML Document ready for sending to the user
	 * @param searchResult
	 * @return
	 */
	private static Document transformSSRtoXML(SingleSearchResult[] searchResult) {
		Document resultDocument = constructResultDocument();
		if (resultDocument == null) {
			return null;
		}
		for (SingleSearchResult oneResult: searchResult) {
			addSSRtoResultDocument(resultDocument, oneResult);
		}
		return resultDocument;
	}

	/**
	 * Add a SingleSearchResult object to the result document
	 * @param resultDocument
	 * @param oneResult
	 */
	private static void addSSRtoResultDocument(Document resultDocument,
			SingleSearchResult oneResult) {
		Node multistatusNode = resultDocument.getFirstChild();
		Element singleResponseElement = createSingleResponseElement(resultDocument, oneResult);
		multistatusNode.appendChild(singleResponseElement);
	}

	/**
	 * Create a single response element for out of the given SingleSearchResult
	 * @param resultDocument
	 * @param oneResult
	 * @return
	 */
	private static Element createSingleResponseElement(Document resultDocument,
			SingleSearchResult oneResult) {
		final String DAV_PREFIX = DEFAULT_DAV_PREFIX;

		String resultHref = createResultHref(oneResult);
		long resultSize = oneResult.getFileSize();
		String fileName = oneResult.getFileName();
		boolean isDirectory = false;
		File resultFile = oneResult.getFile();
		if (resultFile != null) {
			isDirectory = resultFile.isDirectory();
		}

		Element e = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "response");
		Element hrefElement = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "href");
		Element propstatElement = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "propstat");
		Element propElement = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "prop");
		Element displayNameProp = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "displayname");
		Element contentLengthProp = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "getcontentlength");
		Element resourceType = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "resourcetype");
		Element collectionType = resultDocument.createElementNS(DAV_NAMESPACE, DAV_PREFIX + "collection"); 

		Text hrefText = resultDocument.createTextNode(resultHref);
		Text displayNameText = resultDocument.createTextNode(fileName);
		Text contentLengthText = resultDocument.createTextNode(Long.toString(resultSize));
		
		e.appendChild(hrefElement);
		e.appendChild(propstatElement);
		propstatElement.appendChild(propElement);
		propElement.appendChild(displayNameProp);
		propElement.appendChild(contentLengthProp);
		resourceType.appendChild(collectionType);
		if (isDirectory) {
			propElement.appendChild(resourceType);
		}

		hrefElement.appendChild(hrefText);
		displayNameProp.appendChild(displayNameText);
		contentLengthProp.appendChild(contentLengthText);
		
		return e;
	}

	/**
	 * Create a net-wide valid href link for a given single search result
	 * @param oneResult
	 * @return
	 */
	private static String createResultHref(SingleSearchResult oneResult) {
		String shareLocation = SHARE_LOCATION;
		String shareName = oneResult.getShareName();
		String fileName = oneResult.getFileName();
		return shareLocation + shareName + fileName; 
	}

	/**
	 * Construct an empty result document
	 * @return
	 */
	private static Document constructResultDocument() {
		Document d = getEmptyDocument();
		if (d == null) {
			return null;
		}
		Element multistatusElement = d.createElementNS(DAV_NAMESPACE, DEFAULT_DAV_PREFIX + "multistatus");
		d.appendChild(multistatusElement);
		return d;
	}

	/**
	 * Return an empty XML Document
	 * @return
	 */
	private static Document getEmptyDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Find the search string in the XML request
	 * Precondition: The validity checks worked out (I will not do any further checks here)
	 * @param xmlRequest
	 * @return
	 */
	private static String getSearchString(Document xmlRequest) {
		Node searchRequest = getFirstNamedChild(xmlRequest, "searchrequest");
		Node basicSearch = getFirstNamedChild(searchRequest, "basicsearch");
		Node where = getFirstNamedChild(basicSearch, "where");
		Node eq = getFirstNamedChild(where, "eq");
		Node literal = getFirstNamedChild(eq, "literal");
		Node searchStringNode = DOMUtils.getFirstTextChild(literal);
		if (searchStringNode == null) {
			return "";
		}
		return searchStringNode.getNodeValue();
	}

	/**
	 * Redirect to DOMUtils.getFirstNamedChild using the WEBDAV_NAMESPACE
	 * @param xmlRequest
	 * @param string
	 * @return
	 */
	private static Node getFirstNamedChild(Node n, String string) {
		return DOMUtils.getFirstNamedChildNS(n, DAV_NAMESPACE, string);
	}

	/**
	 * Check if the given xmlRequest conforms to what we expect
	 * We expect plain text comparisons in all shares
	 * @param xmlRequest
	 * @return
	 */
	private static boolean isValidRequest(Document xmlRequest) {
//		System.err.println("isValidRequest");
//		System.err.println("Checking xmlRequest");
		if (xmlRequest == null) {
			return false;
		}
//		System.err.println("Checking searchRequest");
		Node searchRequest = getFirstNamedChild(xmlRequest, "searchrequest");
		if (searchRequest == null) {
			return false;
		}
//		System.err.println("Checking basicSearch");
		Node basicSearch = getFirstNamedChild(searchRequest, "basicsearch");
		if (basicSearch == null) {
			return false;
		}
//		System.err.println("Checking selectNode");
		Node selectNode = getFirstNamedChild(basicSearch, "select");
		if (selectNode == null) {
			return false;
		}
//		System.err.println("Checking allprop");
		if (getFirstNamedChild(selectNode, "allprop") == null) {
			return false;
		}
//		System.err.println("Checking from");
		Node fromNode = getFirstNamedChild(basicSearch, "from");
		if (fromNode == null) {
			return false;
		}
//		System.err.println("Checking scope");
		Node scopeNode = getFirstNamedChild(fromNode, "scope");
		if (scopeNode == null) {
			return false;
		}
//		System.err.println("Checking href");
		Node hrefNode = getFirstNamedChild(scopeNode, "href");
		if (hrefNode == null) {
			return false;
		}
//		System.err.println("Checking SHARE_LOCATION");
		if (DOMUtils.getFirstTextChild(hrefNode, SHARE_LOCATION) == null) {
			return false;
		}
//		System.err.println("Checking depth");
		Node depthNode = getFirstNamedChild(scopeNode,"depth");
		if (depthNode == null) {
			return false;
		}
//		System.err.println("Checking depth == infinity");
		if (DOMUtils.getFirstTextChild(depthNode, "infinity") == null) {
			return false;
		}
//		System.err.println("Checking where");
		Node whereNode = getFirstNamedChild(basicSearch, "where");
		if (whereNode == null) {
			return false;
		}
//		System.err.println("Checking eq"); 
		Node eqNode = getFirstNamedChild(whereNode, "eq");
		if (eqNode == null) {
			return false;
		}
//		System.err.println("Checking prop");
		Node propNode = getFirstNamedChild(eqNode, "prop");
		if (propNode == null) {
			return false;
		}
//		System.err.println("Checking displayname");
		if (getFirstNamedChild(propNode, "displayname") == null) {
			return false;
		}
//		System.err.println("Checking literal");
		Node literalNode = getFirstNamedChild(eqNode, "literal");
		if (literalNode == null) {
			return false;
		}
//		System.err.println("Checking validLiteral");
		if (!isValidLiteral(literalNode)) {
			return false;
		}
		return true;
	}

	/**
	 * Check if the request contains an accepted "literal" subtree
	 * @param literalNode The subtree starting at the "literal" node
	 * @return
	 */
	private static boolean isValidLiteral(Node literalNode) {
		if (DOMUtils.getFirstTextChild(literalNode) != null) {
			return true;
		} else {
			return false;
		}
	}
	
}
