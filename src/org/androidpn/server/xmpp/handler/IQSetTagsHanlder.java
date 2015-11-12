package org.androidpn.server.xmpp.handler;


import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.androidpn.server.xmpp.session.SessionManager;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class IQSetTagsHanlder extends IQHandler {

    private static final String NAMESPACE = "androidpn:iq:settags";

    private SessionManager sessionManager;
    
    
    public IQSetTagsHanlder(){
    	sessionManager = SessionManager.getInstance();
	}
	/**
	 * 对客户端发送过来的消息做具体的逻辑处理
	 * 
	 */  
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		// 保护措施
		ClientSession session = sessionManager.getSession(packet.getFrom());
		IQ reply;
		if (session == null) {
			log.error("Session not found for key " + packet.getFrom());
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.internal_server_error);
			return reply;
		}// --
			// 先要认证成功和类型认证
		if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
			if (IQ.Type.set.equals(packet.getType())) {
				Element element = packet.getChildElement();
				String username = element.elementText("username");
				
				String tagsStr = element.elementText("tags");
				String[] tagsArray = tagsStr.split(",");//分割，拿到所有tags
				if(tagsArray != null && tagsArray.length > 0){
					for(String tag : tagsArray){
						sessionManager.setUserTag(username, tag);
					}
					System.out.println("set username tags successfully!!!!!!!!!");
				}				
			}
		}
		return null;
	}

	@Override
	public String getNamespace() {

		return NAMESPACE;
	}
}
