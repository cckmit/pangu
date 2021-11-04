package com.pangu.framework.resource.reader;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;
import com.pangu.framework.resource.other.Getter;
import com.pangu.framework.resource.other.GetterBuilder;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.reflect.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class EasyExcelReader implements ResourceReader {

    @Autowired
    private ConversionService conversionService;

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

    @Override
    public String getFormat() {
        return "xlsx";
    }

    /**
     * 属性信息
     *
     * @author frank
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
        List<E> result = new LinkedList<>();
        Getter idGetter = GetterBuilder.createIdGetter(clz);
        EasyExcel.read(input, new ReadListener<Map<Integer, String>>() {

            Collection<FieldInfo> infos;
            boolean sheetEnd;
            String sheetName;

            @Override
            public void onException(Exception e, AnalysisContext analysisContext) throws Exception {

            }

            @Override
            public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
                infos = null;
                sheetEnd = false;
                sheetName = analysisContext.readSheetHolder().getSheetName();
                CellData cellData = map.get(0);
                String firstCell = cellData.toString();
                if (firstCell.isEmpty()) {
                    sheetEnd = true;
                    return;
                }
                // 字段标识行开始
                if (ROW_SERVER.equals(firstCell)) {
                    infos = new ArrayList<>(map.size());
                    for (Map.Entry<Integer, CellData> entry : map.entrySet()) {
                        Integer k = entry.getKey();
                        if (k == 0) {
                            continue;
                        }
                        CellData v = entry.getValue();
                        String name = v.toString();
                        if (name.isEmpty()) {
                            continue;
                        }
                        try {
                            Field field = clz.getDeclaredField(name);
                            FieldInfo info = new FieldInfo(k, field);
                            infos.add(info);
                        } catch (Exception e) {
                            FormattingTuple message = MessageFormatter.arrayFormat("资源类[{}]分页[{}]的声明属性[{}]无法获取", new Object[]{
                                    clz, analysisContext.readSheetHolder().getSheetName(), name});
                            log.error(message.getMessage());
                            throw new IllegalStateException(message.getMessage(), e);
                        }
                    }
                }
            }

            @Override
            public void invoke(Map<Integer, String> o, AnalysisContext analysisContext) {
                if (sheetEnd) {
                    return;
                }
                String firstCell = o.get(0);
                if (infos == null) {
                    if (ROW_SERVER.equals(firstCell)) {
                        for (Map.Entry<Integer, String> entry : o.entrySet()) {
                            Integer k = entry.getKey();
                            if (k == 0) {
                                continue;
                            }
                            String name = entry.getValue();
                            if (StringUtils.isEmpty(name)) {
                                continue;
                            }
                            if (infos == null) {
                                infos = new ArrayList<>(o.size());
                            }
                            try {
                                Field field = clz.getDeclaredField(name);
                                FieldInfo info = new FieldInfo(k, field);
                                infos.add(info);
                            } catch (Exception e) {
                                FormattingTuple message = MessageFormatter.arrayFormat("资源类[{}]分页[{}]的声明属性[{}]无法获取", new Object[]{
                                        clz, analysisContext.readSheetHolder().getSheetName(), name});
                                log.error(message.getMessage());
                                throw new IllegalStateException(message.getMessage(), e);
                            }
                        }

                    }
                    return;
                }
                if (StringUtils.isNotEmpty(firstCell)) {
                    if (firstCell.equalsIgnoreCase(ROW_IGNORE)) {
                        return;
                    } else if (firstCell.equalsIgnoreCase(ROW_CLIENT)) {
                        return;
                    } else if (firstCell.equalsIgnoreCase(ROW_MANAGER)) {
                        return;
                    } else if (firstCell.equalsIgnoreCase(ROW_SERVER)) {
                        return;
                    } else if (firstCell.equalsIgnoreCase(ROW_END_BEFORE)) {
                        sheetEnd = true;
                        return;
                    }
                }
                // 生成返回对象
                E instance = newInstance(clz);
                for (FieldInfo info : infos) {
                    String content = o.get(info.index);
                    if (StringUtils.isEmpty(content)) {
                        continue;
                    }
                    try {
                        inject(instance, info.field, content);
                    } catch (Exception e) {
                        log.error("数值表[{}]的[{}]分页第[{}]行的配置内容[{}]错误",
                                clz.getSimpleName(), sheetName, analysisContext.readRowHolder().getRowIndex(), content);
                        throw e;
                    }
                }
                if (idGetter.getValue(instance) == null) {
                    log.error(
                            "数值表[{}]的[{}]分页第[{}]行的配置内容[{}]错误 - 主键列NULL",
                            clz.getSimpleName(), sheetName, analysisContext.readRowHolder().getRowIndex(),
                            JsonUtils.object2String(instance));
                }
                result.add(instance);
                if (ROW_END.equals(firstCell)) {
                    sheetEnd = true;
                }
            }

            @Override
            public void extra(CellExtra cellExtra, AnalysisContext analysisContext) {

            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }

            @Override
            public boolean hasNext(AnalysisContext analysisContext) {
                return true;
            }
        }).doReadAll();
        return result;
    }

    private <E> E newInstance(Class<E> clz) {
        try {
            return clz.newInstance();
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("资源[{}]无法实例化", clz);
            log.error(message.getMessage());
            throw new RuntimeException(message.getMessage());
        }
    }

    /**
     * 给实例注入属性
     *
     * @param instance
     * @param field
     * @param content
     */
    private void inject(Object instance, Field field, String content) {
        // Class<?> clz = field.getType();
        try {
            TypeDescriptor targetType = new TypeDescriptor(field);
            Object value = conversionService.convert(content, sourceType, targetType);
            field.set(instance, value);
        } catch (ConverterNotFoundException e) {
            FormattingTuple message = MessageFormatter.format("静态资源[{}]属性[{}]的转换器不存在", instance.getClass()
                    .getSimpleName(), field.getName());
            log.error(message.getMessage(), e);
            throw new IllegalStateException(message.getMessage(), e);
        } catch (Exception e) {
            FormattingTuple message = MessageFormatter.format("属性[{}]注入失败", field);
            log.error(message.getMessage());
            throw new IllegalStateException(message.getMessage(), e);
        }
    }
}
