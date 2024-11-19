package com.teamsoft.teamadmin.util;

import com.teamsoft.teamadmin.model.ExcelColumn;
import com.teamsoft.teamadmin.model.ExcelField;
import com.teamsoft.teamadmin.model.ModelInfo;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * EXCEL读写实体类
 * @author alex
 * @version 2017/9/16
 */
public class POIHandler {
	// 源文件
	private File sourceFile;
	// 源文件流
	private InputStream sourceStream;
	// 输入类型(1.文件 2.流)
	private Integer sourceType;
	// 默认读取sheet
	private Integer defaultSheet = 0;
	// 开启读取行
	private Integer startRow = 0;
	// Workbook对象
	private Workbook wb;

	/**
	 * 构造函数
	 * @param sourceFile 源文件
	 */
	private POIHandler(File sourceFile) {
		this.sourceFile = sourceFile;
		this.sourceType = Constants.System.NUMBER_ONE;
	}

	/**
	 * 构造函数
	 * @param sourcePath 源文件路径
	 */
	private POIHandler(String sourcePath) {
		this.sourceFile = new File(sourcePath);
		this.sourceType = Constants.System.NUMBER_ONE;
	}

	/**
	 * 根据文件路径和模型类构建Excel处理对象
	 * @param sourcePath 源文件路径
	 */
	public static <T> POIHandler build(String sourcePath) {
		return new POIHandler(new File(sourcePath));
	}

	/**
	 * 根据文件和模型类构建Excel处理对象
	 * @param sourceFile 源文件
	 */
	public static POIHandler build(File sourceFile) {
		return new POIHandler(sourceFile);
	}

	/**
	 * 构造函数
	 * @param sourceStream 源文件流
	 */
	private POIHandler(InputStream sourceStream) {
		this.sourceStream = sourceStream;
		this.sourceType = Constants.System.NUMBER_TWO;
	}

	/**
	 * 根据输入流和模型类构建Excel处理对象
	 * @param sourceStream 源文件流
	 */
	public static POIHandler build(InputStream sourceStream) {
		return new POIHandler(sourceStream);
	}

	/**
	 * 获取单元格数据
	 * @param cell 数据单元格
	 * @return 结果值
	 */
	public static Object getCellValue(Cell cell) {
		Object retVal;
		if (cell == null) {
			return null;
		}
		// 判断数据的类型
		switch (cell.getCellTypeEnum()) {
			case NUMERIC: // 数字
				retVal = cell.getNumericCellValue();
				break;
			case STRING: // 字符串
				retVal = cell.getStringCellValue();
				break;
			case BOOLEAN: // Boolean
				retVal = cell.getBooleanCellValue();
				break;
			case FORMULA: // 公式
				retVal = cell.getCellFormula();
				break;
			case BLANK: // 空值
				retVal = "";
				break;
			case ERROR: // 故障
				retVal = "非法字符";
				break;
			default:
				retVal = "未知类型";
				break;
		}
		return retVal;
	}

	/**
	 * 根据build的参数, 初始化workbook对象
	 */
	private void initWorkbook() throws Exception {
		switch (sourceType) {
			case 1: {
				wb = WorkbookFactory.create(new FileInputStream(sourceFile));
				break;
			}
			case 2: {
				wb = WorkbookFactory.create(sourceStream);
				break;
			}
			default: {
				throw new Exception("Source Type NOT Supported");
			}
		}
	}

	/**
	 * 读取Excel文档
	 * @param modelClass 模型类
	 */
	public <T> List<T> read(Class<T> modelClass) throws Exception {
		initWorkbook();
		Sheet sheet = wb.getSheetAt(defaultSheet);
		int lastRowNum = sheet.getLastRowNum();
		if (startRow > lastRowNum) {
			return null;
		}
		List<ExcelField> fields = generateExcelField(modelClass);
		if (fields.size() == 0) {
			return null;
		}
		List<T> retList = new ArrayList<>();
		for (int i = startRow;i <= lastRowNum;i++) {
			Row row = sheet.getRow(i);
			short lastCellNum = row.getLastCellNum();
			T obj = modelClass.newInstance();
			retList.add(obj);
			for (ExcelField field : fields) {
				Field f = field.getField();
				int index = field.getIndex();
				if (index < 0 || index > lastCellNum) {
					continue;
				}
				f.setAccessible(true);
				Type type = field.getType();
				Cell cell = row.getCell(index);
				Object cellVal = getCellValue(cell);
				switch (type.getTypeName()) {
					case "java.lang.Integer": {
						if (cellVal == null) {
							f.set(obj, null);
							break;
						} else if (cellVal instanceof Integer) {
							f.set(obj, cellVal);
						} else if (cellVal instanceof Double) {
							Double cellValDbl = (Double) cellVal;
							f.set(obj, cellValDbl.intValue());
						} else if (cellVal instanceof String) {
							if ("".equals(cellVal)) {
								f.set(obj, null);
								break;
							}
							try {
								Double cellValDbl = Double.valueOf(cellVal.toString());
								f.set(obj, cellValDbl.intValue());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					}
					case "java.lang.String": {
						if (cellVal == null) {
							f.set(obj, null);
							break;
						}
						String cellValStr = cellVal.toString();
						if (!CommonUtils.hasLength(cellValStr)) {
							f.set(obj, null);
						} else {
							f.set(obj, cellValStr);
						}
						break;
					}
				}
			}
		}
		wb.close();
		return retList;
	}

	/**
	 * 读取Excel文档并以List的形式返回
	 */
	public List<List<String>> readToList() {
		/*
		 * Sheet sheet = wb.getSheetAt(defaultSheet); int lastRowNum =
		 * sheet.getLastRowNum(); if (startRow > lastRowNum) { return null; } for (int i
		 * = startRow;i < lastRowNum;i++) { Row row = sheet.getRow(i); short lastCellNum
		 * = row.getLastCellNum(); }
		 */
		return null;
	}

	/**
	 * 根据模型类定义, 获取field列表
	 */
	private <T> List<ExcelField> generateExcelField(Class<T> modelClass) {
		List<ExcelField> result = new ArrayList<>();
		Field[] fs = modelClass.getDeclaredFields();
		for (Field f : fs) {
			ExcelField field = new ExcelField();
			ExcelColumn col = f.getAnnotation(ExcelColumn.class);
			if (col == null) {
				continue;
			}
			field.setField(f);
			field.setIndex(col.index());
			field.setType(f.getGenericType());
			result.add(field);
		}
		return result;
	}

	public Integer getDefaultSheet() {
		return defaultSheet;
	}

	public void setDefaultSheet(Integer defaultSheet) {
		this.defaultSheet = defaultSheet;
	}

	public Integer getStartRow() {
		return startRow;
	}

	public void setStartRow(Integer startRow) {
		this.startRow = startRow;
	}
}