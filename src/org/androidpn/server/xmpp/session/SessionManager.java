/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.androidpn.server.xmpp.session;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.androidpn.server.xmpp.XmppServer;
import org.androidpn.server.xmpp.net.Connection;
import org.androidpn.server.xmpp.net.ConnectionCloseListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.util.ConcurrentHashSet;
import org.xmpp.packet.JID;

/** 
 * This class manages the sessions connected to the server.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class SessionManager {

    private static final Log log = LogFactory.getLog(SessionManager.class);

    private static final String RESOURCE_NAME = "AndroidpnClient";

    private static SessionManager instance;

    private String serverName;

    private Map<String, ClientSession> preAuthSessions = new ConcurrentHashMap<String, ClientSession>();

    private Map<String, ClientSession> clientSessions = new ConcurrentHashMap<String, ClientSession>();

    //设置HashMap<别名,用户名>   (根据标签发送消息)
    private Map<String, String> aliasUsernameMap = new ConcurrentHashMap<String, String>();
    
    //设置HashMap<标签,用户名的Set集合>   (根据标签发送消息)
    private Map<String, ConcurrentHashSet<String>> tagUsernamesMap = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();
    
    private final AtomicInteger connectionsCounter = new AtomicInteger(0);

    private ClientSessionListener clientSessionListener = new ClientSessionListener();

    private SessionManager() {
        serverName = XmppServer.getInstance().getServerName();
    }

    /**
     * Returns the singleton instance of SessionManager.
     * 
     * @return the instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                instance = new SessionManager();
            }
        }
        return instance;
    }

    /**
     * Creates a new ClientSession and returns it.
     *  
     * @param conn the connection
     * @return a newly created session
     */
    public ClientSession createClientSession(Connection conn) {
        if (serverName == null) {
            throw new IllegalStateException("Server not initialized");
        }

        Random random = new Random();
        String streamId = Integer.toHexString(random.nextInt());

        ClientSession session = new ClientSession(serverName, conn, streamId);
        conn.init(session);
        conn.registerCloseListener(clientSessionListener);

        // Add to pre-authenticated sessions
        preAuthSessions.put(session.getAddress().getResource(), session);

        // Increment the counter of user sessions
        connectionsCounter.incrementAndGet();

        log.debug("ClientSession created.");
        return session;
    }

    /**
     * Adds a new session that has been authenticated. 
     *  
     * @param session the session
     */
    public void addSession(ClientSession session) {
        preAuthSessions.remove(session.getStreamID().toString());
        clientSessions.put(session.getAddress().toString(), session);
    }

    /**
     * Returns the session associated with the username.
     * 
     * @param username the username of the client address
     * @return the session associated with the username
     */
    public ClientSession getSession(String username) {
        // return getSession(new JID(username, serverName, null, true));
        return getSession(new JID(username, serverName, RESOURCE_NAME, true));
    }

    /**
     * Returns the session associated with the JID.
     * 
     * @param from the client address
     * @return the session associated with the JID
     */
    public ClientSession getSession(JID from) {
        if (from == null || serverName == null
                || !serverName.equals(from.getDomain())) {
            return null;
        }
        // Check pre-authenticated sessions
        if (from.getResource() != null) {
            ClientSession session = preAuthSessions.get(from.getResource());
            if (session != null) {
                return session;
            }
        }
        if (from.getResource() == null || from.getNode() == null) {
            return null;
        }
        return clientSessions.get(from.toString());
    }

    /**
     * Returns a list that contains all authenticated client sessions.
     * 
     * @return a list that contains all client sessions
     */
    public Collection<ClientSession> getSessions() {
        return clientSessions.values();
    }

    /**
     * Removes a client session.
     * 
     * @param session the session to be removed
     * @return true if the session was successfully removed 
     */
    public boolean removeSession(ClientSession session) {
        if (session == null || serverName == null) {
            return false;
        }
        JID fullJID = session.getAddress();

        // Remove the session from list
        boolean clientRemoved = clientSessions.remove(fullJID.toString()) != null;
        boolean preAuthRemoved = (preAuthSessions.remove(fullJID.getResource()) != null);

        // Decrement the counter of user sessions
        if (clientRemoved || preAuthRemoved) {
            connectionsCounter.decrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Closes the all sessions. 
     */
    public void closeAllSessions() {
        try {
            // Send the close stream header to all connections
            Set<ClientSession> sessions = new HashSet<ClientSession>();
            sessions.addAll(preAuthSessions.values());
            sessions.addAll(clientSessions.values());

            for (ClientSession session : sessions) {
                try {
                    session.getConnection().systemShutdown();
                } catch (Throwable t) {
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * A listner to handle a session that has been closed.
     */
    private class ClientSessionListener implements ConnectionCloseListener {

        public void onConnectionClose(Object handback) {
            try {
                ClientSession session = (ClientSession) handback;
                removeSession(session);
            } catch (Exception e) {
                log.error("Could not close socket", e);
            }
        }
    }
    
    /**
     * 设置用户名别名
     * @param username
     * @param alias
     */
    public void setUserAlias(String username,String alias){
    	aliasUsernameMap.put(alias, username);
    }

    /**
     * 根据别名获取用户名
     * @param alias
     * @return
     */
    public String getUsernameByAlias(String alias){
    	String username = aliasUsernameMap.get(alias);
		return username;
    }
    
    /**
     * 给用户设置标签
     * @param username
     * @param tag
     */
    public void setUserTag(String username,String tag){
    	ConcurrentHashSet<String> hashSet = tagUsernamesMap.get(tag);
    	if(hashSet == null){
    		ConcurrentHashSet<String> set = new ConcurrentHashSet<String>();
    		set.add(username);
    		tagUsernamesMap.put(tag, set);
    	}else{
    		hashSet.add(username);
    	}
    	
    }
    
    /**
     * 根据用户群体的标签拿到用户群体
     * @param tag
     * @return
     */
    public Set<String> getUsernameByTag(String tag){
    	return tagUsernamesMap.get(tag);
    }
    
}
