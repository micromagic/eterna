
例子中使用的是java开发的开源数据库H2，数据库文件在test\WebContent\WEB-INF\db下。
例子中需要的表都已经建好了，无需做任何改动，可在eclipse中直接运行。第一次运行时，需要配置一个应用服务器。
首页的地址为：http://域名:端口/test/test.do


eclipse导入项目说明
1. 将压缩包解压后，把test目录（目录名称可以修改）放到eclipse的工作区下。
2. 打开eclipse，选择菜单file->import，在弹出窗口中选择General->Existing Projects into Workspace，然后选择刚复制进去的test目录，点finish。
3. 在Project Explorer中，选中Deployment Descriptor: test->Servlets->test，右键点击，选择Run As->Run on Server。
4. 在弹出窗口中选择一个应用服务器（建议使用Tomcat v6.0）即可。如果未配置服务器的话，配置一个就可以了。
注：jdk需要1.5或以上版本。


如果你使用的是myeclipse或其它的开发工具，则可以先建一个web项目，然后分别将src和WebContent目录下的文件复制到对应的目录下即可。
如果你没有开发工具，则可将bulid下的classes复制到WebContent/WEB-INF下，然后将WebContent放入web应用服务器（如：tomcat）就可运行了。



micromagic_config.properties文件中的几个配置的说明：

一、
dataSource.url=jdbc:h2:${h2.baseDir}/test
此配置为H2的数据库连接字符串，“${h2.baseDir}”为数据库文件所在的路径，此变量会在test/Test.java这个servlet初始化时设置进去。
如果你把数据库文件放在了别的目录，则可直接修改这个配置，或者添加一个“h2.baseDir”属性，如：
h2.baseDir=C:\\db
表示数据库文件在C盘的db目录下
h2.baseDir=~
表示数据库文件在当前用户路径下，具体可查看H2的说明文档

二、
self.micromagic.useEternaLog=true
表示加载eterna的日志，这样就可以在“[contextRoot]/eterna/setting.jsp”中的error日志中查看到所有的日志输出。

三、
self.micromagic.eterna.digester.checkGrammer=false
表示关闭页面脚本的语法结构检查，这样可以提高加载的效率。
但如果某个脚本有编写错误的话（如少了一个"}"等），初始化时就不会发现这个错误，此错误将会直接出现在页面中，造成页面无法显示。
