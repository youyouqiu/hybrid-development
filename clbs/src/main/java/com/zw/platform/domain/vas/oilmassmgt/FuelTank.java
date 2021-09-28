package com.zw.platform.domain.vas.oilmassmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * TODO  油箱实体
 * <p>Title: FuelTank.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年10月26日下午5:28:50
 * @version 1.0
 */
@Data
public class FuelTank implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  油箱id
	 */
	private String id;
	
	/**
	 *  油箱型号
	 */
	private String type;
	
	/**
	 * 油箱形状
	 */
	private String shape;
	private String shapeStr = "";
	
	/**
	 * 长度
	 */
	private String boxLength;
	
	/**
	 * 宽度
	 */
	private String width;
	
	/**
	 * 高度
	 */
	private String height;
	
	/**
	 * 壁厚
	 */
	private String thickness;
	
	/**
	 * 下圆角半径
	 */
	private String buttomRadius;
	
	/**
	 * 上圆角半径
	 */
	private String topRadius;
	
	/**
	 * 加油时间阈值
	 */
	private String addOilTimeThreshold;
	
	/**
	 * 加油量阈值
	 */
	private String addOilAmountThreshol;
	
	/**
	 * 漏油时间阈值
	 */
	private String seepOilTimeThreshold;
	
	/**
	 * 漏油量阈值
	 */
	private String seepOilAmountThreshol;
	
	/**
	 * 理论容积
	 */
	private String theoryVolume;
	
	/**
	 * 油箱容量
	 */
	private String realVolume;
	
	/**
	 * 标定组数
	 */
	private String calibrationSets;
	
	private Integer flag;

	private Date createDataTime;

	private String createDataUsername;

	private Date updateDataTime;

	private String updateDataUsername;
	
	
    
}
