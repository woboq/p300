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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import de.guruz.p300.connections.SynchronousLogicalStreamConnection;
import de.guruz.p300.downloader.DownloadManager;
import de.guruz.p300.downloader.DownloadSource;
import de.guruz.p300.downloader.SourceState;
import de.guruz.p300.http.HTTPHeaderReader;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.http.HTTPReplyLine;
import de.guruz.p300.http.HTTPRequestWriter;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.http.TcpHTTPConnectionPool;
import de.guruz.p300.http.responsebodyreaders.HTTPResponseBodyReader;
import de.guruz.p300.http.responsebodyreaders.HTTPResponseBodyReaderFactory;
import de.guruz.p300.logging.D;

public class RunningFileDownload implements Runnable {
	int chunkNumber;
	// multiple information because last chunk may be smaller than rest of
	// chunks
	long chunkOffset;

	long chunkSize;

	long fileSize;

	protected final long RETRY_WAIT_TIME = 1000 * 60 * 4 + de.guruz.p300.utils.RandomGenerator
			.getInt(0, 2 * 60 * 1000);

	private SynchronousLogicalStreamConnection m_connection;

	private RandomAccessFile randomAccessFile = null;

	private FileChannel outputFileChannel = null;

	private DownloadSource source = null;

	private ReadableByteChannel m_contents = null;

	HTTPResponseBodyReader responseReader = null;

	DownloadFile downloadFile;
	
	DownloadManager downloadManager;
	
	static int createCount = 0;

	public RunningFileDownload(DownloadManager dm, DownloadFile df, DownloadSource ds, long cO,
			int cN, long cS, long fs) {
		downloadManager = dm;
		downloadFile = df;
		source = ds;
		chunkOffset = cO;
		chunkNumber = cN;
		chunkSize = cS;
		fileSize = fs;
		
		createCount++;
		//D.out (createCount + " __CREATED__ " + "RunndingFileDownload " + source.toString());
	}

	public void run() {
		try {
			Thread.currentThread().setName("RunndingFileDownload " + source.toString());
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			//D.out ("__RUNNING__ " + Thread.currentThread().getName());

			randomAccessFile = this.getRandomAccessFile();

			if (randomAccessFile == null)
				throw new Exception("No random access file");

			outputFileChannel = randomAccessFile.getChannel();

			m_connection = TcpHTTPConnectionPool.acquireOrCreateConnection(source
					.getRemoteHost(), source.getRemotePort(), 30*1000);

			HTTPHeaders requestHeaders = new HTTPHeaders();
			requestHeaders.setHeader("Range", getRangeAsString());

			new HTTPRequestWriter(m_connection, source.getRemoteHost() + ":" + source.getRemotePort(),
					HTTPVerb.GET, source.getRemoteEntity().getPath(),
					requestHeaders).write();

			String replyline = m_connection.readLine();
			HTTPReplyLine hrl = new HTTPReplyLine(replyline);
			HTTPHeaderReader hhr = new HTTPHeaderReader();
			hhr.read(m_connection);

			if (!hrl.isOK())
				throw new Exception("Something wrong with request, received "
						+ hrl.getNr() + " " + hrl.getMsg());

			responseReader = HTTPResponseBodyReaderFactory.createReader(
					m_connection, hrl, hhr.getHeaders());

			m_contents = responseReader.asReadableByteChannel();
			m_connection.setTimeout(30*1000);

			boolean continueLoading = true;

			while (continueLoading) {
				long toReadForChunk = getChunkSize();

				while (toReadForChunk > 0) {
					// "If the given position is greater than the file's current
					// size then no bytes are transferred"
					outputFileChannel.position(getChunkOffset()
							+ getChunkSize() - toReadForChunk);
					//randomAccessFile.seek(outputFileChannel.position());
					long t = outputFileChannel.transferFrom(m_contents,
							outputFileChannel.position(), toReadForChunk);

					// note that according to JDK 6 sources this
					// is for socket->file not more efficient than using
					// a direct buffer :(

					toReadForChunk = toReadForChunk - t;

					if (t != DownloadFile.DEFAULT_CHUNK_SIZE && chunkSize == DownloadFile.DEFAULT_CHUNK_SIZE)
					{
						D.out(this + " " + "Transferred " + t + " bytes for "
								+ source.getRemoteEntity().getPath());
						D.out(this + " timeout is " + m_connection.getTimeout());
					}
//					D.out (this + " " +"toReadForChunk = " + toReadForChunk);
//					D.out (this + " " +"pos = " + outputFileChannel.position());
//					D.out (this + " " +"contents open = " + m_contents.isOpen());
//					D.out (this + " " +"connection open = " + m_connection.isConnected());
//					D.out (this + " " +"chunk number = " + getChunkNumber());
//					D.out (this + " " +"chunk size = " + getChunkSize());
//					D.out (this + " " +"chunk offset = " + getChunkOffset());
//					D.out (this + " " +"Range as string = " + getRangeAsString());
//					D.out (this + " " +"size = " + outputFileChannel.size());
				}

				downloadFile.chunkFinishedProperly(this);

				// FIXME check here if doing again
				if (downloadFile.runningDownloadShouldContinueWithChunk(this,
						chunkNumber + 1)) {
					// this will only happen if it is not the last chunk
					chunkNumber++;
					chunkSize = downloadFile.getChunkSize(chunkNumber);
					chunkOffset = downloadFile.getChunkOffset(chunkNumber);

				} else {
					// it was the last chunk or the following chunk was already
					// loaded
					//System.out.println ("Not continuing to load from " + source.getRemoteHost());
					//downloadManager.debugOutputSlots ();
					//System.out.println ("~~~~");
					continueLoading = false;
					closeOutputFile();
					releaseConnection();
					downloadFile.runningDownloadedDisconnectedProperly(this);
					return;
				}
			}

		} catch (ConnectException e) {
			e.printStackTrace();
			source.setState(SourceState.CANNOTCONNECT);
			source.noRetryTheNextMillisecs(RETRY_WAIT_TIME);
			abort(e.getClass().getSimpleName() + " " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			source.setState(SourceState.TIMEOUT);
			source.noRetryTheNextMillisecs(RETRY_WAIT_TIME);
			abort(e.getClass().getSimpleName() + " " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			source.setState(SourceState.TIMEOUT);
			source.noRetryTheNextMillisecs(RETRY_WAIT_TIME);
			abort(e.getClass().getSimpleName() + " " + e.getMessage());
		} finally {
			downloadManager.debugOutputSlots ();
			Thread.currentThread().setName("EX-RunndingFileDownload " + source.toString());
		}

	}

	private void abort(String msg) {
		closeOutputFile();
		abortConnection();
		downloadFile.runningDownloadedEndedWithError(this, true, msg);
	}

	/**
	 * Called by runningDownload.abort() which is called by DownloadFile if we
	 * are a reused connection that should not get reused
	 * 
	 * also called above when an exception occurs
	 * 
	 */
	public void closeOutputFile() {
		try {
			try {
				outputFileChannel.close();
				randomAccessFile.close();
				// inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			randomAccessFile.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			randomAccessFile = null;
			outputFileChannel = null;
		}

	}

	protected void releaseConnection() {
		if (responseReader != null && !responseReader.hasAborted()
				&& responseReader.hasFinished() && m_connection.isConnected()) {
			TcpHTTPConnectionPool.releaseConnection(m_connection);
		} else {
			abortConnection();
		}

	}

	protected void abortConnection() {
		TcpHTTPConnectionPool.abortConnection(m_connection);
	}

	public synchronized RandomAccessFile getRandomAccessFile() {
		try {
			File incompleteFile = getDownloadFile().getIncompleteFile();
			incompleteFile.createNewFile();
			
			RandomAccessFile raf = new RandomAccessFile(incompleteFile, "rw");
			if (raf.length() != fileSize)
			{
				raf.setLength(fileSize);
				raf.getFD().sync();
			}
			
			return raf;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected int getChunkNumber() {
		return chunkNumber;
	}

	protected long getChunkOffset() {
		return chunkOffset;
	}

	protected long getChunkSize() {
		return chunkSize;
	}

	protected DownloadFile getDownloadFile() {
		return downloadFile;
	}

	// FIXME use wrapper class
	public String getRangeAsString() {
		return "bytes=" + getChunkOffset() + "-" + (getFileSize() - 1);
	}

	// FIXME use wrapper class
	// public String getBeginningOfRangeAsString() {
	// return "bytes=" + getChunkOffset() + "-";
	// }

	protected DownloadSource getSource() {
		return source;
	}

	protected void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	protected void setChunkOffset(long chunkOffset) {
		this.chunkOffset = chunkOffset;
	}

	protected void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
	}

	protected long getFileSize() {
		return fileSize;
	}

	protected void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

}
