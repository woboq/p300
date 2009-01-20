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

import java.net.URL;

import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.hosts.HostLocation;

/**
 * FIXME: later refactor this so that the source is abstract and could be
 * anything, now just a normal HTTP source
 * 
 * FIXME refactor this into java.net.URL ? may not work coz of our additional
 * variabls
 * 
 * @author guruz
 * 
 */
public class DownloadSource implements Comparable<DownloadSource>{
	private RemoteEntity remoteEntity;

	private String remoteHost;
	private int remotePort;

	private SourceState state = SourceState.UNKNOWN;

	// we need the -1, else it does not always start right now :)
	private long noRetryBefore = System.currentTimeMillis() - 1;

	public void setState(SourceState ss) {
		state = ss;
	}

	public void noRetryTheNextMillisecs(long ms) {
		noRetryBefore = System.currentTimeMillis() + ms;
	}

	public boolean couldRetryNow() {
		return (System.currentTimeMillis() > noRetryBefore);
	}

	public boolean equalsParams(RemoteEntity remoteEntity, HostLocation hl) {
		return (remoteEntity.equals(this.remoteEntity) && hl.equals(this
				.getRemoteHost(), this.getRemotePort()));
	}

	public DownloadSource(RemoteEntity remoteEntity, HostLocation hl) {
		super();
		this.remoteEntity = remoteEntity;
		this.remoteHost = hl.getIp();
		this.remotePort = hl.getPort();
	}

	public static DownloadSource fromString(String s) {
		return fromString(s, FileType.FILE);
	}

	public static DownloadSource fromString(String s, FileType ft) {
		try {
			URL u = new URL(s);
			RemoteEntity rf;

			if (ft == FileType.FILE)
				rf = new RemoteFile(u.getPath());
			else if (ft == FileType.DIRECTORY)
				rf = new RemoteDir(u.getPath());
			else
				throw new Exception();

			HostLocation hl = new HostLocation(u.getHost(), u.getPort());
			return new DownloadSource(rf, hl);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null; // FIXME!
	}

	public String toString() {
		return "http://" + remoteHost + ":" + remotePort
				+ remoteEntity.getPath();
	}

	public RemoteEntity getRemoteEntity() {
		return remoteEntity;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public String getConcatenatedFileName() {
		return this.getRemoteHost()
				+ "_"
				+ this.getRemotePort()
				+ de.guruz.p300.utils.URL.decode(this.getRemoteEntity()
						.getPath());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (this.toString().equals(obj.toString()))
			return true;

		return false;
	}

	public HostLocation getHostLocation() {
		return new HostLocation(this.getRemoteHost(), this.getRemotePort());
	}

	public int compareTo(DownloadSource o) {
		return toString().compareTo(o.toString());
	}

}
