package com.zw.platform.util;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


public class DocExportUtil {
    private static final Configuration configuration;

    private static Template template;

    static {
        configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");
    }

    public static void initTemplate(String templateDir, String templateName) throws IOException {
        configuration.setDirectoryForTemplateLoading(new File(templateDir));
        template = configuration.getTemplate(templateName); // 获取模板文件
    }

    public static void initTemplateOnWeb(String templatePath, String templateName, ServletContext context)
        throws IOException {
        configuration.setServletContextForTemplateLoading(context, templatePath);
        template = configuration.getTemplate(templateName); // 获取模板文件
    }

    public static void exportDoc(String exportPath, Map<String, Object> dataMap) throws TemplateException, IOException {
        File outFile = new File(exportPath + "/outFile" + Math.random() * 10000 + ".doc"); // 导出文件
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        template.process(dataMap, out); // 将填充数据填入模板文件并输出到目标文件
    }

    public static void exportDocWithName(String exportPath, Map<String, Object> dataMap, String flieName)
        throws TemplateException, IOException {
        // 导出文件
        File outFile = new File(exportPath + File.separator + flieName + ".doc");
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        template.process(dataMap, out); // 将填充数据填入模板文件并输出到目标文件
        out.close();
    }

    public static void exportDocOnWeb(Map<String, Object> dataMap, Writer writer)
        throws TemplateException, IOException {
        template.process(dataMap, writer); // 将填充数据填入模板文件并输出到目标文件
        writer.close();
    }

    public static void exportDocDefault(Map<String, Object> dataMap, Writer writer, ServletContext servletContext)
            throws Exception {
        initTemplateOnWeb("/file/ftl", "demo20033.ftl", servletContext);
        exportDocOnWeb(dataMap, writer);
    }

}
