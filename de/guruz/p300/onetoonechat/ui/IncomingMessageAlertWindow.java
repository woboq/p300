package de.guruz.p300.onetoonechat.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class IncomingMessageAlertWindow extends JFrame implements ActionListener {

	private static final String OPEN = "open";
	Window m_chatWindow;
	ChatComponent m_chatComponent;
	
	public IncomingMessageAlertWindow (ChatComponent cc, String hostDisplayName)
	{
		this.setTitle("Incoming Message");
		this.setLayout(new BorderLayout ());
		
		JLabel label = new JLabel ("<HTML><BODY><CENTER>Message from " + hostDisplayName + "</CENTER></BODY></HTML>");
		JButton button = new JButton ("Open");
		button.setActionCommand (IncomingMessageAlertWindow.OPEN);
		button.addActionListener(this);
		
		add (label, BorderLayout.NORTH);
		add (button, BorderLayout.CENTER);
		
		setLocationByPlatform(true);
		setSize(250, 100);
		
		m_chatComponent = cc;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(IncomingMessageAlertWindow.OPEN))
		{
			m_chatWindow.setVisible(true);
			setVisible(false);
			
			m_chatComponent.tryToFocusInputField ();
		}
		
	}

	public void setChatWindow(Window w) {
		m_chatWindow = w;
		
	}

}
