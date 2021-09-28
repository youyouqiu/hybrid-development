package com.zw.platform.domain.leaderboard;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;


@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleRank {

    @ExcelField(title = "监控对象")
    private String brand;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "所属区域")
    private String area;

    @ExcelField(title = "报警数")
    private int total;

    @ExcelField(title = "占比")
    private String percentageString;

    private double percentage;

    private int time;

    private String vid;

    private String ratio;

    private String groupId;

    public VehicleRank(String brand, String groupName, String area, String vid, String groupId) {
        this.brand = brand;
        this.groupName = groupName;
        this.area = area;
        this.vid = vid;
        this.groupId = groupId;
        this.percentageString="0%";
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VehicleRank that = (VehicleRank) o;
        return Objects.equals(vid, that.vid);
    }

    @Override public int hashCode() {

        return Objects.hash(vid);
    }

    public VehicleRank(){}

}
