package com.zw.platform.repository.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.station.StationDO;
import com.zw.platform.domain.oilsubsidy.station.StationDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 公交线路站点信息表操作
 *
 * @author zhangjuan
 * @date 2020/10/09
 */
public interface StationManageDao {
    /**
     * 根据站点编号获取站点信息
     *
     * @param number 编号
     * @return 站点信息
     */
    StationDO getByNumber(@Param("number") String number);

    /**
     * 插入站点信息
     *
     * @param stationDO stationDO
     * @return 是否插入成功
     */
    boolean insert(StationDO stationDO);

    /**
     * 根据ID获取站点信息
     *
     * @param id id
     * @return 站点信息
     */
    StationDO getById(@Param("id") String id);

    /**
     * 更新站点信息
     *
     * @param stationDO stationDO
     * @return 是否更新成功
     */
    boolean update(StationDO stationDO);

    /**
     * 根据ID获取被使用的站点信息
     *
     * @param ids 站点ID集合
     * @return 被使用的站点信息
     */
    List<StationDO> getUsedByIds(@Param("ids") Collection<String> ids);

    /**
     * 根据ID批量获取站点新
     *
     * @param ids ids
     * @return 站点信息
     */
    List<StationDO> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 批量删除站点信息
     *
     * @param ids 站点Id集合
     * @return 删除数量
     */
    int deleteBatch(@Param("ids") List<String> ids);

    /**
     * 根据关键字查询站点信息
     *
     * @param keyword 关键字，可为空
     * @return 符合条件的列表
     */
    List<StationDTO> getByKeyword(String keyword);
}
