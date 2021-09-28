package com.zw.platform.domain.infoconfig.form;

import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * 信息配置form <p>Title: ConfigForm.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年7月26日上午11:02:22
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MONITOR_TYPE_VEHICLE = 0;
    public static final int MONITOR_TYPE_PEOPLE = 1;
    public static final int MONITOR_TYPE_THING = 2;

    @ExcelField(title = "brandID")
    private String brandID; // 车辆ID

    @ExcelField(title = "groupid")
    private String groupid; // 分组ID

    @NotEmpty(message = "【终端号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【终端号】格式错误，只能输入字母数字下划线！", regexp = "^[A-Za-z0-9_-]+$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "devices")
    private String devices; // 终端ID

    @ExcelField(title = "sims")
    private String sims;// SIMID

    @NotEmpty(message = "【车牌号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String brands;

    // private String devices;
    // private String sims;
    private String professionals;

    /**
     * 车辆id
     */
    private String vehicleid;

    /** 分组id */
    // private String groupId;
    /**
     * 终端ID
     */
    private String deviceid;

    /**
     * SIM卡ID
     */
    private String simcardid;

    /**
     * 外设ID
     */
    private String peripheralsId;

    /**
     * 服务周期ID
     */
    private String serviceLifecycleId;

    /**
     * 计费日期
     */
    private String billingDateStr;

    /**
     * 到期日期
     */
    private String durDateStr;

    /**
     * 报警状态
     */
    private Integer alarmStatus;

    /**
     * 报警时间
     */
    private Date alarmTime;

    /**
     * 在线状态
     */
    private Integer onlineStatus;

    /**
     * 离线时间
     */
    private Date offlineTime;

    /**
     * 在线时间
     */
    private Date onlineTime;

    /**
     * 最后车的经度
     */
    private Double longitude;

    /**
     * 最后车的纬度
     */
    private Double latitude;

    /**
     * 速度
     */
    private Integer speed;

    /**
     * 方向
     */
    private String orientation;

    /**
     * 位置
     */
    private String location;

    /**
     * 海拔高度
     */
    private Integer altitude;

    /**
     * 是否定位（0是未定位、1是定位）
     */
    private Integer isLocation;

    /**
     * GPS时间
     */
    private Date gpsTime;

    /**
     * 最后返回时间
     */
    private Date returnTime;

    /**
     * 0点火、1是熄火
     */
    private Integer accStatus;

    /**
     * 人ID
     */
    private String peopleId;

    /**
     * 物ID
     */
    private String thingId;

    /**
     * 从业人员
     */
    private String professionalsId;

    private String groupName;

    /**
     * 修改config时存放修改记录id
     */
    private String configId;

    private String configIdForBrand;

    private String configIdForDevice;

    private String configIdForSim;

    private String monitorType; // 监控对象类型

    private Integer plateColor; // 车牌颜色

    private Integer isVideo; // 是否视频

    private Integer checkEdit; // 校验是否新增（仅信息配置修改功能使用）0:新增，1：修改

    /***********用于信息配置修改页面修改相关基础信息使用***********
     * 修改后的监控对象名称（即车牌号）
     */
    private String editBrand;

    /**
     * 修改后的终端号
     */
    private String editDeviceNum;

    /**
     * 修改后的sim卡号
     */
    private String editSimNum;
    /********************************************************/

    private String carGroupName; //车辆所属企业名称

    private String carGroupId; //车辆所属企业id

    private String vehicleOwnerPhone; //车主电话

    private String vehicleType; //车辆类型

    private String deviceGroupName; //终端所属企业名称

    private String deviceGroupId; //终端所属企业id

    private String deviceType; //终端协议类型

    private String functionalType; //终端功能类型

    private String iccidSim; //sim卡iccid

    private String simParentGroupName; //sim卡所属企业名称

    private String simParentGroupId; //sim卡所属企业id

    private String operator; //运营商

    private String terminalTypeId; // 终端厂商、终端型号对应id

    /**
     * 物品
     */
    private String thingName;//物品名称

    private String thingType;//物品类型

    private Boolean sendFlag;//下发监控对象绑定关系信息标识
    /**
     * 车辆营运状态,默认营运
     */
    private Integer operatingState = 0;
    /**
     * 车辆密码  数字、字母   最长6个字符
     */
    @Size(max = 6, message = "【车辆密码】长度不能超过6！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【车辆密码】包含非法字符！", regexp = "^[a-zA-Z0-9]+$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    private String vehiclePassword;

    private String realId;

    /**
     * 入网标识（0代表未入网，1带表入网）
     */
    private Integer accessNetwork;

    public ConfigDTO convert() {
        ConfigDTO configDTO = new ConfigDTO();
        //监控对象信息变动信息
        configDTO.setId(this.brands);
        //修改后的监控对象名称，若是修改成另外一个存在的监控对象或监控对象未改变时该字段为空
        configDTO.setName(this.editBrand);
        configDTO.setVehiclePassword(this.vehiclePassword);

        //分组变动信息
        configDTO.setGroupId(this.groupid.replace("#", ","));
        configDTO.setGroupName(this.groupName.replace("#", ","));

        //终端变动信息,editDeviceNum修改成另外一个存在或未改变的终端时该字段为空
        configDTO.setDeviceId(this.devices);
        configDTO.setDeviceNumber(this.editDeviceNum);

        //终端手机号 editSimNum只有是修改层不存在的终端手机号时有值
        configDTO.setSimCardId(this.sims);
        configDTO.setSimCardNumber(this.editSimNum);
        configDTO.setRealSimCardNumber(this.realId);

        //计费周期
        configDTO.setBillingDate(this.billingDateStr);
        configDTO.setExpireDate(this.durDateStr);
        configDTO.setServiceLifecycleId(this.serviceLifecycleId);

        //从业人员
        configDTO.setProfessionalIds(this.professionalsId);
        if (StringUtils.isNotBlank(this.professionals)) {
            configDTO.setProfessionalNames(this.professionals.replace("#", ";"));
        }

        configDTO.setMonitorType(this.monitorType);
        configDTO.setConfigId(this.configId);
        return configDTO;
    }

    /**
     * 设置入网的状态信息
     * @param cfg
     * @return
     */
    public void assembleAccessNetwork(ConfigForm cfg) {
        //是否变更新监控对象以及sim卡和终端的id
        boolean changeBindIds = !(brands.equals(cfg.brands) && sims.equals(cfg.sims) && devices.equals(cfg.devices));
        //如果变更了需要设置为0
        if (changeBindIds) {
            accessNetwork = 0;
        }
    }

}
