package de.guruz.p300.davclient;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.utils.URL;

public class DavParsingUtils {
	public static RemoteEntity parseReponseNode(Node reponseNode) {
		Element reponseElement = (Element) reponseNode;


		Element hrefElement = (Element) reponseElement.getElementsByTagNameNS(
				"*", "href").item(0);
		Element propstatElement = (Element) reponseElement
				.getElementsByTagNameNS("*", "propstat").item(0);
		Element propElement = (Element) propstatElement.getElementsByTagNameNS(
				"*", "prop").item(0);

		boolean isDirectory = false;
		try {
			Element resourcetypeElement = (Element) propElement
					.getElementsByTagNameNS("*", "resourcetype").item(0);
			// System.out.println
			// (resourcetypeElement.getFirstChild().getNodeName());

			if (resourcetypeElement.getFirstChild() != null
					&& (resourcetypeElement.getFirstChild().getNodeName()
							.equals("DAV:collection")))
				isDirectory = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		String currentPath = hrefElement.getTextContent();
		if (currentPath.toLowerCase().startsWith("http://"))
			currentPath = URL.getOnlyPath(currentPath);

		RemoteEntity re = null;
		if (isDirectory) {
			re = (new RemoteDir(currentPath));
		} else {
			re = (new RemoteFile(currentPath));

		}

		// the size
		if (!isDirectory) {
			try {
				Element lengthElement = (Element) reponseElement
						.getElementsByTagNameNS("*", "getcontentlength")
						.item(0);

				long size = Long.parseLong(lengthElement.getTextContent());

				re.setSize(size);

			} catch (Exception e) {

			}
		}

		return re;

	}
}
