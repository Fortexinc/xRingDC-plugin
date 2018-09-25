package com.fortex.xRingDC.plugin.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fortex.xRingDC.plugin.api.BaseExecutionReportResponse;
import com.fortex.xRingDC.plugin.model.UserModel;

import quickfix.SessionID;

public class SessionCache {
	private static final Map<String, SessionID> USER_NAME_SESSION_CACHE = new HashMap<String, SessionID>();
	private static final Map<SessionID, UserModel> SESSIONID_ACCOUNT_CACHE = new ConcurrentHashMap<SessionID, UserModel>();
	private static final Map<SessionID, BaseExecutionReportResponse> SESSIONID_EXECUTION_REPORT_RESPONSE_CACHE = new HashMap<SessionID, BaseExecutionReportResponse>();
	
	public static void putExecutionReportResponse(SessionID sessionID, BaseExecutionReportResponse executionReportResponse) {
		SESSIONID_EXECUTION_REPORT_RESPONSE_CACHE.put(sessionID, executionReportResponse);
	}
	
	public static void removeExecutionReportResponse(SessionID sessionID) {
		SESSIONID_EXECUTION_REPORT_RESPONSE_CACHE.remove(sessionID);
	}
	
	public static BaseExecutionReportResponse getExecutionReportResponse(SessionID sessionID) {
		return SESSIONID_EXECUTION_REPORT_RESPONSE_CACHE.get(sessionID);
	} 
	
	public static void removeSessionId(SessionID sessionID) {
		SESSIONID_ACCOUNT_CACHE.remove(sessionID);
	}
	
	public static void putUserModelBySessionID(SessionID sessionID, UserModel user) {
		SESSIONID_ACCOUNT_CACHE.put(sessionID, user);
	}

	
	public static SessionID getSessionIdByUserName(String userName) {
		return USER_NAME_SESSION_CACHE.get(userName);
	}
	public static void putSessionIdByUserName(String userName, SessionID sessionID) {
		USER_NAME_SESSION_CACHE.put(userName, sessionID);
	}
	public static UserModel[] getUserModels() {
		return SESSIONID_ACCOUNT_CACHE.values().toArray(new UserModel[]{});
		/*		
		List<String> accounts = new ArrayList<String>();
		for (Entry<SessionID, UserModel> entry : SESSIONID_ACCOUNT_CACHE.entrySet()) {
			accounts.add(entry.getValue().getUserName());
		} 
		
		return accounts;
		*/
	}
	
	public static SessionID[] getAllSessionID() {
		return USER_NAME_SESSION_CACHE.values().toArray(new SessionID[]{});
	}
	public static void removeSessionIdForUserName(SessionID sessionID) {
		Iterator<Entry<String, SessionID>> it = USER_NAME_SESSION_CACHE.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, SessionID> entry = it.next();
			if (sessionID.equals(entry.getValue())) {
				it.remove();
				break;
			}
		}
	}
}
