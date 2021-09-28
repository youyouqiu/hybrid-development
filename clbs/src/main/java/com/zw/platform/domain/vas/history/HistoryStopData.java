package com.zw.platform.domain.vas.history;

import com.zw.platform.domain.oil.BdtdPosition;
import com.zw.platform.domain.oil.Positional;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by LiaoYuecai on 2016/10/25.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HistoryStopData {
    private Positional positional;
    private BdtdPosition bdtdPosition;
    private String StartTime;
    private String endTime;
    private Long stopTime;
}
