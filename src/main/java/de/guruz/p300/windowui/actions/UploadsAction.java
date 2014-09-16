package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;

public class UploadsAction extends AbstractAction {

	public UploadsAction() {
		super("Uploads", IconChooser.getUploadsIcon());
	}
	
	public void actionPerformed(ActionEvent e) {
		MainDialog.getInstance().showSubWindow(null, "Uploads",
				MainDialog.getInstance().uploadsPanel);
	}

}
