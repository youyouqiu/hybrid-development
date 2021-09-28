package com.zw.platform.basic.dto;

import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.form.ThingInfoForm;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 车辆信息DTO
 * @author zhangjuan
 * @date 2020/9/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ThingDTO extends BindDTO {

    @ApiParam(value = "物品重量")
    private Integer weight;

    @ApiParam(value = "类别")
    private String category;

    @ApiParam(value = "类别名称")
    private String categoryName;

    @ApiParam(value = "类型")
    private String type;

    @ApiParam(value = "类型名称")
    private String typeName;

    @ApiParam(value = "品牌")
    private String label;

    @ApiParam(value = "型号")
    private String model;

    @ApiParam(value = "材料")
    private String material;

    @ApiParam(value = "规格")
    private String spec;

    @ApiParam(value = "制造商")
    private String manufacture;

    @ApiParam(value = "经销商")
    private String dealer;

    @ApiParam(value = "产地")
    private String place;

    @ApiParam(value = "生产日期")
    private Date productDate;

    private String productDateStr;

    @ApiParam(value = "物品照片名称")
    private String thingPhoto;

    @ApiParam(value = "创建时间")
    private String createDataTimeStr;

    @ApiParam(value = "修改时间")
    private String updateDataTimeStr;

    /**
     * 数据创建时间
     */
    @ApiParam(value = "数据创建时间")
    private Date createDataTime;

    /**
     * 创建者username
     */
    @ApiParam(value = "创建者username")
    private String createDataUsername;

    /**
     * 数据修改时间
     */
    @ApiParam(value = "数据修改时间")
    private Date updateDataTime;

    /**
     * 修改者username
     */
    @ApiParam(value = "修改者username")
    private String updateDataUsername;

    /**
     * 后端接收前端参数转换
     * @param thingInfoForm
     * @return
     */

    /**
     * 后端接收前端参数转换
     * @param thingInfoForm
     * @return
     */
    public static ThingDTO getAddInstance(ThingInfoForm thingInfoForm) {
        ThingDTO thingDTO = new ThingDTO();
        basicInfo(thingInfoForm, thingDTO);
        thingDTO.setBindType(Vehicle.BindType.UNBIND);
        return thingDTO;
    }

    /**
     * 后端接收前端参数转换
     * @param thingInfoForm
     * @return
     */
    public static ThingDTO getUpdateInstance(ThingInfoForm thingInfoForm) {
        ThingDTO thingDTO = new ThingDTO();
        thingDTO.setId(thingInfoForm.getId());
        basicInfo(thingInfoForm, thingDTO);
        return thingDTO;
    }

    private static void basicInfo(ThingInfoForm thingInfoForm, ThingDTO thingDTO) {
        thingDTO.setName(thingInfoForm.getThingNumber());
        thingDTO.setAlias(thingInfoForm.getName());
        thingDTO.setOrgId(thingInfoForm.getGroupId());
        thingDTO.setOrgName(thingInfoForm.getGroupName());
        thingDTO.category = thingInfoForm.getCategory();
        thingDTO.type = thingInfoForm.getType();
        thingDTO.label = thingInfoForm.getLabel();
        thingDTO.model = thingInfoForm.getModel();
        thingDTO.material = thingInfoForm.getMaterial();
        thingDTO.weight = thingInfoForm.getWeight();
        thingDTO.spec = thingInfoForm.getSpec();
        thingDTO.manufacture = thingInfoForm.getManufacture();
        thingDTO.dealer = thingInfoForm.getDealer();
        thingDTO.place = thingInfoForm.getPlace();
        thingDTO.productDate = thingInfoForm.getProductDate();
        thingDTO.setRemark(thingInfoForm.getRemark());
        thingDTO.thingPhoto = thingInfoForm.getThingPhoto();
        thingDTO.setOrgName(thingInfoForm.getGroupName());
        //模糊搜索缓存维护
        thingDTO.setMonitorType(MonitorTypeEnum.THING.getType());
    }

    public ThingInfoForm convert() {
        ThingInfoForm thingInfoForm = new ThingInfoForm();
        thingInfoForm.setId(getId());
        thingInfoForm.setThingNumber(getName());
        thingInfoForm.setName(getAlias());
        thingInfoForm.setGroupId(getOrgId());
        thingInfoForm.setCategory(category);
        thingInfoForm.setType(type);
        thingInfoForm.setLabel(label);
        thingInfoForm.setModel(model);
        thingInfoForm.setMaterial(material);
        thingInfoForm.setWeight(weight);
        thingInfoForm.setSpec(spec);
        thingInfoForm.setManufacture(manufacture);
        thingInfoForm.setDealer(dealer);
        thingInfoForm.setPlace(place);
        thingInfoForm.setProductDate(productDate);
        thingInfoForm.setProductDateStr(productDateStr);
        thingInfoForm.setRemark(getRemark());
        thingInfoForm.setThingPhoto(thingPhoto);
        thingInfoForm.setGroupName(getOrgName());
        thingInfoForm.setAssign(getGroupName());
        thingInfoForm.setAssignId(getGroupId());
        thingInfoForm.setCreateDataTime(createDataTime);
        thingInfoForm.setUpdateDataTime(updateDataTime);
        thingInfoForm.setSimcardNumber(getSimCardNumber());
        thingInfoForm.setDeviceNumber(getDeviceNumber());

        return thingInfoForm;
    }

    public ThingInfo convertThingInfo() {
        ThingInfo thingInfo = new ThingInfo();
        thingInfo.setId(getId());
        thingInfo.setThingNumber(getName());
        thingInfo.setName(getAlias());
        thingInfo.setGroupId(getOrgId());
        thingInfo.setCategory(category);
        thingInfo.setType(type);
        thingInfo.setLabel(label);
        thingInfo.setModel(model);
        thingInfo.setMaterial(material);
        thingInfo.setWeight(weight);
        thingInfo.setSpec(spec);
        thingInfo.setManufacture(manufacture);
        thingInfo.setDealer(dealer);
        thingInfo.setPlace(place);
        thingInfo.setProductDate(productDateStr);
        thingInfo.setRemark(getRemark());
        thingInfo.setThingPhoto(thingPhoto);
        thingInfo.setCreateDataTime(createDataTime);
        thingInfo.setCreateDataUsername(createDataUsername);
        return thingInfo;
    }

    /**
     * 转换访问的图片地址
     * @param fastDFSClient
     */
    public void convertAccessPhoto(FastDFSClient fastDFSClient) {
        this.thingPhoto = fastDFSClient.getAccessUrl(thingPhoto);
    }
}
