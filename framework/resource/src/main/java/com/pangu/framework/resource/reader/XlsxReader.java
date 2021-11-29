package com.pangu.framework.resource.reader;

import com.pangu.framework.utils.reflect.ReflectionUtils;
import com.pangu.framework.resource.exception.DecodeException;
import com.pangu.framework.resource.other.Getter;
import com.pangu.framework.resource.other.GetterBuilder;
import com.pangu.framework.utils.json.JsonUtils;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class XlsxReader implements ResourceReader {

	private final static Logger logger = LoggerFactory.getLogger(XlsxReader.class);

	/** 配置分隔符 */
	private final static String SPLIT = ":";
	/** 结束标识 */
	private final static String ROW_END = "END";
	/** 上行结束标识 */
	private final static String ROW_END_BEFORE = "END_BEFORE";
	/** 忽略标识 */
	private final static String ROW_IGNORE = "NO";

	private final static TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);

	/** 客户端控制标识 */
	private final static String ROW_CLIENT = "CLIENT";
	/** 管理后台控制标识 */
	private final static String ROW_MANAGER = "MANAGER";
	/** 服务端控制标识同时也是数据开始标识 */
	private final static String ROW_SERVER = "SERVER";

	@Autowired
	@Setter
	private ConversionService conversionService;

	/** 数据标记行 */
	private String tagRow = "SERVER";
	/** 数据开始行号 */
	private int startRow = 0;
	/** 每个分页独立表头 */
	private boolean titlePerSheet;

	@Override
	public String getFormat() {
		return "xlsx";
	}

	@Override
	public void config(String config) {
		String[] array = config.split(SPLIT);
		if (array.length > 0) {
			this.tagRow = array[0];
		}
		if (array.length > 1) {
			this.startRow = Integer.parseInt(array[1]);
		}
		if (array.length > 2) {
			this.titlePerSheet = Boolean.valueOf(array[2]);
		}
	}

	public <E> List<E> read(InputStream input, Class<E> clz) {
		try {
			OPCPackage xlsxPackage = OPCPackage.open(input);
			XSSFReader xssfReader = new XSSFReader(xlsxPackage);
			StylesTable styles = xssfReader.getStylesTable();
			SharedStringsTable strings = xssfReader.getSharedStringsTable();
			XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
			// 创建返回数据集
			List<E> result = new LinkedList<E>();
			List<FieldInfo> fieldInfos = new LinkedList<>();
			while (iter.hasNext()) {
				InputStream stream = iter.next();
				String sheetName = iter.getSheetName();
				List<E> items = readSheet(styles, strings, stream, sheetName, clz, fieldInfos);
				result.addAll(items);
				stream.close();
			}
			return result;
		} catch (Exception e) {
			throw new DecodeException("读取EXCEL文件异常", e);
		}
	}

	// /////////////////////////////////////

	/**
	 * 读取一页数据
	 * @param styles
	 * @param strings
	 * @param sheetInputStream
	 * @param sheetName
	 * @param clz
	 * @param fieldInfos
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private <E> List<E> readSheet(StylesTable styles, SharedStringsTable strings, InputStream sheetInputStream,
			String sheetName, Class<E> clz, List<FieldInfo> fieldInfos) throws IOException,
			ParserConfigurationException, SAXException {
		InputSource sheetSource = new InputSource(sheetInputStream);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		XMLReader sheetParser = saxParser.getXMLReader();
		MyXSSFSheetHandler<E> handler = new MyXSSFSheetHandler<E>(styles, strings, sheetName, clz, fieldInfos);
		sheetParser.setContentHandler(handler);
		logger.debug("正在加载资源[{}] - [{}] ...", clz.getName(), sheetName);
		sheetParser.parse(sheetSource);
		return handler.results;
	}

	/**
	 * 实例化资源
	 * @param <E>
	 * @param clz
	 * @return
	 */
	private static <E> E newInstance(Class<E> clz) {
		try {
			return clz.newInstance();
		} catch (Exception e) {
			FormattingTuple message = MessageFormatter.format("资源[{}]无法实例化", clz);
			logger.error(message.getMessage());
			throw new RuntimeException(message.getMessage());
		}
	}

	// ----------------------------------------------------------------


	/**
	 * 属性信息
	 */
	private class FieldInfo {
		/** 第几列 */
		public final int index;
		/** 资源类属性 */
		public final Field field;
		/** 数值类型 */
		public Type valueType;

		/** MAP 映射 */
		public final boolean mapped;
		public String keyName;
		public Type keyType;

		/**
		 * 构造方法
		 * @throws Exception
		 */
		public FieldInfo(Class<?> clz, int index, String colname) throws Exception {
			Field field;
			// MAP
			String key = null;
			String fname;
			int splt = colname.indexOf(":");
			if (splt > 0) {
				fname = colname.substring(0, splt);
				field = clz.getDeclaredField(fname);
				key = colname.substring(splt + 1);
			} else {
				fname = colname;
				field = clz.getDeclaredField(fname);
			}

			if (StringUtils.isNotBlank(key)) {
				Type t = field.getGenericType();
				if (t instanceof ParameterizedType) {
					Type[] ts = ((ParameterizedType) t).getActualTypeArguments();
					this.keyType = ts[0];
					this.valueType = ts[1];
				}
				this.keyName = key;
				this.mapped = true;
			} else {
				this.valueType = field.getGenericType();
				this.mapped = false;
			}
			this.index = index;
			this.field = field;
			ReflectionUtils.makeAccessible(field);
		}

		/**
		 * 给实例注入属性
		 * @param instance
		 * @param content
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void inject(Object instance, String content) {
			try {
				if (mapped) {
					Map map = (Map) field.get(instance);
					if (map == null) {
						map = new HashMap<>();
						field.set(instance, map);
					}
					Object key;
					if (keyType == String.class) {
						key = keyName;
					} else {
						key = JsonUtils.string2Object(keyName, keyType);
					}
					Object value;
					if (conversionService != null) {
						TypeDescriptor kt = TypeDescriptor.nested(field, 1); 
						value = conversionService.convert(content, sourceType, kt);
					} else {
						value = JsonUtils.string2Object(content, valueType);
					}
					map.put(key, value);
				} else {
					Object value;
					if (conversionService != null) {
						TypeDescriptor targetType = new TypeDescriptor(field);
						value = conversionService.convert(content, sourceType, targetType);
					} else {
						value = JsonUtils.string2Object(content, valueType);
					}
					field.set(instance, value);
				}
			} catch (ConverterNotFoundException e) {
				FormattingTuple message = MessageFormatter.format("静态资源[{}]属性[{}]的转换器不存在",
						instance.getClass().getSimpleName(), field.getName());
				logger.error(message.getMessage(), e);
				throw new IllegalStateException(message.getMessage(), e);
			} catch (Exception e) {
				FormattingTuple message = MessageFormatter.format("属性[{}]注入失败", field);
				logger.error(message.getMessage());
				throw new IllegalStateException(message.getMessage(), e);
			}
		}
	}

	// ------------------------------------------------------------------------------------

	/**
	 * The type of the data value is indicated by an attribute on the cell. The value is usually in a "v" element within
	 * the cell.
	 */
	enum xssfDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
	}

	/**
	 * Derived from http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api
	 * <p/>
	 * Also see Standard ECMA-376, 1st edition, part 4, pages 1928ff, at
	 * http://www.ecma-international.org/publications/standards/Ecma-376.htm
	 * <p/>
	 * A web-friendly version is http://openiso.org/Ecma/376/Part4
	 */
	class MyXSSFSheetHandler<E> extends DefaultHandler {
		/** Table with styles */
		private final StylesTable stylesTable;
		/** Table with unique strings */
		private final SharedStringsTable sharedStringsTable;
		// Set when V start element is seen
		private boolean vIsOpen;
		// Set when cell start element is seen;
		// used when cell close element is seen.
		private xssfDataType nextDataType;
		// Used to format numeric cell values.
		private short formatIndex;
		private String formatString;
		private int thisColumn = -1;
		// The last column printed to the output stream
		private int lastColumnNumber = -1;
		// Gathers characters as they are seen.
		private final StringBuffer value;

		private List<String> cols;
		private final String sheetName;
		private final List<E> results;
		private final Class<E> clz;
		private final Getter idGetter;
		private int currentRow = 0;
		private final List<FieldInfo> fieldInfos;
		private boolean done;
		private boolean start;

		public MyXSSFSheetHandler(StylesTable styles, SharedStringsTable strings, String sheetName, Class<E> clz,
				List<FieldInfo> fieldInfos) {
			this.stylesTable = styles;
			this.sharedStringsTable = strings;
			this.value = new StringBuffer();
			this.nextDataType = xssfDataType.NUMBER;
			this.cols = new LinkedList<>();
			this.results = new LinkedList<>();
			this.sheetName = sheetName;
			this.clz = clz;
			this.fieldInfos = fieldInfos;
			this.idGetter = GetterBuilder.createIdGetter(clz);
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if ("inlineStr".equals(name) || "v".equals(name)) {
				vIsOpen = true;
				// Clear contents cache
				value.setLength(0);
			}
			// c => cell
			else if ("c".equals(name)) {
				// Get the cell reference
				String r = attributes.getValue("r");
				int firstDigit = -1;
				for (int c = 0; c < r.length(); ++c) {
					if (Character.isDigit(r.charAt(c))) {
						firstDigit = c;
						break;
					}
				}
				thisColumn = nameToColumn(r.substring(0, firstDigit));

				// Set up defaults.
				this.nextDataType = xssfDataType.NUMBER;
				this.formatIndex = -1;
				this.formatString = null;
				String cellType = attributes.getValue("t");
				String cellStyleStr = attributes.getValue("s");
				if ("b".equals(cellType))
					nextDataType = xssfDataType.BOOL;
				else if ("e".equals(cellType))
					nextDataType = xssfDataType.ERROR;
				else if ("inlineStr".equals(cellType))
					nextDataType = xssfDataType.INLINESTR;
				else if ("s".equals(cellType))
					nextDataType = xssfDataType.SSTINDEX;
				else if ("str".equals(cellType))
					nextDataType = xssfDataType.FORMULA;
				else if (cellStyleStr != null) {
					// It's a number, but almost certainly one
					// with a special style or format
					int styleIndex = Integer.parseInt(cellStyleStr);
					XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
					this.formatIndex = style.getDataFormat();
					this.formatString = style.getDataFormatString();
					if (this.formatString == null)
						this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
				}
			}

		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			String thisStr = null;
			// v => contents of a cell
			if ("v".equals(name)) {
				// Process the value contents as required.
				// Do now, as characters() may be called more than once
				switch (nextDataType) {
				case BOOL:
					char first = value.charAt(0);
					thisStr = first == '0' ? "FALSE" : "TRUE";
					break;
				case ERROR:
					thisStr = "\"ERROR:" + value.toString() + '"';
					break;
				case FORMULA:
					// A formula could result in a string value,
					// so always add double-quote characters.
					thisStr = value.toString();
					break;
				case INLINESTR:
					// have seen an example of this, so it's untested.
					XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
					thisStr = rtsi.toString();
					break;
				case SSTINDEX:
					String sstIndex = value.toString();
					try {
						int idx = Integer.parseInt(sstIndex);
						XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
						thisStr = rtss.toString();
					} catch (NumberFormatException ex) {
						throw new RuntimeException("Failed to parse SST index '" + sstIndex + "': " + ex);
					}
					break;

				case NUMBER:
					thisStr = value.toString();
					// String n = value.toString();
					// if (this.formatString != null)
					// thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex,
					// this.formatString);
					// else
					// thisStr = n;
					break;

				default:
					thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
					break;
				}

				// Output after we've seen the string contents
				// Emit commas for any fields that were missing on this row
				for (int i = lastColumnNumber + 1; i < thisColumn; ++i) {
					cols.add(null);
				}

				if (lastColumnNumber == -1) {
					lastColumnNumber = 0;
				}

				// Update column
				if (thisColumn > -1) {
					lastColumnNumber = thisColumn;
					// Might be the empty string.
					cols.add(thisStr);
				}

			} else if ("row".equals(name)) {
				// We're onto a new row
				try {
					parseRow();
				} finally {
					lastColumnNumber = -1;
					cols = new LinkedList<>();
					currentRow++;
				}
			}
		}

		private void parseRow() {
			if (currentRow < startRow) {
				// 未到开始行
				return;
			}
			if (cols.isEmpty()) {
				// 空行
				return;
			}
			if (done) {
				return;
			}

			String tag = cols.get(0);
			if (currentRow == 0 && StringUtils.isBlank(tag)) {
				// 跳过当前页
				done = true;
				return;
			}
			if (StringUtils.isNotBlank(tag)) {
				if (tag.equalsIgnoreCase(tagRow)) {
					// 表头属性信息集合
					List<FieldInfo> fieldInfos = new ArrayList<FieldInfo>();
					for (int i = 1; i < cols.size(); i++) {
						String fname = cols.get(i);
						if (StringUtils.isBlank(fname)) {
							continue;
						}
						try {
							FieldInfo info = new FieldInfo(clz, i, fname);
							fieldInfos.add(info);
						} catch (NoSuchFieldException e) {
							FormattingTuple message = MessageFormatter.arrayFormat("资源类[{}]分页[{}]的声明属性[{}]不存在",
									new Object[] { clz, sheetName, fname });
							logger.debug(message.getMessage());
						} catch (Exception e) {
							FormattingTuple message = MessageFormatter.arrayFormat("资源类[{}]分页[{}]的声明属性[{}]无法获取",
									new Object[] { clz, sheetName, fname });
							logger.error(message.getMessage());
							throw new IllegalStateException(message.getMessage(), e);
						}
					}

					if (!fieldInfos.isEmpty()) {
						start = true;
						if (titlePerSheet || this.fieldInfos.isEmpty()) {
							this.fieldInfos.clear();
							this.fieldInfos.addAll(fieldInfos);
						}
					}
					return;
				} else if (tag.equalsIgnoreCase(ROW_END_BEFORE)) {
					// 已结束
					done = true;
					return;
				} else if (tag.equalsIgnoreCase(ROW_IGNORE)) {
					// 忽略行
					return;
				} else if (tag.equalsIgnoreCase(ROW_CLIENT)) {
					// 忽略行
					return;
				} else if (tag.equalsIgnoreCase(ROW_SERVER)) {
					// 忽略行
					return;
				} else if (tag.equalsIgnoreCase(ROW_MANAGER)) {
					// 忽略行
					return;
				}
			}

			if (!start) {
				// 未初始化表头
				return;
			}
			// 生成返回对象
			E instance = newInstance(clz);
			for (FieldInfo info : fieldInfos) {
				int index = info.index;
				if (index > cols.size() - 1) {
					continue;
				}
				String content = cols.get(index);
				if (StringUtils.isBlank(content)) {
					continue;
				}
				try {
					info.inject(instance, content);
				} catch (Exception e) {
					logger.error("数值表[{}]的[{}]分页第[{}]行的配置内容[{}]错误", clz.getSimpleName(), sheetName,
							currentRow, content);
					throw e;
				}
			}

			if (idGetter.getValue(instance) == null) {
				logger.error("数值表[{}]的[{}]分页第[{}]行的配置内容[{}]错误 - 主键列NULL", clz.getSimpleName(),
						sheetName, currentRow, JsonUtils.object2String(instance));
			}
			results.add(instance);
			if (StringUtils.isNotBlank(tag) && tag.equalsIgnoreCase(ROW_END)) {
				done = true;
				return;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (vIsOpen) {
				value.append(ch, start, length);
			}
		}

		/**
		 * Converts an Excel column name like "C" to a zero-based index.
		 * @param name
		 * @return Index corresponding to the specified name
		 */
		private int nameToColumn(String name) {
			int column = -1;
			for (int i = 0; i < name.length(); ++i) {
				int c = name.charAt(i);
				column = (column + 1) * 26 + c - 'A';
			}
			return column;
		}

	}

}
