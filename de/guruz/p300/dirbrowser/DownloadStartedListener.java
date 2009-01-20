package de.guruz.p300.dirbrowser;

import de.guruz.p300.hosts.Host;

public interface DownloadStartedListener {
	public void downloadWasStarted (Host h, RemoteEntity e);
}
