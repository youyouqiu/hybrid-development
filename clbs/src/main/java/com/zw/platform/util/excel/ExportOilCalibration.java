package com.zw.platform.util.excel;

import com.zw.platform.util.Encodes;
import com.zw.platform.util.Reflections;
import com.zw.platform.util.excel.annotation.ExcelField;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 导出标定模板
 * <p>Title: ExportOilCalibration.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年11月2日上午9:26:07
 */
public class ExportOilCalibration {

    private static Logger log = LoggerFactory.getLogger(ExportOilCalibration.class);

    /**
     * 工作薄对象
     */
    private HSSFWorkbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    /**
     * 当前行号
     */
    private int rownum;

    public String[] list;
    /**
     * 注解列表（Object[]{ ExcelField, Field/Method }）
     */
    List<Object[]> annotationList = new ArrayList<Object[]>();

    /**
     * 构造函数
     * @param title 表格标题，传“空值”，表示无标题
     * @param cls   实体对象，通过annotation.ExportField获取标题
     */
    public ExportOilCalibration(String title, Class<?> cls) {
        this(title, cls, 1);
    }

    /**
     * 构造函数
     * @param title  表格标题，传“空值”，表示无标题
     * @param cls    实体对象，通过annotation.ExportField获取标题
     * @param type   导出类型（1:导出数据；2：导出模板）
     * @param groups 导入分组
     */
    public ExportOilCalibration(String title, Class<?> cls, int type, int... groups) {
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
        Collections.sort(annotationList, new Comparator<Object[]>() {
            public int compare(Object[] o1, Object[] o2) {
                return new Integer(((ExcelField) o1[0]).sort()).compareTo(new Integer(((ExcelField) o2[0]).sort()));
            }

            ;
        });
        // Initialize
        List<String> headerList = new ArrayList<String>();
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
        initialize(title, headerList);
    }

    /**
     * 构造函数
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    public ExportOilCalibration(String title, List<String> headerList) {
        initialize(title, headerList);
    }

    /**
     * 初始化函数
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    private void initialize(String title, List<String> headerList) {
        this.wb = new HSSFWorkbook();
        this.sheet = wb.createSheet("Export");
        this.styles = createStyles(wb);
        // Create title
        if (StringUtils.isNotBlank(title)) {
            Row titleRow = sheet.createRow(rownum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(),
                headerList.size() - 1));
        }
        // Create header
        if (headerList != null && headerList.size() > 0) {
            //			throw new RuntimeException("headerList not null!");
            Row headerRow = sheet.createRow(rownum++);
            headerRow.setHeightInPoints(16);
            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellStyle(styles.get("header"));
                String[] ss = StringUtils.split(headerList.get(i), "**", 2);
                if (ss.length == 2) {
                    cell.setCellValue(ss[0]);
                    Comment comment = this.sheet.createDrawingPatriarch()
                        .createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
                    comment.setString(new HSSFRichTextString(ss[1]));
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
     * @param headerList   表头列表
     * @param requiredList 必填列
     */
    public ExportOilCalibration(String title, Class<?> cls, int type, String time, String group, int... groups) {
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
        Collections.sort(annotationList, new Comparator<Object[]>() {
            public int compare(Object[] o1, Object[] o2) {
                return new Integer(((ExcelField) o1[0]).sort()).compareTo(new Integer(((ExcelField) o2[0]).sort()));
            }

            ;
        });
        // Initialize
        List<String> headerList = new ArrayList<String>();
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
        initialize(title, headerList, time, group);
    }

    /**
     * 初始化函数(wangjianyu)
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    private void initialize(String title, List<String> headerList, String time, String group) {
        this.wb = new HSSFWorkbook();
        this.sheet = wb.createSheet("Export");
        this.styles = createStyles(wb);
        // Create title
        if (StringUtils.isNotBlank(title)) {
            Row titleRow = sheet.createRow(rownum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);

            Row rtime = sheet.createRow(rownum++);
            rtime.setHeightInPoints(20);
            Cell ctime = rtime.createCell(0);
            ctime.setCellStyle(styles.get("time"));
            ctime.setCellValue(time);

            Row rgroup = sheet.createRow(rownum++);
            rgroup.setHeightInPoints(20);
            Cell cgroup = rgroup.createCell(0);
            /*cgroup.setCellStyle(styles.get("time"));*/
            cgroup.setCellValue(group);

            sheet.addMergedRegion(new CellRangeAddress(rgroup.getRowNum(), rgroup.getRowNum(), rgroup.getRowNum() - 2,
                headerList.size() - 1));//合并行
            sheet.addMergedRegion(new CellRangeAddress(rtime.getRowNum(), rtime.getRowNum(), rtime.getRowNum() - 1,
                headerList.size() - 1));//合并行
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(),
                headerList.size() - 1));
        }
        // Create header
        if (headerList != null && headerList.size() > 0) {
            Row headerRow = sheet.createRow(rownum++);
            headerRow.setHeightInPoints(16);
            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellStyle(styles.get("header"));
                String[] ss = StringUtils.split(headerList.get(i), "**", 2);
                if (ss.length == 2) {
                    cell.setCellValue(ss[0]);
                    Comment comment = this.sheet.createDrawingPatriarch()
                        .createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
                    comment.setString(new HSSFRichTextString(ss[1]));
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
     * 增加结尾标签(wangjianyu)
     * @param title      表格标题，传“空值”，表示无标题
     * @param headerList 表头列表
     */
    public <E> ExportOilCalibration foot(String foot1, String foot2) {

        Row rtime = sheet.createRow(rownum++);
        rtime.setHeightInPoints(20);
        Cell ctime = rtime.createCell(0);
        /*ctime.setCellStyle(styles.get("time"));*/
        ctime.setCellValue(foot1);

        Row rgroup = sheet.createRow(rownum++);
        rgroup.setHeightInPoints(20);
        Cell cgroup = rgroup.createCell(0);
        /*cgroup.setCellStyle(styles.get("time"));*/
        cgroup.setCellValue(foot2);

        log.debug("Initialize success.");
        return this;
    }

    /**
     * <p>Title: </p>
     * @param headerList   表头
     * @param requiredList 必填字段
     * @param selectMap    下拉框的列
     */
    public ExportOilCalibration(List<String> headerList, List<String> requiredList, Map<String, String[]> selectMap) {
        initializeHead(headerList, requiredList, selectMap);
    }

    /**
     * 初始化函数
     * @param headerList   表头列表
     * @param requiredList 必填列
     * @param selectMap    下拉框的列
     */
    private void initializeHead(List<String> headerList, List<String> requiredList, Map<String, String[]> selectMap) {
        this.wb = new HSSFWorkbook();
        this.sheet = wb.createSheet("Export");
        this.styles = createStyles(wb);
        // Create header
        if (headerList != null && headerList.size() > 0) {

            // 下拉列
            List<String> selectColumns = new ArrayList<String>();
            if (selectMap != null) {
                selectColumns = new ArrayList(selectMap.keySet());
            }
            Row headerRow = sheet.createRow(rownum++);
            headerRow.setHeightInPoints(16);
            for (int i = 0; i < headerList.size(); i++) {
                Cell cell = headerRow.createCell(i);
                // 若为必填字段
                String headIndex = headerList.get(i);
                if (requiredList.contains(headIndex)) {
                    headerList.set(i, headIndex + "*");
                    CellStyle styleReq = wb.createCellStyle();
                    //生成一个字体
                    Font font = wb.createFont();
                    font.setColor(Font.COLOR_RED);//HSSFColor.VIOLET.index //字体颜色
                    //把字体应用到当前的样式
                    styleReq.setFont(font);
                    cell.setCellStyle(styleReq);
                }
                // 下拉框
                if (selectMap != null && selectColumns.contains(headIndex)) {
                    String[] selectList = selectMap.get(headIndex);
                    CellRangeAddressList regions = new CellRangeAddressList(1, 1, i, i);
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(selectList);
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    try {
                        this.sheet.addValidationData(dataValidation);
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                }
                String[] ss = StringUtils.split(headerList.get(i), "**", 2);
                if (ss.length == 2) {
                    cell.setCellValue(ss[0]);
                    Comment comment = this.sheet.createDrawingPatriarch()
                        .createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
                    comment.setString(new HSSFRichTextString(ss[1]));
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
     * 创建表格样式
     * @param wb 工作薄对象
     * @return 样式列表
     */
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
        //		style.setWrapText(true);
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
     * 添加一行
     * @return 行对象
     */
    public Row addRow() {
        return sheet.createRow(rownum++);
    }

    /**
     * 添加一个单元格
     * @param row    添加的行
     * @param column 添加列号
     * @param val    添加值
     * @return 单元格对象
     */
    public Cell addCell(Row row, int column, Object val) {
        return this.addCell(row, column, val, HorizontalAlignment.GENERAL, Class.class);
    }

    /**
     * 添加一个单元格
     * @param row    添加的行
     * @param column 添加列号
     * @param val    添加值
     * @param align  对齐方式（1：靠左；2：居中；3：靠右）
     * @return 单元格对象
     */
    public Cell addCell(Row row, int column, Object val, HorizontalAlignment align, Class<?> fieldType) {
        Cell cell = row.createCell(column);
        //		CellStyle style = styles.get("data"+(align>=1&&align<=3?align:""));
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
                CellStyle cellStyle = wb.createCellStyle();
                DataFormat format = wb.createDataFormat();
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
            cell.setCellValue(val.toString());
        }
        //cell.setCellStyle(style);
        return cell;
    }

    /**
     * 添加数据（通过annotation.ExportField添加数据）
     * @return list 数据列表
     */
    public <E> ExportOilCalibration setDataList(List<E> list) {
        for (E e : list) {
            int colunm = 0;
            Row row = this.addRow();
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
                this.addCell(row, colunm++, val, ef.align(), ef.fieldType());
                sb.append(val + ", ");
            }
            log.debug("Write success: [" + row.getRowNum() + "] " + sb.toString());
        }
        return this;
    }

    /**
     * 输出数据流
     * @param os 输出数据流
     */
    public ExportOilCalibration write(OutputStream os) throws IOException {
        wb.write(os);
        return this;
    }

    /**
     * 输出到客户端
     * @param fileName 输出文件名
     */
    public ExportOilCalibration write(HttpServletResponse response, String fileName) throws IOException {
        response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + Encodes.urlEncode(fileName));
        write(response.getOutputStream());
        return this;
    }

    /**
     * 输出到文件
     */
    public ExportOilCalibration writeFile(String name) throws FileNotFoundException, IOException {
        FileOutputStream os = new FileOutputStream(name);
        this.write(os);
        return this;
    }

    /**
     * 清理临时文件
     */
    public ExportOilCalibration dispose() {
        return this;
    }

}
