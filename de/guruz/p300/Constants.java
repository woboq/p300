package de.guruz.p300;

public class Constants {
	/*
	 * how often to send a UDP IamHere
	 */ 
	public static final long DISCOVERY_MULTICAST_IAMHERE_INTERVAL_MSEC = 1000*60;
	
	/*
	 * How often to do a HTTP hostfinder request
	 * 
	 * 11.2008: guruz changed this from 3* to 1.3* to improve discovery, however with more traffic
	 */
	public static final long DISCOVERY_HTTP_HOSTFINDER_INTERVAL_MSEC = (long) (1.3*DISCOVERY_MULTICAST_IAMHERE_INTERVAL_MSEC);
	
	/*
	 * How long does it take for a host to be seen as offline
	 */
	public static final long DISCOVERY_HOST_SEEMS_ONLINE_TIMEOUT_MSEC = (long) (DISCOVERY_HTTP_HOSTFINDER_INTERVAL_MSEC * 1.3);
	
	public static final long DISCOVERY_ALLOWME_MESSAGE_INTERVAL = 2 * DISCOVERY_MULTICAST_IAMHERE_INTERVAL_MSEC;
	
	/*
	 * How long do we (as a server) hold a HTTP connection on keep alive?
	 */
	public static final long LANVPN_HTTP_KEEPALIVE_TIMEOUT_MSEC = (long) (DISCOVERY_HTTP_HOSTFINDER_INTERVAL_MSEC * 2.5);

	public static final int MAX_INCOMING_HTTP_CONNECTIONS = 60;


}
