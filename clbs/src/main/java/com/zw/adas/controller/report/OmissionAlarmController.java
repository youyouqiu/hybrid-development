package com.zw.adas.controller.report;

import com.zw.adas.service.report.OmissionAlarmService;
import com.zw.platform.domain.reportManagement.query.OmissionAlarmQuery;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.validation.OmissionAlarmDetailCondition;
import com.zw.platform.validation.OmissionAlarmPageCondition;
import com.zw.talkback.common.ControllerTemplate;
import com.zw.talkback.util.common.QueryFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;

/**
 * @author wanxing
 * @Title: 漏报报警
 * @date 2021/1/1915:04
 */
@Controller
@RequestMapping("/adas/omissionAlarm")
@Slf4j
public class OmissionAlarmController {


    @Autowired
    private OmissionAlarmService omissionAlarmService;

    @Autowired
    private OfflineExportService exportService;

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPageByKeyword(@Validated({OmissionAlarmPageCondition.class}) OmissionAlarmQuery query,
        BindingResult result) {
        return execute(query, result,
            () -> omissionAlarmService.getPageByKeyword(query), "查询报警漏报列表异常");
    }

    /**
     * 企业详情
     */
    @RequestMapping(value = { "/orgDetail" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean orgDetail(@Validated({ OmissionAlarmDetailCondition.class})OmissionAlarmQuery query,
        BindingResult result) {
        return execute(query, result,
            () -> omissionAlarmService.orgDetail(query), "查询报警漏报列表详情异常");
    }


    /**
     * 企业-月-每天-漏报报警次数合计
     */
    @RequestMapping(value = { "/orgDayCount" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean orgDayCount(@Validated({OmissionAlarmPageCondition.class}) OmissionAlarmQuery query,
        BindingResult result) {
        return execute(query, result,
            () -> omissionAlarmService.orgDayCount(query), "查询报警漏报列表企业日报警异常");
    }

    /**
     * 导出
     * @param query
     * @return
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(@Validated({OmissionAlarmPageCondition.class}) OmissionAlarmQuery query,
        BindingResult result) {
        //校验
        JsonResultBean jsonResultBean = checkParameters(query, result);
        if (!jsonResultBean.isSuccess()) {
            return jsonResultBean;
        }
        //校验
        return ControllerTemplate.addExportOffline(exportService, query.getOfflineExportInfo(), "漏报报表导出异常");
    }

    /**
     * 检查参数
     * @param query
     * @param result
     * @return
     */
    private JsonResultBean checkParameters(OmissionAlarmQuery query, BindingResult result) {

        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        //校验
        try {
            DateUtil.formatDate(query.getMonth(), Date8Utils.MONTH_FORMAT);
        } catch (ParseException e) {
            return new JsonResultBean(JsonResultBean.FAULT, "日期格式错误，只能为yyyyMM");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 通用方法
     * @param query 查询
     * @param result 校验结果
     * @param function 真实方法
     * @param message 错误原因
     * @param <Q>
     * @return
     */
    private <Q> JsonResultBean execute(OmissionAlarmQuery query, BindingResult result,
        QueryFunction<Q> function, String message) {
        //校验
        JsonResultBean jsonResultBean = checkParameters(query, result);
        if (!jsonResultBean.isSuccess()) {
            return jsonResultBean;
        }
        try {
            return new JsonResultBean(function.execute());
        } catch (Exception e) {
            log.error(message, e);
            return new JsonResultBean(JsonResultBean.FAULT, message);
        }
    }

}
