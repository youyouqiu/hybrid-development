package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;


/***
 @Author zhengjc
 @Date 2019/1/31 15:16
 @Description 新增物品form表单接收实体
 @version 1.0
 **/

@Data
public class SwaggerThingInfoForm {

    @ApiParam(name = "id", value = "物品uuid", required = true)
    private String id;

    /**
     * 物品编号
     */
    @ApiParam(name = "thingNumber", value = "物品编号", required = true)
    @NotEmpty(message = "【物品编号】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【物品编号】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String thingNumber;

    /**
     * 物品名称
     */
    @ApiParam(name = "name", value = "物品名称")
    private String name;

    /**
     * 所属企业名称
     */
    @ApiParam(name = "groupName", value = "所属企业名称", required = true)
    private String groupName;

    /**
     * 所属企业Id
     */
    @ApiParam(name = "groupId", value = "所属企业Id", required = true)
    private String groupId;

    /**
     * 物品类别
     */
    @ApiParam(name = "category", value = "物品类别", required = true)
    private String category;

    /**
     * 物品类型
     */
    @ApiParam(name = "name", value = "物品类型", required = true)
    private String type;

    /**
     * 品牌
     */
    @ApiParam(name = "label", value = "品牌")
    private String label;

    /**
     * 型号
     */
    @ApiParam(name = "model", value = "型号")
    private String model;

    /**
     * 主要材料
     */
    @ApiParam(name = "material", value = "主要材料")
    private String material;

    /**
     * 物品重量
     */
    @ApiParam(name = "weight", value = "物品重量")
    @Max(value = 999999999, message = "【物品重量】长度不超过9！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value = 0, message = "不能小于0", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer weight;

    /**
     * 规格
     */
    @ApiParam(name = "spec", value = "规格")
    private String spec;

    /**
     * 制造商
     */
    @ApiParam(name = "manufacture", value = "制造商")
    private String manufacture;

    /**
     * 经销商
     */
    @ApiParam(name = "name", value = "物品名称")
    private String dealer;

    /**
     * 产地
     */
    @ApiParam(name = "place", value = "产地")
    private String place;

    /**
     * 生产日期
     */
    @ApiParam(name = "productDate", value = "生产日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date productDate;

    /**
     * 备注
     */
    @ApiParam(name = "remark", value = "备注")
    private String remark;

    /**
     * 物品照片名称
     */
    @ApiParam(name = "thingPhoto", value = "物品照片名称")
    private String thingPhoto;

}
