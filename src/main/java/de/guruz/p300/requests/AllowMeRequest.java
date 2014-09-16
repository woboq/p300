package de.guruz.p300.requests;

import de.guruz.p300.MainDialog;
import de.guruz.p300.hosts.allowing.GetMeAllowedHelper;
import de.guruz.p300.hosts.allowing.HostAllowanceManager;
import de.guruz.p300.http.HTTPVerb;

public class AllowMeRequest extends Request {

	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (!(rt == HTTPVerb.GET)) {
			return false;
		}
		
		HostAllowanceManager hostAllowanceManager = MainDialog.getHostAllowanceManager();
		String authCookie = hostAllowanceManager.getLocalAuthCookieForImplicitAllow ();
		String path = GetMeAllowedHelper.getHttpPathForCookie (authCookie);
		
		if (!reqpath.equals(path)) {
			return false;
		}

		return true;
	}
	
	@Override
	public void handle() throws Exception {
		HostAllowanceManager hostAllowanceManager = MainDialog.getHostAllowanceManager();
		String remoteIP = requestThread.getRemoteIP();
		
		//D.out("Incoming HTTP from " + remoteIP + " with proper allow cookie! adding to implicit allows");
		hostAllowanceManager.hostDectectedAsLocal(remoteIP);   
										
		
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpContentLength(0);
		this.requestThread.httpContents();
		this.requestThread.close();
	}

}
