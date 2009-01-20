package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;

public class AddShareAction extends AbstractAction {

	public AddShareAction() {
		super("Add A Shared Folder", IconChooser.getAddIcon());
	}


	public void actionPerformed(ActionEvent arg0) {
		MainDialog md = MainDialog.getInstance();
		md.showSubWindow(null, "Configuration",
				md.configurationPanel);
		md.configurationPanel.addShare ();
	}

}
