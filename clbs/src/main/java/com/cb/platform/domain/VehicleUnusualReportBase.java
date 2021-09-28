package com.cb.platform.domain;


import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleUnusualReportBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer alarmType;

    private byte[] groupId;

    private byte[] vehicleId;

    private int count;

    public int mountainRoadForbid;

    public int passengerVehicleForbid;

    public int total;

    public String groupName;

}
