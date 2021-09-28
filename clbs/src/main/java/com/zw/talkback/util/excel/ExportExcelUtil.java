package com.zw.talkback.util.excel;

import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.response.ResponseUtil;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public class ExportExcelUtil {

    public static void setResponseHead(HttpServletResponse res, String fileName) throws UnsupportedEncodingException {
        String exportFileName = fileName + ".xls";
        ResponseUtil.setExcelResponse(res, exportFileName);
    }

    public static boolean export(ExportExcelParam exportExcelParam) {
        ExportExcel export = getExportExcel(exportExcelParam);
        return write(export, exportExcelParam.getOut());

    }

    private static ExportExcel getExportExcel(ExportExcelParam exportExcelParam) {
        ExportExcel export = new ExportExcel(exportExcelParam.getTitle(), exportExcelParam.getEntityClass(),
            exportExcelParam.getType(), exportExcelParam.getGroup());
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
}
