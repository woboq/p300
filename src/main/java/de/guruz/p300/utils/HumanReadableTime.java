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

public class HumanReadableTime {
	public static String timeDifferenceAsString (long secs, long milisecs) {
		StringBuilder retval = new StringBuilder ();
		long val = secs + (milisecs / 1000);
		
		long seconds = val % 60;
		val = val / 60; // jetzt minuten
		long minutes = val % 60;
		val = val / 60; // jetzt stunden
		long hours = val % 24;
		val = val / 24; // jetzt tage
		long days = val % 7;
		val = val / 7; // jetzt wochen
		long weeks = val;
		
		if (weeks > 0) {
			retval.append(weeks);
			if (weeks == 1) {
				retval.append ( " week");
			} else {
				retval.append(" weeks");
			}
		}
		if (days > 0) {
			if (retval.length() > 0) {
				retval.append(", ");
			}
			retval.append(days);
			if (days == 1) {
				retval.append ( " day");
			} else {
				retval.append(" days");
			}
		}
		if (hours > 0) {
			if (retval.length() > 0) {
				retval.append(", ");
			}
			retval.append(hours);
			if (hours == 1) {
				retval.append ( " hour");
			} else {
				retval.append(" hours");
			}
		}
		if (minutes > 0) {
			if (retval.length() > 0) {
				retval.append(", ");
			}
			retval.append(minutes);
			if (minutes == 1) {
				retval.append ( " minute");
			} else {
				retval.append(" minutes");
			}
		}
		if (seconds > 0) {
			if (retval.length() > 0) {
				retval.append(", ");
			}
			retval.append(seconds);
			if (seconds == 1) {
				retval.append ( " second");
			} else {
				retval.append(" seconds");
			}
		}
		
		return retval.toString();
	}
}
