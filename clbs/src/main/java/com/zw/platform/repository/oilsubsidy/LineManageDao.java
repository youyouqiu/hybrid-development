package com.zw.platform.repository.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.line.Line1301CommandDTO;
import com.zw.platform.domain.oilsubsidy.line.Line1302CommandDTO;
import com.zw.platform.domain.oilsubsidy.line.LineDO;
import com.zw.platform.domain.oilsubsidy.line.LineDTO;
import com.zw.platform.domain.oilsubsidy.line.LineQuery;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author wanxing
 * @Title: 路线Dao
 * @date 2020/10/915:22
 */
public interface LineManageDao extends CrudDao<LineDO> {

    /**
     * 模糊查询
     * @param query
     * @return
     */
    List<LineDTO> getListByKeyword(@Param("query") LineQuery query);

    /**
     * 通过Id查询名称
     * @param ids
     * @return
     */
    List<String> getNamesByIds(@Param("ids") Collection<String> ids);

    /**
     * 通过Id查询名称
     * @param ids
     * @return
     */
    List<Map<String, String>> getNameAndIdentifyByIds(@Param("ids") Collection<String> ids);

    /**
     * 通过Id查询
     * @param ids
     * @return
     */
    @MapKey("id")
    Map<String, Map<String, Object>> getListMapByIds(@Param("ids") Collection<String> ids);

    /**
     * 获取绑定的线路
     * @return
     */
    List<Line1301CommandDTO> getBindLine();

    /**
     * 获取绑定的线路
     * @return
     */
    List<Line1302CommandDTO> getBindLineStation();

    /**
     * 更新和新增时检查,同企业，名称是否重复
     * @param orgId
     * @param id
     * @param identify
     * @return
     */
    int checkIdentifyExist(@Param("orgId") String orgId, @Param("id") String id, @Param("identify") String identify);

    /**
     * 获取当前用户的所属企业包含下级企业的线路
     * @param orgId
     * @return
     */
    List<LineDTO> getLineByOrgId(@Param("orgId") String orgId);

    /**
     * 通过lineId获取车辆Id集合
     * @param lineId
     * @return
     */
    List<String> getVehicleIdByLineId(@Param("lineId") String lineId);

}
