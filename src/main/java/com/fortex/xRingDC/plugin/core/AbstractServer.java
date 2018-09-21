package com.fortex.xRingDC.plugin.core;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fortex.xRingDC.plugin.log.FixServerLogFactory;
import com.fortex.xRingDC.plugin.message.FIXFileFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.mina.acceptor.AbstractSocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;

public abstract class AbstractServer {
	
	
	private static enum CONNECTION_TYPE{INITIATOR,ACCEPTOR};
	private final Map<InetSocketAddress, List<TemplateMapping>> dynamicSessionMappings = new HashMap<InetSocketAddress, List<TemplateMapping>>();
	protected AbstractSocketAcceptor acceptor;
	protected MessageStoreFactory messageStoreFactory;
//	protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//	protected AbstractSocketInitiator initiator;
	public AbstractServer(SessionSettings settings) throws Exception{
		this.initialize(settings, CONNECTION_TYPE.ACCEPTOR);
	}
	

	/**
	 * 
	 * <p>Description:Initialize the acceptor and initiator.</p> 
	 *
	 * @author Patrick Chi
	 * @date 2016-07-13 
	 * @param confFilePath:Path of configuration file for initiator and acceptor.
	 * @param serverType:Specify the server is initiator or acceptor.
	 * @throws Exception
	 */
	private void initialize(SessionSettings settings,  CONNECTION_TYPE serverType) throws Exception{		
		//SessionSettings settings = new SessionSettings(new FileInputStream(confFilePath));
		LogFactory logFactory = new FixServerLogFactory(settings);
		MessageFactory messageFactory = new DefaultMessageFactory();
		messageStoreFactory = new FIXFileFactory(settings);
		
		//MessageStore store = messageStoreFactory.create(null);
		//FileStore f = null;
		acceptor = new SocketAcceptor(getAcceptorApplication(settings), messageStoreFactory, settings, logFactory, messageFactory);		
		configureDynamicSessions(settings, getAcceptorApplication(settings), messageStoreFactory, logFactory, messageFactory);
		
	}
	
	private void configureDynamicSessions(SessionSettings settings, Application application,
            MessageStoreFactory messageStoreFactory, LogFactory logFactory,
            MessageFactory messageFactory) throws ConfigError, FieldConvertError {
        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionID = sectionIterator.next();
            if (isSessionTemplate(settings, sessionID)) {
                InetSocketAddress address = getAcceptorSocketAddress(settings, sessionID);
                getMappings(address).add(new TemplateMapping(sessionID, sessionID));
            }
        }

        for (Map.Entry<InetSocketAddress, List<TemplateMapping>> entry : dynamicSessionMappings
                .entrySet()) {
            acceptor.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
                    settings, entry.getValue(), application, messageStoreFactory, logFactory,
                    messageFactory));
        }
    }

    private List<TemplateMapping> getMappings(InetSocketAddress address) {
        List<TemplateMapping> mappings = dynamicSessionMappings.get(address);
        if (mappings == null) {
            mappings = new ArrayList<TemplateMapping>();
            dynamicSessionMappings.put(address, mappings);
        }
        return mappings;
    }

    private InetSocketAddress getAcceptorSocketAddress(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        String acceptorHost = "0.0.0.0";
        if (settings.isSetting(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS)) {
            acceptorHost = settings.getString(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS);
        }
        int acceptorPort = (int) settings.getLong(sessionID, SETTING_SOCKET_ACCEPT_PORT);

        return new InetSocketAddress(acceptorHost, acceptorPort);
    }

    private boolean isSessionTemplate(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }
	
	/**
	 * 
	 * <p>Description:Get instance of Application for acceptor</p> 
	 *
	 * @author Patrick Chi
	 * @date 2016-07-13 
	 * @return
	 */
	protected abstract Application getAcceptorApplication(SessionSettings settings);
	
	 
	/**
	 * 
	 * <p>Description:execute to start the server</p> 
	 *
	 * @author Patrick Chi
	 * @date 2016-07-13
	 */
	protected abstract void doExecute(SessionSettings settings);
}
