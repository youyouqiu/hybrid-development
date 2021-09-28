package com.zw.platform.dto.driverMiscern;

import java.util.regex.Pattern;

/**
 * @Description: 驾驶员识别管理相关常量类
 * @Author Tianzhangxu
 * @Date 2020/9/29 13:36
 */
public final class DeviceDriverConstant {
    /**
     *下发和查询状态 0:等待下发; 1:下发失败; 2:下发中; 3:下发成功
     */
    public static final int ISSUE_STATUS_WAIT = 0;

    public static final int ISSUE_STATUS_FAIL = 1;

    public static final int ISSUE_STATUS_ISSUING = 2;

    public static final int ISSUE_STATUS_SUCCESS = 3;

    /**
     * 下发结果 0:终端已应答 1:终端未应答 2:终端离线
     */
    public static final int ISSUE_RESULT_ACK = 0;

    public static final int ISSUE_RESULT_NOT_ACK = 1;

    public static final int ISSUE_RESULT_DEVICE_OFFLINE = 2;

    /**
     * 指令类型 0：查询  1：下发
     */
    public static final int TYPE_QUERY = 0;

    public static final int TYPE_ISSUE = 1;

    /**
     * 人脸图片地址协议HTTP
     */
    public static final int URL_TYPE_HTTP = 1;

    /**
     * 图片来源 1：第三方图片
     */
    public static final int PIC_SOURCE_TYPE = 1;

    /**
     * 查询指令下发推送地址
     */
    public static final String DRIVER_DISCERN_QUERY_ACK_URL = "/topic/query/batch";

    /**
     * 下发指令下发推送地址
     */
    public static final String DRIVER_DISCERN_ISSUE_ACK_URL = "/topic/issue/batch";

    /**
     * 判断终端人脸ID格式
     * PS：由于终端人脸ID长度限制32位，平台驾驶员ID为UUID(36位),所以对“-”进行了处理,下发下去的人脸ID为去横杠的
     */
    public static final Pattern FACE_ID_PATTERN = Pattern.compile("[0-9a-z]+");

    /**
     * 处理后的UUID长度
     */
    public static final int UUID_SERIALIZED_LENGTH = 32;
}
