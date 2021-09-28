package com.zw.platform.domain.vas.loadmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 油杆传感器导入Form
 * <p>Title: LoadSensorImportForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年12月22日上午10:20:11
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoadSensorImportForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
       

    /**
     * 传感器型号号
     */
    @ExcelField(title = "传感器型号*")
    private String sensorNumber = "";

    /**
     * 滤波系数
     */
    @ExcelField(title = "滤波系数*")
    private String filteringFactorStr = "";
    private String filterFactor;

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

    private Integer sensorType = 6 ;


}
