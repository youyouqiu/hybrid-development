package com.zw.talkback.domain.intercom.form;

import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * 对讲对象查询数据返回
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class IntercomObjectForm extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = -8011493718400630974L;

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

    public static final Integer SIM_CARD_STATUS_BIND = 0;

    public static final Integer SIM_CARD_STATUS_NOT_BIND = 1;

    public static final Integer NOT_ENABLE_STATUS = 0;

    public static final Integer ENABLE_STATUS = 1;

    /**
     * 对讲平台存储的对讲对象ID
     */
    private Long userId;

    /**
     * 对象ID
     */
    private String monitorId;

    /**
     * SIM卡ID
     */
    private String simcardId;

    /**
     * 信息配置表ID
     */
    private String configId;

    /**
     * 原始机型(index)
     */
    private Long originalModelId;

    /**
     * 原始机型编号
     */
    private String modelId;

    /**
     * 原始机型名称
     */
    private String originalModelName;

    /**
     * 所属组织ID
     */
    private String groupId;

    /**
     * 对象名称
     */
    private String monitorName;

    /**
     * 生成状态: 0: 未生成; 1:生成成功; 2:生成失败
     */
    private Integer status;

    /**
     * 所属组织
     */
    private String groupName;

    /**
     * SIM卡
     */
    @ExcelField(title = "定位终端手机号", required = true, repeatable = false)
    private String simcardNumber;

    /**
     * 对讲终端ID
     */
    @ExcelField(title = "对讲终端ID", required = true, repeatable = false)
    private String intercomDeviceId;

    /**
     * 对讲终端密码
     */
    @ExcelField(title = "设备密码", required = true)
    private String devicePassword;

    /**
     * 优先级
     */
    @ExcelField(title = "优先级")
    private Integer priority;

    /**
     * 个呼号码
     */
    private String number;

    /**
     * 组数
     */
    private Integer maxGroupNum;

    /**
     * 是否录音: 0: 不录音; 1: 录音
     */
    private Integer recordEnable = 0;

    /**
     * 文本信息: 1:支持 ;0:不支持
     */
    private Integer textEnable = 0;

    /**
     * 是否支持图片消息 1:支持 0:不支持
     */
    private Integer imageEnable = 0;

    /**
     * 是否支持离线语音消息 1:支持 0:不支持
     */
    private Integer audioEnable = 0;

    /**
     * 客户代码, 默认为1
     */
    private Long customerCode;

    /**
     * 0: sim卡存在绑定关系; 1: sim不存在绑定关系(存在于平台);
     */
    private Integer simCardStatus;

    private transient ConfigList configList;

    private Integer audioConferenceEnable;
    private Integer videoConferenceEnable;
    private Integer videoCallEnable;
    private Integer sendTextEnable;
    private Integer sendImageEnable;
    private Integer sendAudioEnable;
    private Integer tempGroupEnable;
    private Integer videoFuncEnable;
    private String comments;
    private Integer knobNum;

    public static String getIntercomDeviceId(String modelId, String intercomDeviceId) {
        return modelId + intercomDeviceId;
    }

    public static String getRecordEnableFormat(Integer recordEnable) {
        return Objects.nonNull(recordEnable) && recordEnable == 1 ? "开启" : "停止";
    }
}
