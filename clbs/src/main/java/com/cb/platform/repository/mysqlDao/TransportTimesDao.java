package com.cb.platform.repository.mysqlDao;


import com.cb.platform.domain.TransportTimesEntity;
import com.cb.platform.domain.TransportTimesExportEntity;
import com.cb.platform.domain.TransportTimesQuery;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TransportTimesDao {

    boolean addTransport(@Param("entity")TransportTimesEntity transportTimesEntity);

    TransportTimesEntity findById(@Param("id") String id);

    Page<TransportTimesEntity> searchTransport(@Param("query")TransportTimesQuery query,@Param("list")List<String> list);

    boolean updateTransport(@Param("entity")TransportTimesEntity entity);

    boolean deleteTransport(@Param("list") List<String> list);

    Integer deleteByVehicleId(@Param("vehicleId") String vehicleId);

    boolean insertList(@Param("list") List<TransportTimesEntity> list);

    List<TransportTimesExportEntity> exportTransport(@Param("brand")String brand,@Param("list")List<String> list);

    /**
     * 根据车辆ID删除趟次记录
     * @param list
     * @return
     */
    boolean deleteTransportByVid(@Param("list")List<String> list);

    List<TransportTimesEntity> findByVehiclesId(@Param("list")List<String> list);

    /**
     * 根据品名ID获取数量
     * @param list
     * @return
     */
    List<String> findByItemName(@Param("list") List<String> list);

    List<String> findByIdList(@Param("list") List<String> list);

    /**
     * 获取危险品类别
     * @return
     */
    List<String> findDangerTypeList();

}
