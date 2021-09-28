/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.repository.modules;

import com.zw.ws.entity.t808.location.HBaseGpsInfo;

/**
 * <p>Title: OilDao.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Jiangxiaoqiang
 * @date 2016年9月21日下午4:12:06
 * @version 1.0
 * 
 */
public interface OilDao {
	
	/**
	 * 保存油耗数据
	 *TODO
	 * @Title: save
	 * @param hBaseGpsInfo
	 * @return void
	 * @throws
	 * @author Jiangxiaoqiang
	 */
	void saveOil(final HBaseGpsInfo hBaseGpsInfo);
}
