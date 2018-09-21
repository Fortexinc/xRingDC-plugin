package com.fortex.xRingDC.plugin.core;

import org.apache.log4j.Logger;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.MsgDirection;
import quickfix.field.MsgType;
import quickfix.field.RefMsgType;
import quickfix.field.ResetSeqNumFlag;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.Logon;
import quickfix.fix44.Logout;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.Reject;
import quickfix.fix44.ResendRequest;
import quickfix.fix44.SequenceReset;
import quickfix.fix44.TestRequest;

public abstract class AcceptorApplication extends MessageCracker implements Application {
	
	@Override
	public void onCreate(SessionID sessionId) {

	}
	
	public abstract int[] getLoginType();

//	protected abstract Session getServerSession();

	//protected abstract void doFromApp(Message message, SessionID sessionID)
	//		throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType;

	protected abstract void responseMsgTypes(Message message);

	/**
	 * 
	 * <p>Description:Set fields to response for logon</p> 
	 *
	 * @author Patrick Chi
	 * @date 2017-03-27 
	 * @param message
	 */
	private void responseCommonMsgTypes(Message message) {
		Group groupHeartbeatSend = new Logon.NoMsgTypes();
		groupHeartbeatSend.setString(RefMsgType.FIELD, Heartbeat.MSGTYPE);
		groupHeartbeatSend.setChar(MsgDirection.FIELD, MsgDirection.SEND);
		message.addGroup(groupHeartbeatSend);
		
		Group groupHeartbeatReceive = new Logon.NoMsgTypes();
		groupHeartbeatReceive.setString(RefMsgType.FIELD, Heartbeat.MSGTYPE);
		groupHeartbeatReceive.setChar(MsgDirection.FIELD, MsgDirection.RECEIVE);
		message.addGroup(groupHeartbeatReceive);
		
		Group groupTestRequestSend = new Logon.NoMsgTypes();
		groupTestRequestSend.setString(RefMsgType.FIELD,  TestRequest.MSGTYPE);
		groupTestRequestSend.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(groupTestRequestSend);
		
		Group groupTestRequestReceive = new Logon.NoMsgTypes();
		groupTestRequestReceive.setString(RefMsgType.FIELD,  TestRequest.MSGTYPE);
		groupTestRequestReceive.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(groupTestRequestReceive);
		
		Group groupRequestResendSend = new Logon.NoMsgTypes();
		groupRequestResendSend.setString(RefMsgType.FIELD,  ResendRequest.MSGTYPE);
		groupRequestResendSend.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(groupRequestResendSend);
		
		Group groupRequestResendReceive = new Logon.NoMsgTypes();
		groupRequestResendReceive.setString(RefMsgType.FIELD,  ResendRequest.MSGTYPE);
		groupRequestResendReceive.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(groupRequestResendReceive);
		
		Group groupRejectSend = new Logon.NoMsgTypes();
		groupRejectSend.setString(RefMsgType.FIELD,  Reject.MSGTYPE);
		groupRejectSend.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(groupRejectSend);
		
		Group groupRejectReject = new Logon.NoMsgTypes();
		groupRejectReject.setString(RefMsgType.FIELD,  Reject.MSGTYPE);
		groupRejectReject.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(groupRejectReject);
		
		Group sequenceResetSend = new Logon.NoMsgTypes();
		sequenceResetSend.setString(RefMsgType.FIELD,  SequenceReset.MSGTYPE);
		sequenceResetSend.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(sequenceResetSend);
		
		Group sequenceResetReceive = new Logon.NoMsgTypes();
		sequenceResetReceive.setString(RefMsgType.FIELD,  SequenceReset.MSGTYPE);
		sequenceResetReceive.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(sequenceResetReceive);
		
		Group logonSend = new Logon.NoMsgTypes();
		logonSend.setString(RefMsgType.FIELD,  Logon.MSGTYPE);
		logonSend.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(logonSend);
		
		Group logonReceive = new Logon.NoMsgTypes();
		logonReceive.setString(RefMsgType.FIELD,  Logon.MSGTYPE);
		logonReceive.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(logonReceive);
		
		Group logoutReceive = new Logon.NoMsgTypes();
		logoutReceive.setString(RefMsgType.FIELD,  Logout.MSGTYPE);
		logoutReceive.setChar(MsgDirection.FIELD,  MsgDirection.RECEIVE);
		message.addGroup(logoutReceive);
		
		Group logoutSend = new Logon.NoMsgTypes();
		logoutSend.setString(RefMsgType.FIELD,  Logout.MSGTYPE);
		logoutSend.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(logoutSend);
		
		Group businessReject = new Logon.NoMsgTypes();
		businessReject.setString(RefMsgType.FIELD,  BusinessMessageReject.MSGTYPE);
		businessReject.setChar(MsgDirection.FIELD,  MsgDirection.SEND);
		message.addGroup(businessReject);
		
		responseMsgTypes(message);
	}

	@Override
	public void onLogout(SessionID sessionID) {
		
		Logger.getLogger("Event").info(sessionID + " logouted.");
	}

	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		String msgType;
		try {
			msgType = message.getHeader().getString(MsgType.FIELD);
			if(msgType.equals(MsgType.LOGON) && message.isSetField(ResetSeqNumFlag.FIELD)) {
				
				responseCommonMsgTypes(message);
			}
		} catch (FieldNotFound e) {
			Logger.getLogger("EventError").error(e.getMessage(), e);
		}
		
	}
	
	@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {

	}
}
