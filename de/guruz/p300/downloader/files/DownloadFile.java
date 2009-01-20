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
package de.guruz.p300.downloader.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.guruz.p300.downloader.DownloadEntity;
import de.guruz.p300.downloader.DownloadManager;
import de.guruz.p300.downloader.DownloadSource;
import de.guruz.p300.downloader.DownloadState;
import de.guruz.p300.downloader.FileType;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.BitSetUtils;
import de.guruz.p300.utils.FileNameUtils;
import de.guruz.p300.utils.IconChooser;

public class DownloadFile extends DownloadEntity {
	/**
	 * 256 KB
	 * 
	 * FIXME CHANGING THIS WILL CORRUPT THE DOWNLOAD INFORMATION FILE!
	 */
	public static final long DEFAULT_CHUNK_SIZE = 256 * 1024;

	private BitSet alreadyDownloadedChunks;

	private long fileSize = 0;

	// String sha1

	private String informationFileName;

	/**
	 * This is not an absolute filename but more a name with a dir structure
	 * attached for saving later. This means if we load a file
	 * 192.168.0.1:4337/shares/bla/bla.mp3 then this will contain something like
	 * 192.168.0.1_4337/shares/bla/bla.mp3
	 */
	private String outputFileName;

	private File incompleteFile;

	private List<RunningFileDownload> runningDownloads;

	private List<DownloadSource> sources;

	private String incompleteFileName;

	private File informationFile;

	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(); // FIXME

	long lastLoad = 0;

	long lastSave = 0;

	private boolean needsSave = true;

	/**
	 * Constructor for creating a new download
	 * 
	 * @param dm
	 */
	public DownloadFile(DownloadManager dm, long fileSize) {
		super(dm);
		sources = new ArrayList<DownloadSource>();
		runningDownloads = Collections.synchronizedList(new ArrayList<RunningFileDownload>());

		this.fileSize = fileSize;
		
		m_percentageNumberFormat = NumberFormat.getInstance();
		m_percentageNumberFormat.setMaximumFractionDigits(2);
		m_percentageNumberFormat.setMinimumFractionDigits(2);
	}

	/**
	 * This constructor is called when loading a DownloadInformation file
	 * 
	 * @param dm
	 * @param informationFileName
	 * @param ds
	 */
	public DownloadFile(DownloadManager dm, long fileSize,
			String informationFileName, String incompleteFileName,
			DownloadSource ds) {
		this(dm, fileSize);

		// create the information file object
		if (informationFileName != null) {
			this.informationFileName = informationFileName;
		} else {
			this.informationFileName = this.manager.getNewInformationFileName();
		}
		this.informationFile = new File(this.informationFileName);

		if (incompleteFileName != null) {
			this.incompleteFileName = incompleteFileName;
			this.incompleteFile = new File(this.incompleteFileName);
		} else {
			// the incomplete filename is based on the name of the information
			// file
			this.incompleteFile = new File(manager.getIncompleteDir(),
					this.informationFile.getName());
			this.incompleteFileName = incompleteFile.getAbsolutePath();
		}

		if (ds != null)
			this.addSource(ds);

		merge(true);
	}

	public void addSource(DownloadSource s) {

		try {
			rwlock.writeLock().lock();

			if (this.getSources().contains(s))
				return;

			getSources().add(s);

			// System.out.println("having " + getSources().size()
			// + " sources in list");

			if (this.outputFileName == null
					|| this.outputFileName.length() == 0) {
				String filename = s.getConcatenatedFileName();

				this.setOutputFileName(filename);
				// System.out.println("setting output filename to " + filename);
			}
		} finally {
			rwlock.writeLock().unlock();
		}

	}

	public boolean doNotHaveToLoadChunk(int chunkNo) {
		try {
			this.rwlock.readLock().lock();

			if (chunkNo > this.getNumberOfChunks() - 1)
				return true;

			if (getAlreadyDownloadedChunks().get(chunkNo))
				return true;

			List<RunningFileDownload> rds = this.getRunningDownloads();
			for (RunningFileDownload rd : rds) {
				if (rd.getChunkNumber() == chunkNo) {
					return true;
				}
			}
		} finally {
			this.rwlock.readLock().unlock();
		}

		return false;
	}

	private void finishFile() throws Exception {
		File finishedInformationFile = new File(manager
				.getFinishedInformationDir(), this.informationFile.getName());

		// System.out.println("I would here move " + this);
		this.setState(DownloadState.CAN_REMOVE_DOWNLOAD);

		// move the information file because we are done
		manager.getMaintainaner().moveFile(this.informationFile,
				finishedInformationFile);

		// FIXME check the sha1 of the download before moving

		// move downloaded file to download dir
		this.moveFileToDownloadsDir();
	}

	protected BitSet getAlreadyDownloadedChunks() {
		try {
			this.rwlock.readLock().lock();
			return alreadyDownloadedChunks;
		} finally {
			this.rwlock.readLock().unlock();
		}

	}

	protected long getChunkOffset(int chunkNumber) {
		return chunkNumber * DEFAULT_CHUNK_SIZE;
	}

	/**
	 * Chunk number count begins with 0
	 * 
	 * @param chunkNumber
	 * @return
	 */
	protected long getChunkSize(int chunkNumber) {
		try {
			this.rwlock.readLock().lock();
			if (chunkNumber == getNumberOfChunks() - 1) {
				return (long) (this.getFileSize() - this
						.getChunkOffset(chunkNumber));
			} else {
				return DEFAULT_CHUNK_SIZE;
			}
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	public String getDisplayFilename() {
		try {
			this.rwlock.readLock().lock();
			return this.getOutputFileName().replace('\\', '/');
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	protected long getFileSize() {
		try {
			this.rwlock.readLock().lock();
			return fileSize;
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	protected File getIncompleteFile() {
		try {
			this.rwlock.readLock().lock();
			return incompleteFile;
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	public File getInformationFile() {
		try {
			this.rwlock.readLock().lock();
			return informationFile;
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	private int getNumberOfChunks() {
		try {
			this.rwlock.readLock().lock();

			if (this.getFileSize() == 0)
				return 0;

			if (this.getFileSize() % DEFAULT_CHUNK_SIZE == 0)
				return (int) (this.getFileSize() / DEFAULT_CHUNK_SIZE);
			else
				return (int) (this.getFileSize() / DEFAULT_CHUNK_SIZE) + 1;

		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	private int getNumberOfChunksLoaded() {
		try {
			this.rwlock.readLock().lock();
			return getAlreadyDownloadedChunks().cardinality();
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	protected String getOutputFileName() {
		try {
			this.rwlock.readLock().lock();
			return outputFileName;
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	private int m_percentageDone = 0;
	private float m_percentageDoneFloat = (float) 0.0;

	public int getPercentageDone() {
		return m_percentageDone;
	}
	
	public float getPercentageDoneFloat() {
		return m_percentageDoneFloat;
	}

	protected void updatePercentageDone() {
		try {
			rwlock.readLock().lock();

			int chunksLoaded = this.getNumberOfChunksLoaded();
			int numOfChunks = this.getNumberOfChunks();

			if (numOfChunks == 0 && chunksLoaded != 0) {
				m_percentageDone = 100;
				m_percentageDoneFloat = (float) 100.0;
			} else if (chunksLoaded == 0) {
				m_percentageDone = 0;
				m_percentageDoneFloat = (float) 0.0;
			} else {
				m_percentageDone = (int) (((float) chunksLoaded / (float) numOfChunks) * 100.0);
				m_percentageDoneFloat = (float) (((float) chunksLoaded / (float) numOfChunks) * 100.0);
			}
		} finally {
			rwlock.readLock().unlock();
		}
	}

	/**
	 * Return a random chunk number to download that is not already downloaded..
	 * 
	 * 
	 * FIXME: change this to: * determine the biggest block that is not finished *
	 * begin loading from the middle of that block * if nodes are already
	 * loading that middle then try the middle of the halves etc
	 * 
	 * @return number of chunk, starting by 0
	 * @throws NoFreechunkException
	 */
	private int getRandomChunkNumber() throws NoFreeChunkException {

		try {
			this.rwlock.readLock().lock();

			int chunkCount = this.getNumberOfChunks();

			// try to start from the beginning
			if (!doNotHaveToLoadChunk(0))
				return 0;

			// if there is only one source (=us) then pick the next free chunk
			if (this.getSources().size() == 1) {
				int freeRangeStart = 0;
				for (int i = 0; i < chunkCount
						&& this.getAlreadyDownloadedChunks().get(i); i++) {
					freeRangeStart = i;
				}

				return freeRangeStart;
			}

			// return a random unset chunk
			// do 50 attempts to find a chunk that needs to get loaded and that
			// is currently not in load
			for (int i = 0; i < 50; i++) {
				int chunkNo = BitSetUtils.getRandomUnsetBit(this
						.getAlreadyDownloadedChunks(), chunkCount);
				boolean ok = true;
				for (RunningFileDownload rd : this.getRunningDownloads()) {
					if (rd.getChunkNumber() == chunkNo)
						ok = false;
				}

				if (ok == true) {
					return chunkNo;
				}
			}

			// will happen with very very very small chance
			throw new NoFreeChunkException();

		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	public class NoFreeChunkException extends Exception {

	}

	// FIXME we should not return this directly? locking etc..
	protected List<RunningFileDownload> getRunningDownloads() {
		return runningDownloads;
	}

	public List<DownloadSource> getSources() {
		return sources;
	}

	public DownloadState getState() {
		try {
			rwlock.readLock().lock();
			return this.state;
		} finally {
			rwlock.readLock().unlock();
		}
	}

	private void initializeInformationFile(Properties props) throws IOException {
		if (!this.getInformationFile().exists())
			this.getInformationFile().createNewFile();
		if (this.getInformationFile().length() == 0) {
			FileOutputStream fos = new FileOutputStream(this
					.getInformationFile());
			props.storeToXML(fos, "http://p300.eu/");
			fos.close();
		}
	}

	private boolean isNeedsSave() {
		return needsSave;
	}

	private void loadInformationFile(Properties props) throws Exception {
		// FIXME check
		FileInputStream fis = new FileInputStream(this.getInformationFile());
		props.loadFromXML(fis);
		fis.close();

		// first, load the size of the file. without it, we cannot do
		// the bitset thing.
		String fs_s = props.getProperty("fileSize", "-1");
		long fs = Long.decode(fs_s);
		if (fs != -1) {
			fileSize = fs;
		}

		// load, AND-ify bitset
		if (this.alreadyDownloadedChunks == null) {
			this.alreadyDownloadedChunks = new BitSet(this.getNumberOfChunks());
		}
		String bitset_s = props.getProperty("alreadyDownloadedChunks", null);
		if (bitset_s != null) {
			if (bitset_s.length() == this.getNumberOfChunks()) {
				BitSet bs = de.guruz.p300.utils.BitSetUtils
						.setFromString(bitset_s);

				this.getAlreadyDownloadedChunks().or(bs);
			}
		}

		if (this.getNumberOfChunksLoaded() == this.getNumberOfChunks())
			this.setState(DownloadState.FINISHED);

		// load and save outputFilename
		String outputFilename_s = props.getProperty("outputFilename", null);
		if (outputFilename_s != null) {
			this.setOutputFileName(outputFilename_s);
		}

		// ///////////////

		// List<DownloadSource> sources = this.getSources();
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
					DownloadSource s = DownloadSource.fromString(value_string,
							FileType.FILE);
					this.addSource(s);
					it.remove();
				}
			} catch (Exception ex) {
				D.out(ex.getMessage());
				ex.printStackTrace();
			}
		}

		// System.out.println (props.size());

		// //////////////

		// load state
		String state_s = props.getProperty("state", null);
		DownloadState stateFromFile = null;
		try {
			stateFromFile = DownloadState.valueOf(state_s);
		} catch (Exception e) {

		}

		if (stateFromFile == DownloadState.FINISHED
				&& this.getState() != DownloadState.CAN_REMOVE_DOWNLOAD) {
			// we may not be finished, but another p300 instance may
			// have
			// finished us
			// Reading a FINISHED rules over everything
			this.setState(DownloadState.FINISHED);
		} else if ((this.getState() == DownloadState.NEW)
				&& (stateFromFile != null)) {
			// If we are NEW, we accept everything
			this.setState(stateFromFile);
			// } else {
			// for everything else, our state rules over the written
			// state
			// (do nothing)
		}

		updatePercentageDone();

	}

	public void merge() {
		merge(false);
	}

	/**
	 * Read possibly new information from disk, merge and then safe to disk
	 * (disk = download information file)
	 * 
	 * Also takes care if a download is finished
	 * 
	 */
	public void merge(boolean force) {

		try {
			rwlock.writeLock().lock();

			if (this.getState() == DownloadState.CAN_REMOVE_DOWNLOAD)
				return;

			try {
				// System.out.println("merge");

				Properties props = new Properties();

				initializeInformationFile(props);

				// load only if the file last modified changed
				if (lastLoad != this.getInformationFile().lastModified()) {
					// System.out.println(" load");
					loadInformationFile(props);
					lastLoad = this.getInformationFile().lastModified();

				}

				// Save only if forced OR we need a save and the last one is
				// more than 50 secs ago
				if (force
						|| (isNeedsSave() && (System.currentTimeMillis()
								- lastSave > 50 * 1000))) {
					// System.out.println(" save");
					saveInformationFile(props);

					lastSave = this.getInformationFile().lastModified();
					lastLoad = this.getInformationFile().lastModified();
					setNeedsSave(false);
				}

				// Why was this in? FIXME
				// having this not commented out will break downloading of
				// 0-bytes files
				// because they will finish before even a source is added ->
				if (this.getNumberOfChunks() == this.getNumberOfChunksLoaded())
					this.setState(DownloadState.FINISHED);

				if (this.getState() == DownloadState.FINISHED) {
					// System.out.println(" finish " + this.toString()
					// + " (sources:" + this.getSources().size());
					finishFile();
				}

			} catch (Exception e) {
				System.err.println(this.toString() + ":");
				e.printStackTrace();

			}

			updateUiRow();
			// manager.updateDownloadsTableModel();
			manager.invalidateUi();

		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * The download is finished, the download information file has been removed,
	 * now we can move the download :)
	 * 
	 * FIXME what if the filename is too long? check this
	 * 
	 * @throws Exception
	 */
	public void moveFileToDownloadsDir() throws Exception {
		try {
			rwlock.writeLock().lock();
			String completedDir = this.manager.getFinishedDownloadDir();

			File completeFile = new File(completedDir, FileNameUtils
					.sanitizeFilepathForLocalOS(this.getOutputFileName()));
			// completeFile = new File
			// (FileNameUtils.sanitizeFilepathForLocalOS(completeFile));

			if (!this.incompleteFile.exists() && this.getFileSize() > 0) {
				D.out("Could not move finished download to " + completeFile
						+ " because incomplete file does not exist (anymore)");
				return;
			}

			if (completeFile.exists()) {
				D.out("Could not move finished download to " + completeFile
						+ " because it exists");
				// since this should not happen (downloading the "same" file
				// twice)
				// we return
				return;
			}

			// create and forget ;)
			completeFile.getParentFile().mkdirs();

			if (!completeFile.createNewFile()) {
				D.out("Could not create finished download " + completeFile);
				return;
			}

			// if the file had 0 bytes then we did not download it anyway, so we
			// cannot move anything.
			if (this.fileSize == 0) {
				completeFile.createNewFile();
				incompleteFile.delete();
			} else {
				manager.getMaintainaner().moveFile(this.incompleteFile,
						completeFile);
			}

		} finally {
			rwlock.writeLock().unlock();
		}

	}

	/**
	 * Changes state to paused and merges. running download chunks will continue
	 * 
	 */
	public void pause() {
		// FIXME
	}

	/**
	 * This will get called if a chunk was not downloaded properly. It will be
	 * called if an error happens DURING downloading and when the file couldn't
	 * even be downloaded
	 * 
	 * FIXME we have to limit the number of attempts per minute!!
	 * 
	 * @param rd
	 * @param fatal
	 * @param msg
	 */
	public void runningDownloadedEndedWithError(RunningFileDownload rd,
			boolean fatal, String msg) {
		try {
			this.rwlock.writeLock().lock();
			D.out("NOT LOADED Chunk " + rd.getChunkNumber() + " of "
					+ this.getDisplayFilename() + ": " + msg);
			getRunningDownloads().remove(rd); // FIXME?
			manager.releaseFileDownloadSlot(rd.getSource().getRemoteHost());
		} finally {
			this.rwlock.writeLock().unlock();
		}
	}

	/**
	 * This gets called by a RunningDownload if it has succesfully been able to
	 * download and store a chunk. The chunk won't be loaded again and we try
	 * the next one
	 * 
	 * @param rd
	 */
	public void chunkFinishedProperly(RunningFileDownload rd) {
		try {
			rwlock.writeLock().lock();

			// int percentageBefoe = this.getPercentageDone();
			this.getAlreadyDownloadedChunks().set(rd.getChunkNumber());
			this.setNeedsSave(true);
			this.updatePercentageDone();

			if (this.getNumberOfChunks() == this.getNumberOfChunksLoaded()) {
				// force merge when we loaded the last chunk that needed loading
				this.merge(true);

			} else {
				this.merge();
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	public boolean runningDownloadShouldContinueWithChunk(
			RunningFileDownload rd, int chunkNo) {
		try {
			rwlock.writeLock().lock();

			return (!doNotHaveToLoadChunk(chunkNo));

		} finally {
			rwlock.writeLock().unlock();
		}
	}

	public void runningDownloadedDisconnectedProperly(RunningFileDownload rd) {
		try {
			rwlock.writeLock().lock();

			//D.out(rd + " finished properly from " + rd.getSource().getRemoteHost());
			
			getRunningDownloads().remove(rd);

			//manager.debugOutputSlots();
			manager.releaseFileDownloadSlot(rd.getSource().getRemoteHost());
			manager.debugOutputSlots();
			
			// call this because there may have been downloads waiting for a
			// slot
			manager.startAllDownloads();
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	private void saveInformationFile(Properties props) throws Exception {
		props.setProperty("file_type", FileType.FILE.toString());

		props.setProperty("fileSize", Long.toString(fileSize));

		props.setProperty("chunkSize", Long
				.toString(DownloadFile.DEFAULT_CHUNK_SIZE));

		props.setProperty("alreadyDownloadedChunks",
				de.guruz.p300.utils.BitSetUtils
						.setToString(this.getAlreadyDownloadedChunks(), this
								.getNumberOfChunks()));

		if (this.getOutputFileName() != null)
			props.setProperty("outputFilename", this.getOutputFileName());

		// and save out list
		for (int i = 0; i < this.getSources().size(); i++) {
			props.setProperty("source." + i, sources.get(i).toString());
		}
		// save the state
		props.setProperty("state", this.getState().toString());

		// set p300 version
		props.setProperty("information_file_version", ""
				+ INFORMATION_FILE_VERSION);

		FileOutputStream fos = new FileOutputStream(this.getInformationFile());
		props.storeToXML(fos, "bla");
		fos.close();

	}

	public void setNeedsSave(boolean b) {
		needsSave = b;
	}

	protected void setOutputFileName(String outputFileName) {
		try {
			this.rwlock.writeLock().lock();
			this.outputFileName = outputFileName.replace('/',
					File.separatorChar);
		} finally {
			this.rwlock.writeLock().unlock();
		}
	}

	protected void setState(DownloadState state) {
		//D.out("Entering setState");
		try {
			rwlock.readLock().lock();
			this.state = state;
		} finally {
			rwlock.readLock().unlock();
		}
		//D.out("Leaving setState");
	}

	public void startLoadingIfPossible() {
		try {
			this.rwlock.writeLock().lock();
			for (DownloadSource ds : getSources()) {
				// falls noch nicht am ziehen und die source zuletzt
				// nicht
				// benutzt wurde starte einen RunnindDownload
				startLoadingIfPossible(ds);
			}
		} finally {
			this.rwlock.writeLock().unlock();
		}
	}

	/**
	 * If we are a NEW or RUNNING download and the download source is not
	 * already used then we download a random chunk
	 * 
	 * @param ds
	 */
	static int slotCount = 0;
	public void startLoadingIfPossible(DownloadSource ds) {
		try {
			this.rwlock.writeLock().lock();

			//System.out.println (this.getRunningDownloads().size());
			
			
			// if we are not already running or new we exit here, because
			// we may be already removed or paused or whatever
			if (!(this.getState() == DownloadState.RUNNING || this.getState() == DownloadState.NEW))
			{
				//D.out(toString() + " could not run download: State is " + this.getState());
				return;
			}

			if (this.getNumberOfChunksLoaded() == this.getNumberOfChunks()) {
				//D.out(toString() + " could not run download: All chunks done");
				this.setState(DownloadState.FINISHED);
				return;
			}

			// a download may be running for that source already
			for (RunningFileDownload rd : this.getRunningDownloads()) {
				// FIXME equals?
				if (rd.getSource().equals(ds)) {
					//D.out(toString() + " could not run download: Already running for this source "  + ds.getRemoteHost());
					return;
				}

			}

			// we may not start at all right now because we shall not try a
			// source
			// (hammering protection, see HTTPDownloadAgent)
			if (!ds.couldRetryNow())
			{
				//D.out(toString() + " could not run download: Could not retry now "  + ds.getRemoteHost());
				return;
			}

			if (!manager.acquireFileDownloadSlot(ds.getRemoteHost())) {
				// / D.out("No slot free for " + ds.getRemoteHost());
				//D.out(toString() + " could not run download: No slot free for " + ds.getRemoteHost());
				//D.out(toString() + "       but runningDownloads = " + this.getRunningDownloads().size());
				return;
			}
			else
			{
				slotCount++;
				//D.out(slotCount + " " + toString() + " GOT A SLOT FOR " + ds.getRemoteHost());
			}

			// D.out("Slot free for " + ds.getRemoteHost());

			// set to RUNNING here because we may be new
			this.setState(DownloadState.RUNNING);

			// source not already loading :)

			// pick a chunk to load
			try {
				int chunkNumber = this.getRandomChunkNumber();
				long chunkSize = this.getChunkSize(chunkNumber);
				long chunkOffset = this.getChunkOffset(chunkNumber);
				long fileSize = this.getFileSize();

				// reset the retry time
				ds.noRetryTheNextMillisecs(-1);
				
				// create a running download
				RunningFileDownload rd = new RunningFileDownload(manager, this, ds,
						chunkOffset, chunkNumber, chunkSize, fileSize);
				getRunningDownloads().add(rd);

				// threadPoolExecutor.execute(rd);
				manager.getDownloadExecutor().execute(rd);

				//new Thread (rd).start();
				
			} catch (NoFreeChunkException e) {
				//D.out("No free chunk for download " + this);
				//ds.noRetryTheNextMillisecs(30*1000);
				manager.releaseFileDownloadSlot(ds.getRemoteHost());
			}
		} catch (Throwable t) {
			//D.out("THROWABLE!!!!!!!!" + t.toString());
			t.printStackTrace();
		} finally {
			this.rwlock.writeLock().unlock();
		}
	}

	// FIXME: difference to pause?
	public void stop() {

	}
	
	NumberFormat m_percentageNumberFormat;

	public String toString() {
		try {
			this.rwlock.readLock().lock();
			return "DownloadFile " + this.getIncompleteFile() + " ("
					+ m_percentageNumberFormat.format(this.getPercentageDoneFloat()) + "%)";
		} finally {
			this.rwlock.readLock().unlock();
		}
	}

	@Override
	protected void updateUiRow() {
		getUIRow()
				.setDownloadIcon(
						IconChooser.getImageIconFromFilename(this
								.getDisplayFilename()));
		getUIRow().setDisplayName(this.getDisplayFilename());
		getUIRow().setStateString(m_percentageNumberFormat.format(this.getPercentageDoneFloat()) + "%");

	}

}
