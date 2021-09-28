/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.platform.util.common;

import lombok.extern.log4j.Log4j2;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>
 * Title: ReschedulableTimer.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * 
 * @author: Jiangxiaoqiang
 * @date 2016年9月19日下午4:07:48
 * @version 1.0
 */
@Log4j2
public class ReschedulableTimer extends Timer {

	private Runnable task;

	private TimerTask timerTask;

	private Timer timer = new Timer();

	public void schedule(Runnable runnable, long delay) {
		task = runnable;
		timerTask = new TimerTask(){
			public void run() {
				task.run();
			}
		};
		timer.schedule(timerTask, delay);
	}

	public void reschedule(long delay) {
		if(log.isDebugEnabled()) {
			log.info("rescheduling after seconds " + delay);
		}
		timerTask.cancel();
		timerTask = new TimerTask(){
			public void run() {
				task.run();
			}
		};
		timer.schedule(timerTask, delay);
	}
}
