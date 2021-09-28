package com.zw.platform.basic.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 对讲信息列表绑定信息
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IntercomDTO extends BindDTO {
    @ApiModelProperty(value = "原始机型(index)")
    private Long originalModelId;

    @ApiModelProperty(value = "原始机型")
    private String originalModel;

    @ApiModelProperty(value = "设备密码")
    private String devicePassword;

    @ApiModelProperty(value = "是否支持文本信息 1:支持 ;0:不支持")
    private Integer textEnable;

    @ApiModelProperty(value = "是否支持图片消息 1:支持 ;0:不支持")
    private Integer imageEnable;

    @ApiModelProperty(value = "是否支持离线语音消息 1:支持 ;0:不支持")
    private Integer audioEnable;

    @ApiModelProperty(value = "优先级 1/2/3/4/5")
    private Integer priority;

    @ApiModelProperty(value = "客户代码: 默认为1")
    private Long customerCode;

    @ApiModelProperty(value = "生成状态: 0: 未生成; 1:生成成功; 2:生成失败")
    private Integer status;

    @ApiModelProperty(value = "是否录音: 0: 不录音; 1: 录音")
    private Integer recordEnable;

    @ApiModelProperty(value = "最大组数")
    private Integer maxGroupNum;

    @ApiModelProperty(value = "对讲机型")
    private String intercomModelName;

    @ApiModelProperty(value = "是否支持语音会议 : 1:支持; 0:不支持")
    private Integer audioConferenceEnable;

    @ApiModelProperty(value = "是否支持视频会议功能1:支持 0:不支持")
    private Integer videoConferenceEnable;

    @ApiModelProperty(value = "是否支持视频电话功能 1:支持 0:不支持")
    private Integer videoCallEnable;

    @ApiModelProperty(value = "是否支持发送IM文本消息 1:支持 0:不支持")
    private Integer sendTextEnable;

    @ApiModelProperty(value = "是否支持发送IM图片消息1:支持 0:不支持")
    private Integer sendImageEnable;

    @ApiModelProperty(value = "是否支持发送离线语音消息1:支持 0:不支持")
    private Integer sendAudioEnable;

    @ApiModelProperty(value = "是否支持创建临时组功能:1:支持 ;0:不支持")
    private Integer tempGroupEnable;

    @ApiModelProperty(value = "是否支持实时视频功能 1:支持 0:不支持")
    private Integer videoFuncEnable;

    @ApiModelProperty(value = "机型备注")
    private String comments;

    @ApiModelProperty(value = "原始机型中的旋钮数量")
    private Integer knobNum;

    @ApiModelProperty(value = "分组对应的组旋钮位置编号")
    private String groupKnobNos;

    @ApiModelProperty(value = "最大好友数")
    private Integer maxFriendNum;

    @ApiModelProperty(value = "当前组数")
    private Integer currentGroupNum;

    @ApiModelProperty(value = "七位对讲设备标识，不包含机型")
    private String intercomDeviceId;

    public Map<String, String> convertToAddRequestParams() {
        Map<String, String> intercomParams = new HashMap<>(32);

        intercomParams.put("custId", String.valueOf(this.customerCode));
        intercomParams.put("parentId", "-1");
        intercomParams.put("ms.name", this.getName());
        intercomParams.put("ms.priority", String.valueOf(this.priority));
        intercomParams.put("ms.number", this.getCallNumber());
        // -----根据机型能力填写 START 1:支持 0:不支持----
        // 是否支持视频会议
        intercomParams.put("ms.videoConferenceEnable", String.valueOf(this.videoConferenceEnable));
        // 是否支持音频会议
        intercomParams.put("ms.audioConferenceEnable", String.valueOf(this.audioConferenceEnable));
        // 是否支持视频会话
        intercomParams.put("ms.videoCallEnable", String.valueOf(this.videoCallEnable));
        // 是否支持文本消息
        intercomParams.put("ms.sendTextEnable", String.valueOf(this.sendTextEnable));
        // 是否支持图片消息
        intercomParams.put("ms.sendImageEnable", String.valueOf(this.sendImageEnable));
        // 是否支持离线语音消息
        intercomParams.put("ms.sendAudioEnable", String.valueOf(this.sendAudioEnable));
        // 是否支临时组功能
        intercomParams.put("ms.tempGroupEnable", String.valueOf(this.tempGroupEnable));
        // 是否支持实时视频功能
        intercomParams.put("ms.videoFuncEnable", String.valueOf(this.videoFuncEnable));
        // 备注
        intercomParams.put("ms.comments", this.comments);
        // -----根据机型能力填写 END----
        // APP登陆开关, 此处没有App,因此为空
        intercomParams.put("ms.appEnable", "0");
        intercomParams.put("ms.appPassword", "000000");
        // 是否开启录音
        intercomParams.put("ms.recordEnable", String.valueOf(this.recordEnable));
        // 用户电话
        intercomParams.put("ms.phoneNumber", this.getSimCardNumber());
        return intercomParams;
    }

    public static String getRecordEnableFormat(Integer recordEnable) {
        return Objects.nonNull(recordEnable) && recordEnable == 1 ? "开启" : "停止";
    }

    public class Status {
        /**
         * 0 未生成，1 生成成功，2 生成失败
         */
        public static final int NOT_GENERATE_STATUS = 0;

        public static final int SUCCESS_STATUS = 1;

        public static final int FAILED_STATUS = 2;
    }
}
