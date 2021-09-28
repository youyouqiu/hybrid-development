package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class ThingInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name; // 物品名称
    private String thingNumber; // 物品编号

    private Integer weight; // 物品重量

    private String remark;

    private String groupId;

    private String category;

    private String categoryName;

    private String type;

    private String typeName;

    private String label;

    private String model;

    private String material;

    private String spec;

    private String manufacture;

    private String dealer;

    private String place;

    private String productDate;

    /**
     * 物品照片名称
     */
    private String thingPhoto;

    private String groupName;// 分组名称

    private String assign; // 所属分组

    private String deviceNumber; // 终端

    private String deviceId; // 终端id

    private String deviceType; // 终端类型

    private String simcardNumber; // SIM卡
    private Integer flag;
    private Date createDataTime;//
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

    private String professionalsName; // 从业人员名称

    private String assignmentName; // 分组名称

    /**
     * 下发参数
     */

    public ThingInfo(ThingDTO thingDTO) {
        this.id = thingDTO.getId();
        this.name = thingDTO.getAlias();
        this.thingNumber = thingDTO.getName();
        this.weight = thingDTO.getWeight();
        this.remark = thingDTO.getRemark();
        this.groupId = thingDTO.getOrgId();
        this.category = thingDTO.getCategory();
        this.categoryName = thingDTO.getCategoryName();
        this.type = thingDTO.getType();
        this.typeName = thingDTO.getTypeName();
        this.label = thingDTO.getLabel();
        this.model = thingDTO.getModel();
        this.material = thingDTO.getMaterial();
        this.spec = thingDTO.getSpec();
        this.manufacture = thingDTO.getManufacture();
        this.dealer = thingDTO.getDealer();
        this.place = thingDTO.getPlace();
        if (thingDTO.getProductDate() != null) {
            this.productDate = DateUtil.getDayStr(thingDTO.getProductDate());
        }
        this.thingPhoto = thingDTO.getThingPhoto();
        this.groupName = thingDTO.getOrgName();
        this.assign = thingDTO.getGroupName();
        this.deviceNumber = thingDTO.getDeviceNumber();
        this.deviceId = thingDTO.getDeviceId();
        this.deviceType = thingDTO.getDeviceType();
        this.simcardNumber = thingDTO.getSimCardNumber();
        this.professionalsName = thingDTO.getProfessionalNames();
        this.assignmentName = thingDTO.getGroupName();
    }
}
