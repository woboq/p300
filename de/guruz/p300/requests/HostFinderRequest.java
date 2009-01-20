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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.HostMap;
import de.guruz.p300.hosts.httpmulticast.HostFinderThread;
import de.guruz.p300.http.HTTPVerb;
import de.guruz.p300.logging.D;
import de.guruz.p300.threads.RequestThread;

/**
 * A request used by the HostFinderThread of other p300 hosts
 * 
 * @author guruz
 * 
 */
public class HostFinderRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if ((rt != HTTPVerb.GET) && (rt != HTTPVerb.POST)) {
			return false;
		}

		return (reqpath.equals("/hostfinder/0.1/get"));
	}

	@Override
	public void handle() throws Exception {
		// den content parsen. wir wollen ja auch tolle hosts haben :)
		if (this.requestThread.hasClientContent()
				&& (this.requestThread.verb == HTTPVerb.POST)
				&& (this.requestThread.getClientContent() != null)
				&& (this.requestThread.getClientContent().length > 0)
				&& (!this.requestThread.getHeader(RequestThread.X_P300_WE_ARE,
						"").equals(Configuration.instance().getUniqueHash()))) {
			ByteArrayInputStream bais = new ByteArrayInputStream(
					this.requestThread.getClientContent());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					bais));
			HostFinderThread hft = MainDialog.hostFinderThread;

			String line = reader.readLine();
			while (line != null) {
				//if (line.contains(" - "))
				//	D.out ("Outgoing req: from " + this.requestThread
				//			.getRemoteIP() + " received " + line);
				
				hft.parseHTTPMulticastLine(line, this.requestThread
						.getRemoteIP());

				// D.out ("Parsed line " + line);

				line = reader.readLine();
			}

			reader.close();

		}
		else
		{
			D.out ("Hostfinder: Ignoring incoming request from " + this.requestThread.getRemoteIP());
		}

		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpSendWeAreHeader();

		this.requestThread.httpContents();

		HostMap hm = MainDialog.hostMap;
		String kh = hm.writeOutKnownHosts();
		this.requestThread.write(kh);

		this.requestThread.close();
	}

}
