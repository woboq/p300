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

import java.text.NumberFormat;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.search.Indexer;
import de.guruz.p300.shares.ShareManager;
import de.guruz.p300.utils.HumanReadableSize;
import de.guruz.p300.utils.OsUtils;

/**
 * The / page of the webinterface
 * @author guruz
 *
 */
public class IndexRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if ((rt != HTTPVerb.GET) && (rt != HTTPVerb.POST) && (rt != HTTPVerb.HEAD)) {
			return false;
		}
		
		return (reqpath.equals ("/"));
	}
	
	private String HTMLWarning(String s) {
		return "<p class=\"warning\"><b>Warning: </b> "+ s +"</p>";
	}

	@Override
	public void handle() throws Exception {
		//System.out.println("Index request");

		
		long now = System.currentTimeMillis();
		Configuration conf = Configuration.instance();
		

		this.requestThread.httpStatus(200, "OK");
		
		// windows needs this?
		this.requestThread.httpHeadersForDAV();
		
		this.requestThread.httpContentType("text/html");
		this.requestThread.httpContents ();
		
		boolean authenticated = this.requestThread.isAuthenticated();
		
		Layouter layouter = new Layouter (this.requestThread); 
		layouter.replaceBasicStuff ();
		layouter.replaceTitle ("p300 on " + this.requestThread.getLocalDisplay());
		
		this.requestThread.write(layouter.getBeforeMainDiv());
		
		//StringBuilder content = new StringBuilder ("");
				
		// output some warnings if we are logged in and there is a need to
		if (authenticated) {
			if (Configuration.isJava15()) {
				this.requestThread.write(this.HTMLWarning ("No java 1.6 found, not using tray icon and splash screen"));
			}
			
			// testing on multicast working. Warn if we have not received anything for 3 minutes
			if (now - MainDialog.multicastListenThread.lastReceive > 1000*60*3) {
				this.requestThread.write (this.HTMLWarning ("No multicast traffic received in the last 3 minutes."));
			}

			// testing of configuration stuff
			if (ShareManager.instance().getShareNames ().length == 0) {
				this.requestThread.write (this.HTMLWarning ("No directories shared. People cannot download anything."));
			}
			// commented because not true anymore with implicit allowing
//			if (conf.getAllowedHosts ().length == 0) {
//				this.requestThread.write (this.HTMLWarning ("No hosts except localhost are allowed. Noone can connect to you."));
//			}

			if (MainDialog.updaterThread.haveNewVersionReady()) {
				this.requestThread.write (this.HTMLWarning ("This is an old release. Restart p300 for a newer one."));
			}
			
		}
		
		this.requestThread.write ("<table>");
		
		this.requestThread.write ("<tr><th>Revision/Version</th><td>");
		this.requestThread.write (Configuration.getSVNRevision ());
		
		
		if (conf.isOutdatedSVNRevision()) {
			this.requestThread.write(" <b>(outdated)</b>");
		}
		this.requestThread.write("</td></tr>");
		
		if (MainDialog.launchedByRevision != null && MainDialog.launchedByRevision.length() > 0) {
			this.requestThread.write ("<tr><th>Launched by Revision</th><td>");
			this.requestThread.write (MainDialog.launchedByRevision);
			this.requestThread.write("</td></tr>");
		}
		
			
		
			
		if (authenticated) {
			this.requestThread.write("<tr><th valign='top'>Known local adresses</th><td>");
			String urls[] = MainDialog.listenThread.getKnownLocalURLs ();
			for (String element : urls) {
				this.requestThread.write (element);
				this.requestThread.write ("<br>");
			}
			
			this.requestThread.write("</td></tr>");
	
		}
		
		
		this.requestThread.write ("<tr><th>p300 Uptime</th><td>");
		this.requestThread.write (de.guruz.p300.utils.HumanReadableTime.timeDifferenceAsString(0, now - MainDialog.startedOn));
		this.requestThread.write ("</td></tr>");
		
		this.requestThread.write ("<tr><th>OS</th><td>");
		this.requestThread.write (OsUtils.getOS ());
		this.requestThread.write ("</td></tr>");
		
		
		// memory stuff
        Runtime rt = Runtime.getRuntime();
        float freeMemory = rt.freeMemory();
        float totalMemory = rt.totalMemory();
        float maxMemory = rt.maxMemory();

        long usedJavaMem = (long)(totalMemory - freeMemory);
        long allocatedJavaMem = (long)totalMemory;
        long maxJavaMem = (long)maxMemory;


        
        this.requestThread.write("<tr><th valign='top'>Virtual machine memory</th><td>");
        this.requestThread.write (HumanReadableSize.get(usedJavaMem));
        this.requestThread.write(" used<br>");
        this.requestThread.write( HumanReadableSize.get(allocatedJavaMem));
        this.requestThread.write(" allocated<br>");
        this.requestThread.write(HumanReadableSize.get(maxJavaMem)); 
        this.requestThread.write(" maximum possible");
        this.requestThread.write("</td></tr>");
        
		
		this.requestThread.write ("<tr><th>Processors/Cores</th><td>");
		this.requestThread.write (Runtime.getRuntime().availableProcessors());
		this.requestThread.write ("</td></tr>");
		
		
		
		
		this.requestThread.write ("<tr><th>Java</th><td>");
		this.requestThread.write(Configuration.getJavaVersion());
		this.requestThread.write ("</td></tr>");
		
		// we do not care about this in any way
		//content.append ("<tr><th>Filename encoding</th><td>");
		//content.append (System.getProperty("file.encoding"));
		//content.append ("</td></tr>");
		
		
		
		
//		this.requestThread.write ("<tr><th>HTTP Connections</th><td>");
//		this.requestThread.write (MainDialog.listenThread.getActiveHTTPCount());
//		this.requestThread.write ("</td></tr>");
		
		this.requestThread.write ("<tr><th>Upload Connections</th><td>");
		this.requestThread.write (MainDialog.listenThread.getActiveUploadCount());
		this.requestThread.write ("</td></tr>");
	
//		this.requestThread.write ("<tr><th>Incoming Traffic</th><td>");
//		this.requestThread.write (HumanReadableSize.get(MainDialog.listenThread.incomingTraffic));
//		this.requestThread.write ("</td></tr>");
//		
//		this.requestThread.write ("<tr><th>Outgoing Traffic</th><td>");
//		this.requestThread.write (HumanReadableSize.get(MainDialog.listenThread.outgoingTraffic));
//		this.requestThread.write ("</td></tr>");
		
		int bwLimitKB = conf.getOutputBWLimitInKB();
		if (bwLimitKB != 0) {
			this.requestThread.write ("<tr><th>Upload Bandwidth Limit</th><td>");
			this.requestThread.write (bwLimitKB);
			this.requestThread.write (" KB/sec</td></tr>");
		}
		
		int indexedFileCount = Indexer.lastIndexFileCount;
		long indexedFileSize = Indexer.lastIndexAllSize;
		
		if ((indexedFileCount > -1) && (indexedFileSize > -1)) {
			NumberFormat nf = NumberFormat.getInstance();
			this.requestThread.write ("<tr><th>Total amount of files</th><td>");
			this.requestThread.write (nf.format(indexedFileCount));
			this.requestThread.write(" (last indexer run)</td></tr>");
			
			this.requestThread.write ("<tr><th>Total size of files</th><td>");
			this.requestThread.write (HumanReadableSize.get(indexedFileSize));
			this.requestThread.write (" (last indexer run)</td></tr>");
		}
		
		this.requestThread.write ("</table>");
		
		this.requestThread.write (layouter.getAfterMainDiv());
		

				
		
		this.requestThread.flush();
		this.requestThread.close();
	}

}
