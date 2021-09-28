package com.zw.platform.controller.alarm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.switching.SwitchTypeService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/7/9 10:19
 */
@Controller
@RequestMapping("/v/switchSignalAlarm")
public class SwitchSignalAlarmController {
    private static final Logger log = LogManager.getLogger(SwitchSignalAlarmController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 开关信号报警查询页面
     */
    private static final String LIST = "vas/alarm/switchSignalAlarm/switchSignalAlarm";

    /**
     * 错误页
     */
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Resource
    private SwitchTypeService switchTypeService;

    @Resource
    private AlarmSearchService alarmSearchService;

    @Resource
    private HttpServletRequest request;

    /**
     * 开关信号报警查询页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView indexPage(String vehicleId, String alarmTime) {
        try {
            ModelAndView mav = new ModelAndView(LIST);
            List<SwitchType> ioSwitchType = switchTypeService.getIoSwitchType();
            JSONObject alarmTypeName = installIoAlarmTypeTree(ioSwitchType);
            mav.addObject("alarmTypeName", alarmTypeName);
            mav.addObject("vehicleId", vehicleId);
            if (alarmTime != null) {
                alarmTime = alarmTime.substring(0, 10) + " 00:00:00";
            }
            mav.addObject("alarmTime", alarmTime);
            return mav;
        } catch (Exception e) {
            log.error("开关信号报警查询页面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询开关信号报警列表
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public JsonResultBean list(String vehicleIds, String alarmTypeNames, Integer status, String startTime,
        String endTime, Integer pushType) {
        try {
            return alarmSearchService
                .getIoAlarmHandle(vehicleIds, alarmTypeNames, status, startTime, endTime, pushType);
        } catch (Exception e) {
            log.error("查询开关信号报警列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分页查询开关信号报警
     */
    @RequestMapping(value = { "/getIoAlarmList" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getIoAlarmList(AlarmSearchQuery query) {
        try {
            return alarmSearchService.getIoAlarmList(query);
        } catch (Exception e) {
            log.error("分页查询开关信号报警异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出开关信号报警(生成excel文件)
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse response) {
        try {
            alarmSearchService.exportIoAlarm(response);
        } catch (Exception e) {
            log.error("导出开关信号报警异常", e);
        }
    }

    /**
     * 组装报警类型名称树
     */
    private JSONObject installIoAlarmTypeTree(List<SwitchType> ioSwitchType) {
        JSONObject alarmTypeName = new JSONObject();
        JSONArray tree = new JSONArray();
        if (CollectionUtils.isNotEmpty(ioSwitchType)) {
            for (SwitchType switchType : ioSwitchType) {
                String identify = switchType.getIdentify();
                JSONObject parentNode = new JSONObject();
                parentNode.put("isParent", true);
                parentNode.put("name", switchType.getName());
                parentNode.put("id", identify);
                parentNode.put("pId", 0);
                parentNode.put("isCondition", false);
                JSONObject childNodeOne = new JSONObject();
                childNodeOne.put("isParent", false);
                childNodeOne.put("name", switchType.getStateOne());
                childNodeOne.put("id", identify + "1");
                childNodeOne.put("pId", identify);
                childNodeOne.put("isCondition", true);
                JSONObject childNodeTwo = new JSONObject();
                childNodeTwo.put("isParent", false);
                childNodeTwo.put("name", switchType.getStateTwo());
                childNodeTwo.put("id", identify + "2");
                childNodeTwo.put("pId", identify);
                childNodeTwo.put("isCondition", true);
                JSONObject abnormalIo = new JSONObject();
                abnormalIo.put("isParent", false);
                abnormalIo.put("name", switchType.getName() + "异常");
                abnormalIo.put("id", identify + "3");
                abnormalIo.put("pId", identify);
                abnormalIo.put("isCondition", true);
                tree.add(parentNode);
                tree.add(childNodeOne);
                tree.add(childNodeTwo);
                tree.add(abnormalIo);
            }
            for (int i = 90; i <= 92; i++) {
                JSONObject sensor = new JSONObject();
                sensor.put("isParent", false);
                sensor.put("name", i == 90 ? "终端I/O异常" : (i == 91 ? "I/O采集1异常" : "I/O采集2异常"));
                sensor.put("id", "0x" + i);
                sensor.put("pId", 0);
                sensor.put("isCondition", true);
                tree.add(sensor);
            }
        }
        alarmTypeName.put("tree", tree);
        return alarmTypeName;
    }

    /**
     * 处理IO报警
     */
    @ResponseBody
    @RequestMapping(value = "/updateIOAlarm", method = RequestMethod.POST)
    public JsonResultBean updateIoAlarm(HandleAlarms handleAlarms) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            return alarmSearchService.updateIoAlarm(handleAlarms, ip);
        } catch (Exception e) {
            log.error("处理IO报警异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
