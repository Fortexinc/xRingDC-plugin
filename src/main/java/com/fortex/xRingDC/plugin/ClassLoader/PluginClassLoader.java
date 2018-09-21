package com.fortex.xRingDC.plugin.ClassLoader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import com.fortex.xRingDC.plugin.api.BaseExecutionReportResponse;
import com.fortex.xRingDC.plugin.cache.SessionCache;

import quickfix.SessionID;

public class PluginClassLoader {
	private static final String PLUGIN_CLASS_NAME = "com.fortex.xRingDC.plugin.impl.ExecutionReportResponseImpl";
	
	public static void loadClass(String pluginFilePath, SessionID sessionID) throws Exception{
		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		URL url = new File(pluginFilePath).toURI().toURL();
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url }, parent);
		BaseExecutionReportResponse executionReportResponse = (BaseExecutionReportResponse) urlClassLoader
				.loadClass(PLUGIN_CLASS_NAME).newInstance();
		SessionCache.putExecutionReportResponse(sessionID, executionReportResponse);
		urlClassLoader.close();
	} 
}
