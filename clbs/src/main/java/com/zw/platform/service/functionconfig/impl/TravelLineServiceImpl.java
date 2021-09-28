package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LinePassPoint;
import com.zw.platform.domain.functionconfig.TravelLine;
import com.zw.platform.repository.modules.TravelLineDao;
import com.zw.platform.service.functionconfig.TravelLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelLineServiceImpl implements TravelLineService {

	@Autowired
	private TravelLineDao travelDao;
	
	@Override
	public List<LinePassPoint> getPassPointById(final String id) {
		return travelDao.getPassPointById(id);
	}

	@Override
	public TravelLine getTravelLineById(final String id) {
		return travelDao.getTravelLineById(id);
	}

	@Override
	public List<LineContent> getAllPointsById(String id) {
		return travelDao.getAllPointsById(id);
	}

}
