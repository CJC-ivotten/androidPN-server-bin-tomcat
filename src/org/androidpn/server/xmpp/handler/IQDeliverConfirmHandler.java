package org.androidpn.server.xmpp.handler;

import org.androidpn.server.service.NotificationService;
import org.androidpn.server.service.ServiceLocator;
import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

public class IQDeliverConfirmHandler extends IQHandler {

    private static final String NAMESPACE = "androidpn:iq:deliverconfirm";

    private NotificationService notificationService;
    
    
    public IQDeliverConfirmHandler(){
    	notificationService = ServiceLocator.getNotificationService();
    }
    
	/**
	 * 对客户端发送过来的消息做具体的逻辑处理
	 * 取出UUID，删除数据库对应的消息
	 */
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		//保护措施
		ClientSession session = sessionManager.getSession(packet.getFrom());
        IQ reply;
		if (session == null) {
            log.error("Session not found for key " + packet.getFrom());
            reply = IQ.createResultIQ(packet);
            reply.setChildElement(packet.getChildElement().createCopy());
            reply.setError(PacketError.Condition.internal_server_error);
            return reply;
        }//--
		//先要认证成功和类型认证
		if(session.getStatus() == Session.STATUS_AUTHENTICATED){
			if(IQ.Type.set.equals(packet.getType())){
				Element element = packet.getChildElement();
				String uuid = element.elementText("uuid");
				//删除消息
				notificationService.deleteNotificationByUUID(uuid);
			}
		}
		return null;
	}

	@Override
	public String getNamespace() {
		
		return NAMESPACE;
	}

}
