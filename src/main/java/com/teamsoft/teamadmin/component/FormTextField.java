package com.teamsoft.teamadmin.component;

import javax.swing.*;

/**
 * 带默认宽高的textfield
 * @author zhangcc
 * @version 2017/11/10
 */
public class FormTextField extends JTextField {
	// 表单组件名称
	private String name;

	public FormTextField() {
		super();
		setSize(50, 25);
	}

	/**
	 * 带名称的输入框
	 * @param name 名称
	 */
	public FormTextField(String name) {
		this();
		this.name = name;
	}

	public String getName() {
		return name;
	}
}