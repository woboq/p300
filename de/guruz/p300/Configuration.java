/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.guruz.p300;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.guruz.p300.downloader.files.DownloadFile;
import de.guruz.p300.logging.D;
import de.guruz.p300.search.Indexer;
import de.guruz.p300.search.IndexerThread;
import de.guruz.p300.search.IndexerThread.IntervalLevel;
import de.guruz.p300.shares.Share;
import de.guruz.p300.utils.DirectoryUtils;
import de.guruz.p300.utils.LongestIsGreaterComparator;
import de.guruz.p300.utils.OsUtils;
import de.guruz.p300.utils.RandomGenerator;

/**
 * Manages our configuration (most stuff)
 * 
 * @author guruz
 * 
 */
public class Configuration {
	protected static Configuration conf;

	protected Preferences prefs;

	/**
	 * Singleton pattern Return cached instance or create new one
	 * 
	 * @return Instance of Configuration
	 * @see #Configuration()
	 */
	public static Configuration instance() {
		synchronized (Configuration.class) {
			if (Configuration.conf == null) {
				Configuration.conf = new Configuration();
			}
		}

		return Configuration.conf;
	}

	/**
	 * Singleton pattern Create a new Configuration instance Can only be used by
	 * the class itself
	 * 
	 * @see #instance()
	 */
	protected Configuration() {
		// System.err.println(">Configuration");
		this.prefs = Preferences.userRoot().node("de/guruz/p300");

		this.highestSeenSVNRevision = this.prefs.getInt(
				"highestSeenSVNRevision", Configuration.getSVNRevision());
		// System.err.println("<Configuration");

	}

	/**
	 * @return Preferences node of the Configuration
	 */
	public Preferences preferences() {
		return this.prefs;
	}

	/**
	 * For a given share name, return the directory name as a String
	 * 
	 * @param sn
	 *            Share name (e.g. "movies")
	 * @return Directory String (e.g. "/home/user/movies")
	 * @see #setDirectoryFromShareName(String, String)
	 * @see #unshareShare(String)
	 * @see #getShareNames()
	 * @see #addShare(Share)
	 * @see #removeShare(Share)
	 * @see #removeShare(String)
	 */
	public String getDirFromShareName(String sn) {
		return this.preferences().node("shares").get(sn, "");
	}

	/**
	 * Set the directory (String) of a share
	 * 
	 * @param sn
	 *            Share name (e.g. "movies")
	 * @param d
	 *            Directory as String (e.g. "/home/user/movies")
	 * @see #getDirFromShareName(String)
	 * @see #unshareShare(String)
	 * @see #getShareNames()
	 * @see #addShare(Share)
	 * @see #removeShare(Share)
	 * @see #removeShare(String)
	 */
	private void setDirectoryFromShareName(String sn, String d) {
		this.preferences().node("shares").put(sn, d);
	}

	/**
	 * Unshare a share = remove the share from config
	 * 
	 * @param sn
	 *            The share name to remove
	 * @see #setDirectoryFromShareName(String, String)
	 * @see #getDirFromShareName(String)
	 * @see #getShareNames()
	 * @see #addShare(Share)
	 * @see #removeShare(Share)
	 * @see #removeShare(String)
	 */
	private void unshareShare(String sn) {
		this.preferences().node("shares").remove(sn);
	}

	/**
	 * Returns a list of all known shares
	 * 
	 * @return List of Strings containing all share names
	 * @see #setDirectoryFromShareName(String, String)
	 * @see #getDirFromShareName(String)
	 * @see #unshareShare(String)
	 * @see #addShare(Share)
	 * @see #removeShare(Share)
	 * @see #removeShare(String)
	 */
	public String[] getShareNames() {
		Preferences p = this.preferences().node("shares");

		try {
			return p.keys();
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	/**
	 * Return the default HTTP port of p300 Currently 4337 (static)
	 * 
	 * @return Port 4337 as int
	 * @see #getMulticastHost()
	 * @see #getMulticastPort()
	 */
	public int getDefaultHTTPPort() {
		return 4337;
	}

	/**
	 * Return the multicast host used by p300
	 * 
	 * @return 239.192.0.42 as String (static)
	 * @see #getMulticastPort()
	 * @see #getDefaultHTTPPort()
	 */
	public static String getMulticastHost() {
		return "239.192.0.42";
	}

	/**
	 * Return the multicast port used by p300
	 * 
	 * @return 4337 as int (static)
	 * @see #getMulticastHost()
	 * @see #getDefaultHTTPPort()
	 */
	public static int getMulticastPort() {
		return 4337;
	}

	private String altInstanceHash = null;
	private String instanceHash = null;

	/**
	 * 
	 * 
	 * @return
	 * @see #instanceHash
	 */
	public String getUniqueHash() {


		if (Configuration.useAltUniqueInstanceHash) {
			if (altInstanceHash == null) {
				altInstanceHash = RandomGenerator.uuid() + "@" + de.guruz.p300.utils.Base64.encode(getHostName());
			}
			return altInstanceHash;
		}

		if (instanceHash == null)
		{
			this.doSync();
			
			String hash = this.preferences().node("conf").get("hash", null);

			if (hash == null) {
				hash = RandomGenerator.uuid();
				this.preferences().node("conf").put("hash", hash);
				this.doFlush();
			}
			
			instanceHash = hash + "@" + de.guruz.p300.utils.Base64.encode(getHostName());
		}
		
		
		return instanceHash;
	}

	/**
	 * Return the admin password used for configuring p300 through the web
	 * interface If no password has ever been created, create a random one and
	 * set it
	 * 
	 * @return A String of 9 letters (upper + lower case), the password for this
	 *         p300 installation
	 * @see #setAdminPassword(String)
	 */
	public String getAdminPassword() {
		// System.err.println(">getAdminPassword");
		// Sync first!
		// If p300 instance "A" has been started without password but
		// p300 instance "B" in the meantime created a pass, we need
		// to retrieve it by synching the config using doSync
		this.doSync();
		String adminpass = this.preferences().node("conf").get("adminpass",
				null);
		// System.err.println("First try: " + adminpass);

		if (adminpass == null) {
			adminpass = RandomGenerator.string().substring(0, 9);
			// preferences().node("conf").put("adminpass", adminpass);
			this.setAdminPassword(adminpass);
		}

		// System.err.println("<getAdminPassword: " + adminpass);
		return adminpass;
	}

	/**
	 * Set the admin password for this p300 installation
	 * 
	 * @param p
	 *            A password String
	 * @see #getAdminPassword()
	 */
	public void setAdminPassword(String p) {
		// System.err.println(">setAdminPassword: " + p);
		// doSync();
		this.preferences().node("conf").put("adminpass", p);
		// Flush to disk immediately; If other p300 instances are asking
		// the config for the password, they shouldn't get an empty one;
		// Because then, they'll create another one!
		this.doFlush();
		// System.err.println("<setAdminPassword");

	}

	/**
	 * Return the displayed name of the local p300 installation
	 * 
	 * @return A String being the local name (e.g. a host name like "computer")
	 * @see #setLocalDisplayName(String)
	 */
	public String getLocalDisplayName() {
		// return preferences().node("conf").get("displayname", getHostName ());
		String dn = this.preferences().node("conf").get("displayname", null);

		if (dn == null) {
			dn = this.getHostName();
			this.setLocalDisplayName(dn);
		}

		return dn.replace(' ', '_');
	}

	/**
	 * Set the displayed name of the local machine
	 * 
	 * @param dn
	 *            A String being the local name (e.g. a name like "computer")
	 * @see #getLocalDisplayName()
	 */
	public void setLocalDisplayName(String dn) {
		this.preferences().node("conf").put("displayname", dn);
	}

	/**
	 * Find out the local hostname, using only the local part (without the
	 * network) e.g. for "hal9000.network", return "hal9000" Replaces spaces
	 * with underscores If there's an error, returns "unknown"
	 * 
	 * @return The local part of the host name or "unknown" if an error occurs
	 */
	public String getHostName() {
		try {
			String hn = java.net.InetAddress.getLocalHost().getHostName();
			return cleanHostname (hn);
		} catch (UnknownHostException e) {
			byte[] ipAddr = new byte[]{127, 0, 0, 1};
			InetAddress addr;
			try {
				addr = InetAddress.getByAddress(ipAddr);
				String hostname = addr.getCanonicalHostName();
				return cleanHostname (hostname);
			} catch (UnknownHostException e1) {
				D.out ("When getting hostname: " + e1.toString());
				return "unknown";
			}
		}
	}
	
	private String cleanHostname (String hn) {
		
		int pointIdx = hn.indexOf('.');
		if (pointIdx != -1) {
			hn = hn.substring(0, pointIdx);
		}
		hn = hn.replace(" ", "_");
		
		if (hn.equals("localhost"))
			return "unknown";
		
		return hn;
	}

	/**
	 * Subversion revision of this p300 installation -1 means "not yet
	 * retrieved" 0 is usually used for development builds
	 * 
	 * @see #getSVNRevision()
	 */
	private static int SVNRevision = -1;

	/**
	 * This is for development and a workaround for crappy OS X behaviour when
	 * it comes to multicast and SO_REUSEADDR. It makes p300 return and
	 * alternative ID
	 */
	public static boolean useAltUniqueInstanceHash = false;

	/**
	 * Return the Subversion revision of this p300 installation
	 * 
	 * @return An integer denoting the Subversion revision, e.g. 400
	 * @see #SVNRevision
	 */
	public static int getSVNRevision() {
		if (Configuration.SVNRevision != -1) {
			return Configuration.SVNRevision;
		}

		// If we don't already have a revision (i.e. it's -1), retrieve the
		// revision file contained in the built JAR.
		// Read it, parse it, return it.
		try {
			InputStream is = Resources.getResourceAsStream ("de/guruz/p300/revision");

			if (is == null) {
				throw new Exception("Cannot find revision as resource");
			}

			BufferedInputStream fileInput = new BufferedInputStream(is);

			byte[] buffer = new byte[20];
			fileInput.read(buffer);
			fileInput.close();

			Integer rev = Integer.parseInt((new String(buffer)).trim());

			Configuration.SVNRevision = rev;
			return rev;

		} catch (Exception e) {
			e.printStackTrace();
			Configuration.SVNRevision = 0;
			return 0;
		}
	}

	/**
	 * Used for determining the most current p300 revision without a central
	 * authority
	 * 
	 * @see #updateHighestSeenSVNRevision(int)
	 * @see #getHighestSeenSVNRevision()
	 * @see #isOutdatedSVNRevision()
	 */
	private int highestSeenSVNRevision = -1;

	/**
	 * When seeing another p300 host, this function updates the
	 * {@link #highestSeenSVNRevision} variable
	 * 
	 * @param r
	 *            The seen revision (can be less than the current
	 *            {@link #highestSeenSVNRevision}
	 * @see #highestSeenSVNRevision
	 * @see #getHighestSeenSVNRevision()
	 * @see #isOutdatedSVNRevision()
	 */
	public void updateHighestSeenSVNRevision(int r) {
		if (r > this.highestSeenSVNRevision) {
			this.highestSeenSVNRevision = r;
			this.preferences().putInt("highestSeenSVNRevision", r);
		}
	}

	/**
	 * Return the highest seen Subversion revision in this p300 network
	 * 
	 * @return An int denoting the highest seen SVN revision
	 * @see #highestSeenSVNRevision
	 * @see #updateHighestSeenSVNRevision(int)
	 * @see #isOutdatedSVNRevision()
	 */
	public int getHighestSeenSVNRevision() {
		return this.highestSeenSVNRevision;
	}

	/**
	 * Check if the local p300 is up2date against other seen p300 hosts
	 * Development builds (rev 0) are always up to date.
	 * 
	 * @return True: This host's p300 is outdated (possibly perform an update);
	 *         False: This host's p300 is up to date
	 * @see #highestSeenSVNRevision
	 * @see #updateHighestSeenSVNRevision(int)
	 * @see #getHighestSeenSVNRevision()
	 */
	public boolean isOutdatedSVNRevision() {
		int rev = Configuration.getSVNRevision();

		// svn
		if (rev == 0) {
			return false;
		}

		return (this.highestSeenSVNRevision > rev);
	}



	/**
	 * Set a given host/ip range to be allowed to access this machine
	 * 
	 * @param h
	 *            A host or IP range as String, e.g. "192.168.0.1" or "192."
	 * @param a
	 *            True: Host/range is allowed to access; False: Remove access
	 * @see #isHostAllowed(String)
	 * @see #getAllowedHosts()
	 */
	public void setIpExplicitlyAllowed(String h, boolean a) {
		Preferences node = this.preferences().node("allowedHosts");

		if (a) {
			node.put(h, "allowed");
		} else {
			node.remove(h);
		}

		this.doFlush();
	}

	/**
	 * Return a list of all hosts/ip ranges allowed to access this host
	 * 
	 * @return A list of Strings of the form "192.168.0.1" and/or "192."
	 * @see #isHostAllowed(String)
	 * @see #setHostAllowed(String, boolean)
	 */
	public String[] getExplicitlyAllowedIps() {
		try {
			//this.doSync();
			String keys[] = this.preferences().node("allowedHosts").keys();

			LongestIsGreaterComparator cmp = LongestIsGreaterComparator
					.instance();
			java.util.Arrays.sort(keys, cmp);

			return keys;
		} catch (BackingStoreException e) {

			e.printStackTrace();
			return new String[0];
		}
	}

	/**
	 * Return a list of hosts/ip ranges having no traffic limit on this host
	 * 
	 * @return A list of hosts/ip ranges of the form "192.168.0.1" and/or "192."
	 * @see #isHostUnlimited(String)
	 * @see #setUnlimitedHosts(String[])
	 */
	public String[] getUnlimitedIps() {
		try {
			//this.doSync();
			// String[] keys = preferences().node("unlimitedHosts").keys();
			// for (int i = 0; i < keys.length; i++) {
			// System.err.println("get: " + keys[i]);
			// }
			String[] keys = this.preferences().node("unlimitedHosts").keys();

			LongestIsGreaterComparator cmp = LongestIsGreaterComparator
					.instance();
			java.util.Arrays.sort(keys, cmp);

			return keys;
		} catch (BackingStoreException e) {

			e.printStackTrace();
			return new String[0];
		}
	}

	/**
	 * Set the list of hosts that have no traffic limit on this machine
	 * 
	 * @param hosts
	 *            A list of Strings of the form "192.168.0.1" and/or "192."
	 * @see #isHostUnlimited(String)
	 * @see #getUnlimitedHosts()
	 */
	public void setUnlimitedIps(String[] hosts) {
		Preferences node = this.preferences().node("unlimitedHosts");
		try {
			this.doSync();
			node.removeNode();
			node = this.preferences().node("unlimitedHosts");
			for (String element : hosts) {
				node.put(element, "unlimited");
			}
			this.doFlush();
			// String[] keys = preferences().node("unlimitedHosts").keys();
			// for (int i = 0; i < keys.length; i++) {
			// System.err.println("set: " + keys[i]);
			// }
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}


	/**
	 * TODO: Expand: Source (website?), example values (=> guruz) Return the
	 * default file encoding of the file system Necessary to decode file names
	 * properly to encode them into HTML
	 * 
	 * @return A file encoding, like "<?>"
	 * @see #getDefaultEncoding()
	 */
	public String getFileEncoding() {
		return System.getProperty("file.encoding");
	}

	/**
	 * Return the bandwidth limit
	 * 
	 * @return 0 to *: The bandwidth limit in KB/s
	 * @see #setOutputBWLimitInKb(int)
	 */
	public int getOutputBWLimitInKB() {
		return (int) this.preferences().getLong("outputBWLimitInKB", 0);
	}

	/**
	 * Set the bandwidth limit
	 * 
	 * @param bw
	 *            A value of 0 to * denoting the bandwidth limit in KB/s
	 * @see #getOutputBWLimitInKB()
	 */
	public void setOutputBWLimitInKb(int bw) {
		this.preferences().putLong("outputBWLimitInKB", bw);
	}

	/**
	 * Used where? (=> guruz) Return the default encoding used in p300
	 * <?>
	 * 
	 * @return "UTF-8" (static)
	 * @see #getFileEncoding()
	 */
	public static String getDefaultEncoding() {
		return "UTF-8";
	}

	/**
	 * Set the user stylesheet URL
	 * 
	 * @param stylesheet
	 *            URL as String: Location of user stylesheet
	 * @see #getUserStyleSheet()
	 */
	public void setUserStyleSheet(String stylesheet) {
		this.preferences().put("userStyleSheet", stylesheet);
	}

	/**
	 * Return the user stylesheet location as String
	 * 
	 * @return A String, being the location of the user stylesheet
	 * @see #setUserStyleSheet(String)
	 */
	public String getUserStyleSheet() {
		return this.preferences().get("userStyleSheet", "");
	}

	/**
	 * Return the file system location of the HTTP Upload log as String
	 * 
	 * @return A String showing the location of the HTTP Upload log
	 * @see #setHTTPuploadLog(String)
	 * @see #setHTTPaccessLog(String)
	 * @see #setHTTPerrorLog(String)
	 * @see #getHTTPaccessLog()
	 * @see #getHTTPerrorLog()
	 */
	public String getHTTPuploadLog() {
		return this.preferences().get("HTTPuploadLog",
				"%HOME%/.p300/upload_log");
	}

	/**
	 * Set the file system location of the HTTP Upload log
	 * 
	 * @param f
	 *            A String being the location of the HTTP Upload log
	 * @see #getHTTPuploadLog()
	 * @see #setHTTPaccessLog(String)
	 * @see #setHTTPerrorLog(String)
	 * @see #getHTTPaccessLog()
	 * @see #getHTTPerrorLog()
	 */
	public void setHTTPuploadLog(String f) {
		this.preferences().put("HTTPuploadLog", f);
	}

	/**
	 * Return the file system location of the HTTP access log as String
	 * 
	 * @return A String showing the location of the HTTP access log
	 * @see #setHTTPaccessLog(String)
	 * @see #getHTTPuploadLog()
	 * @see #setHTTPerrorLog(String)
	 * @see #getHTTPaccessLog()
	 * @see #getHTTPerrorLog()
	 * @see #setHTTPuploadLog(String)
	 */
	public String getHTTPaccessLog() {
		return this.preferences().get("HTTPaccessLog",
				"%HOME%/.p300/access_log");
	}

	/**
	 * Set the file system location of the HTTP access log
	 * 
	 * @param f
	 *            A String being the location of the HTTP access log
	 * @see #getHTTPaccessLog()
	 * @see #setHTTPaccessLog(String)
	 * @see #getHTTPuploadLog()
	 * @see #setHTTPerrorLog(String)
	 * @see #getHTTPerrorLog()
	 * @see #setHTTPuploadLog(String)
	 */
	public void setHTTPaccessLog(String f) {
		this.preferences().put("HTTPaccessLog", f);
	}

	/**
	 * Return the file system location of the HTTP error log as String
	 * 
	 * @return A String showing the location of the HTTP error log
	 * @see #setHTTPerrorLog(String)
	 * @see #getHTTPaccessLog()
	 * @see #setHTTPaccessLog(String)
	 * @see #getHTTPuploadLog()
	 * @see #getHTTPerrorLog()
	 * @see #setHTTPuploadLog(String)
	 */
	public String getHTTPerrorLog() {
		return this.preferences().get("HTTPerrorLog", "%HOME%/.p300/error_log");
	}

	/**
	 * Set the file system location of the HTTP error log
	 * 
	 * @param f
	 *            A String being the location of the HTTP error log
	 * @see #getHTTPerrorLog()
	 * @see #setHTTPerrorLog(String)
	 * @see #getHTTPaccessLog()
	 * @see #setHTTPaccessLog(String)
	 * @see #getHTTPuploadLog()
	 * @see #setHTTPuploadLog(String)
	 */
	public void setHTTPerrorLog(String f) {
		this.preferences().put("HTTPerrorLog", f);
	}

	/**
	 * Returns the setting for the icon set. We only have one icon set
	 * currently, so it returns 0.
	 * 
	 * @return 0 (static) - new iconset
	 * @see #setIconType(int)
	 */
	public int getIconType() {
		// return preferences().getInt("icontype", 0);
		return 0;
	}

	/**
	 * Set the icon type (icon set) We only have one "new iconset", so this
	 * can't be used; {@link #getIconType()} will always return 0.
	 * 
	 * @param newIconType
	 *            An integer denoting the icontype to use
	 * @see #getIconType()
	 */
	public void setIconType(int newIconType) {
		this.preferences().putInt("icontype", newIconType);
	}

	public boolean isUnshareableFile(String fn) {
		String rfn = fn.toLowerCase();

		if (rfn.equals(".ds_store")) {
			return true;
		}

		if (rfn.equals("desktop.ini")) {
			return true;
		}

		if (rfn.startsWith(".trash")) {
			return true;
		}

		return false;
	}

	/**
	 * this is called inside a get-method before reading anything from the
	 * preferences that may be changed by multiple instances of p300 (allowed
	 * hosts, admin password)
	 * 
	 */
	public void doFlush() {
		// System.err.println(">doFlush");
		try {
			this.doSync();
			this.preferences().flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		// System.err.println("<doFlush");
	}

	/**
	 * this is called inside a get-method after writing anything to the
	 * preferences that may be changed by multiple instances of p300 (allowed
	 * hosts, admin password)
	 * 
	 */
	public void doSync() {
		// System.err.println(">doSync");
		// We need to call this twice because of a goddamn Java prefs bug
		// or something... it will only sync if called twice.
		try {
			this.preferences().sync();
			this.preferences().sync();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		// System.err.println("<doSync");
	}

	/**
	 * 
	 * 
	 * @see #getDiscoveredP300Hosts()
	 * @see #setDiscoveredP300Hosts(String[])
	 * @author guruz
	 */
	Object discoveredHostsSynchronizer = new Object();

	/**
	 * Read a list of previously discovered P300 hosts from config
	 * 
	 * @return A list of IPs running P300
	 * @see #discoveredHostsSynchronizer
	 * @see #setDiscoveredP300Hosts(String[])
	 * @author guruz
	 */
	public String[] getDiscoveredP300Hosts() {
		synchronized (this.discoveredHostsSynchronizer) {
			try {
				return this.preferences().node("discoveredP300Hosts").keys();
			} catch (BackingStoreException e) {

				e.printStackTrace();
				return new String[0];
			}
		}
	}

	/**
	 * Write a list of discovered P300 hosts to config
	 * 
	 * @param hostips
	 *            A list of IPs of hosts running p300
	 * @see #discoveredHostsSynchronizer
	 * @see #getDiscoveredP300Hosts()
	 * @author guruz
	 */
	public void setDiscoveredP300Hosts(String[] hostips) {
		synchronized (this.discoveredHostsSynchronizer) {
			Preferences p = this.preferences().node("discoveredP300Hosts");
			try {
				p.clear();
				for (String element : hostips) {
					p.put(element, "OK");
				}
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	/**
	 * 
	 * 
	 * @author guruz
	 * @param hostip
	 * @see #getBootstrapP300Host()
	 */
	public void setBootstrapP300Host(String hostip) {
		this.preferences().put("bootstrapP300Host", hostip);
	}

	/**
	 * 
	 * @author guruz
	 * @return
	 * @see #setBootstrapP300Host(String)
	 */
	public String getBootstrapP300Host() {
		return this.preferences().get("bootstrapP300Host", null);
	}

	/**
	 * 
	 * 
	 * @author guruz
	 * @return
	 */
	public Preferences getSessionStore() {
		return this.preferences().node("sessionStore");
	}

	/**
	 * 
	 * 
	 * @author guruz
	 * @see #isFirstStart()
	 */
	private Boolean firstStart = null;

	/**
	 * Returns true during the whole first invocation and false in all future
	 * runs
	 * 
	 * @return True: First run of p300; False: Not first run of p300
	 * @see #firstStart
	 * @author guruz
	 */
	public boolean isFirstStart() {
		if (this.firstStart == null) {
			boolean first = this.preferences().getBoolean("firstStart", true);
			this.firstStart = first;
			this.preferences().putBoolean("firstStart", false);
			this.doFlush();
		}

		return this.firstStart.booleanValue();
	}

	/**
	 * Read the speed of the file indexer
	 * 
	 * @return Integer representing the speed of the file indexer in
	 *         milliseconds wait time between files
	 * @author tomcat
	 * @see #setIndexerSpeed(int)
	 * @see #setIndexerInterval(IntervalLevel)
	 * @see #getIndexerInterval()
	 * @see de.guruz.p300.search.Indexer
	 */
	public int getIndexerSpeed() {
		return this.preferences().getInt("indexerspeed",
				Indexer.defaultSpeedValue);
	}

	/**
	 * Set the speed of the file indexer
	 * 
	 * @param indexerSpeed
	 *            Integer representing the speed of the file indexer in
	 *            milliseconds wait time between file
	 * @author tomcat
	 * @see #getIndexerInterval()
	 * @see #getIndexerSpeed()
	 * @see #setIndexerInterval(IntervalLevel)
	 * @see de.guruz.p300.search.Indexer
	 */
	public void setIndexerSpeed(int indexerSpeed) {
		this.preferences().putInt("indexerspeed", indexerSpeed);
	}

	/**
	 * 
	 * 
	 * @param s
	 * @return
	 * @author guruz
	 */
	public static String configDirFileName(String s) {
		return System.getProperty("user.home") + File.separator + ".p300" + File.separator + s;
	}

	/**
	 * 
	 * 
	 * @author guruz
	 * @return
	 * @see #setTOSFlag(byte)
	 */
	public byte getTOSFlag() {
		byte val = (byte) this.preferences().getInt("UploadTOSFlag", 4);
		return val;
	}

	/**
	 * 
	 * @author guruz
	 * @param b
	 * @see #getTOSFlag()
	 */
	public void setTOSFlag(byte b) {
		this.preferences().putInt("UploadTOSFlag", b);
	}

	/**
	 * Read the interval time at which the indexer will run (like hourly or
	 * daily)
	 * 
	 * @author tomcat
	 * @return One of the IntervalLevel levels
	 * @see #setIndexerSpeed(int)
	 * @see #getIndexerSpeed()
	 * @see #setIndexerInterval(IntervalLevel)
	 * @see de.guruz.p300.search.IndexerThread.IntervalLevel
	 */
	public IntervalLevel getIndexerInterval() {
		String enumName = this.preferences().get("indexerinterval",
				IndexerThread.defaultInterval.name());
		return Enum.valueOf(IntervalLevel.class, enumName);
	}

	/**
	 * Set the interval time at which the indexer will run (like hourly or
	 * daily)
	 * 
	 * @author tomcat
	 * @param level
	 *            One of the IntervalLevel levels
	 * @see #setIndexerSpeed(int)
	 * @see #getIndexerSpeed()
	 * @see #getIndexerInterval()
	 * @see de.guruz.p300.search.IndexerThread.IntervalLevel
	 */
	public void setIndexerInterval(IntervalLevel level) {
		String enumName = level.name();
		this.preferences().put("indexerinterval", enumName);
	}

	
	protected long m_lastRevisionRetrieval = 0;
	
	/**
	 *  The config item name is wrong for legacy (=typo)
	 * reasons
	 */
	public void updateLastRevisionRetrieval() {
		m_lastRevisionRetrieval = System.currentTimeMillis();
		this.preferences().putLong("LastRevisionRetrievel",
				m_lastRevisionRetrieval);

	}

	/**
	 *  The config item name is wrong for legacy (=typo)
	 * reasons
	 */
	public long getLastRevisionRetrieval() {
		return this.preferences().getLong("LastRevisionRetrievel", m_lastRevisionRetrieval);
	}

	/**
	 *  The config item name is wrong for legacy (=typo)
	 * reasons
	 * 
	 * @author guruz
	 * @see #updateLastRevisionRetrieval()
	 * @see #getLastRevisionRetrieval()
	 * @see #getLastUpdateRetrieval()
	 */
	public void updateLastUpdateRetrieval() {
		this.preferences().putLong("LastUpdateRetrievel",
				System.currentTimeMillis());
	}

	/**
	 *  The config item name is wrong for legacy (=typo)
	 * reasons
	 * 
	 * @author guruz
	 * @see #updateLastRevisionRetrieval()
	 * @see #updateLastUpdateRetrieval()
	 * @see #updateLastUpdateRetrieval()
	 * @return
	 */
	public long getLastUpdateRetrieval() {
		return this.preferences().getLong("LastUpdateRetrievel", 0);
	}





	/**
	 * Set the location of the browser on UNIX systems
	 * 
	 * @param val
	 *            The browser location (file system, like /usr/bin/firefox)
	 * @author tomcat
	 * @see #getUnixBrowser()
	 * @see de.guruz.p300.utils.launchers.BareBonesBrowserLaunch
	 */
	public void setUnixBrowser(String val) {
		if (val == null) {
			val = "";
		}
		this.preferences().put("unixBrowser", val);
	}

	/**
	 * Read the location of the browser on UNIX systems
	 * 
	 * @return The browser location (file system, like /usr/bin/firefox)
	 * @author tomcat
	 * @see #setUnixBrowser(String)
	 * @see de.guruz.p300.utils.launchers.BareBonesBrowserLaunch
	 */
	public String getUnixBrowser() {
		return this.preferences().get("unixBrowser", "");
	}

	/**
	 * Get Java vendor and Java version for the front page
	 * 
	 * @author guruz
	 * @return A string containg Java version and vendor
	 * @see OsUtils#getOS()
	 * @see OsUtils#isWindows()
	 * @see OsUtils#isOSX()
	 * @see OsUtils#isLinux()
	 * @see #isJava15()
	 */
	public static String getJavaVersion() {
		String vendor = System.getProperty("java.vendor", "?");
		String version = System.getProperty("java.version", "?");

		return version + " (" + vendor + ')';

	}

	/**
	 * Convenience method for share objects Adds a share to the list of shares
	 * 
	 * @param s
	 *            The share object to add
	 * @see #setDirectoryFromShareName(String, String)
	 * @see #getDirFromShareName(String)
	 * @see #unshareShare(String)
	 * @see #removeShare(Share)
	 * @see #removeShare(String)
	 * @see #getShareNames()
	 * @author tomcat
	 */
	public void addShare(Share s) {
		this.setDirectoryFromShareName(s.getName(), s.getFileLocation());
	}

	/**
	 * Convenience method for share objects Remove a share from the list of
	 * shares
	 * 
	 * @param s
	 *            The share object to remove
	 * @see #setDirectoryFromShareName(String, String)
	 * @see #getDirFromShareName(String)
	 * @see #unshareShare(String)
	 * @see #removeShare(String)
	 * @see #getShareNames()
	 * @see #addShare(Share)
	 * @author tomcat
	 */
	public void removeShare(Share s) {
		this.unshareShare(s.getName());
	}

	/**
	 * Find out if we're running on Java 1.5
	 * 
	 * @return True: Running on Java >=1.5; False: Java <1.5
	 * @author guruz
	 * @see #getJavaVersion()
	 */
	public static boolean isJava15() {
		return System.getProperty("java.version", "?").startsWith("1.5");
	}

	/**
	 * Convenience method for share objects Remove a share name from the list of
	 * shares
	 * 
	 * @param shareName
	 *            Name of the share to remove
	 * @see #setDirectoryFromShareName(String, String)
	 * @see #getDirFromShareName(String)
	 * @see #unshareShare(String)
	 * @see #getShareNames()
	 * @see #addShare(Share)
	 * @see #removeShare(Share)
	 * @author tomcat
	 */
	public void removeShare(String shareName) {
		this.unshareShare(shareName);
	}

	/**
	 * 
	 * @author guruz
	 * @return
	 * @see #setRunLatest(boolean)
	 */
	public boolean getAlwaysRunLatestVersion() {
		return this.preferences().getBoolean("alwaysRunLatestVersion", true);
	}

	/**
	 * 
	 * @return
	 * @author guruz
	 * @see #setTryUpdateEveryWeek(boolean)
	 */
	public boolean getTryUpdateEveryWeek() {
		return this.preferences().getBoolean("tryUpdateEveryWeek", true);
	}

	/**
	 * 
	 * 
	 * @param b
	 * @author guruz
	 * @see #getAlwaysRunLatestVersion()
	 */
	public void setRunLatest(boolean b) {
		this.preferences().putBoolean("alwaysRunLatestVersion", b);

	}

	/**
	 * 
	 * @param b
	 * @author guruz
	 * @see #getTryUpdateEveryWeek()
	 */
	public void setTryUpdateEveryWeek(boolean b) {
		this.preferences().putBoolean("tryUpdateEveryWeek", b);

	}

	/**
	 * Create the settings directory if it doesn't exist
	 * 
	 * @see #configDirFileName(String)
	 * @author guruz
	 */
	public static void createDotP300() {
		File d = new File(Configuration.configDirFileName(""));
		DirectoryUtils.makeSureDirectoryExists(d);
	}

	public String getIncompleteDownloadDir() {
		File d = new File(Configuration
				.configDirFileName("incomplete_downloads"));
		de.guruz.p300.utils.DirectoryUtils.makeSureDirectoryExists(d);
		return d.getAbsolutePath();
	}

	// FIXME: dirsep?
	public String getFinishedDownloadDir() {
		return this.preferences().get("finishedDownloadDir",
				"%HOME%/p300_downloads");
	}

	public void setFinishedDownloadDir(String d) {
		this.preferences().put("finishedDownloadDir", d);
	}

	public String getDownloadInformationDir() {
		File d = new File(Configuration
				.configDirFileName("download_informations"));
		de.guruz.p300.utils.DirectoryUtils.makeSureDirectoryExists(d);
		return d.getAbsolutePath();
	}

	public String getFinishedDownloadInformationDir() {
		File d = new File(Configuration
				.configDirFileName("finished_download_informations"));
		de.guruz.p300.utils.DirectoryUtils.makeSureDirectoryExists(d);
		return d.getAbsolutePath();
	}

	public static int getClientReceiveBufferSize ()
	{
		return (int) (DownloadFile.DEFAULT_CHUNK_SIZE / 2);
	}
	
	public static int getServerSendBufferSize ()
	{
		return (int) (DownloadFile.DEFAULT_CHUNK_SIZE / 2);
	}

	public void setShowAdvancedOptions(boolean b) {
		this.preferences().putBoolean("showAdvancedOptions", b);
	}

	public boolean isShowAdvancedOptions() {
		return this.preferences().getBoolean("showAdvancedOptions", false);
	}
	
	/**
	 * default is false because this feature was introduced late.
	 * on the first start, it is set to true
	 * @return
	 */
	public boolean isLocalNetworkIpsImplcitlyAllowed ()
	{
		return this.preferences().getBoolean("localNetworkIpsImplicitlyAllowed", false);
	}
	
	public void setLocalNetworkIpsImplicitlyAllowed (boolean b)
	{
		this.preferences().putBoolean("localNetworkIpsImplicitlyAllowed", b);
	}
	
	public void setPlayChatSound(boolean b) {
		this.preferences().putBoolean("sounds/chatMessage", b);
	}

	public boolean isPlayChatSound() {
		return this.preferences().getBoolean("sounds/chatMessage", true);
	}
	
}
