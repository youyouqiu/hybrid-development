package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * msginfo
 *
 * @author  Tdz
 * @create 2017-04-06 14:58
 **/
@Data
public class MsgInfo {
    private List<GpsAttachInfo> gpsAttachInfoList;

    private long gps_latitude;

    private long alarm;

    private Integer gps_speed;

    private long gps_time;

    private Integer status;

    private long gps_longitude;

    private Integer gps_direction;

    private Integer gps_altitude;

    private String vehicleId;

    private String msgSNAck;


    private List<LocationAttachOilExpand> oilExpend = new ArrayList<LocationAttachOilExpand>();

    private List<WorkDayInfo> shakeDates = new ArrayList<>();

    private List<LocationAttachOilTank> oilMass = new ArrayList<LocationAttachOilTank>();

    private List<IoSignalMessage> ioSignalData = new ArrayList<IoSignalMessage>();

    private List<LocationAttachSimCrad> simCrad=new ArrayList<>();


}
