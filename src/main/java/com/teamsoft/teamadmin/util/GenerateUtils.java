package com.teamsoft.teamadmin.util;

import cn.hutool.core.util.StrUtil;
import com.mysql.cj.util.StringUtils;
import com.teamsoft.teamadmin.model.Column;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.teamsoft.teamadmin.util.Constants.System.GLOBAL_PARAMS;

/**
 * 自动生成代码
 * @author zhangcc
 * @version 2017/8/28
 */
public class GenerateUtils {
	// 项目根目录
	private static String outputPath;
	// 源码根目录(相对)
	private static String javaRoot;
	// 页面根目录(相对)
	private static String pageRoot;
	// 页面根目录Freemarker(相对)
	private static String pageRootFreeMarker;
	// 包根目录
	private static String packageRoot;
	// 忽略的字段（全局）
	private static final List<String> ignoreColumns = new ArrayList<>();
	// 忽略的字段-model
	private static final List<String> ignoreColumnsForModel = new ArrayList<>();
	// 忽略的字段-mapper.xml
	// 表名
	private static final List<String> ignoreColumnsForMapperUpdate = new ArrayList<>();
	private static String tableName;
	// 类名
	private static String modelName;
	// 模块名
	private static String commentName;
	// 包名(com.example.xxx xxx部分)
	private static String packageName;
	// 包名文件路径(xxx.xxx -> xxx/xxx)
	private static String packageNamePath;
	// 包路径(com.example.xxx 所有部分)
	private static String packagePath;
	// jsp文件路径前缀(从pageroot开始)
	private static String pagePrefix;
	// 前缀(controller和page路径前缀)
	// 路径前缀，影响Controller的RequestMapping和页面存放位置
	private static String pathPrefix;
	private static String pathSuffix;
	private static String preffix = "";
	// 公共包前缀
	private static String commonPackagePre = "com.teamsoft.framework.common";

	// 日期
	private static String date;
	// 字段集合
	private static List<Column> cols;

	static {
		ignoreColumns.add("id");
		ignoreColumnsForModel.add("creator_id");
		ignoreColumnsForMapperUpdate.addAll(Arrays.asList("creator_id", "create_time"));
	}

	/**
	 * 生成代码入口
	 * @param generateParams 代码生成参数
	 */
	public static void generate(Map<String, String> generateParams) {
		// 设置输出根目录
		outputPath = GLOBAL_PARAMS.get("outputPath");
		// 设置源码根目录
		javaRoot = GLOBAL_PARAMS.get("javaRoot");
		javaRoot = outputPath + "/" + javaRoot;
		// 设置页面根目录
		pageRoot = GLOBAL_PARAMS.get("pageRoot");
		pageRoot = outputPath + "/" + pageRoot;
		pageRootFreeMarker = GLOBAL_PARAMS.get("pageRootFreeMarker");
		pageRootFreeMarker = outputPath + "/" + pageRootFreeMarker;
		// 包名(com.example.xxx xxx部分)
		packageName = generateParams.get("packageName");
		// 包名文件路径
		packageNamePath = packageName.replace(".", "/");
		// jsp文件路径前缀(从pageroot开始)
		int pagePreffixIdx = packageName.indexOf(".");
		pagePrefix = pagePreffixIdx > 0 ? packageName.substring(0, pagePreffixIdx).replace(".", "/") : "";
		pagePrefix = pagePrefix.isEmpty() ? "" : pagePrefix + "/";
		// 设置包根路径
		packagePath = GLOBAL_PARAMS.get("packageRoot");
		packageRoot = javaRoot + "/" + packagePath.replace(".", "/");
		// 模块名(用户|菜单)
		commentName = generateParams.get("modelName");
		// 路径前缀，影响Controller的RequestMapping和页面存放位置
		pathPrefix = generateParams.get("pathPrefix");
		pathPrefix = StrUtil.isEmpty(pathPrefix) ? "" : pathPrefix + "/";

		// 表名
		tableName = generateParams.get("tableName");
		// 根据表名获取实体类名
		modelName = tableName.substring(tableName.indexOf("_") + 1);
		modelName = modelName.substring(0, 1).toUpperCase() + modelName.substring(1);

		// 路径后缀
		pathSuffix = "";
		String firstName = modelName;
		if (modelName.indexOf("_") > 0) {
			firstName = modelName.substring(0, modelName.indexOf("_"));
		}

		if (firstName.equalsIgnoreCase(generateParams.get("pathPrefix"))) {
			pathSuffix = modelName.substring(modelName.indexOf("_") + 1);
			pathSuffix = pathSuffix.substring(0, 1).toLowerCase() + pathSuffix.substring(1);
		} else {
			pathSuffix = modelName.substring(0, 1).toLowerCase() + modelName.substring(1);
		}

		while (pathSuffix.contains("_")) {
			int index = pathSuffix.indexOf("_");
			pathSuffix = pathSuffix.substring(0, index) + pathSuffix.substring(index + 1, index + 2).toUpperCase() + pathSuffix.substring(index + 2);
		}

		while (modelName.contains("_")) {
			int index = modelName.indexOf("_");
			modelName = modelName.substring(0, index) + modelName.substring(index + 1, index + 2).toUpperCase() + modelName.substring(index + 2);
		}
		// 当前日期
		date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
		// 获取字段列表
		cols = DBUtils.getTableInfo(tableName);

		generateModel(); // 生成实体类
		generateMapper(); // 生成Mapper接口
		generateMapperXml(); // 生成Mapper XML
		generateServiceInterface(); // 生成服务接口
		generateService(); // 生成服务类
		generateController(); // 生成控制类
		generateListPage(); // 生成列表页
		generateEditPage(); // 生成编辑页
	}

	/**
	 * 生成实体类
	 */
	private static void generateModel() {
		System.out.println("开始生成实体类...");
		if (cols == null || cols.isEmpty()) {
			System.out.println("未获取到表字段信息.");
			return;
		}
		String preffixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File modelFile = new File(packageRoot + "/" + preffixPath + packageNamePath + "/model/" + modelName + ".java");
		System.out.println("待生成文件路径: " + modelFile.getAbsolutePath());
		modelFile.getParentFile().mkdirs(); // 生成父目录
		StringBuilder sb = new StringBuilder(64);
		BufferedWriter bw = null;
		try {
			// 是否引入bigdecimal
			boolean isContainsBigDecimal = cols.stream()
					.anyMatch(col -> "BigDecimal".equals(col.getType()));
			boolean hasCreatorId = cols.stream()
					.anyMatch(col -> "creator_id".equals(col.getName()));
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(modelFile.toPath())));
			String preffixDot = CommonUtils.isEmpty(preffix) ? "" : preffix + ".";
			sb.append("package ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".model;\n\n");
			sb.append("import ").append(commonPackagePre).append(".model.Entity;\nimport lombok.Data;\nimport lombok.EqualsAndHashCode;\n");
			sb.append("import org.apache.ibatis.type.Alias;\n");
			if (isContainsBigDecimal) {
				sb.append("import java.math.BigDecimal;\n");
			}
			if (hasCreatorId) {
				sb.append("import com.teamsoft.framework.common.annotation.Translate;\n");
				sb.append("import static com.teamsoft.framework.common.core.CommonConstants.System.CACHE_KEY_USER_LIST;\n");
			}
			sb.append("\n");
			sb.append("/**\n").append(" * ").append(commentName).append("实体类\n");
			sb.append(" * @author zhangcc\n * @version ").append(date).append("\n */\n");
			sb.append("@Data\n@Alias(\"").append(modelName).append("\")\n@EqualsAndHashCode(callSuper = true)\n");
			sb.append("public class ").append(modelName).append(" extends Entity {\n");
			sb.append("\tprivate static final long serialVersionUID = 1L;\n\n");
			for (Column col : cols) {
				String colName = col.getName();
				if (CommonUtils.containsIgnoreCase(ignoreColumns, colName) || CommonUtils.containsIgnoreCase(ignoreColumnsForModel, colName)) {
					continue;
				}
				sb.append("\t/** ").append(col.getComment()).append(" */\n");
				sb.append("\tprivate ").append(col.getType()).append(" ").append(CommonUtils.snakeToCamel(colName)).append(";\n");
			}
			if (hasCreatorId) {
				sb.append("\t// VO\n")
						.append("\t@Translate(from = \"creatorId\", groupCode = CACHE_KEY_USER_LIST)\n")
						.append("\tprivate String creator;\n");
			}
			sb.append("}");
			bw.write(sb.toString());
			bw.flush();
			System.out.println("生成实体类完成.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 生成Mapper接口
	 */
	private static void generateMapper() {
		System.out.println("开始生成Mapper接口...");
		String prefixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File mapperFile = new File(packageRoot + "/" + prefixPath + packageNamePath + "/mapper/" + modelName + "Mapper.java");
		System.out.println("待生成文件路径: " + mapperFile.getAbsolutePath());
		mapperFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		StringBuilder sb = new StringBuilder(64);
		try {
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(mapperFile.toPath())));
			String preffixDot = CommonUtils.isEmpty(preffix) ? "" : preffix + ".";
			sb.append("package ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".mapper;\n\n");
			sb.append("import ").append(commonPackagePre).append(".mapper.CommonMapper;\n");
			sb.append("import ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".model.").append(modelName).append(";\n\n");
			sb.append("/**\n").append(" * ").append(commentName).append("数据库操作接口\n");
			sb.append(" * @author zhangcc\n * @version ").append(date).append("\n */\n");
			sb.append("public interface ").append(modelName).append("Mapper").append(" extends CommonMapper<").append(modelName).append("> " +
					"{\n").append("}");
			bw.write(sb.toString());
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("生成Mapper接口完成.");
	}

	/**
	 * 生成Mapper XML
	 */
	private static void generateMapperXml() {
		System.out.println("开始生成Mapper XML...");
		String prefixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File mapperFile = new File(packageRoot + "/" + prefixPath + packageNamePath + "/mapper/" + modelName + "Mapper.xml");
		System.out.println("待生成文件路径: " + mapperFile.getAbsolutePath());
		mapperFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		StringBuilder sb = new StringBuilder(64);
		try {
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(mapperFile.toPath())));
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
			String preffixDot = CommonUtils.isEmpty(preffix) ? "" : preffix + ".";
			sb.append("<mapper namespace=\"").append(packagePath).append(".").append(preffixDot).append(packageName).append(".mapper.").append(modelName).append("Mapper\">\n");
			sb.append("\t<select id=\"listByEntity\" resultType=\"").append(modelName).append("\">\n");
			sb.append("\t\tSELECT *\n\t\tFROM ").append(tableName.toUpperCase()).append("\n");
			sb.append("\t\t<if test=\"page != null\">\n\t\t\tLIMIT ${page.pageStart}, ${page.pageSize}\n");
			sb.append("\t\t</if>\n\t</select>\n");
			sb.append("\t<select id=\"countByEntity\" resultType=\"Integer\">\n");
			sb.append("\t\tSELECT COUNT(1)\n\t\tFROM ").append(tableName.toUpperCase()).append("\n\t</select>\n");
			sb.append("\t<select id=\"get\" resultType=\"").append(modelName).append("\">\n");
			sb.append("\t\tSELECT *\n\t\tFROM ").append(tableName.toUpperCase()).append("\n\t\tWHERE ID = #{id}\n\t\tLIMIT 1\n\t</select>\n");
			sb.append("\t<insert id=\"save\">\n\t\tINSERT INTO ").append(tableName.toUpperCase()).append("\n\t\t(");
			StringBuilder sb2 = new StringBuilder();
			sb2.append("\t\tVALUES (");
			StringBuilder sb3 = new StringBuilder();
			sb3.append("\t\tSET ");
			// 去除忽略的列
			List<Column> colsTemp = cols.stream()
					.filter(column -> !CommonUtils.containsIgnoreCase(ignoreColumns, column.getName()) || "id".equalsIgnoreCase(column.getName()))
					.collect(Collectors.toList());
			// update已设置
			boolean isUpdateSetted = false;
			for (int i = 0; i < colsTemp.size(); i++) {
				Column col = colsTemp.get(i);
				boolean isLast = i == colsTemp.size() - 1;
				String camelColName = CommonUtils.snakeToCamel(col.getName());
				sb.append(col.getName().toUpperCase());
				if ("create_time".equalsIgnoreCase(col.getName())) {
					sb2.append("NOW()");
				} else {
					sb2.append("#{entity.").append(camelColName).append("}");
				}
				if (!"id".equalsIgnoreCase(col.getName()) && !CommonUtils.containsIgnoreCase(ignoreColumnsForMapperUpdate, col.getName())) {
					if (isUpdateSetted) {
						sb3.append(",\n\t\t\t");
					}
					sb3.append(col.getName().toUpperCase()).append(" = #{entity.").append(camelColName).append("}");
					isUpdateSetted = true;
				}
				if (!isLast) {
					sb.append(", ");
					sb2.append(", ");
				} else {
					sb.append(")\n");
					sb2.append(")\n");
					sb3.append("\n");
				}
			}
			sb.append(sb2);
			sb.append("\t</insert>\n");
			sb.append("\t<update id=\"update\">\n");
			sb.append("\t\tUPDATE ").append(tableName.toUpperCase()).append("\n");
			sb.append(sb3);
			sb.append("\t\tWHERE ID = #{entity.id}\n\t</update>\n");
			sb.append("\t<delete id=\"removeAll\">\n");
			sb.append("\t\tDELETE FROM ").append(tableName.toUpperCase()).append("\n\t\tWHERE ID IN\n");
			sb.append("\t\t<foreach collection=\"ids\" item=\"item\" open=\"(\" close=\")\" separator=\",\">\n");
			sb.append("\t\t\t#{item}\n\t\t</foreach>\n\t</delete>\n</mapper>");
			bw.write(sb.toString());
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("生成Mapper XML完成.");
	}

	/**
	 * 生成Service接口
	 */
	private static void generateServiceInterface() {
		System.out.println("开始生成Service Interface...");
		String preffixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File iServiceFile = new File(packageRoot + "/" + preffixPath + packageNamePath + "/service/I" + modelName + "Service.java");
		System.out.println("待生成文件路径: " + iServiceFile.getAbsolutePath());
		iServiceFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		StringBuilder sb = new StringBuilder(64);
		try {
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(iServiceFile.toPath())));
			String preffixDot = CommonUtils.isEmpty(preffix) ? "" : preffix + ".";
			sb.append("package ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".service;\n\n");
			sb.append("import ").append(commonPackagePre).append(".service.ICommonService;\n");
			sb.append("import ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".model.").append(modelName).append(";\n\n");
			sb.append("/**\n * ").append(commentName).append("服务接口\n * @author zhangcc\n * @version ").append(date).append("\n */\n");
			sb.append("public interface I").append(modelName).append("Service extends ICommonService<");
			sb.append(modelName).append("> {\n}");
			bw.write(sb.toString());
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("生成Service Interface完成.");
	}

	/**
	 * 生成Service服务类
	 */
	private static void generateService() {
		System.out.println("开始生成Service服务类...");
		String preffixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File serviceFile = new File(packageRoot + "/" + preffixPath + packageNamePath + "/service/" + modelName + "ServiceImpl.java");
		System.out.println("待生成文件路径: " + serviceFile.getAbsolutePath());
		serviceFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		StringBuilder sb = new StringBuilder(64);
		try {
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(serviceFile.toPath())));
			String preffixDot = CommonUtils.isEmpty(preffix) ? "" : preffix + ".";
			sb.append("package ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".service;\n\n");
			sb.append("import ").append(commonPackagePre).append(".service.CommonService;\n");
			sb.append("import ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".mapper.").append(modelName).append("Mapper;\n");
			sb.append("import ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".model.").append(modelName).append(";\n");
			sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
			sb.append("import org.springframework.stereotype.Service;\n\n");
			sb.append("/**\n * ").append(commentName).append("服务类\n * @author zhangcc\n * @version ").append(date).append("\n */\n");
			sb.append("@Service\npublic class ").append(modelName).append("ServiceImpl extends CommonService<");
			sb.append(modelName).append("> implements I").append(modelName).append("Service {\n");
			sb.append("\t@Autowired\n\tpublic ").append(modelName).append("ServiceImpl(").append(modelName).append("Mapper mapper) {\n");
			sb.append("\t\tsuper.mapper = mapper;\n\t}\n}");
			bw.write(sb.toString());
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("生成Service服务类完成.");
	}

	/**
	 * 生成Controller类
	 */
	private static void generateController() {
		System.out.println("开始生成Controller类...");
		String preffixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File controlFile = new File(packageRoot + "/" + preffixPath + packageNamePath + "/controller/" + modelName + "Controller.java");
		System.out.println("待生成文件路径: " + controlFile.getAbsolutePath());
		String modelNameLow = modelName.substring(0, 1).toLowerCase() + modelName.substring(1);
		int idx = packageNamePath.indexOf("/");
		if (idx > 0) {
			String packageNamePathLow = packageNamePath.substring(0, 1).toLowerCase() + packageNamePath.substring(1);
			modelNameLow = packageNamePathLow.substring(0, idx) + "/" + modelNameLow;
		}
		controlFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		StringBuilder sb = new StringBuilder(64);
		try {
			boolean hasCreatorId = cols.stream()
					.anyMatch(col -> "creator_id".equals(col.getName()));
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(controlFile.toPath())));
			String preffixDot = CommonUtils.isEmpty(preffix) ? "" : preffix + ".";
			sb.append("package ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".controller;\n\n");
			sb.append("import ").append(commonPackagePre).append(".controller.CommonController;\n");
			sb.append("import ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".model.").append(modelName).append(";\n");
			sb.append("import ").append(packagePath).append(".").append(preffixDot).append(packageName).append(".service.I").append(modelName).append("Service;\n");
			sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
			sb.append("import org.springframework.stereotype.Controller;\n");
			sb.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
			if (hasCreatorId) {
				sb.append("import com.teamsoft.framework.sys.model.User;\n");
				sb.append("import com.teamsoft.mgr.common.util.WebUtil;\n");
				sb.append("import org.springframework.util.StringUtils;\n");
			}
			sb.append("\n");
			sb.append("/**\n * ").append(commentName).append("控制类\n * @author zhangcc\n * @version ").append(date).append("\n */\n");
			sb.append("@Controller\n@RequestMapping(\"").append(pathPrefix).append(preffixPath).append(pathSuffix).append("\")\n");
			sb.append("public class ").append(modelName).append("Controller extends CommonController<").append(modelName).append("> {\n");
			sb.append("\t/**\n\t * 构造方法注入\n\t */\n\t@Autowired\n");
			sb.append("\tpublic ").append(modelName).append("Controller(I").append(modelName).append("Service service) {\n");
			sb.append("\t\tthis.service = service;\n\t}");
			if (hasCreatorId) {
				sb.append("\n\n\t/**\n")
						.append("\t * 保存前操作\n")
						.append("\t */\n")
						.append("\tprotected void beforeSave(").append(modelName).append(" entity) {\n")
						.append("\t\t// 新增时增加创建人ID\n")
						.append("\t\tif (!StringUtils.hasLength(entity.getId())) {\n")
						.append("\t\t\tUser user = WebUtil.getCurrendUser();\n")
						.append("\t\t\tentity.setCreatorId(user.getId());\n")
						.append("\t\t}\n")
						.append("\t}");
			}
			sb.append("\n}");
			bw.write(sb.toString());
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("生成Controller类完成.");
	}

	/**
	 * 生成列表页
	 */
	private static void generateListPage() {
		System.out.println("开始生成列表页...");
		String modelNameLow = modelName.substring(0, 1).toLowerCase() + modelName.substring(1);
		String prefixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File outFile;
		if ("FreeMarker".equals(GLOBAL_PARAMS.get("pageType"))) {
			outFile = new File(pageRootFreeMarker + "/" + pathPrefix + prefixPath + pagePrefix + pathSuffix + "/list.ftl");
		} else {
			outFile = new File(pageRoot + "/" + pathPrefix + prefixPath + pagePrefix + pathSuffix + "/list.jsp");
		}
		System.out.println("待生成文件路径: " + outFile.getAbsolutePath());
		outFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outFile.toPath())));
			String listPageTemplate;
			if ("FreeMarker".equals(GLOBAL_PARAMS.get("pageType"))) {
				listPageTemplate = Constants.System.TEMPLATES.get("listPage.freemarker");
			} else {
				listPageTemplate = Constants.System.TEMPLATES.get("listPage");
			}
			Map<String, String> data = new HashMap<>();
			data.put("modelNameLow", modelNameLow);
			data.put("packageName", packageName);
			data.put("pagePreffix", pagePrefix);
			// 表头
			StringBuilder colsBuilder = new StringBuilder();
			// 表内容
			StringBuilder colsDataBuilder = new StringBuilder();
			// 去除忽略的列
			List<Column> colsTemp = cols.stream()
					.filter(column -> !CommonUtils.containsIgnoreCase(ignoreColumns, column.getName()))
					.collect(Collectors.toList());
			// 生成显示的Table项
			boolean isFirst = true;
			for (int i = 0; i < colsTemp.size(); i++) {
				Column col = colsTemp.get(i);
				if (!isFirst) {
					colsBuilder.append("\t\t\t\t\t\t\t");
					colsDataBuilder.append("\t\t\t");
				}
				if ("creator_id".equalsIgnoreCase(col.getName())) {
					colsBuilder.append("<th>").append("创建人").append("</th>");
					colsDataBuilder.append("<td>{{=item.").append(CommonUtils.snakeToCamel("creator")).append("}}</td>");
				} else {
					colsBuilder.append("<th>").append(col.getComment()).append("</th>");
					colsDataBuilder.append("<td>{{=item.").append(CommonUtils.snakeToCamel(col.getName())).append("}}</td>");
				}
				if (i < colsTemp.size() - 1) {
					colsBuilder.append("\n");
					colsDataBuilder.append("\n");
				}
				isFirst = false;
			}
			data.put("cols", colsBuilder.toString());
			data.put("colsData", colsDataBuilder.toString());
			data.put("preffix", pathPrefix + prefixPath);
			data.put("pathSuffix", pathSuffix);
			bw.write(SwingUtils.template(listPageTemplate, data));
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("生成列表页完成.");
	}

	/**
	 * 生成编辑页
	 */
	private static void generateEditPage() {
		System.out.println("开始生成编辑页...");
		String modelNameLow = modelName.substring(0, 1).toLowerCase() + modelName.substring(1);
		String preffixPath = CommonUtils.isEmpty(preffix) ? "" : preffix + "/";
		File outFile;
		if ("FreeMarker".equals(GLOBAL_PARAMS.get("pageType"))) {
			outFile = new File(pageRootFreeMarker + "/" + pathPrefix + preffixPath + pagePrefix + pathSuffix + "/edit.ftl");
		} else {
			outFile = new File(pageRoot + "/" + pathPrefix + preffixPath + pagePrefix + pathSuffix + "/edit.jsp");
		}
		System.out.println("待生成文件路径: " + outFile.getAbsolutePath());
		outFile.getParentFile().mkdirs(); // 生成父目录
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outFile.toPath())));
			String editPageTemplate;
			if ("FreeMarker".equals(GLOBAL_PARAMS.get("pageType"))) {
				editPageTemplate = Constants.System.TEMPLATES.get("editPage.freemarker");
			} else {
				editPageTemplate = Constants.System.TEMPLATES.get("editPage");
			}
			Map<String, String> data = new HashMap<>();
			data.put("modelNameLow", modelNameLow);
			data.put("packageName", packageName);
			data.put("pagePreffix", pagePrefix);
			data.put("pathSuffix", pathSuffix);
			// 数据列
			StringBuilder colsBuilder = new StringBuilder();
			// 去除忽略的列
			List<Column> colsTemp = cols.stream()
					.filter(column -> !CommonUtils.containsIgnoreCase(ignoreColumns, column.getName()) && !CommonUtils.containsIgnoreCase(ignoreColumnsForMapperUpdate, column.getName()))
					.collect(Collectors.toList());
			// 生成显示的Table项
			boolean isFirst = true;
			for (int i = 0; i < colsTemp.size(); i++) {
				Column col = colsTemp.get(i);
				if (!isFirst) {
					colsBuilder.append("\t\t");
				}
				colsBuilder.append("<div class=\"layui-form-item\">\n");
				colsBuilder.append("\t\t\t<label class=\"layui-form-label\">").append(col.getComment()).append("</label>\n");
				colsBuilder.append("\t\t\t<div class=\"layui-input-block\">\n");
				colsBuilder.append("\t\t\t\t<input name=\"").append(CommonUtils.snakeToCamel(col.getName()));
				colsBuilder.append("\" placeholder=\"请输入").append(col.getComment()).append("\" class=\"layui-input\" autocomplete=\"off\" ");
				colsBuilder.append("value=\"${resultInfo.data.").append(CommonUtils.snakeToCamel(col.getName())).append("}\">\n");
				colsBuilder.append("\t\t\t</div>\n\t\t</div>");
				if (i < colsTemp.size() - 1) {
					colsBuilder.append("\n");
				}
				isFirst = false;
			}
			data.put("cols", colsBuilder.toString());
			data.put("preffix", pathPrefix + preffixPath);
			bw.write(SwingUtils.template(editPageTemplate, data));
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("生成编辑页完成.");
	}
}