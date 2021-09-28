package com.zw.platform.dto.platformInspection;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 809查岗/督办消息DTO
 *
 * @author Zhang Yanhui
 * @since 2020/9/29 11:12
 */

@Data
public class Zw809MessageDTO implements Serializable {
    private static final long serialVersionUID = 496797402883158682L;

    private String id;

    /**
     * 转发平台id
     */
    private String platformId;

    /**
     * 接收消息的企业id
     */
    private String groupId;

    @ExcelField(title = "企业")
    private String groupName;

    /**
     * 监控对象名称
     */
    @ExcelField(title = "监控对象")
    private String brand;

    @ExcelField(title = "上级平台名称")
    private String platformName;

    /**
     * 消息类型，0：标准809查岗，1：标准809督办 2:西藏809查岗 3:西藏809督办 4:监控平台巡检人员
     */
    private Integer type;
    @ExcelField(title = "业务类型")
    private String typeStr;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 报警类型 809
     */
    private String warnType;

    /**
     * 报警类型 808
     */
    private String alarmType;

    /**
     * 报警/查岗时间
     */
    private Long warnTime;

    /**
     * 报警来源
     */
    private Integer warnSrc;

    /**
     * 督办等级
     */
    private Integer supervisionLevel;

    /**
     * 督办截止时间
     */
    private Long supervisionEndTime;

    /**
     * 督办/查岗人
     */
    private String supervisor;

    /**
     * 督办联系电话
     */
    private String supervisionTel;

    /**
     * 督办联系邮件
     */
    private String supervisionEmail;


    /**
     * 西藏运输企业名称
     */
    private String enterprise;

    /**
     * 查岗/督办时间
     */
    private Date time;
    @ExcelField(title = "时间")
    private String timeStr;

    /**
     * 过期时间
     */
    private Date expireTime;

    @ExcelField(title = "内容")
    private String infoContent;

    /**
     * 处理结果 0：未处理，1：已处理，2：已过期
     */
    private Integer result;
    @ExcelField(title = "处理状态")
    private String resultStr;

    /**
     * 处理时间
     */
    private Date ackTime;
    @ExcelField(title = "应答时间")
    private String ackTimeStr;

    @ExcelField(title = "应答人")
    private String dealer;

    @ExcelField(title = "应答内容")
    private String ackContent;

    /**
     * 报警开始时间
     */
    private Long alarmStartTime;

    /**
     * 源消息编号
     */
    private Integer sourceMsgSn;

    /**
     * 源数据类型
     */
    private Integer sourceDataType;

    /**
     * 事件id
     */
    private String eventId;

    /**
     * 信息id
     */
    private Integer infoId;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 对象id
     */
    private String objectId;

    /**
     * 对象类型
     */
    private Integer objectType;

    private Integer supervisionId;

    /**
     * 数据类型
     */
    private Integer dataType;

    private String handleId;

    private Integer msgGnssCenterId;

    /**
     * 消息类型
     */
    private Integer msgId;

    /**
     * 消息编号
     */
    private Integer msgSn;

    /**
     * 协议类型
     */
    private Integer protocolType;

    /**
     * 上级ip
     */
    private String serverIp;

    /**
     * 应答时限
     */
    private Long answerTime;

    /**
     * 应答人电话
     */
    private String dealerTelephone;

    /**
     * 应答人身份证号码
     */
    private String identityNumber;

    /**
     * 社会保险号
     */
    private String socialSecurityNumber;

    private String mediaUrl;
}
