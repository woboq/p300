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
package de.guruz.p300.downloader;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.downloader.directories.DownloadDirectory;
import de.guruz.p300.downloader.files.DownloadFile;
import de.guruz.p300.hosts.HostLocation;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.FileUtils;

public class DownloadManagerMaintenance extends Object {

	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	DownloadManager manager;

	public DownloadManagerMaintenance(DownloadManager dm) {
		super();
		manager = dm;
	}

	protected void checkInformationFiles(List<DownloadFile> downloadFiles,
			List<DownloadDirectory> downloadDirectories) {
		// gehe durch das verzeichnis mit den download information files
		// durch und pruefe ob jede ein DownloadFile objekt hat. Lege
		// ein
		// solches objekt an
		// falls notwendig
		File informationDir = new File(manager.getInformationDir());
		File[] informationFiles = informationDir.listFiles();

		// System.out.println ("Length of " + informationDir + " is " +
		// informationFiles.length);

		if (informationFiles != null) {
			for (int i = 0; i < informationFiles.length; i++) {
				boolean haveIt = false;

				// check DownloadFile objects
				for (DownloadEntity df : downloadFiles) {
					if (df.getInformationFile().equals(informationFiles[i]))
						haveIt = true;
				}

				// check DownloadFile objects
				for (DownloadEntity dd : downloadDirectories) {
					if (dd.getInformationFile().equals(informationFiles[i]))
						haveIt = true;
				}

				if (!haveIt) {
					// System.out.println ("Adding " + informationFiles[i]);

					try {
						manager.startDownload(informationFiles[i]);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				} else {
					// System.out.println ("Already having " +
					// informationFiles[i]);
				}
			}
		}
	}

	protected void mergeAll(List<? extends DownloadEntity> l) {
		// gehe jedes DownloadFile objekt durch und mache merge ()
		// dies sorgt fuer ein periodisches laden der informationen aus
		// der information file
		// und fuer ein neues schreiben geaendeter informationen
		for (DownloadEntity de : l) {
			de.merge();
		}
	}

	protected void removeFinished(List<? extends DownloadEntity> l) {
		// gehe nochmal durch und entferne alles was finished ist und
		// man
		// entfernen kann aus unserer liste. die download information
		// file wurde
		// dann schon gemoved
		Iterator<DownloadEntity> it = (Iterator<DownloadEntity>) l.iterator();
		while (it.hasNext()) {
			DownloadEntity de = it.next();
			if (de.getState() == DownloadState.CAN_REMOVE_DOWNLOAD) {
				it.remove();
				
				MainDialog.getInstance().downloadsPanel.addFinishedDownload (de);
			}
		}
	}

	protected void tryToStart(List<? extends DownloadEntity> l) {
		// probier zu laden
		for (DownloadEntity de : l) {
			de.startLoadingIfPossible();
		}
	}

	Runnable maintainanceRunnable = new Runnable() {
		public void run() {
			// D.out("Maintenance: Waiting for lock");
			synchronized (manager) {
				////D.out("Maintenance: Doing");

				checkInformationFiles(manager.downloadFiles,
						manager.downloadDirectories);

				mergeAll(manager.downloadFiles);
				mergeAll(manager.downloadDirectories);

				removeFinished(manager.downloadFiles);
				removeFinished(manager.downloadDirectories);

				// manager.updateDownloadsTableModel();

				manager.invalidateUi();

				// sortiere nach Prozentzahl der Fertigstellung
				// damit wir spaeter die unfertigen zuerst fertigmachen?
				// FIXME


			}
		}
	};

	/**
	 * Compares its two arguments for order. Returns a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 */
	Comparator<DownloadFile> downloadPercentageComparator = new Comparator<DownloadFile>() {
		public int compare(DownloadFile o1, DownloadFile o2) {
			int p1 = ((DownloadFile) o1).getPercentageDone();
			int p2 = ((DownloadFile) o2).getPercentageDone();

			if (p1 < p2)
				return -1;
			else if (p1 > p2)
				return 1;
			else
				return 0;

		}
	};

	Runnable startAllDownloadsRunnable = new Runnable() {
		public void run() {
			// D.out("Starting all downloads: Waiting for lock");
			synchronized (manager) {
				 //D.out("Starting all downloads: Doing");
				 
				// manager.debugOutputSlots ();

				// first sort so we start the
				Collections.sort(manager.downloadFiles, Collections
						.reverseOrder(downloadPercentageComparator));

				tryToStart(manager.downloadFiles);
				tryToStart(manager.downloadDirectories);
			}
		}
	};

	Runnable updateUiRunnable = new Runnable() {
		public void run() {
			manager.updateDownloadsTableModel();
		}
	};

	Runnable cleanOldStateFiles = new Runnable() {
		public void run() {

			// clean old finished information files
			File finishedInformationDir = new File(manager
					.getFinishedInformationDir());
			File finishedInformationFiles[] = finishedInformationDir
					.listFiles();
			for (int i = 0; i < finishedInformationFiles.length; i++) {
				File finishedInformationFile = finishedInformationFiles[i];

				if (finishedInformationFile.lastModified() + 1000 * 60 * 60
						* 24 * 14 < System.currentTimeMillis()) {
					// older than 2 weeks
					D.out(finishedInformationFile
							+ " is older than 2 weeks: deleting");
					finishedInformationFile.delete();
					finishedInformationFile.deleteOnExit();
				}
			}

			// clean old incomplete files which have no information file
			File downloadInformationDir = new File(manager.getInformationDir());
			File incompleteDir = new File(manager.getIncompleteDir());
			File incompleteFiles[] = incompleteDir.listFiles();
			for (int i = 0; i < incompleteFiles.length; i++) {
				File incompleteFile = incompleteFiles[i];

				if (incompleteFile.lastModified() + 1000 * 60 * 60 * 24 * 14 < System
						.currentTimeMillis()) {
					// older than 2 weeks
					// D.out(incompleteFile + " is older than 2 weeks:
					// deleting");
					// finishedInformationFile.delete();
					// finishedInformationFile.deleteOnExit();

					String name = incompleteFile.getName();

					File associatedInformationFile = new File(
							downloadInformationDir, name);

					if (!associatedInformationFile.exists()) {
						D
								.out(incompleteFile
										+ " is older than 2 weeks and has no associated information file: deleting");
						incompleteFile.delete();
						incompleteFile.deleteOnExit();
					}
				}
			}

		}
	};

	public void init() {
		// start after 0 seconds every 60 seconds
		executor.scheduleWithFixedDelay(maintainanceRunnable, 0, 60,
				TimeUnit.SECONDS);
		
		executor.scheduleWithFixedDelay(startAllDownloadsRunnable, 1, 60,
				TimeUnit.SECONDS);

		// update UI every 5 secs
		executor.scheduleWithFixedDelay(updateUiRunnable, 0, 5,
				TimeUnit.SECONDS);

		// clean downloader state files after 10m every 5h
		executor.scheduleWithFixedDelay(cleanOldStateFiles, 10 * 60,
				60 * 60 * 5, TimeUnit.SECONDS);
	}

	public void startAllDownloads() {
		executor.schedule(startAllDownloadsRunnable, 0, TimeUnit.SECONDS);
	}

	public void downloadDirectory(final List<RemoteEntity> entities,
			final List<HostLocation> hls) {
		Runnable r = new Runnable() {

			public void run() {
				// D.out("Adding from new dir");

				for (RemoteEntity re : entities) {
					if (re instanceof RemoteFile) {
						// System.out.println("Adding file " + re.getPath());
						manager.startDownload(hls, (RemoteFile) re, re
								.getSize(), null);
					} else if (re instanceof RemoteDir) {
						// System.out.println("Adding dir " + re.getPath());
						manager.startDownload(hls, (RemoteDir) re);
					}
				}

				// manager.updateDownloadsTableModel();
				manager.invalidateUi();

				// call this because there may have been downloads waiting for a
				// slot
				startAllDownloads();
			}
		};

		executor.schedule(r, 0, TimeUnit.SECONDS);

	}

	public void moveFile(final File from, final File to) {
		Runnable r = new Runnable() {
			public void run() {
				FileUtils.renameTo(from, to);
			}

		};

		executor.schedule(r, 0, TimeUnit.SECONDS);
	}

}