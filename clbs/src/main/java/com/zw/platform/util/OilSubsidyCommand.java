package com.zw.platform.util;

/**
 * @author wanxing
 * @Title: 油补指令类
 * @date 2020/10/12 15:48
 */
public interface OilSubsidyCommand {

    /**
     * 主链路静态信息交换消息
     */
    int UP_BASE_MSG = 0x1300;

    /**
     * 公交线路 GIS 信息消息
     */
    int UP_BASE_MSG_LINE_INFO_REQ = 0x1301;
    /**
     * 公交线路 GIS 信息消息
     */
    int UP_BASE_MSG_GIS_INFO_REQ = 0x1302;


    /**
     * 自动上报车辆运营公里
     */
    int UP_CXG_MSG_TAKE_OPERATEMILE_ACK = 0x120B;


    int T809_UP_EXG_MSG = 0x1200;
}
