package de.guruz.p300.requests;

import de.guruz.p300.http.HTTPVerb;

public class FwdRequest  extends Request {
	public static boolean couldHandle(HTTPVerb rt, String reqpath) {
		if (!((rt == HTTPVerb.GET) || (rt == HTTPVerb.HEAD))) {
			return false;
		}

		if (!reqpath.startsWith("/fwd/")) {
			return false;
		}

		return true;
	}
	
	@Override
	public void handle() throws Exception {
		this.requestThread.httpStatus(200, "OK");
		this.requestThread.httpContentType("text/html");
		this.requestThread.httpContents ();
		
		String url = "http://p300.eu";
		
		// we have this because we only want specific things forwarded
		if (this.requestThread.path.endsWith("/main"))
			url = "http://p300.eu";
		else if (this.requestThread.path.endsWith("/donate"))
			url = "http://p300.eu/donate/";
		else if (this.requestThread.path.endsWith("/earn"))
			url = "http://p300.eu/earn/";
		else if (this.requestThread.path.endsWith("/support"))
			url = "http://p300.eu/support/";
		else if (this.requestThread.path.endsWith("/new_version_loaded"))
			url = "http://p300.eu/new_version_loaded/";
		
		
		
		this.requestThread.write("<html>");
		
		// we dont do it like this because it does not preserve the referer
		//this.requestThread.write("<meta http-equiv=\"refresh\" content=\"0;url=" + url + "\">");
		
		this.requestThread.write("<body>");
		this.requestThread.write("<html><body>");
		this.requestThread.write("<a href=\"" + url + "\">click here</a>");
		this.requestThread.write("<script type='text/javascript'>");
		this.requestThread.write("window.location = \"" + url + "\";");
		this.requestThread.write("</script>");
		this.requestThread.write("</body></html>");
		
		this.requestThread.flush();
		this.requestThread.close();

	}

}
