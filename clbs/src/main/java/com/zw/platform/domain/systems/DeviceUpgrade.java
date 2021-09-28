package com.zw.platform.domain.systems;

import java.util.Date;
import java.util.List;

import com.zw.platform.domain.taskjob.TaskJobForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 终端包升级
 */
@Data
public class DeviceUpgrade extends TaskJobForm implements T808MsgBody {


    private List<String> monitorIds;
    //命令类型
    private Integer commandType;
    //批量升级个数
    private Integer batchUpgradeNum;
    //依赖版本编号
    private String dependSoftVersion;
    //设备型号
    private String equipmentModel;
    //厂家编号
    private String factoryNumber;
    //制造商id
    private String manufacturerId;
    //升级时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduleUpgradeTime;
    //升级文件的id
    private String upgradeFileId;
    //升级策略开关
    private Integer upgradeStrategyFlag;

    //软件版本
    private String softVersion;

    private String id;
    //上传时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;
    //文件名称
    private String fileName;
    //fastdfs
    private String url;
    //协议
    private Integer protocolType;

    private Integer flag;

    private String vehicleId;

    private Date createDataTime = new Date();
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    private String vid;
    private byte [] data;

    //下发属性
    public Integer getDataLen() {
        if (data == null) {
            return 0;
        }
        return data.length;
    }

    public Integer getType() {
        if (StringUtils.isEmpty(this.getUpgradeType())) {
            return 0;
        }
        return Integer.parseInt(this.getUpgradeType().toLowerCase().replace("0x", ""), 16);
    }

    public String getVersion() {
        return softVersion;
    }

    public String getProducerID() {
        return manufacturerId;
    }

    public Integer getVersionLen() {
        if (softVersion == null) {
            return 0;
        }
        return softVersion.length();
    }
}
