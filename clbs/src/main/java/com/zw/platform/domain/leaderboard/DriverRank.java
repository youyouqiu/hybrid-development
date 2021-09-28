package com.zw.platform.domain.leaderboard;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


@Data
public class DriverRank {
    /**
     * 驾驶员id
     */
    private transient String id;

    /**
     * 从业资格证号
     */
    private transient String cardNumber;

    /**
     * 所属企业id
     */
    private transient String groupId;

    @ExcelField(title = "驾驶员")
    private String driverName;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "报警数")
    private int total;

    @ExcelField(title = "占比")
    private String percentageString;

    /**
     * 环比
     */
    private String ringRatio;

}
