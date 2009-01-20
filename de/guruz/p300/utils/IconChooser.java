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


import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.guruz.p300.Resources;
import de.guruz.p300.dirbrowser.RemoteEntity;

/**
 * This class contains functions for determining an appropriate icon image for a certain file
 */ 
public class IconChooser {
	// HTML code for images
	private static String[] imgTagSrc = {"<img src='",  "' class='icon'>"};
	
	// Icons for various file formats
	private static String parentIconHtmlTag  = IconChooser.iconImgTag ("/back.gif");
	private static String folderIconHtmlTag  = IconChooser.iconImgTag ("/folder.gif");
	private static String imgIconHtmlTag     = IconChooser.iconImgTag ("/image2.gif");
	private static String audioIconHtmlTag   = IconChooser.iconImgTag ("/sound2.gif");
	private static String compIconHtmlTag    = IconChooser.iconImgTag ("/compressed.gif");
	private static String movIconHtmlTag     = IconChooser.iconImgTag ("/movie.gif");
	private static String genericIconHtmlTag = IconChooser.iconImgTag ("/generic.gif");
	
	
	private static ImageIcon folderImageIcon = IconChooser.iconImageFromResource ("22x22/folder.png");
	private static ImageIcon imgImageIcon = IconChooser.iconImageFromResource ("22x22/image.png");
	private static ImageIcon audioImageIcon = IconChooser.iconImageFromResource ("22x22/sound.png");
	private static ImageIcon compImageIcon = IconChooser.iconImageFromResource ("22x22/compressed.png");
	private static ImageIcon movImageIcon = IconChooser.iconImageFromResource ("22x22/movie.png");
	private static ImageIcon genericImageIcon = IconChooser.iconImageFromResource ("22x22/generic.png");
	
	private static ImageIcon onlineHostImageIcon = IconChooser.iconImageFromResource ("16x16/computer.png");
	private static ImageIcon offlineHostImageIcon = IconChooser.iconImageFromResource ("16x16/computer_grey.png");
	
	private static ImageIcon sharemonkeyImageIcon = iconImageFromResource("22x22/face-monkey.png");
	
	private static ImageIcon amazonImageIcon = iconImageFromResource("22x22/amazon.png");
	
	private static ImageIcon buecherdeImageIcon = iconImageFromResource("22x22/buecher.de.png");
	
	private static ImageIcon homeImageIcon = iconImageFromResource("16x16/go-home.png");

	private static ImageIcon webinterfaceImageIcon = iconImageFromResource("16x16/applications-internet.png");
	
	private static ImageIcon addImageIcon = iconImageFromResource("16x16/list-add.png");
	
	private static ImageIcon searchImageIcon = iconImageFromResource("16x16/system-search.png");
	
	private static ImageIcon uploadsImageIcon = iconImageFromResource("16x16/user-home.png");
	
	private static ImageIcon downloadsImageIcon = iconImageFromResource("16x16/download.png");
	
	private static ImageIcon configurationImageIcon = iconImageFromResource("16x16/applications-system.png");
	private static ImageIcon consoleImageIcon = iconImageFromResource("16x16/utilities-system-monitor.png");
	private static ImageIcon donationsImageIcon = iconImageFromResource("16x16/emblem-favorite.png");
	
	
	private static ImageIcon browseImageIcon = iconImageFromResource("16x16/folder-open.png");
	private static ImageIcon chatImageIcon = iconImageFromResource("16x16/internet-mail.png");
	
	private static ImageIcon smallP300Icon = iconImageFromResource("trayicon_16x16.png");
	
	private static ImageIcon downloadImageIcon = iconImageFromResource("22x22/document-save.png");
	
	// Create HTML code from image source and a given filename
	public static String iconImgTag (String fn) {
		return IconChooser.imgTagSrc[0] + fn + IconChooser.imgTagSrc[1];
	}

	public static ImageIcon iconImageFromResource(String s) {
		String url = "de/guruz/p300/requests/static/" + s;
		URL r = Resources.getResource(url);

		return new ImageIcon (r);
	}
	
	public static ImageIcon getFolderImageIcon () {
		return IconChooser.folderImageIcon;
	}
	
	public static ImageIcon getOnlineHostImageIcon () {
		return IconChooser.onlineHostImageIcon;
	}
	
	public static ImageIcon getOfflineHostImageIcon () {
		return IconChooser.offlineHostImageIcon;
	}
	
	public static Icon getSharemonkeyIcon() {
		return sharemonkeyImageIcon;
	}
	
	public static Icon getAmazonIcon() {
		return amazonImageIcon;
	}
	
	public static Icon getBuecherDeIcon() {
		return buecherdeImageIcon;
	}
	
	public static Icon getHomeIcon() {
		return homeImageIcon;
	}
	
	public static Icon getWebinterfaceIcon() {
		return webinterfaceImageIcon;
	}
	
	public static Icon getAddIcon() {
		return addImageIcon;
	}
	
	public static Icon getSearchIcon() {
		return searchImageIcon;
	}
	
	
	public static Icon getUploadsIcon() {
		return uploadsImageIcon;
	}
	
	
	public static Icon getDownloadsIcon() {
		return downloadsImageIcon;
	}
	
	public static Icon getConfigurationIcon() {
		return configurationImageIcon;
	}

	public static Icon getConsoleIcon() {
		return consoleImageIcon;
	}

	public static Icon getDonationsIcon() {
		return donationsImageIcon;
	}
	
	public static Icon getBrowseIcon() {
		return browseImageIcon;
	}
	
	public static Icon getChatIcon() {
		return chatImageIcon;
	}
	
	
	public static ImageIcon getImageIconFromEntity (RemoteEntity re)
	{
		if (re.isDirectory())
			return IconChooser.getFolderImageIcon ();
		else
			return IconChooser.getImageIconFromFilename (re.getName());
	}
	
	
	public static ImageIcon getImageIconFromFilename (String fn) {
		String rfn = fn.toLowerCase();
		
		if (Mime.isExtensionOf(rfn, Mime.dirExts)) {
			return IconChooser.folderImageIcon;
		}
		else if (Mime.isExtensionOf(rfn, Mime.imgExts)) {
			return IconChooser.imgImageIcon;
		}
		else if (Mime.isExtensionOf(rfn, Mime.audioExts)) {
			return IconChooser.audioImageIcon;
		}
		else if (Mime.isExtensionOf(rfn, Mime.compExts)) { 
			return IconChooser.compImageIcon;
		}
		else if (Mime.isExtensionOf(rfn, Mime.movExts)) {
			return IconChooser.movImageIcon;
		}
		
		return IconChooser.genericImageIcon;
	}


	/** Convert given filename in local directory to HTML code of an image
	 * 
	 * @param fn
	 * @return
	 */
	public static String fileNameToHTMLImageTag (String fn) {
		String rfn = fn.toLowerCase();
		
		if (Mime.isParent(rfn)) {
			return IconChooser.parentIconHtmlTag;
		}
		else if (Mime.isExtensionOf(rfn, Mime.dirExts)) {
			return IconChooser.folderIconHtmlTag;
		}
		else if (Mime.isExtensionOf(rfn, Mime.imgExts)) {
			return IconChooser.imgIconHtmlTag;
		}
		else if (Mime.isExtensionOf(rfn, Mime.audioExts)) {
			return IconChooser.audioIconHtmlTag;
		}
		else if (Mime.isExtensionOf(rfn, Mime.compExts)) { 
			return IconChooser.compIconHtmlTag;
		}
		else if (Mime.isExtensionOf(rfn, Mime.movExts)) {
			return IconChooser.movIconHtmlTag;
		}
		
		return IconChooser.genericIconHtmlTag;
	}

	public static Icon getSmallP300Icon() {
		return smallP300Icon;
	}

	public static int getGuiIconWidth() {
		// TODO Auto-generated method stub
		return folderImageIcon.getIconWidth();
	}

	public static Icon getDownloadIcon() {
		return downloadImageIcon;
	}





}