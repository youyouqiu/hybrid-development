package com.zw.platform.repository.modules;

import com.zw.platform.domain.reportManagement.Ridership;
import com.zw.platform.domain.reportManagement.query.RidershipQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhangsq
 * @date 2018/3/23 8:52
 */
public interface RidershipDao {

    /**
     * 插入客流报表
     *
     * @param ridership
     */
    void insert(Ridership ridership);

    List<Ridership> findByVehicleIdAndDate(@Param("ridershipQuery") RidershipQuery ridershipQuery);


}
