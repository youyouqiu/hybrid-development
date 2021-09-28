package com.zw.lkyw.controller.historicalSnapshot;

import com.zw.lkyw.domain.historicalSnapshot.HistoricalSnapshotQuery;
import com.zw.lkyw.service.historicalSnapshot.HistoricalSnapshotService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 @Author lijie
 @Date 2020/1/6 10:56
 @Description 两客一危实时监控历史抓拍
 @version 1.0
 **/
@Controller
@RequestMapping("/lkyw/historicalSnapshot")
public class HistoricalSnapshotController {

    private Logger log = LogManager.getLogger(HistoricalSnapshotController.class);

    @Autowired
    private HistoricalSnapshotService historicalSnapshotService;


    @Value("${sys.error.msg}")
    private String syError;

    @RequestMapping(value = "/photograph", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean send8801(String vehicleId) {
        try {
            if (!MonitorUtils.isOnLine(vehicleId)) {
                return new JsonResultBean(JsonResultBean.FAULT,  "该监控对象未在线");
            }
            JsonResultBean re = historicalSnapshotService.send8801(vehicleId);
            return re;
        } catch (Exception e) {
            log.error("下发抓拍异常！", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }


    @RequestMapping(value = "/mediaList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMediaList(HistoricalSnapshotQuery query) {
        try {
            JsonResultBean re = historicalSnapshotService.getHistoricalSnapshot(query);
            return re;
        } catch (Exception e) {
            log.error("历史抓拍分页查询异常！", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/mediaMapData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMediaMapData(HistoricalSnapshotQuery query) {
        try {
            JsonResultBean re = historicalSnapshotService.getMediaMapData(query);
            return re;
        } catch (Exception e) {
            log.error("历史抓拍地图打点查询异常！", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/mediaMapDataDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMediaMapDataDetail(HistoricalSnapshotQuery query) {
        try {
            JsonResultBean re = historicalSnapshotService.getMediaMapDataDetail(query);
            return re;
        } catch (Exception e) {
            log.error("历史抓拍地图查询详情异常！", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

}
