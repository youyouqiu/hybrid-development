package com.zw.platform.domain.vas.alram;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmLinkageParam extends BaseFormBean implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 报警处理联动: 用于前端控制是否勾选复选框 0: 未勾选; 1: 勾选.
     */
    private Integer alarmHandleLinkageCheck;

    /**
     * 报警处理方式: 1: 下发短信; 2: 拍照; 3: 不做处理;
     */
    private Integer alarmHandleType;

    /**
     * 处理结果: 1:处理中; 2:已处理; 3: 不作处理; 4: 将来处理;
     */
    private Integer alarmHandleResult;

    /**
     * 处理人姓名
     */
    private String handleUsername;

    private String pos;

    private String vehicleId;

    private String alarmTypeId;

    private String photoId;

    private String recordingId;

    private String msgId;

    private String outputControlId;

    private Integer videoFlag;
    /**
     * 音视频资源列表
     */
    private Integer uploadAudioResourcesFlag;
}
