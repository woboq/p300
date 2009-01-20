/**
 * 
 */
package de.guruz.p300.hosts.allowing;

class GetMeAllowedHelperQueueEntry {
	String m_remoteIp;
	int m_remotePort;
	String m_remoteCookie;

	public GetMeAllowedHelperQueueEntry(String remoteIp, int remotePort, String remoteCookie) {
		m_remoteIp = remoteIp;
		m_remotePort = remotePort;
		m_remoteCookie = remoteCookie;
	}

	public String getRemoteIp() {
		return m_remoteIp;
	}

	public int getRemotePort() {
		return m_remotePort;
	}

	public String getRemoteCookie() {
		return m_remoteCookie;
	}
}