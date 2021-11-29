package com.pangu.framework.resource.reader;

import com.pangu.framework.resource.other.Getter;
import com.pangu.framework.resource.other.GetterBuilder;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.reflect.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ExcelReader implements ResourceReader {

    private final static Logger logger = LoggerFactory.getLogger(ExcelReader.class);

    /**
     * 配置分隔符
     */
    private final static String SPLIT = ":";
    /**
     * 结束标识
     */
    private final static String ROW_END = "END";
    /**
     * 上行结束标识
     */
    private final static String ROW_END_BEFORE = "END_BEFORE";
    /**
     * 忽略标识
     */
    private final static String ROW_IGNORE = "NO";

    private final static TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);

    /**
     * 客户端控制标识
     */
    private final static String ROW_CLIENT = "CLIENT";
    /**
     * 管理后台控制标识
     */
    private final static String ROW_MANAGER = "MANAGER";
    /**
     * 服务端控制标识同时也是数据开始标识
     */
    private final static String ROW_SERVER = "SERVER";

    /**
     * 数据标记行
     */
    private String tagRow = "SERVER";
    /**
     * 数据开始行号
     */
    private int startRow = 0;
    /**
     * 每个分页独立表头
     */
    private boolean titlePerSheet;

    @Override
    public String getFormat() {
        return "excel";
    }

    /**
     * 属性信息
     *
     * @author author
     */
    private static class FieldInfo {
        /**
         * 第几列
         */
        public final int index;
        /**
         * 资源类属性
         */
        public final Field field;

        /**
         * 构造方法
         */
        public FieldInfo(int index, Field field) {
            ReflectionUtils.makeAccessible(field);
            this.index = index;
            this.field = field;
        }
    }

    @Override
    public <E> List<E> read(InputStream input, Class<E> clz) {
        // 基本信息获取
        Workbook wb = getWorkbook(input, clz);
        Sheet[] sheets = getSheets(wb, clz);
        Getter idGetter = GetterBuilder.createIdGetter(clz);
        // 表头信息
        Collection<FieldInfo> infos = titlePerSheet ? null : getCellInfos(sheets[0], clz);
        // 创建返回数据集
        List<E> result = new LinkedList<E>();
        for (Sheet sheet : sheets) {
            logger.debug("正在加载资源[{}] - [{}] ...", clz.getName(), sheet.getSheetName());
            boolean start = false;
            int rowNum = 0;
            // 清除上次表头信息
            if (titlePerSheet) {
                infos = null;
            }
            for (Row row : sheet) {
                // 忽略空行
                if (row == null) {
                    continue;
                }
                // 判断数据行开始没有
                if (!start) {
                    Cell cell = row.getCell(0);
                    if (cell == null) {
                        continue;
                    }
                    String content = getCellContent(cell);
                    if (content == null) {
                        continue;
                    }
                    if (content.equalsIgnoreCase(tagRow)) {
                        start = true;
                    }
                    continue;
                }
                // 跳过忽略行
                rowNum++;
                if (rowNum <= startRow) {
                    continue;
                }

                // 读取表头信息
                if (infos == null) {
                    infos = getCellInfos(sheet, clz);
                }

                // 忽略单行
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String content = getCellContent(cell);
                    if (content != null) {
                        if (content.equalsIgnoreCase(ROW_IGNORE)) {
                            continue;
                        } else if (content.equalsIgnoreCase(ROW_CLIENT)) {
                            continue;
                        } else if (content.equalsIgnoreCase(ROW_MANAGER)) {
                            continue;
                        } else if (content.equalsIgnoreCase(ROW_SERVER)) {
                            continue;
                        } else if (content.equalsIgnoreCase(ROW_END_BEFORE)) {
                            // 忽略本行并结束
                            break;
                        }
                    }
                }

                // 生成返回对象
                E instance = newInstance(clz);
                for (FieldInfo info : infos) {
                    cell = row.getCell(info.index);
                    if (cell == null) {
                        continue;
                    }
                    String content = getCellContent(cell);
                    if (StringUtils.isEmpty(content)) {
                        continue;
                    }
                    try {
                        inject(row, instance, info.field, content);
                    } catch (Exception e) {
                        logger.error("数值表[{}]的[{}]分页第[{}]行的配置内容[{}]错误",
								clz.getSimpleName(), sheet.getSheetName(), row.getRowNum(), content);
                        throw e;
                    }
                }
                if (idGetter.getValue(instance) == null) {
                    logger.error(
                            "数值表[{}]的[{}]分页第[{}]行的配置内容[{}]错误 - 主键列NULL",
							clz.getSimpleName(), sheet.getSheetName(), row.getRowNum(),
							JsonUtils.object2String(instance));
                }
                result.add(instance);

                // 结束处理
                cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }
                String content = getCellContent(cell);
                if (content == null) {
                    continue;
                }
                if (content.equalsIgnoreCase(ROW_END)) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取字符串形式的单元格内容
     *
     * @param cell
     * @return
     */
    private String getCellContent(Cell cell) {
        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            // // 公式类型的强制设置单元格为文本格式
            // cell.setCellType(Cell.CELL_TYPE_STRING);
            // return cell.getStringCellValue();
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                double v1 = cell.getNumericCellValue();
                Double v2 = Double.valueOf(v1);
                if (v1 == v2.intValue()) {
                    return String.valueOf(v2.intValue());
                } else if (v1 == v2.longValue()) {
                    return String.valueOf(v2.longValue());
                } else {
                    return v2.toString();
                }
            default:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                return cell.getStringCellValue();
        }
    }

    @Autowired
    private ConversionService conversionService;

    /**
     * 给实例注入属性
     *
     * @param instance
     * @param field
     * @param content
     */
    private void inject(Row row, Object instance, Field field, String content) {
        // Class<?> clz = field.getType();
        try {
            TypeDescriptor targetType = new TypeDescriptor(field);
            Object value = conversionService.convert(content, sourceType, targetType);
            field.set(instance, value);
        } catch (ConverterNotFoundException e) {
            FormattingTuple message = MessageFormatter.format("静态资源[{}]属性[{}]的转换器不存在", instance.getClass()
                    .getSimpleName(), field.getName());
            logger.error(message.getMessage(), e);
            throw new IllegalStateException(message.getMessage(), e);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("属性[{}]注入失败", field);
            logger.error(message.getMessage());
            throw new IllegalStateException(message.getMessage(), e);
        }
    }

    /**
     * 实例化资源
     *
     * @param <E>
     * @param clz
     * @return
     */
    private <E> E newInstance(Class<E> clz) {
        try {
            return clz.newInstance();
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("资源[{}]无法实例化", clz);
            logger.error(message.getMessage());
            throw new RuntimeException(message.getMessage());
        }
    }

    /**
     * 获取表格信息
     *
     * @param sheet
     * @param clz
     * @return
     */
    private Collection<FieldInfo> getCellInfos(Sheet sheet, Class<?> clz) {
        // 获取属性控制行
        Row fieldRow = getFieldRow(sheet, clz);
        if (fieldRow == null) {
            FormattingTuple message = MessageFormatter.format("无法获取资源[{}]的EXCEL文件的属性控制列", clz);
            logger.error(message.getMessage());
            throw new IllegalStateException(message.getMessage());
        }

        // 获取属性信息集合
        List<FieldInfo> result = new ArrayList<FieldInfo>();
        for (int i = 1; i < fieldRow.getLastCellNum(); i++) {
            Cell cell = fieldRow.getCell(i);
            if (cell == null) {
                continue;
            }

            String name = getCellContent(cell);
            if (StringUtils.isBlank(name)) {
                continue;
            }

            try {
                Field field = clz.getDeclaredField(name);
                FieldInfo info = new FieldInfo(i, field);
                result.add(info);
            } catch (Exception e) {
                FormattingTuple message = MessageFormatter.arrayFormat("资源类[{}]分页[{}]的声明属性[{}]无法获取", new Object[]{
                        clz, sheet.getSheetName(), name});
                logger.error(message.getMessage());
                throw new IllegalStateException(message.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 获取属性控制行
     *
     * @param sheet
     * @param clz
     * @return
     */
    private Row getFieldRow(Sheet sheet, Class<?> clz) {
        for (Row row : sheet) {
            Cell cell = row.getCell(0);
            if (cell == null) {
                continue;
            }
            String content = getCellContent(cell);
            if (content != null && content.equals(tagRow)) {
                return row;
            }
        }
        return null;
    }

    /**
     * 获取资源类型对应的工作簿
     *
     * @param wb  Excel Workbook
     * @param clz 资源类型
     * @return
     */
    private Sheet[] getSheets(Workbook wb, Class<?> clz) {
        try {
            List<Sheet> result = new ArrayList<Sheet>();
            String name = clz.getSimpleName();
            // 处理多Sheet数据合并
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                if (sheet.getLastRowNum() <= 0) {
                    continue;
                }
                Row row = sheet.getRow(0);
                if (row == null) {
                    // 忽略首行NULL的工作表
                    continue;
                }
                if (row.getLastCellNum() <= 0) {
                    continue;
                }
                Cell cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }
                if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                }
                String text = cell.getStringCellValue();
                if (name.equals(text)) {
                    result.add(sheet);
                }
            }
            if (result.size() > 0) {
                return result.toArray(new Sheet[0]);
            }

            // 没有需要多Sheet合并的情况
            Sheet sheet = wb.getSheet(name);
            if (sheet != null) {
                return new Sheet[]{sheet};
            } else {
                return new Sheet[]{wb.getSheetAt(0)};
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无法获取资源类[" + clz.getSimpleName() + "]对应的Excel数据表", e);
        }
    }

    /**
     * 通过输入流获取{@link Workbook}
     *
     * @param input
     * @return
     */
    private Workbook getWorkbook(InputStream input, @SuppressWarnings("rawtypes") Class clz) {
        try {
            return WorkbookFactory.create(input);
        } catch (InvalidFormatException e) {
            throw new RuntimeException("静态资源[" + clz.getSimpleName() + "]异常,无效的文件格式", e);
        } catch (IOException e) {
            throw new RuntimeException("静态资源[" + clz.getSimpleName() + "]异常,无法读取文件", e);
        }
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

}
