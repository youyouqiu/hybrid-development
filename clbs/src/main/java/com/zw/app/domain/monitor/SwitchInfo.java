package com.zw.app.domain.monitor;

import lombok.Data;


@Data
public class SwitchInfo {
    /**
     * IO名称
     */
    private String name;

    /**
     * IO高电平状态类型
     */
    private String highSignalType;

    /**
     * IO低电平状态类型
     */
    private String lowSignalType;

    /**
     * IO类型(1 终端io  2 io1  3 io2)
     */
    private Integer ioType;

    /**
     * IO位置
     */
    private Integer ioSite;

    /**
     * 状态1
     */
    private String stateOne;

    /**
     * 状态2
     */
    private String stateTwo;
}
