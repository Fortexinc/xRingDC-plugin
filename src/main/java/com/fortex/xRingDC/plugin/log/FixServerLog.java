package com.fortex.xRingDC.plugin.log;

import org.apache.log4j.Logger;

import quickfix.Log;
import quickfix.MessageUtils;
import quickfix.SessionID;
import quickfix.SystemTime;
import quickfix.field.converter.UtcTimestampConverter;
import quickfix.fix44.Heartbeat;

public class FixServerLog implements Log{
	private boolean isLogIncoming;
	private boolean isLogOutgoing;
	private boolean isLogHeartBeat;
	private boolean isLogAdminMsg;
	
	private static final String EVENT_CATEGORY = "Event";

    private static final String EVENT_ERROR_CATEGORY = "EventError";

    private static final String OUTGOING_CATEGORY = "Outgoing";

    private static final String INCOMING_CATEGORY = "Incoming";
    
    private static final String HEART_BEAT_CATEGORY = "HeartBeat";
    
    private static final String ADMIN_MSG_CATEGORY = "AdminMsg";
	
    private Logger incomingMsgLog;

    private Logger outgoingMsgLog;
    
    private Logger heartbeatLog;
    
    private Logger eventLog;
    
    private Logger eventErrorLog;
    
    private Logger adminMsgLog;
    
    private SessionID sessionID;
    
    
	public FixServerLog(String baseDir, SessionID sessionID, boolean isLogIncoming, boolean isLogOutgoing,
			boolean isLogHeartBeat, boolean isLogAdminMsg) {
		this.isLogIncoming = isLogIncoming;
		this.isLogOutgoing = isLogOutgoing;
		this.isLogHeartBeat = isLogHeartBeat;
		this.isLogAdminMsg = isLogAdminMsg;
		this.incomingMsgLog = Logger.getLogger(INCOMING_CATEGORY);
		this.outgoingMsgLog = Logger.getLogger(OUTGOING_CATEGORY);
		this.heartbeatLog = Logger.getLogger(HEART_BEAT_CATEGORY);
		this.eventErrorLog = Logger.getLogger(EVENT_ERROR_CATEGORY);
		this.eventLog = Logger.getLogger(EVENT_CATEGORY);
		this.adminMsgLog = Logger.getLogger(ADMIN_MSG_CATEGORY);
		this.sessionID = sessionID;
	}
    
	@Override
	public void clear() {
		
	}

	@Override
	public void onIncoming(String message, String msgType) {
		String msg = "<" + UtcTimestampConverter.convert(SystemTime.getDate(), false) + ", " + sessionID + ", "
                + INCOMING_CATEGORY + "> (" + message + ")";
		boolean isAdminMessage = MessageUtils.isAdminMessage(msgType);
		if (Heartbeat.MSGTYPE.equals(msgType)) {
			if(isLogHeartBeat) {
				heartbeatLog.info(msg);
			} 
		} else if (msgType != null && isLogAdminMsg && isAdminMessage) {
			adminMsgLog.info(msg);
		} else if (isLogIncoming && !isAdminMessage) {
			incomingMsgLog.info(msg);
		}
		
	}

	@Override
	public void onOutgoing(String message, String msgType) {
		String msg = "<" + UtcTimestampConverter.convert(SystemTime.getDate(), false) + ", " + sessionID + ", "
                + OUTGOING_CATEGORY + "> (" + message + ")";
		boolean isAdminMessage = MessageUtils.isAdminMessage(msgType);
		if (Heartbeat.MSGTYPE.equals(msgType)) {
			if(isLogHeartBeat) {
				heartbeatLog.info(msg);
			}
		} else if (msgType != null && isLogAdminMsg && isAdminMessage)  {
			adminMsgLog.info(msg);
		} else if (isLogOutgoing && !isAdminMessage) {
			outgoingMsgLog.info(msg);
		}
	}
	
	
	@Override
	public void onEvent(String message) {
		String msg = "<" + UtcTimestampConverter.convert(SystemTime.getDate(), false) + ", " + sessionID + ", "
                + EVENT_CATEGORY + "> (" + message + ")";
		eventLog.info(msg);
	}

	@Override
	public void onErrorEvent(String message) {
		String msg = "<" + UtcTimestampConverter.convert(SystemTime.getDate(), false) + ", " + sessionID + ", "
                + EVENT_ERROR_CATEGORY + "> (" + message + ")";
		eventErrorLog.error(msg);
	}

}
