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
package de.guruz.p300.utils.launchers;

import java.lang.reflect.Method;

import de.guruz.p300.Configuration;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.OsUtils;

public class BareBonesBrowserLaunch {

	private static final String errMsg = "Error attempting to launch web browser";
	private static String[] browsers = { "gnome-open", "x-www-browser",
			"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };

	public static String findUnixBrowser() {
//		System.err.println(">findUnixBrowser");
		String browser = null;
		try {
			for (int count = 0; (count < BareBonesBrowserLaunch.browsers.length) && (browser == null); count++)
			{
				int i = Runtime.getRuntime().exec(
						new String[] { "which", BareBonesBrowserLaunch.browsers[count] })
						.waitFor();
//				System.err.println("which exitcode: " + browsers[count] + ", " + i);
				if (i == 0) {
					browser = BareBonesBrowserLaunch.browsers[count];
				}
			}
		} catch (Exception e) {
			D.out(BareBonesBrowserLaunch.errMsg + ":\n" + e.getLocalizedMessage());
		}
//		System.err.println("<findUnixBrowser: " + browser);
		return browser;
	}
	
	private static String getUnixBrowser() {
		String configBrowser = Configuration.instance().getUnixBrowser();
		if ((configBrowser == null) || configBrowser.equals("")) {
			return BareBonesBrowserLaunch.findUnixBrowser();
		} else {
			return configBrowser;
		}
	}
	
	public static void openURL(String url) {
//		System.err.println(">openURL: " + url);
		try {
			D.out("Trying to open " + url);
			if (OsUtils.isOSX()) {
				Class<?> fileMgr = (Class<?>)Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
				
				
			} else if (OsUtils.isWindows()) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String browser = BareBonesBrowserLaunch.getUnixBrowser();
				
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					D.out("Using " + browser + " to open " + url);
					Runtime.getRuntime().exec(new String[] { browser, url });
//					System.err.println("browser exitcode: " + i);
				}
			}
		} catch (Exception e) {
			D.out(BareBonesBrowserLaunch.errMsg + ":\n" + e.getLocalizedMessage());
		}
//		System.err.println("<openURL");
	}

}
