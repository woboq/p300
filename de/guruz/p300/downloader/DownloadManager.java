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
package de.guruz.p300.downloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.downloader.directories.DownloadDirectory;
import de.guruz.p300.downloader.files.DownloadFile;
import de.guruz.p300.hosts.HostLocation;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.FileNameUtils;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.MapWithSemaphores;

public class DownloadManager {
	List<DownloadFile> downloadFiles;

	List<DownloadDirectory> downloadDirectories;

	private MapWithSemaphores fileDownloadSlots = new MapWithSemaphores(3);

	private MapWithSemaphores directoryDownloadSlots = new MapWithSemaphores(3);
	//private MapWithSemaphores directoryDownloadSlots = fileDownloadSlots;
	
	private String finishedDownloadDir;

	private String finishedInformationDir;

	private String incompleteDownloadDir;

	private String informationDir;

	DownloadManagerMaintenance maintenance;

	ExecutorService downloadExecutor = Executors.newCachedThreadPool();

	// Timer timer = new Timer(true);

	/**
	 * The default constructor when called from p300
	 * 
	 */
	public DownloadManager() {
		this(Configuration.instance().getIncompleteDownloadDir(), Configuration
				.instance().getFinishedDownloadDir(), Configuration.instance()
				.getDownloadInformationDir(), Configuration.instance()
				.getFinishedDownloadInformationDir());
	};

	/**
	 * This constructor is also more explicitely called when we are running the
	 * download manager test
	 * 
	 * @param incompleteDownloadDir
	 * @param finishedDownloadDir
	 * @param informationDir
	 * @param finishedInformationDir
	 */
	public DownloadManager(String incompleteDownloadDir,
			String finishedDownloadDir, String informationDir,
			String finishedInformationDir) {
		this.incompleteDownloadDir = incompleteDownloadDir;
		this.finishedDownloadDir = FileNameUtils
				.replaceVariables(finishedDownloadDir);
		this.informationDir = informationDir;
		this.finishedInformationDir = finishedInformationDir;

		// make sure dirs exist
		de.guruz.p300.utils.DirectoryUtils
				.makeSureDirectoryExists(this.incompleteDownloadDir);
		// de.guruz.p300.utils.DirectoryUtils.makeSureDirectoryExists(this.finishedDownloadDir);
		de.guruz.p300.utils.DirectoryUtils
				.makeSureDirectoryExists(this.informationDir);
		de.guruz.p300.utils.DirectoryUtils
				.makeSureDirectoryExists(this.finishedInformationDir);

		downloadFiles = new ArrayList<DownloadFile>();

		downloadDirectories = new ArrayList<DownloadDirectory>();

		maintenance = new DownloadManagerMaintenance(this);
	}

	private DownloadDirectory getDownloadDirByLocation(HostLocation hl,
			RemoteDir rd) {
		synchronized (this) {
			for (DownloadDirectory dd : getDownloadDirectories()) {
				for (DownloadSource ds : dd.getSources()) {
					if (ds.equalsParams(rd, hl))
						return dd;
				}
			}
		}

		return null;
	}

	private List<DownloadDirectory> getDownloadDirectories() {
		return this.downloadDirectories;
	}

	/**
	 * Checks if we have in our list of downloads already a file from that host
	 * 
	 * @param hl
	 * @param rf
	 * @return
	 */
	public DownloadFile getDownloadFileByLocation(HostLocation hl, RemoteFile rf) {
		synchronized (this) {
			for (DownloadFile df : downloadFiles) {
				for (DownloadSource ds : df.getSources()) {
					if (ds.equalsParams(rf, hl))
						return df;
				}
			}
		}

		return null;
	}

	protected List<DownloadFile> getDownloadFiles() {
		return downloadFiles;
	}

	public String getFinishedDownloadDir() {
		return finishedDownloadDir;
	}

	public String getFinishedInformationDir() {
		return finishedInformationDir;
	}

	public String getIncompleteDir() {
		return incompleteDownloadDir;
	}



	protected String getInformationDir() {
		return informationDir;
	}

	/**
	 * Creates a new information filename for download information. the name for
	 * the incomplete file is based on that, too
	 * 
	 * @return
	 */
	public String getNewInformationFileName() {
		File f = new File(this.getInformationDir(),
				de.guruz.p300.utils.RandomGenerator.string());
		return f.getAbsolutePath();
	}

	public int getNumberOfDownloads() {
		return downloadFiles.size() + downloadDirectories.size();
	}

	public void releaseFileDownloadSlot(String k) {
		fileDownloadSlots.releaseSlot(k);
	}

	public boolean acquireFileDownloadSlot(String k) {
		return fileDownloadSlots.acquireSlot(k);
	}

	public void releaseDirectoryDownloadSlot(String k) {
		directoryDownloadSlots.releaseSlot(k);
	}

	public boolean acquireDirectoryDownloadSlot(String k) {
		return directoryDownloadSlots.acquireSlot(k);
	}

	public void start() {
		maintenance.init();

	}

	/**
	 * This is called by the maintainanceRunnable for information files we had
	 * in the download information dir
	 * 
	 * @param informationFile
	 */
	public void startDownload(File informationFile) {
		synchronized (this) {
			//System.out.println ("Entering startDownload");
			// this functions needs to check if the file
			// is a information file for a file or directory

			if (!DownloadEntity.isValidInformationFile(informationFile)) {
				D.out("Unknown download information format: "
						+ informationFile.getPath());
				return;
			}
			//System.out.println ("startDownload: checked validity");
			FileType ft = DownloadEntity.getType(informationFile);
			if (ft == FileType.DIRECTORY) {
				//System.out.println ("startDownload: is dir");
				DownloadDirectory dd = new DownloadDirectory(this,
						informationFile);
				//System.out.println ("startDownload: adding dir");
				this.getDownloadDirectories().add(dd);
				//this.updateDownloadsTableModel();
				
			} else if (ft == FileType.FILE) {
				//System.out.println ("startDownload: is file");
				DownloadFile df = new DownloadFile(this, -1, informationFile
						.getAbsolutePath(), null, null);
				this.getDownloadFiles().add(df);
				//System.out.println ("startDownload: adding f");
				//this.updateDownloadsTableModel();
			} else {
				D.out("Unknown download information format: "
						+ informationFile.getPath());
			}
			
			//System.out.println ("startDownload: invalidating ui:");
			invalidateUi();
			
			// this just adds that download file. it will be scheduled for
			// starting later
			// (in the maintainanceRunnable)
			
			//System.out.println ("Exiting startDownload");
		}

	}

	public void startDownload(List<HostLocation> hls, RemoteDir rd) {
		synchronized (this) {
			DownloadDirectory dd = null;

			for (HostLocation hl : hls) {
				dd = getDownloadDirByLocation(hl, rd);

				if (dd != null)
					break;
			}

			if (dd != null) {
				D.out("Already exists");
			} else {
				List<DownloadSource> sources = new ArrayList<DownloadSource>();
				for (HostLocation hl : hls) {
					sources.add(new DownloadSource(rd, hl));
				}

				dd = new DownloadDirectory(this, null, sources
						.toArray(new DownloadSource[0]));

				dd.merge();

				this.getDownloadDirectories().add(dd);
				//this.updateDownloadsTableModel();
				invalidateUi();

				dd.startLoadingIfPossible();
			}
		}
	}

//	public void startDownload(Host h, RemoteDir rd) {
//		synchronized (this) {
//			// check if a download for this already exists
//			startDownload(h.getLocationsAsList(), rd);
//		}
//	}


	/**
	 * Can be called from the tester or the GUI to start a new download
	 * 
	 * @param hl
	 * @param rf
	 * @param fileSize
	 */
	public void startDownload(HostLocation hl, RemoteFile rf, long fileSize) {
		startDownload(Collections.singletonList(hl), rf, fileSize, null);
	}

	/**
	 * Can be called from the tester or the GUI to start a new download
	 * 
	 * @param hl
	 * @param rf
	 * @param fileSize
	 * @param sha1
	 */
	public void startDownload(List<HostLocation> hls, RemoteFile rf, long fileSize,
			String sha1) {
		synchronized (this) {
			// check if a download for this already exists
			DownloadFile df = null;

			for (HostLocation hl : hls) {
				df = getDownloadFileByLocation(hl, rf);

				if (df != null)
					break;
			}
			
			// FIXME: also check by sha1, then add that source to the download

			if (df != null) {
				D.out("Already exists");
			} else {
				DownloadSource ds = new DownloadSource(rf, hls.get(0));
				df = new DownloadFile(this, fileSize, null, null, ds);
				if (hls.size() > 1)
				{
					for (int i = 1; i < hls.size(); i++)
						df.addSource(new DownloadSource (rf, hls.get(i)));
				}

				df.setNeedsSave(true);
				df.merge(true);

				// if (sha1 != null)
				// df.setSha1
				// FIXME

				// go :)
				this.getDownloadFiles().add(df);
				//this.updateDownloadsTableModel();
				invalidateUi();

				df.startLoadingIfPossible();
			}
		}
	}

	public void stop() {
	}

	java.util.concurrent.locks.ReentrantLock updateTableModelLock = new java.util.concurrent.locks.ReentrantLock();

	int c = 0;

	public void updateDownloadsTableModel() {
		if (MainDialog.isHeadless())
			return;
		
		if (!MainDialog.instance.isDownloadsPanelShown ())
			return;
		
		if (!checkUiInvalidated())
			return;
		
		//D.out("Doing Downloader UI update");
		

		// work on a copy of those lists so we do not get any
		// ConcurrentModificationExceptions
		final List<DownloadFile> dFs = (List<DownloadFile>) ((ArrayList) downloadFiles)
				.clone();
		final List<DownloadDirectory> dDs = (List<DownloadDirectory>) ((ArrayList) downloadDirectories)
				.clone();

		Runnable r = new Runnable() {

			public void run() {
				c++;
				//D.out("UI update" + (int) c);
				DownloadsTableModel dtm = DownloadsTableModel.instance();
				dtm.setRowCount(dFs.size() + dDs.size());

				int i = 0;
				for (DownloadFile df : dFs) {
					try {
						dtm.setValueAt(df.getUIRow().getDownloadIcon(), i, 0);
						dtm.setValueAt(df.getUIRow().getDisplayName(), i, 1);
						dtm.setValueAt(df.getUIRow().getStateString(), i, 2);

						i++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				ImageIcon dirIcon = IconChooser.getFolderImageIcon();
				for (DownloadDirectory dd : dDs) {

					dtm.setValueAt(dirIcon, i, 0);
					dtm.setValueAt(dd.getUIRow().getDisplayName(), i, 1);
					dtm.setValueAt(dd.getUIRow().getStateString(), i, 2);

					i++;
				}

			}
		};

		SwingUtilities.invokeLater(r);

	}
	
	
	protected boolean m_uiInvalidated = true;
	
	public void invalidateUi ()
	{
		m_uiInvalidated = true;
	}
	
	public boolean checkUiInvalidated ()
	{
		if (m_uiInvalidated)
		{
			m_uiInvalidated = false;
			return true;
		}
		else
		{
			return false;
		}
	}

	public DownloadManagerMaintenance getMaintainaner() {
		return maintenance;
	}

	public ExecutorService getDownloadExecutor() {
		return downloadExecutor;
	}

	public void startAllDownloads() {
		maintenance.startAllDownloads();

	}

	public void debugOutputSlots() {
				//fileDownloadSlots.debugOutput ();
	}

}
