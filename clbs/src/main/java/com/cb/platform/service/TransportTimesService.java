package com.cb.platform.service;


import com.cb.platform.domain.TransportTimesEntity;
import com.cb.platform.domain.TransportTimesQuery;
import com.github.pagehelper.Page;
import com.zw.platform.basic.service.IpAddressService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TransportTimesService extends IpAddressService {

    boolean addTransport(TransportTimesEntity transportTimesEntity) throws Exception;

    TransportTimesEntity findById(String id) throws Exception;

    List<TransportTimesEntity> findByVids(List<String> list);

    Page<TransportTimesEntity> searchTransport(TransportTimesQuery query) throws Exception;

    boolean updateTransport(TransportTimesEntity entity, String ipaddress) throws Exception;

    boolean deleteTransport(List<String> list, String ipaddress) throws Exception;

    boolean deleteTransportById(TransportTimesEntity entity, String ipaddress) throws Exception;

    boolean insertList(List<TransportTimesEntity> list) throws Exception;

    boolean deleteByVehicleId(String vehicleId, String ip, String brand);

    /**
     * 根据品名ID获取数量
     *
     * @param list
     * @return
     */
    List<String> findByItemName(List<String> list);

    /**
     * 导入趟次管理
     * @param file
     * @return
     * @throws Exception
     */
    Map<String, Object> importTransport(MultipartFile file) throws Exception;

    /**
     * 根据车辆ID删除趟次记录
     * @param
     * @return
     */
    boolean deleteTransportByVid(List<String> list) throws Exception;

    /**
     * 导出
     * @param title
     * @param type
     * @param res
     * @param barnd
     * @return
     * @throws IOException
     */
    boolean export(String title, int type, HttpServletResponse res, String barnd) throws IOException, Exception;

    boolean download(HttpServletResponse res) throws IOException;
}
