package com.zw.platform.domain.vas.f3;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/12.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TempStatistics implements Serializable {
    private String vehcileId;//车辆id

    @ExcelField(title = "监控对象")
    private String plateNumber;//车牌号

    private long locationTime;//位置数据更新时间

    /*
     * 用于excel导出
     */
    @ExcelField(title = "时间")
    private String stime;

    @ExcelField(title = "总里程")
    private Double gpsMile;//行驶里程

    @ExcelField(title = "速度")
    private Double speed;//速度

    private Double mileageSpeed = 0.0;// 里程传感器车速

    private Double mileageTotal = 0.0; // 里程传感器累积里程

    private String longtitude;//经度

    private String latitude;//纬度

    private String positionCoordinates;//位置坐标(纬度,经度)

    @ExcelField(title = "温度传感器温度1")
    private Double tempValueOne;//一号温度传感器温度值

    @ExcelField(title = "温度传感器温度2")
    private Double tempValueTwo;//二号温度传感器温度值

    @ExcelField(title = "温度传感器温度3")
    private Double tempValueThree;//三号温度传感器温度值

    @ExcelField(title = "温度传感器温度4")
    private Double tempValueFour;//四号温度传感器温度值

    @ExcelField(title = "温度传感器温度5")
    private Double tempValueFive;//五号温度传感器温度值
    
    private Integer tempHighLowOne; //一号温度传感器高低温报警
    
    private Integer tempHighLowTwo; //二号温度传感器高低温报警
    
    private Integer tempHighLowThree; //三号温度传感器高低温报警
    
    private Integer tempHighLowFour; //四号温度传感器高低温报警
    
    private Integer tempHighLowFive; //五号温度传感器高低温报警

    @ExcelField(title = "位置")
    private String address;//位置

}
