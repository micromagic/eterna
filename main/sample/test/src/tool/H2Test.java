package tool;
 
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 测试h2数据库是否能够读取.
 */
public class H2Test 
{
	public static void main(String[] params) 
			throws SQLException, ClassNotFoundException
	{
		Class.forName("org.h2.Driver");
		String filePath = H2Test.class.getResource("H2Test.class").getFile();
		File thisFile = new File(filePath);
		// 获取项目所在的目录 test/build/classes/tool/H2Test.class, 需要4个parent到test目录
		File projectPath = thisFile.getParentFile().getParentFile().getParentFile().getParentFile();
		System.out.println("projectPath:" + projectPath);
		// 获取数据库文件所在的目录, test//WebContent/WEB-INF/db
		String baseDir = projectPath + "/WebContent/WEB-INF/db";
		Connection conn1 = DriverManager.getConnection("jdbc:h2:" + baseDir + "/test", "sa", "sa"); 
		System.out.println(conn1.getMetaData().getDatabaseProductName());
		conn1.setAutoCommit(false);
		ResultSet rs = conn1.createStatement().executeQuery("select * from my_table");
		while (rs.next())
		{
			System.out.print(rs.getString(1) + ",");
			System.out.print(rs.getString(2) + ",");
			System.out.print(rs.getString(3) + ",");
			System.out.print(rs.getString(4) + ",");
			System.out.println(rs.getString(5)); 
		}
		rs.close();
		conn1.createStatement().executeUpdate("update my_table set name = 'tt测试3' where id = 't003'");
		Connection conn2 = DriverManager.getConnection("jdbc:h2:" + baseDir + "/test", "sa", "sa"); 
		conn2.setAutoCommit(false);
		rs = conn2.createStatement().executeQuery("select * from t_student");
		while (rs.next())
		{
			System.out.print(rs.getString(1) + ",");
			System.out.print(rs.getString(2) + ",");
			System.out.println(rs.getString(3));
		}			
		T t = new T();
		t.conn = conn1;
		t.start();
		System.out.println("begin update 2:" + System.currentTimeMillis());	
		try
		{
			conn2.createStatement().executeUpdate("update my_table set name = 'tt测试t3' where id = 't003'");
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		System.out.println("  end update 2:" + System.currentTimeMillis());
		conn2.commit();
		conn1.close();
		conn2.close();
	}
	
	static class T extends Thread
	{
		Connection conn;
		
		public void run()
		{
			try
			{
				sleep(800L);
				conn.commit();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
	}
	
}
