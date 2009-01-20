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

package de.guruz.p300.threads;

import java.io.File;
import java.io.FileOutputStream;

import de.guruz.p300.Configuration;
import de.guruz.p300.http.ToMemoryDownloader;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.DirectoryUtils;
import de.guruz.p300.utils.FileUtils;
import de.guruz.p300.utils.JarChecker;
import de.guruz.p300.utils.SHA1;
import de.guruz.p300.windowui.actions.NewVersionLoadedAction;

/**
 * This thread downloads the latest p300.jar if possible
 * 
 * @author guruz
 * 
 */
public class UpdaterThread extends Thread {
	int versionToCheckFor = 0;

	protected boolean shouldUpdate() {
		Configuration conf = Configuration.instance();

		// check if we are already running the latestseenrevision
		if (Configuration.getSVNRevision() >= this.versionToCheckFor) {
			return false;
		}

		// check here if not checked in last 24 hours
		if (conf.getLastUpdateRetrieval() + 1000 * 60 * 60 * 24 > System
				.currentTimeMillis()) {
			return false;
		}

		// check here if we already have the jar for that version
		// as .p300/jar/p300/REVISIONNUMBER.jar
		if (UpdaterThread.jarAlreadyExists(this.versionToCheckFor)) {
			return false;
		}

		if (conf.getTryUpdateEveryWeek() == false) {
			return false;
		}

		return true;
	}

	protected boolean doUpdate() {
		D.out("Updater: Trying to download update revision "
				+ this.versionToCheckFor);

		// download the
		// http://p300.eu/releases/REVISIONNUMBER/p300.jar.sha1
		ToMemoryDownloader sha1FileDownloader = new ToMemoryDownloader();
		String realSha1URL = "http://p300.eu/releases/"
				+ this.versionToCheckFor + "/p300.jar.sha1";
		boolean sha1LoadOK = sha1FileDownloader.downloadWithAlternateURL(
				realSha1URL, realSha1URL);

		if (!sha1LoadOK) {
			D.out("Updater: Could not check for update :(");
			return false;
		}

		String claimedSha1 = new String(sha1FileDownloader.results()).trim()
				.toLowerCase();
		// D.out("Updater: SHA1 of latest revision = " + claimedSha1);

		// download the
		// http://p300.eu/releases/REVISIONNUMBER/p300.jar
		// to memory via nyud or from the real location
		ToMemoryDownloader mainJarFileDownloader = new ToMemoryDownloader();
		String mirrorMainJarURL = "http://p300.eu.nyud.net/releases/"
				+ this.versionToCheckFor + "/p300.jar";
		String realMainJarURL = "http://p300.eu/releases/"
				+ this.versionToCheckFor + "/p300.jar";
		boolean mainJarLoaderOK = mainJarFileDownloader
				.downloadWithAlternateURL(mirrorMainJarURL, realMainJarURL);

		if (!mainJarLoaderOK) {
			D.out("Updater: Could not download update :(");
			return false;
		}

		// and check sha1
		String fileSha1 = SHA1.asHexString(mainJarFileDownloader.results())
				.toLowerCase();
		// D.out("Updater: SHA1 of download = " + fileSha1);

		if (!fileSha1.equals(claimedSha1)) {
			D.out("Updater: SHA1 did not match, sorry");
			return false;
		}

		// write the jar to disk
		File f = UpdaterThread.jarFileForRevision(this.versionToCheckFor);

		try {
			File tempF = new File(f.getPath() + ".temp");
			FileOutputStream fos = new FileOutputStream(tempF);
			fos.write(mainJarFileDownloader.results());
			fos.close();

			// check signature
			JarChecker.checkSignature(tempF);
			// FIXME

			FileUtils.renameTo(tempF, f);
		} catch (Exception e) {
			D.out("Updater: Cannot write to " + f.getAbsolutePath() + " :(");
			return false;
		}

		D
				.out("Updater: Latest p300.jar stored. It will be launched next time you start p300");

		sha1FileDownloader = null;

		this.newVersionReady = true;

		// open a special page in the browser
		new NewVersionLoadedAction().actionPerformed(null);

		return true;
	}

	@Override
	public void run() {
		this.setPriority(Thread.MIN_PRIORITY);

		while (true) {
			try {
				// wait a long time :)
				// we get only waken up by the NewVersionNotificationThread
				// anyway
				// also a multicast listen thread may wake us up
				// but it should not, since multicast may for some
				// people (= the balub net) return newer revisions
				// than actually released
				synchronized (this) {
					this.wait();
				}

				if (!this.shouldUpdate()) {
					continue;
				}

				// update the check-time
				Configuration.instance().updateLastUpdateRetrieval();

				this.doUpdate();

				// tell the user that he/she should restart p300
				// FIXME

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	protected static boolean jarAlreadyExists(int r) {
		File f = UpdaterThread.jarFileForRevision(r);
		return (f.exists() && f.canRead());
	}

	protected static File jarFileForRevision(int r) {
		String dir = Configuration.configDirFileName("jar/p300/");
		File dirFile = new File(dir);
		DirectoryUtils.makeSureDirectoryExists(dirFile);
		return new File(dirFile, r + ".jar");
	}

	public void go(int x) {
		synchronized (this) {
			this.versionToCheckFor = x;
			this.notifyAll();
		}
	}

	public boolean testDoUpdate() {
		return this.doUpdate();
	}

	public int getVersionToCheckFor() {
		return this.versionToCheckFor;
	}

	public void setVersionToCheckFor(int versionToCheckFor) {
		this.versionToCheckFor = versionToCheckFor;
	}

	private boolean newVersionReady = false;

	public boolean haveNewVersionReady() {
		return this.newVersionReady;
	}
}
