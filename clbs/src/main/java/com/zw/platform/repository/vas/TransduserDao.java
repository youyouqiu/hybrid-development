package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.f3.TransduserManage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransduserDao {
    /**
     * 根据传感器类别查询传感器管理
     */
    Page<TransduserManage> findTransduserManageBytype(@Param(value = "transduserType") int transduserType,
                                                      @Param(value = "param") String param);

    /**
     * 根据传感器型号和类型查询传感器信息
     */
    TransduserManage getSensorByNumber(@Param(value = "sensorNumber") String sensorNumber,
                                       @Param(value = "sensorType") Integer sensorType);

    /**
     * 增加传感器管理
     */
    boolean addTransduserManage(TransduserManage transduserManage);

    /***
     * 修改传感器管理
     */
    boolean updateTransduserManage(TransduserManage transduserManage);

    /**
     * 删除传感器管理
     */
    boolean deleteTransduserManage(String id);

    /**
     * 根据传感器id查询绑定车辆条数
     */
    Integer checkBoundNumberById(String id);

    /**
     * 根据id查询传感器管理
     */
    TransduserManage findTransduserManageById(String id);

    /**
     * 批量新增传感器
     */
    boolean addTransduserByBatch(List<TransduserManage> list);

}
