package com.zw.platform.util.excel;

import com.google.common.collect.Lists;
import com.zw.platform.util.Encodes;
import com.zw.platform.util.Reflections;
import com.zw.platform.util.excel.annotation.ExcelField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigDataExportExcel {
    private static Logger log = LoggerFactory.getLogger(ExportExcel.class);
    /**
     * 工作薄对象
     */
    private SXSSFWorkbook wb;

    /**
     * 工作表对象
     */
    private SXSSFSheet sheet;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    public Map<String, CellStyle> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, CellStyle> styles) {
        this.styles = styles;
    }

    /**
     * 当前行号
     */
    private int rowNum;

    /**
     * 窗口中能访问Row的数量
     */
    private int windowSize;

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String[] list;

    /**
     * 注解列表（Object[]{ ExcelField, Field/Method }）
     */
    private List<Object[]> annotationList = new ArrayList<>();

    /**
     * 构造函数
     * @param windowSize 导出excel的窗口的row行数
     * @param title      表格标题，传“空值”，表示无标题
     * @param cls        实体对象，通过annotation.ExportField获取标题
     */
    public BigDataExportExcel(int windowSize, String title, Class<?> cls) {
        this(windowSize, title, cls, 1);
    }

    public BigDataExportExcel(int windowSize, String title, Class<?> cls, int type, int... groups) {
        // Get annotation field
        Field[] fs = cls.getDeclaredFields();
        for (Field f : fs) {
            ExcelField ef = f.getAnnotation(ExcelField.class);
            if (ef != null && (ef.type() == 0 || ef.type() == type)) {
                if (groups != null && groups.length > 0) {
                    boolean inGroup = false;
                    for (int g : groups) {
                        if (inGroup) {
                            break;
                        }
                        for (int efg : ef.groups()) {
                            if (g == efg) {
                                inGroup = true;
                                annotationList.add(new Object[] { ef, f });
                                break;
                            }
                        }
                    }
                } else {
                    annotationList.add(new Object[] { ef, f });
                }
            }
        }
        // Get annotation method
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            ExcelField ef = m.getAnnotation(ExcelField.class);
            if (ef != null && (ef.type() == 0 || ef.type() == type)) {
                if (groups != null && groups.length > 0) {
                    boolean inGroup = false;
                    for (int g : groups) {
                        if (inGroup) {
                            break;
                        }
                        for (int efg : ef.groups()) {
                            if (g == efg) {
                                inGroup = true;
                                annotationList.add(new Object[] { ef, m });
                                break;
                            }
                        }
                    }
                } else {
                    annotationList.add(new Object[] { ef, m });
                }
            }
        }
        // Field sorting
        annotationList.sort(Comparator.comparingInt(o -> ((ExcelField) o[0]).sort()));
        // Initialize
        List<String> headerList = new ArrayList<>();
        for (Object[] os : annotationList) {
            String t = ((ExcelField) os[0]).title();
            // 如果是导出，则去掉注释
            if (type == 1) {
                String[] ss = StringUtils.split(t, "**", 2);
                if (ss.length == 2) {
                    t = ss[0];
                }
            }
            headerList.add(t);
        }
        initialize(windowSize, title, headerList);
    }

    /**
     * 构造函数
     * @param windowSize 导出excel的窗口的row行数
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    public BigDataExportExcel(int windowSize, String title, List<String> headerList) {
        initialize(windowSize, title, headerList);
    }

    /**
     * 初始化函数
     * @param windowSize 导出excel的窗口的row行数
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    private void initialize(int windowSize, String title, List<String> headerList) {
        if (windowSize < 100) {
            windowSize = 100;
        }
        this.wb = new SXSSFWorkbook(windowSize);
        wb.setCompressTempFiles(true);
        this.sheet = wb.createSheet("Export");
        this.styles = createStyles(wb);
        // Create title
        if (StringUtils.isNotBlank(title)) {
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(),
                headerList.size() - 1));
        }
        sheet.trackAllColumnsForAutoSizing();
        // Create header
        if (headerList != null && headerList.size() > 0) {
            // throw new RuntimeException("headerList not null!");
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(16);

            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellStyle(styles.get("header"));
                String[] ss = StringUtils.split(headerList.get(i), "**", 2);
                if (ss.length == 2) {
                    cell.setCellValue(ss[0]);
                    Comment comment = this.sheet.createDrawingPatriarch()
                        .createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
                    comment.setString(new XSSFRichTextString(ss[1]));
                    cell.setCellComment(comment);
                } else {
                    cell.setCellValue(headerList.get(i));
                }
                sheet.autoSizeColumn(i);
            }
            for (int i = 0; i < headerList.size(); i++) {
                int colWidth = sheet.getColumnWidth(i) * 2;
                sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);
            }
        }
        log.debug("Initialize success.");
    }

    /**
     * 构造函数(wangjianyu)
     * @param windowSize 导出excel的窗口的row行数
     * @param title      表格标题，传“空值”，表示无标题
     */
    public BigDataExportExcel(int windowSize, String title, Class<?> cls, int type, String time, String group,
        int... groups) {
        // Get annotation field
        Field[] fs = cls.getDeclaredFields();
        for (Field f : fs) {
            ExcelField ef = f.getAnnotation(ExcelField.class);
            if (ef != null && (ef.type() == 0 || ef.type() == type)) {
                if (groups != null && groups.length > 0) {
                    boolean inGroup = false;
                    for (int g : groups) {
                        if (inGroup) {
                            break;
                        }
                        for (int efg : ef.groups()) {
                            if (g == efg) {
                                inGroup = true;
                                annotationList.add(new Object[] { ef, f });
                                break;
                            }
                        }
                    }
                } else {
                    annotationList.add(new Object[] { ef, f });
                }
            }
        }
        // Get annotation method
        Method[] ms = cls.getDeclaredMethods();
        for (Method m : ms) {
            ExcelField ef = m.getAnnotation(ExcelField.class);
            if (ef != null && (ef.type() == 0 || ef.type() == type)) {
                if (groups != null && groups.length > 0) {
                    boolean inGroup = false;
                    for (int g : groups) {
                        if (inGroup) {
                            break;
                        }
                        for (int efg : ef.groups()) {
                            if (g == efg) {
                                inGroup = true;
                                annotationList.add(new Object[] { ef, m });
                                break;
                            }
                        }
                    }
                } else {
                    annotationList.add(new Object[] { ef, m });
                }
            }
        }
        // Field sorting
        annotationList.sort(Comparator.comparing(o -> (((ExcelField) o[0]).sort())));
        // Initialize
        List<String> headerList = new ArrayList<>();
        for (Object[] os : annotationList) {
            String t = ((ExcelField) os[0]).title();
            // 如果是导出，则去掉注释
            if (type == 1) {
                String[] ss = StringUtils.split(t, "**", 2);
                if (ss.length == 2) {
                    t = ss[0];
                }
            }
            headerList.add(t);
        }
        initialize(windowSize, title, headerList, time, group);
    }

    /**
     * 初始化函数
     * @param windowSize 导出excel的窗口的row行数
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    private void initialize(int windowSize, String title, List<String> headerList, String time, String group) {
        if (windowSize < 100) {
            windowSize = 100;
        }
        this.wb = new SXSSFWorkbook(windowSize);
        wb.setCompressTempFiles(true);
        this.sheet = wb.createSheet("Export");
        this.styles = createStyles(wb);
        // Create title
        if (StringUtils.isNotBlank(title)) {
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);

            Row rtime = sheet.createRow(rowNum++);
            rtime.setHeightInPoints(20);
            Cell ctime = rtime.createCell(0);
            ctime.setCellStyle(styles.get("time"));
            ctime.setCellValue(time);

            Row rgroup = sheet.createRow(rowNum++);
            rgroup.setHeightInPoints(20);
            Cell cgroup = rgroup.createCell(0);
            /* cgroup.setCellStyle(styles.get("time")); */
            cgroup.setCellValue(group);

            sheet.addMergedRegion(new CellRangeAddress(rgroup.getRowNum(), rgroup.getRowNum(), rgroup.getRowNum() - 2,
                headerList.size() - 1));// 合并行
            sheet.addMergedRegion(new CellRangeAddress(rtime.getRowNum(), rtime.getRowNum(), rtime.getRowNum() - 1,
                headerList.size() - 1));// 合并行
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(),
                headerList.size() - 1));
        }
        // Create header
        if (headerList != null && headerList.size() > 0) {
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(16);
            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellStyle(styles.get("header"));
                String[] ss = StringUtils.split(headerList.get(i), "**", 2);
                if (ss.length == 2) {
                    cell.setCellValue(ss[0]);
                    Comment comment = this.sheet.createDrawingPatriarch()
                        .createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
                    comment.setString(new XSSFRichTextString(ss[1]));
                    cell.setCellComment(comment);
                } else {
                    cell.setCellValue(headerList.get(i));
                }
                sheet.autoSizeColumn(i);
            }
            for (int i = 0; i < headerList.size(); i++) {
                int colWidth = sheet.getColumnWidth(i) * 2;
                sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);
            }
        }
        log.debug("Initialize success.");
    }

    private Map<String, CellStyle> createStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        style.setFont(titleFont);
        styles.put("title", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("time", style);

        style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("data", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.LEFT);
        styles.put("data1", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.CENTER);
        styles.put("data2", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        styles.put("data3", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        // style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = wb.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(headerFont);
        styles.put("header", style);

        return styles;
    }

    /**
     * 获取当前工作表
     * @return
     */
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * 添加一行
     * @return 行对象
     */
    public Row addRow() {
        return sheet.createRow(rowNum++);
    }

    /**
     * 添加一行（按指定行数添加）
     * @param rowNumber 指定行数
     * @return
     */
    public Row addRow(int rowNumber) {
        return sheet.createRow(rowNumber);
    }

    /**
     * 获取一行（按指定行数获取）
     * @param rowNumber 指定行数
     * @return
     */
    public Row getRow(int rowNumber) {
        return sheet.getRow(rowNumber);
    }

    /**
     * 添加一个单元格
     * @param row    添加的行
     * @param column 添加列号
     * @param val    添加值
     * @return 单元格对象
     */
    public Cell addCell(Row row, int column, Object val) {
        CellStyle cellStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        Cell cell = row.createCell(column);
        return this.addCell(cell, row, column, val, HorizontalAlignment.GENERAL, Class.class, cellStyle, format);
    }

    /**
     * 添加一个单元格
     * @param row    添加的行
     * @param column 添加列号
     * @param val    添加值
     * @param align  对齐方式（1：靠左；2：居中；3：靠右）
     * @return 单元格对象
     */
    public Cell addCell(Cell cell, Row row, int column, Object val, HorizontalAlignment align, Class<?> fieldType,
        CellStyle cellStyle, DataFormat format) {
        cell = row.createCell(column);
        try {
            if (val == null) {
                cell.setCellValue("");
            } else if (val instanceof String) {
                cell.setCellValue((String) val);
            } else if (val instanceof Integer) {
                cell.setCellValue((Integer) val);
            } else if (val instanceof Long) {
                cell.setCellValue((Long) val);
            } else if (val instanceof Double) {
                cell.setCellValue((Double) val);
            } else if (val instanceof Float) {
                cell.setCellValue((Float) val);
            } else if (val instanceof Date) {
                cell.setCellValue((Date) val);
                cellStyle.setDataFormat(format.getFormat("yyyy-MM-dd HH:mm:ss"));
                cell.setCellStyle(cellStyle);

            } else {
                if (fieldType != Class.class) {
                    cell.setCellValue((String) fieldType.getMethod("setValue", Object.class).invoke(null, val));
                } else {
                    cell.setCellValue((String) Class.forName(this.getClass().getName()
                        .replaceAll(this.getClass().getSimpleName(),
                            "fieldtype." + val.getClass().getSimpleName() + "Type")).getMethod("setValue", Object.class)
                        .invoke(null, val));
                }
            }
        } catch (Exception ex) {
            log.info("Set cell value [" + row.getRowNum() + "," + column + "] error: " + ex.toString());
            cell.setCellValue(val != null ? val.toString() : "");
        }
        // cell.setCellStyle(style);
        return cell;
    }

    /**
     * 添加数据（通过annotation.ExportField添加数据）
     * @return list 数据列表
     */
    public <E> BigDataExportExcel setDataList(List<E> list) {
        if (list != null && !list.isEmpty()) {
            CellStyle cellStyle = wb.createCellStyle();
            DataFormat format = wb.createDataFormat();
            Row row;
            Cell cell = null;
            for (E e : list) {
                int colunm = 0;
                row = this.addRow();
                StringBuilder sb = new StringBuilder();
                for (Object[] os : annotationList) {
                    ExcelField ef = (ExcelField) os[0];
                    Object val = null;
                    // Get entity value
                    try {
                        if (StringUtils.isNotBlank(ef.value())) {
                            val = Reflections.invokeGetter(e, ef.value());
                        } else {
                            if (os[1] instanceof Field) {
                                val = Reflections.invokeGetter(e, ((Field) os[1]).getName());
                            } else if (os[1] instanceof Method) {
                                val = Reflections
                                    .invokeMethod(e, ((Method) os[1]).getName(), new Class[] {}, new Object[] {});
                            }
                        }

                    } catch (Exception ex) {
                        // Failure to ignore
                        log.info(ex.toString());
                        val = "";
                    }
                    this.addCell(cell, row, colunm++, val, ef.align(), Class.class, cellStyle, format);
                    sb.append(val + ", ");
                }
                log.debug("Write success: [" + row.getRowNum() + "] " + sb.toString());
            }

            // 合并单元格,按照第第一列来合并
            mergeRegionExcel(list, 0);
        }
        return this;
    }

    /**
     * 合并单元格
     * @param list
     * @param colNum 根据某列合并单元格 例如：第一列有三个相同,后面的列值合并三列
     * @param <E>
     */
    private <E> void mergeRegionExcel(List<E> list, Integer colNum) {
        ExcelField excelField;
        Object[] object;
        List<int[]> rowFlag = null;
        if (!CollectionUtils.isEmpty(annotationList)) {
            CellStyle style = wb.createCellStyle();// 样式对象
            if (colNum != null) {
                excelField = (ExcelField) annotationList.get(colNum)[0];
                if (excelField.mergedRegion()) {
                    rowFlag = addMergedRegion(sheet, colNum, 1, list.size(), style);
                }
            }
            for (int i = 0; i < annotationList.size(); i++) {

                if (colNum != null && i == colNum) {
                    continue;
                }
                object = annotationList.get(i);
                excelField = (ExcelField) object[0];
                if (excelField.mergedRegion()) {
                    if (rowFlag != null) {
                        for (int[] ints : rowFlag) {
                            addMergedRegion(sheet, i, ints[0], ints[1], style);
                        }
                    } else {
                        addMergedRegion(sheet, i, 1, list.size(), style);
                    }
                }
            }
        }
    }

    private List<int[]> addMergedRegion(Sheet sheet, int cellLine, int startRow, int endRow, CellStyle style) {
        List<int[]> rowFlag = Lists.newLinkedList();
        style.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直
        style.setAlignment(HorizontalAlignment.LEFT);// 水平
        // 获取第一行的数据,以便后面进行比较
        String beforeValue = sheet.getRow(startRow).getCell(cellLine).getStringCellValue();
        String afterValue;
        int count = 0;
        for (int i = startRow + 1; i <= endRow; i++) {
            afterValue = sheet.getRow(i).getCell(cellLine).getStringCellValue();
            if (beforeValue.equals(afterValue)) {
                startRow = i;
                count++;
            } else {
                beforeValue = afterValue;
                // 执行合并单元格
                if (count > 0) {
                    operateSheet(sheet, cellLine, startRow, endRow, count, style, rowFlag);
                    count = 0;
                }
            }
            // 由于上面循环中合并的单元放在有下一次相同单元格的时候做的，所以最后如果几行有相同单元格则要运行下面的合并单元格。
            if (i == endRow && count > 0) {
                operateSheet(sheet, cellLine, startRow, endRow, count, style, rowFlag);
            }
        }
        return rowFlag;
    }

    private void operateSheet(Sheet sheet, int cellLine, int startRow, int endRow, int count, CellStyle style,
        List<int[]> rowFlag) {
        int[] arr = new int[2];
        arr[0] = (startRow - count);
        arr[1] = startRow;
        rowFlag.add(arr);
        sheet.addMergedRegion(new CellRangeAddress(startRow - count, startRow, cellLine, cellLine));
        String cellValueTemp = sheet.getRow(startRow - count).getCell(cellLine).getStringCellValue();
        Row row = sheet.getRow(startRow - count);
        Cell cell = row.createCell(cellLine);
        cell.setCellValue(cellValueTemp); // 跨单元格显示的数据
        cell.setCellStyle(style); // 样式
    }

    /**
     * 输出数据流
     * @param os 输出数据流
     */
    public BigDataExportExcel write(OutputStream os) throws IOException {
        wb.write(os);
        this.dispose();
        return this;
    }

    /**
     * 输出到客户端
     * @param fileName 输出文件名
     */
    public BigDataExportExcel write(HttpServletResponse response, String fileName) throws IOException {
        response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(fileName));
        write(response.getOutputStream());
        return this;
    }

    /**
     * 输出到文件
     */
    public BigDataExportExcel writeFile(String name) throws FileNotFoundException, IOException {
        FileOutputStream os = new FileOutputStream(name);
        this.write(os);
        this.dispose();
        return this;
    }

    /**
     * 清理临时文件
     */
    public BigDataExportExcel dispose() {
        wb.dispose();
        return this;
    }

    /**
     * 移动行
     * @param startRow 开始行
     * @param endRow   结束行
     * @param shiftRow 移动行数（-1：上移一行，1：下移一行）
     * @return
     */
    public BigDataExportExcel shiftRows(int startRow, int endRow, int shiftRow) {
        sheet.shiftRows(startRow, endRow, shiftRow);
        return this;
    }
}
