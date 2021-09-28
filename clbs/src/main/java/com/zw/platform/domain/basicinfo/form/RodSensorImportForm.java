package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 油杆传感器导入Form
 * <p>Title: RodSensorImportForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年12月22日上午10:20:11
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RodSensorImportForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
       
    /**
     * 传感器品牌
     */
    private String sensorBrand = "";
    /**
     * 传感器型号号
     */
    @ExcelField(title = "传感器型号")
    private String sensorNumber = "";

    /**
     * 传感器长度
     */
    @ExcelField(title = "传感器长度(mm)")
    private String sensorLength = "";
    
    /**
     * 量程
     */
    private String measuringRange;
    
    /**
     * 上盲区-add by liubq 2016-11-16
     */
    private String upperBlindZone;
    
    /**
     * 下盲区-add by liubq 2016-11-16
     */
    private String lowerBlindArea;
       
    /**
     * 滤波系数
     */
    @ExcelField(title = "滤波系数*")
    private String filteringFactorStr = "";
    private String filteringFactor;    

    /**
     * 波特率
     */
    @ExcelField(title = "波特率*")
    private String baudRateStr="";
    private String baudRate;

    /**
     * 奇偶效验
     */
    @ExcelField(title = "奇偶效验*")
    private String oddEvenCheckStr = "";
    private Short oddEvenCheck;


    /**
     * 补偿使能
     */
    @ExcelField(title = "补偿使能*")
    private String compensationCanMakeStr = "";
    private Short compensationCanMake;
    
    @ExcelField(title = "备注")
    private String remark;


    /**
     * 设备厂商
     */
    //@ExcelField(title = "设备厂商")
    private String manuFacturer;

    /**
     * 启停状态
     */
    //@ExcelField(title = "启停状态")
    private String isStartStr = "";
    private Short isStart=1;

    /**
     * 出厂时间
     */
    //@ExcelField(title = "出厂时间")
    private String factoryDateStr = "";
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date factoryDate;
  
    /**
     * 标定数量
     */
    //@ExcelField(title = "标定数量")
    private String calibrationNumber;

    /**
     * 系数K
     */
    //@ExcelField(title = "系数K")
    private String factorK;

    /**
     * 系数B
     */
    //@ExcelField(title = "系数B")
    private String factorB;
    
   

    /**
     * 上传间隔
     */
    //@ExcelField(title = "上传间隔")
    private Short uploadInterval;

    /**
     * 描述
     */
    //@ExcelField(title = "描述")
    private String description;      
}
