package com.zw.platform.domain.infoconfig.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * 信息配置导入的Form
 * <p>Title: ConfigImportForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2017年1月6日下午5:00:03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigImportForm extends ImportErrorData {

    private static final long serialVersionUID = 1L;
    /**
     * 信息列表导出
     */
    @ExcelField(title = "监控对象", required = true, repeatable = false)
    private String carLicense; // 车牌号

    @ExcelField(title = "监控对象类型", required = true)
    private String monitorType; // 监控对象类型

    @ExcelField(title = "车牌颜色(仅车辆时必填)")
    private String plateColorStr;

    @ExcelField(title = "所属企业", required = true)
    private String companyName;

    @ExcelField(title = "分组")
    private String groupName; // 组名

    @ExcelField(title = "终端号", required = true, repeatable = false)
    private String deviceNumber; // 终端编号

    @ExcelField(title = "通讯类型", required = true)
    private String deviceType; // 通讯类型

    @ExcelField(title = "终端厂商")
    private String terminalManufacturer;

    @ExcelField(title = "终端型号")
    private String terminalType;

    @ExcelField(title = "功能类型", required = true)
    private String functionalType = "";

    @ExcelField(title = "终端手机号", required = true, repeatable = false)
    private String simcardNumber; // SIM卡号

    @ExcelField(title = "真实SIM卡号")
    private String realId; // 真实sim卡号

    @ExcelField(title = "计费日期")
    private String billingDateStr;

    @ExcelField(title = "到期日期")
    private String durDateStr;

    @ExcelField(title = "从业人员")
    private String professionals; // 从业人员

    // @ExcelField(title = "外设")
    private String peripheralsId; // 外设

    /**
     * 车辆id
     */
    private String brandID;

    /**
     * 终端id
     */
    private String deviceID;

    /**
     * Sim卡id
     */
    private String simID;

    /**
     * 服务周期id
     */
    private String serviceLifecycleId = "";

    /**
     * 分组id，逗号相隔
     */
    private String assignIds = "";
    /**
     * 分组名称，逗号相隔
     */
    private String assignNames = "";
    // 分组所属组织id
    private String assignGroups = "";
    /**
     * 从业人员名称，逗号相隔
     */
    private String professionalNames = "";
    /**
     * 所属企业名称
     */
    private String groupId;

    private String vehicleType;

    private String vehType;

    private Integer plateColor;// 车牌颜色

    /**
     * 物品
     */
    private String category;// 物品类别

    private String categoryName;// 物品类别名称

    private String type;// 物品类型

    private String typeName;// 物品类型名称

    /**
     * 终端型号id
     */
    private String terminalTypeId;

    /**
     * 导入时使用  判断导入字段是否为空
     */
    private Integer isEmpty = 1;

    /**
     * uuid
     */
    @ApiParam(value = "uuid")
    private String id = UUID.randomUUID().toString();

    /**
     * 是否显示
     */
    @ApiParam(value = "是否显示")
    private Integer flag = 1;

    /**
     * 优先级
     */
    @ApiParam(value = "优先级")
    private Integer priority = 1;

    /**
     * 顺序
     */
    @ApiParam(value = "顺序")
    private Integer sortOrder = 1;

    /**
     * 是否可编辑
     */
    @ApiParam(value = "是否可编辑")
    private Integer editable = 1;

    /**
     * 是否可用
     */
    @ApiParam(value = "是否可用")
    private Integer enabled = 1;

    /**
     * 数据创建时间
     */
    @ApiParam(value = "数据创建时间")
    private Date createDataTime = new Date();

    /**
     * 创建者username
     */
    @ApiParam(value = "创建者username")
    private String createDataUsername;

    /**
     * 数据修改时间
     */
    @ApiParam(value = "数据修改时间")
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    @ApiParam(value = "修改者username")
    private String updateDataUsername;

    /**
     * 该条数据是否需要添加到数据库
     * (rowAdd & n) == 1 -> 需要添加到数据库中
     * 1 车
     * 2 人
     * 4 物
     * 8 终端
     * 16 sim卡
     */
    private int rowAdd;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
