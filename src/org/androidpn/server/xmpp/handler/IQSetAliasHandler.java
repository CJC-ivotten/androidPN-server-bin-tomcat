package org.androidpn.server.xmpp.handler;


import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.androidpn.server.xmpp.session.SessionManager;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class IQSetAliasHandler extends IQHandler {

	// 命名空间
	private static final String NAMESPACE = "androidpn:iq:setalias";

	private SessionManager sessionManager;

	public IQSetAliasHandler(){
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
				String alias = element.elementText("alias");
				if(username != null && !username.equals("")  
						&& alias != null && !alias.equals("") ){
					System.out.print("set username alias successfully!!!!!!");
					sessionManager.setUserAlias(username, alias);
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
