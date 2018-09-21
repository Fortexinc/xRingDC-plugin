package com.fortex.xRingDC.plugin.webconsole;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.fortex.xRingDC.plugin.cache.SessionCache;
import com.fortex.xRingDC.plugin.model.UserModel;
import com.fortex.xRingDC.plugin.utils.ConfigSetting;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import quickfix.Session;
import quickfix.SessionID;

public class RunningServer {
	private IoAcceptor acceptor;
	private static final int PORT = Integer.parseInt(ConfigSetting.getServerProperty("restApiPort"));
	public RunningServer() throws IOException {
		acceptor = new NioSocketAcceptor();
	}
	
	public void start() throws Exception {
		acceptor.setHandler(new RunningServerHandler());
		acceptor.bind(new InetSocketAddress(PORT));
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		acceptor.getSessionConfig().setReadBufferSize(1024);  
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	}
	
	private static class RunningServerHandler extends IoHandlerAdapter{

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			
		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			String requestStr = message.toString();
			JSONObject request = JSONObject.fromObject(requestStr);
			
			String requestType = request.getString("requestType");
			JSONObject json = new JSONObject();
			if ("status".equals(requestType)) {
				json.put("status", "success");
				Logger.getLogger("Event").info("Output the status to webconsole with json format:" + json.toString());
				session.write(json.toString());
			} else if ("logonUsers".equals(requestType)) {
				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				JSONArray array = new JSONArray();
				//List<String> userNames = SessionCache.getUserNames();
				
				UserModel[] models = SessionCache.getUserModels();
				for (UserModel model : models) {
					JSONObject obj = new JSONObject();
					obj.put("userName", model.getUserName());
					obj.put("ip", model.getIp());
					obj.put("logonTime", formater.format(model.getLogonTime()));
					array.add(obj);
				}
				String result = array.toString();
				session.write(result);
				Logger.getLogger("Event").info("Output the user name to webconsole with json format:" + result);
				
				/*
				for (String userName : userNames) {
					array.put(userName);
				}
				json.put("userNames", array);
				Logger.getLogger("Event").info("Output the user name to webconsole with json format:" + json.toString());
				*/
			} else if ("disconnect".equals(requestType)) {
				String userName = request.getString("account");
				SessionID sessionID = SessionCache.getSessionIdByUserName(userName);
				if (sessionID != null) {
					Session s = Session.lookupSession(SessionCache.getSessionIdByUserName(userName));
					if (s != null)
						s.disconnect();
				} 
				SessionCache.removeSessionId(sessionID);
				SessionCache.removeSessionIdForUserName(sessionID);
				json.put("status", "success");
				Logger.getLogger("Event").info("Output the connection status to webconsole with json format:" + json.toString());
				session.write(json.toString());
			}
			
		}
	}
}
