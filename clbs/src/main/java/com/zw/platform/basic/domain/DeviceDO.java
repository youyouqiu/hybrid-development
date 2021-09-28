package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.imports.DeviceImportDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.util.common.Converter;
import com.zw.ws.common.PublicVariable;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Objects;

/**
 * @Author: zjc
 * @Description:终端类数据库对应类
 * @Date: create in 2020/10/22 9:15
 */
@Data
public class DeviceDO extends BaseDO {

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 终端名称
     */
    private String deviceName;

    /**
     * 启停状态0:停用,1:启用
     */
    private Integer isStart;

    /**
     * 设备类型:0:交通部2011版,1:交通部2013-F3版,2:GV320,3:TH,5:北斗天地协议,
     * 6:康凯斯有线,7:康凯斯无线,8:博实结,9:艾赛欧超长待机,10:F3超长待机设备,
     * 11:交通部JT/T808-2019,12:交通部JT/T808-2013(川标),13:交通部JT/T808-2013(冀标)
     */
    private String deviceType;
    /**
     * 通道数
     * 1,2,3,4;其中1:4,2:5,3:8,4:16
     */
    private Integer channelNumber;

    /**
     * 是否视频 0不是视频，1是视频
     */
    private Integer isVideo;
    /**
     * 条码
     */
    private String barCode;

    /**
     * 制造商
     */
    private String manuFacturer;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 终端安装时间(yyyy-MM-dd)
     */
    private Date installTime;

    /**
     * 是否注册（0：成功；1：车辆已被注册；2：数据库中无该车辆；）
     */
    private Integer isRegister;

    /**
     * 备注
     */
    private String remark;

    /**
     * 鉴权码
     */
    private String authCode;

    /**
     * 通道编码
     */
    private String channelId;

    /**
     * 1:简易型车机；2：行车记录仪； 3：对讲设备；4：手咪设备
     */
    private String functionalType;

    /**
     * 采购时间(yyyy-MM-dd)
     */
    private Date procurementTime;

    /**
     * 音频编码方式：0:保留；1：g.721;2:g722;3：g.723;4:g.728;8:g.726;19:aac;26:adpcma;
     */
    private Integer audioCode;

    /**
     * 视频编码方式：98：h.264；99：h.265
     */
    private Integer videoCode;

    /**
     * 音频声道数
     */
    private Integer audioChannel;

    /**
     * 音频采样率：0:8khz; 1:22.05khz; 2:44.1khz; 3:48khz;
     */
    private Integer audioSampling;

    /**
     * 音频采样位数：0:8位；1:16位；2:32位；
     */
    private Integer audioSamplingBit;

    /**
     * 音频帧长度
     */
    private Integer audioFpsLen;

    /**
     * 注册信息-制造商id
     */
    private String manufacturerId;

    /**
     * 注册信息-终端型号
     */
    private String deviceModelNumber;

    /**
     * 终端型号id
     */
    private String terminalTypeId;

    /**
     * 是否符合要求 0:否，1:是
     */
    private Integer complianceRequirements;

    /**
     * 安装单位
     */
    private String installCompany;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 联系方式
     */
    private String telephone;

    /**
     * mac地址
     */
    private String macAddress;

    /**
     * 企业id
     */

    private String orgId;

    /**
     * 新增实例转换
     * @param deviceDTO
     * @param userName
     * @return
     */
    public static DeviceDO getAddInstance(DeviceDTO deviceDTO, String userName) {
        DeviceDO deviceDO = new DeviceDO();
        //初始化相关参数
        deviceDTO.initAdd(userName);
        BeanUtils.copyProperties(deviceDTO, deviceDO);
        deviceDO.setCreateDataTime(new Date());
        return deviceDO;
    }

    /**
     * 修改实例转换
     * @param deviceDTO
     * @param userName
     * @return
     */
    public static DeviceDO getUpdateInstance(DeviceDTO deviceDTO, String userName) {
        DeviceDO deviceDO = new DeviceDO();
        //初始化相关参数
        deviceDTO.initUpdate(userName);
        BeanUtils.copyProperties(deviceDTO, deviceDO);
        return deviceDO;
    }

    public static DeviceDO getImportData(DeviceImportDTO data, TerminalTypeInfo terminalTypeInfo, String orgId) {
        DeviceDO deviceDO = new DeviceDO();
        BeanUtils.copyProperties(data, deviceDO);
        deviceDO.deviceType = PublicVariable.getDeviceTypeId(data.getDeviceType());
        deviceDO.functionalType = PublicVariable.getFunctionTypeId(data.getFunctionalType());
        deviceDO.isStart = PublicVariable.getDeviceStart(data.getIsStarts());
        //设置导入的  是否符合要求
        deviceDO.complianceRequirements = (StringUtils.isNotBlank(data.getComplianceRequirements()) && Objects
            .equals(data.getComplianceRequirements(), "否")) ? 0 : 1;

        final String installTimeStr = data.getInstallTime();
        if (StringUtils.isNotEmpty(installTimeStr)) {
            deviceDO.installTime = Converter.toDate(installTimeStr, "yyyy-MM-dd");
        }
        final String procurementTimeStr = data.getProcurementTime();
        if (StringUtils.isNotEmpty(procurementTimeStr)) {
            deviceDO.procurementTime = Converter.toDate(procurementTimeStr, "yyyy-MM-dd");
        }

        deviceDO.isVideo = terminalTypeInfo.getSupportVideoFlag();
        deviceDO.channelNumber = terminalTypeInfo.getChannelNumber();
        deviceDO.terminalTypeId = terminalTypeInfo.getId();
        deviceDO.orgId = orgId;
        //创建时间
        deviceDO.setCreateDataTime(new Date());
        deviceDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return deviceDO;
    }

}
