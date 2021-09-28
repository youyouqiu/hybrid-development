package com.zw.talkback.domain.basicinfo;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class IntercomObjectInfo implements Serializable {

    private static final long serialVersionUID = -1119194811869613208L;

    /**
     * 未生成
     */
    public static final int NOT_GENERATE_STATUS = 0;

    /**
     * 生成成功
     */
    public static final int GENERATE_SUCCESS_STATUS = 1;

    /**
     * 生成失败
     */
    public static final int GENERATE_FAILED_STATUS = 2;

    /**
     * 对象名称
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 监控对象类型名称
     */
    @ExcelField(title = "监控对象类型")
    private String monitorTypeName;

    @ExcelField(title = "生成状态")
    private String statusName;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "群组")
    private String assignmentName;

    @ExcelField(title = "终端手机号")
    private String simcardNumber;

    @ExcelField(title = "对讲设备标识")
    private String intercomDeviceId;

    @ExcelField(title = "原始机型")
    private String modelId;

    @ExcelField(title = "对讲机型")
    private String intercomModelName;

    @ExcelField(title = "优先级")
    private Integer priority;

    @ExcelField(title = "客户代码")
    private Long customerCode;

    @ExcelField(title = "个呼号码")
    private String number;

    @ExcelField(title = "组数")
    private String groupNum;

    /**
     * 我们平台的对讲对象管理ID
     */
    private String id;

    /**
     * 对讲平台存储的对讲对象ID
     */
    private Long userId;

    /**
     * 对象ID
     */
    private String monitorId;

    /**
     * 监控对象类型
     */
    private String monitorType;

    /**
     * SIM卡ID
     */
    private String simcardId;

    /**
     * 信息配置表ID
     */
    private String configId;

    /**
     * 原始机型ID(index)
     */
    private Long originalModelId;

    /**
     * 所属组织ID
     */
    private String groupId;

    /**
     * 监控对象所属组织
     */
    private String monitorGroupId;

    /**
     * 生成状态: 0: 未生成; 1:生成成功; 2:生成失败
     */
    private Integer status;

    /**
     * 定位终端号
     */
    private String deviceNumber;

    /**
     * 定位終端ID
     */
    private String deviceId;
    /**
     * 原始机型
     */
    private String originalModelName;

    /**
     * 组数
     */
    private Integer maxGroupNum;

    /**
     * 当前所在组数量
     */
    private Integer currentGroupNum;

    /**
     * 是否录音: 0: 不录音; 1: 录音
     */
    private Integer recordEnable;

    /**
     * 文本信息: 1:支持 ;0:不支持
     */
    private Integer textEnable;

    /**
     * 是否支持图片消息 1:支持 0:不支持
     */
    private Integer imageEnable;

    /**
     * 是否支持离线语音消息 1:支持 0:不支持
     */
    private Integer audioEnable;

    /**
     * 对讲终端密码
     */
    private String devicePassword;

    private String assignmentId;

    /**
     * 最大好友数
     */
    private Integer maxFriendNum;

    /**
     * 已使用的旋钮"逗号分隔"
     */
    private String knobNos;

    /**
     * 原始机型中的旋钮数量
     */
    private Integer knobNum;

    /**
     * SIM卡授权码
     */
    private String authCode;

    private String flag;
    private Date createDataTime = new Date();
    private String createDataUsername;

    private Integer audioConferenceEnable;
    private Integer videoConferenceEnable;
    private Integer videoCallEnable;
    private Integer sendTextEnable;
    private Integer sendImageEnable;
    private Integer sendAudioEnable;
    private Integer tempGroupEnable;
    private Integer videoFuncEnable;
    private String comments;

    /**
     * 数据修改时间
     */
    @ApiParam(value = "数据修改时间")
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    @ApiParam(value = "修改者username")
    private String updateDataUsername;

    private String intercomInfoId;

    public IntercomObjectInfo(IntercomDTO intercomDTO) {
        this.monitorName = intercomDTO.getName();
        this.monitorTypeName = MonitorTypeEnum.getNameByType(intercomDTO.getMonitorType());
        this.statusName = 0 == intercomDTO.getStatus() ? "未生成" : 1 == intercomDTO.getStatus() ? "已生成" : "生成失败";
        this.groupName = intercomDTO.getOrgName();
        this.assignmentName = intercomDTO.getGroupName();
        this.simcardNumber = intercomDTO.getSimCardNumber();
        this.intercomDeviceId = intercomDTO.getIntercomDeviceId();
        this.modelId = intercomDTO.getOriginalModel();
        this.intercomModelName = intercomDTO.getIntercomModelName();
        this.priority = intercomDTO.getPriority();
        this.customerCode = intercomDTO.getCustomerCode();
        this.number = intercomDTO.getCallNumber();
        this.groupNum = intercomDTO.getCurrentGroupNum() + "/" + intercomDTO.getMaxGroupNum();
        this.id = intercomDTO.getIntercomInfoId();
        this.userId = intercomDTO.getUserId();
        this.monitorId = intercomDTO.getId();
        this.monitorType = intercomDTO.getMonitorType();
        this.simcardId = intercomDTO.getSimCardId();
        this.configId = intercomDTO.getConfigId();
        this.originalModelId = intercomDTO.getOriginalModelId();
        this.groupId = intercomDTO.getOrgId();
        this.monitorGroupId = intercomDTO.getOrgId();
        this.status = intercomDTO.getStatus();
        this.deviceNumber = intercomDTO.getDeviceNumber();
        this.deviceId = intercomDTO.getDeviceId();
        this.originalModelName = intercomDTO.getOriginalModel();
        this.maxGroupNum = intercomDTO.getMaxGroupNum();
        this.currentGroupNum = intercomDTO.getCurrentGroupNum();
        this.recordEnable = intercomDTO.getRecordEnable();
        this.textEnable = intercomDTO.getTextEnable();
        this.imageEnable = intercomDTO.getImageEnable();
        this.audioEnable = intercomDTO.getAudioEnable();
        this.devicePassword = intercomDTO.getDevicePassword();
        this.assignmentId = intercomDTO.getGroupId();
        this.maxFriendNum = intercomDTO.getMaxFriendNum();
        this.knobNum = intercomDTO.getKnobNum();
        this.authCode = intercomDTO.getAuthCode();
        this.audioConferenceEnable = intercomDTO.getAudioConferenceEnable();
        this.videoConferenceEnable = intercomDTO.getVideoConferenceEnable();
        this.videoCallEnable = intercomDTO.getVideoCallEnable();
        this.sendTextEnable = intercomDTO.getSendTextEnable();
        this.sendImageEnable = intercomDTO.getSendImageEnable();
        this.sendAudioEnable = intercomDTO.getSendAudioEnable();
        this.tempGroupEnable = intercomDTO.getTempGroupEnable();
        this.videoFuncEnable = intercomDTO.getVideoFuncEnable();
        this.comments = intercomDTO.getComments();
    }

}
