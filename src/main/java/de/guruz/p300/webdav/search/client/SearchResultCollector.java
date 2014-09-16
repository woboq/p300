/**
 * 
 */
package de.guruz.p300.webdav.search.client;

import java.util.List;

import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.hosts.Host;

/**
 * Interface for collecting search results from the SingleHostSearchWorkers
 * The methods are called by the workers
 * @author tomcat
 *
 */
public interface SearchResultCollector {

	/**
	 * A new result list has been retrieved from the given host 
	 * @param host
	 * @param resultList
	 */
	public void newResult(Host host, List<RemoteEntity> resultList);
	
	/**
	 * The query on the given host was successful but had no result
	 * @param host
	 */
	public void noResult(Host host);
	
	/**
	 * The query on the given host had any kind of error
	 * The error message is submitted as msgd
	 * @param host
	 * @param msg
	 */
	public void hasError(Host host, String msg);
	
}
