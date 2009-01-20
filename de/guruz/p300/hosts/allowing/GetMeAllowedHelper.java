package de.guruz.p300.hosts.allowing;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import de.guruz.p300.Configuration;
import de.guruz.p300.Constants;
import de.guruz.p300.MainDialog;

public class GetMeAllowedHelper {



	public GetMeAllowedHelper() {
		super();
		m_queue = new ArrayBlockingQueue<GetMeAllowedHelperQueueEntry> (300);
		m_rateLimitingMap = new HashMap<String, Long> ();
	}

	/**
	 * This queue contains the requests
	 */
	protected BlockingQueue<GetMeAllowedHelperQueueEntry> m_queue;

	/*
	 * key: ip:port value: Long with semantic
	 * "no sending UDP to this ip:port before this timestamp"
	 */
	protected Map<String, Long> m_rateLimitingMap;

	/**
	 * Add to the queue to send an UDP packet later. But only if this queue still has space
	 */
	public void add(String remoteIp, int remotePort, String remoteCookie) {
		if (m_queue.remainingCapacity() > 0)
			m_queue.add(new GetMeAllowedHelperQueueEntry(remoteIp, remotePort, remoteCookie));
	}

	/*
	 * Starts the thread that deals with m_queue;
	 */
	public void start() {
		new Thread() {
			public void run() {
				DatagramSocket dsocket;
				try {
					dsocket = new DatagramSocket();
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}

				while (!MainDialog.requestedShutdown) {
					try {
						// poll a long while. if no entry is in, poll again
						GetMeAllowedHelperQueueEntry qe = m_queue.poll(1000, TimeUnit.SECONDS);

						if (qe != null) {
							String mapKey = qe.getRemoteIp() + ':'
									+ qe.getRemotePort();

							// send the packet, but only if rate limiting allows this
							if (!m_rateLimitingMap.containsKey(mapKey)) {
								// the entry is not in the map already, this means
								// it was newly added. we will try with UDP and HTTP
								getMeAllowedUdp (dsocket, qe);
								getMeAllowedHttp (qe);
								m_rateLimitingMap
										.put(
												mapKey,
												new Long(
														System
																.currentTimeMillis()
																+ Constants.DISCOVERY_ALLOWME_MESSAGE_INTERVAL));
							} else {
								long noSendBefore = m_rateLimitingMap
										.get(mapKey);
								if (noSendBefore < System.currentTimeMillis()) {
									// try with UDP or HTTP
									getMeAllowed (dsocket, qe);
									m_rateLimitingMap
											.put(
													mapKey,
													new Long(
															System
																	.currentTimeMillis()
																	+ Constants.DISCOVERY_ALLOWME_MESSAGE_INTERVAL));
								}
							}

						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				dsocket.close();
			}
			
			
			
			protected AllowKind m_allowKind = AllowKind.UDP; 
			
			protected void getMeAllowed (DatagramSocket socket, GetMeAllowedHelperQueueEntry qe)
			{
				// invert allow kind
				m_allowKind = (m_allowKind == AllowKind.UDP ? AllowKind.HTTP : AllowKind.UDP);
				
				if (m_allowKind == AllowKind.UDP)
				{
					getMeAllowedUdp (socket, qe);
				}
				else if (m_allowKind == AllowKind.HTTP)
				{
					getMeAllowedHttp (qe);
				}
			}
			
			protected void getMeAllowedUdp (DatagramSocket socket, GetMeAllowedHelperQueueEntry qe)
			{
				sendPacketFor(socket, qe);
			}
			
			protected void getMeAllowedHttp (GetMeAllowedHelperQueueEntry qe)
			{
				GetMeAllowedByHttpJob.getMeAllowedHttp(qe);
			}
			

			protected void sendPacketFor(DatagramSocket socket, GetMeAllowedHelperQueueEntry qe) {
				try {
					// create and send udp
					String stringPacket = "p300 0.1 A "
							+ MainDialog.getCurrentHTTPPort() + ' '
							+ Configuration.instance().getUniqueHash() + ' ' + qe.getRemoteCookie ()+  " END";
					byte buf[];
					try {
						buf = stringPacket.getBytes("UTF-8");
					} catch (UnsupportedEncodingException e) {
						buf = stringPacket.getBytes();
					}

					DatagramPacket packet = new DatagramPacket(buf, buf.length,
							InetAddress.getByName(qe.getRemoteIp()), qe.getRemotePort());
					//D.out("******** SenD allow packet to " + qe.getRemoteIp() + ": " + stringPacket);
					socket.send(packet);
					
					//D.out("******** SenT allow packet to " + qe.getRemoteIp() + ": " + stringPacket);

					buf = null;

					// to limit the UDP send frequency, we sleep a bit here
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}.start();
	}

	public static String getHttpPathForCookie(String authCookie) {
		return "/hostfinder/0.1/allowMe?"+ authCookie;
	}

}
