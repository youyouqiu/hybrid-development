package com.zw.platform.repository.modules;


import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LinePassPoint;
import com.zw.platform.domain.functionconfig.TravelLine;
import com.zw.platform.domain.functionconfig.form.LinePassPointForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.form.GpsLine;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author tangshunyu
 * @date 2017/6/9
 */
public interface TravelLineDao {

    /**
     * 保存路线起点、终点信息
     * @param form
     * @return
     */
    boolean saveStartAndEndPoint(final GpsLine form);

    /**
     * 保存途经点信息
     * @param list
     * @return
     */
    boolean savePassPoint(final List<LinePassPointForm> list);

    /**
     * 保存所有点信息
     * @param lineContent
     */
    boolean saveAllPoint(final List<LineContent> lineContent);

    // 保存围栏表
    boolean fenceInfo(final ManageFenceFrom fenceForm);

    // 根据id查询TravelLine实体
    TravelLine findTravelLineById(@Param("id") String id);

    /**
     * 根据id删除当前行驶线路上所有的途经点数据
     * @param form
     */
    int deletePassPoint(GpsLine form);

    /**
     * 根据id删除当前行驶线路的所有经纬度点
     * @param form
     */
    int deleteLineContent(GpsLine form);

    Integer countTravelLineById(GpsLine form);

    /**
     * 修改行驶线路起点、终点等信息
     * @param form
     * @return
     */
    boolean updateStartAndEndPoint(GpsLine form);

    // 保存修改后途经点信息
    boolean updatePassPoint(final List<LinePassPointForm> list);

    // 保存所有点信息
    boolean updateAllPoint(final List<LineContent> lineContent);

    // 修改行驶线路主表信息
    // void updateTravelLine(final TravelLineForm form);

    List<LinePassPoint> getPassPointById(final String id);

    TravelLine getTravelLineById(final String id);

    List<LineContent> getAllPointsById(final String id);

    // 删除行驶路线
    void deleteTravelLineById(String id);

    void deletePassPointById(String id);

    void deleteLineContentById(String id);

}
