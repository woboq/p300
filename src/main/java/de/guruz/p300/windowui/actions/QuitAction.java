package de.guruz.p300.windowui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class QuitAction extends AbstractAction {

	public QuitAction() {
		super("Quit p300");
	}
	
	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}

}
