package com.zw.app.domain.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * io传感器绑定信息（终端+外接）
 * @author hujun
 * @date 2018/8/22 19:18
 */
@Data
public class IoSensorConfigInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer highSignalType;//高电平状态类型
    private Integer lowSignalType;//低电平状态类型
    private String name;//传感器名称
    private String stateOne;//状态1
    private String stateTwo;//状态2
    private Integer ioType;//io传感器类型
    private Integer ioSite;//io位置
}
