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
package de.guruz.p300.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * This class does our console output
 * @author guruz
 *
 */
public class D {
	private static Formatter Normalformater = new FormatterWithDateAndNewline ();
	
	private static Logger Normallogger = null;
	private static StreamHandler consoleHandler;
	
	static {
		Normallogger = Logger.getLogger("de.guruz.p300");
		
		// remove loggers that may be there. I do not know if this makes sense 
		Handler handlers[] = Normallogger.getHandlers();
		for (Handler h : handlers)
			Normallogger.removeHandler(h);
		
		Normallogger.setLevel(Level.INFO);
		Normallogger.setFilter(null);
		//Normallogger.info("blaat");
		Normallogger.setUseParentHandlers(false);
		//Normallogger.info("blaat2");
		
		consoleHandler = new StreamHandler (System.out, Normalformater);
		//consoleHandler = new ConsoleHandler ();
		addLogHandler(consoleHandler);
	}
	
	public static void addLogHandler (Handler lh) {
		lh.setFormatter(Normalformater);
		Normallogger.addHandler (lh);
	}

	public static void out (String m) {
		Normallogger.log(Level.INFO,m);
		consoleHandler.flush();
	}

	// called by the Main class before launching another jar
	public static void deInit() {
		Normallogger.removeHandler(consoleHandler);
	}


}
