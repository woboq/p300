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
package de.guruz.p300.utils;

import java.io.File;

public class FileNameUtils {
	private static final char[] ILLEGAL_CHARS_ANY_OS = { '/', '\n', '\r', '\t',
			'\0', '\f' };
	private static final char[] ILLEGAL_CHARS_UNIX = { '`' };
	private static final char[] ILLEGAL_CHARS_WINDOWS = { '?', '*', '\\', '<',
			'>', '|', '\"', ':' };

	public static String replaceVariables(String s) {
		String home = System.getProperty("user.home", "./");
		return s.replace("%HOME%", home);
	}

	public static boolean isValidCharForFilename(char c) {
		for (int i = 0; i < ILLEGAL_CHARS_ANY_OS.length; i++) {
			if (ILLEGAL_CHARS_ANY_OS[i] == c)
				return false;
		}

		if (OsUtils.isWindows()) {
			for (int i = 0; i < ILLEGAL_CHARS_WINDOWS.length; i++) {
				if (ILLEGAL_CHARS_WINDOWS[i] == c)
					return false;
			}
		} else {
			for (int i = 0; i < ILLEGAL_CHARS_UNIX.length; i++) {
				if (ILLEGAL_CHARS_UNIX[i] == c)
					return false;
			}
		}

		return true;
	}

	public static String sanitizeFilenameForLocalOS(String fn) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < fn.length(); i++) {
			if (isValidCharForFilename(fn.charAt(i)))
				sb.append(fn.charAt(i));
			else
				sb.append('_');
		}

		return sb.toString();
	}

	public static String sanitizeFilepathForLocalOS(String fp) {
		// yeah yeah, this is inefficient, i know :)
		File f = new File (fp);
		String s =  sanitizeFilenameForLocalOS(f.getName());
		
		File g = f;
		
		while (g.getParentFile() != null)
		{
			g = g.getParentFile();
			
			s = sanitizeFilenameForLocalOS (g.getName()) + File.separatorChar + s;
		}
		
		return s;
		
	}
}
