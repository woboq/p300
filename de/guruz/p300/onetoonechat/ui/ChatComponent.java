package de.guruz.p300.onetoonechat.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import de.guruz.p300.Configuration;
import de.guruz.p300.MainDialog;
import de.guruz.p300.Resources;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.logging.D;
import de.guruz.p300.onetoonechat.Message;
import de.guruz.p300.utils.AePlayWave;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.windowui.JAutoscrollingTextArea;

public class ChatComponent extends JPanel implements ActionListener {

	private static final String SEND = "send";

	JSplitPane m_splitPane;

	JAutoscrollingTextArea m_chatOutputArea;

	JTextArea m_chatInputArea;

	JScrollPane m_chatInputAreaScrollPane;

	JPanel m_chatInputPanel;

	JButton m_chatSendButton;

	IncomingMessageAlertWindow m_alertWindow;

	Host m_host;

	public ChatComponent() {
		super();

		m_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		m_chatOutputArea = new JAutoscrollingTextArea();

		m_chatOutputArea
				.setText("Type a message and press ENTER to send\nYou can insert a new line by pressing the CTRL button and then X\n\n");

		m_chatInputArea = new JTextArea("");
		m_chatInputArea.setWrapStyleWord(true);
		m_chatInputArea.setLineWrap(true);
		m_chatInputArea.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if ((e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X)
						|| (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_S)
						|| (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER)
						|| (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					m_chatInputArea.append("\n");
					e.consume();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					actionPerformed(new ActionEvent(this, 0, ChatComponent.SEND));
					e.consume();
				}

			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			public void keyTyped(KeyEvent e) {

			}
		});

		m_chatSendButton = new JButton(IconChooser.getChatIcon());
		m_chatSendButton.setToolTipText("Send");
		m_chatSendButton.setActionCommand(ChatComponent.SEND);
		m_chatSendButton.addActionListener(this);

		m_chatInputPanel = new JPanel(new BorderLayout());
		m_chatInputAreaScrollPane = new JScrollPane(m_chatInputArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		m_chatInputPanel.add(m_chatSendButton, BorderLayout.EAST);
		m_chatInputPanel.add(m_chatInputAreaScrollPane, BorderLayout.CENTER);
		


		m_splitPane.setTopComponent(m_chatOutputArea);
		m_splitPane.setBottomComponent(m_chatInputPanel);

		setLayout(new BorderLayout());
		this.add(m_splitPane, BorderLayout.CENTER);

		setSize(400, 400);
		m_splitPane.setDividerLocation(300);
		

	}

	public void setHost(Host h) {
		m_host = h;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals(ChatComponent.SEND)) {
			String txt = m_chatInputArea.getText();

			if (txt.trim().replaceAll("\10\13", "").length() > 0) {

				// D.out ("Sending!");

				Message m = new Message(null, m_host, txt);
				MainDialog.getInstance().lanMessageRouter.route(m);
			}

			m_chatInputArea.setText("");
			
			tryToFocusInputField();
		}

	}

	public synchronized void addToConversation(Message m) {
		String ts = "[" + DateFormat.getDateTimeInstance().format(new Date())
				+ "] ";
		if (m.getFrom() == null)
			m_chatOutputArea.append(ts
					+ Configuration.instance().getLocalDisplayName() + ": "
					+ m.getText() + "\n");
		else
			m_chatOutputArea.append(ts + m.getFrom() + ": " + m.getText()
					+ "\n");


		playMessageReceivedSound ();
	}

	
	static javax.sound.sampled.Clip m_messageReceivedSoundClip = null;
	
	private static synchronized void playMessageReceivedSound() {
		if (!Configuration.instance().isPlayChatSound())
			return;
		
		java.net.URL u = Resources.getResource("de/guruz/p300/onetoonechat/ui/chat2_b.wav");		
		AePlayWave.playOneAtATime(u);
	}

	public synchronized IncomingMessageAlertWindow getAlertWindow() {
		if (m_alertWindow == null) {
			m_alertWindow = new IncomingMessageAlertWindow(this, m_host
					.getDisplayName());
		}

		return m_alertWindow;
	}

	public void tryToFocusInputField() {
		m_chatInputArea.requestFocusInWindow();
		
	}

}
