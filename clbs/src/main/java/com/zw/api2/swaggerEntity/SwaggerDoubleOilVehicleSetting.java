package com.zw.api2.swaggerEntity;

import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;


/**
 * TODO 油箱车辆关联表 <p>Title: OilVehicleSetting.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年10月26日上午9:12:41
 * @version 1.0
 */
@Data
public class SwaggerDoubleOilVehicleSetting implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 油量测量高度（高度1,高度2......）
     */
    @ApiParam(value = "油量测量高度（高度1,高度2......）",required = true)
    private String oilLevelHeights = "";

    /**
     * 油量值（值1,值2......）
     */
    @ApiParam(value = " 油量值（值1,值2......）",required = true)
    private String oilValues = "";


    /**
     * 油箱与车辆关联
     */
    @ApiParam(value = " 油箱1与车辆关联id",required = true)
    @NotEmpty(message = "【油箱1与车辆关联id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String id;


    /**
     * 油箱与车辆关联
     */
    @ApiParam(value = " 油箱2与车辆关联id")
    private String id2;

    /**
     * 油箱id
     */
    @NotEmpty(message = "【油箱1id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = " 油箱1id")
    private String oilBoxId;

    /**
     * 油箱id
     */
    @NotEmpty(message = "【油箱2id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = " 油箱2id")
    private String oilBoxId2;
}
