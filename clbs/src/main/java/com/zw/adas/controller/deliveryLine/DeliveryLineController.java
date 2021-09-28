package com.zw.adas.controller.deliveryLine;

import com.zw.adas.domain.report.query.DeliveryLineQuery;
import com.zw.adas.service.report.DeliveryLineService;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.common.ControllerTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author wanxing
 * @Title: 车辆终端运行状态
 * @date 2020/11/1310:32
 */
@Controller
@Slf4j
@RequestMapping("/adas/deliveryLine")
public class DeliveryLineController {

    @Autowired
    private DeliveryLineService deliveryLineService;

    @Autowired
    private LineService lineService;

    /**
     * 导出
     *
     * @param query  查询条件
     * @param result 参数校验结果
     * @return 导出接口
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(@Valid DeliveryLineQuery query, BindingResult result, HttpServletResponse response) {
        if (result.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        ExportExcelUtil.setResponseHead(response, "监管平台下发路线");
        try {
            deliveryLineService.export(query, response);
        } catch (Exception e) {
            String errorMsg = "监管平台下发路线导出报错";
            log.error(errorMsg, e.getMessage());
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 线路下发查询
     *
     * @param query  查询条件
     * @param result 校验结果
     * @return 分页结果
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getList(@Valid DeliveryLineQuery query, BindingResult result) {
        if (result.hasErrors()) {
            return new PageGridBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(result));
        }
        return ControllerTemplate.getResultBean(
            () -> deliveryLineService.pageList(query), query, "线路下发查询报错");
    }

    @RequestMapping(value = "/line", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(@NotNull String lineUuid) {
        try {
            List<LineContent> lineList = lineService.findLineContentById(lineUuid);
            return new JsonResultBean(lineList);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "获取线路详情异常");
        }
    }




}
