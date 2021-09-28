package com.zw.platform.repository.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.line.DirectionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 上下行Dao
 * @date 2020/10/915:41
 */
public interface DirectionManageDao extends CrudDao<DirectionDO>  {

    /**
     * 通过路线Id，查询
     * @param lineId
     * @return
     */
    List<DirectionDO> getListByLineId(String lineId);

    /**
     * 通过线路ID获取上下行Id
     * @param lineId
     * @return
     */
    List<String> getIdsByLineId(String lineId);

    /**
     * 通过路线Ids，查询
     * @param lineIds
     * @return
     */
    List<String> getIdsByLineIds(@Param("ids") Set<String> lineIds);
}
