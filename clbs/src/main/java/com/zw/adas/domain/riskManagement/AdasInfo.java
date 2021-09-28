package com.zw.adas.domain.riskManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class AdasInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String simCardNumber;

    private String eventId;

    private Integer riskLevel;

    private String eventNumber;

    private Integer level;

    private Float riskSpeed;

    private Long warnTime;

    @Deprecated
    private Long warmTime;

    private Integer dealStatus;

    private Integer type;

    private String deviceId;

    private String deviceNumber;

    private Float speed;

    private Integer fatigueLevel;

    private Integer sensorId;

    private Integer warnId;

    private String riskId;

    private Long createDataTime;

    private Long eventTime;

    private String riskType;

    private String riskEventId;

    private byte[] riskEventIdByte;

    private String id;

    private String vehicleId;

    private String job;

    private String brand;

    private String riskNumber;

    private List<AdasMediaInfo> mediaInfoList;

    private Integer frontVehicleDistance;

    private Float frontVehicleSpeed;

    private boolean merge;

    private Integer originLevel;

    private Integer roadMarkings;

    private Integer roadMarkingsData;

    private Integer speedFactor;

    private Integer yunzhengStatus;

    private String address;

    private String driver;

    //报警标识,川标多媒体
    private AlarmSign alarmSign;

    //报警标识，中位标准
    private List<AlarmSign> alarmSigns;

    //协议类型
    private Byte protocolType;

    //是否达到时间阈值 (0未达到阈值，1达到阈值)
    private Byte timeThresholdReminder;

    //是否达到距离阈值(0未达到阈值，1达到阈值)
    private Byte distanceThresholdReminder;

    private Byte up809Flag;

    private String t809PlatId;

    private String originalEventId;

    private Integer t809Pos;

    private String t809ProtocolType;

    private Double direction;

    private Double longitude;

    private Double latitude;
}
