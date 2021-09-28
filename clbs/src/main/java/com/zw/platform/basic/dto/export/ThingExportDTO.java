package com.zw.platform.basic.dto.export;

import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.ws.common.PublicVariable;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * zw_m_thing_info
 *
 * @author zhangjuan 2020-09-28
 */
@Data
public class ThingExportDTO implements Serializable, ConverterDateUtil {

    private static final long serialVersionUID = 1L;



    @ExcelField(title = "物品编号", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String thingNumber;


    @ExcelField(title = "物品名称", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String name;



    @ExcelField(title = "所属企业", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String orgName;



    @ExcelField(title = "分组", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String groupName;

    @ExcelField(title = "终端号", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String deviceNumber;

    @ExcelField(title = "终端手机号", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String simCardNumber;


    /**
     * 类别
     */
    @ExcelField(title = "物品类别", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String category;

    /**
     * 类型
     */
    @ExcelField(title = "物品类型", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String type;

    /**
     * 品牌
     */
    @ApiParam(value = "品牌")
    @ExcelField(title = "品牌", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String label;

    /**
     * 型号
     */
    @ApiParam(value = "型号")
    @ExcelField(title = "型号", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String model;

    /**
     * 材料
     */
    @ApiParam(value = "主要材料")
    @ExcelField(title = "主要材料", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String material;
    @ApiParam(value = "物品重量")
    @ExcelField(title = "重量", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private Integer weight;

    /**
     * 规格
     */
    @ApiParam(value = "规格")
    @ExcelField(title = "规格", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String spec;

    /**
     * 制造商
     */
    @ApiParam(value = "制造商")
    @ExcelField(title = "制造商", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String manufacture;

    /**
     * 经销商
     */
    @ApiParam(value = "经销商")
    @ExcelField(title = "经销商", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String dealer;

    /**
     * 产地
     */
    @ApiParam(value = "产地")
    @ExcelField(title = "产地", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String place;



    @ExcelField(title = "生产日期", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String productDateStr;
    @ApiParam(value = "备注")
    @ExcelField(title = "备注", groups = { PublicVariable.IMPORT_THING_FIELDS })
    private String remark;


    private String createDataTimeStr;

    private String assignmentName;

    private String assignmentId;

    private String assignmentGroupId;

    /**
     * 物品照片名称
     */
    private String thingPhoto;

    private String categoryName;

    private String typeName;

    private String assignId;

}
