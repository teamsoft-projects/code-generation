package com.teamsoft.teamadmin.model;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Excel读写注解数据实体类
 * @author alex
 * @version 2017/10/16
 */
public class ExcelField {
	private Field field;
	private Integer index;
	private Type type;

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}