package com.zw.adas.domain.riskManagement;

import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

import java.util.List;

/**
 * f3交互实体
 *
 * @author XK
 */
@Data
public class HBaseRiskDeal {
    private List<byte[]> riskIdOrEventIds;
    private Integer status;
    private String name;
    private Long now;
    private Integer riskResult;
    private String driverName;
    private String driverId;
    private String driverNumber;
    private String tableName;

    public static HBaseRiskDeal getInstance(AdasDealInfo dealInfo, List<String> riskIdOrEventIds, Integer status,
                                            String name, Long now, Integer riskResult, String tableName) {
        HBaseRiskDeal hbaseRiskDeal = new HBaseRiskDeal();
        hbaseRiskDeal.setDriverId(dealInfo.getDriverId());
        hbaseRiskDeal.setDriverName(dealInfo.getDriverName());
        hbaseRiskDeal.setDriverNumber(dealInfo.getDriverNumber());
        hbaseRiskDeal.setName(name);
        hbaseRiskDeal.setNow(now);
        hbaseRiskDeal.setRiskIdOrEventIds(UuidUtils.batchTransition(riskIdOrEventIds));
        hbaseRiskDeal.setStatus(status);
        hbaseRiskDeal.setRiskResult(riskResult);
        hbaseRiskDeal.setTableName(tableName);
        return hbaseRiskDeal;
    }
}
