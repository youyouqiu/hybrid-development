package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.IconDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 监控对象个性化图标设置
 * @author zhangjuan
 */
public interface MonitorIconService {
    /**
     * 监控对象图标更新
     * @param iconId      图标ID
     * @param monitorList 监控对象列表 id:监控对象ID type：监控对象类型 people/vehicle/thing
     * @return 是否更新成功
     */
    boolean update(String iconId, List<Map<String, String>> monitorList);

    /**
     * 删除监控对象的个性图标，使用监控对象默认图标
     * @param monitorList 监控对象列表
     * @return 是否更新成功
     */
    boolean delete(List<Map<String, String>> monitorList);

    /**
     * 获取指定监控对象的图标
     * 监控对象个性化图标存在：使用个性化图标
     * 监控对象个性化图标不存在：人和物使用默认的图标，车辆使用的是车辆类别的图标
     * @param monitorIds 监控对象ID集合
     * @return 监控对象id-图标名称Map
     */
    Map<String, String> getByMonitorId(Collection<String> monitorIds);

    /**
     * 获取监控对象图标
     * @param monitorId 监控对象ID
     * @return 监控对象图标名称
     */
    String getMonitorIcon(String monitorId);

    /**
     * 获取用户权限下监控对象监控对象图标
     * 监控对象个性化图标存在：使用个性化图标
     * 监控对象个性化图标不存在：人和物使用默认的图标，车辆使用的是车辆类别的图标
     * @return 监控对象id-图标名称Map
     */
    Map<String, String> getUserOwnMonitorIcon();

    /**
     * 获取图标路径
     * @param request request
     * @return 图标路径
     */
    String getIconPath(HttpServletRequest request);

    /**
     * 获取所有的图标列表
     * @return 图标列表
     */
    List<IconDTO> getIconList();

    /**
     * 图标删除
     * @param iconId  图标ID
     * @param request 删除请求
     * @return 删除结果
     */
    boolean deleteIcon(String iconId, HttpServletRequest request);

    /**
     * 上传图标
     * @param request 上传请求
     * @param file    上传文件
     * @return 上传结果
     */
    Map<String, Object> uploadImg(HttpServletRequest request, MultipartFile file);

    /**
     * 膝盖图标方向缓存
     * @param flag true false
     * @return 是否更新成功
     */
    boolean updateIconDirection(String flag);

}
