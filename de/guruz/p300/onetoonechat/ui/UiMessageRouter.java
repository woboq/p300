package de.guruz.p300.onetoonechat.ui;

import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.onetoonechat.Message;

public class UiMessageRouter {
	
	/*
	 * This should only be called from the LanMessageRouter, not from the UI!
	 */
	public void route (Message m)
	{
		Host h = m.getFrom();
		if (h == null)
			h = m.getTo();
		
		if (h != null)
		{
			ChatWindowMap cwm = MainDialog.getInstance().chatWindowMap;
			if (cwm != null){
				ChatComponent cc = cwm.getChatComponentFor(h);
				cc.addToConversation (m);
				
				cwm.showChatWindowFor(h);
			}
			
		}
	}
}
