package com.teamsoft.teamadmin.util;

import com.teamsoft.teamadmin.model.Column;
import com.teamsoft.teamadmin.model.SqlExcute;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据库连接工具
 * @author zhangcc
 * @version 2017/8/25
 */
public class DBUtils {
	private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static String DB_SCHEMA;
	private static String DB_URL;
	private static String DB_USER;
	private static String DB_PASS;
	private static Connection con;
	private static PreparedStatement pstmt;
	private static ResultSet res;

	static {
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用配置文件初始化JDBC连接信息
	 */
	public static void initDatabase() {
		DB_URL = Constants.System.GLOBAL_PARAMS.get("dburl");
		// 获取SCHEMA
		DB_SCHEMA = DB_URL.substring(DB_URL.lastIndexOf("/") + 1);
		int index = DB_SCHEMA.indexOf("?");
		if (index > 0) {
			DB_SCHEMA = DB_SCHEMA.substring(0, index);
		}
		DB_URL = DB_URL + "?characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai";
		DB_USER = Constants.System.GLOBAL_PARAMS.get("dbuser");
		DB_PASS = Constants.System.GLOBAL_PARAMS.get("dbpass");
	}

	/**
	 * 创建连接
	 */
	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	}

	/**
	 * 关闭连接
	 */
	private static void close(ResultSet res, Statement stmt, Connection con) {
		try {
			if (res != null) {
				res.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭连接
	 */
	private static void close(Statement stmt, Connection con) {
		close(null, stmt, con);
	}

	/**
	 * 执行批量SQL
	 * @param list 数据List
	 * @param sql  查询SQL
	 */
	public static <T> void excute(List<T> list, String sql) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = getConnection();
			for (Object obj : list) {
				Pattern pattern = Pattern.compile(":(\\w+)");
				Matcher matcher = pattern.matcher(sql);
				String resultSql = matcher.replaceAll("?");
				matcher.reset();
				pstmt = con.prepareStatement(resultSql);
				int index = 1;
				while (matcher.find()) {
					String key = matcher.group(1);
					Method method = obj.getClass().getDeclaredMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
					pstmt.setObject(index++, method.invoke(obj));
				}
				pstmt.execute();
				System.out.println(resultSql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(pstmt, con);
		}
	}

	/**
	 * 执行批量SQL
	 * @param list 数据List
	 */
	public static void excute(List<SqlExcute> list) {
		PreparedStatement pstmt = null;
		try (Connection con = getConnection()) {
			for (SqlExcute excuter : list) {
				pstmt = con.prepareStatement(excuter.getSql());
				for (int i = 0; i < excuter.getParams().size(); i++) {
					pstmt.setObject(i + 1, excuter.getParams().get(i));
				}
				pstmt.execute();
				System.out.println("执行SQL " + excuter.getSql() + " 成功， 参数 " + excuter.getParams().stream().map(String::valueOf).collect(Collectors.joining(",")));
				pstmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取数据列表
	 */
	public static <T> List<T> getList(String sql, List<Object> params, Class<T> clazz) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet res = null;
		List<T> retList = new ArrayList<>();
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			res = pstmt.executeQuery();
			Field[] fs = clazz.getDeclaredFields();
			while (res.next()) {
				// TODO
				/*T obj = clazz.newInstance();
				for (Field f : fs) {

				}*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(res, pstmt, con);
		}

		return retList;
	}

	/**
	 * 执行查询
	 */
	public static ResultSet excuteQuery(String sql, Object... params) {
		if (sql.equalsIgnoreCase("end")) {
			close(res, pstmt, con);
			return null;
		}
		try {
			if (con == null || con.isClosed()) {
				con = getConnection();
			}
			pstmt = con.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			res = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 根据数据库生成实体类
	 * @param tabName 表名
	 */
	static List<Column> getTableInfo(String tabName) {
		if (tabName == null) {
			return null;
		}
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet res = null;
		List<Column> result = new ArrayList<>();
		try {
			con = getConnection();
			String sql = "SELECT COLUMN_NAME COLNAME, DATA_TYPE DATATYPE, COLUMN_COMMENT COLCOMMENT " +
					"FROM INFORMATION_SCHEMA.COLUMNS " +
					"WHERE TABLE_NAME = '" + tabName + "' AND TABLE_SCHEMA='" + DB_SCHEMA + "' " +
					"ORDER BY ORDINAL_POSITION";
			pstmt = con.prepareStatement(sql);
			res = pstmt.executeQuery();
			while (res.next()) {
				Column column = new Column();
				column.setName(res.getString("colName"));
				String dataType = res.getString("dataType");
				if ("varchar".equals(dataType) || "longtext".equals(dataType) || "text".equals(dataType)) {
					dataType = "String";
				} else if ("int".equals(dataType) || "smallint".equals(dataType) || "tinyint".equals(dataType)) {
					dataType = "Integer";
				} else if ("double".equals(dataType)) {
					dataType = "Double";
				} else if ("datetime".equals(dataType)) {
					dataType = "Date";
				} else if ("decimal".equals(dataType)) {
					dataType = "BigDecimal";
				} else if ("bigint".equals(dataType)) {
					dataType = "BigInteger";
				} else if ("float".equals(dataType)) {
					dataType = "Float";
				} else {
					throw new Exception("不支持的数据类型: " + dataType);
				}
				column.setType(dataType);
				column.setComment(res.getString("colComment"));
				result.add(column);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(res, pstmt, con);
		}
		return result;
	}
}