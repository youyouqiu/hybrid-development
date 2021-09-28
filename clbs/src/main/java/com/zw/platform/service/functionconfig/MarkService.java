package com.zw.platform.service.functionconfig;

import com.zw.platform.domain.functionconfig.Mark;

/**
 * 
 * <p>
 * Title: 标注service
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
 * @author: wangying
 * @date 2016年8月8日下午2:21:25
 * @version 1.0
 */
public interface MarkService {

	/**
	 * 
		 * 
	 * @Title: 查询标注
	 * @param id
	 * @return
	 * @return Mark
	 * @throws @author
	 *             wangying
	 */
	Mark findMarkById(String id) ;

}
