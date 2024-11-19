package com.teamsoft.teamadmin.component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * checkbox或radio按钮组
 * @author zhangcc
 * @version 2017/11/14
 */
public class FormButtonGroup extends Component {
	// 存放按钮列表
	private final List<JToggleButton> buttons = new ArrayList<>();
	// 时间处理
	Consumer<JToggleButton> actionPerformed;

	/**
	 * 添加元素
	 * @param t 待添加元素
	 */
	public void add(JToggleButton t) {
		buttons.add(t);

		t.addActionListener(e -> {
			if (actionPerformed != null) {
				actionPerformed.accept(t);
			}
			buttons.forEach(b -> {
				if (b != t) {
					b.setSelected(false);
				}
			});
			t.setSelected(true);
		});
	}

	/**
	 * 添加事件监听
	 */
	public void addActionListener(Consumer<JToggleButton> c) {
		actionPerformed = c;
	}

	/**
	 * 获取所有按钮列表
	 */
	public List<JToggleButton> getButtons() {
		return buttons;
	}

	/**
	 * 设置选中值
	 * @param val 值
	 */
	public void setSelected(String val) {
		Optional<JToggleButton> selected = buttons.stream().filter(t -> val.equals(t.getText())).findAny();
		selected.ifPresent(AbstractButton::doClick);
	}

	/**
	 * 获取选中值
	 * @return 选中值
	 */
	public String getSelected() {
		Optional<JToggleButton> selected = buttons.stream().filter(AbstractButton::isSelected).findAny();
		return selected.map(AbstractButton::getText).orElse(null);
	}
}