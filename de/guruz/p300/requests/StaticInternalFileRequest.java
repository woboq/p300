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

import java.io.InputStream;

import de.guruz.p300.Resources;
import de.guruz.p300.http.HTTPVerb;

/**
 * Requests for icons etc.
 * 
 * @author guruz
 * 
 */
public class StaticInternalFileRequest extends Request {

	static final String[] iconFileNames = { "static/22x22/folder.png",
			"static/22x22/computer.png", "static/22x22/generic.png",
			"static/22x22/sound.png", "static/22x22/movie.png",
			"static/22x22/image.png", "static/22x22/back.png",
			"static/22x22/compressed.png",
			"static/22x22/applications-internet.png",
			"static/22x22/folder-open.png" };

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if ((rt != HTTPVerb.GET) && (rt != HTTPVerb.HEAD)) {
			return false;
		}

		return (StaticInternalFileRequest.webFilenameToFSFilename(reqpath) != null);
	}

	public static String webFilenameToFSFilename(String wfn) {

		if (wfn.equals("/favicon.ico")) {
			return "static/favicon.ico";
		}

		if (wfn.equals("/baselayout.css")) {
			return "static/baselayout.css";
		}

		if (wfn.equals("/layout.css")) {
			return "static/layout.css";
		}

		if (wfn.equals("/script.js")) {
			return "static/script.js";
		}

		if (wfn.equals("/folder.gif")) {
			return StaticInternalFileRequest.iconFileNames[0];
		}
		if (wfn.equals("/comp_blue.gif")) {
			return StaticInternalFileRequest.iconFileNames[1];
		}
		if (wfn.equals("/generic.gif")) {
			return StaticInternalFileRequest.iconFileNames[2];
		}
		if (wfn.equals("/sound2.gif")) {
			return StaticInternalFileRequest.iconFileNames[3];
		}
		if (wfn.equals("/movie.gif")) {
			return StaticInternalFileRequest.iconFileNames[4];
		}
		if (wfn.equals("/image2.gif")) {
			return StaticInternalFileRequest.iconFileNames[5];
		}
		if (wfn.equals("/back.gif")) {
			return StaticInternalFileRequest.iconFileNames[6];
		}
		if (wfn.equals("/compressed.gif")) {
			return StaticInternalFileRequest.iconFileNames[7];
		}
		if (wfn.equals("/network.gif")) {
			return StaticInternalFileRequest.iconFileNames[8];
		}
		if (wfn.equals("/folder_open.gif")) {
			return StaticInternalFileRequest.iconFileNames[9];
		}
		return null;

	}

	@Override
	public void handle() throws Exception {
		String fn = StaticInternalFileRequest
				.webFilenameToFSFilename(this.requestThread.path);

		if (fn == null) {
			this.requestThread.close(404, "Not existing");
			return;
		}

		// first retrieve the size
		InputStream is = Resources.getResourceAsStream("de/guruz/p300/requests/" + fn);
		int size = 0;
		byte b[] = new byte[1];
		int thisRead = is.read(b);
		while (thisRead != -1) {
			size++;
			thisRead = is.read(b);
		}
		
		// now retrieve it again and send it this time with the proper size
		// normally we should not need all this since we have chunked encoding, but somehow we need it
		// anyway
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpHeader("Cache-Control", "max-age=6000"); // 100
		this.requestThread.httpContentLength(size);
		this.requestThread.httpContents();
		is = Resources.getResourceAsStream("de/guruz/p300/requests/" + fn);
		thisRead = is.read(b);
		while (thisRead != -1) {
			this.requestThread.write(b, thisRead);
			thisRead = is.read(b);
		}
		this.requestThread.flush();
		this.requestThread.close();

	}

}