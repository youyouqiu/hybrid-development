package com.zw.platform.domain.vas.loadmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/***
 @Author gfw
 @Date 2018/9/15 17:10
 @Description Ad导入form
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class AdValueForm implements Comparable<AdValueForm> {
    /**
     * AD序号
     */
    @ExcelField(title = "序号")
    private String adNumber;
    /**
     * AD值*
     */
    @ExcelField(title = "AD值*")
    private String adValue;
    /**
     * 实际载重
     */
    @ExcelField(title = "实际载重（Kg）*")
    private String adActualValue;

    @Override
    public int compareTo(AdValueForm o) {
        int i =Integer.parseInt(this.getAdValue())-Integer.parseInt(this.getAdValue());
        return i;
    }
}
