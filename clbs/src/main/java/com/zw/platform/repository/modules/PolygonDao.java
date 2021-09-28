package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.PolygonContent;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Tdz on 2016/8/8.
 */
public interface PolygonDao {
    /**
	 * 查询多边形从表
	 * @author wangying
	 */
    List<Polygon> getPolygonById(@Param("id") final String id);

    /**
     * 根据多边形id查询多边形主表id
     * @author Liubangquan
     */
    Polygon findPolygonById(@Param("id") final String id);
    
    /**
     * 新增多边形坐标点
     */
    boolean addPolygonsContent(final PolygonForm form);
    
    /**
    * 根据多边形id删除多边形点数据-修改多边形时先删点再加点信息
    * @param fenceId 多边形id
     * @return boolean
     */
    boolean deletePolygonContent(String fenceId);

    /**
     * 删除多边形
     * @param fenceId 多边形id
     * @return boolean
     */
    boolean deletePolygon(String fenceId);
    
    /**
    * 更新多边形坐标点：备注-此处添加时坐标点的id为指定多边形的id而不是生成的uuid了
    * @author Liubangquan
     */
    boolean updatePolygonContent(final PolygonForm form);
    
    /**
     * 新增多边形
     */
    boolean addPolygons(final PolygonForm form);
    
    /**
    * 更新多边形区域
    * @author Liubangquan
     */
    boolean updatePolygon(final PolygonForm form);

    boolean addFenceInfo(final ManageFenceFrom fenceForm);
    
    /**
     * 根据ids 查询多边形主表
     * @author wangying
     */
    List<Polygon> findPolygonByIds(@Param("ids") final List<String> ids);

    /**
    * 根据多边形主表id查询其点信息
    * @author Liubangquan
     */
    List<PolygonContent> findPolygonContentsById(@Param("id") String id);
    
    boolean addMoreContent(List<PolygonForm> polygonForm);
}
