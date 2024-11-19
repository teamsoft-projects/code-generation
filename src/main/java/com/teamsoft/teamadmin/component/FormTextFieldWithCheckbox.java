package com.teamsoft.teamadmin.component;

import javax.swing.*;

/**
 * 带复选框的文本输入框
 * @author zhangcc
 * @version 2018/5/17
 */
public class FormTextFieldWithCheckbox extends JPanel {
	// 表单组件名称
	private String name;
	// 输入框
	private final JTextField txtf = new JTextField();
	// 复选框
	private final JCheckBox checkBox = new JCheckBox();

	public FormTextFieldWithCheckbox() {
		super();
		setLayout(null);
		setSize(50, 25);
		this.add(txtf);
		this.add(checkBox);
	}

	public FormTextFieldWithCheckbox(String name) {
		this();
		this.name = name;
	}

	/**
	 * 重写设置位置方法, 设置位置时更新输入框和复选框的大小和位置
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		txtf.setBounds(0, 0, width - 25, height);
		checkBox.setBounds(width - 20, 0, 45, height);
	}

	/**
	 * 设置文本
	 */
	public void setText(String text) {
		txtf.setText(text);
	}

	/**
	 * 获取文本
	 */
	public String getText() {
		return txtf.getText();
	}

	/**
	 * 是否选中了复选框
	 */
	public boolean isChecked() {
		return checkBox.isSelected();
	}
}