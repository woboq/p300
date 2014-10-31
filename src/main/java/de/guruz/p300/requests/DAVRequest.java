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
package de.guruz.p300.requests;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.shares.Share;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.shares.SharedEntity;
import de.guruz.p300.utils.HTTP;
import de.guruz.p300.utils.Mime;

/**
 * A request via WebDAV
 * SEARCH: http://greenbytes.de/tech/webdav/draft-reschke-webdav-search-latest.html
 * @author guruz
 *
 */
public class DAVRequest extends Request {
	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (rt == HTTPVerb.PROPFIND) {
			return (reqpath.startsWith("/shares") || reqpath.equals("/"));
		} else {
			return false;
		}
	}

	@Override
	public void handle() throws Exception {
		if ((this.requestThread.verb == HTTPVerb.PROPFIND)
				&& this.requestThread.path.equals("/")) {
			this.handleDAVIndex();
		} else if ((this.requestThread.verb == HTTPVerb.PROPFIND)
				&& (this.requestThread.path.equals("/shares") || this.requestThread.path
						.equals("/shares/"))) {
			// check for propfind here if it is for /shares
			this.handleDAVShareIndex();
		} else if (this.requestThread.verb == HTTPVerb.PROPFIND) {
			SharedEntity sharedEntity = SharedEntity
					.requestPathToSharedEntity(this.requestThread.path);

			String depth = this.requestThread.getHeader("Depth", "0");
			
			this.handleDAVPropfind(sharedEntity, depth);

		} else {
			// should not happen
			this.requestThread.close();
		}

	}

	protected void handleDAVIndex() throws Exception {
		this.printDAVPropfindHeader();
		this.printDAVPropfindElement(SharedEntity.getRootDirectory());
		this.printDAVPropfindElement(SharedEntity.getSharesDirectory());
		this.printDAVPropfindFooter();
	}

	protected void handleDAVShareIndex() throws Exception {
		this.printDAVPropfindHeader();
		this.printDAVPropfindElement(SharedEntity.getSharesDirectory());

		Share[] shares = ShareManager.instance().getShares();
		for (Share element : shares) {
			SharedEntity entity = SharedEntity.getEntityFromShare(element);
			this.printDAVPropfindElement (entity);
		}
		

		this.printDAVPropfindFooter();

	}

	protected void handleDAVPropfind(SharedEntity sharedEntity, String depth)
			throws Exception {
		
		
		if ((sharedEntity == null) || !sharedEntity.isShareable()) {
			this.requestThread.close(400, "Not existing or not readable");
			return;
		}

		// System.out.println ("handleDAVPropfind for " + file.getName());

		Document d = de.guruz.p300.utils.DOMUtils
				.documentFromByteArray(this.requestThread.getClientContent());

		if (d == null) {
			// special case from the RFC
			this.handleDAVPropfindAllprop(sharedEntity, depth);
			return;
		}

		d.setXmlStandalone(true);
		d.setStrictErrorChecking(false);

		DOMConfiguration dc = d.getDomConfig();
		if (dc.canSetParameter("canonical-form", Boolean.TRUE)) {
			dc.setParameter("canonical-form", Boolean.TRUE);
		}
		if (dc.canSetParameter("validate", Boolean.FALSE)) {
			dc.setParameter("validate", Boolean.FALSE);
		}
		if (dc.canSetParameter("namespaces", Boolean.TRUE)) {
			dc.setParameter("namespaces", Boolean.TRUE);
		}
		if (dc.canSetParameter("namespace-declarations", Boolean.TRUE)) {
			dc.setParameter("namespace-declarations", Boolean.TRUE);
		}
		d.normalizeDocument();
		d.normalize();

		Node first = d.getElementsByTagNameNS("DAV:", "propfind").item(0);

		if (first != null) {
			/*
			 * First try to handle prop and allprop, doing the same things:
			 */
			first = d.getElementsByTagNameNS("DAV:", "prop").item(0);
			// we also want allprop
			if (first == null) {
				first = d.getElementsByTagNameNS("DAV:", "allprop").item(0);
			}

			if (first != null) {
				this.handleDAVPropfindAllprop(sharedEntity, depth);
				return;
			}

			/*
			 * It was not found, try to find propname tag
			 */
			first = d.getElementsByTagNameNS("DAV:", "propname").item(0);

			if (first != null) {
				this.handleDAVPropfindSupportedProps(sharedEntity);
			}
		}

		this.requestThread.close();

	}

	protected void handleDAVPropfindSupportedProps(SharedEntity sharedEntity)
			throws Exception {
		this.printDAVPropfindHeader();
		this.requestThread.write("<DAV:response>");

		this.requestThread.write("<DAV:href>");
		this.requestThread.write(this.getURLForDAV(sharedEntity));
		this.requestThread.write("</DAV:href>");

		this.requestThread.write("<DAV:propstat>");

		this.requestThread.write("<DAV:prop>");

		this.requestThread.write("<DAV:resourcetype/>");
		this.requestThread.write("<DAV:getcontenttype/>"); //
		this.requestThread.write("<DAV:getcontentlength/>");
		this.requestThread.write("<DAV:getlastmodified/>");

		this.requestThread.write("</DAV:prop>");
		this.requestThread.write("<DAV:status>HTTP/1.1 200 OK</DAV:status>");
		this.requestThread.write("</DAV:propstat>");
		this.requestThread.write("</DAV:response>");

		this.printDAVPropfindFooter();

	}

	protected void handleDAVPropfindAllprop(SharedEntity sharedEntity,
			String depth) throws Exception {
		// we are a bit of an asshole here, we return all
		// properties no matter what was requested anyway.
		// FIXME?
		// maybe this needs may too much time to fix

		this.printDAVPropfindHeader();

		// System.out.println("Depth " + depth);

		if (depth.equals("1") && sharedEntity.isDirectory()) {
			// current and subelements
			this.printDAVPropfindElement(sharedEntity);

			
			SharedEntity sharedEntities[] = sharedEntity.getDirectorySubEntities();

			for (SharedEntity element : sharedEntities) {
				this.printDAVPropfindElement(element);
			}
			
			sharedEntities = sharedEntity.getFileSubEntities();

			for (SharedEntity element : sharedEntities) {
				this.printDAVPropfindElement(element);
			}

		} else if (depth.equals("0") || !sharedEntity.isDirectory()) {
			// only current
			this.printDAVPropfindElement(sharedEntity);
		} else {
			System.err.println (depth + " " + sharedEntity.getRequestedPath());
		}

		this.printDAVPropfindFooter();

	}

	protected void printDAVPropfindFooter() throws Exception {
		this.requestThread.write("</DAV:multistatus>\n");

		this.requestThread.close();
	}

	protected void printDAVPropfindHeader() throws Exception {
		this.requestThread.httpStatus(207, "Multi-Status");

		this.requestThread.httpHeadersForDAV();
		this.requestThread.httpContentType("text/xml", "utf-8");
		this.requestThread.httpContents();
		// requestThread.httpContents();

		this.requestThread.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");

		this.requestThread
				.write("<DAV:multistatus xmlns=\"DAV:\" xmlns:DAV=\"DAV:\">\n");

	}
	
	protected String getURLForDAV (SharedEntity sharedEntity) {
		String host = this.requestThread.getHeader("Host", null);
		String reqpath = sharedEntity.getRequestedPath();

		if (host == null) {
			host = reqpath;
		} else {
			host = "http://" + host + reqpath;
		}
		
		if (sharedEntity.isDirectory() && !reqpath.endsWith("/")) {
			host = host + '/';
		}
		
		return host;
	}
	
	protected void printDAVPropfindElement(SharedEntity sharedEntity) throws Exception {
		// reqpath alrady ends and begins with / :)

		if (sharedEntity.isShareable() == false) {
			return;
		}
		

		String host = this.getURLForDAV (sharedEntity);

		// System.out.println(host);

		this.requestThread.write("<DAV:response>\n");

		this.requestThread.write(" <DAV:href>");
		this.requestThread.write(host);
		this.requestThread.write("</DAV:href>\n");

		this.requestThread.write(" <DAV:propstat>\n");

		this.requestThread.write("  <DAV:prop>\n");

		if (sharedEntity.isDirectory()) {
			this.requestThread
					.write("   <DAV:resourcetype><DAV:collection/></DAV:resourcetype>\n");
			
			this.requestThread.write("   <DAV:getcontenttype>");
			this.requestThread.write("x-directory/webdav");
			this.requestThread.write("</DAV:getcontenttype>\n");
			
			// MS needs this?
			//requestThread.write("   <DAV:iscollection>1</DAV:iscollection>\n");
			
		} else {
			this.requestThread.write("   <DAV:resourcetype/>\n");
			
			this.requestThread.write("   <DAV:getcontenttype>");
			this.requestThread.write(Mime.getMIMEType(sharedEntity.getPhysicalFileObject()));
			this.requestThread.write("</DAV:getcontenttype>\n");

			// MS needs this?
			//requestThread.write("   <DAV:iscollection>0</DAV:iscollection>\n");
			
			// makes only sense for a file :)
			this.requestThread.write("   <DAV:getcontentlength>");
			this.requestThread.write(Long.toString(sharedEntity.getFileSize()));
			this.requestThread.write("</DAV:getcontentlength>\n");
		}

	
		this.requestThread.write("   <DAV:getlastmodified>");
		long t = sharedEntity.getLastModified();
		this.requestThread.write(HTTP.getHTTPDate(t));
		this.requestThread.write("</DAV:getlastmodified>\n");

		// FIXME? This is not possible with java-only. How can we use the JNI stat function? man 3 stat
		/*
		requestThread.write("<DAV:creationdate>");
		 t = f.lastModified();
		requestThread.write(HTTP.getHTTPDate(t));
		requestThread.write("</DAV:creationdate>\n");
		*/
		
		this.requestThread.write("  </DAV:prop>\n");

		this.requestThread.write("  <DAV:status>HTTP/1.1 200 OK</DAV:status>\n");

		this.requestThread.write(" </DAV:propstat>\n");

		this.requestThread.write("</DAV:response>\n");

	}
	
}
