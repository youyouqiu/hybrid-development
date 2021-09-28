package com.zw.platform.service.reportManagement.impl;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.reportManagement.Ridership;
import com.zw.platform.domain.reportManagement.query.RidershipQuery;
import com.zw.platform.repository.modules.RidershipDao;
import com.zw.platform.service.reportManagement.RidershipService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhangsq
 * @since 2018/3/23 9:08
 */
@Service
public class RidershipServiceImpl implements RidershipService {

    @Autowired
    private RidershipDao ridershipDao;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void add(String vehicleId, String starTime, String endTime, Integer onTheTrain, Integer getOffTheCar)
        throws ParseException {
        Ridership ridership = new Ridership();
        Map<String, String> vehicleInfo = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId));
        // 解析缓存信息
        String brand = vehicleInfo.get("name");
        Integer plateColor = Integer.parseInt(vehicleInfo.get("plateColor"));
        String vehType = cacheManger.getVehicleType(vehicleInfo.get("vehicleType")).getType();
        String assign = vehicleInfo.get("groupName");
        String orgName = vehicleInfo.get("orgName");
        ridership.setVehicleId(vehicleId);
        ridership.setBrand(brand);
        ridership.setAssignmentName(assign);
        ridership.setGroupName(orgName);
        ridership.setVehicleType(vehType);
        ridership.setPlateColor(plateColor);
        ridership.setAboard(onTheTrain);
        ridership.setGetOff(getOffTheCar);
        ridership.setStartTime(DateUtils.parseDate(starTime, DATE_FORMAT));
        ridership.setEndTime(DateUtils.parseDate(endTime, DATE_FORMAT));
        ridership.setCreateDataUsername(SystemHelper.getCurrentUsername());
        ridership.setCreateDataTime(new Date());
        ridershipDao.insert(ridership);
    }

    @Override
    public List<Ridership> findByVehicleIdAndDate(RidershipQuery ridershipQuery) {
        List<Ridership> ridershipList = ridershipDao.findByVehicleIdAndDate(ridershipQuery);
        if (CollectionUtils.isNotEmpty(ridershipList)) {
            Map<String, BindDTO> configLists = VehicleUtil.batchGetBindInfosByRedis(ridershipQuery.getVehicleIds());
            for (Ridership ridership : ridershipList) {
                BindDTO configList = configLists.get(ridership.getVehicleId());
                ridership.setAssignmentName(configList.getGroupName());
                if (ridership.getPlateColor() != null) {
                    ridership.setPlateColorStr(VehicleUtil.getPlateColorStr(ridership.getPlateColor().toString()));
                }
                ridership.setStartTimeStr(DateUtil.getDateToString(ridership.getStartTime(), null));
                ridership.setEndTimeStr(DateUtil.getDateToString(ridership.getEndTime(), null));
            }
        }
        RedisKey redisKey = HistoryRedisKeyEnum.PASSENGER_FLOW_REPORT.of(SystemHelper.getCurrentUsername());
        RedisHelper.delete(redisKey);
        if (CollectionUtils.isNotEmpty(ridershipList)) {
            RedisHelper.addToList(redisKey, ridershipList);
            RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        }
        return ridershipList;
    }

    @Override
    public boolean export(String o, int i, HttpServletResponse res) throws Exception {
        RedisKey redisKey = HistoryRedisKeyEnum.PASSENGER_FLOW_REPORT.of(SystemHelper.getCurrentUsername());
        List<Ridership> riderships = RedisHelper.getList(redisKey, Ridership.class);
        return ExportExcelUtil
            .export(new ExportExcelParam(o, i, riderships, Ridership.class, null, res.getOutputStream()));
    }

}
