package ru.techstandard.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBConnect {
	private static Connection conn = null;
	private static String url = "jdbc:mysql://localhost/techstandard?useUnicode=true&characterEncoding=UTF-8";
	private static String user = "tsmysqlusr";
	private static String pass = "tsmysqlusr";
	
	private static Connection initConnection () {

			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(url, user, pass);
				conn.setAutoCommit(true);
				MyLogger.info("connection established");
			} catch (Exception e) {
				MyLogger.warning("[DBConnect] getConnection exception: "+e.getMessage());
				MyLogger.warning("[DBConnect] getConnection exception: "+e.toString());
				System.out.println("(syso) [DBConnect] getConnection exception:"+e.getMessage()+"| "+e.toString());
			}
		return conn;
	}
	
	public static Connection getConnection() {
		try {
			if (conn == null || !conn.isValid(1))
				conn = initConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			MyLogger.warning(e.getMessage());
		}
		return conn;
	}
	
//	public static String saveString(String input) {
//		String str = input == null?null:input.trim();
//		return ((str == null || str.equalsIgnoreCase("null"))?null:"'"+str+"'");
//	}
	
	public static String saveDate(Date input) {
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
		return (input == null ? null : "'"+dt.format(input)+"'");
	}
	
	public static String saveDateTime(Date input) {
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return (input == null ? null : "'"+dt.format(input)+"'");
	}

	public static String getSubDeptsList(int deptId) {
		List<Integer> subDepts = getSubDepts(deptId);
		StringBuilder subDeptsList = new StringBuilder();
		subDeptsList.append(deptId);
		for (int idx = 0; idx < subDepts.size(); idx++) {
			subDeptsList.append(",");
			subDeptsList.append(subDepts.get(idx));
		}
		return subDeptsList.toString();
	}
	
	private static List<Integer> getSubDepts(int deptId) {
		conn = getConnection();
		List<Integer> subs = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM departments WHERE parent_id="+deptId);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				subs.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getSubDepts exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] getSubDepts exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] getSubDepts exception: "+sqle.toString());
		}
		List<Integer> tmp = new ArrayList<Integer>();
		for (int idx=0; idx<subs.size(); idx++) {
			tmp.addAll(getSubDepts(subs.get(idx)));
		}
		subs.addAll(tmp);
		return subs;
	}
}
