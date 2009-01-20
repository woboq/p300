package de.guruz.p300.windowui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class JAutoscrollingTextArea extends JComponent {

	protected final JScrollPane m_scrollPane;
	protected final JTextArea m_textArea;
	

	protected boolean m_autoScroll = true;
	protected final JCheckBoxMenuItem m_autoScrollMenuItem;

	public JAutoscrollingTextArea() {
		super();
		m_textArea = new JTextArea();
		m_scrollPane = new JScrollPane(m_textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		m_textArea.setEditable(false);
		m_textArea.putClientProperty("html.disable", Boolean.TRUE);
		m_textArea.setWrapStyleWord(true);
		m_textArea.setLineWrap(true);

		setLayout(new BorderLayout());
		add(m_scrollPane, BorderLayout.CENTER);
		
		
		// setup popup menu used for autoscrolling
		m_textArea.setToolTipText("Use the right mouse button to disable automatic scrolling");
		JPopupMenu textAreaPopupMenu = new JPopupMenu ();
		m_autoScrollMenuItem = new JCheckBoxMenuItem ("Automatic scrolling?", m_autoScroll);
		m_autoScrollMenuItem.addItemListener(new ItemListener () {
			public void itemStateChanged(ItemEvent e) {
				m_autoScroll = m_autoScrollMenuItem.getState();
				append(null); // scroll or not.. :)
			}});
		textAreaPopupMenu.add(m_autoScrollMenuItem);
		m_textArea.addMouseListener(new PopupListener (textAreaPopupMenu));
	}

	public void setText(String string) {
		m_textArea.setText(string);
	}

	public void append(String string) {
		if (string !=null)
			m_textArea.append(string);
		if (m_autoScroll)
			m_textArea.setCaretPosition(m_textArea.getDocument().getLength());
	}

	public void ensureSizeSmallerThan(int i) {
		if (m_textArea.getDocument().getLength() > 1024 * 512) {
			try {
				m_textArea.getDocument().remove(0, 1024 * 8);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

}
