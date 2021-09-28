package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouzongbo on 2018/4/17 9:47
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleSubTypeQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 类别Id
     */
    private String vehicleCategory;

    private String vehicleType;

    private String description;

    private String pid;

    private String icoId;

    private String vehicleSubtypes;
    private String icoName;
    /**
     * 行驶方式（0：自行；1：运输）
     */
    private String drivingWay;
    private Short flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
}
