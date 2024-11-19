package com.teamsoft.teamadmin.component;

import com.teamsoft.teamadmin.exception.VerifyException;
import com.teamsoft.teamadmin.util.CommonUtils;
import com.teamsoft.teamadmin.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * 表单Panel
 * @author Cheng
 */
public class FormPanel extends JPanel {
	// 存放标签-输入元素集合
	private final Map<FormLabel, Component> fieldItems = new LinkedHashMap<>();
	// 存放操作域按钮列表
	private final List<JButton> btns = new ArrayList<>();
	// 存放所有元素列表
	private final Map<String, Component> elements = new HashMap<>();

	/**
	 * 构造方法入口
	 * 设置默认布局为绝对定位
	 * TODO 增加XML数据库功能， 保存配置文件信息
	 * TODO 增加针对checkbox和radio的设值、取值处理
	 */
	public FormPanel() {
		super(null);
	}

	/**
	 * 根据数据Map设置初始化数据
	 * @param data 待填入数据
	 */
	public void initFormData(Map<String, String> data) {
		for (Map.Entry<FormLabel, Component> entry : fieldItems.entrySet()) {
			JLabel lbl = entry.getKey();
			String fieldData = data.get(lbl.getName());
			if (CommonUtils.hasLength(fieldData)) {
				Component c = entry.getValue();
				if (c instanceof FormTextField) {
					((FormTextField) c).setText(fieldData);
				} else if (c instanceof FormButtonGroup) {
					((FormButtonGroup) c).setSelected(fieldData);
				}
			}
		}
	}

	/**
	 * 获取表单Panel的值Map
	 */
	public Map<String, String> getFormData() {
		Map<String, String> retMap = new TreeMap<>();
		for (Map.Entry<FormLabel, Component> entry : fieldItems.entrySet()) {
			Component comp = entry.getValue();
			if (comp instanceof FormTextField) {
				retMap.put(entry.getKey().getName(), ((FormTextField) comp).getText());
			} else if (comp instanceof FormTextFieldWithCheckbox) {
				FormTextFieldWithCheckbox field = (FormTextFieldWithCheckbox) comp;
				String key = entry.getKey().getName();
				retMap.put(key, field.getText());
				retMap.put(key + "Selected", String.valueOf(field.isChecked()));
			} else if (comp instanceof FormButtonGroup) {
				retMap.put(entry.getKey().getName(), ((FormButtonGroup) comp).getSelected());
			}
		}
		return retMap;
	}

	/**
	 * 新增关联组件
	 * @param c        新增的组件
	 * @param forLabel 成组出现的label
	 */
	public void add(Component c, FormLabel forLabel) {
		fieldItems.put(forLabel, c);
		super.add(c);
		// 将组件放入元素列表内
		String key = forLabel.getName();
		if (elements.get(key) != null) {
			throw new VerifyException("FormPanel组件名重复");
		}
		elements.put(key, c);
	}

	/**
	 * 新增关联按钮组
	 * @param group    新增按钮组
	 * @param forLabel 成组出现的label
	 */
	public void add(FormButtonGroup group, FormLabel forLabel) {
		fieldItems.put(forLabel, group);
		for (JToggleButton button : group.getButtons()) {
			super.add(button);
		}
		// 将组件放入元素列表内
		String key = forLabel.getName();
		if (elements.get(key) != null) {
			throw new VerifyException("FormPanel组件名重复: " + key);
		}
		elements.put(key, group);
	}

	/**
	 * 添加按钮域按钮
	 * @param btn 待添加按钮
	 */
	public void addButton(FormButton btn) {
		btns.add(btn);
		super.add(btn);
		// 将组件放入元素列表内
		String key = btn.getId();
		if (elements.get(key) != null) {
			throw new VerifyException("FormPanel组件名重复: " + key);
		}
		elements.put(key, btn);
	}

	/**
	 * 根据Form内唯一的Id获取Form组件
	 * @param id Form内组件唯一标识
	 * @return 获取到的组件
	 */
	public Component get(String id) {
		return elements.get(id);
	}

	/**
	 * 根据Form内唯一的Id获取按钮组件
	 * @param id Form内组件唯一标识
	 * @return 获取到的按钮组件
	 */
	public JButton getButton(String id) {
		Component c = elements.get(id);
		if (c == null) {
			throw new VerifyException("未找到按钮组件 " + id);
		}
		if (!(c instanceof JButton)) {
			throw new VerifyException("组件 " + id + " 不是按钮组件");
		}
		return (JButton) c;
	}

	/**
	 * 将容器中的Label和TextField重新排位
	 * @param form 表单容器
	 */
	public void reBoundField(FormPanel form) {
		// 容器宽度
		int width = form.getWidth();
		// Label/TextField组合区域高度
		int compHeight = form.getHeight() - Constants.Swing.OPERATION_AREA_HEIGHT;
		// 计算Label/TextField组合的数量, , 计算宽高和位置
		int compCount = form.getFieldItems().size();
		// Field区域距离0.33分割线的预留间隙
		int compVGap = 10;
		// 计算宽度
		int splitWid = (int) (width * 0.33);
		// 计算Field的宽度(为了美观, TextField只占用1-0.33=0.67中的0.55的宽度)
		int fieldWidth = (int) (width * 0.55 - compVGap);
		// 需要跨行的多field数量
		int subCount = 0;
		// 计算每行最大可放置小组件数量
		int maxFieldCount = 0;
		for (int i = 2; i < 10000; i++) {
			int fieldVGap = (fieldWidth - i * Constants.Swing.LITTLE_FIELD_WIDTH) / (i - 1);
			if (fieldVGap < 10) {
				maxFieldCount = i - 1;
				break;
			}
		}
		// 循环组合集合, 计算是否有跨行的小组件
		for (Map.Entry<FormLabel, Component> entry : form.getFieldItems().entrySet()) {
			Component tempComp = entry.getValue();
			if (!(tempComp instanceof FormButtonGroup)) {
				continue;
			}
			FormButtonGroup group = (FormButtonGroup) tempComp;
			List<JToggleButton> buttons = group.getButtons();
			// 小组件数量
			int fieldCount = buttons.size();
			// 如果小组件数量超出可放置最大数量
			if (fieldCount > maxFieldCount) {
				// 增加小组件的跨行数量
				int rowspanCount = fieldCount % maxFieldCount == 0 ? fieldCount / maxFieldCount : fieldCount / maxFieldCount + 1;
				entry.getKey().setRowspanCount(rowspanCount);
				subCount += rowspanCount;
			}
		}
		subCount = subCount == 0 ? 0 : subCount - 1;
		// 垂直间隙计算时, 先加跨行的数量
		compCount += subCount;
		// 计算组合间垂直间隙
		int compGap = (compHeight - compCount * Constants.Swing.COMPONENT_HEIGHT) / (compCount + 1);
		// 循环设置Label/TextField的宽高和位置
		int i = 0;
		for (Map.Entry<FormLabel, Component> entry : form.getFieldItems().entrySet()) {
			int y = (i + 1) * compGap + i * Constants.Swing.COMPONENT_HEIGHT;
			FormLabel lbl = entry.getKey();
			// 设置Label的宽高和位置
			lbl.setBounds(0, y, splitWid - compVGap, lbl.getHeight());
			Component tempComp = entry.getValue();
			if (!(tempComp instanceof FormButtonGroup)) {
				if (tempComp instanceof JCheckBox) {
					tempComp.setBounds(splitWid + compVGap, y, 45, 25);
				} else {
					tempComp.setBounds(splitWid + compVGap, y, fieldWidth, tempComp.getHeight());
				}
				i++;
				continue;
			}
			// 单独处理按钮组的情况
			FormButtonGroup group = (FormButtonGroup) tempComp;
			List<JToggleButton> buttons = group.getButtons();
			// 小组件数量
			int fieldCount = buttons.size();
			// 获取跨行数量
			Integer rowspanCount = lbl.getRowspanCount();
			// 计算Field可展示域的X轴坐标
			int fieldAvailableX = splitWid + compVGap;
			for (int j = 1; j <= rowspanCount; j++) {
				y = (i + 1) * compGap + i * Constants.Swing.COMPONENT_HEIGHT;
				// 单行Field数量
				int localCount = j * maxFieldCount > fieldCount ? fieldCount - (j - 1) * maxFieldCount : maxFieldCount;
				if (localCount == 1) {
					Component c = buttons.get((j - 1) * maxFieldCount);
					c.setBounds(fieldAvailableX, y, Constants.Swing.LITTLE_FIELD_WIDTH, Constants.Swing.COMPONENT_HEIGHT);
					continue;
				}
				// 计算多Field水平间隙
				int fieldVGap = (fieldWidth - localCount * Constants.Swing.LITTLE_FIELD_WIDTH) / (localCount - 1);
				// 循环设置每一个field的位置和大小
				for (int k = 0; k < localCount; k++) {
					Component c = buttons.get((j - 1) * maxFieldCount + k);
					c.setBounds(fieldAvailableX + k * fieldVGap + k * Constants.Swing.LITTLE_FIELD_WIDTH, y, Constants.Swing.LITTLE_FIELD_WIDTH, Constants.Swing.COMPONENT_HEIGHT);
				}
				i++;
			}
		}
		// 计算按钮的数量, 计算宽高和位置
		// 操作区域组件的y轴坐标
		int operationAreaHeight = compHeight + (Constants.Swing.OPERATION_AREA_HEIGHT - Constants.Swing.COMPONENT_HEIGHT) / 2 - 10;
		// 操作区域按钮集合
		List<JButton> btns = form.getBtns();
		// 操作区域组件数量
		int operationAreaCount = btns.size();
		// 操作区域组件的间隙
		int operationAreaGap = (width - operationAreaCount * Constants.Swing.BUTTON_WIDTH) / (operationAreaCount + 1);
		// 循环设置按钮的宽高和位置
		for (i = 0; i < operationAreaCount; i++) {
			int buttonX = (i + 1) * operationAreaGap + i * Constants.Swing.BUTTON_WIDTH;
			btns.get(i).setBounds(buttonX, operationAreaHeight, Constants.Swing.BUTTON_WIDTH, Constants.Swing.COMPONENT_HEIGHT);
		}
	}

	private Map<FormLabel, Component> getFieldItems() {
		return fieldItems;
	}

	private List<JButton> getBtns() {
		return btns;
	}
}