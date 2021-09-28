package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 读卡器管理查询
 * <p>Title: CardReaderInfoQuery.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年7月21日下午5:36:20
 * @version 1.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CardReaderInfoQuery extends BaseQueryBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 读卡器信息表
	 */
	private String id;
	/**
	 * 读卡器编号
	 */
	private String cardReaderNumber;
	/**
	 * 读卡器类型
	 */
	private String cardReaderType;
	/**
	 * 启停状态
	 */
	private Integer isStart;
	/**
	 * 设备厂商
	 */
	private String manuFacturer;
	/**
	 * 出厂时间
	 */
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date factoryDate;
	/**
	 * 描述 
	 */
	private String description;
	/**
	 * 1-显示；0-不显示（假删除）
	 */
	private Integer flag;
	/**
	 * 创建时间
	 */
	private Date createDateTime;
	/**
	 * 创建人
	 */
	private String createDateUsername;
	/**
	 * 更新时间
	 */
	private Date updateDateTime;
	/**
	 * 更新人
	 */
	private String updateDateUsername;
	
}
