package com.zw.platform.util.excel;

import com.zw.platform.util.response.ResponseUtil;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class ExportExcelUtil {

    public static void setResponseHead(HttpServletResponse res, String fileName) {
        String exportFileName = fileName + ".xlsx";
        ResponseUtil.setExcelResponse(res, exportFileName);
    }

    public static boolean export(ExportExcelParam exportExcelParam) {
        ExportExcel export = getExportExcel(exportExcelParam);
        return write(export, exportExcelParam.getOut());

    }

    public static boolean bigDataExport(BigDataExportExcelParam bigDataExportExcelParam) {
        BigDataExportExcel bigDataExportExcel = getBigDataExportExcel(bigDataExportExcelParam);
        return bigDatawrite(bigDataExportExcel, bigDataExportExcelParam.getOut());
    }

    private static BigDataExportExcel getBigDataExportExcel(BigDataExportExcelParam bigDataExportExcelParam) {
        BigDataExportExcel bigDataExportExcel =
            new BigDataExportExcel(bigDataExportExcelParam.getWindowSize(), bigDataExportExcelParam.getTitle(),
                bigDataExportExcelParam.getEntityClass(), bigDataExportExcelParam.getType(),
                bigDataExportExcelParam.getGroup());
        bigDataExportExcel.setDataList(bigDataExportExcelParam.getExportData());
        return bigDataExportExcel;
    }

    private static ExportExcel getExportExcel(ExportExcelParam exportExcelParam) {
        ExportExcel export =
            new ExportExcel(exportExcelParam.getTitle(), exportExcelParam.getEntityClass(), exportExcelParam.getType(),
                exportExcelParam.getGroup());
        export.setDataList(exportExcelParam.getExportData());
        return export;
    }

    private static boolean write(ExportExcel exportExcel, OutputStream out) {

        try {
            exportExcel.write(out);
            out.flush();
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(out);
        }
        return true;
    }

    private static boolean bigDatawrite(BigDataExportExcel bigDataExportExcel, OutputStream out) {

        try {
            bigDataExportExcel.write(out);
            out.flush();
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(out);
        }
        return true;
    }

    /**
     * ???????????????????????????, ??????????????????(customColumnList)????????????!!!
     * @param exportExcelParam exportExcelParam
     * @return boolean
     */
    public static boolean exportCustomData(ExportExcelParam exportExcelParam) {
        ExportExcel export = getExportCustomExcel(exportExcelParam);
        return write(export, exportExcelParam.getOut());
    }

    private static ExportExcel getExportCustomExcel(ExportExcelParam exportExcelParam) {
        ExportExcel export =
            new ExportExcel(exportExcelParam.getTitle(), exportExcelParam.getEntityClass(), exportExcelParam.getType(),
                exportExcelParam.getCustomColumnList(), exportExcelParam.getGroup());
        export.setDataList(exportExcelParam.getExportData());
        return export;
    }

    /**
     * ???????????????????????????
     * @param exportExcelParam
     * @return
     */
    public static boolean exportTimeZoneData(ExportExcelParam exportExcelParam) {
        TimeZoneExportExcel export = getTimeZoneExportExcel(exportExcelParam);
        return timeZoneDataWrite(export, exportExcelParam.getOut());

    }

    private static boolean timeZoneDataWrite(TimeZoneExportExcel exportExcel, OutputStream out) {

        try {
            exportExcel.write(out);
            out.flush();
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(out);
        }
        return true;
    }

    private static TimeZoneExportExcel getTimeZoneExportExcel(ExportExcelParam exportExcelParam) {
        TimeZoneExportExcel export =
            new TimeZoneExportExcel(exportExcelParam.getTitle(), exportExcelParam.getEntityClass(),
                exportExcelParam.getType(), exportExcelParam.getGroup());
        export.setDataList(exportExcelParam.getExportData());
        return export;
    }

    /**
     * ??????excel?????????
     *
     * @param headList     ????????????
     * @param requiredList ????????????
     * @param selectMap    ????????????????????????
     * @param exportList   ????????????
     * @param response     http??????
     * @throws Exception Exception
     */
    public static void writeTemplateToFile(List<String> headList, List<String> requiredList,
                                           Map<String, String[]> selectMap, List<Object> exportList,
                                           HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        // ????????????????????????????????????
        export.write(out);
        out.close();
    }
}
