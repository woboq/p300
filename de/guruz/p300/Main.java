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
package de.guruz.p300;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import de.guruz.p300.logging.D;
import de.guruz.p300.utils.JarChecker;
import de.guruz.p300.utils.JarClassLoader;
import de.guruz.p300.utils.JarFileFilter;

/**
 * The Main class takes care of loading the latest locally stored .jar
 * 
 * @author guruz
 * 
 */
public class Main {

	/**
	 * This main method looks for a newer jar in the .p300 dir
	 * 
	 * @return
	 */
	public static void main(String[] args) throws Exception {
		Configuration.createDotP300();

		D.out("JVM: " + Configuration.getJavaVersion());

		int myRevision = Configuration.getSVNRevision();
		// String myRevisionString = myRevision + ".jar";

		// do not update for dev version
		if (myRevision == 0) {
			D.out("Already running development version, not using updates");
			MainDialog.main(args);
			return;
		}

		// do not run latest stored revision
		if (Configuration.instance().getAlwaysRunLatestVersion() == false) {
			D.out("I was told not to run latest stored revision");
			MainDialog.main(args);
			return;
		}

		String mainJarDirName = Configuration.configDirFileName("jar/p300/");
		File mainJarDir = new File(mainJarDirName);

		File jars[] = mainJarDir.listFiles(new JarFileFilter());
		File jarToUse = null;

		if (jars == null) {
			jars = new File[0];
		}

		for (File jar : jars) {
			// set this jar as jarToUse if suitable
			if (jarToUse == null
					|| (getRevisionFromFilename(jarToUse) < getRevisionFromFilename(jar))) {
				jarToUse = jar;
			}

			// check if our running revision is larger, if yes re-set jarToUse
			// to null
			if (myRevision > getRevisionFromFilename(jarToUse)) {
				jarToUse = null;
			}
		}

		// is this jar actually equal to our revision
		if (jarToUse == null || getRevisionFromFilename(jarToUse) == myRevision) {
			D.out("No newer jar found, using myself");
			MainDialog.main(args);
			return;
		}

		if (JarChecker.checkSignature(jarToUse) == false) {
			D.out("Unsigned jar file or wrong signature, not running "
					+ jarToUse);
			MainDialog.main(args);
			return;
		}

		D
				.out("This is p300, revision " + myRevision + ", will use "
						+ jarToUse);
		System.setProperty("launchedByP300Revision", "" + myRevision);

		D.deInit();
		System.out.flush();
		System.err.flush();

		// Load the jar
		// Create the class loader for the application jar file
		JarClassLoader cl = new JarClassLoader(jarToUse.toURI().toURL());
		// Get the application's main class name
		String name = null;
		try {
			name = cl.getMainClassName();
		} catch (IOException e) {
			System.err.println("I/O error while loading JAR file:");
			e.printStackTrace();
			System.exit(1);
		}

		if (name == null) {
			name = "de.guruz.p300.MainDialog";
		}

		// Get arguments for the application
		String[] newArgs = new String[args.length];
		System.arraycopy(args, 0, newArgs, 0, newArgs.length);
		// Invoke application's main class
		try {
			cl.invokeClass(name, newArgs);
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found: " + name);
			System.exit(1);
		} catch (NoSuchMethodException e) {
			System.out
					.println("Class does not define a 'main' method: " + name);
			System.exit(1);
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			System.exit(1);
		}
	}

	public static int getRevisionFromFilename(String fn) {
		return Integer.parseInt(fn.replace(".jar", ""));
	}

	public static int getRevisionFromFilename(File f) {
		return getRevisionFromFilename(f.getName());
	}

}
