package com.teamsoft.teamadmin.util;

import com.teamsoft.teamadmin.component.FormPanel;
import com.teamsoft.teamadmin.exception.VerifyException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Swing工具类
 */
public class SwingUtils {
	/**
	 * 弹框提示
	 * @param parent  父容器
	 * @param message 提示消息
	 */
	public static void alert(Window parent, String message) {
		JOptionPane.showMessageDialog(parent, message);
	}

	/**
	 * 初始化字体设置
	 */
	public static void initFontSet() {
		Font font = new Font("宋体", Font.PLAIN, 13);
		FontUIResource fontRes = new FontUIResource(font);
		for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, fontRes);
			}
		}
	}

	/**
	 * 递归设置容器的所有自容器中类型为FormPanel的容器的label和textfield排位
	 * @param root 顶层容器
	 */
	public static void rescureRelocation(Container root) {
		if (root == null) {
			return;
		}
		if (root instanceof FormPanel) {
			((FormPanel) root).reBoundField((FormPanel) root);
			return;
		}
		for (Component c : root.getComponents()) {
			if (c instanceof Container) {
				rescureRelocation((Container) c);
			}
		}
	}

	/**
	 * 读取配置xml
	 * 初始化默认配置
	 */
	public static void initConfigProperties() {
		String configPath = System.getProperty("user.dir") + File.separator + "config.xml";
		SAXBuilder builder = new SAXBuilder();
		try {
			Document document = builder.build(new FileInputStream(configPath));
			Element root = document.getRootElement();
			List<Element> children = root.getChildren();
			for (Element elem : children) {
				String key = elem.getAttributeValue("name");
				String value = elem.getAttributeValue("value");
				Constants.System.GLOBAL_PARAMS.put(key, value);
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将常量中的配置信息写入配置文件中
	 */
	public static void storeConfigProperties() {
		String configPath = System.getProperty("user.dir") + File.separator + "config.xml";

		Document document = new Document();
		Element data = new Element("data");
		document.addContent(data);
		for (Map.Entry<String, String> entry : Constants.System.GLOBAL_PARAMS.entrySet()) {
			Element ele = new Element("entry");
			ele.setAttribute("name", entry.getKey());
			ele.setAttribute("value", entry.getValue());
			data.addContent(ele);
		}

		Format format = Format.getPrettyFormat();
		format.setEncoding(StandardCharsets.UTF_8.name());
		format.setIndent("    ");
		XMLOutputter out = new XMLOutputter(format);
		try (FileWriter writer = new FileWriter(configPath)) {
			out.output(document, writer);
		} catch (IOException e) {
			e.printStackTrace();
			throw new VerifyException("写入配置文件失败");
		}
	}

	/**
	 * 初始化模板内容
	 */
	public static void initTemplate() {
		File templateDir = null;
		try {
			templateDir = new File(SwingUtils.class.getResource("/template").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		assert templateDir != null;
		File[] fs = templateDir.listFiles();
		if (fs == null || fs.length == 0) {
			return;
		}
		for (File f : fs) {
			String name = f.getName();
			name = name.substring(0, name.lastIndexOf("."));
			Constants.System.TEMPLATES.put(name, CommonUtils.fileRead(f));
		}
	}

	/**
	 * 简单模板实现
	 * @param template 模板内容
	 * @param data     填充数据
	 * @return 模板填充后的结果
	 */
	public static String template(String template, Map<String, String> data) {
		if (!CommonUtils.hasLength(template) || data == null || data.isEmpty()) {
			return template;
		}
		Matcher m = Constants.System.TEMPLATE_DATA_PATTEN.matcher(template);
		while (m.find()) {
			String key = m.group(1);
			String sourceKey = "##(" + key + ")";
			String dataStr = data.get(key);
			dataStr = dataStr == null ? "" : dataStr;
			template = template.replace(sourceKey, dataStr);
		}
		return template;
	}
}