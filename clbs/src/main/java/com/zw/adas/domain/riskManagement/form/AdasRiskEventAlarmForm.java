package com.zw.adas.domain.riskManagement.form;

import com.zw.adas.utils.AdasCommonHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.protocol.util.ProtocolTypeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by PengFeng on 2017/8/25  15:51
 * @author PengFeng
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventAlarmForm extends BaseFormBean {

    private String eventNumber;

    private String eventTime;

    private String riskEvent;

    private transient byte[] idbyte;

    private String riskType;

    private String speed;

    private Byte picFlag;

    private Byte videoFlag;

    /**
     * 原车纬度 ORIGINAL_LONGITUDE
     */
    private String originalLatitude;

    /**
     * 原车经度 ORIGINAL_LATITUDE
     */
    private String originalLongitude;

    /**
     * 报警等级
     */
    private String level;

    /**
     * 车辆状态
     */
    private Integer vehicleStatus;

    /**
     * 驾驶员姓名
     */
    private String driverName;

    /**
     * 驾驶证号
     */
    private String driverNo;

    /**
     * 经度
     */
    private String latitude;

    /**
     * 纬度
     */
    private String longitude;

    /**
     * 道路类型
     */
    private Integer roadType;

    private String roadTypeStr;

    /**
     * 路网限速
     */
    private String roadLimitSpeed;

    /**
     * 监控对象
     */
    private String brand;

    /**
     * 车辆颜色 PLATE_COLOR
     */
    private String plateColor;

    /**
     * 是否有终端多媒体文件
     */
    private Boolean hasMedia;

    /**
     * 视频终端证据
     */
    private Boolean hasVideo;

    /**
     * 图片终端证据
     */
    private Boolean hasPic;

    /**
     * 手动下发9208的媒体状态（0代表可以获取附件，1代表附件失效不能点击获取附件，2代表附件获取中）
     */
    private Byte attachmentStatus;

    /**
     * 数量
     */
    private Integer mediaCount;

    /**
     * 媒体信息mediaInfo
     */
    private String mediaInfoStr;

    private Integer protocolType;

    /**
     * 转换多媒体文件标志
     */
    public void assembleMediaFlag() {
        hasPic = picFlag != null && picFlag == 1;
        hasVideo = videoFlag != null && videoFlag == 1;
        hasMedia = hasPic || hasVideo;
    }

    public void transFormData(AdasCommonHelper adasCommonHelper) {
        riskType = adasCommonHelper.geRiskTypeName(riskEvent);
        riskEvent = adasCommonHelper.geEventName(riskEvent);
        if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(protocolType + "")) {
            level = adasCommonHelper.getLevelNameOfBeijing(level);
        } else if (ProtocolTypeUtil.noLevelProtocol(protocolType)) {
            level = adasCommonHelper.getNoLevelName(level);
        } else if (ProtocolTypeUtil.XIANG_PROTOCOL_808_2013.equals(protocolType + "")) {
            if (riskEvent.contains("报警")) {
                level = "报警";
            } else if (riskEvent.contains("预警")) {
                level = "预警";
            } else {
                level = "";
            }
        } else {
            level = adasCommonHelper.getLevelName(level);
        }
        plateColor = PlateColor.getNameOrBlankByCode(plateColor);
        assembleMediaFlag();
    }
}
