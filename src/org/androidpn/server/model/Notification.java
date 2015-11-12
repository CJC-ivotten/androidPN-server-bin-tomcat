package org.androidpn.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 储存离线消息的表
 * 服务器是通过NotificationController来推送消息，
 * 自己增加的离线消息的推送内容(所需要的字段)可以从里面模仿提取。
 * 通过注解，将这个映射成对应数据库的一张表(模仿服务器的User实体类)
 * @author chenjiacheng
 *
 */
@Entity  //代表这个类是实体类，要做对象关系映射
@Table(name = "notification")  //表示这个实体类是一张表，表名是notification
public class Notification {

	@Id   //代表是主键
    @GeneratedValue(strategy = GenerationType.AUTO)   //生成策略是“自动”
	private long id;//主键
	
	@Column(name = "api_key", length = 64)
	private String apiKey;

	@Column(name = "username", nullable = false, length = 64)
	private String username;
	
	@Column(name = "title", nullable = false, length = 64)
	private String title;
	
	@Column(name = "message", nullable = false, length = 1000)
	private String message;
	
	@Column(name = "uri", length = 256)
	private String uri;
	
	@Column(name = "image_url",  length = 256)
	private String imageUrl;
	
	@Column(name = "uuid", nullable = false, length = 64, unique = true)
	private String uuid;
	
	
	
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


	
}
