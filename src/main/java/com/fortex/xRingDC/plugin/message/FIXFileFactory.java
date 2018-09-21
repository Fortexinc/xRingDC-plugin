package com.fortex.xRingDC.plugin.message;

import java.util.concurrent.ConcurrentHashMap;

import quickfix.FileStoreFactory;
import quickfix.MessageStore;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class FIXFileFactory extends FileStoreFactory{
	private static ConcurrentHashMap<SessionID, FIXFileStore> STORE_MAP = new ConcurrentHashMap<SessionID, FIXFileStore>();
	public FIXFileFactory(SessionSettings settings) {
		super(settings);
		
	}
	
	
	public MessageStore create(SessionID sessionID) {
        try {
            boolean syncWrites = false;
            if (settings.isSetting(sessionID, SETTING_FILE_STORE_SYNC)) {
                syncWrites = settings.getBool(sessionID, SETTING_FILE_STORE_SYNC);
            }
            int maxCachedMsgs = 10000;
            if (settings.isSetting(sessionID, SETTING_FILE_STORE_MAX_CACHED_MSGS)) {
                long maxCachedMsgsSetting = settings.getLong(sessionID, SETTING_FILE_STORE_MAX_CACHED_MSGS);
                if (maxCachedMsgsSetting >= 0 && maxCachedMsgsSetting <= (long) Integer.MAX_VALUE) {
                    maxCachedMsgs = (int) maxCachedMsgsSetting;
                }
            }
            if (STORE_MAP.get(sessionID) == null)
            	STORE_MAP.put(sessionID, new FIXFileStore(settings.getString(sessionID, FileStoreFactory.SETTING_FILE_STORE_PATH), sessionID, syncWrites, maxCachedMsgs));
            return STORE_MAP.get(sessionID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	 }
}
