package com.zw.ws.entity.t808.video;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 终端休眠唤醒模式设置
 * @author: lifudong
 */
@Data
public class WakeUpParam {
	/**
	 * 休眠唤醒模式
	 * 按位设置:0表示不设置 1表示设置
     * Bit0:条件唤醒
     * Bit1:定时唤醒
     * Bit2:手动唤醒
	 */
	private Integer wakeupMode;
	
	/**
	 * 唤醒条件类型
	 * 休眠唤醒模式中bit0为1时此字段有效，否则置0；
     * 按位设置: 0表示不设置  1表示设置
     * Bit0:紧急报警
     * Bit1:碰撞侧翻报警
     * Bit2:车辆开门
	 */
	private Integer wakeupCondition;
	
	/**
	 * 定时唤醒日设置
     * 按位设置: 0表示不设置  1表示设置
     * Bit0:周一
     * Bit1:周二
     * Bit2:周三
     * Bit3:周四
     * Bit4:周五
     * Bit5:周六
     * Bit6:周日
	 */
	private Integer wakeupTime;
	
	/**
	 * 定时唤醒启用标志
	 * 按位设置: 0表示不设置  1表示设置
	 * Bit0:时间段1唤醒时间启用
     * Bit1:时间段2唤醒时间启用
     * Bit2:时间段3唤醒时间启用
     * Bit3:时间段4唤醒时间启用
	 */
	private Integer wakeupTimeFlag;
	
	/**
	 * 时间段1唤醒时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupTime1;
	
	/**
	 * 时间段1关闭时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupClose1;
	
	/**
	 * 时间段2唤醒时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupTime2;
	
	/**
	 * 时间段2关闭时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupClose2;
	
	/**
	 * 时间段3唤醒时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupTime3;
	
	/**
	 * 时间段3关闭时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupClose3;
	
	/**
	 * 时间段4唤醒时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupTime4;
	
	/**
	 * 时间段4关闭时间HHMM，取值时间00:00-23:59
	 */
	private String wakeupClose4;

}
