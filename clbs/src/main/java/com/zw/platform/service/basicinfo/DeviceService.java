package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.query.DeviceQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 设备管理Service
 * @author wangying
 */
public interface DeviceService {

    /**
     * 生成导入模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    /**
         * @param id
     * @return DeviceInfo
     * @Title: 根据id查询终端
     * @author wangying
     */
    DeviceInfo findDeviceById(String id) throws Exception;

    /**
     * 根据终端编号查询终端信息
     * @return DeviceInfo
     * @throws @Title: findDeviceByDeviceNumber
     * @author Liubangquan
     */
    DeviceInfo findDeviceByDeviceNumber(String deviceNumber) throws Exception;

    /**
     * 根据当前登录用户查询其组织下的设备，如果绑定了车的需显示车牌号
     * @author Fan Lu
     * @return
     */
    Page<Map<String, Object>> findDeviceByUser(DeviceQuery query);


    /**
     * 查询设备所属组织
     * @param id 设备id
     * @return 设备信息及其组织
     * @author Fan Lu
     */
    Map<String, Object> findDeviceGroupById(String id) throws Exception;

    DeviceInfo findByDevice(String deviceNumber) throws Exception;

    /**
     * 根据终端编号查询终端组织id
     */
    String findGroupIdByNumber(String deviceNumber);

}
