package com.zw.app.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

/**
 * App参数校验工具类
 * @author hujun
 * @date 2018/9/18 9:54
 */
public class AppParamCheckUtil {

    private static final boolean SUCCESS = true;

    private static final boolean ERROR = false;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 校验空字符串及64位
     * @param param
     * @return
     */
    public static boolean check64String(String param) {
        if (StringUtils.isNotBlank(param) && param.length() <= 64) {
            return SUCCESS;
        }
        return ERROR;
    }

    /**
     * 校验时间格式
     * @param param
     * @return
     */
    public static boolean checkDate(String param, int type) {
        if (StringUtils.isNotBlank(param)) {
            try {
                switch (type) {
                    case 1:// 校验完整日期
                        DateUtils.parseDate(param, DATE_TIME_FORMAT);
                        break;
                    case 2:// 校验年月日日期
                        DateUtils.parseDate(param, DATE_FORMAT);
                        break;
                    default:
                        return ERROR;
                }
                return SUCCESS;
            } catch (Exception e) {
                return ERROR;
            }
        }
        return ERROR;
    }

    /**
     * 校验状态类型 0：全部，1：在线，2：离线
     * @param type
     * @return
     */
    public static boolean checkType(Integer type) {
        if (type != null && (type == 0 || type == 1 || type == 2)) {
            return SUCCESS;
        }
        return ERROR;
    }

    //endTime  "yyyy-MM-dd HH:mm:ss"  判断是否包含当天
    public static Boolean nowDayFlag(String endTime) {
        Date date = new Date();
        String nowDay = DateFormatUtils.format(date, DATE_FORMAT);
        return (endTime.substring(0, 10)).equals(nowDay);
    }
}
