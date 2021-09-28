package com.zw.adas.controller.driverStatistics;

import com.alibaba.fastjson.JSONArray;
import com.zw.adas.domain.driverStatistics.query.AdasDriverQuery;
import com.zw.adas.domain.driverStatistics.show.AdasDriverStatisticsShow;
import com.zw.adas.service.driverStatistics.AdasDriverStatisticsService;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.adas.utils.controller.AdasQueryListFunction;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/7/4 13:31
 @Description 司机统计
 @version 1.0
 **/
@Controller
@RequestMapping("/m/reportManagement/driverStatistics")
public class AdasDriverStatisticsController {
    private static Logger log = LogManager.getLogger(AdasDriverStatisticsController.class);

    private static final String LIST_PAGE = "modules/reportManagement/driverStatistics";

    @Autowired
    private AdasDriverStatisticsService adasDriverStatisticsService;

    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/setDataToRedis")
    @ResponseBody
    JsonResultBean setDataToRedis(AdasDriverQuery adasDriverQuery) {
        return AdasControllerTemplate
            .setDataListToRedis(() -> adasDriverStatisticsService.getDriverInfo(adasDriverQuery),
                HistoryRedisKeyEnum.DRIVER_STATISTICS_INFO_LIST, "缓存司机统计数据到redis中失败");
    }

    @RequestMapping(value = "/getDrivers", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getDrivers(AdasDriverQuery adasDriverQuery) {
        return AdasControllerTemplate
            .getResultBean(adasDriverQuery, HistoryRedisKeyEnum.DRIVER_STATISTICS_INFO_LIST, "查询司机统计列表接口报错",
                    (AdasQueryListFunction<AdasDriverStatisticsShow>) (datas, groupName) -> adasDriverStatisticsService
                            .getDriverInfoByCardNumber(datas, adasDriverQuery.getSimpleQueryParam()));
    }

    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(AdasDriverQuery adasDriverQuery, HttpServletResponse response, HttpServletRequest request) {
        ExportExcelUtil.setResponseHead(response, "驾驶员统计");
        AdasControllerTemplate
            .getResultBean(() -> adasDriverStatisticsService.export(adasDriverQuery, response, request), "导出督办管理异常！");
    }

    @RequestMapping(value = "/getIcCardDriverInfo")
    @ResponseBody
    public JsonResultBean getIcCardDriverInfo(String vehicleId, String cardNumber) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverStatisticsService.getIcCardDriverInfo(vehicleId, cardNumber),
                "获取实时监控ic卡司机动态信息异常！");
    }

    @RequestMapping(value = "/bindIcCardTree")
    @ResponseBody
    public JsonResultBean bindIcCardTree() {
        try {
            String result = adasDriverStatisticsService.bindIcCardTree().toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("查询驾驶员统计树结构异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @RequestMapping(value = "/bindIcCardTreeByAssign")
    @ResponseBody
    public JsonResultBean bindIcCardTreeByAssign(String assignmentId, boolean isChecked) {
        try {
            if (StringUtils.isNotEmpty(assignmentId)) {
                JSONArray result = adasDriverStatisticsService.bindIcCardTreeByAssign(assignmentId, isChecked);
                // 压缩数据
                String tree = ZipUtil.compress(result.toJSONString());
                return new JsonResultBean(tree);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "参数异常");
        } catch (Exception e) {
            log.error("查询驾驶员统计分组下监控对象异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @RequestMapping(value = "/bindIcCardTreeByGroup")
    @ResponseBody
    public JsonResultBean bindIcCardTreeByGroup(String groupId, boolean isChecked) {
        try {
            if (StringUtils.isNotEmpty(groupId)) {
                Map<String, JSONArray> result = adasDriverStatisticsService.bindIcCardTreeByGroup(groupId, isChecked);
                return new JsonResultBean(result);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "参数异常");
        } catch (Exception e) {
            log.error("查询驾驶员统计组织下监控对象异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @RequestMapping(value = "/bindIcCardTreeSearch")
    @ResponseBody
    public JsonResultBean bindIcCardTreeSearch(String queryParam) {
        try {
            String result = adasDriverStatisticsService.bindIcCardTreeSearch(queryParam).toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("模糊查询驾驶员统计监控对象异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @RequestMapping(value = "/getAdasProfessionalDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAdasProfessionalDetail(String cardNumber, String name) {
        return AdasControllerTemplate
            .getResultBean(() -> adasDriverStatisticsService.getAdasProfessionalDetail(cardNumber, name),
                "司机统计模块获取从业人员异常");
    }

    @RequestMapping(value = "/exportDetail")
    public void exportDetail(AdasDriverQuery adasDriverQuery, HttpServletResponse response) {
        AdasControllerTemplate
            .getResultBean(() -> adasDriverStatisticsService.exportDetail(adasDriverQuery, response), "导出驾驶员明细异常！");
    }

    @RequestMapping(value = "/exportDetails")
    public void exportDetails(AdasDriverQuery adasDriverQuery, HttpServletResponse response) {
        AdasControllerTemplate
            .getResultBean(() -> adasDriverStatisticsService.exportDetails(adasDriverQuery, response), "批量导出驾驶员明细异常！");
    }

    @RequestMapping(value = "data-migration/icHistory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean migrateIcHistory() {
        try {
            return new JsonResultBean(adasDriverStatisticsService.migrate443());
        } catch (Exception e) {
            log.error("迁移数据出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

}
