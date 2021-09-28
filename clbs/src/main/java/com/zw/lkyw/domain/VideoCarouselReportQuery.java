package com.zw.lkyw.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zw.platform.util.common.BaseQueryBean;

import lombok.Data;

@Data
public class VideoCarouselReportQuery  extends BaseQueryBean {

    private String monitorName;
    private List<String> monitorIds;
    //1代表报表最上面的查询接口，2代表报表下面的查询接口
    private byte flag;
    private String status;
    private String startTime;
    private String endTime;
    private Map<String, String> queryParam = new HashMap<>(16);

}
