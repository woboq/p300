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
package de.guruz.p300.threads;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import de.guruz.p300.Configuration;
import de.guruz.p300.Constants;
import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.allowing.GetMeAllowedHelper;
import de.guruz.p300.hosts.allowing.UnallowedHosts;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.Integers;

/**
 * This thread receives (and sends) the LAN multicast packets for discovery
 * 
 * @author guruz
 * 
 */
public class UdpListenThread extends Thread {
	MulticastSocket m_allDevicesMulticastSocket;

	MulticastSocket m_singleDeviceMulticastSocket;

	DatagramSocket m_broadcastSocket;

	InetSocketAddress m_multicastIsa;

	protected GetMeAllowedHelper m_udpAllowHelper;

	public long lastReceive;

	public long lastReceiveNotFromUs;

	public long lastSentIAmHerePeriodic;

	long lastIAmHereAll;

	Timer m_discoverLaterTimer = new Timer();

	public UdpListenThread() {
		m_udpAllowHelper = new GetMeAllowedHelper();

		try {
			Configuration conf = Configuration.instance();

			this.m_multicastIsa = new InetSocketAddress(
					conf.getMulticastHost(), conf.getMulticastPort());

			// these constructors should already call setReuseAddr
			try {
				this.m_broadcastSocket = new DatagramSocket(null);
				m_broadcastSocket.setReuseAddress(true);
				m_broadcastSocket.setBroadcast(true);
				m_broadcastSocket.bind(new InetSocketAddress(4337));
			} catch (BindException be) {
				this.m_broadcastSocket = null;
				D.out("Error binding broadcast/unicast socket ("
						+ be.getMessage() + ")");
				// Catch all errors (tomcat 2007-12-22)
			} catch (Throwable e) {
				e.printStackTrace();
				m_broadcastSocket = null;
			}

			try {
				this.m_allDevicesMulticastSocket = new MulticastSocket(conf
						.getMulticastPort());
				// System.out.println (this.allSocket.getReuseAddress());
				this.m_allDevicesMulticastSocket.setLoopbackMode(false);
				this.m_allDevicesMulticastSocket.setTimeToLive(100); // hm?
				this.m_allDevicesMulticastSocket.setReuseAddress(true);
				this.m_allDevicesMulticastSocket.setBroadcast(false);
				this.m_allDevicesMulticastSocket.joinGroup(this.m_multicastIsa
						.getAddress());
			} catch (BindException be) {
				this.m_allDevicesMulticastSocket = null;
				D.out("Error binding m_allDevicesMulticastSocket socket ("
						+ be.getMessage() + ")");
				// Catch all errors (tomcat 2007-12-22)
			} catch (Throwable e) {
				e.printStackTrace();
				m_allDevicesMulticastSocket = null;
			}

			try {
				m_singleDeviceMulticastSocket = new MulticastSocket(conf
						.getMulticastPort());

				Enumeration<NetworkInterface> nifaces = NetworkInterface
						.getNetworkInterfaces();
				while (nifaces.hasMoreElements()) {
					NetworkInterface iface = nifaces.nextElement();
					try {
						m_singleDeviceMulticastSocket
								.setNetworkInterface(iface);
						m_singleDeviceMulticastSocket.setLoopbackMode(false);
						m_singleDeviceMulticastSocket.setTimeToLive(100); // hm?
						m_singleDeviceMulticastSocket.setReuseAddress(true);
						m_singleDeviceMulticastSocket.setBroadcast(false);
						m_singleDeviceMulticastSocket.joinGroup(m_multicastIsa
								.getAddress());
					} catch (Throwable t) {
						D.out (iface.getDisplayName() + ": " + t.getMessage());
					}
				}

			} catch (BindException be) {
				m_singleDeviceMulticastSocket = null;
				D.out("Error binding m_singleDeviceMulticastSocket socket ("
						+ be.getMessage() + ")");
			} catch (Throwable e) {
				e.printStackTrace();
				m_singleDeviceMulticastSocket = null;
			}

		} catch (Throwable e) {

			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		try {
			this.setName("MulticastListenThread");
		} catch (Exception e) {
		}

		if (this.m_allDevicesMulticastSocket == null) {
			D
					.out("Multicast socket not successfully created, NOT using multicast");
			return;
		}

		try {
			D.out("Starting to listen for UDP");
			m_udpAllowHelper.start();
			sendPingToAll();
			sendIAmHere();

			boolean cont = true;
			while (cont) {
				// D.out("Now polling on allDevicesMulticastSocket");
				DatagramPacket multicastPacket = receiveOn(m_allDevicesMulticastSocket);
				if (multicastPacket != null)
					handleIncomingPacket(multicastPacket);

				// D.out("Now polling on broadcastSocket");
				DatagramPacket broadcastPacket = receiveOn(m_broadcastSocket);
				if (broadcastPacket != null)
					handleIncomingPacket(broadcastPacket);

				if (lastSentIAmHerePeriodic
						+ Constants.DISCOVERY_MULTICAST_IAMHERE_INTERVAL_MSEC < System
						.currentTimeMillis()) {
					sendIAmHere();
					lastSentIAmHerePeriodic = System.currentTimeMillis();
				}

			}

			this.m_allDevicesMulticastSocket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void handleIncomingPacket(DatagramPacket packet) {
		String addr = packet.getAddress().getHostAddress();

		// do we not allow the packet? note that down ...
		if (!MainDialog.getHostAllowanceManager().isIpAllowed(addr)) {
			// setLastUnallowedReceiveFrom(addr);
			UnallowedHosts.addIP(addr);
		}
		// ... but continue

		String payload = null;
		try {
			payload = new String(packet.getData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parsePacket(payload, addr);
	}

	private DatagramPacket receiveOn(DatagramSocket datagramSocket)
			throws IOException {
		DatagramPacket packet;
		byte[] buf = new byte[512];
		packet = new DatagramPacket(buf, buf.length);
		try {
			datagramSocket.setSoTimeout(14 * 1000);
			datagramSocket.receive(packet);

			this.lastReceive = System.currentTimeMillis();

			return packet;

			// String addr = packet.getAddress().getHostAddress();

			// D.out("address=" + addr);
		} catch (java.net.SocketTimeoutException e) {
			return null;
		}
	}

	private void parsePacket(String payload, final String remoteIp) {
		try {
			// D.out("UDP from " + remoteIp + "=" + payload);

			if (!payload.startsWith("p300")) {
				D.out("Invalid packet received from " + remoteIp);
				return;
			}

			String contents[] = payload.split(" ", 10);
			// D.out(remoteIp + " to us: " + payload + " length=" +
			// contents.length);

			if (contents.length < 3) {
				D.out("received via multicast: " + payload);
				D.out("packet too short, count is " + contents.length);
				return;
			}

			if (!contents[1].equals("0.1")) {
				D.out("received via multicast: " + payload);
				D.out("packet for wrong version: " + contents[1]);
				return;
			}

			String command = contents[2];
			if (command.equals("P")) {
				// a Ping message
				if (contents.length < 5) {
					D.out("received via multicast: " + payload);
					D.out("ping packet too short, count is " + contents.length);
					return;
				}
				this.sendIAmHere();
			} else if (contents[2].equals("H")) {
				// a I am here message
				if (contents.length < 7) {
					D.out("received via multicast: " + payload);
					D.out("here i am packet too short, count is "
							+ contents.length);
					return;
				}

				final String remotePort = contents[3];
				String remoteHash = contents[4];
				String ourHash = Configuration.instance().getUniqueHash();
				String remoteCookie = null;
				if (contents.length > 8)
					remoteCookie = contents[7];

				if (!ourHash.equals(remoteHash)) {
					// we received a packet that is not from us
					this.lastReceiveNotFromUs = System.currentTimeMillis();

					// now that we have learnt about it, discover it later with
					// HTTP if it is allowed
					m_discoverLaterTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							if (MainDialog.m_hostAllowanceManager
									.isIpAllowed(remoteIp))
								MainDialog.hostFinderThread
										.addPossibleP300Host(remoteIp,
												remotePort);
						}

					}, 10 * 1000);

					// also try via UDP to get the host to allow us
					// remote cookie exists only since november 2008
					if (remoteCookie != null && !remoteCookie.equals("END"))
						m_udpAllowHelper.add(remoteIp, Integers
								.getIntegerDefault(remotePort, 4337),
								remoteCookie);

				}
			} else if (contents[2].equals("A")) {
				// a Allow message
				if (contents.length < 6) {
					D.out("received via multicast: " + payload);
					D.out("aallow packet too short, count is "
							+ contents.length);
					return;
				}

				final String remotePort = contents[3];
				String remoteHash = contents[4];
				String remoteCookieThatShouldBeOurs = contents[5];
				String localCookie = MainDialog.getHostAllowanceManager()
						.getLocalAuthCookieForImplicitAllow();

				if (localCookie.equals(remoteCookieThatShouldBeOurs)) {
					// D.out("******** Received proper Allow packet from "
					// + remoteIp);

					// host implizit erlauben
					MainDialog.m_hostAllowanceManager
							.hostDectectedAsLocal(remoteIp);

					// delayed add into hostfinder (only if allowed)
					m_discoverLaterTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							if (MainDialog.m_hostAllowanceManager
									.isIpAllowed(remoteIp))
								MainDialog.hostFinderThread
										.addPossibleP300Host(remoteIp,
												remotePort);
						}

					}, 10 * 1000);
				} else {
					// D.out("******** Received wrong allow packet from "
					// + remoteIp);
				}
			} else {
				D.out("received via multicast: " + payload);
				D.out("unknown packet: " + contents[2]);
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Used by sendPingToALl (used by the configuration) so that we have nearly
	 * immediatly the current hosts
	 * 
	 * @param dp
	 */
	public void sendMulticastPacketToAll(DatagramPacket dp) {
		if (this.m_allDevicesMulticastSocket == null)
			return;

		synchronized (this.m_allDevicesMulticastSocket) {
			try {
				dp.setSocketAddress(this.m_multicastIsa);

				// D.out("sending " + new String(dp.getData()));

				Enumeration<NetworkInterface> nifaces = NetworkInterface
						.getNetworkInterfaces();
				while (nifaces.hasMoreElements()) {
					NetworkInterface iface = nifaces.nextElement();

					try {
						m_singleDeviceMulticastSocket
								.setNetworkInterface(iface);
						m_singleDeviceMulticastSocket.send(dp);

						// D.out(iface.toString());
					} catch (Throwable t) {
						// do not print an error here, it is not critical
						// t.printStackTrace();
					}
				}

				m_allDevicesMulticastSocket.send(dp);

				// D.out("...sent!");

			} catch (Exception e) {
				e.printStackTrace();
				D.out("Sending multicast packet to all failed: "
						+ e.getMessage());
			}
		}

	}

	public DatagramPacket constructIAmHerePacket() {
		Configuration conf = Configuration.instance();
		String stringPacket = "p300 0.1 H "
				+ MainDialog.getCurrentHTTPPort()
				+ ' '
				+ conf.getUniqueHash()
				+ ' '
				+ conf.getLocalDisplayName()
				+ ' '
				+ Configuration.getSVNRevision()
				+ ' '
				+ MainDialog.getHostAllowanceManager()
						.getLocalAuthCookieForImplicitAllow() + ' ' + "END";

		byte buf[];

		try {
			buf = stringPacket.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			buf = stringPacket.getBytes();
		}

		return new DatagramPacket(buf, buf.length);
	}

	public void sendIAmHere() {
		// we have sent one in the past 5 seconds -> dont send again
		if (System.currentTimeMillis() - this.lastIAmHereAll < 5 * 1000) {
			return;
		}

		DatagramPacket dp = this.constructIAmHerePacket();

		MainDialog.multicastListenThread.sendMulticastPacketToAll(dp);
		MainDialog.multicastListenThread.sendBroadcastPacket(dp);

		this.lastIAmHereAll = System.currentTimeMillis();

	}

	private void sendBroadcastPacket(DatagramPacket dp) {
		if (this.m_broadcastSocket == null)
			return;

		try {
			dp.setAddress(InetAddress.getByName("255.255.255.255"));
			dp.setPort(Configuration.getMulticastPort());

			m_broadcastSocket.send(dp);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected DatagramPacket constructPingPacket() {
		Configuration conf = Configuration.instance();

		String stringPacket = "p300 0.1 P " + MainDialog.getCurrentHTTPPort()
				+ ' ' + conf.getUniqueHash() + " END";
		byte buf[];
		try {
			buf = stringPacket.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			buf = stringPacket.getBytes();
		}

		return new DatagramPacket(buf, buf.length);
	}

	public void sendPingToAll() {
		sendMulticastPacketToAll(this.constructPingPacket());
		sendBroadcastPacket(this.constructPingPacket());
	}

	public void printKnownHosts() {
		MainDialog.hostMap.printKnownOnlineHosts();

	}

}
