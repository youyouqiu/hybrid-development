package com.zw.platform.domain.vas.alram;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 */
@Data
public class IoVehicleConfigInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车id
     */
    private String vehicleId;
    /**
     * 功能名称
     */
    private String name;

    /**
     * io位置
     */
    private Integer ioSite;

    /**
     * io类型（1：终端io；2：io采集1(0x91)；3:io采集2(0x92)）
     */
    private Integer ioType;

    /**
     * 高电平状态类型
     */
    private Integer highSignalType;
    /**
     * 低电平状态类型
     */
    private Integer lowSignalType;
    /**
     * 状态1
     */
    private String stateOne;
    /**
     * 状态2
     */
    private String stateTwo;

    /**
     * 功能id
     */
    private String identify;
    /**
     * 标识
     */
    private Integer pos;

    public void assemblePos() {
        Integer pos = null;
        if (this.ioType == 1) {
            pos = 14000;
        } else if (this.ioType == 2) {
            pos = 14100;
        } else if (this.ioType == 3) {
            pos = 14200;
        }
        if (pos != null) {
            this.pos = pos + this.ioSite;
        }
    }
}
