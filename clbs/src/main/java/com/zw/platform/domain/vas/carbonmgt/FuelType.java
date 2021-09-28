package com.zw.platform.domain.vas.carbonmgt;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 燃料类型实体
 * @author tangshunyu
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuelType  implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	@ExcelField(title="燃油类型")
	private String fuelType; //燃料类型
	//0:柴油 1：汽油 2：天然气
	private String fuelCategory; // 燃料类别
	private String describes; //燃料类型描述
	private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

}
