package com.zw.adas.domain.platforminspection;

import java.util.Date;

import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wanxing
 * @Title: 平台巡检DTO
 * @date 2020/11/2415:05
 */
@Data
public class PlatformInspectionDTO {


    private String id;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String[] inspectionTypeArr = {"未知", "车辆运行监测巡检", "驾驶行为监测巡检 ", "驾驶员身份识别巡检"};

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String[] inspectionStatusArr = {"未知", "下发中", "下发成功 ", "终端响应超时", "终端离线,未下发"};

    @ExcelField(title = "所属组织")
    private String orgName;

    @ExcelField(title = "车牌号")
    private String brand;
    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 车牌颜色（0蓝、1黄、2白、3黑）
     */
    @Getter(AccessLevel.NONE)
    private Integer plateColor;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    /**
     * 巡检的类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检 3.驾驶员身份识别巡检)
     */
    private Integer inspectionType;

    @ExcelField(title = "巡检类型")
    private String inspectionTypeStr;

    /**
     * 巡检人
     */
    @ExcelField(title = "巡检人")
    private String inspector;

    /**
     * 巡检时间
     */
    @ExcelField(title = "巡检时间")
    private Date inspectionTime;

    /**
     * 巡检状态(1下发中，2下发成功，3终端响应超时，4终端离线 )
     */
    private Integer inspectionStatus;

    @ExcelField(title = "巡检下发状态")
    private String inspectionStatusStr;

    /**
     * 巡检结果的表的id
     */
    private String inspectionResultId;

    public String getInspectionTypeStr() {
        int index = inspectionType == null ? 0 : inspectionType;
        return inspectionTypeArr[index];
    }

    public String getInspectionStatusStr() {
        int index = inspectionStatus == null ? 0 : inspectionStatus;
        return inspectionStatusArr[index];
    }

    public String getPlateColorStr() {
        return PlateColor.getNameOrBlankByCode(plateColor);
    }
}
