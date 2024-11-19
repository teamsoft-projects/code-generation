package com.teamsoft.teamadmin.model;

/**
 * 数据表实体类
 * @author zhangcc
 * @version 2017/8/28
 */
public class Column {
	private String name;
	private String type;
	private String comment;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
