package uitl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.JOptionPane;

@SuppressWarnings("unused")
public class MySQL {
	private static String ServerIP;
	private static int ServerPort;
	private static String driver;
	private static String url;
//serverTimeZone=UTC
	private static String user="wfx";
	private static String pwd="123456";
	
	public static Connection getConnection(){
	
		if(driver==null || url==null){
			Properties pps = new Properties();
			try {
				File curDir = new File(".");
				String parent = curDir.getAbsolutePath();
				pps.load(new FileInputStream(
						parent.substring(0,parent.length()-1)+"src/Client_cfg.ini"));
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
				return null;
			} catch (IOException e2) {
				e2.printStackTrace();
				return null;
			}
			
			driver = pps.getProperty("driver");
			url = pps.getProperty("url");
			
			ServerIP = pps.getProperty("ServerIP");
			ServerPort = Integer.parseInt(pps.getProperty("ServerPort"));
		}

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1){
			e1.printStackTrace();
			return null;
		}
	
		try {
			return DriverManager.getConnection(url,user,pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void closeRes(ResultSet rs,Statement stm,Connection conn){
		try{
			if(rs!=null){
				rs.close();
			}
			if(stm!=null){
				stm.close();
			}
			if(conn!=null){
				conn.close();
			}
		}catch(SQLException e){
			System.out.println("关闭数据库连接时发生异常!");
			e.printStackTrace();
		}
	}
	public static String getServerIP(){
		return ServerIP;
	}
	public static int getServerPort(){
		return ServerPort;
	}
}
