package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.util.common.BaseQueryBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2021/2/23 14:48
 */
public interface TerminalTypeService {
    /**
     * 获取终端型号列表
     */
    Page<TerminalTypeInfo> getTerminalType(BaseQueryBean query) throws Exception;

    /**
     * 根据id查询终端类型信息
     */
    TerminalTypeInfo getTerminalTypeInfoById(String id) throws Exception;

    /**
     * 根据终端型号id查询是否被终端绑定
     */
    List<String> queryDeviceInfoByTerminalTypeId(String terminalTypeId);

    /**
     * 生成导入模板
     */
    void generateTypeTemplate(HttpServletResponse response) throws Exception;

    /**
     * 新增终端类型
     */
    void addTerminalType(TerminalTypeInfo info, String ipAddress) throws Exception;

    /**
     * 修改终端类型
     */
    void updateTerminalTypeInfo(TerminalTypeInfo info, String ipAddress) throws Exception;

    /**
     * 删除终端类型
     */
    Map<String, Object> deleteTerminalType(String id, String ipAddress) throws Exception;

    /**
     * 导入终端型号
     */
    Map<String, Object> importTerminalType(MultipartFile multipartFile, String ipAddress) throws Exception;

    /**
     * 根据终端厂商和终端型号校验数据库中是否有重复的记录
     */
    boolean verifyTerminalTypeByManufacturer(String terminalType, String terminalManufacturer) throws Exception;

    /**
     * 导出终端型号
     */
    void exportTerminalType(String title, int type, String fuzzyParam, HttpServletResponse response) throws Exception;

    /**
     * 获取终端厂商list
     */
    List<String> getTerminalManufacturer() throws Exception;

    /**
     * 查询全部的终端型号数据并存入缓存
     */
    void queryTerminalTypeSaveToRedis();

    List<Map<String, Object>> getTerminalTypeByName(String name);

    /**
     * 根据终端id和mac地址查询终端信息是否已存在mac地址
     * @param deviceId   终端id
     * @param macAddress mac地址
     * @return 是否存在
     */
    boolean repetitionMacAddress(String deviceId, String macAddress);
}
