package com.fortex.xRingDC.plugin.subcribers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.fortex.xRingDC.plugin.api.BaseExecutionReportResponse;
import com.fortex.xRingDC.plugin.cache.SessionCache;
import com.fortex.xRingDC.plugin.model.FortexMsg;
import com.fortex.xRingDC.plugin.model.Subscriber;
import com.fortex.xRingDC.plugin.utils.ConfigSetting;

import quickfix.Message;
import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SystemTime;
import quickfix.field.BeginString;
import quickfix.field.MsgSeqNum;
import quickfix.field.OrigSendingTime;
import quickfix.field.PossDupFlag;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.TargetCompID;

public class SubscriberFilter {
	
	private static HashMap<String, Subscriber> LP_LEG = new HashMap<String, Subscriber>();
	private static HashMap<String, Subscriber> CLIENT_LEG = new HashMap<String, Subscriber>();
	private static HashMap<String, TreeMap<String, Subscriber>> FILTER_BY_ACCOUNT = new HashMap<String, TreeMap<String, Subscriber>>();	
	private static HashMap<String, TreeMap<String, Subscriber>> FILTER_BY_DOMAIN = new HashMap<String, TreeMap<String, Subscriber>>();
	private static HashMap<String, TreeMap<String, Subscriber>> FILTER_BY_LP = new HashMap<String, TreeMap<String, Subscriber>>();	
	private static HashMap<String, TreeMap<String, Subscriber>> FILTER_BY_PB = new HashMap<String, TreeMap<String, Subscriber>>();
	private MessageStoreFactory messageStoreFactory;
	
	public SubscriberFilter(MessageStoreFactory messageStoreFactory) {
		this.messageStoreFactory = messageStoreFactory;
	}
	
	public static void resetSettings() {
		LP_LEG.clear();
		CLIENT_LEG.clear();
		FILTER_BY_ACCOUNT.clear();
		FILTER_BY_DOMAIN.clear();
		FILTER_BY_LP.clear();
		FILTER_BY_PB.clear();
	}
	
	public static void putSessionForLpLeg(String subKey, Subscriber subscriber) {
		LP_LEG.put(subKey, subscriber);
	}
	
	public static Subscriber getSessionForLpLeg(String subKey) {
		return LP_LEG.get(subKey);
	}
	
	public static void putSessionForClientLeg(String subKey, Subscriber subscriber) {
		CLIENT_LEG.put(subKey, subscriber);
	}
	
	public static Subscriber getSessionForClientLeg(String subKey) {
		return CLIENT_LEG.get(subKey);
	}
	
	public static void putSubscriberForFilterByLp(String lp, Subscriber subscriber) {
		if (FILTER_BY_LP.get(lp) == null) {
			FILTER_BY_LP.put(lp, new TreeMap<String, Subscriber>());
		}
		FILTER_BY_LP.get(lp).put(subscriber.getSubscriberName(), subscriber);
	}
	
	public static TreeMap<String, Subscriber> getSubscriberForFilterByLp(String lp) {
		return FILTER_BY_LP.get(lp);
	}

	public static void putSubscriberForFilterByPB(String pb, Subscriber subscriber) {
		if (FILTER_BY_PB.get(pb) == null) {
			FILTER_BY_PB.put(pb, new TreeMap<String, Subscriber>());
		}
		FILTER_BY_PB.get(pb).put(subscriber.getSubscriberName(), subscriber);	
	}
	
	public static TreeMap<String, Subscriber> getSubscriberForFilterByPB(String pb) {
		return FILTER_BY_PB.get(pb);
	}
	
	
	public static void putSubscriberForFilterByDomain(String domain, Subscriber subscriber) {
		if (FILTER_BY_DOMAIN.get(domain) == null) {
			FILTER_BY_DOMAIN.put(domain, new TreeMap<String, Subscriber>());
		}
		FILTER_BY_DOMAIN.get(domain).put(subscriber.getSubscriberName(), subscriber);
	}
	
	public static TreeMap<String, Subscriber> getSubscriberForFilterByDomain(String domain) {
		return FILTER_BY_DOMAIN.get(domain);
	}
	
	public static void putSubscriberForFilterByAccount(String account, Subscriber subscriber) {
		if (FILTER_BY_ACCOUNT.get(account) == null) {
			FILTER_BY_ACCOUNT.put(account, new TreeMap<String, Subscriber>());
		}
		FILTER_BY_ACCOUNT.get(account).put(subscriber.getSubscriberName(), subscriber);
	}
	
	public static TreeMap<String, Subscriber> getSubscriberForFilterByAccount(String account) {
		return FILTER_BY_ACCOUNT.get(account);
	}
	
	private static boolean isSubscriber(String subKey, String subject, HashMap<String, TreeMap<String, Subscriber>> subscriptions){
		TreeMap<String, Subscriber> subscribers = subscriptions.get("*");
//	check for wildcard inclusion and specific exclusion		
		if(subscribers != null
			&& subscribers.containsKey(subKey) 
			&& ((subscribers = subscriptions.get("-" + subject)) == null 
				|| !subscribers.containsKey(subKey))){
				return true;
		} 
//	check for specific inclusion		
		if((subscribers = subscriptions.get(subject)) != null){
			return subscribers.containsKey(subKey);
		}
		return false;		
	}
	
	private static boolean isAccountSubscriber(String subKey, String account){
		return isSubscriber(subKey, account, FILTER_BY_ACCOUNT);		
	}
	
	private static boolean isDomainSubscriber(String subKey, String domain){
		return isSubscriber(subKey, domain, FILTER_BY_DOMAIN);
	}
	
	private static boolean isLPSubscriber(String subKey, String lp){
		return isSubscriber(subKey, lp, FILTER_BY_LP);
	}
		
	private static boolean isPBSubscriber(String subKey, String pb){
		return isSubscriber(subKey, pb, FILTER_BY_PB);
	}

	private static boolean isSubscriber(String subKey, FortexMsg msg){		
		return isPBSubscriber(subKey, msg.PB)
				&& isLPSubscriber(subKey, msg.LP) 
				&& isDomainSubscriber(subKey, msg.domain)
				&& isAccountSubscriber(subKey, msg.acct);
	}
	
	
	
	public void sendToClientForSubscriber(FortexMsg msg, String senderCompId) throws SQLException{
		try {
			HashMap<String, Subscriber> subscriptions = (msg.msgType == FortexMsg.MSGTYPE.LPMSG ? LP_LEG : CLIENT_LEG);
			for(Subscriber subscriber : subscriptions.values()){
				SessionID sessionID = SessionCache.getSessionIdByUserName(subscriber.getUserName());
				BaseExecutionReportResponse response = SessionCache.getExecutionReportResponse(sessionID);
				if (response != null) {
					Message fixMsg = response.generateExecutionReportMessage(sessionID, msg, subscriber);
					Session session = null;
					if (sessionID != null && (session = Session.lookupSession(sessionID)) != null) {
						if(isSubscriber(subscriber.getSubscriberName(), msg) && session != null){
							session.send(fixMsg);
						}
					} else {
						MessageStore store = this.messageStoreFactory.create(sessionID);
						try {
							if(isSubscriber(subscriber.getSubscriberName(), msg)) {
								fixMsg.getHeader().removeField(PossDupFlag.FIELD);
								fixMsg.getHeader().removeField(OrigSendingTime.FIELD);
								Message.Header header = fixMsg.getHeader();
								header.setString(BeginString.FIELD, sessionID.getBeginString());
						        header.setString(SenderCompID.FIELD, sessionID.getSenderCompID());
						        header.setString(TargetCompID.FIELD, sessionID.getTargetCompID());
						        int senderMsgSeqNum = store.getNextSenderMsgSeqNum();
						        header.setInt(MsgSeqNum.FIELD, senderMsgSeqNum);
						        header.setUtcTimeStamp(SendingTime.FIELD, SystemTime.getDate(), true);
								store.set(senderMsgSeqNum, fixMsg.toString());
								store.incrNextSenderMsgSeqNum();
							}
						} catch (IOException e) {
							Logger.getLogger("EventError").error(e.getMessage(), e);
						}
					}
				}
			}
			ConfigSetting.updateProperty(ConfigSetting.PropertyType.TYPE_SEQNO, "seqno", msg.seqno2);			
		} catch (Exception e) {
			Logger.getLogger("EventError").error(e.getMessage(), e);
		}
	}
}
