package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.DeviceListDTO;
import com.zw.platform.basic.dto.query.DeviceQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjc
 * @Description:终端service
 * @Date: create in 2020/10/22 9:10
 */
public interface DeviceService {

    /**
     * 新增终端
     * @param deviceDTO
     * @return
     */
    boolean add(DeviceDTO deviceDTO) throws BusinessException;

    /**
     * 修改终端
     * @param deviceDTO
     * @return
     */
    boolean updateNumber(DeviceDTO deviceDTO) throws BusinessException;

    /**
     * 删除终端信息
     * @param id
     * @return
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 查询列表信息
     * @param deviceQuery
     * @return
     */
    Page<DeviceListDTO> getListByKeyWord(DeviceQuery deviceQuery);

    /**
     * 获取终端的绑定默认信息
     * @param bindDTO
     * @return
     */
    DeviceDTO getDefaultInfo(ConfigDTO bindDTO);

    /**
     * 根据终端编号获取终端
     * @param number
     * @return
     */
    DeviceDTO getByNumber(String number);

    /**
     * 更新终端编号
     * @param id
     * @param deviceNumber
     * @return
     */
    boolean updateNumber(String id, String deviceNumber);

    /**
     * 检验是否重复:true代表已经存在，false代表不存在
     * @param deviceNumber
     * @param id
     * @return
     */
    boolean checkIsExist(String deviceNumber, String id);

    /**
     * 根据终端查询，终端的相关信息
     * @param id
     * @return
     */
    DeviceDTO findById(String id);

    /**
     * 导入
     * @param multipartFile
     * @return
     * @throws BusinessException
     */
    JsonResultBean importData(MultipartFile multipartFile) throws Exception;

    /**
     * 验证终端是否绑定
     * @param deviceNumber
     * @return
     */
    boolean checkIsBind(String deviceNumber);

    /**
     * 导出终端
     * @throws Exception
     */
    void exportDevice() throws Exception;

    /**
     * 批量删除
     * @param deviceIds
     * @return
     */
    boolean deleteBatch(List<String> deviceIds) throws BusinessException;

    /**
     * 获取指定企业下的终端id
     * @param orgId
     * @return
     */
    Set<String> getOrgDeviceIds(String orgId);

    /**
     * 根据终端号查询终端
     * @param deviceNumbers 终端号 若为空查询全部
     * @return 终端信息
     */
    List<DeviceDTO> getByDeviceNumbers(Collection<String> deviceNumbers);

    /**
     * 终端批量入库
     * @param deviceList 终端列表
     * @return 是否操作成功
     */
    boolean addByBatch(Collection<DeviceDO> deviceList);

    /**
     * 获取终端未绑定下拉框值
     * @param keyword    终端编号关键字
     * @param deviceType 协议类型
     * @return 未绑定终端信息
     */
    List<Map<String, String>> getUbBindSelectList(String keyword, Integer deviceType);

    List<DeviceDTO> getDeviceListByIds(Collection<String> deviceIds);

    boolean generateTemplate(HttpServletResponse response);

    /**
     *
     * @param deviceDTO
     */
    void updateDeviceManufacturer(DeviceDTO deviceDTO);
}
