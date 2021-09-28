package com.zw.platform.basic.constant;

/**
 * 正则表达式常量
 *
 * @author zhangjuan
 */
public interface RegexKey {
    /**
     * 人员编号正则
     */
    String PEOPLE_NUM_REG = "^[a-zA-Z0-9\u4e00-\u9fa5-]{2,20}$";

    /**
     * 15身份识别号正则
     */
    String IDENTITY_REG_15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0-2]\\d)|3[0-1])[\\dX]{3}$";

    /**
     * 18位身份证号正则表达式
     */
    String IDENTITY_REG_18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0-2]\\d)|3[0-1])[\\dX]{4}$";

    /**
     * 国内电话号码正则表达式
     */
    String PHONE = "^[1][3456789]\\d{9}$";

    /**
     * 座机正则表达式
     */
    String TEL_PHONE = "^(\\d{3,4}-?)?\\d{7,9}$";

    /**
     * 车主名字正则
     */
    String VEHICLE_OWNER_REGEX = "^[A-Za-z\\u4e00-\\u9fa5]{1,8}$";

    /**
     * 小数点保留一位，不算"."一共10位
     */
    String DOUBLE_REGEX_10_1 = "^(?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9])$";
}
