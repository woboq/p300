package de.guruz.p300.hosts.allowing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.guruz.p300.Configuration;
import de.guruz.p300.utils.LongestIsGreaterComparator;
import de.guruz.p300.utils.RandomGenerator;

public class HostAllowanceManager {
	
	protected Map<String,Long> m_implicitlyAllowedIps = new HashMap<String,Long> ();
	
	/**
	 * The user can set certain hosts and ip ranges to have no traffic limit
	 * This method checks if an IP is unlimited
	 * 
	 * @param ip
	 *            A host as IP String, e.g. "192.168.0.1"
	 * @return True: The host has no traffic limit; False: The host has traffic
	 *         limit
	 */
	public boolean isIpUnlimited(String ip)
	{
		if (de.guruz.p300.utils.IP.isLocalhostIP(ip)) {
			return true;
		}
		
		if (de.guruz.p300.utils.IP.isOurIP(ip)) {
			return true;
		}


		List<String> AllowedKeys = getAllAllowedIps();
		
		
		String UnlimitedKeys[] = Configuration.instance ().getUnlimitedIps();

		return HostAllowanceManager.checkIPMatching(ip, AllowedKeys, UnlimitedKeys);
	}
	
//    public void allowIp (String ip, boolean doAllow)
//    {
//    	Configuration.instance ().setIpExplicitlyAllowed(ip, doAllow);
//    	
//    }
    
	/**
	 * Check if a given IP is allowed to access this host The user can set the
	 * list of allowed hosts in the p300 configuration web interface
	 * 
	 * @param ip
	 *            A host's ip as String, e.g. "192.168.0.1"
	 * @return True: The given host is allowed to access this one; False: The
	 *         given host is not allowed to access
	 */
    public boolean isIpAllowed (String ip)
    {
		if (de.guruz.p300.utils.IP.isLocalhostIP(ip)) {
			return true;
		}
		
		if (de.guruz.p300.utils.IP.isOurIP(ip)) {
			return true;
		}
		
		if (isImplicitAllowOn () && m_implicitlyAllowedIps.containsKey(ip))
		{
			//D.out(ip + " implicitly allowed");
			return true;
		}

		String keys[] = null;

		keys = Configuration.instance ().getExplicitlyAllowedIps();

		// Keys can be of the forms
		// - "192.168.0.", e.g. positively matching "192.168.0.1"
		// - "192.168.0.1", matching only "192.168.0.1"
		// - "192.", matching "192.169.130.120"
		for (String current : keys) {
			if (ip.equals(current)
					|| (current.endsWith(".") && ip.startsWith(current))) {
				// System.out.println(current + " allows for ip=" + ip);
				return true;
			}
		}

		// System.out.print("Denying connection from " + ip);

		return false;
    }
    
    public void hostDectectedAsLocal (String ip)
    {
    	// currently only the key is relevant, the value 3600 could later be used for timeouting
    	m_implicitlyAllowedIps.put(ip, new Long (3600));
    }

	public static boolean checkIPMatching (String ip, List<String> allowedKeys, String unlimitedKeys[]) {
	//		 Keys can be of the forms
			// - "192.168.0.", e.g. positively matching "192.168.0.1"
			// - "192.168.0.1", matching only "192.168.0.1"
			// - "192.", matching "192.169.130.120"
			for (String currentAllowed : allowedKeys) {
				if (ip.equals(currentAllowed)
						|| (currentAllowed.endsWith(".") && ip.startsWith(currentAllowed))) {
					for (String currentUnlimited : unlimitedKeys) {
						if (currentUnlimited.equals(currentAllowed)) {
							// the prefix/host is excactly in unlimited (and the prefixes are sorted):
							// be unlimited
							return true;
						}
					}
					// not unlimited here for this prefix, continue evaluating
				}
			}
			// no match: we are not unlimited
			return false;
		}

	public List<String> getAllAllowedIps() {
		ArrayList<String> i = new ArrayList<String> ();
		
		i.addAll(getImplicitlyAllowedIps());
		i.addAll (getExplicitlyAllowedIps());
		
		LongestIsGreaterComparator cmp = LongestIsGreaterComparator.instance();
		Collections.sort(i, cmp);
		
		return i;
	}
	
	public List<String> getExplicitlyAllowedIps() {
		return Arrays.asList(Configuration.instance ().getExplicitlyAllowedIps());
	}
	
	public Set<String> getImplicitlyAllowedIps() {
		return m_implicitlyAllowedIps.keySet();
	}

	public boolean isImplicitAllowOn() {
		return Configuration.instance().isLocalNetworkIpsImplcitlyAllowed();
	}

	/**
	 * We send this cookie (=string) with our UDP IAmHere multicast/broadcast packets. 
	 * Everyone that received the packet can use the cookie to claim (via UDP or HTTP) 
	 * that it is in our local network.
	 * 
	 * 
	 */
	String m_localCookie = RandomGenerator.string() + RandomGenerator.uuid();
	
	public String getLocalAuthCookieForImplicitAllow() {
		return m_localCookie;
	}


}
