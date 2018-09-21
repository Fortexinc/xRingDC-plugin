package com.fortex.xRingDC.plugin.model;

public class FortexMsg {	
	public enum MSGTYPE {UNDEFINED, LPMSG, CLIENTMSG};
	//public Message fixMsg = null;
	public MSGTYPE msgType = MSGTYPE.UNDEFINED;
	public String client = "";
	public String acct = "";
	public String domain = "";
	public String LP = "";
	public String PB = "";
	public String seqno2 = "";
	public ReportsTradeCapture dbMsg;
}
