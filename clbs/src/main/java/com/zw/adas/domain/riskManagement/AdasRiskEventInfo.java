package com.zw.adas.domain.riskManagement;

import com.zw.platform.domain.reportManagement.T809AlarmFileListAck;
import com.zw.platform.domain.reportManagement.WarnMsgFileInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 风险事件缓存实体
 * lijie
 */
@Data
public class AdasRiskEventInfo {
    private String vehicleId;//车id

    private String brand;//车牌

    private String riskEventId;//风险事件id

    private String riskId;//风险id

    private Integer mediaType;//媒体类型

    private long warmTime;//风险时间

    private String riskType;//风险类型

    private Integer riskLevel;//风险等级

    private String address;//风险位置

    private String riskNumber;//风险编号

    private Integer eventId;//风险事件类型

    private String eventNumber;//风险事件编号

    private String resultPath;//ftp路径

    private String driver;//司机姓名

    private Integer mediaCount;//媒体个数

    private Integer protocolType;//协议类型（1黑标，12川标，13冀标）

    private String token;//桂标设置的多媒体token，目前只有桂标需要（保证多个对媒体token一致）

    private String alarmId;//报警标识号

    private String t809PlatId;

    private String t809ProtocolType;

    private boolean autoDeal = false;

    private List<WarnMsgFileInfo> fileList = new ArrayList<>();

    private T809AlarmFileListAck fileListAck;

    private AlarmSign alarmSign;

    private Double longitude;

    private Double latitude;

    private Double direction;

    public AdasRiskEventInfo() {
    }

    public AdasRiskEventInfo(AdasInfo adasInfo, Integer mediaCount) {
        this.vehicleId = adasInfo.getVehicleId();
        this.brand = adasInfo.getBrand();
        this.riskEventId = adasInfo.getRiskEventId();
        this.riskId = adasInfo.getRiskId();
        this.warmTime = adasInfo.getWarnTime() == null ? adasInfo.getWarmTime() : adasInfo.getWarnTime();
        this.riskType = adasInfo.getRiskType();
        this.riskLevel = adasInfo.getRiskLevel();
        this.address = adasInfo.getAddress();
        this.riskNumber = adasInfo.getRiskNumber();
        this.eventId = Integer.parseInt(adasInfo.getEventId());
        this.eventNumber = adasInfo.getEventNumber();
        this.driver = adasInfo.getDriver();
        this.mediaCount = mediaCount;
        this.protocolType = adasInfo.getProtocolType().intValue();
        this.t809PlatId = adasInfo.getT809PlatId();
        this.t809ProtocolType = adasInfo.getT809ProtocolType();
        this.direction = adasInfo.getDirection();
        this.longitude = adasInfo.getLongitude();
        this.latitude = adasInfo.getLatitude();
    }

    public AdasRiskEventInfo(AdasInfo adasInfo, Integer mediaType, String resultPath) {
        this.vehicleId = adasInfo.getVehicleId();
        this.brand = adasInfo.getBrand();
        this.riskEventId = adasInfo.getRiskEventId();
        this.riskId = adasInfo.getRiskId();
        this.mediaType = mediaType;
        this.warmTime =  adasInfo.getWarnTime() == null ? adasInfo.getWarmTime() : adasInfo.getWarnTime();
        this.riskType = adasInfo.getRiskType();
        this.riskLevel = adasInfo.getRiskLevel();
        this.address = adasInfo.getAddress();
        this.riskNumber = adasInfo.getRiskNumber();
        this.eventId = Integer.parseInt(adasInfo.getEventId());
        this.eventNumber = adasInfo.getEventNumber();
        this.driver = adasInfo.getDriver();
        this.resultPath = resultPath;
        this.protocolType = adasInfo.getProtocolType().intValue();
        this.t809PlatId = adasInfo.getT809PlatId();
        this.t809ProtocolType = adasInfo.getT809ProtocolType();
        this.direction = adasInfo.getDirection();
        this.longitude = adasInfo.getLongitude();
        this.latitude = adasInfo.getLatitude();
    }

    //黑标巡检
    private boolean isInspection = false;
    private String inspectionResultId;
    //巡检类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检 3.驾驶员身份识别巡检）
    private Integer inspectionType;

    public AdasRiskEventInfo(String vehicleId, String inspectionResultId, Integer inspectionType, Integer mediaCount) {
        this.vehicleId = vehicleId;
        this.inspectionResultId = inspectionResultId;
        this.inspectionType = inspectionType;
        this.mediaCount = mediaCount;
        this.isInspection = true;
    }

}