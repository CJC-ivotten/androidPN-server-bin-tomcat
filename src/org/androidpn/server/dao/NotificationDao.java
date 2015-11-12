package org.androidpn.server.dao;

import java.util.List;

import org.androidpn.server.model.Notification;

public interface NotificationDao {

	/**
	 * 把离线消息保存到Notification表当中
	 * @param notification
	 */
	void saveNotification(Notification notification);
		
	/**
	 * 当指定用户上线后，查找该用户的离线推送消息,返回离线消息
	 * @param username
	 * @return
	 */
	List<Notification> findNotificationsByUsername(String username);
	
	/**
	 * 当推送之后，要把该消息删除
	 * @param notification
	 */
	void deleteNotification(Notification notification);
	
	/**
	 * 根据UUID来删除离线消息
	 * @param uuid
	 */
	void deleteNotificationByUUID(String uuid);
	
	
}
