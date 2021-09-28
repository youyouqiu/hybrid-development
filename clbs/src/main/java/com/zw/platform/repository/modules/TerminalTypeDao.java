package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.DeviceChannelSettingInfo;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjc
 * @Description:终端类型dao
 * @Date: create in 2021/2/23 14:52
 */
public interface TerminalTypeDao {
    /**
     * 获取终端型号列表(包含模糊搜索)
     */
    Page<TerminalTypeInfo> getTerminalTypeList(@Param("simpleQueryParam") String simpleQueryParam);

    /**
     * 根据终端型号通道id查询通道参数
     */
    List<DeviceChannelSettingInfo> getChannelParamByChannelId(@Param("channelId") List<String> channelId);

    /**
     * 根据终端型号id查询终端型号信息
     */
    TerminalTypeInfo getTerminalTypeInfo(String terminalTypeId);

    /**
     * 根据终端型号id查询哪些终端绑定了该终端型号
     */
    List<String> getDeviceIdByTerminalTypeId(String terminalTypeId);

    /**
     * 查询终端厂商
     */
    List<String> getTerminalManufacturer();

    /**
     * 根据id查询终端型号信息
     */
    List<TerminalTypeInfo> getTerminalTypeInfoByIds(@Param("typeIds") List<String> typeIds);

    /**
     * 删除终端型号关联的视频参数设置
     */
    void deleteChannelSet(@Param("settingId") List<String> settingId);

    /**
     * 删除终端型号
     */
    void deleteTerminalType(@Param("terminalTypeId") List<String> terminalTypeId);

    /**
     * 新增终端型号
     */
    void addTerminalType(TerminalTypeInfo info);


    /**
     * 修改终端型号
     */
    void updateTerminalType(TerminalTypeInfo info);

    /**
     * 新增终端型号视频参数设置
     */
    void addChannelSetParam(@Param("info") List<DeviceChannelSettingInfo> info);



    /**
     * 修改终端型号视频参数设置
     */
    void updateChannelSetParam(DeviceChannelSettingInfo settingInfo);

    /**
     * 查询全部的终端型号(不包括模糊搜索)
     */
    List<TerminalTypeInfo> getAllTerminalType();

    /**
     * 根据终端厂商名称查询终端型号
     * @param name
     * @return
     */
    List<Map<String, Object>> getTerminalTypeByFacturerName(@Param("name") String name);

    /**
     * 根据终端厂商和终端型号查询记录(用于验证同一终端厂商下终端型号是否重复)
     */
    List<String> getTerminalTypeByTerminalManufacturer(@Param("terminalType") String terminalType,
        @Param("terminalManufacturer") String terminalManufacturer);

    /**
     * 通过mac地址和终端ID查询是否有 存在的终端信息
     * @param macAddress MAC地址
     * @return list
     */
    List<DeviceInfo> getListByDeviceIdAndMacAddress(@Param("macAddress") String macAddress);

    /**
     * 批量新增终端型号
     */
    void addTerminalTypeToBatch(@Param("info") List<TerminalTypeInfo> info);

    List<TerminalTypeInfo> findTerminalTypeInfo(@Param("terminalTypeIds") Set<String> terminalTypeIds);

    /**
     * 根据终端型号id查询终端型号信息
     */
    List<VideoChannelSetting> getTerminalTypeChannelInfo(@Param("channelList") List<String> channelList);

    TerminalTypeInfo getTerminalTypeInfoBy(@Param("terminalType") String terminalType,
        @Param("terminalManufacturer") String terminalManufacturer);
}
