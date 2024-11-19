package com.teamsoft.teamadmin.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 常量类
 * @author zhangcc
 * @version 2017/11/10
 */
public class Constants {
	/**
	 * 系统常量
	 */
	public interface System {
		// 全局配置
		Map<String, String> GLOBAL_PARAMS = new LinkedHashMap<>();
		// 模板内容
		Map<String, String> TEMPLATES = new HashMap<>();
		// 模板参数正则
		Pattern TEMPLATE_DATA_PATTEN = Pattern.compile("##\\((.+?)\\)");
		/**
		 * 数字常量
		 */
		Integer NUMBER_ZERO = 0;
		Integer NUMBER_ONE = 1;
		Integer NUMBER_TWO = 2;
		Integer NUMBER_THREE = 3;
		SimpleDateFormat FMT_YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Swing常量类
	 */
	public interface Swing {
		// 标准组件高度
		Integer COMPONENT_HEIGHT = 25;
		// 标准按钮宽度
		Integer BUTTON_WIDTH = 100;
		// 操作区域按钮所占高度
		Integer OPERATION_AREA_HEIGHT = 80;
		// CheckBox/Radio标准宽度
		Integer LITTLE_FIELD_WIDTH = 120;
	}
}