/**
 * 
 */
package de.guruz.p300.webdav.search.client;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import de.guruz.p300.hosts.Host;

/**
 * The WebDAV SEARCH client class
 * Manages the searcher threadpool
 * Creates the search request documents
 * Dispatches searches to SingleHostSearchWorkers in the threadpool
 * @author tomcat
 *
 */
public class WebDAVSearchClient {

	private static final String DAV_NAMESPACE = "DAV:";
	
	/**
	 * The threadpool that will be used to launch searches
	 */
	private static final ExecutorService SEARCH_THREADPOOL = Executors.newCachedThreadPool();
	
	/**
	 * Search only the hosts specified in the list of hosts and send the results to the collectors
	 * - Create query from searchString (createQuery)
	 * - Create SingleHostSearchWorkers using the queries
	 * - Start workers using threadpool
	 * @param searchString
	 * @param collectors
	 * @param hosts
	 */
	public static void searchSpecificHosts(String searchString, List<SearchResultCollector> collectors, List<Host> hosts) {
//		System.err.println("searchSpecificHosts()");
		if (searchString == null) {
			searchString = "";
		}
		if (collectors == null || collectors.size() == 0) {
			return;
		}
		if (hosts == null || hosts.size() == 0) {
			return;
		}
		Document query = createQuery(searchString);
		if (query == null) {
			System.err.println("Couldn't create a WebDAV SEARCH query for string: " + searchString);
			return;
		}
		for (Host host: hosts) {
			SingleHostSearchWorker worker = new SingleHostSearchWorker(collectors, host, query);
//			System.err.println("Starting search...");
			SEARCH_THREADPOOL.execute(worker);
		}
	}
	
	/**
	 * Create an XML WebDAV SEARCH query from a searchString
	 * @param searchString
	 * @return
	 * @throws ParserConfigurationException
	 */
	private static Document createQuery(String searchString) {
//		System.err.println("createQuery()");
		
		Document query;
		try {
			query = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException parserError) {
			System.err.println("ParserConfigurationException: " + parserError.getLocalizedMessage());
			return null;
		}
		
		// Create Nodes & Elements
		Element searchRequestElement = query.createElementNS(DAV_NAMESPACE, "searchrequest");
		Element basicSearchElement = query.createElementNS(DAV_NAMESPACE, "basicsearch");
		Element selectElement = query.createElementNS(DAV_NAMESPACE, "select");
		Element allpropElement = query.createElementNS(DAV_NAMESPACE, "allprop");
		Element fromElement = query.createElementNS(DAV_NAMESPACE, "from");
		Element scopeElement = query.createElementNS(DAV_NAMESPACE, "scope");
		Element hrefElement = query.createElementNS(DAV_NAMESPACE, "href");
		Element depthElement = query.createElementNS(DAV_NAMESPACE, "depth");
		Text hrefTextElement = query.createTextNode("/shares/");
		Text depthTextElement = query.createTextNode("infinity");
		Element whereElement = query.createElementNS(DAV_NAMESPACE, "where");
		Element equalsElement = query.createElementNS(DAV_NAMESPACE, "eq");
		Element propElement = query.createElementNS(DAV_NAMESPACE, "prop");
		Element displaynameElement = query.createElementNS(DAV_NAMESPACE, "displayname");
		Element literalElement = query.createElementNS(DAV_NAMESPACE, "literal");
		Text literalTextElement = query.createTextNode(searchString);
		
		// Build XML tree
		query.appendChild(searchRequestElement);
		searchRequestElement.appendChild(basicSearchElement);
		basicSearchElement.appendChild(selectElement);
		basicSearchElement.appendChild(fromElement);
		basicSearchElement.appendChild(whereElement);
		selectElement.appendChild(allpropElement);
		fromElement.appendChild(scopeElement);
		scopeElement.appendChild(hrefElement);
		scopeElement.appendChild(depthElement);
		hrefElement.appendChild(hrefTextElement);
		depthElement.appendChild(depthTextElement);
		whereElement.appendChild(equalsElement);
		equalsElement.appendChild(propElement);
		equalsElement.appendChild(literalElement);
		propElement.appendChild(displaynameElement);
		literalElement.appendChild(literalTextElement);
		
//		System.err.println(DOMUtils.prettyPrintXMLDocument(query));
		
		return query;
	}
	
}
