package com.teamsoft.teamadmin.component;

import javax.swing.*;

/**
 * 带默认宽高的按钮
 * @author zhangcc
 * @version 2017/11/10
 */
public class FormButton extends JButton {
	// 按钮Form内唯一标识
	private final String id;

	public FormButton(String id, String text) {
		super(text);
		this.id = id;
		setSize(120, 25);
	}

	String getId() {
		return id;
	}
}