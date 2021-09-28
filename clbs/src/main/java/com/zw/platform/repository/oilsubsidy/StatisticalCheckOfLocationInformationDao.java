package com.zw.platform.repository.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationDO;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定位信息DAO层
 *
 * @author XK
 * @date 2020/10/10
 */
public interface StatisticalCheckOfLocationInformationDao {

    /**
     * 插入定位信息
     *
     * @param subsidyLocationInformationDO subsidyLocationInformationDO
     * @return 是否插入成功
     */
    boolean insert(OilSubsidyLocationInformationDO subsidyLocationInformationDO);

    /**
     * 根据对接码组织和时间获取定位信息
     *
     * @param orgIds 对接码组织
     * @return 定位信息
     */
    List<OilSubsidyLocationInformationDTO> getByOrgIdAndTime(@Param("orgIds") List<String> orgIds,
        @Param("startTime") String startTime, @Param("endTime") String endTime);
}
