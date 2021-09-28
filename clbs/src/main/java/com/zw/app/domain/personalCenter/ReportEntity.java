package com.zw.app.domain.personalCenter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

@Data
public class ReportEntity extends BaseEntity {

    private String moniterIds;//监控对象ids

    private String moniterId;//监控对象id

    private String startTime;//开始时间

    private String endTime;//结束时间

    private int type;//监控对象报警类型0（终端），1（平台）

}
