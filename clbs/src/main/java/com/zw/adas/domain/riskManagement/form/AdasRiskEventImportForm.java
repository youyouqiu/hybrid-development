package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 风险事件Form
 * <p>Title: RodSensorImportForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: zjc
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventImportForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 风险事件
     */
    @ExcelField(title = "风险事件")
    private String riskEvent;

    /**
     * 风险类型
     */
    @ExcelField(title = "风险类型")
    private String riskType;

    /**
     * 描述
     */
    @ExcelField(title = "描述")
    private String description;

    /**
     * 事件id
     */
    private Integer functionId;

}
