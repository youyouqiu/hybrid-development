package com.zw.platform.controller.connectionparamsset_809;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery809;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.common.ControllerTemplate;
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
 * 809转发报警查询
 * @author zhouzongbo on 2018/12/25 13:49
 */
@Controller
@RequestMapping("/m/monitorForwardingAlarmSearch")
public class MonitorForwardingAlarmSearch809 {

    private static final Logger logger = LogManager.getLogger(MonitorForwardingAlarmSearch809.class);

    /**
     * 809转发报警查询
     */
    private static final String LIST_PAGE = "modules/monitorForwarding/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private OfflineExportService exportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        try {
            List<AlarmType> alarms = alarmSearchService.getAlarmType();
            modelAndView.addObject("type", JSON.toJSONString(alarms));
            return modelAndView;
        } catch (Exception e) {
            return new ModelAndView(ERROR_PAGE);
        }

    }

    @RequestMapping(value = "/get809ForwardAlarmName", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean get809ForwardAlarmName() {
        try {
            return alarmSearchService.get809ForwardAlarmName();
        } catch (Exception e) {
            logger.error("获取809转发报警名称异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/alarmType", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmType() {
        try {
            List<AlarmType> alarms = alarmSearchService.getAlarmType();
            return JSONObject.toJSONString(alarms);
        } catch (Exception e) {
            return "";
        }

    }

    /**
     * 查询809转发报警并存入Redis, 然后通过Redis进行分页
     */
    @RequestMapping(value = "/find809Alarms", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean find809Alarms(String alarmType, String vehicleIds, String alarmStartTime,
        String alarmEndTime) {
        try {
            return alarmSearchService.find809Alarms(alarmType, vehicleIds, alarmStartTime, alarmEndTime);
        } catch (Exception e) {
            logger.error("809转发报警查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 超速明细列表数据
     */
    @ResponseBody
    @RequestMapping(value = "/getAlarmPage", method = RequestMethod.POST)
    public PageGridBean getAlarmPage(AlarmSearchQuery809 query) {
        return ControllerTemplate
            .getPassPageBean(() -> alarmSearchService.getAlarmPage(query), "809转发报警分页查询异常");
    }

    /**
     * 导出车辆超速报警统计报表
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean export(AlarmSearchQuery809 query) {
        try {
            return ControllerTemplate.addExportOffline(exportService, alarmSearchService.export(query), "809报警导出异常");
        } catch (Exception e) {
            logger.error("809报警导出异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出809报警数据
     */
    @RequestMapping(value = "/export809Alarms", method = RequestMethod.GET)
    public void export809Alarms(HttpServletResponse response) {
        try {
            alarmSearchService.export809Alarms(response);
        } catch (Exception e) {
            logger.error("809报警导出异常", e);
        }
    }

}
