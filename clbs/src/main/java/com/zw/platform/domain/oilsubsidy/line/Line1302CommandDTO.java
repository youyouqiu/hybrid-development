package com.zw.platform.domain.oilsubsidy.line;

import com.zw.platform.util.OilSubsidy1302CommandField;
import lombok.Data;

/**
 * @author wanxing
 * @Title: 1302指令实体
 * @date 2020/10/1216:45
 */
@Data
public class Line1302CommandDTO extends CommandOf809CommonDTO {

    /**
     * 线路标识
     */
    private String identify;

    /**
     * 站点序号
     */
    private String stationInfoOrder;
    /**
     * 站点经度
     */
    private Double longitude;
    /**
     * 站点纬度
     */
    private Double latitude;

    /**
     * 方向信息(0代表上行，1代表下行)
     */
    private Integer directionType;

    @Override
    public String toString() {
        return OilSubsidy1302CommandField.LINE_NO + ":=" + this.identify + ";" + OilSubsidy1302CommandField.LNG + ":="
            + (long)(this.longitude * 1000000) + ";"
            + OilSubsidy1302CommandField.LAT + ":=" + (long)(this.latitude * 1000000) + ";"
            + OilSubsidy1302CommandField.POINT_NO + ":=" + this.stationInfoOrder + ";"
            + OilSubsidy1302CommandField.IS_UP_DOWN + ":=" + this.directionType + ";";
    }
}
