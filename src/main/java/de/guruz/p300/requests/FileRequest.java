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

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.logging.D;
import de.guruz.p300.logging.HTTPUploadLog;
import de.guruz.p300.shares.SharedEntity;
import de.guruz.p300.utils.FileUtils;
import de.guruz.p300.utils.HTTP;
import de.guruz.p300.utils.HTTPRange;
import de.guruz.p300.utils.HumanReadableSize;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.MinMax;
import de.guruz.p300.utils.URL;
import de.guruz.p300.utils.XML;

/**
 * A file upload request
 * @author guruz
 *
 */
public class FileRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if ((rt != HTTPVerb.GET) && (rt != HTTPVerb.POST) && (rt != HTTPVerb.HEAD)) {
			return false;
		}

		return (reqpath.startsWith("/shares") || reqpath.equals("/"));
	}

	@Override
	public void handle() throws Exception {
		// System.out.println("thread.path = " + thread.path);

		if (this.requestThread.path.equals("/shares/") 
				|| this.requestThread.path.equals("/shares")) {
			this.requestThread.close(302, "Fwd", "/");
			return;
		}
		
		SharedEntity sharedEntity = SharedEntity
				.requestPathToSharedEntity(this.requestThread.path);

		if ((sharedEntity == null) || !sharedEntity.isShareable()) {
			this.requestThread.close(400, "Not existing or not readable");
			return;
		}

		if (sharedEntity.isDirectory()) {
			this.handleDirlistingRequest(sharedEntity);
		} else if (sharedEntity.isFile()) {
			this.handleFileDownloadRequest(sharedEntity);
		} else {
			this.requestThread.close(403, "Woops");
			return;
		}
	}

	// eigene zugewiesene quota
	protected int currentWriteQuota;

	public void setCurrentQuota(int q) {
		this.currentWriteQuota = q;
	}

	// ueber-quota von anderen threads
	protected int currentExcessiveQuota;

	public void setCurrentExcessiveQuota(int q) {
		this.currentExcessiveQuota = q;
	}

	protected int quotaUsed;

	public int getQuotaUsed() {
		return this.quotaUsed;
	}

	public int getQuotaNotUsed() {
		return this.currentWriteQuota - this.quotaUsed;
	}

	public boolean usedAllQuota() {
		return (!(this.getQuotaNotUsed() > 0));
	}

	protected boolean noLimit;

	public void setNoLimit(boolean b) {
		this.noLimit = b;
	}

	protected synchronized void handleFileDownloadRequest(
			SharedEntity sharedEntity) throws Exception {

		// System.out.println("File download request for " +
		// file.getAbsolutePath() + " with " + file.length());

		// System.err.println(">fr.handleFileDownloadRequest: " + file);

		// propfind?

		long fileSize = sharedEntity.getFileSize();

		String rangeHeader = this.requestThread.getHeader("Range", null);
		HTTPRange range = HTTP.parseRangeHeader(rangeHeader, fileSize);
		boolean doRange = range.length != fileSize;

		// System.out.print("begin: " + range.from);
		// System.out.print("end: " + range.to);
		// System.out.print("length: " + range.length);

		if (range.length == 0) {
			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpContentLength(0);
			this.requestThread.httpHeader("Last-modified", HTTP
					.getHTTPDate(sharedEntity.getLastModified()));
			this.requestThread.httpContents();

			this.requestThread.close();
			return;
		}

		this.requestThread.doNotLog();

		if (!doRange) {
			this.requestThread.httpStatus(200, "OK");
			this.requestThread.httpContentLength(fileSize);
			this.requestThread.httpHeader("Last-modified", HTTP
					.getHTTPDate(sharedEntity.getLastModified()));
			this.requestThread.httpContents();

			HTTPUploadLog.out(this.requestThread.getRemoteIP(),
					this.requestThread.getRequestLine(), 200, fileSize);

		} else {
			// System.out.println("Range from " + range.from + " to " + range.to
			// + " (size " + range.length + \");

			this.requestThread.httpStatus(206, "Partial");
			this.requestThread.httpHeader("Last-modified", HTTP
					.getHTTPDate(sharedEntity.getLastModified()));
			this.requestThread.httpContentLength(range.length);
			String rangeString = "bytes " + range.from + '-' + range.to + '/'
					+ fileSize;

			// System.out.println ("Writing as range: " + rangeString);
			this.requestThread.httpHeader("Content-Range", rangeString);
			this.requestThread.httpContents();

			HTTPUploadLog.out(this.requestThread.getRemoteIP(),
					this.requestThread.getRequestLine(), 206, range.length);
		}

		// Only a HEAD request, we can end this now :)
		if (this.requestThread.isHeadRequest()) {
			this.requestThread.close();
			// System.err.println("<fr.handleFileDownloadRequest");
			return;
		}

		BufferedInputStream fileInput = new BufferedInputStream(
				new FileInputStream(sharedEntity.getPhysicalFileObject()));

		// set the TOS flag
		try {
			byte TOS = Configuration.instance().getTOSFlag();
			this.requestThread.setTrafficClass(TOS);
		} catch (Exception e) {
		}

		// change our priority. i suppose this does not have any measurable
		// effect, but we make sure
		try {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		} catch (Exception e) {
		}

		// only a range request? skip the beginning
		if (doRange) {
			fileInput.skip(range.from);
		}

		long toBeWritten = range.length;
		// 256 KB
		byte[] buffer = new byte[1024 * 256];
		long bytesRead;

		// System.out.println(file.getAbsolutePath() + ": " + "incAcUpCo");

		MainDialog.listenThread.increaseActiveUploadCount();

		try {
			MainDialog.bandwidthThread.register(this);
			while (toBeWritten > 0) {

				if (this.noLimit || this.isUnlimitedThread()) {
					// no limit
					long thisWrite = MinMax.min(toBeWritten, buffer.length);

					bytesRead = fileInput.read(buffer, 0, (int) thisWrite);

					if (bytesRead != thisWrite) {
						// throw new Exception("Not enough read from file");
						// System.out.println ("this write was adjusted");
						thisWrite = bytesRead;
					}

					try {
						this.requestThread.write(buffer, (int) thisWrite);
						this.requestThread.haveWritten(thisWrite);
					} catch (Exception e) {
						this.requestThread.close();
						// System.err.println("<fr.handleFileDownloadRequest");
						return;
					}

				} else {
					// mit limit

					// wir waiten nur wenn wir ein limit haben
					synchronized (MainDialog.bandwidthThread) {
						// System.out.println("waiting for quota");
						MainDialog.bandwidthThread.wait();

					}
					
					int quota = this.currentWriteQuota + this.currentExcessiveQuota;

					if (quota == 0) {
						// System.out.println("end waiting for quota = " +
						// quota);
						continue;
						// } else {
						// System.out.println("end waiting for quota, GOT QUOTA
						// = " + quota);
					}

					// minimum of what has to be written and quota
					long thisWrite = MinMax.min(MinMax.min(toBeWritten, quota),
							buffer.length);
					// System.out.println("thisWrite = " + thisWrite);
					// System.out.println("quota = " + quota);
					// System.out.println("buffer.length = " + buffer.length);

					// 

					if (thisWrite == 0) {
						continue;
					}

					bytesRead = fileInput.read(buffer, 0, (int) thisWrite);

					if (bytesRead != thisWrite) {
						// throw new Exception("Not enough read from file");
						// System.out.println ("this write was adjusted");
						thisWrite = bytesRead;
					}
					// write to out here
					try {
						this.requestThread.write(buffer, (int) thisWrite);
						this.requestThread.haveWritten(thisWrite);
						this.requestThread.flush();
					} catch (Exception e) {
						this.requestThread.close();
						// System.err.println("<fr.handleFileDownloadRequest");
						return;
					}

					this.quotaUsed = (int) MinMax.min(quota - thisWrite,
							this.currentWriteQuota);
					toBeWritten = toBeWritten - thisWrite;

				}
			}
		} finally {
			MainDialog.listenThread.decreaseActiveUploadCount();
			MainDialog.bandwidthThread.unregister(this);
		}

		fileInput.close();
		FileUtils.gcHack(); // need this so file gets closed faster by Sun JVM


		// outStream.flush();
		this.requestThread.close();

		// System.err.println("<fr.handleFileDownloadRequest");
	}

	protected void handleDirlistingRequest(SharedEntity sharedEntity)
			throws Exception {

		// System.out.println("Dirlisting request for " + dir.getName());
		String reqpath = this.requestThread.path;

		// Redirect here, we want the client to have a proper request
		if (!reqpath.endsWith("/")) {
			reqpath = reqpath.concat("/");

			// requestThread.close(302, "OK", reqpath);
			// return;
		}

		this.requestThread.httpStatus(200, "OK");

		// write out html code
		this.requestThread.httpContentType("text/html", Configuration.getDefaultEncoding());
		this.requestThread.httpContents();

		Layouter layouter = new Layouter(this.requestThread);
		layouter.setOpenedShare(sharedEntity.getShareName());
		layouter.replaceBasicStuff();
		layouter.replaceTitle(this.requestThread.getLocalDisplay() + " - "
				+ URL.decode(reqpath));

		this.requestThread.write(layouter.getBeforeMainDiv());

		this.requestThread.write("<pre>");

		this.requestThread.write(IconChooser.fileNameToHTMLImageTag("../"));
		this.requestThread.write("<a href=\"../\">../</a>\n");

		int fileCount = 0;
		fileCount = fileCount
				+ this.printDirlisting(sharedEntity.getDirectorySubEntities());
		fileCount = fileCount
				+ this.printDirlisting(sharedEntity.getFileSubEntities());

		this.requestThread.write("\n<br>\n");

		String fileCountHTML = FileRequest.getFileCountHTML(fileCount);
		this.requestThread.write("<p>" + fileCountHTML + "</p></pre>");

		this.requestThread.write(layouter.getAfterMainDiv());

		this.requestThread.flush();
		this.requestThread.close();

	}

	public int printDirlisting(SharedEntity sharedEntities[]) throws Exception {
		int fileCount = 0;

		for (SharedEntity sharedEntity : sharedEntities) {
			//D.out (sharedEntity.getPhysicalFileObject().getAbsolutePath());
			//D.out (" exists = " + sharedEntity.getPhysicalFileObject().exists());
			//D.out (" canRead = " + sharedEntity.getPhysicalFileObject().canRead());
			//D.out (" unshareable = " + Configuration.instance().isUnshareableFile(
			//		sharedEntity.getPhysicalFileObject().getName()));
			if (!sharedEntity.isShareable()) {
				continue;
			}

			String fileFullName = sharedEntity.getRequestedPath();
			String fileShortName = sharedEntity.getShortName();

			if (sharedEntity.isDirectory()) {
				fileFullName = fileFullName + '/';
				fileShortName = fileShortName + '/';
			}

			this.requestThread.write(IconChooser
					.fileNameToHTMLImageTag(fileShortName));
			this.requestThread.write("<a ");

			if (!sharedEntity.isDirectory()) {
				this.requestThread.write("title=\""
						+ HumanReadableSize.get(sharedEntity.getFileSize())
						+ "\" ");
			}

			this.requestThread.write("href=\"" + fileFullName + "\">"
					+ XML.encode(fileShortName) + "</a>");

			this.requestThread.write("\n");

			fileCount++;
		}

		return fileCount;

	}

	@Override
	public String toString() {
		return "FileRequest: " + this.requestThread.path;
	}

	public boolean isUnlimitedThread() {
		return MainDialog.getHostAllowanceManager().isIpUnlimited(
				this.requestThread.getRemoteIP());
	}

	public static String getFileCountHTML(int fileCount) {
		if (fileCount == 0) {
			return "<i>0 entries</i>";
		} else if (fileCount == 1) {
			return "<i>" + fileCount + " entry</i>";
		} else {
			return "<i>" + fileCount + " entries</i>";
		}
	}

}
