package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RiskRankEntity extends BaseEntity {
    private String vehicleIds;//车辆ids

    private String startTime;//开始时间

    private String endTime;//结束时间

    private Integer riskType;//风险类型1（疲劳），2（分心），3（异常），4,(碰撞)，5（组合）不传默认汇总。

    private Integer page;//第几页

    private Integer pageSize;//每页条数

    private String type;//监控对象类型0（车），1（人），2（物），3（所有）（字符串类型）

    private Integer status;//已处理（6），未处理（1）

    private String vehicleId;

}
