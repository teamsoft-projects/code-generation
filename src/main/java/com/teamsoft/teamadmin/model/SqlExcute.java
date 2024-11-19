package com.teamsoft.teamadmin.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 待执行SQL实体类
 * @author zhangcc
 * @version 2018/5/17
 */
@Data
public class SqlExcute {
	private String sql;
	private List<Object> params = new ArrayList<>();

	public SqlExcute setParams(Object param) {
		params.add(param);
		return this;
	}
}