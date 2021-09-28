package com.zw.platform.service.functionconfig;

import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LinePassPoint;
import com.zw.platform.domain.functionconfig.TravelLine;

import java.util.List;

/**
 * 导航线路service
 * @author tangshunyu
 *
 */
public interface TravelLineService {
	/**
	 * 根据id查询途经点
	 * @param id
	 * @return
	 */
	List<LinePassPoint> getPassPointById(final String id);
	
	/**
	 * 根据id查询导航路线信息
	 * @param id
	 * @return
	 */
	TravelLine getTravelLineById(final String id);
	
	/**
	 * 根据id查询所有点的信息
	 * @param id
	 * @return
	 */
	List<LineContent> getAllPointsById(final String id);
}
