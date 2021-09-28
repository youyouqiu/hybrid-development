package com.zw.platform.controller.veermanagement;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.f3.WinchStatistics;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.service.winchstatistics.VeerStatisticalService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.ZipUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 正反转统计Controller
 * @author hujun 2017/7/6
 */
@Controller
@RequestMapping("/v/veerManagement/veerStatistics")
public class VeerStatisticsController {
    private static Logger log = LogManager.getLogger(VeerStatisticsController.class);

    private static final String LIST_PAGE = "vas/veerManagement/veerStatistics/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private VeerStatisticalService veerStatisticalService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SensorSettingsService sensorSettingsService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static Logger logger = LogManager.getLogger(VeerStatisticsController.class);

    Map<String, String[]> mapRedis = new HashMap<String, String[]>();

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询已绑定的车
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.findVehicleBrandByType(3, null);
            mav.addObject("vehicleList", StringUtils.isNotBlank(JSON.toJSONString(vehicleList))
                ? JSONArray.parseArray(JSON.toJSONString(vehicleList)) : "");
            return mav;
        } catch (Exception e) {
            log.error("获取正反转统计信息异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            if (!"".equals(startTime) && !"".equals(endTime)) {
                Date d = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(endTime, DATE_FORMAT);
                long startTimes = d.getTime() / 1000;
                long endTimes = d1.getTime() / 1000;
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_VEER.of(band, startTimes, endTimes, "");
                List<WinchStatistics> result = com.zw.platform.basic.core.RedisHelper.getListObj(
                        redisKey, (query.getStart() + 1), (query.getStart() + query.getLimit()));
                Page<WinchStatistics> results  = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
            return null;
        } catch (Exception e) {
            log.error("获取数据异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    //正转数据
    @RequestMapping(value = "/positiveList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getPositiveList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            if (!"".equals(startTime) && !"".equals(endTime)) {
                Date d = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(endTime, DATE_FORMAT);
                long startTimes = d.getTime() / 1000;
                long endTimes = d1.getTime() / 1000;
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_VEER.of(band, startTimes, endTimes, "-p");
                List<WinchStatistics> result = com.zw.platform.basic.core.RedisHelper.getListObj(
                        redisKey, (query.getStart() + 1), (query.getStart() + query.getLimit()));
                Page<WinchStatistics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
            return null;
        } catch (Exception e) {
            log.error("获取数据异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    //反转数据
    @RequestMapping(value = "/inversionList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getInversionList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            if (!"".equals(startTime) && !"".equals(endTime)) {
                Date d = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(endTime, DATE_FORMAT);
                long startTimes = d.getTime() / 1000;
                long endTimes = d1.getTime() / 1000;
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_VEER.of(band, startTimes, endTimes, "-i");
                List<WinchStatistics> result = com.zw.platform.basic.core.RedisHelper.getListObj(
                        redisKey, (query.getStart() + 1), (query.getStart() + query.getLimit()));
                Page<WinchStatistics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
            return null;
        } catch (Exception e) {
            log.error("获取数据异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/vehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTree(String type) {
        try {
            JSONArray result = vehicleService.vehicleTruckTree(type, true);
            return result.toJSONString();
        } catch (Exception e) {
            log.error("获取车辆树异常", e);
            return null;
        }
    }

    /**
     * 根据id查询正反转信息
     * @param band
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/getWinchInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getWinchInfo(String band, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            List<WinchStatistics> winchInfo = null;
            if (!"".equals(startTime) && !"".equals(endTime)) {
                winchInfo = veerStatisticalService.getInfoDtails(band, startTime, endTime);
            }
            String result = JSON.toJSONString(winchInfo);
            /*  String userName = SystemHelper.getCurrentUser().getUsername();
            veerStatisticalService.putToRedis(winchInfo, userName);*/
            //压缩数据
            result = ZipUtil.compress(result);
            msg.put("winchStatisics", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("正反转统计异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
