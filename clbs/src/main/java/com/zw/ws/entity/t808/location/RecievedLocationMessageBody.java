package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p> Title: RecievedLocationMessageBody.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p>
 * <p> team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年8月10日下午3:07:38
 */
@Data
public class RecievedLocationMessageBody implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<GpsAttachInfo> gpsAttachInfoList;

    private double latitude;

    private Integer alarm;

    private double speed;

    private long time;

    private Integer status;

    private double longitude;

    private Integer direction;

    private double altitude;

    private String vehicleId;

    private String msgSNAck;

    private MsgInfo gpsInfo;

    private int softwareFance;

    private String mileage;

    private String formattedAddress;

    private List<LocationAttachOilExpand> oilExpend = new ArrayList<>();

    private List<WorkDayInfo> shakeDates = new ArrayList<>();

    private List<LocationAttachOilTank> oilMass = new ArrayList<>();

    private List<LocationAttachSimCrad> simCrads = new ArrayList<>();

    private List<IoSignalMessage> ioSignalData = new ArrayList<>();
}
