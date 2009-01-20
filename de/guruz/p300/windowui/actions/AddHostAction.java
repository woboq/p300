package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.IP;
import de.guruz.p300.utils.IconChooser;

/**
 * This can be called with or without gui, if with then urlOrHostip is null
 * because it will be asked for it
 * 
 * @author guruz
 * 
 */
public class AddHostAction extends AbstractAction {

	protected String m_urlOrHostip = null;

	public AddHostAction() {
		super("Add Another p300 Host", IconChooser.getAddIcon());
	}

	public AddHostAction(String urlOrHostip) {
		super("Add Another p300 Host", IconChooser.getAddIcon());
		m_urlOrHostip = urlOrHostip;

	}

	private void askHost(final String ipport) {
		

		new Thread(new Runnable() {

			public void run() {
				try {
					// FIXME: was wenn host = url

					String host = (ipport.split(":"))[0];
					InetAddress inetAddress = InetAddress.getByName(host);
					if (inetAddress instanceof Inet4Address) {
						String ip = inetAddress.getHostAddress();
						Configuration.instance().setIpExplicitlyAllowed(ip,
								true);
						D.out("Allowing host " + ip);
						
						String new_ipport = ip + ":" + IP.getPortFromHostPort (ipport);
						MainDialog.hostFinderThread.askImmediatly(new_ipport);
						Configuration.instance().setBootstrapP300Host(new_ipport);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}

		}).start();
	}

	public void actionPerformed(ActionEvent arg0) {
		String s = null;
		if (m_urlOrHostip == null) {
			s = (String) JOptionPane
					.showInputDialog(
							MainDialog.getWindow(),
							"Enter a Hostname, Hostname:Port, IP, IP:Port or HTTP-URL of a p300 node",
							"Manually add host", JOptionPane.PLAIN_MESSAGE,
							null, null, null);
		} else {
			s = m_urlOrHostip;
		}

		if (s != null && s.trim().length() > 0) {
			if (s.startsWith("http://")) {
				try {
					URL u = new URL(s);

					String host = u.getHost();
					int port = u.getPort();
					if (port == -1)
						port = 4337;

					askHost(host + ":" + port);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (IP.matchesIPorHostnamePort(s)) {
				// ip:port or hostname:port
				askHost(s);
			} else if (!s.contains(":")) {
				// ip or host
				askHost(s + ":4337");
			} else {
				if (m_urlOrHostip == null)
					JOptionPane.showMessageDialog(MainDialog.getWindow(),
							"Sorry, invalid input");
			}
		}

	}

}
