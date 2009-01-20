package de.guruz.p300.onetoonechat;

import de.guruz.p300.logging.D;
import de.guruz.p300.onetoonechat.ui.UiMessageRouter;

public class LanMessageRouter {
	UiMessageRouter m_uiMessageRouter;
	
	LanMessageRemoteOutbox m_lanMessageRemoteOutbox;
	
	public void route (Message m)
	{
		
		
		if (m.getFrom() == null)
		{
			// local message
			
			if (m_lanMessageRemoteOutbox != null)
				m_lanMessageRemoteOutbox.route (m);
			
		}
		else if (m.getTo() == null)
		{
			// remote message
			if (m_uiMessageRouter != null)
				m_uiMessageRouter.route(m);
		}
		else
		{
			D.out ("Could not route message from=" + m.getFrom() + " to=" + m.getTo());
		}
	}
	
	public void setUiMessageRouter (UiMessageRouter umr)
	{
		m_uiMessageRouter = umr;
	}
	
	public void setLanMessageRemoteOutbox (LanMessageRemoteOutbox lmro)
	{
		m_lanMessageRemoteOutbox = lmro;
	}
}
