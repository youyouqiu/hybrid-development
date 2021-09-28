package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.OfflineDisplacementDealDTO;
import com.zw.platform.domain.reportManagement.query.OfflineDisplacementQuery;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.reportManagement.OfflineDisplacementService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.OfflineDisplacementBatchDeal;
import com.zw.platform.util.common.OfflineDisplacementDeal;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * @author wanxing
 * @Title: 离线位移日报表
 * @date 2020/10/2010:44
 */
@Controller
@RequestMapping("/m/reportManagement/offlineDisplacement")
public class OfflineDisplacementController {

    private static final Logger log = LogManager.getLogger(OfflineDisplacementController.class);

    private static final String LIST_PAGE = "modules/reportManagement/offlineDisplacement/list";

    @Autowired
    private OfflineDisplacementService offlineDisplacementService;
    @Autowired
    private OfflineExportService exportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@Valid OfflineDisplacementQuery query, BindingResult result) {

        if (result.hasErrors()) {
            return new PageGridBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        //校验
        try {
            DateUtil.formatDate(query.getDate(), DateUtil.DATE_YMD_FORMAT);
        } catch (Exception e) {
            return new PageGridBean(JsonResultBean.FAULT, "日期格式错误，只能为yyyyMMdd");
        }
        return ControllerTemplate
            .getPassPageBean(() -> offlineDisplacementService.queryList(query), "查询离线位移日列表异常");
    }

    /**
     * 单条处理
     */
    @RequestMapping(value = { "/deal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deal(@Validated({OfflineDisplacementDeal.class}) OfflineDisplacementDealDTO param,
        BindingResult result) {
        //校验
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        try {
            offlineDisplacementService.deal(param.getMonitorId(),
                param.getOfflineMoveEndTime(), param.getHandleResult(), param.getRemark());
        } catch (Exception e) {
            log.error("处理离线位移日错误", e);
            return new JsonResultBean(JsonResultBean.FAULT, "处理离线位移日错误，请联系管理员");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/batchDeal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchDeal(@Validated({OfflineDisplacementBatchDeal.class}) OfflineDisplacementDealDTO param,
        BindingResult result) {
        //校验
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        try {
            offlineDisplacementService.batchDeal(param.getPrimaryKeys(), param.getHandleResult(), param.getRemark());
        } catch (Exception e) {
            log.error("批量处理离线位移日错误", e);
            return new JsonResultBean(JsonResultBean.FAULT, "批量处理离线位移日错误，请联系管理员");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(@Valid OfflineDisplacementQuery query, BindingResult result) {

        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        try {
            DateUtil.formatDate(query.getDate(), DateUtil.DATE_YMD_FORMAT);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "日期格式错误，只能为yyyyMMdd");
        }
        //校验
        return ControllerTemplate.addExportOffline(exportService, query.getOfflineExportInfo(), "离线位移日报表导出异常");
    }

}
