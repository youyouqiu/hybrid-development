package com.zw.platform.service.functionconfig;

import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LineSpot;
import com.zw.platform.domain.functionconfig.query.LineSegmentInfo;

import java.util.List;

public interface LineService {

    /**
     * 查询线路点的信息
     */
    List<LineContent> findLineContentById(String id);

    List<LineContent> findLineContentsById(String id);

    List<LineSpot> findLineSpotByLid(String id);

    /**
     * 根据线路id查询线路信息
     * @return Line
     * @author Liubangquan
     */
    Line findLineById(String id);

    List<LineSpot> findLineSpotByVid(String vid);

    List<LineSegmentInfo> findSegmentContentByLid(String id);
}
