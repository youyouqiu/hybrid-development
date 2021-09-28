package com.zw.platform.domain.basicinfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 读卡器
 * @author liubq
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CardReaderInfo {

	private String id = ""; // 读卡器信息表
	private String cardReaderNumber = ""; // 读卡器编号
	private String cardReaderType = ""; // 读卡器类型
	private Integer isStart; // 启停状态
	private String manuFacturer = ""; // 设备厂商
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date factoryDate; // 出厂时间
	private String description = ""; // 描述 
	private Integer flag; // 1-显示；0-不显示（假删除）
	private Date createDateTime; // 创建时间
	private String createDateUsername = ""; // 创建人
	private Date updateDateTime; // 更新时间
	private String updateDateUsername = ""; // 更新人
	
}
