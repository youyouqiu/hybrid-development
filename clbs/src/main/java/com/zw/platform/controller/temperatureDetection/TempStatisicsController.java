package com.zw.platform.controller.temperatureDetection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.f3.TempStatistics;
import com.zw.platform.service.tempStatistics.TempStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.ZipUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 温度统计controller Created by Administrator on 2017/7/12.
 */
@RequestMapping("/v/temperatureDetection/temperatureStatistics")
@Controller
public class TempStatisicsController {

    private static Logger logger = LogManager.getLogger(TempStatisicsController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private TempStatisticsService tempStatisticsService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 温度传感器统计
     * @param startTime
     * @param endTime
     * @param band
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/statisics", method = RequestMethod.POST)
    public JsonResultBean findtempStatisics(String startTime, String endTime, String band) {
        try {
            JSONObject msg = new JSONObject();
            List<TempStatistics> tempStatistics = null;
            if (!"".equals(startTime) && !"".equals(endTime) && !"".equals(band)) {
                tempStatistics = tempStatisticsService.findVehicleDataByBrand(startTime, endTime, band);
            }
            String result = JSON.toJSONString(tempStatistics);
            // 压缩数据
            result = ZipUtil.compress(result);
            msg.put("tempStatisics", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("温度传感器统计异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @param band 车辆id
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            List<TempStatistics> result;
            long startTimes;
            long endTimes;
            if (!"".equals(startTime) && !"".equals(endTime)) {
                Date d = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(endTime, DATE_FORMAT);
                startTimes = d.getTime() / 1000;
                endTimes = d1.getTime() / 1000;
                RedisKey redisKey = HistoryRedisKeyEnum.STATS_TEMP.of(band, startTimes, endTimes, "");
                // 看不懂-n等是什么用途，希望有人可以解释下，然后加好注释
                RedisKey redisKeyNew = HistoryRedisKeyEnum.STATS_TEMP.of(band, startTimes, endTimes, "-n");
                RedisKey usingKey = RedisHelper.isContainsKey(redisKeyNew) ? redisKeyNew : redisKey;
                result = RedisHelper.getListObj(usingKey, query.getStart() + 1, query.getStart() + query.getLimit());
                Page<TempStatistics> results = RedisUtil.queryPageList(result, query, usingKey);
                return new PageGridBean(query, results, true);
            }
            return null;
        } catch (Exception e) {
            logger.error("获取数据异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    // 高温数据
    @RequestMapping(value = "/highList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getHighList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            List<TempStatistics> result;
            long startTimes;
            long endTimes;
            if (!"".equals(startTime) && !"".equals(endTime)) {
                Date d = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(endTime, DATE_FORMAT);
                startTimes = d.getTime() / 1000;
                endTimes = d1.getTime() / 1000;
                String mark = endTimes < System.currentTimeMillis() / 1000 ? "-n-h" : "-h";
                RedisKey redisKey = HistoryRedisKeyEnum.STATS_TEMP.of(band, startTimes, endTimes, mark);
                result = RedisHelper.getListObj(redisKey, query.getStart() + 1, query.getStart() + query.getLimit());
                Page<TempStatistics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
            return null;
        } catch (Exception e) {
            logger.error("获取数据异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    // 低温数据
    @RequestMapping(value = "/lowList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getLowList(final PositionalQuery query, String band, String startTime, String endTime) {
        try {
            List<TempStatistics> result;
            long startTimes;
            long endTimes;
            if (!"".equals(startTime) && !"".equals(endTime)) {
                Date d = DateUtils.parseDate(startTime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(endTime, DATE_FORMAT);
                startTimes = d.getTime() / 1000;
                endTimes = d1.getTime() / 1000;
                String mark = endTimes < System.currentTimeMillis() / 1000 ? "-n-l" : "-l";
                RedisKey redisKey = HistoryRedisKeyEnum.STATS_TEMP.of(band, startTimes, endTimes, mark);
                result = RedisHelper.getListObj(redisKey, query.getStart() + 1, query.getStart() + query.getLimit());
                Page<TempStatistics> results = RedisUtil.queryPageList(result, query, redisKey);
                return new PageGridBean(query, results, true);
            }
            return null;
        } catch (Exception e) {
            logger.error("获取数据异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出excel
     * @param response
     * @param type
     */
    @RequestMapping(value = "/exportDataList", method = RequestMethod.GET)
    public void exportTempStatisicsList(HttpServletResponse response, int type, String brand, String stime,
        String ntime) {
        try {
            if (!"".equals(stime) && !"".equals(ntime)) {
                Date d = DateUtils.parseDate(stime, DATE_FORMAT);
                Date d1 = DateUtils.parseDate(ntime, DATE_FORMAT);
                long startTimes = d.getTime() / 1000;
                long endTimes = d1.getTime() / 1000;
                String mark = endTimes < System.currentTimeMillis() / 1000 ? "-n" : "";
                String typeMark;
                String fileName;
                switch (type) {
                    case 1:
                        typeMark = "";
                        fileName = "温度报表（全部数据）";
                        break;
                    case 2:
                        typeMark = "-h";
                        fileName = "温度报表（高温数据）";
                        break;
                    case 3:
                        typeMark = "-l";
                        fileName = "温度报表（低温数据）";
                        break;
                    default:
                        return;
                }
                RedisKey redisKey = HistoryRedisKeyEnum.STATS_TEMP.of(brand, startTimes, endTimes, mark + typeMark);
                tempStatisticsService.exportTempStatisticsList(response, type, redisKey, fileName);
            }
        } catch (Exception e) {
            logger.error("导出温度报表异常", e);
        }

    }
}
