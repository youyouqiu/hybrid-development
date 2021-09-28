package com.zw.platform.domain.vas.alram;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监控对象联动设置
 * @author create by zhouzongbo on 2020/9/29.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialAlarmDO extends BaseFormBean {
    private static final long serialVersionUID = -1085182562161992989L;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 报警类型id
     */
    private String alarmTypeId;
    /**
     * 拍照设置id，没有值代码不设置
     */
    private String photoId;
    /**
     * 录像id，没有值代码不设置
     */
    private String recordingId;
    /**
     * 是否设置实时视频特殊报警， 0 否 1 是
     */
    private Integer videoFlag;
    /**
     * 短信设置id
     */
    private String msgId;
    /**
     * 是否设置上传音视频资源列表， 0 否 1 是
     */
    private Integer uploadAudioResourcesFlag;
    /**
     * 联动策略输出控制id
     */
    private String outputControlId;
    /**
     * 处理方式: 0: 下发短信; 1: 拍照
     */
    private Integer alarmHandleType;
    /**
     * 处理结果: 1:处理中; 2:已处理; 3: 不作处理; 4: 将来处理;
     */
    private Integer alarmHandleResult;
    /**
     * 处理人姓名， 用于809上传使用
     */
    private String handleUsername;
    /**
     * 报警处理联动是否勾选， 0: 未勾选; 1: 勾选
     */
    private Integer alarmHandleLinkageCheck;
}
