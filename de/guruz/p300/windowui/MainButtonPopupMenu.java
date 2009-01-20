package de.guruz.p300.windowui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.guruz.p300.windowui.actions.AddDowloadAction;
import de.guruz.p300.windowui.actions.AddHostAction;
import de.guruz.p300.windowui.actions.AddShareAction;
import de.guruz.p300.windowui.actions.ChatWithOtherUsersAction;
import de.guruz.p300.windowui.actions.ConfigurationAction;
import de.guruz.p300.windowui.actions.ConsoleAction;
import de.guruz.p300.windowui.actions.DonationsAction;
import de.guruz.p300.windowui.actions.EarnAction;
import de.guruz.p300.windowui.actions.GetHelpAction;
import de.guruz.p300.windowui.actions.QuitAction;
import de.guruz.p300.windowui.actions.SendFeatureRequestAction;
import de.guruz.p300.windowui.actions.SendFeedbackAction;
import de.guruz.p300.windowui.actions.ShowDownloadsAction;
import de.guruz.p300.windowui.actions.UploadsAction;

public class MainButtonPopupMenu extends JPopupMenu implements
PopupMenuListener {
	protected MainButtonPopupMenu() {
		super ("p300");
		this.addPopupMenuListener(this);
	}

	private static MainButtonPopupMenu i = null;

	public static synchronized MainButtonPopupMenu instance() {
		if (MainButtonPopupMenu.i == null) {
			MainButtonPopupMenu.i = new MainButtonPopupMenu();
		}

		return MainButtonPopupMenu.i;
	}

	public void popupMenuCanceled(PopupMenuEvent arg0) {
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
		this.removeAll();
		
		add(new AddHostAction());
		add(new AddShareAction());
		add(new AddDowloadAction());
		addSeparator();
		add(new ShowDownloadsAction());
		add(new UploadsAction());
		addSeparator();
		add(new ConfigurationAction());
		add(new ConsoleAction());
		
		JMenu clipboardItem = new JMenu ("Copy own URL to clipboard");
		LocalURLsPopupMenu.addItems (clipboardItem);
		add (clipboardItem);
		
		addSeparator();
		add(new GetHelpAction());
		add(new ChatWithOtherUsersAction());
		add(new SendFeedbackAction());
		add(new SendFeatureRequestAction());
		add(new DonationsAction());
		add(new EarnAction());
		addSeparator();
		add(new QuitAction());
		
		

		
	}



}
