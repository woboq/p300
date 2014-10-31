package de.guruz.p300.requests;

import de.guruz.p300.http.HTTPVerb;

public class UnknownRequest extends Request {
	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		return true;
		
	}
	
	@Override
	public void handle() throws Exception {
		this.requestThread.close(501, "Not implemented");
	}

}
