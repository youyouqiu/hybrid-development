package com.zw.platform.controller.oilmassmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionlList;
import com.zw.platform.domain.oil.PositionlQuery;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmassmgt.OilQuantityStatisticsService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.service.sensor.SensorPollingService;
import com.zw.platform.service.switching.SwitchingSignalService;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.ZipUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 油量统计Controller <p>Title: OilQuantityStatisticsController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:25:07
 */
@Controller
@RequestMapping("/v/oilmassmgt/oilquantitystatistics")
public class OilQuantityStatisticsController {

    @Autowired
    private OilQuantityStatisticsService quantityStatisticsService;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private SwitchingSignalService signalService;

    @Autowired
    private SensorPollingService sensorPollingService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static Logger logger = LogManager.getLogger(OilQuantityStatisticsController.class);

    private static final String LIST_PAGE = "vas/oilmassmgt/oilquantitystatistics/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            // 查询参考车辆
            List<OilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceBrand();
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            logger.error("分页查询异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/getOilInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilInfo(String band, String startTime, String endTime) {
        try {
            JSONObject msg = new JSONObject();
            List<Positional> oilInfo = null;
            oilInfo = quantityStatisticsService.getOilMassInfo(band, startTime, endTime);
            Integer[] signal = signalService.findAirStatus(band);
            String ioStatus = sensorPollingService.findStatus(band);
            // 根据车辆id获取与其绑定的油箱的理论容积
            List<FuelTankForm> fuelList = fuelTankManageService.getFuelTankDetailByVehicleId(band);
            String box1Volume = "";
            String box2Volume = "";
            if (null != fuelList && fuelList.size() > 0) {
                for (FuelTankForm f : fuelList) {
                    if ("1".equals(Converter.toBlank(f.getTanktyp()))) { // 油箱1
                        box1Volume = Converter.toBlank(f.getTheoryVolume());
                    } else if ("2".equals(Converter.toBlank(f.getTanktyp()))) {
                        box2Volume = Converter.toBlank(f.getTheoryVolume());
                    }
                }
            }
            String result = JSON.toJSONString(oilInfo);
            // 压缩数据
            result = ZipUtil.compress(result);
            msg.put("box1", box1Volume);
            msg.put("box2", box2Volume);
            msg.put("oilInfo", result);
            msg.put("signal", signal);
            msg.put("ioStatus", ioStatus);
            msg.put("infoDtails", quantityStatisticsService.getInfoDtails(oilInfo, band, signal));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("获得油量信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/getOilPagInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilPagiInfo(final PositionlQuery query) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "");
        Page<PositionlList> result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_OIL_DATA::of, query);
        return new PageGridBean(query, result, true);
    }

    /**
     * 加油数据
     * @param query
     * @return
     */
    @RequestMapping(value = "/getOilAmountPagiInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilAmountPagiInfo(final PositionlQuery query) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-a");
        Page<PositionlList> result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_OIL_DATA::of, query);
        return new PageGridBean(query, result, true);
    }

    /**
     * 漏油数据
     * @param query
     * @return
     */
    @RequestMapping(value = "/getOilSpillPagiInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilSpillPagiInfo(final PositionlQuery query) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-s");
        Page<PositionlList> result = RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_OIL_DATA::of, query);
        return new PageGridBean(query, result, true);
    }

    @RequestMapping(value = "/getSensorMessage", method = RequestMethod.POST)
    @ResponseBody
    public String getSensorMessage(final String band) {
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(band));
        if (flogKey) {
            return "true";
        }
        return "";
    }

    /**
     * 导出油量报表
     * @param response
     */
    @RequestMapping(value = "/exportDataList", method = RequestMethod.GET)
    public void exportOilPagInfoList(HttpServletResponse response, int type, String vehicleId) {
        try {
            quantityStatisticsService.exportOilPagInfoList(response, type, vehicleId);
        } catch (Exception e) {
            logger.error("导出油量报表异常", e);
        }
    }

}
