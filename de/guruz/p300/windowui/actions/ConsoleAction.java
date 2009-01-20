package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;

public class ConsoleAction extends AbstractAction {

	public ConsoleAction() {
		super("Console", IconChooser.getConsoleIcon());
	}
	
	public void actionPerformed(ActionEvent e) {
		MainDialog.getInstance().showSubWindow(null, "Console",
				MainDialog.getInstance().consolePanel);
	}

}
