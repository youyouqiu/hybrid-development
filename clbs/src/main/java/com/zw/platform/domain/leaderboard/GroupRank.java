package com.zw.platform.domain.leaderboard;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Objects;

@Data
public class GroupRank {

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

    private String groupId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupRank groupRank = (GroupRank) o;
        return Objects.equals(groupId, groupRank.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }

    public GroupRank() {
    }

}
