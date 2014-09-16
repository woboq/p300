package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.guruz.p300.MainDialog;
import de.guruz.p300.logging.D;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.windowui.ManualDownloadWindow;

public class AddDowloadAction extends AbstractAction{

	public AddDowloadAction() {
		super("Add Manual Download", IconChooser.getAddIcon());
	}

	public void actionPerformed(ActionEvent arg0) {
		D.out ("Manual add");
		
		String url = (String) JOptionPane
		.showInputDialog(
				MainDialog.getWindow(),
				"Enter a HTTP-URL (HTTPS not supported yet, HTTP-Redirects not supported yet)",
				"Add URL", JOptionPane.PLAIN_MESSAGE,
				null, null, null);
		
		if (url != null && url.trim().length() > 0)
		{
			ManualDownloadWindow mdw = new ManualDownloadWindow (url);
			mdw.setVisible (true);
			
			mdw.startDownload ();
		}
	}

}
