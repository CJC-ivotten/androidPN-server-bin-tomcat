# androidPN-server-bin-tomcat

前面的预备知识：socket mina框架 xmpp协议

二次开发比从头开发更好

android是基于linux开发的
chrome是基于chromium开发的

开源项目选择： android push notification AndroidPN 韩国人写的
这个项目是个半成品，完成了主体功能，遗留一些bug和可扩展的功能，有利于自己的修复bug和学习，发挥空间特别大

开发工具：
eclipse
myeclipse
mysql数据库
tomcat服务器




二次开发

服务器：
1、加入190秒检测一次客户端是否掉线，在spring-config.xml中加入检测逻辑，通过设置要求客户端间隔发送消息给服务器，通过               

setReaderIdleTime方法 
2、服务器是通过NotificationController来推送消息，自己增加的离线消息的推送内容 ( 所需要的字段 ) 可以从里面模仿提取。
3、发送离线消息：
		a) : 创建数据库（表的方式），android PN 框架使用 hibernate 来管理数据库（表），一个实体类就对应一个表
			新建一张表Notification，来储存离线消息。( 仿照User的实体类来创建，并模仿注解方式添加映射 )
		b) : 仿照 UserDaoHibernate.java ( 属于User表的一套增删改查的方法，实现自定义的UserDao接口 )来编写属于 	

			Notification(表)的增删改查一套方法NotificationDaoHibernate.java（也增加NotificationDao接口）
		c) : 记得要在spring-config.xml中配置
		d) : android PN 的架构：业务逻辑层-------UserServer-----中间服务层-------Dao-------数据库层
			当然我们也遵循它的设计
		e) : 在NotificationManager.java中增加保存消息的方法saveNotification，将离线的消息保存到数据库中		

			（Notification）,包括广播和指定个人
4、为消息加上一个字段：UUID。
	因为，即使可以发送离线消息，但是任然会发生丢失消息，比如在，客户端掉线了，服务器还没来得及确认客户端下线，这时推	

	送消息的话，就会导致消息丢失，所以要加上UUID，在客户端接收到消息后，发送一条回执，根据UUID来删除离线消息	
	拿id当作UUID，因为概率几乎不可能为一样

5、处理回执消息机制：所有客户端发来的消息都会经过XmppIoHandler.java处理，追踪下去的话，我们要新建一个			

IQDeliverConfirmHandler（继承IQHandler，同时要在IQRouter.java里面注册）来处理我们的回执消息。（仿照IQRegisterHandler等来写）
	主要是为了解析到UUID，拿到UUID去删除对用的消息。这样就可以保证百分之一百的推送率（其实也可以再进行优化，假设用	

户长时间没有上线，可以设定一个时间值来向客户端发送一条短信，确保消息的到达）

6：根据别名发送消息：
	a) : 从HashMap开始（在SessionManager.java），提供setUserAlias，getUsernameByAlias方法。
	b) : 在NotificationManger.java中添加sendNotificationByAlias方法
	c) : 修改浏览器的网页的界面，增加根据别名（标签）推送消息的checkBox

7、同样，类似IQDeliverConfirmHandler。我们同样新建一个IQ解析器IQSetAliasHandler.java，来解析客户端发送过来的别名（标签）IQ。
	
8、在根据别名的基础上，仿照加入根据标签推送消息：
	a) : 在SessionManager.java 里面设置HashMap<标签,用户名的Set集合>   (根据标签发送消息) 。设置get，set方法
    		private Map<String, ConcurrentHashSet<String>> tagUsernamesMap
	b) : 在NotificationManager.java里面设置sendNotificationByTag方法
	c) : from.jsp增加浏览器的选项
	d) : NotificationController.java设置接受浏览器提交的表单
	e) : 同样，类似IQDeliverConfirmHandler。我们同样新建一个IQ解析器IQSetTagsHandler.java，来解析客户端发送过来的别名
		（标签）IQ。
	f) : 然后紧接着在IQRouter.java注册IQSetTagsHandler
	
	（顺便客户端）：
	a) : 新增一个IQ( SetTagsIQ.java)，用于向服务器发送标签消息的IQ
	b) : 在SeiviceManager.java 中新增setTags方法，用于向服务器发送标签
	c) : 紧接着在DemoAppActivity设置tags。

9、富媒体消息推送的实现：  （通过图片作为例子）
	a) : 修改from.jsp，增加选择文件上传选项。记得要添加   enctype="multipart/form-data"  标签
	b) : 上传图片到服务器，使用  commons-fileupload-1.3.1-bin  的开源库（接受参数和上传文件）
	c) : 服务器保存图片到指定位置（图片数据库），返回一个浏览器可以访问的 imageUrl 地址。
	d) : 向客户端发送imageUrl地址（通过IQ发送）：
		1) :  在NotificationManager.java 添加  notification.addElement("imageUrl").setText(imageUrl); 的字段，增加一项发	

			送内容
		2) : 在Notification.java 的数据库表中添加imageUrl字段，增加saveNotification方法的字段等等
	e) : 在客户端接受和解析imageUrl地址：
		1) : 在PacketReader.java 中的parsePackets方法中解析( XmlPullparser解析器 )服务器发送过来的xml数据，拿到	

		imageUrl字段
		2) : 解析imageUrl字段：在NotificationIQProvider.java 中 //解析到服务器发送过来的imageUrl
		3) : 在 NotificationPacketListener.java 中拿到 imageUr l地址，并且intent（广播的形式，在通知栏显示）出去
		4) : 在NotificationReceiver.java 中拿到 imageUrl ，然后传递给通知栏，点击通知栏，激活NotificationDetailsActivity
			在里面进行网络加载图片（使用vollry开源框架）。

注意：res -->raw -->androidpn.properties文件中的：xmppHost，如果使用模拟器调试则改为10.0.2.2，如果使用真机调试，则改为本地ip地	

		址

客户端：
1、加入心跳线程，60秒发送一次空格数据包XML，保持会话。在PackerWriter.java中加入心跳线程，经过两层封装，在XmppManger.java     

中调用，在客户端登陆完成（LoginTask）之后调用
2、a : 优化android PN 断线重连线程（ReconnectionThread.java）
     b : 修复了（ReconnectionThread.java）线程在经过身份认证之后还执行的BUG
           在XmppManger.java 的 startReconnectionThread方法中，修复了一个小BUG//加入不为空的判断，再new一个线程对象，就规避       

    一个断线重连线程start两次二崩溃
3、android PN 客户端有三种断线重连机制 ： ReconnectionThread.java
				   ConnectivityReceiver.java
				   PhoneStateChangeListener.java
	重连实现都是通过调用 xmppManager.connect();//进行重连操作，先连接，后注册，再登陆


4、理解android PN的任务队列执行机制：单队列机制，一旦提交任务，会提交一组任务（连接，注册，登录），一次只能执行一个任务，        

在一个任务的代码最后会通知（通过runTask方法）下一个任务去执行。连接任务完成后会通知注册任务，需要注意一点的是登录任务是       在

注册任务完成后的回调再去通知登录任务开始执行。  但是！！！！！！万一注册失败，服务器就不会相应客户端，导致写在回调去          通知下

一个登陆任务就永远不会得到执行，又由于是单队列机制，在接下去的任务里，无论怎么进行断线重连，都不会得到执行，从而导      致整个客

户端失效。同样！！！！！！在登陆任务那里也存在同样的问题
	
	解决问题4的BUG：增加一个丢弃任务dropTask(int dropConut)的功能，把如果连接任务失败，就把后两个的任务丢弃掉，就不	

	           会影响后两个任务的进行
			a) : 然后让注册线程睡眠十秒，加上标志位（isRegisterSucceed）。如果注册不成功，再添加一段丢弃，	

		      断线重连代码
			b) : 外一在9.99秒服务器响应了，就会出现一个任务连续两次通知一下咯任务，打破单队列机制，崩溃。
			c) : 所以加同步锁 synchronized (xmppManager) {    }
			d) : 万一服务器在第十秒以后相响应了，还是会打破单队列机制，所以要加上一个标志位( hasDropTask )

5、在DirectSocketFactory.java中的socket连接中设置超时时间：        //修复一个小bug，手动设置客户端连接超时时间，为10秒。不然要      

等三分多种才报超时错误（实测得出来的数据）
6、为了保证推送的到达，每当客户端接收到消息的时候，客户端都要发送一条回执给服务器（这样才让服务器去删除消息），且服务器发送	

过来的是XMPP协议格式的数据（IQ）,所以回执也要是IQ  ，所以我们要自定义一    个DeliverConfirmIQ（回执确认IQ）

7、新增一个IQ( SetAliasIQ.java)，用于向服务器发送别名（标签） 

8、向服务器发送别名SetAliasIQ的请求：
	a) : 在ServiceManager.java提供设置别名的方法（setAlias( String alias )）
	b) : //建立等待的机制，等待身份认证成功，在XmppManager.java中的心跳线程启动的后面解锁（因为此时身份认证肯定完成了）

9、完善客户端的接受功能 :
	a) : 加入查看推送历史消息的功能  : 
		1) : 使用 litepal 数据库开源框架：按步骤配置https://github.com/LitePalFramework/LitePal
		2) : 增加一个实体类，映射成一张表NotificationHistory.java 
		3) : 在NotificationPacketListener.java里面，在发送广播之前，把消息存入litepal数据库表( NotificationHistory.java )当	

			中
		4) : 增加NotificationHistoryActivity，在里面增加显示历史推送的消息的代码
		5) : 在NotificationHistoryActivity，中增加删除历史推送功能（通过给ListView设置上下文菜单来实现）

	b) : 加入开机自动启动推送服务的功能 ： 
		1) : 在NotificationSettingsActivity.java 中增加一个可选框autoStart，（记得 root.addPreference(autoStart);）
		2) : 增加BootCompletedReceiver.java 的开机广播接收器, 检查用户是否勾选开机自动启动（默认true），是的话就启动
			 // Start the service
	       		 ServiceManager serviceManager = new ServiceManager(context);
	       		 serviceManager.setNotificationIcon(R.drawable.notification);
	       		 serviceManager.startService();
		3) : 记得在清单文件中注册该广播接收器，和添加权限
	c) : 在其他程序中集成该推送项目 ： 
		方法一：（将推送客户端集成成为一个library）
			1) : 修改推送项目的属性，把它修改为一个library（指定工程，右键，properties，勾选Is Library，确认）
			2) : 在需要加入推送功能的项目B，（指定工程，右键，properties，add ,确认）
			3) : 在项目B的清单文件中添加配置代码（看图片）
			4) : 加入启动代码（根据需要添加）	
			 // Start the service
	        		 ServiceManager serviceManager = new ServiceManager(context);
	      		 serviceManager.setNotificationIcon(R.drawable.notification);
	       		 serviceManager.startService();

		方法二：（将推送项目打包成为jar包）
			1) : 导出项目为一个jar包（jar file），只选择src，asmack，确认
			2) : 在需要集成推送的项目的libs加入该jar包，
3) : 复制粘贴资源（图片，布局，string，第三方库，清单文件），合并到新的项目中。
