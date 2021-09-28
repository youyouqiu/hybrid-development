package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.domain.basicinfo.form.DeviceForm;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.protocol.msg.t808.T808MsgHead;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 终端实体
 * @author wangying
 */
@Data
@NoArgsConstructor
public class DeviceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * JT/T808-2019
     */
    public static final int DEVICE_TYPE_2019 = 11;

    public static final int DEVICE_TYPE_2013 = 1;

    private String id; // Id

    private String deviceNumber; // 终端编号

    private String deviceName; // 设备名称

    private Integer isStart; // 启停状态

    /**
     * 设备类型:0:交通部JT/T808-2011(扩展);1:交通部JT/T808-2013;2:移为GV320;3:天禾;5:北斗天地协议;8:博实结;9:ASO;10:F3超长待机;11:808-2019
     */
    private String deviceType;

    private Integer channelNumber; // 通道数

    private Integer isVideo; // 是否视频

    private String barCode; // 条码

    private String manuFacturer; // 制造商

    private Integer flag; // 逻辑删除标志

    private Date installTime; //安装时间

    private Date createDataTime;    //创建时间

    private Date procurementTime; //采购时间

    private String remark;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    // add by liubq 2016/10/20
    private String groupId = ""; // 所属企业id

    private String groupName = ""; // 所属企业名称

    private String functionalType; // 设备功能类型

    private String authCode;  //鉴权码

    // 终端注册信息
    private String manufacturerId;    // 注册信息-制造商ID

    private String deviceModelNumber; // 注册信息-终端型号

    private String terminalManufacturer; //终端厂商

    private String terminalTypeId; //终端型号id

    private String terminalType;// 终端型号

    private String installTimeStr;

    /**
     * mac地址
     */
    private String macAddress;

    public static DeviceInfo fromForm(DeviceForm form) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(form.getId());
        deviceInfo.setDeviceNumber(form.getDeviceNumber());
        deviceInfo.setIsStart(form.getIsStart());
        deviceInfo.setIsVideo(form.getIsVideo());
        deviceInfo.setDeviceType(form.getDeviceType());
        deviceInfo.setFunctionalType(form.getFunctionalType());
        deviceInfo.setFlag(form.getFlag());
        deviceInfo.setCreateDataUsername(form.getCreateDataUsername());
        return deviceInfo;
    }

    /**
     * 协议判断
     * @param deviceType deviceType
     * @return true: JT/T808-2019; false:
     */
    public static Integer judgeProtocolType(Integer deviceType) {

        int protocolType = T808MsgHead.PROTOCOL_TYPE_2013;
        if (Objects.nonNull(deviceType)) {
            for (Integer type : ProtocolEnum.PROTOCOL_TYPE_808_2019) {
                if (type.equals(deviceType)) {
                    protocolType = T808MsgHead.PROTOCOL_TYPE_2019;
                }
            }
        }
        return protocolType;
    }

    /**
     * 根据设备类型获取协议类型
     * @param deviceTypeStr
     * @return
     */
    public static int getProtocolType(String deviceTypeStr) {
        return judgeProtocolType(Integer.parseInt(deviceTypeStr));
    }

    public static boolean isProtocol2019(String deviceType) {
        return getProtocolType(deviceType) == T808MsgHead.PROTOCOL_TYPE_2019;
    }

    public DeviceInfo(DeviceDTO device) {
        this.id = device.getId();
        this.deviceNumber = device.getDeviceNumber();
        this.deviceName = device.getDeviceName();
        this.isStart = device.getIsStart();
        this.deviceType = device.getDeviceType();
        this.channelNumber = device.getChannelNumber();
        this.isVideo = device.getIsVideo();
        this.barCode = device.getBarCode();
        this.manuFacturer = device.getManuFacturer();
        this.installTime = device.getInstallTime();
        this.procurementTime = device.getProcurementTime();
        this.remark = device.getRemark();
        this.groupId = device.getOrgId();
        this.groupName = device.getOrgName();
        this.functionalType = device.getFunctionalType();
        this.authCode = device.getAuthCode();
        this.manufacturerId = device.getManufacturerId();
        this.deviceModelNumber = device.getDeviceModelNumber();
        this.terminalManufacturer = device.getTerminalManufacturer();
        this.terminalTypeId = device.getTerminalTypeId();
        this.terminalType = device.getTerminalType();
        this.installTimeStr = device.getInstallTimeStr();
        this.macAddress = device.getMacAddress();
    }
}
