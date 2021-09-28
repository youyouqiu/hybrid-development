package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 上级平台消息处理实体
 */
@Data
public class SuperPlatformMsg implements Serializable {

    private String id = UUID.randomUUID().toString();

    /**
     * 上级平台id
     */
    private String platformId;

    @ExcelField(title = "企业")
    private String groupName;

    /**
     * 消息类型，0：查岗，1：督办
     */
    private Integer type;

    /**
     * 消息类型，0：查岗，1：督办
     */
    @ExcelField(title = "业务类型")
    private String typeStr;

    /**
     * 查岗/督办时间
     */
    @ExcelField(title = "时间")
    private String timeStr;

    /**
     * 查岗/督办时间
     */
    private Date time;

    /**
     * 查岗/督办消息
     */
    @ExcelField(title = "内容")
    private String msg;

    /**
     * 处理结果 0：未处理，1：已处理，2：已过期
     */
    @ExcelField(title = "处理状态")
    private String resultStr;

    /**
     * 处理结果 0：未处理，1：已处理，2：已过期
     */
    private Integer result = 0;

    /**
     * 应答时间
     */
    private Date ackTime;

    /**
     * 应答时间
     */
    @ExcelField(title = "应答时间")
    private String ackTimeStr;

    /**
     * 处理人
     */
    @ExcelField(title = "应答人")
    private String dealer;

    /**
     * 应答内容
     */
    @ExcelField(title = "应答内容")
    private String ackContent;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 组织id
     */
    private String groupId;

    /**
     * 应答时限
     */
    private Long answerTime;

    /**
     * 上级平台巡检请求消息源报文序列号
     */
    private Integer sourceMsgSn;
}
