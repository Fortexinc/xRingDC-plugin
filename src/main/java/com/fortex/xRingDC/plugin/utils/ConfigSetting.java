package com.fortex.xRingDC.plugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author Ivan Huo
 *
 */
public class ConfigSetting {
	public enum PropertyType {TYPE_SERVER, TYPE_DATABASE, TYPE_SEQNO, TYPE_TRADE,};
	private static final Properties SERVER = new Properties();
	private static final Properties DATABASE = new Properties();
	private static final Properties SEQNO = new Properties();
	private static final Properties TRADE = new Properties();
	static{
		try {
			SERVER.load(new FileInputStream(System.getProperty("user.dir") + File.separator  + "config/server/server.properties")); 
			DATABASE.load(new FileInputStream(System.getProperty("user.dir") + File.separator + "config/server/database.properties"));
			SEQNO.load(new FileInputStream(System.getProperty("user.dir") + File.separator  + "config/tradeDC/seqno.properties"));
			TRADE.load(new FileInputStream(System.getProperty("user.dir") + File.separator  + "config/tradeDC/acceptorTrade.properties"));
		} catch (IOException e) {
			Logger.getLogger("EventError").error("load config file error.", e);
		}
	}
	
	public static String getLocPath(){
		String path= ConfigSetting.class.getProtectionDomain().getCodeSource().getLocation().getFile();
				
		if(path.toLowerCase().indexOf(".jar")>0){
			if(path.indexOf(File.separator)>0)
				path=path.substring(0, path.lastIndexOf(File.separator)) + File.separator;
			else
				path=path.substring(0, path.lastIndexOf("/")) + File.separator;
		}else{
			path = path + ".." + File.separator;
		}
		return path;
	}
	
	public static String getServerProperty(String key) {
		return SERVER.getProperty(key);
	}
	
	public static String getDatabaseProperty(String key) {
		return DATABASE.getProperty(key);
	}
	
	public static String getSeqNoProperty(String key) {
		return SEQNO.getProperty(key);
	}
	
	public static String getTradeProperty(String key) {
		return TRADE.getProperty(key);
	}
	
	public static void updateProperty(PropertyType type, String key, String value){
		switch(type){
		case TYPE_SERVER :
			SERVER.setProperty(key, value);
			updateProperty("config/server/server.properties", key, value);
			break;
		case TYPE_DATABASE :
			DATABASE.setProperty(key, value);
			updateProperty("config/server/database.properties", key, value);
			break;
		case TYPE_SEQNO :
			SEQNO.setProperty(key, value);
			updateProperty("config/tradeDC/seqno.properties", key, value);
			break;
		case TYPE_TRADE :
			TRADE.setProperty(key, value);
			updateProperty("config/tradeDC/acceptorTrade.properties", key, value);
			break;
		default:
			return;
		}		
	}
	
	private static void updateProperty(String filePath, String key, String value) {   
		
        Properties prop = new Properties();   
        String propertyFile = System.getProperty("user.dir") + File.separator  + filePath;
        try {   
            File file = new File(propertyFile);   
            if (!file.exists())   
                file.createNewFile();   
            InputStream fis = new FileInputStream(file);   
            prop.load(fis);   
            fis.close();   
            OutputStream fos = new FileOutputStream(propertyFile);   
            prop.setProperty(key, value);   
            prop.store(fos, "Updated " + key + "=" + value);       
            fos.close();   
        } catch (IOException e) {   
        	Logger.getLogger("EventError").error("save config file error.", e);  
        }
	}
}
