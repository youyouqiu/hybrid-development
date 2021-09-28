package com.zw.platform.util.response;

import com.zw.platform.util.StringUtil;
import com.zw.platform.util.excel.ExportExcel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class ResponseUtil {
    private static final Logger log = LogManager.getLogger(ResponseUtil.class);

    public static void setCharacterEncoding(HttpServletResponse response, String charset) {
        response.setCharacterEncoding(charset);
    }

    public static void setAttachmentHeader(HttpServletResponse response, String fileName) {
        fileName = StringUtil.encodingDownloadFileName(fileName);
        setHeader(response, "Content-disposition", "attachment;filename=" + fileName);
    }

    public static void setHeader(HttpServletResponse response, String name, String value) {
        response.setHeader(name, value);
    }

    public static void setContentType(HttpServletResponse response, String contentType) {
        response.setContentType(contentType);
    }

    public static void setDownloadpResponse(HttpServletResponse response, String fileName, ContentType contentType) {
        setCharacterEncoding(response, "UTF-8");
        setAttachmentHeader(response, fileName);
        setContentType(response, contentType + ";charset=UTF-8");
    }

    public static void setWordResponse(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.WORD);
    }

    public static void setExcelResponse(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.EXCEL);
    }

    public static void setZipResponse(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.ZIP);
    }

    public static void setJpegResponse(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.JPEG);
    }

    public static void setMp3Response(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.MP3);
    }

    public static void setMp4Response(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.MP4);
    }

    public static void setTifResponse(HttpServletResponse response, String fileName) {
        setDownloadpResponse(response, fileName, ContentType.TIF);
    }

    public static void writeFile(HttpServletResponse response, ExportExcel exportExcel) {
        try (OutputStream out = response.getOutputStream()) {
            exportExcel.write(out);
        } catch (IOException e) {
            log.error("response写文件异常");
        }
    }

}
