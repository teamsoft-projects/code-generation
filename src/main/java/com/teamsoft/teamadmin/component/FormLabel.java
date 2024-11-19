package com.teamsoft.teamadmin.component;

import javax.swing.*;

/**
 * 标签组件封装
 * @author zhangcc
 */
public class FormLabel extends JLabel implements Comparable<FormLabel> {
	// 表单中组件名称
	private final String name;
	// 关联组件跨行数量
	private Integer rowspanCount = 1;

	/**
	 * 构造方法重写
	 */
	public FormLabel(String name, String text) {
		super(text);
		this.name = name;
		setHorizontalAlignment(RIGHT);
		setSize(50, 25);
	}

	/**
	 * 比较
	 */
	public int compareTo(FormLabel o) {
		if (o == null) {
			return -1;
		}
		return this.name == null ? (o.name == null ? 0 : -1) : this.name.compareTo(o.name);
	}

	public String getName() {
		return name;
	}

	public Integer getRowspanCount() {
		return rowspanCount;
	}

	public void setRowspanCount(Integer rowspanCount) {
		this.rowspanCount = rowspanCount;
	}
}