package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.ManageFenceInfo;
import com.zw.platform.domain.functionconfig.form.*;
import com.zw.platform.domain.functionconfig.query.ManageFenceQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Administrator on 2016/8/9.
 */
public interface ManageFenceDao {

    List<ManageFenceInfo> find(ManageFenceQuery query, @Param("simpleQueryParam1") String simpleQueryParam1);

    int delete(String id);

    ManageFenceInfo gettable(String id);

    boolean delete(@Param("id") String id, @Param("tableName") String tableName);

    boolean deleteSpotbyline(String id);

    int select(String id);

    /**
     * 根据围栏id查询围栏类型
     * @return String(围栏类型)
     */
    String findType(String id);

    /**
     * 根据围栏id查询围栏具体类型id(shape)
     */
    String findFenceTypeId(String fenceId);

    boolean addSegment(List<LineSegmentForm> list);

    boolean addSegmentContent(List<SegmentContentForm> list);

    boolean resetSegment(String lineId);

    List<MarkForm> findMarkByName(String name);

    List<LineForm> findLineByName(String name);

    List<RectangleForm> findRectangleByName(String name);

    List<CircleForm> findCircleByName(String name);

    List<PolygonForm> findPolygonByName(String name);

    List<TravelLineForm> findTravelLineByName(String name);

    /**
     * 根据id查询标注围栏实体
     */
    MarkForm getMarkForm(String id);

    LineForm getLineForm(String id);

    RectangleForm getRectangleForm(String id);

    CircleForm getCircleForm(String id);

    PolygonForm getPolygonForm(String id);

    TravelLineForm getTravelLineForm(String id);

    AdministrationForm getAdministrationForm(String id);
}
