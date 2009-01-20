package de.guruz.p300.utils;

import java.io.File;

public class Mime {

	public static String getMIMEType (String fn) {
		String rfn = fn.toLowerCase();
		
		
		if (Mime.isExtensionOf(rfn, Mime.imgExts)) {
			return "image/";
		}
		else if (Mime.isAudioFileName(rfn)) {
			return "audio/";
		}
		else if (Mime.isExtensionOf(rfn, Mime.compExts)) { 
			return "application/octet-stream";
		}
		else if (Mime.isExtensionOf(rfn, Mime.movExts)) {
			return "video/";
		}
		
		return "application/octet-stream";
	}

	// Extensions for various file formats
	static String[] dirExts   = {"/"};
	static String[] imgExts   = {"gif", "jpeg", "jpg", "bmp", "png"};
	static String[] audioExts = {"mp3", "wav", "mod", "aac", "ogg", "wma"};
	static String[] compExts  = {"zip", "gz", "rar", "ace", "cab", "bz2"};
	static String[] movExts   = {"avi", "mpeg", "mpg", "mov", "mkv", "mp4", "ogm", "rm", "swf", "wmv", "flv"};
	// Check if filename ends with some extension in String array
	public static boolean isExtensionOf(String filename, String[] exts) {
		for (String ext: exts) {
			if (filename.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}
	public static String getMIMEType (File f) {
		return getMIMEType (f.getName ());
	}
	public static boolean isAudioFileName(String filename) {
		return isExtensionOf(filename, audioExts);
	}
	/** Check if filename is parent of current directory, e.g. its name is ..
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean isParent(String filename) {
		return filename.equals (Mime.PARENT_STRING);
	}

	// What does the parent directory look like?
	static final String PARENT_STRING = "../";

}
