package com.zw.platform.service.oilsubsidy;


import com.zw.platform.domain.oilsubsidy.station.StationDTO;
import com.zw.platform.domain.oilsubsidy.station.StationQuery;

import java.util.Collection;
import java.util.List;

/**
 * 站点管理服务层
 *
 * @author zhangjuan
 */
public interface StationManageService extends CrudService<StationDTO, StationQuery> {
    /**
     * 校验站点编号是否重复
     *
     * @param id     id 修改时必填，新增时为空
     * @param number 站点编号
     * @return true 重复 false 未重复
     */
    boolean checkNumRepeat(String id, String number);

    /**
     * 过滤站点被使用的ID和被使用的站点名称
     *
     * @param ids 站点ID集合
     * @return 被使用的站点名称，多个用“、”隔开
     */
    List<String> getUsedName(Collection<String> ids);

    /**
     * 获取所有站点，用于下拉
     * @return
     */
    List<StationDTO> getAll();
}
