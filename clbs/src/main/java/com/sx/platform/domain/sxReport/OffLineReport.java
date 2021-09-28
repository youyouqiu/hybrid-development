package com.sx.platform.domain.sxReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


@Data
public class OffLineReport {
    private String vehicleId = "";			//车辆ID

    @ExcelField(title = "车牌号")
    private String brnad = ""; 		// 车牌号

    @ExcelField(title = "车辆颜色")
    private String color = ""; 				// 车辆颜色

    @ExcelField(title = "分组名称")
    private String assignmentName = ""; 	// 分组名称

    @ExcelField(title = "所属企业")
    private String groupName = "";          //所属企业

    @ExcelField(title = "车主")
    private String vehicleOwner;

    @ExcelField(title = "车主电话")
    private String vehicleOwnerPhone;

    @ExcelField(title = "终端编号")
    private String deviceNumber;            //终端编号

    @ExcelField(title = "终端手机号")
    private String simcardNumber;           //SIM卡号

    @ExcelField(title = "离线时长")
    private String offLineDay;				//离线时长

    @ExcelField(title = "最后在线时间")
    private String lastTime;				//最后在线时间

    @ExcelField(title = "最后在线位置")
    private String  lastLocation;			//最后在线位置





}
