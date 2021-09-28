package com.zw.platform.util.excel;

import com.zw.platform.util.Reflections;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.excel.validator.ImportValidator;
import com.zw.platform.util.imports.ImportErrorData;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式）
 */
public class ImportExcel {

    private static final Logger log = LoggerFactory.getLogger(ImportExcel.class);

    /**
     * 工作表对象
     */
    private final Sheet sheet;

    /**
     * 标题行号
     */
    private final int headerNum;

    private ImportValidator validator;

    private String dateFormat = "yyyy-MM-dd";

    public <T> void setImportValidator(ImportValidator<T> validator) {
        this.validator = validator;
    }

    public ImportValidator getImportValidator() {
        return this.validator;
    }

    /**
     * 构造函数
     * @param fileName  导入文件，读取第一个工作表
     * @param headerNum 标题行号，数据行号=标题行号+1
     */
    public ImportExcel(String fileName, int headerNum) throws InvalidFormatException, IOException {
        this(new File(fileName), headerNum);
    }

    /**
     * 构造函数
     * @param file      导入文件对象，读取第一个工作表
     * @param headerNum 标题行号，数据行号=标题行号+1
     */
    public ImportExcel(File file, int headerNum) throws InvalidFormatException, IOException {
        this(file, headerNum, 0);
    }

    /**
     * 构造函数
     * @param fileName   导入文件
     * @param headerNum  标题行号，数据行号=标题行号+1
     * @param sheetIndex 工作表编号
     */
    public ImportExcel(String fileName, int headerNum, int sheetIndex) throws InvalidFormatException, IOException {
        this(new File(fileName), headerNum, sheetIndex);
    }

    /**
     * 构造函数
     * @param file       导入文件对象
     * @param headerNum  标题行号，数据行号=标题行号+1
     * @param sheetIndex 工作表编号
     */
    public ImportExcel(File file, int headerNum, int sheetIndex) throws InvalidFormatException, IOException {
        this(file.getName(), new FileInputStream(file), headerNum, sheetIndex);
    }

    /**
     * 构造函数
     * @param multipartFile 导入文件对象
     * @param headerNum     标题行号，数据行号=标题行号+1
     * @param sheetIndex    工作表编号
     */
    public ImportExcel(MultipartFile multipartFile, int headerNum, int sheetIndex) throws IOException {
        this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), headerNum, sheetIndex);
    }

    public ImportExcel(MultipartFile multipartFile) throws IOException {
        this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), 1, 0);
    }

    public ImportExcel(MultipartFile multipartFile, int headerNum, int sheetIndex, String dateFormat)
        throws IOException {
        this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), headerNum, sheetIndex);
        this.dateFormat = dateFormat;
    }

    /**
     * 构造函数
     * @param fileName   导入文件对象
     * @param headerNum  标题行号，数据行号=标题行号+1
     * @param sheetIndex 工作表编号
     */
    public ImportExcel(String fileName, InputStream is, int headerNum, int sheetIndex) {
        //工作薄对象
        Workbook wb;
        if (StringUtils.isBlank(fileName)) {
            throw new RuntimeException("导入文档为空!");
        } else if (fileName.toLowerCase().endsWith("xls")) {
            try {
                wb = new HSSFWorkbook(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (fileName.toLowerCase().endsWith("xlsx")) {
            try {
                wb = new XSSFWorkbook(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("文档格式不正确!");
        }
        if (wb.getNumberOfSheets() < sheetIndex) {
            throw new RuntimeException("文档中没有工作表!");
        }
        this.sheet = wb.getSheetAt(sheetIndex);
        this.headerNum = getHeaderNum(headerNum, wb);
        log.debug("Initialize success.");
    }

    /**
     * 判断第一行是不是表头
     */
    public int getHeaderNum(int headerNum, Workbook wb) {

        Row row = sheet.getRow(0);
        short lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                //没有表头，从第一行数据开始
                headerNum = 0;
                break;
            }
            CellType cellType = cell.getCellType();
            String name = cellType.name();
            String value = null;
            switch (name) {
                case "STRING":
                    value = cell.getStringCellValue();
                    break;
                case "NUMERIC":
                    value = cell.getNumericCellValue() + "";
                    break;
                case "BLANK":
                    value = "";
                    break;
                case "DATE":
                    value = cell.getDateCellValue().getTime() + "";
                    break;
                case "BOOLEAN":
                    value = cell.getBooleanCellValue() + "";
                    break;
                default:
                    break;
            }
            if (value == null) {
                continue;
            }
            //表头包含*号或者中位冒号，代表表头,信息配置的获取导入特殊处理
            boolean flag1 = value.contains("*") || value.contains("：") || value.contains("粗体标题对应列为必填项");
            boolean flag2 = false;
            boolean flag3;
            CellStyle cellStyle = cell.getCellStyle();
            short color;
            if (cellStyle instanceof HSSFCellStyle) {
                HSSFCellStyle hssfCellStyle = (HSSFCellStyle) cellStyle;
                HSSFFont font = hssfCellStyle.getFont(wb);
                color = font.getColor();
                //表头的样式如果和平台设置的一样 认为有表头存在
                flag3 = ExportExcel.HEADER_FOUNT_NAME.equals(font.getFontName())
                    && font.getFontHeightInPoints() == ExportExcel.HEADER_FONT_HEIGHT_INPOINTS
                    && font.getBold() == ExportExcel.HEADER_BOLD && font.getColor() == ExportExcel.HEADER_COLOR;
            } else {
                XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
                XSSFFont font = xssfCellStyle.getFont();
                color = font.getColor();
                //表头的样式如果和平台设置的一样 认为有表头存在
                flag3 = ExportExcel.HEADER_FOUNT_NAME.equals(font.getFontName())
                    && font.getFontHeightInPoints() == ExportExcel.HEADER_FONT_HEIGHT_INPOINTS
                    && font.getBold() == ExportExcel.HEADER_BOLD && font.getColor() == ExportExcel.HEADER_COLOR;
            }
            //判断颜色是不是红色
            if (color == 10) {
                flag2 = true;
            }
            if (flag1 && flag2) {
                //代表有表头，数据从设置的行开始
                break;
            }
            if (flag3) {
                //如果样式和平台的样式一致代表有表头，数据从设置的行开始
                break;
            }
            //无表头，并且循环到m/infoconfig/infoinput/export.gsp最后一行
            if (i == lastCellNum - 1) {
                headerNum = 0;
            }
        }
        return headerNum;
    }

    /**
     * 获取行对象
     */
    public Row getRow(int rowNum) {
        return this.sheet.getRow(rowNum);
    }

    /**
     * 获取数据行号
     */
    public int getDataRowNum() {
        return headerNum + 1;
    }

    /**
     * 获取最后一个数据行号
     */
    public int getLastDataRowNum() {
        int num = this.sheet.getLastRowNum() + headerNum;
        if (headerNum == 0) {
            //如果第一行没有表头，要加一行，因为excel的行角标从0开始
            num = num + 1;
        }
        return num;
    }

    /**
     * 获取最后一个列号
     */
    public int getLastCellNum() {
        //excel版本判断  如果是空表  XSSFWorkbook版本getRow(headerNum)为 null
        if (this.getRow(headerNum) == null) {
            return 0;
        }
        return this.getRow(headerNum).getLastCellNum();
    }

    /**
     * 获取单元格值
     * @param row    获取的行
     * @param column 获取单元格列号
     * @return 单元格值
     */
    public Object getCellValue(Row row, int column) {
        Object val = "";
        DecimalFormat df = getDecimalFormat();
        try {
            Cell cell = row.getCell(column);
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                        val = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                    } else {
                        val = df.format(cell.getNumericCellValue());
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.FORMULA) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue() + "";
                } else if (cell.getCellType() == CellType.ERROR) {
                    val = cell.getErrorCellValue() + "";
                } else if (cell.getCellType() == CellType.BLANK) {
                    val = "";
                }
            }
        } catch (Exception e) {
            return val;
        }
        return val;
    }

    protected DecimalFormat getDecimalFormat() {
        return new DecimalFormat("#.#");
    }

    /**
     * 获取导入数据列表
     * @param cls    导入对象类型
     * @param groups 导入分组
     */
    public <E> List<E> getDataList(Class<E> cls, int... groups) throws InstantiationException, IllegalAccessException {
        List<Object[]> annotationList = getAnnotationList(cls, groups);
        // Get excel data
        List<E> dataList = new ArrayList<>();
        List<String> invalidTitles = new ArrayList<>();
        Map<Integer, Set<Object>> repeatColumns = new HashMap<>();
        for (int i = getDataRowNum() - 1, n = getLastDataRowNum(); i < n; i++) {
            E e = cls.newInstance();
            int column = 0;
            Row row = this.getRow(i);
            if (isBlankRow(row)) {
                continue;
            }
            buildAnnotaionList(annotationList, invalidTitles, repeatColumns, i, e, column, row);
            if (!invalidTitles.isEmpty() && validator != null) {
                String titles = String.join(",", invalidTitles);
                validator.recordInvalidInfo(String.format("第%d条数据【%s】必填字段未填<br/>", i, titles));
                invalidTitles.clear();
            }
            dataList.add(e);
        }
        return dataList;
    }

    /**
     * 获取导入数据列表
     * @param cls 导入对象类型
     */
    public <E extends ImportErrorData> List<E> getDataListNew(Class<E> cls, int... groups)
        throws InstantiationException, IllegalAccessException {
        List<Object[]> annotationList = getAnnotationList(cls, groups);
        // Get excel data
        List<E> dataList = new ArrayList<>();
        List<String> invalidTitles = new ArrayList<>();
        Map<Integer, Set<Object>> repeatColumns = new HashMap<>();
        for (int i = getDataRowNum() - 1, n = getLastDataRowNum(); i < n; i++) {
            E e = cls.newInstance();
            int column = 0;
            Row row = this.getRow(i);
            if (isBlankRow(row)) {
                continue;
            }
            buildAnnotaionListNew(annotationList, invalidTitles, repeatColumns, i, e, column, row);
            if (!invalidTitles.isEmpty() && validator != null) {
                String titles = String.join(",", invalidTitles);
                validator.recordInvalidInfo(String.format("第%d条数据【%s】必填字段未填<br/>", i, titles));
                invalidTitles.clear();
            }
            dataList.add(e);
        }
        return dataList;
    }

    private <E extends ImportErrorData> void buildAnnotaionListNew(List<Object[]> annotationList,
        List<String> invalidTitles, Map<Integer, Set<Object>> repeatColumns, int i, E e, int column, Row row) {
        for (Object[] os : annotationList) {
            Object val = this.getCellValue(row, column++);
            ExcelField ef = (ExcelField) os[0];
            if (ef.required() && StringUtils.isBlank(val.toString())) {
                String title = ef.title();
                e.setErrorMsg(title + "：不能为空");
                if (validator != null) {
                    invalidTitles.add(ef.title());
                    validator.setValidatorSet(i);
                }
                continue;
            }
            // Get param type and type cast
            if (val.toString().isEmpty()) {
                continue;
            }
            if (!ef.repeatable()) {
                Set<Object> repeatColumn = repeatColumns.computeIfAbsent(column, k -> new HashSet<>());
                if (repeatColumn.contains(val)) {
                    String title = ef.title();
                    e.setErrorMsg(title + "：重复");
                    if (validator != null) {
                        validator.recordInvalidInfo(String.format("第%d条数据【%s】导入字段重复<br/>", i, ef.title()));
                        validator.setValidatorSet(i);
                    }
                }
                repeatColumn.add(val);
            }
            Class<?> valType = getValueType(os[1]);
            val = getValue(i, column, val, ef, valType);
            // set entity value
            if (os[1] instanceof Field) {
                Reflections.invokeSetter(e, ((Field) os[1]).getName(), val);
            } else if (os[1] instanceof Method) {
                String methodName = ((Method) os[1]).getName();
                if ("get".equals(methodName.substring(0, 3))) {
                    methodName = "set" + StringUtils.substringAfter(methodName, "get");
                }
                Reflections.invokeMethod(e, methodName, new Class[] { valType }, new Object[] { val });
            }
        }
    }

    private <E> void buildAnnotaionList(List<Object[]> annotationList, List<String> invalidTitles,
        Map<Integer, Set<Object>> repeatColumns, int i, E e, int column, Row row) {
        for (Object[] os : annotationList) {
            Object val = this.getCellValue(row, column++);
            ExcelField ef = (ExcelField) os[0];
            if (validator != null && !validator.validateRequired(ef, val)) {
                invalidTitles.add(ef.title());
                validator.setValidatorSet(i);
                continue;
            }

            checkRepeat(repeatColumns, i, column, val, ef);

            // Get param type and type cast
            if (val.toString().isEmpty()) {
                continue;
            }
            Class<?> valType = getValueType(os[1]);
            val = getValue(i, column, val, ef, valType);
            // set entity value
            if (os[1] instanceof Field) {
                Reflections.invokeSetter(e, ((Field) os[1]).getName(), val);
            } else if (os[1] instanceof Method) {
                String methodName = ((Method) os[1]).getName();
                if ("get".equals(methodName.substring(0, 3))) {
                    methodName = "set" + StringUtils.substringAfter(methodName, "get");
                }
                Reflections.invokeMethod(e, methodName, new Class[] { valType }, new Object[] { val });
            }
        }
    }

    private <E> List<Object[]> getAnnotationList(Class<E> cls, int[] groups) {
        List<Object[]> annotationList = new ArrayList<>();
        // Get annotation field
        Field[] fs = cls.getDeclaredFields();
        for (Field f : fs) {
            ExcelField ef = f.getAnnotation(ExcelField.class);
            getAnnotations(annotationList, ef, groups, new Object[] { ef, f });
        }
        // Get annotation method
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            ExcelField ef = m.getAnnotation(ExcelField.class);
            getAnnotations(annotationList, ef, groups, new Object[] { ef, m });
        }
        // Field sorting
        annotationList.sort(Comparator.comparing(o -> (((ExcelField) o[0]).sort())));
        return annotationList;
    }

    private void checkRepeat(Map<Integer, Set<Object>> repeatColumns, int row, int column, Object val, ExcelField ef) {
        if (validator != null && !ef.repeatable()) {
            Set<Object> repeatColumn = repeatColumns.computeIfAbsent(column, k -> new HashSet<>());
            if (repeatColumn.contains(val)) {
                validator.recordInvalidInfo(String.format("第%d条数据【%s】导入字段重复<br/>", row, ef.title()));
                validator.setValidatorSet(row);
            }
            repeatColumn.add(val);
        }
    }

    private Class<?> getValueType(Object o) {
        Class<?> valType = Class.class;
        if (o instanceof Field) {
            valType = ((Field) o).getType();
        } else if (o instanceof Method) {
            Method method = ((Method) o);
            if ("get".equals(method.getName().substring(0, 3))) {
                valType = method.getReturnType();
            } else if ("set".equals(method.getName().substring(0, 3))) {
                valType = ((Method) o).getParameterTypes()[0];
            }
        }
        return valType;
    }

    private Object getValue(int i, int column, Object val, ExcelField ef, Class<?> valType) {
        try {
            if (valType == String.class) {
                String s = String.valueOf(val.toString());
                if (RegexUtils.checkDate(s)) {
                    val = Converter.toString(Converter.toDate(s), dateFormat);
                }
                if (StringUtils.endsWith(s, ".0")) {
                    val = StringUtils.substringBefore(s, ".0");
                } else {
                    val = String.valueOf(val.toString());
                }
            } else if (valType == Integer.class) {
                val = Double.valueOf(val.toString()).intValue();
            } else if (valType == Long.class) {
                val = Double.valueOf(val.toString()).longValue();
            } else if (valType == Double.class) {
                val = Double.valueOf(val.toString());
            } else if (valType == Float.class) {
                val = Float.valueOf(val.toString());
            } else if (valType == Short.class) {
                val = Short.valueOf(val.toString());
            } else if (valType == Date.class) {
                val = Converter.toDate(val.toString(), dateFormat);
            } else if (valType == DateTime.class) {
                val = DateUtil.getJavaDate((Double) val);
            } else {
                if (ef.fieldType() != Class.class) {
                    val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val.toString());
                } else {
                    val = Class.forName(this.getClass().getName()
                        .replaceAll(this.getClass().getSimpleName(), "fieldtype." + valType.getSimpleName() + "Type"))
                        .getMethod("getValue", String.class).invoke(null, val.toString());
                }
            }
        } catch (Exception ex) {
            log.info("Get cell value [" + i + "," + column + "] error: " + ex.toString());
            val = null;
        }
        return val;
    }

    private void getAnnotations(List<Object[]> annotationList, ExcelField ef, int[] groups, Object[] e) {
        if (ef == null || ef.type() == 1) {
            return;
        }
        if (groups == null || groups.length == 0) {
            annotationList.add(e);
            return;
        }
        for (int g : groups) {
            for (int efg : ef.groups()) {
                if (g == efg) {
                    annotationList.add(e);
                    return;
                }
            }
        }
    }

    /**
     * 判断导入的当前行是否为空行
     * @author Liubangquan
     */
    private static boolean isBlankRow(Row row) {
        DecimalFormat df = new DecimalFormat("#");
        if (row == null) {
            return true;
        }
        boolean result = true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String value;
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        value = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                    } else {
                        value = df.format(cell.getNumericCellValue());
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    value = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.FORMULA) {
                    value = cell.getCellFormula();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    value = cell.getBooleanCellValue() + "";
                } else if (cell.getCellType() == CellType.ERROR) {
                    value = cell.getErrorCellValue() + "";
                } else if (cell.getCellType() == CellType.BLANK) {
                    value = "";
                } else {
                    break;
                }
                if (!value.trim().equals("")) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}
