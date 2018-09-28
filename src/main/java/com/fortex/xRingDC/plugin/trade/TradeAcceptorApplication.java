package com.fortex.xRingDC.plugin.trade;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import com.fortex.lib.globalservices.GlobalRuntime;
import com.fortex.xRingDC.plugin.cache.SessionCache;
import com.fortex.xRingDC.plugin.core.AcceptorApplication;
import com.fortex.xRingDC.plugin.core.PluginClassLoader;
import com.fortex.xRingDC.plugin.db.DBOperation;
import com.fortex.xRingDC.plugin.model.UserModel;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.BusinessRejectReason;
import quickfix.field.MsgDirection;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.Password;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.Text;
import quickfix.field.Username;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Logon;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.OrderCancelRequest;

public class TradeAcceptorApplication extends AcceptorApplication {
	
	private static final int[] TRADE_LOGIN_TYPE = new int[]{2, 10};

	public TradeAcceptorApplication(SessionSettings settings) {

	}

	/* (non-Javadoc)
	 * @author Ivan Huo
	 * @see com.fortex.quickRing.AcceptorApplication#setLoginType()
	 */
	@Override
	public int[] getLoginType() {
		return TRADE_LOGIN_TYPE;
	}

	


	@Override
	protected void responseMsgTypes(Message message) {
		Group newOrderSingle = new Logon.NoMsgTypes();
		newOrderSingle.setString(RefMsgType.FIELD,  NewOrderSingle.MSGTYPE);
		newOrderSingle.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(newOrderSingle);
		
		Group orderCancelRequest = new Logon.NoMsgTypes();
		orderCancelRequest.setString(RefMsgType.FIELD,  OrderCancelRequest.MSGTYPE);
		orderCancelRequest.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(orderCancelRequest);
		
		Group executionReport = new Logon.NoMsgTypes();
		executionReport.setString(RefMsgType.FIELD,  ExecutionReport.MSGTYPE);
		executionReport.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(executionReport);
		
		Group orderCancelReject = new Logon.NoMsgTypes();
		orderCancelReject.setString(RefMsgType.FIELD,  OrderCancelReject.MSGTYPE);
		orderCancelReject.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(orderCancelReject);
	}

	@Override
	public void onLogon(SessionID sessionId) {
		
	}

	@Override
	public void onLogout(SessionID sessionID) {
		super.onLogout(sessionID);
		SessionCache.removeSessionIdForUserName(sessionID);
		SessionCache.removeSessionId(sessionID);
	}
	
	@Override
	public void fromAdmin(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {	
		try {
			
			
			String msgType = message.getHeader().getString(MsgType.FIELD);
			if (msgType.equals(MsgType.LOGON)) {
				String userName = message.getString(Username.FIELD);
				//if (SessionCache.getSessionIdByAccount(userName) == null) {
				if (!message.isSetField(Username.FIELD) || !message.isSetField(Password.FIELD)) {
					throw new RejectLogon("User name or password is not set.");
				}
				String password = message.getString(Password.FIELD);
				String targetId = sessionId.getTargetCompID();
				
				UserModel user = DBOperation.getUserInfo(userName, getLoginType());
				if (user == null) {
					throw new RejectLogon("User " + userName + " does not exist.");
				} else if (!user.getPasswordHash().equals(GlobalRuntime.hashPwd(password))) {
					throw new RejectLogon("The password of user " + userName + " is incorrect.");
				} else if (!user.getTargetId().equals(targetId)) {
					throw new RejectLogon("The SenderCompID " + targetId +  " of user " + userName + " is incorrect.");
				} else {
					String pluginFilePath = System.getProperty("user.dir") + File.separator + "plugin" + File.separator
					+ sessionId.getTargetCompID() + ".jar";
					if (new File(pluginFilePath).exists()) {
						if (SessionCache.getExecutionReportResponse(sessionId) == null) {
							PluginClassLoader.loadClass(pluginFilePath, sessionId);
						}
						SessionCache.putUserModelBySessionID(sessionId, user);
						SessionCache.putSessionIdByUserName(userName, sessionId);
					}
				}
				
				if (user != null && Session.lookupSession(sessionId) != null) {
					String ip = Session.lookupSession(sessionId).getRemoteAddress();
					if (ip.indexOf("/") != -1)
						ip = ip.substring(ip.indexOf("/") + 1);
					user.setIp(ip);
					user.setLogonTime(new Date());
				}
			}
		} catch (Exception e) {
			Logger.getLogger("EventError").error(e.getMessage(), e);
			throw new RejectLogon("Failed to login, unknown error in service");
		}

	}
	
	private Message generateBusinessReject(Session session, Message message, String text) throws FieldNotFound {
		Message msg = session.getMessageFactory().create(session.getSessionID().getBeginString(),
				BusinessMessageReject.MSGTYPE);
		msg.setString(RefSeqNum.FIELD, message.getHeader().getString(MsgSeqNum.FIELD));
		msg.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
		msg.setInt(BusinessRejectReason.FIELD, BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE);
		msg.setString(Text.FIELD, text);
		return msg;
	}

	@Override
	public void fromApp(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		Session session = Session.lookupSession(sessionId);
		if (session != null)
			session.send(generateBusinessReject(session, message, "Unsupported message type:" + message.getHeader().getString(MsgType.FIELD)));
		
	}
}
