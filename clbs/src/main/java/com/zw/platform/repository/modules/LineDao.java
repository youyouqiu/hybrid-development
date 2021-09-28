package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LineSpot;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.LineSpotForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.query.LineSegmentInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/8.
 */
public interface LineDao {

    boolean add(final LineForm form);

    /**
     * 修改线路主表信息
     * @author Liubangquan
     */
    boolean updateLine(final LineForm form);

    /**
     * 保存线路点信息
     * @author Liubangquan
     */
    boolean addLineContent(final LineForm form);

    /**
     * 批量新增 线信息
     * @param lineFormList 线信息
     * @return boolean
     */
    boolean addLineContentBatch(final List<LineForm> lineFormList);

    /**
     * 更新线路坐标点：备注-此处添加时坐标点的id为指定线路的id而不是生成的uuid了
     * @author Liubangquan
     */
    boolean updateLineContent(final LineForm form);

    boolean fenceInfo(final ManageFenceFrom fenceForm);

    /**
     * 根据线路id查询围栏的id
     * @param lineId
     * @return
     */
    String getFenceIdByLineId(@Param("lineId") String lineId);

    /**
     * 根据线路主信息id查询 线路点信息
     * @author Liubangquan
     */
    List<LineContent> findLineContentById(@Param("id") String id);

    List<LineContent> findLineContentsById(@Param("id") String id);

    List<LineSpot> findLineSpotByLid(@Param("id") String id);

    List<LineSegmentInfo> findSegmentContentByLid(@Param("id") String id);

    List<Map<String, Object>> findBindInfoByLid(@Param("id") String id);

    /**
     * 根据线路id查询线路主表
     * @author Liubangquan
     */
    Line findLineById(@Param("id") String id);

    /**
     * 根据id list 查询线路主表
     */
    List<Line> findLineByIds(@Param("ids") List<String> ids);

    /**
     * 获取所有的线路信息
     * @author Liubangquan
     */
    List<Line> findAllLine();

    void addMonitoringTag(final LineSpotForm form);

    List<LineSpot> findLineSpotByVid(@Param("id") String id);

    /**
     * 删除线
     * @param fenceId 线id
     * @return boolean
     */
    boolean deleteLine(String fenceId);

    /**
     * 删除线
     * @param fenceId 线id
     * @return boolean
     */
    boolean deleteLineContent(String fenceId);

    /**
     * 根据线路名字查询线路id
     * @param name
     * @return
     */
    String findLineByName(String name);

}
