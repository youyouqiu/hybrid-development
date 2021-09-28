package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 电子围栏线-FORM
 * @author wangjianyu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LineForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "【线路名称】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【线路名称】长度不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "name")
    private String name; // 名称

    @Size(max = 100, message = "【描述】长度不能超过100个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "description")
    private String description; // 描述

    @NotEmpty(message = "【偏移量】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "width")
    private Integer width; // 线路宽度

    @ExcelField(title = "type")
    private String type; // 类型

    private String groupId; // 所属企业

    private String typeId; // 围栏种类id

    /**
     * 新增或修改线路标识:0-新增；1-修改
     */
    @NotEmpty(message = "【新增或修改线路标识】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【新增或修改线路标识】填值错误！", regexp = "^[0-1]$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String addOrUpdateLineFlag = "0";

    /**
     * 被修改的线路id
     */
    private String lineId = "";

    /**
     * 点序号集合
     */
    @NotEmpty(message = "【点序号集合】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String pointSeqs;
    /**
     * 经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)
     */
    @NotEmpty(message = "【经度集合】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String longitudes;
    /**
     * 纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)
     */
    @NotEmpty(message = "【纬度集合】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String latitudes;

    /**
     * 围栏的每个点信息
     */
    private String pointSeq;
    private String longitude;
    private String latitude;
    private String pointType = "0"; // 点类型  0：围栏线点   1：监控点    2：监控线段
    private int segmentId;

    /**
     * 添加轨迹时用到的字段
     */
    private String lineName; // 线路名称
    private String shapeId; // 围栏与车辆关联表对应的线路id
    private String lineType; // 线路类型
    private String lineDescription; // 线路描述
    private String lineWidth; // 线路宽度
}
