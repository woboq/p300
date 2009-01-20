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
package de.guruz.p300.tests.manual;

import java.util.Collections;

import junit.framework.TestCase;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.downloader.DownloadManager;
import de.guruz.p300.hosts.HostLocation;

public class DownloadManagerTest extends TestCase {

	DownloadManager man;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		String incompleteDownloadDir = "/tmp/p300_download_incomplete";
		String finishedDownloadDir = "/tmp/p300_download_finished";
		String informationDir = "/tmp/p300_download_information";
		String finishedInformationDir = "/tmp/p300_download_information_finished";
		
		man = new DownloadManager (incompleteDownloadDir, finishedDownloadDir, informationDir, finishedInformationDir);
		man.start ();
		
	}

	protected void tearDown() throws Exception {
		man.stop ();
		man = null;
	}
	
//	public void testFile () {
//
//		
//		HostLocation hl = new HostLocation ("127.0.0.1", 4337);
//		RemoteFile rf = new RemoteFile ("/shares/music/Covenant/Europa/tension.MP3");
//		man.startDownload(hl, rf, 9256960);
//		
//		// sleep a while and check for file?
//		
//		try {
//			Thread.sleep (1000*60*10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void testDir () {
		
		HostLocation hl = new HostLocation ("127.0.0.1", 4337);
		RemoteDir rd = new RemoteDir ("/shares/test");
		
		man.startDownload(Collections.singletonList(hl), rd);
		
		// sleep a while and check for file?
		
		try {
			Thread.sleep (1000*60*10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

}
