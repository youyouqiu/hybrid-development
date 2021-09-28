package com.zw.platform.util.excel;

import com.zw.platform.commons.ParallelWorker;
import com.zw.platform.util.common.ZipUtility;
import com.zw.platform.util.ffmpeg.FileUtils;
import com.zw.platform.util.multimedia.UploadUtil;
import com.zw.platform.util.response.ResponseUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 @Author lijie
 @Date 2019/9/9 15:48
 @Description 根据excel模板导出excel
 @version 1.0
 **/
@Component
public class TemplateExportExcel {

    private static Logger log = LogManager.getLogger(TemplateExportExcel.class);

    public static final String OFF_LINE_REPORT = "/file/generalCargo/OffLineReport.xls";

    public static final String OIL_SUBSIDIES_LOCATION_INFORMATION_STATISTICAL = "/file/oilSubsidiesReport/定位信息统计.xls";

    @Autowired
    ServletContext servletContext;

    @Resource
    private HttpServletRequest request;

    public void templateExportExcel(String path, HttpServletResponse res, Map<String, Object> data, String fileName) {
        InputStream is = null;
        OutputStream os = null;
        try {
            ExportExcelUtil.setResponseHead(res, fileName);
            os = res.getOutputStream();
            is = servletContext.getResourceAsStream(path);
            Context context = new Context();
            Set<Map.Entry<String, Object>> d = data.entrySet();
            for (Map.Entry entry : d) {
                context.putVar(entry.getKey().toString(), entry.getValue());
            }
            JxlsHelper.getInstance().processTemplate(is, os, context);
        } catch (Exception e) {
            log.error("导出" + fileName + "异常！", e);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

    public void templateExportExcels(String path, HttpServletResponse res,
        List<Map<String, Object>> datas, String zipFileName) {
        InputStream is = null;
        File file = null;
        zipFileName = zipFileName + ".zip";
        long time = System.currentTimeMillis();
        String realPath = request.getSession().getServletContext().getRealPath("/");
        // 临时文件保存路径
        String filePath = realPath + "/templateExportExcels" + "_" + time;
        try {
            if (datas == null || datas.size() == 0) {
                FileUtils.writeFile(is, res.getOutputStream());
                return;
            }
            file = new File(filePath);
            file.deleteOnExit();
            file.mkdir();
            ParallelWorker.invoke(datas, 100, list -> exportDataTask(path, list, filePath));
            // 文件写入完毕,进行数据压缩
            boolean packedResult = packedDataFile(file, zipFileName + time);
            if (packedResult) {
                File zipFile = new File(realPath + zipFileName + time);
                is = new FileInputStream(zipFile);
                // 清空文件夹
                // 将压缩文件放入response
                ResponseUtil.setZipResponse(res, zipFileName);
                FileUtils.writeFile(is, res.getOutputStream());
                UploadUtil.delete(realPath + zipFileName + time);
            }
        } catch (Exception e) {
            log.error("导出" + zipFileName + "异常！", e);
        } finally {
            IOUtils.closeQuietly(is);
            // 删除临时文件夹
            if (file != null && file.exists()) {
                UploadUtil.delete(filePath);
            }
        }
    }

    private void exportDataTask(String path, List<Map<String, Object>> datas, String filePath) {
        for (Map<String, Object> data : datas) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = servletContext.getResourceAsStream(path);
                String fileName = data.get("templateSingleFileName") == null ? "导出excel文件" :
                    data.get("templateSingleFileName").toString();
                outputStream = new FileOutputStream(filePath + "/" + fileName + ".xls");
                Context context = new Context();
                Set<Map.Entry<String, Object>> d = data.entrySet();
                for (Map.Entry entry : d) {
                    context.putVar(entry.getKey().toString(), entry.getValue());
                }
                JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            } catch (Exception e) {
                log.error("批量导出数据异常", e);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    /**
     * 文件打包
     *
     * @param directory
     * @return
     */
    private boolean packedDataFile(File directory, String zipName) {
        boolean packSuccess = false;
        // 判断文件夹是否为空
        boolean isEmptyDir = true;

        String[] files = directory.list();
        if (files != null && files.length > 0) {
            isEmptyDir = false;
        }

        try {
            // 文件夹不为空则将文件夹压缩
            if (!isEmptyDir) {
                String srcPath = directory.getAbsolutePath();
                String zipPath = directory.getParentFile().getAbsolutePath();
                packSuccess = ZipUtility.zip(srcPath, zipPath, zipName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("压缩导出的excel失败", e);
        }
        return packSuccess;
    }

}
