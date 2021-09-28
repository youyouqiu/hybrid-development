package com.zw.platform.domain.param;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * 远程升级
 * @author zhouzongbo on 2019/1/30 16:14
 */
@Data
public class SensorRemoteUpgrade implements T808MsgBody {

    private static final long serialVersionUID = 6685593195707799037L;

    /**
     * 代表擦除终端升级数据存储区
     */
    public static final int ERASE_TERMINAL_UPGRADE_DATA = 0x01;
    /**
     * 代表下发升级数据命令
     */
    public static final int ISSUE_UPGRADE_DATA_COMMAND = 0x02;
    /**
     * 0x03：代表下发总数据校验命令
     */
    public static final int TOTAL_DATA_VALIDATION_COMMAND = 0x03;
    /**
     * 0x04：开始外设升级命令
     */
    public static final int START_PERIPHERAL_UPGRADE_COMMAND = 0x04;
    /**
     * 结束升级文件下发
     */
    public static final int END_UPGRADE_FILE_ISSUE = 0x05;


    /**
     * 外设ID
     */
    private Integer id;

    /**
     * 消息长度
     */
    private Integer len;

    /**
     * 0x01：代表擦除终端升级数据存储区；
     * 0x02：代表下发升级数据命令；见 表 19；
     * 0x03：代表下发总数据校验命令；见 表 20 平台下发；
     * 0x04：开始外设升级命令；见 表 21；
     * 0x05：结束升级文件下发；同表 18；在“开始升级指令”之前发送。终端收到后，表明平台中断本次升级文件下发。
     */
    private Integer control;

    private String deviceType;
}
