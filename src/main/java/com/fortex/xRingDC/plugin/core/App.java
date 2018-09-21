package com.fortex.xRingDC.plugin.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.fortex.lib.globalservices.FortexLogger;
import com.fortex.xRingDC.plugin.utils.ConfigSetting;
import com.fortex.xRingDC.plugin.webconsole.RunningServer;

import quickfix.SessionSettings;

public class App 
{
	
	private void start() throws Exception {
		String starterClassName = ConfigSetting.getServerProperty("starterClass");
		String acceptorConfigFile = System.getProperty("user.dir") + File.separator + ConfigSetting.getServerProperty("acceptorConfigFile");
		SessionSettings settings = new SessionSettings(new FileInputStream(acceptorConfigFile));
		try {
			RunningServer runningServer = new RunningServer();
			runningServer.start();
			Constructor<?> con = Class.forName(starterClassName).getConstructor(SessionSettings.class);
			AbstractServer starter = (AbstractServer)con.newInstance(settings);	
			starter.doExecute(settings);
		} catch (Exception e) {
			Logger.getLogger("EventError").error(e.getMessage(), e);
		}
		
	}
    public static void main( String[] args ) throws Exception
    {
		System.setProperty ("WORKDIR", ConfigSetting.getLocPath());
		org.apache.log4j.PropertyConfigurator.configure(App.class.getResourceAsStream("/log4j.properties"));
		FortexLogger.defaultLogger = Logger.getLogger("Event");
		try {
			int port = Integer.parseInt(ConfigSetting.getTradeProperty("SocketAcceptPort"));
			if (!isPortUsing(port)) {
				App app = new App();
				app.start();
			} else {
				String errorMsg = "The port " + port + " was already in used, system exited.";
				Logger.getLogger("EventError").error(errorMsg);
				System.out.println(errorMsg);
			}
		} catch (Exception e) {
			Logger.getLogger("EventError").error(e.getMessage(), e);
		}
    }
    
    public static boolean isPortUsing(int port) throws UnknownHostException{
		 ServerSocket serverSocket = null;
	       boolean flag = false;
	       try {  
	    	   serverSocket = new ServerSocket(port);
	       } catch (IOException e) {  
	    	   flag = true;  
	       }  finally {
	    	   if (serverSocket != null)
				try {
					serverSocket.close();
				} catch (IOException e) {Logger.getLogger("EventError").error(e.getMessage(), e);}
	       }
	       return flag;  
	 }
}
