package com.zw.platform.domain.multimedia.form;

import lombok.Data;

/**
 * 超长待机指令信息
 * @author hujun 2017/7/25 15:03
 *
 */
@Data
public class LongOrderForm {
	private String vid;//车辆id
    private Integer orderType;//指令类型
	private Integer serialNumber;//流水号
	
	//上报频率设置
	private String vehicleNumber;//车辆编号
    private Integer requitePattern;//上报模式(1按频率上报  2按定点上报)
    private Integer locationNumber;//上报时间间隔
    private Integer locationPattern;//定位模式
    private String requiteTime;//上报起始时间点
    private String locationTime;//定点时间
    private Integer locationTimeNum;//定点个数
    private String[] locationTimes;//定点时间集合
    //位置跟踪
    private Integer longValidity;//跟踪有效时间（时长,秒）
    private Integer longInterval;//时间间隔（秒）
    //透传消息
    private String longData;

    
}
