package com.teamsoft.teamadmin.window;

import com.teamsoft.teamadmin.component.*;
import com.teamsoft.teamadmin.util.Constants;
import com.teamsoft.teamadmin.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

class ConfigCenter extends JDialog {
	private static final Integer WINDOW_WIDTH = 600;
	private static final Integer WINDOW_HEIGHT = 380;
	// Tab页容器
	private final JTabbedPane tabbedPane;
	// 数据库配置Form容器
	private FormPanel pnlDatabaseConfig;
	// 项目配置Form容器
	private FormPanel pnlProjectConfig;

	ConfigCenter() {
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());
		getContentPane().add(pnlMain);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		pnlMain.add(tabbedPane);

		// 设置第一个Tab页的内容
		_setFirstTab();
		// 设置第二个Tab页的内容
		_setSecondTab();
		// 添加事件绑定
		_addActionListener();

		setTitle("配置中心");
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		setVisible(true);
	}

	/**
	 * 设置第一个Tab页的内容
	 */
	private void _setFirstTab() {
		// 第一个Tab
		pnlDatabaseConfig = new FormPanel();
		tabbedPane.addTab("数据库配置", pnlDatabaseConfig);

		FormLabel lblDBUrl = new FormLabel("dburl", "数据库地址");
		pnlDatabaseConfig.add(lblDBUrl);
		FormTextField txtfDBUrl = new FormTextField();
		pnlDatabaseConfig.add(txtfDBUrl, lblDBUrl);

		FormLabel lblUsername = new FormLabel("dbuser", "用户名");
		pnlDatabaseConfig.add(lblUsername);
		FormTextField txtfUsername = new FormTextField();
		pnlDatabaseConfig.add(txtfUsername, lblUsername);

		FormLabel lblPassword = new FormLabel("dbpass", "密码");
		pnlDatabaseConfig.add(lblPassword);
		FormTextField txtfPassword = new FormTextField();
		pnlDatabaseConfig.add(txtfPassword, lblPassword);

		FormLabel lblPageType = new FormLabel("pageType", "页面类型");
		pnlDatabaseConfig.add(lblPageType);
		FormButtonGroup bg = new FormButtonGroup();
		JRadioButton rdoPageFreeMarker = new JRadioButton("FreeMarker", false);
		JRadioButton rdoPageJsp = new JRadioButton("JSP", false);
		bg.add(rdoPageFreeMarker);
		bg.add(rdoPageJsp);
		pnlDatabaseConfig.add(bg, lblPageType);

		FormButton btnSave = new FormButton("save", "保存");
		pnlDatabaseConfig.addButton(btnSave);

		pnlDatabaseConfig.initFormData(Constants.System.GLOBAL_PARAMS);
	}

	/**
	 * 设置批量导入Tab页的内容
	 */
	private void _setSecondTab() {
		// 第二个Tab
		pnlProjectConfig = new FormPanel();
		tabbedPane.addTab("项目配置", pnlProjectConfig);

		FormLabel lblOutputPath = new FormLabel("outputPath", "输出目录");
		pnlProjectConfig.add(lblOutputPath);
		FormTextField txtfOutputPath = new FormTextField();
		pnlProjectConfig.add(txtfOutputPath, lblOutputPath);

		FormLabel lblJavaRoot = new FormLabel("javaRoot", "Java根目录");
		pnlProjectConfig.add(lblJavaRoot);
		FormTextField txtfJavaRoot = new FormTextField();
		pnlProjectConfig.add(txtfJavaRoot, lblJavaRoot);

		FormLabel lblPackageRoot = new FormLabel("packageRoot", "包根目录");
		pnlProjectConfig.add(lblPackageRoot);
		FormTextField txtfPackageRoot = new FormTextField();
		pnlProjectConfig.add(txtfPackageRoot, lblPackageRoot);

		FormLabel lblPageRoot = new FormLabel("pageRoot", "页面根目录");
		pnlProjectConfig.add(lblPageRoot);
		FormTextField txtfPageRoot = new FormTextField();
		pnlProjectConfig.add(txtfPageRoot, lblPageRoot);

		FormButton btnSave = new FormButton("save", "保存");
		pnlProjectConfig.addButton(btnSave);
		pnlProjectConfig.initFormData(Constants.System.GLOBAL_PARAMS);
		if("FreeMarker".equals(Constants.System.GLOBAL_PARAMS.get("pageType"))) {
			((FormTextField)pnlProjectConfig.get("pageRoot")).setText(Constants.System.GLOBAL_PARAMS.get("pageRootFreeMarker"));
		}
	}

	/**
	 * 添加事件绑定
	 */
	private void _addActionListener() {
		// 界面启动后, 重排FormPanel容器中的元素
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				SwingUtils.rescureRelocation(ConfigCenter.this);
			}
		});
		// 点击页面类型事件
		((FormButtonGroup) pnlDatabaseConfig.get("pageType")).addActionListener(t -> {
			String text = t.getText();
			if ("FreeMarker".equals(text)) {
				((FormTextField) pnlProjectConfig.get("pageRoot")).setText(Constants.System.GLOBAL_PARAMS.get("pageRootFreeMarker"));
			} else {
				((FormTextField) pnlProjectConfig.get("pageRoot")).setText(Constants.System.GLOBAL_PARAMS.get("pageRoot"));
			}
		});
		// 添加数据库配置保存事件
		pnlDatabaseConfig.getButton("save").addActionListener(e -> {
			String pageType = ((FormButtonGroup) pnlDatabaseConfig.get("pageType")).getSelected();
			Map<String, String> formData = pnlProjectConfig.getFormData();
			String pageRoot = formData.get("pageRoot");
			if("FreeMarker".equals(pageType)) {
				formData.put("pageRootFreeMarker", pageRoot);
				formData.remove("pageRoot");
			}
			Constants.System.GLOBAL_PARAMS.putAll(pnlDatabaseConfig.getFormData());
			Constants.System.GLOBAL_PARAMS.putAll(formData);
			SwingUtils.storeConfigProperties();
			ConfigCenter.this.dispose();
		});
		// 添加项目配置保存事件
		pnlProjectConfig.getButton("save").addActionListener(e -> {
			String pageType = ((FormButtonGroup) pnlDatabaseConfig.get("pageType")).getSelected();
			Map<String, String> formData = pnlProjectConfig.getFormData();
			String pageRoot = formData.get("pageRoot");
			if("FreeMarker".equals(pageType)) {
				formData.put("pageRootFreeMarker", pageRoot);
				formData.remove("pageRoot");
			}
			Constants.System.GLOBAL_PARAMS.putAll(pnlDatabaseConfig.getFormData());
			Constants.System.GLOBAL_PARAMS.putAll(formData);
			SwingUtils.storeConfigProperties();
			ConfigCenter.this.dispose();
		});
	}
}