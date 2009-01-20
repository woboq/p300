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
package de.guruz.p300.downloader.directories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import de.guruz.p300.dirbrowser.DirListing;
import de.guruz.p300.downloader.DownloadEntity;
import de.guruz.p300.downloader.DownloadManager;
import de.guruz.p300.downloader.DownloadSource;
import de.guruz.p300.downloader.DownloadState;
import de.guruz.p300.downloader.FileType;
import de.guruz.p300.hosts.HostLocation;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.FileNameUtils;
import de.guruz.p300.utils.RandomGenerator;

public class DownloadDirectory extends DownloadEntity {

	@Override
	protected void setState(DownloadState state) {
		super.setState(state);
		this.needsSave = true;
	}

	boolean needsSave;

	List<DownloadSource> sources = new ArrayList<DownloadSource>();

	String informationFileName;

	File informationFile;

	RunningWebDavDirectoryDownload runningDownload;

	// boolean informationFileEverLoaded = false;

	public DownloadDirectory(DownloadManager manager,
			String informationFileName, DownloadSource... ds) {
		super(manager);
		this.informationFileName = informationFileName;
		Collections.addAll(this.sources, ds);
		this.needsSave = true;

		if (this.informationFileName == null) {
			this.informationFileName = manager.getNewInformationFileName();
			this.informationFile = new File(this.informationFileName);
		}

		merge();
	}

	public DownloadDirectory(DownloadManager manager, File f) {
		super(manager);
		this.informationFileName = f.getAbsolutePath();
		this.informationFile = new File(this.informationFileName);
		this.needsSave = true;

		merge();
	}

	public File getInformationFile() {
		return this.informationFile;
	}

	// public DownloadSource getSource() {
	// return sources;
	// }

	protected long lastRead = 0;

	protected long lastWrite = 0;

	@Override
	public void merge() {
		//System.out.println ("merge of " + this.informationFileName);
		//System.out.println (this.informationFileName + " state is " + this.getState());
		if (this.getState() == DownloadState.CAN_REMOVE_DOWNLOAD)
			return;

		long now = System.currentTimeMillis();

		try {

			File informationFile = getInformationFile();

			if (informationFile.exists() && informationFile.canRead()
					&& informationFile.length() > 0
					&& lastRead != informationFile.lastModified()) {
				//System.out.println ("reading of " + this.informationFileName);
				Properties props = new Properties();
				FileInputStream fis = new FileInputStream(informationFile);
				props.loadFromXML(fis);
				fis.close();
				
				// load sources and merge them with our list. remove them from
				// file
				Set<Object> entries = props.keySet();
				Iterator<Object> it = entries.iterator();
				while (it.hasNext()) {
					Object k = it.next();

					try {
						String key_string = (String) k;

						if (key_string.startsWith("source.")) {
							String value_string = props.getProperty(key_string);
							DownloadSource s = DownloadSource.fromString(
									value_string, FileType.DIRECTORY);
							
							if (!this.sources.contains(s))
								this.sources.add(s);
							
							it.remove();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				// the state may be fresher, so always get it
				if (this.getState() != DownloadState.FINISHED) {
					// System.out.println ("Dir State is not FINISHED, reloading
					// state");
					String st = props.getProperty("state", "RUNNING");
					this.state = DownloadState.valueOf(st);
				}

				lastRead = informationFile.lastModified();

				// System.out.println("(Re)Loaded dir with state " +
				// this.state);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// save the information file if needed
			if (needsSave || !informationFile.exists()
			/*
			 * || ((informationFile.exists() && lastRead != informationFile
			 * .lastModified()) )
			 */) {
				//System.out.println ("writing of " + this.informationFileName);
				Properties props = new Properties();
				props.setProperty("information_file_version", ""
						+ INFORMATION_FILE_VERSION);
				props.setProperty("file_type", FileType.DIRECTORY.toString());
				props.setProperty("state", this.getState().toString());
				// props.setProperty("source.0", this.getSource().toString());

				for (int i = 0; i < sources.size(); i++) {
					props.setProperty("source." + i, this.sources.get(i)
							.toString());
				}

				FileOutputStream fos = new FileOutputStream(informationFileName);
				props.storeToXML(fos,
						"p300");
				fos.close();

				lastRead = lastWrite = informationFile.lastModified();
				needsSave = false;
				// System.out.println("Saving dir");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// FIXME check if this directory information is finished and can be
		// removed
		if (this.getState() == DownloadState.FINISHED) {
			this.setState(DownloadState.CAN_REMOVE_DOWNLOAD);

			File finishedInformationFile = new File(manager
					.getFinishedInformationDir(), this.informationFile
					.getName());

			// move the information file because we are done
			manager.getMaintainaner().moveFile(this.informationFile, finishedInformationFile);
		}

		updateUiRow();
	}

	@Override
	public void startLoadingIfPossible() {
		synchronized (this) {

			// if there is currently a RunningWebDavDirDownload we exit FIXME
			if (runningDownload != null)
				return;

			// check state
			// if we are not already running or new we exit here, because
			// we may be already removed or paused or whatever
			if (!(this.getState() == DownloadState.RUNNING || this.getState() == DownloadState.NEW))
				return;

			// get a random number and try loading from that source on
			DownloadSource s = getRandomSourceWithFreeSlot();

			if (s == null)
				return;
			
			// download using DavDirLoader
			runningDownload = new RunningWebDavDirectoryDownload(this, s);

			// set state
			setState(DownloadState.RUNNING);

			//D.out("Starting to fetch " + this.toString());
			runningDownload.start();
		}
	}

	private DownloadSource getRandomSourceWithFreeSlot() 
	{
		DownloadSource s = null;
		int n = RandomGenerator.getInt(0, sources.size());
		for (int i = n; i < sources.size(); i++)
		{
			s = sources.get(i);
			if (s.couldRetryNow() && manager.acquireDirectoryDownloadSlot (s.getRemoteHost()))
			{
				break;
			}
			else
			{
				s = null;
			}
		}
		if (s == null)
		{
			// try again the others
			for (int i = 0; i < n; i++)
			{
				s = sources.get(i);
				if (s.couldRetryNow() && manager.acquireDirectoryDownloadSlot (s.getRemoteHost()))
				{
					break;
				}
				else
				{
					s = null;
				}
			}
		}
		
		return s;
		
		
	}

	public String getDisplayFilename() {
		if (sources.size() > 0)
			return this.sources.get(0).getConcatenatedFileName().replace('\\', '/');
		else
			return "(no source)";
	}

	public void finishedProperly(final DirListing dl) {
		synchronized (this) {
			DownloadSource s = runningDownload.getSource();
			manager.releaseDirectoryDownloadSlot(s.getRemoteHost());
			// System.out.println("Finished properly with " +
			// dl.getEntities().size());
			//D.out("Fetched " + this.toString());

			// change state
			this.setState(DownloadState.FINISHED);
			this.merge();

			// create finished dir
			try {
				File f = new File(manager.getFinishedDownloadDir(), FileNameUtils.sanitizeFilepathForLocalOS(s.getConcatenatedFileName()));
				//f = new File (FileNameUtils.sanitizeFilenpathForLocalOS(f));
				
				f.mkdirs();
			} catch (Throwable t) {
				D.out("Could not create output dir for "
						+ this.getDisplayFilename());
			}

			final HostLocation hl = new HostLocation(s.getRemoteHost(), s
					.getRemotePort());

			manager.getMaintainaner().downloadDirectory(dl.getEntities(), getHostLocations ());

			updateUiRow();
			//manager.updateDownloadsTableModel();
			manager.invalidateUi();
		}

	}

	private List<HostLocation> getHostLocations() {
		ArrayList<HostLocation> hls = new ArrayList<HostLocation> ();
		
		for (DownloadSource s : sources)
		{
			hls.add(s.getHostLocation ());
		}
		
		return hls;
	}

	public void finishedWithError(String error) {
		synchronized (this) {
			DownloadSource s = runningDownload.getSource();
			manager.releaseDirectoryDownloadSlot(s.getRemoteHost());

			D.out(this.getDisplayFilename() + ": " + error);

			s
					.noRetryTheNextMillisecs(1000 * 60 * 1 + de.guruz.p300.utils.RandomGenerator
							.getInt(0, 2 * 60 * 1000));
			runningDownload = null;

			// System.out.println("Finished unproperly :( " + error);
		}

	}

	public String getDisplayStatus() {
		synchronized (this) {
			DownloadState s = this.getState();
			if (s == DownloadState.FINISHED
					|| s == DownloadState.CAN_REMOVE_DOWNLOAD)
				return "OK";
			else
				return "Queued";
		}
	}

	protected void updateUiRow() {
		getUIRow().setDisplayName(this.getDisplayFilename());
		getUIRow().setStateString(this.getDisplayStatus());

	}

	public List<DownloadSource> getSources() {
		return sources;
	}

}
