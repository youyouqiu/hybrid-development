package com.zw.lkyw.domain.historicalSnapshot;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/***
 @Author lijie
 @Date 2020/1/6 11:38
 @Description 查询历史抓拍实体
 @version 1.0
 **/
@Data
public class HistoricalSnapshotQuery  extends BaseQueryBean {

    private Integer type = 0;//查询的类型（0 全部，1 图片，2 视频）

    @NotNull(message = "车辆id不能为空")
    private String vehicleIds;//车辆ids用逗号隔开

    private List<String> vids;

    @NotNull(message = "开始时间不能为空")
    private String startTime;//开始时间

    @NotNull(message = "结束时间不能为空")
    private String endTime;//结束时间

    private String snapshotTime;

    private String latitude;

    private String longitude;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

}
