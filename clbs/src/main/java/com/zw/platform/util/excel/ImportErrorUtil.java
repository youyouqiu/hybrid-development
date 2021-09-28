package com.zw.platform.util.excel;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.imports.ImportErrorData;
import com.zw.platform.util.imports.ProgressBar;
import com.zw.platform.util.imports.ZwImportException;
import com.zw.platform.util.imports.lock.ImportModule;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author denghuabing
 * @version V1.0
 * @description: 导入的错误信息生成excel工具类
 * @date 2020/9/7
 **/
@Slf4j
public class ImportErrorUtil {

    /**
     * 生成错误excels
     * @param module   模块
     * @param fileName 文件名
     * @param title    excel头
     * @return
     */
    public static <T> void generateErrorExcel(ImportModule module, String fileName, String title,
        HttpServletResponse response) throws Exception {
        RedisKey redisKey =
            HistoryRedisKeyEnum.IMPORT_ERROR_USER_MODULE.of(SystemHelper.getCurrentUserId(), module.name());
        if (!RedisHelper.isContainsKey(redisKey)) {
            throw new ZwImportException("错误信息已超时！");
        }
        List<String> list = RedisHelper.getList(redisKey, 0, 1);
        Class<?> clazz = Class.forName(list.get(0));
        List<T> data = RedisHelper.getListObj(redisKey, 0, -1);
        ExportExcelUtil.setResponseHead(response, fileName);
        ExportExcel export = new ExportExcel(title, clazz, 1);
        export.setDataList(data);
        // 输出导文件
        try (OutputStream out = response.getOutputStream()) {
            export.write(out);
        }
    }

    public static <T extends ImportErrorData> void putDataToRedis(List<T> data, ImportModule module) {
        RedisKey redisKey =
            HistoryRedisKeyEnum.IMPORT_ERROR_USER_MODULE.of(SystemHelper.getCurrentUserId(), module.name());
        //删除历史数据
        RedisHelper.delete(redisKey);

        //顺序插入数据,并在头部插入类名
        RedisHelper.addObjectToList(redisKey, data, 30 * 60);
        // 推送进度条变更, 这里有个极端情况， 如果导入的1w条数据都有错误, 那么会推送大量的数据给前端, 因此, 感觉这里不传错误信息会好一点
        ProgressBar.pushProgress(module, Collections.singletonList("导入有误."));
    }
}
