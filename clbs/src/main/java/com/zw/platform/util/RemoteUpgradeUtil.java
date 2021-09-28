package com.zw.platform.util;

/**
 * 远程升级工具类
 *
 * @author hujun
 * @date 2019/1/23 16:43
 */
public class RemoteUpgradeUtil {

    /**
     * 平台->终端传输状态: 传输中
     */
    public static final int PLATFORM_STATUS_UNDERWAY = 0;

    /**
     * 传输完成
     */
    public static final int PLATFORM_STATUS_FINISH = 1;

    /**
     * 设备已经在升级中
     */
    public static final int PLATFORM_STATUS_ALREADY_UPGRADE = 2;

    /**
     * 传输失败
     */
    public static final int PLATFORM_STATUS_FAILED = 3;

    /**
     * 终端离线
     */
    public static final int PLATFORM_STATUS_DEVICE_OFFLINE = 4;


    /* 终端->外设传输状态 */
    public static final int F3_STATUS_UNDERWAY = 0;
    public static final int F3_STATUS_FINISH = 1;
    public static final int F3_STATUS_FAILED = 2;
    /**
     * 终端存储区擦出失败
     */
    public static final int TERMINAL_STORADE_ERASE_FAILED = 3;

    /**
     * 数据校验失败
     */
    public static final int DATA_VALIDATION_FAILED = 4;

    /**
     * 下发升级指令失败
     */
    public static final int SEND_UPGRADE_COMMAND_FAILED = 5;

    /**
     * 外设升级超时
     */
    public static final int PERIPHERAL_UPGRADE_TIME_OUT = 6;
}
