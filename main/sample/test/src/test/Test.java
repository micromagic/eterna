
package test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import self.micromagic.app.EternaServlet;
import self.micromagic.eterna.share.EternaInitialize;
import self.micromagic.util.Utility;

public class Test extends EternaServlet 
		implements EternaInitialize
{
	/*
	 * 定义h2数据库所在目录的属性名.
	 */
	public static final String H2_BASE_DIR_FLAG = "h2.baseDir";
	
	public Test()
	{
	}

	@Override
	public void init(ServletConfig config) 
			throws ServletException
	{
		if (Utility.getProperty(H2_BASE_DIR_FLAG) == null)
		{
			// 初始化h2数据库文件所在的目录
			String baseDir = config.getServletContext().getRealPath("/WEB-INF/db");
			Utility.setProperty(H2_BASE_DIR_FLAG, baseDir);
			System.out.println(H2_BASE_DIR_FLAG + ":" + baseDir);
		}
		super.init(config);
	}

	static long autoReloadTime()
	{
		// 设置最短文件更新时间检查间隔为5秒
		return 5000L;
	}

	static
	{
		// 注销H2自动注册的数据库驱动, 这样tomcat就不会在
		// clearReferencesJdbc时, 报数据库驱动未注销的警报
		org.h2.Driver.unload();
	}

	private static final long serialVersionUID = 2481944292489365997L;

}
