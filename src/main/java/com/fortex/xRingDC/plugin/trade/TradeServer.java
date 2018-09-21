package com.fortex.xRingDC.plugin.trade;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.fortex.xRingDC.plugin.cache.SessionCache;
import com.fortex.xRingDC.plugin.core.AbstractServer;
import com.fortex.xRingDC.plugin.core.PluginClassLoader;
import com.fortex.xRingDC.plugin.db.DBOperation;
import com.fortex.xRingDC.plugin.db.DropcopyQuery;
import com.fortex.xRingDC.plugin.model.FortexMsg;
import com.fortex.xRingDC.plugin.model.ReportsTradeCapture;
import com.fortex.xRingDC.plugin.model.Subscriber;
import com.fortex.xRingDC.plugin.subcribers.SubscriberFilter;
import com.fortex.xRingDC.plugin.utils.ConfigSetting;
import com.fortex.xRingDC.plugin.utils.TimeSchedule;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.FileStore;
import quickfix.FixVersions;
import quickfix.RuntimeError;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class TradeServer extends AbstractServer{
 	private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
 	private static final int STOPPING_PERIOD = 3;
	
	public TradeServer(SessionSettings settings) throws Exception {
		super(settings);
	}


	/*
	 * (non-Javadoc)
	 * @see com.fortex.quickRing.AbstractServer#getAcceptorApplication()
	 */
	@Override
	protected Application getAcceptorApplication(SessionSettings settings) {
		return new TradeAcceptorApplication(settings);
	}

	/*
	 * (non-Javadoc)
	 * @see com.fortex.quickRing.AbstractServer#doExecute()
	 */
	@Override
	protected void doExecute(SessionSettings settings) {
		try {
			ScheduledExecutorService messageExecutor = Executors.newSingleThreadScheduledExecutor();
			SubscriberFilter filter = new SubscriberFilter(this.messageStoreFactory);
			messageExecutor.scheduleAtFixedRate(
					new MessageScheduler(filter, settings.getDefaultProperties().getProperty("SenderCompID")), 1,
					settings.getLong("Period"), TimeUnit.SECONDS);
			
			TimeSchedule schedule = new TimeSchedule(settings);
			ScheduledExecutorService stoppingExecutor = Executors.newSingleThreadScheduledExecutor();
			
			stoppingExecutor.scheduleAtFixedRate(new AcceptorScheduler(schedule), 1,
					STOPPING_PERIOD, TimeUnit.SECONDS);
			
			System.out.println("##############Application was started.###################");
			shutdownLatch.await();
		} catch(Exception e) {
			Logger.getLogger("EventError").error(e.getMessage(), e);
		}
	}
	
	private class MessageScheduler implements Runnable{
		private SubscriberFilter filter;
		private String senderCompID;
		MessageScheduler(SubscriberFilter filter, String senderCompID) {
			this.filter = filter;
			this.senderCompID = senderCompID;
		}
		@Override
		public void run() {
			try {
				String path = System.getProperty("user.dir") + File.separator  + "config/tradeDC/Subscription.json";
				if (new File(path).exists()) {
					SubscriberFilter.resetSettings();
					String content = FileUtils.fileRead(path);
					JSONArray jsonArray = JSONArray.fromObject(content);
					for (Object obj : jsonArray) { 
						Subscriber s = (Subscriber)JSONObject.toBean((JSONObject)obj, Subscriber.class);
				    	if(s.isLpLeg()){
				    		SubscriberFilter.putSessionForLpLeg(s.getSubscriberName(), s);
				    	}
				    	if(s.isClientLeg()){
				    		SubscriberFilter.putSessionForClientLeg(s.getSubscriberName(), s);
				    	}
				    	for(String lp : s.getFilterByLP()){
				    		SubscriberFilter.putSubscriberForFilterByLp(lp, s);
				    	}
				    	for(String pb : s.getFilterByPB()){
				    		SubscriberFilter.putSubscriberForFilterByPB(pb, s);
				    	}
				    	for(String domain : s.getFilterByDomain()){
				    		SubscriberFilter.putSubscriberForFilterByDomain(domain, s);
				    	}
				    	
				    	for(String acct : s.getFilterByAcct()){
				    		SubscriberFilter.putSubscriberForFilterByAccount(acct, s);
				    	}
				    	
						SessionID sessionID = new SessionID(FixVersions.BEGINSTRING_FIX44,
								senderCompID,
								DBOperation.getTargetIDByUserName(s.getUserName()));
						String pluginFilePath = System.getProperty("user.dir") + File.separator + "plugin" + File.separator
								+ sessionID.getTargetCompID() + ".jar";
						
						if (new File(pluginFilePath).exists()) {
							if (SessionCache.getExecutionReportResponse(sessionID) == null) {
								PluginClassLoader.loadClass(pluginFilePath, sessionID);
							}
							SessionCache.putSessionIdByUserName(s.getUserName(), sessionID);
						}
				     }
					
					String resetDone = ConfigSetting.getSeqNoProperty("resetDone");
					if(resetDone == null || "".equals(resetDone)){
						resetDone = "Y";
						ConfigSetting.updateProperty(ConfigSetting.PropertyType.TYPE_SEQNO, "resetDone", resetDone);
						ConfigSetting.updateProperty(ConfigSetting.PropertyType.TYPE_SEQNO, "seqno", "1");
					}					
					
					String seqnoStr = ConfigSetting.getSeqNoProperty("seqno");
					int seqNo = 1;
					if (seqnoStr != null && !"".equals(seqnoStr)) {
						seqNo = Integer.parseInt(seqnoStr);
					}
					else{
						seqnoStr = "1";
						ConfigSetting.updateProperty(ConfigSetting.PropertyType.TYPE_SEQNO, "seqno", seqnoStr);					
					}
					
					String deleteAckedMsgOnlyStr = ConfigSetting.getSeqNoProperty("deleteAckedMsgOnly");					
					if (deleteAckedMsgOnlyStr == null || "".equals(deleteAckedMsgOnlyStr)){
						deleteAckedMsgOnlyStr = "N";
						ConfigSetting.updateProperty(ConfigSetting.PropertyType.TYPE_SEQNO, "deleteAckedMsgOnly", deleteAckedMsgOnlyStr);
					}
					boolean deleteAckedMsgOnly = "Y".equals(deleteAckedMsgOnlyStr);
					
					Logger.getLogger("Event").info("Start to retrieve execution report from database from seqno: " + seqnoStr + ", DeleteAckedMsgOnly Set to " + deleteAckedMsgOnlyStr);
					
					List<ReportsTradeCapture> list = DBOperation.getReportsTradeCapture(seqNo, deleteAckedMsgOnly);
					for (ReportsTradeCapture dbMsg : list) {
						FortexMsg msg = new FortexMsg();
						msg.msgType = (dbMsg.getPbleg().equals(DropcopyQuery.LPLEG) ? FortexMsg.MSGTYPE.LPMSG : FortexMsg.MSGTYPE.CLIENTMSG);
						msg.PB = dbMsg.getPrimebroker();
						msg.LP = dbMsg.getExeBroker();
						msg.client = dbMsg.getClient();
						msg.acct = dbMsg.getRefAcct();
						msg.domain = dbMsg.getDomain();
						msg.seqno2 = dbMsg.getSeqno2();
						msg.dbMsg = dbMsg;
						filter.sendToClientForSubscriber(msg, senderCompID);
					}
				}
			} catch (SQLException e) {
				Logger.getLogger("EventError").error(e.getMessage(), e);
			} catch (IOException e) {
				Logger.getLogger("EventError").error(e.getMessage(), e);
			} catch (Exception e) {
				Logger.getLogger("EventError").error(e.getMessage(), e);
			}
		}
	}
	
	private class AcceptorScheduler implements Runnable {
		private boolean isStarted = false;
		private TimeSchedule schedule;

		AcceptorScheduler(TimeSchedule schedule) {
			this.schedule = schedule;
		}

		@Override
		public void run() {
			if (!schedule.isSessionTime() && isStarted) {
				acceptor.stop();
				isStarted = false;
				for (SessionID sessionID : SessionCache.getAllSessionID()) {
					if (sessionID != null) {
						try {
							((FileStore) messageStoreFactory.create(sessionID)).deleteFiles();
						} catch (IOException e) {
							Logger.getLogger("EventError").error(e.getMessage(), e);
						}
					}
				}
			} else if (schedule.isSessionTime() && !isStarted) {
				try {
					acceptor.start();
					isStarted = true;
				} catch (RuntimeError e) {
					Logger.getLogger("EventError").error(e.getMessage(), e);
				} catch (ConfigError e) {
					Logger.getLogger("EventError").error(e.getMessage(), e);
				}
			}
		}
	}	
}
