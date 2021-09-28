package com.zw.platform.domain.vas.alram;

import lombok.Data;

import java.io.Serializable;

/**
 * 报警设置-智能联动请求入参
 * @author Administrator
 */
@Data
public class AlarmLinkageDTO
    implements Serializable {
    private static final long serialVersionUID = -8693415475178087807L;
    /**
     * 下发短信
     */
    public static final int HANDLE_TYPE_MSG = 1;

    /**
     * 拍照
     */
    public static final int HANDLE_TYPE_PHOTO = 2;

    /**
     * 未勾选报警处理联动
     */
    public static final int ALARM_HANDLE_LINKAGE_NON_CHECK = 0;

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

    /**
     * 处理人所属企业信息. 已报警联动设置时为准, 后续修改用户的所属企业也不受影响
     */
    private String handleUserOrgName;

    /**
     * 报警标识, 多个逗号分隔
     */
    private String pos;

    /**
     * 短信相关参数
     */
    private MsgParamDTO msg;

    /**
     * 拍照相关参数
     */
    private PhotoDTO photo;

    /**
     * 录像相关参数
     */
    private PhotoDTO recording;

    /**
     * I/O控制
     */
    private OutputControlDTO outputControl;

    /**
     * 实时视频
     */
    private Integer videoFlag;

    /**
     * 上传音视频资源列表: 默认为0
     */
    private Integer uploadAudioResourcesFlag;

    /**
     * 协议类型
     */
    private String deviceType;
}
