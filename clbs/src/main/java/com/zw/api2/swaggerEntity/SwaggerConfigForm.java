package com.zw.api2.swaggerEntity;

import lombok.Data;

@Data
public class SwaggerConfigForm {


    private String brandID = ""; // 车辆id

    private String deviceID = ""; // 设备id

    private String simID = ""; // sim卡id

    private String citySelID = ""; // 分组id

    private String brands = ""; // 车牌

    private String devices = ""; // 终端号

    private String sims;// sim卡号

    private String deviceType;//终端类型，（类型： 1 ：交通部JTB808；2：移为GV320；3：天禾）

    private String monitorType;//监控对象类型（0：车，1：人，2：物）

    private String groupid;//分组名字
}
