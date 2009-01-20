package de.guruz.p300.windowui.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class CopyToClipboardAction extends AbstractAction {

	final String m_clipboardText;
	
	public CopyToClipboardAction(String title, String clipboardText) {
		super(title);
		
		m_clipboardText = clipboardText;
	}
	
	public void actionPerformed(ActionEvent ae) {
		try {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

			Transferable transferableText = new StringSelection(m_clipboardText);
			cb.setContents(transferableText, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
