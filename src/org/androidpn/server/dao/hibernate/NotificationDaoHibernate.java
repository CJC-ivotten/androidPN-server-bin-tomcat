package org.androidpn.server.dao.hibernate;

import java.util.List;

import org.androidpn.server.dao.NotificationDao;
import org.androidpn.server.model.Notification;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class NotificationDaoHibernate extends HibernateDaoSupport implements
		NotificationDao {

	// 存到Notification的表当中（仿照UserDaoHibernate）
	public void saveNotification(Notification notification) {
		getHibernateTemplate().saveOrUpdate(notification);
		getHibernateTemplate().flush();
	}

	// 删除离线消息
	public void deleteNotification(Notification notification) {
		getHibernateTemplate().delete(notification);
	}

	// 根据当户名查询离线消息
	@SuppressWarnings("unchecked")
	public List<Notification> findNotificationsByUsername(String username) {
		List<Notification> list = getHibernateTemplate().find(
				"from Notification where username=?", username);
		if (list != null && list.size() > 0) {
			return list;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void deleteNotificationByUUID(String uuid) {
		List<Notification> list = getHibernateTemplate().find(
				"from Notification where uuid=?", uuid);
		if (list != null && list.size() > 0) {
			Notification notification = list.get(0);
			deleteNotification(notification);
		}
		
	}

}
