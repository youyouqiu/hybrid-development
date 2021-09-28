package com.zw.platform.util.excel;


import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;


/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式）
 */
public class ImportNewExcel extends ImportExcel {

    /**
     * 构造函数
     * @param file 导入文件对象
     * @param headerNum 标题行号，数据行号=标题行号+1
     * @param sheetIndex 工作表编号
     * @throws InvalidFormatException
     * @throws IOException
     */
    public ImportNewExcel(MultipartFile multipartFile, int headerNum, int sheetIndex)
        throws InvalidFormatException, IOException {
        super(multipartFile, headerNum, sheetIndex);
    }

    @Override
    protected DecimalFormat getDecimalFormat() {
        return new DecimalFormat("#.##");
    }

}
