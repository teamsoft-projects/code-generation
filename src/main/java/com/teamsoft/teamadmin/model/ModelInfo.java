package com.teamsoft.teamadmin.model;

import lombok.Data;

/**
 * 模块信息实体类
 * @author zhangcc
 * @version 2017/11/16
 */
@Data
public class ModelInfo {
	@ExcelColumn(index = 0)
	private String modelName;
	@ExcelColumn(index = 1)
	private String packageName;
	@ExcelColumn(index = 2)
	private String tableName;
	@ExcelColumn(index = 3)
	private String parentName;
	private String packagePrefix;
	private String pathPrefix;
}