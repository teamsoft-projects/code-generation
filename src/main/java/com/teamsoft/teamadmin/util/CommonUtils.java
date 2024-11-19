package com.teamsoft.teamadmin.util;

import com.teamsoft.teamadmin.model.ModelInfo;
import com.teamsoft.teamadmin.model.SqlExcute;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 公共工具类
 * @author zhangcc
 */
public class CommonUtils {
	/**
	 * 获取指定类的源文件绝对路径
	 */
	public static <T> String getClassAbsolutePath(Class<T> clazz) {
		String projectRoot = new File("").getAbsolutePath();

		String packageName = clazz.getPackage().getName();
		packageName = "/" + packageName.replaceAll("\\.", "/");
		return projectRoot + "/src/" + new File(clazz.getName()).getPath().replace(".", "/") + ".java";
	}

	/**
	 * 判断集合中是否存在指定字符串, 忽略大小写
	 */
	public static boolean containsIgnoreCase(Collection<String> collection, String str) {
		for (String temp : collection) {
			if (str.equalsIgnoreCase(temp)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断字符串是否为null或长度为0
	 */
	public static boolean hasLength(String str) {
		return str != null && str.length() != 0;
	}

	/**
	 * 字符串是否以指定后缀结尾
	 * @param str 待检查字符串 suffix 后缀
	 * @return 是否以指定后缀结尾
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		return !(!hasLength(str) || !hasLength(prefix) || str.length() < prefix.length())
				&& str.substring(0, prefix.length()).equalsIgnoreCase(prefix);
	}

	/**
	 * 字符串是否以指定后缀结尾
	 * @param str 待检查字符串 suffix 后缀
	 * @return 是否以指定后缀结尾
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		return !(!hasLength(str) || !hasLength(suffix) || str.length() < suffix.length())
				&& str.substring(str.length() - suffix.length()).equalsIgnoreCase(suffix);
	}

	/**
	 * 读取文件, 并将读取结果生成字符串
	 * @param source 待读取文件
	 * @return 文件字符串形式
	 */
	public static String fileRead(File source) {
		if (source == null || !source.exists() || !source.isFile()) {
			return null;
		}
		BufferedReader br = null;
		StringBuilder result = new StringBuilder(64);
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
			String line;
			while ((line = br.readLine()) != null) {
				result.append(line).append("\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	/**
	 * 对象转Map
	 */
	public static Map<String, String> objectToMap(Object obj) {
		Map<String, String> retmMap = new HashMap<>();
		try {
			Field[] fs = obj.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				retmMap.put(f.getName(), String.valueOf(f.get(obj)));
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return retmMap;
	}

	/**
	 * 是否为NULL或空串
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * 生成数据库相关数据(菜单、功能、权限等)
	 */
	public static void generateDatabase(List<ModelInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		try {
			Map<String, List<ModelInfo>> map = new LinkedHashMap<>();
			for (ModelInfo modelInfo : list) {
				String parentName = modelInfo.getParentName();
				if (isEmpty(parentName)) {
					continue;
				}
				List<ModelInfo> modelInfos = map.computeIfAbsent(parentName, k -> new ArrayList<>());
				String packageName = modelInfo.getPackageName();
				int packageNameIdx = packageName.lastIndexOf(".");
				String packagePrefix = packageNameIdx > 0 ? packageName.substring(0, packageNameIdx).replace(".", "/") : "";
				packagePrefix = packagePrefix.length() == 0 ? "" : packagePrefix + "/";
				modelInfo.setPackagePrefix(packagePrefix);
				modelInfos.add(modelInfo);
			}
			if (map.isEmpty()) {
				return;
			}
			List<SqlExcute> sqls = new ArrayList<>();
			int sortParent = 100;
			for (Map.Entry<String, List<ModelInfo>> entry : map.entrySet()) {
				String parentName = entry.getKey();
				// 查询菜单信息
				ResultSet res = DBUtils.excuteQuery("select * from sys_menu where name=? limit 1", parentName);
				String parentId;
				assert res != null;
				if (res.next()) {
					parentId = res.getString("id");
				} else {
					parentId = generateUUId();
					// 上级菜单不存在, 创建上级菜单
					SqlExcute sqlExcute = new SqlExcute();
					sqlExcute.setSql("insert into sys_menu values(?,?,?,?,?,?,?)");
					sqlExcute.setParams(parentId).setParams("top").setParams(parentName).setParams("layui-icon-file-b")
							.setParams("").setParams(sortParent++).setParams(Constants.System.FMT_YMDHMS.format(new Date()));
					sqls.add(sqlExcute);
				}
				DBUtils.excuteQuery("end");
				int sort = 1;
				// 循环并创建子菜单
				for (ModelInfo modelInfo : entry.getValue()) {
					String tableName = modelInfo.getTableName();
					String packagePrefix = modelInfo.getPackagePrefix();
					String pathPrefix = modelInfo.getPathPrefix();
					pathPrefix = pathPrefix == null ? "" : pathPrefix + "/";
					// 根据表名获取实体类名
					String modelName = tableName.substring(tableName.indexOf("_") + 1);
					while (modelName.contains("_")) {
						int index = modelName.indexOf("_");
						modelName = modelName.substring(0, index) + modelName.substring(index + 1, index + 2).toUpperCase() + modelName.substring(index + 2);
					}
					// 创建子菜单SQL
					SqlExcute sqlExcute = new SqlExcute();
					String menuId = generateUUId();
					sqlExcute.setSql("insert into sys_menu values(?,?,?,?,?,?,?)");
					sqlExcute.setParams(menuId)
							.setParams(parentId)
							.setParams(modelInfo.getModelName())
							.setParams("layui-icon-file-b")
							.setParams(pathPrefix + packagePrefix + modelName + "/list")
							.setParams(sort++)
							.setParams(Constants.System.FMT_YMDHMS.format(new Date()));
					sqls.add(sqlExcute);
					// 创建菜单功能SQL
					// 新增
					String sql = "insert into sys_menu_function values(?,?,?,?,?,?,?,?,?)";
					SqlExcute sqlExcuteFuncAdd = new SqlExcute();
					sqlExcuteFuncAdd.setSql(sql);
					sqlExcuteFuncAdd.setParams(generateUUId())
							.setParams(menuId)
							.setParams(modelName + "-add")
							.setParams("add")
							.setParams("layui-icon-add-1")
							.setParams(pathPrefix + packagePrefix + modelName + "/edit")
							.setParams("新增")
							.setParams(1)
							.setParams(Constants.System.FMT_YMDHMS.format(new Date()));
					sqls.add(sqlExcuteFuncAdd);
					// 修改
					SqlExcute sqlExcuteFuncEdit = new SqlExcute();
					sqlExcuteFuncEdit.setSql(sql);
					sqlExcuteFuncEdit.setParams(generateUUId())
							.setParams(menuId)
							.setParams(modelName + "-edit")
							.setParams("edit")
							.setParams("layui-icon-edit")
							.setParams(pathPrefix + packagePrefix + modelName + "/edit")
							.setParams("修改").setParams(2).setParams(Constants.System.FMT_YMDHMS.format(new Date()));
					sqls.add(sqlExcuteFuncEdit);
					// 删除
					SqlExcute sqlExcuteFuncRemove = new SqlExcute();
					sqlExcuteFuncRemove.setSql(sql);
					sqlExcuteFuncRemove.setParams(generateUUId())
							.setParams(menuId)
							.setParams(modelName + "-remove")
							.setParams("remove")
							.setParams("layui-icon-delete")
							.setParams(pathPrefix + packagePrefix + modelName + "/remove")
							.setParams("删除")
							.setParams(3)
							.setParams(Constants.System.FMT_YMDHMS.format(new Date()));
					sqls.add(sqlExcuteFuncRemove);
				}
			}
			DBUtils.excute(sqls);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成32位的UUId
	 * @return UUId
	 */
	public static String generateUUId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 蛇形命名改驼峰
	 */
	public static String snakeToCamel(String snakeCase) {
		StringBuilder camelCase = new StringBuilder();
		String[] words = snakeCase.split("_");
		boolean isFirst = true;
		for (String word : words) {
			if (word.isEmpty()) {
				continue;
			}
			if (isFirst) {
				camelCase.append(word);
				isFirst = false;
			} else {
				camelCase.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
			}
		}
		return camelCase.toString();
	}
}