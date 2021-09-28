package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.StrUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * zw_m_thing_info
 * @author zhangjuan 2020-09-28
 */
@Data
@NoArgsConstructor
public class ThingDO implements Serializable, ConverterDateUtil {

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编号
     */
    private String thingNumber;

    /**
     * 所属企业
     */
    private String orgId;

    /**
     * 类别
     */
    private String category;

    /**
     * 类型
     */
    private String type;

    /**
     * 品牌
     */
    private String label;

    /**
     * 型号
     */
    private String model;

    /**
     * 材料
     */
    private String material;

    /**
     * 物品重量
     */
    private Integer weight;

    /**
     * 规格
     */
    private String spec;

    /**
     * 制造商
     */
    private String manufacture;

    /**
     * 经销商
     */
    private String dealer;

    /**
     * 产地
     */
    private String place;

    /**
     * 生产日期
     */
    private Date productDate;

    /**
     * 物品照片名称
     */
    private String thingPhoto;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 物品图标id
     */
    private String thingIcon;

    /**
     * 0不显示、1显示
     */
    private Integer flag;

    /**
     * create_data_time
     */
    private Date createDataTime;
    /**
     * create_data_username
     */
    private String createDataUsername;
    /**
     * update_data_time
     */
    private Date updateDataTime;
    /**
     * update_data_username
     */
    private String updateDataUsername;

    public ThingDO(ThingDTO thingDTO) {
        if (StrUtil.isBlank(thingDTO.getId())) {
            this.id = UUID.randomUUID().toString();
            this.createDataTime = new Date();
            this.createDataUsername = SystemHelper.getCurrentUsername();
            thingDTO.setId(this.id);
        } else {
            this.id = thingDTO.getId();
            this.updateDataTime = new Date();
            this.updateDataUsername = SystemHelper.getCurrentUsername();
        }
        this.thingNumber = thingDTO.getName();
        this.name = thingDTO.getAlias();
        this.orgId = thingDTO.getOrgId();
        this.category = thingDTO.getCategory();
        this.type = thingDTO.getType();
        this.label = thingDTO.getLabel();
        this.model = thingDTO.getModel();
        this.material = thingDTO.getMaterial();
        this.weight = thingDTO.getWeight();
        this.spec = thingDTO.getSpec();
        this.manufacture = thingDTO.getManufacture();
        this.dealer = thingDTO.getDealer();
        this.place = thingDTO.getPlace();
        this.productDate = thingDTO.getProductDate();
        this.thingPhoto = thingDTO.getThingPhoto();
        this.remark = thingDTO.getRemark();
        this.flag = 1;
    }

    /**
     * 转换保存的图片地址
     */
    public void convertSavePhoto(String webServerUrl) {
        if (StringUtils.isNotBlank(thingPhoto) && thingPhoto.contains(webServerUrl)) {
            this.thingPhoto = this.thingPhoto.split(webServerUrl)[1];
        }
    }

}
