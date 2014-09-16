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
package de.guruz.p300.search;

import java.io.File;

import de.guruz.p300.Configuration;

public class Common {
	public static final File dataBaseFile = new File(Configuration.configDirFileName ("fileindex.xml.gz"));	

	// Encode a String for XML attribute conformity
	// Removes all illegal characters & encodes all necessary ones
	public static String encodeForXMLAttribute(String s) {
		if (s != null) {
			char[] stringChars = s.toCharArray();
			char[] newChars = new char[stringChars.length];
			for (int i = 0; i < stringChars.length; i++) {
				newChars[i] = Common.returnGoodXMLChar(stringChars[i]);
			}
			String newString = String.valueOf(newChars);
			newString = newString.replace("\u0000", "");
			newString = newString.replace("&", "&amp;");
			newString = newString.replace("<", "&lt;");
			newString = newString.replace(">", "&gt;");
			newString = newString.replace("\"", "&quot;");
			newString = newString.replace("'", "&apos;");
			return newString;
		} else {
			return "";
		}
	}

	// For a bad XML char, returns \u0000 (which has to be filtered out before writing to XML!)
	// For a good XML char, returns the char
	private static char returnGoodXMLChar(char c) {
		// Not allowed in XML, but no information lost if we ditch them
		if ((c != '\u0009') && (c != '\n') && (c != '\r') && ((c < '\u0020') || (c > '\uD7FF')) && ((c < '\uE000') || (c > '\uFFFD'))) {
			return '\u0000';
		} else {
			return c;
		}
	}

}