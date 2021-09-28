package com.cb.platform.domain;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 趟次运输表
 */
@Data
public class TransportTimesEntity implements Serializable {

    private static final long serialVersionUID = -2368044494718961985L;

    private String id;
    //车辆id
    private String vehicleId;

    //品名id
    private String itemNameId;

    //危险品类别
    private Integer dangerType;
    //单位
    private Integer unit;
    //数量
    private Long count;
    //运输类型：
    //1：营运性危险货物运输、
    //2：非营运性危险货物运输
    private Integer transportType;
    //运输日期
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transportDate;
    //起始地点
    private String startSite;
    //途径地点
    private String viaSite;
    //目的地点
    private String aimSite;
    //押运员(对应平台从业人员id)
    private String professinoalId;
    //从业资格证号
    private String professinoalNumber;
    //电话
    private String phone;
    //备注
    private String remark;
    private Integer flag;
    private  Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;


}
