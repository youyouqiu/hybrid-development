package com.zw.lkyw.domain.historicalSnapshot;

import lombok.Data;

import java.util.Date;

/***
 @Author lijie
 @Date 2020/1/6 14:20
 @Description 历史抓拍信息
 @version 1.0
 **/
@Data
public class HistoricalSnapshotInfo {

    private String id;
    private long type;
    private String mediaName;
    private String mediaUrl;
    private long formatCode;
    private long eventCode;
    private long wayId;
    private String vehicleId;
    private long flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    private long mediaId;
    private String riskId;
    private String riskEventId;
    private long source;
    private String visitId;
    private String mediaUrlNew;
    private String description;
    private String address;
    private String monitorName;
    private String longitude;
    private String latitude;
    private String speed;


}
