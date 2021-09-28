package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 途经点Form
 * @author tangshunyu
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LinePassPointForm extends BaseFormBean implements Serializable{

	private static final long serialVersionUID = 1L;

    @ExcelField(title = "lineId")
	private String lineId;	//行驶线路Id
	
    @ExcelField(title = "sortOrder")
	private Integer sortOrder;	//顺序
	
    @ExcelField(title = "longitude")
    private Double longitude;	//经度
	
    @ExcelField(title = "latitude")
    private Double latitude;	//纬度

}
