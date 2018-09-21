package com.fortex.xRingDC.plugin.api;

import com.fortex.xRingDC.plugin.model.FortexMsg;
import com.fortex.xRingDC.plugin.model.Subscriber;
import com.fortex.xRingDC.plugin.model.Subscriber.ENUM_GIVEUPBY;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.NoPartyIDs;
import quickfix.field.PartyID;
import quickfix.field.PartyRole;
import quickfix.field.Side;

public abstract class BaseExecutionReportResponse{
	
	public abstract Message generateExecutionReportMessage(SessionID sessionID, FortexMsg fortexMsg, Subscriber subcriber);
	
	protected void changeMsgSide(Message fixMsg){
		try {
			fixMsg.setString(Side.FIELD, ("BUY".equals(fixMsg.getString(Side.FIELD).toUpperCase()) ? "SELL" : "BUY"));
		} catch (FieldNotFound e) {
		}
	}
	
	protected void changeMsgSide(FortexMsg msg, Message fixMsg, Subscriber value){
		if(msg.msgType == FortexMsg.MSGTYPE.CLIENTMSG && !value.isClientLegChangeSide()){
			changeMsgSide(fixMsg);	
		}
	}
	
	protected void changeMsgPartyIDs(FortexMsg msg, Message fixMsg,Subscriber subscriber){
		String clientIDGiveupAs = (subscriber.getGiveupAs().equals("") ? msg.PB : subscriber.getGiveupAs());
		String LPValue = msg.LP;
		String clientValue = "";
						
		ENUM_GIVEUPBY giveupBy = ENUM_GIVEUPBY.toValue(subscriber.getGiveupBy());  

		switch(giveupBy) {
		case ACCT :
			clientValue = msg.acct;
			break;
		case CLIENT:
		default:				
			clientValue = msg.client;
			break;
		}
		setMsgPartyIDs(msg, fixMsg, subscriber, clientIDGiveupAs, LPValue, clientValue);
	}
	
	protected void setMsgPartyIDs(FortexMsg msg,Message fixMsg, Subscriber subscriber, String clientIDGiveupAs, String LPValue, String clientValue){
		
		fixMsg.removeGroup(NoPartyIDs.FIELD);
		
		if(msg.msgType == FortexMsg.MSGTYPE.CLIENTMSG && !subscriber.isClientLegChangeSide()){
//	the subscriber wants to see the trade from the client's perspective, i.e., client vs broker
			String giveupAs = clientValue;
			clientValue = clientIDGiveupAs;
			clientIDGiveupAs = giveupAs;
		}
//	the clientID value should be the broker, pb, or whatever, two legs are as Broker vs LP and Broker vs Client		
		quickfix.Group partyIDGroup1 = new quickfix.fix44.ExecutionReport.NoPartyIDs();
		partyIDGroup1.setString(PartyID.FIELD, clientIDGiveupAs);				
		partyIDGroup1.setInt(PartyRole.FIELD, PartyRole.CLIENT_ID);
		fixMsg.addGroup(partyIDGroup1);
		
		quickfix.Group partyIDGroup2 = new quickfix.fix44.ExecutionReport.NoPartyIDs();
		partyIDGroup2.setString(PartyID.FIELD, msg.msgType == FortexMsg.MSGTYPE.LPMSG ? LPValue : clientValue);
		partyIDGroup2.setInt(PartyRole.FIELD, PartyRole.CONTRA_FIRM);
		fixMsg.addGroup(partyIDGroup2);
	}
}
