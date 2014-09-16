package de.guruz.p300.utils;

import de.guruz.p300.Configuration;

public class OsUtils {

	/**
	 * Return the operating system name
	 * 
	 * @author guruz
	 * @return A string containg the OS name (e.g. "Windows", "Mac", "Linux")
	 * @see OsUtils#isWindows()
	 * @see OsUtils#isOSX()
	 * @see OsUtils#isLinux()
	 * @see Configuration#getJavaVersion()
	 */
	public static String getOS() {
		return (System.getProperty("os.name"));
	}

	/**
	 * Check if this is a Microsoft Windows system
	 * 
	 * @return True: Is a Windows system; False: Is not Windows
	 * @author guruz
	 * @see OsUtils#isOSX()
	 * @see OsUtils#isLinux()
	 * @see Configuration#getJavaVersion()
	 * @see getOS
	 */
	public static boolean isWindows() {
		return (System.getProperty("os.name").startsWith("Windows"));
	}

	/**
	 * Check if this is an Apple Mac system
	 * 
	 * @return True: Is an Apple Mac system; False: Is not Mac
	 * @author guruz
	 * @see OsUtils#isLinux()
	 * @see Configuration#getJavaVersion()
	 * @see getOS
	 * @see isWindows
	 */
	public static boolean isOSX() {
		return (System.getProperty("os.name").startsWith("Mac"));
	}

	/**
	 * Check if this is a GNU/Linux system
	 * 
	 * @return True: Is a GNU/Linux system; False: Is not Linux
	 * @author guruz
	 * @see Configuration#getJavaVersion()
	 * @see getOS
	 * @see isWindows
	 * @see isOSX
	 */
	public static boolean isLinux() {
		return (System.getProperty("os.name").startsWith("Linux"));
	}

}
