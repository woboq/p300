package de.guruz.p300.utils.launchers;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JTextField;

import de.guruz.p300.logging.D;
import de.guruz.p300.utils.OsUtils;

public class FileManagerLaunch {
	public static void openDirectory(String d) {

		D.out("Opening " + d);

		if (OsUtils.isWindows()) {
			BareBonesBrowserLaunch.openURL("file://" + d);
		} else if (OsUtils.isOSX()) {
			String u = new File(d).toURI().toASCIIString();
			BareBonesBrowserLaunch.openURL(u);

			// gescheit escapen?

			// tell application "Finder" to open location "http://www.tuaw.com"
		} else {

			// JOptionPane.showConfirmDialog(MainDialog.getWindow(), d);
//
//			JOptionPane.showInputDialog(MainDialog.getWindow(), null,
//					"Location", JOptionPane.INFORMATION_MESSAGE, null,
//					new Object[] { d }, d);

			JDialog w = new JDialog ();
			JTextField tf = new JTextField (d);
			tf.setEditable(false);
			w.add(tf);
			//w.setMinimumSize(tf.getPreferredSize());
			//w.()
			w.pack();
			w.setLocationByPlatform(true);
			w.setVisible(true);
			
		}
	}
}
