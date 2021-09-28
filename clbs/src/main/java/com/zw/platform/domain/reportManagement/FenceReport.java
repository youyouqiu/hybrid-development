package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 围栏进出统计实体
 * @author tangshunyu
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FenceReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ExcelField(title = "监控对象")
	private String plateNumber;	//监控对象
	
	@ExcelField(title = "所属分组")
	private String assignmentName;	//所属分组
	
	@ExcelField(title = "车牌颜色")
	private String plateColor;		//车牌颜色
	
	@ExcelField(title = "围栏类型")
	private String fenceType;		//围栏类型
	
	@ExcelField(title = "围栏名称")
	private String fenceName;		//围栏名称
	
	@ExcelField(title = "进围栏次数")
	private Integer enterFenceTime;	//进围栏次数
	
	@ExcelField(title = "出围栏次数")
	private Integer outFenceTime;	//出围栏次数
	
	@ExcelField(title = "围栏内累计时长")
	private  String timeTotal = "";		//围栏内累计时长

}
