package com.teamsoft.teamadmin.window;

import com.teamsoft.teamadmin.component.*;
import com.teamsoft.teamadmin.model.ModelInfo;
import com.teamsoft.teamadmin.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主界面
 */
public class Main extends JFrame {
	private static final Integer WINDOW_WIDTH = 600;
	private static final Integer WINDOW_HEIGHT = 420;
	// 页面顶层容器
	private JPanel pnlMain;
	// 菜单-配置中心
	private JMenuItem menuConfigCenter;

	// Tab页容器
	private JTabbedPane tabbedPane;
	// 单个生成FormPanel
	private FormPanel pnlSingleGenerate;
	// 批量生成FormPanel
	private FormPanel pnlMultiGenerate;

	private Main() {
		_setMain();
		_addActionListener();
	}

	private void _setMain() {
		pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());
		getContentPane().add(pnlMain);

		// 设置菜单栏
		_setMenu();

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		pnlMain.add(tabbedPane);

		// 设置单模块生成Tab页的内容
		_setSingleGenerateTab();
		// 设置批量导入Tab页的内容
		_setMultiGenerateTab();
		// 初始化数据内容
		_initData();

		setTitle("后台管理系统代码生成工具");
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	/**
	 * 设置第一个Tab页的内容
	 */
	private void _setSingleGenerateTab() {
		// 第一个Tab
		pnlSingleGenerate = new FormPanel();
		pnlSingleGenerate.setLayout(null);
		tabbedPane.addTab("单个生成", pnlSingleGenerate);

		FormLabel lblTableName = new FormLabel("tableName", "表名(不区分大小写)");
		pnlSingleGenerate.add(lblTableName);
		FormTextField txtfTableName = new FormTextField();
		txtfTableName.setText("t_member_check_record");
		pnlSingleGenerate.add(txtfTableName, lblTableName);

		FormLabel lblPackageName = new FormLabel("packageName", "包名(相对路径)");
		pnlSingleGenerate.add(lblPackageName);
		FormTextField txtfPackagePath = new FormTextField();
		txtfPackagePath.setText("member");
		pnlSingleGenerate.add(txtfPackagePath, lblPackageName);

		FormLabel lblPathPrefix = new FormLabel("pathPrefix", "路径前缀(斜杠隔开)");
		pnlSingleGenerate.add(lblPathPrefix);
		FormTextField txtfPathPrefix = new FormTextField();
		txtfPathPrefix.setText("member");
		pnlSingleGenerate.add(txtfPathPrefix, lblPathPrefix);

		FormLabel lblModuleName = new FormLabel("modelName", "模块名(会员 etc.)");
		pnlSingleGenerate.add(lblModuleName);
		FormTextField txtfModuleName = new FormTextField();
		txtfModuleName.setText("客户检查记录");
		pnlSingleGenerate.add(txtfModuleName, lblModuleName);

		FormLabel lblParentName = new FormLabel("parentName", "上级菜单(会员管理 etc.)");
		pnlSingleGenerate.add(lblParentName);
		FormTextFieldWithCheckbox txtfParentName = new FormTextFieldWithCheckbox();
		txtfParentName.setText("客户管理");
		pnlSingleGenerate.add(txtfParentName, lblParentName);

		FormButton btnBuild = new FormButton("generate", "开始生成");
		pnlSingleGenerate.addButton(btnBuild);

		FormButton btnExit = new FormButton("exit", "退出");
		pnlSingleGenerate.addButton(btnExit);
	}

	/**
	 * 设置批量导入Tab页的内容
	 */
	private void _setMultiGenerateTab() {
		// 批量生成Tab
		pnlMultiGenerate = new FormPanel();
		pnlMultiGenerate.setLayout(null);
		tabbedPane.addTab("批量生成", pnlMultiGenerate);

		FormLabel lblChooseFile = new FormLabel("chooseFile", "选择文件");
		pnlMultiGenerate.add(lblChooseFile);
		FormTextField txtfChooseFile = new FormTextField();
		pnlMultiGenerate.add(txtfChooseFile, lblChooseFile);

		FormLabel lblIsGenerateDB = new FormLabel("isGenerateDB", "数据库数据");
		pnlMultiGenerate.add(lblIsGenerateDB);
		JCheckBox cbxIsGenerateDB = new JCheckBox();
		pnlMultiGenerate.add(cbxIsGenerateDB, lblIsGenerateDB);

		FormButton btnBuild = new FormButton("generate", "开始生成");
		pnlMultiGenerate.addButton(btnBuild);

		FormButton btnExit = new FormButton("exit", "退出");
		pnlMultiGenerate.addButton(btnExit);
	}

	/**
	 * 设置菜单栏
	 */
	private void _setMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menuConfig = new JMenu("配置");
		menuBar.add(menuConfig);
		menuConfigCenter = new JMenuItem("配置中心");
		menuConfig.add(menuConfigCenter);
		JMenuItem menuExit = new JMenuItem("退出");
		menuExit.addActionListener(e -> System.exit(0));
		menuConfig.add(menuExit);

		JMenu menuHelp = new JMenu("帮助(H)");
		menuBar.add(menuHelp);
		JMenuItem menuItemDesc = new JMenuItem("使用说明");
		menuItemDesc.addActionListener(e -> SwingUtils.alert(Main.this, "您点击了使用说明!!"));
		menuHelp.add(menuItemDesc);

		pnlMain.add(menuBar, BorderLayout.NORTH);
	}

	/**
	 * 初始化数据内容
	 */
	private void _initData() {
		// 设置默认保存路径为桌面/out目录
		String desktopPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		desktopPath = desktopPath.replace("\\", "/");
		Constants.System.GLOBAL_PARAMS.put("outputPath", desktopPath + "/output");
	}

	/**
	 * 添加事件绑定
	 */
	private void _addActionListener() {
		// 界面启动后, 重排FormPanel容器中的元素
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				SwingUtils.rescureRelocation(Main.this);
			}
		});

		// 点击配置中心, 弹出配置中心页面
		menuConfigCenter.addActionListener(e -> new ConfigCenter());

		// 单个模块生成事件
		((FormButton) pnlSingleGenerate.get("generate")).addActionListener(e -> {
			// 初始化数据库配置
			DBUtils.initDatabase();
			// 判断是否需要生成菜单
			FormTextFieldWithCheckbox ftxtf = (FormTextFieldWithCheckbox) pnlSingleGenerate.get("parentName");
			String parentName = ftxtf.getText();
			Map<String, String> data = pnlSingleGenerate.getFormData();
			// 如果已勾选生成菜单
			if (ftxtf.isChecked() && !CommonUtils.isEmpty(parentName)) {
				// 生成菜单
				List<ModelInfo> list = new ArrayList<>();
				ModelInfo modelInfo = new ModelInfo();
				modelInfo.setModelName(data.get("modelName"));
				modelInfo.setParentName(data.get("parentName"));
				modelInfo.setTableName(data.get("tableName"));
				modelInfo.setPackageName(data.get("packageName"));
				modelInfo.setPathPrefix(data.get("pathPrefix"));
				list.add(modelInfo);
				CommonUtils.generateDatabase(list);
			}
			// 生成代码
			GenerateUtils.generate(data);
			SwingUtils.alert(Main.this, "生成完成");
		});
		// 单个模块退出事件
		((FormButton) pnlSingleGenerate.get("exit")).addActionListener(e -> {
			System.exit(0);
		});

		// 选择批量生成源文件
		FormTextField txtfChooseFile = (FormTextField) pnlMultiGenerate.get("chooseFile");
		txtfChooseFile.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Excel文件", "xls", "xlsx");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(Main.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					txtfChooseFile.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		// 批量生成按钮事件
		((FormButton) pnlMultiGenerate.get("generate")).addActionListener(e -> {
			String filePath = txtfChooseFile.getText();
			JCheckBox cbxIsGenerateDB = (JCheckBox) pnlMultiGenerate.get("isGenerateDB");
			boolean isGenerateDB = cbxIsGenerateDB.isSelected();
			POIHandler handler = POIHandler.build(filePath);
			handler.setStartRow(1);
			// 初始化数据库配置
			DBUtils.initDatabase();
			try {
				List<ModelInfo> list = handler.read(ModelInfo.class);
				if (isGenerateDB) {
					// 生成菜单和功能信息
					CommonUtils.generateDatabase(list);
				}
				for (ModelInfo modelInfo : list) {
					// 生成代码
					GenerateUtils.generate(CommonUtils.objectToMap(modelInfo));
				}
				SwingUtils.alert(Main.this, "生成完成");
			} catch (Exception e1) {
				SwingUtils.alert(Main.this, "生成代码失败！" + e1.getMessage());
			}
		});
		// 批量生成模块退出事件
		((FormButton) pnlMultiGenerate.get("exit")).addActionListener(e -> {
			System.exit(0);
		});
	}

	/**
	 * 主入口
	 */
	public static void main(String[] args) {
		try {
			SwingUtils.initFontSet();
			SwingUtils.initConfigProperties();
			SwingUtils.initTemplate();
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			SwingUtilities.invokeLater(Main::new);
		} catch (Exception e) {
			SwingUtils.alert(null, "启动失败！" + e.getMessage());
		}
	}
}