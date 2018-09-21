package com.fortex.xRingDC.plugin.log;

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class FixServerLogFactory implements LogFactory{
	
	private static FixServerLog logger;
	
	private static final String SETTING_LOG_INCOMING_MESSAGE = "LogIncomingMsg";
	
	private static final String SETTING_LOG_OUTGOING_MESSAGE = "LogOutgoingMsg";
	
	private static final String SETTING_LOG_HEARTBEAT_MESSAGE = "LogHeartBeatMsg";
	
	private static final String SETTING_LOG_ADMIN_MESSAGE = "LogAdminMsg";
	
	
	
	private SessionSettings settings;
	
	public FixServerLogFactory(SessionSettings settings) {
		this.settings = settings;
	}

	@Override
	public Log create() {
		throw new UnsupportedOperationException();
	}


	@Override
	public Log create(SessionID sessionID) {
		try {
			boolean isLogIncomingMsg = settings.getBool(SETTING_LOG_INCOMING_MESSAGE);
			boolean isLogOutgoingMsg = settings.getBool(SETTING_LOG_OUTGOING_MESSAGE);
			boolean isLogHeartBeatMsg = settings.getBool(SETTING_LOG_HEARTBEAT_MESSAGE);
			boolean isLogAdminMsg = settings.getBool(SETTING_LOG_ADMIN_MESSAGE);
			logger = new FixServerLog("logs", sessionID, isLogIncomingMsg, isLogOutgoingMsg, isLogHeartBeatMsg, isLogAdminMsg);
		} catch (Exception e) {
			 throw new RuntimeException(e);
		}
		return logger;
	}
}
