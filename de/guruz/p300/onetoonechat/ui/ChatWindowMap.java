package de.guruz.p300.onetoonechat.ui;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import de.guruz.p300.hosts.Host;

public class ChatWindowMap {
	private Map<Host,Window> m_hostToWindow;
	private Map<Host,ChatComponent> m_hostToChatComponent;
	
	
	
	public ChatWindowMap() {
		super();
		m_hostToWindow = new HashMap<Host, Window>();
		m_hostToChatComponent = new HashMap<Host, ChatComponent> ();
	}
	
	public Window getChatWindowFor (Host h)
	{
		synchronized (this)
		{
			Window w = m_hostToWindow.get(h);
			
			if (w == null)
			{
				w = new JFrame ();
				
				if (w instanceof JFrame)
				{
					JFrame f = (JFrame) w;
					f.setLocationByPlatform(true);
					f.setTitle("Chat: " + h.getDisplayName());
					f.add (getChatComponentFor(h));
					
					f.setSize(600, 400);
				}
				
				
				
				m_hostToWindow.put(h, w);
			}
			
			return w;
		}
	}

	public void showChatWindowFor (Host h)
	{
		synchronized (this) {
			Window w = getChatWindowFor(h);
			
			if (!w.isVisible())
			{
				IncomingMessageAlertWindow aw = getChatComponentFor(h).getAlertWindow ();
				aw.setChatWindow (w);
				aw.setVisible(true);
				aw.toFront();
			}

			
			
		}
	}
	
	public ChatComponent getChatComponentFor (Host h)
	{
		synchronized (this) {
			ChatComponent cc = m_hostToChatComponent.get(h);
			
			if (cc == null)
			{
				cc = new ChatComponent ();
				cc.setHost (h);
				
				m_hostToChatComponent.put(h, cc);
			}
			
			return cc;
		}
		
		
	}
}
