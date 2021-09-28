package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/***
 @Author lijie
 @Date 2020/4/30 11:33
 @Description 批量修改车辆信息
 @version 1.0
 **/
@Data
public class BatchUpdateVehicleForm extends BaseFormBean implements ConverterDateUtil, Serializable {

    private String groupName;//组织名称

    private String groupId;//组织id

    private String aliases;//别名

    private String vehicleOwner;//车主

    private String vehicleCategoryId;//车辆类别

    private String vehicleCategoryName;//车辆类别名字

    private String codeNum;//识别码

    private String vehicleSubTypeId;//车辆子类型

    private String vehicleType; //车辆类型

    private String vehicleOwnerPhone;//车主电话

    private String phoneCheck;//电话是否检验

    private String vehicleColor;//车辆颜色

    private Integer plateColor;//车牌颜色

    private String fuelType;//燃料类型

    private String areaAttribute;//区域类型

    private String province;//省

    private String city;//市

    private String county;//区

    private String provinceId;//省域id

    private String cityId;//市域id

    private String category;//运营类别

    private String vehiclePurpose;//运营类别id

    private String purposeCodeNum;//运营类别识别码

    private String tradeName;//所属行业

    private Integer isStart;//车辆状态

    private String numberLoad;//核定载人数

    private String loadingQuality;//核定载质量

    private String vehicleInsuranceNumber;//车辆保险单号

    private String vehicleLevel;//车辆等级

    private String vehicleTechnologyValidity;//技术等级有效期

    private Date vehicleTechnologyValidityDate;//技术等级有效期

    private String maintainMileage;//保养里程数

    private String maintainValidity;//保养有效期

    private Date maintainValidityDate;//保养有效期

    private String vehiclePlatformInstallDate;//车台安装日期

    private Date vehiclePlatformInstall;//车台安装日期

    private String remark;//备注

    private Integer stateRepair;//维修状态

    public void setVehicleTechnologyValidity(String vehicleTechnologyValidity) {
        this.vehicleTechnologyValidity = vehicleTechnologyValidity;
        if (StringUtils.isNotEmpty(vehicleTechnologyValidity)) {
            this.vehicleTechnologyValidityDate =
                DateUtil.getStringToDate(vehicleTechnologyValidity, DateUtil.DATE_Y_M_D_FORMAT);
        }
    }

    public void setMaintainValidity(String maintainValidity) {
        this.maintainValidity = maintainValidity;
        if (StringUtils.isNotEmpty(maintainValidity)) {
            this.maintainValidityDate = DateUtil.getStringToDate(maintainValidity, DateUtil.DATE_Y_M_D_FORMAT);
        }
    }

    public void setVehiclePlatformInstallDate(String vehiclePlatformInstallDate) {
        this.vehiclePlatformInstallDate = vehiclePlatformInstallDate;
        if (StringUtils.isNotEmpty(vehiclePlatformInstallDate)) {
            this.vehiclePlatformInstall =
                DateUtil.getStringToDate(vehiclePlatformInstallDate, DateUtil.DATE_Y_M_D_FORMAT);
        }
    }

    public VehicleDTO convert() {
        VehicleDTO vehicle = new VehicleDTO();
        BeanUtils.copyProperties(this, vehicle);
        vehicle.setId(null);
        vehicle.setOrgId(this.groupId);
        vehicle.setOrgName(this.groupName);
        vehicle.setAlias(this.aliases);
        if (StringUtils.isNotBlank(this.numberLoad)) {
            vehicle.setNumberLoad(Integer.parseInt(this.numberLoad));
        }
        if (StringUtils.isNotBlank(this.vehicleTechnologyValidity)) {
            this.vehicleTechnologyValidityDate =
                DateUtil.getStringToDate(this.vehicleTechnologyValidity, DateUtil.DATE_Y_M_D_FORMAT);
            vehicle.setVehicleTechnologyValidity(this.vehicleTechnologyValidityDate);
        }
        if (StringUtils.isNotBlank(this.maintainValidity)) {
            this.maintainValidityDate = DateUtil.getStringToDate(this.maintainValidity, DateUtil.DATE_Y_M_D_FORMAT);
            vehicle.setMaintainValidity(this.maintainValidityDate);
        }

        if (StringUtils.isNotBlank(this.vehiclePlatformInstallDate)) {
            this.vehiclePlatformInstall =
                DateUtil.getStringToDate(this.vehiclePlatformInstallDate, DateUtil.DATE_Y_M_D_FORMAT);
            vehicle.setVehiclePlatformInstallDate(this.vehiclePlatformInstall);
        }

        return vehicle;
    }
}
