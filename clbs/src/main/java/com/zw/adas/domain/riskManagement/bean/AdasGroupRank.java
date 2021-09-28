package com.zw.adas.domain.riskManagement.bean;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Objects;


@Data
public class AdasGroupRank {

    @ExcelField(title = "企业名称")
    private String groupName;

    @ExcelField(title = "所属区域")
    private String area;

    @ExcelField(title = "报警数")
    private int total;

    private double percentage;

    @ExcelField(title = "占比")
    private String percentageString;

    private int time;

    private String ratio;

    /**
     * 企业Id
     */
    private String groupId;

    public AdasGroupRank(String groupId, String groupName) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.percentageString = "0%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdasGroupRank groupRank = (AdasGroupRank) o;
        return Objects.equals(groupId, groupRank.groupId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(groupId);
    }

    public AdasGroupRank() {
    }

}
