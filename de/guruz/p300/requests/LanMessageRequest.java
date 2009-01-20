package de.guruz.p300.requests;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.logging.D;
import de.guruz.p300.onetoonechat.Message;
import de.guruz.p300.threads.RequestThread;

public class LanMessageRequest extends Request {

	public static final String CHATPATH = "/lanmessage/0.1";

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (rt != HTTPVerb.POST) {
			return false;
		}

		if (!reqpath.equals(LanMessageRequest.CHATPATH)) {
			return false;
		}

		return true;
	}

	public void handle() throws Exception {
		byte body[] = this.requestThread.getClientContent();

		String weAreHeader = this.requestThread.getHeader(
				RequestThread.X_P300_WE_ARE, null);

		if (weAreHeader == null) {
			D.out("LanMessageRequest: No ID header from "
					+ this.requestThread.getRemoteIP());
		} else if (body == null || body.length == 0) {
			D.out("LanMessageRequest: No message XML from "
					+ this.requestThread.getRemoteIP());
		} else {
			Host from = MainDialog.hostMap.get(weAreHeader);
			if (from == null) {
				D.out("LanMessageRequest: No local host with IP "
						+ this.requestThread.getRemoteIP());
			} else {
				if (!from.hasIp(this.requestThread.getRemoteIP())) {
					D
							.out("LanMessageRequest: Spoof? Host with given ID has not IP "
									+ this.requestThread.getRemoteIP());
				} else {
					Document d = de.guruz.p300.utils.DOMUtils
							.documentFromByteArray(body);

					if (d != null) {
						handleMessage(from, d);
					} else {
						D.out("LanMessageRequest: No valid message XML from "
								+ this.requestThread.getRemoteIP());
					}
				}
			}
		}

		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpContentLength(0);
		this.requestThread.httpContents();
		this.requestThread.flush();
		this.requestThread.close();

	}

	private void handleMessage(Host from, Document d) {

		try {
			NodeList nl = d.getElementsByTagName("text");

			if (nl != null && nl.getLength() > 0) {
				Node n = nl.item(0);
				if (n.getParentNode().getLocalName().equals("message-v1")
						&& n.getParentNode().getParentNode().getLocalName().equals("p300")) {

					String t = n.getTextContent();
					
					

					Message m = new Message(from, null, t);

					if (MainDialog.getInstance() != null) {
						MainDialog.getInstance().lanMessageRouter.route(m);
					} else {
						D.out("LanMessageRequest: Received chat message: " + t);
					}
				} else {
					D.out("LanMessageRequest: Received unknown chat message");
				}
			} else {
				D
						.out("LanMessageRequest: Received unknown chat message without text");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
