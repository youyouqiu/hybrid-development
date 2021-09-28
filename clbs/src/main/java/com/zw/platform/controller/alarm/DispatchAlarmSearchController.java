package com.zw.platform.controller.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ZipUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 业务类报表-调度报警查询
 * @author penghj
 * @version 1.0
 * @date 2019/11/11 9:20
 */
@Controller
@RequestMapping("/a/businessReport/alarmSearch")
public class DispatchAlarmSearchController {
    private static Logger logger = LogManager.getLogger(DispatchAlarmSearchController.class);

    private static final String LIST_PAGE = "vas/alarm/dispatchAlarm/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MonitorTreeService monitorTreeService;

    /**
     * 获得调度报警页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getSchedulingAndTaskAlarmSearchPage() {
        try {
            return new ModelAndView(LIST_PAGE)
                .addObject("type", JSON.toJSONString(alarmSearchService.getDispatchAlarmType()));
        } catch (Exception e) {
            logger.error("获得调度报警页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询调度报警(排班、任务和sos报警)
     */
    @RequestMapping(value = "/queryDispatchAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean queryDispatchAlarm(String alarmType, Integer status, String alarmStartTime,
        String alarmEndTime, String monitorIds) {
        try {
            return alarmSearchService.queryDispatchAlarm(alarmType, status, alarmStartTime, alarmEndTime, monitorIds);
        } catch (Exception e) {
            logger.error("查询调度报警异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询调度报警列表(排班、任务和sos报警)
     */
    @RequestMapping(value = "/getDispatchAlarmList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getDispatchAlarmList(AlarmSearchQuery alarmSearchQuery) {
        try {
            return alarmSearchService.getDispatchAlarmList(alarmSearchQuery);
        } catch (Exception e) {
            logger.error("查询调度报警列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出调度报警
     * @param response response
     */
    @RequestMapping(value = "/exportDispatchAlarm", method = RequestMethod.GET)
    public void exportDispatchAlarm(HttpServletResponse response) {
        try {
            alarmSearchService.exportDispatchAlarm(response);
        } catch (Exception e) {
            logger.error("导出调度报警异常", e);
        }
    }

    /**
     * 获取监控对象树
     */
    @RequestMapping(value = "/alarmSearchTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmSearchTree(String type, String isIncludeQuitPeople) {
        try {
            String result = vehicleService.monitorTreeByType(type, isIncludeQuitPeople).toJSONString();
            return new JsonResultBean(ZipUtil.compress(result));
        } catch (Exception e) {
            logger.error("获取监控对象树", e);
            return null;
        }
    }

    /**
     * 根据分组id查询监控对象信息（组装成树节点的格式）
     */
    @RequestMapping(value = "/putMonitorByAssign", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putMonitorByAssign(String assignmentId, boolean isChecked, String monitorType,
        String deviceType, Integer webType, String isIncludeQuitPeople, Integer status) {
        try {
            MonitorTreeReq treeReq = new MonitorTreeReq();
            List<String> deviceTypes = treeReq.getDeviceTypes(monitorType, deviceType, webType, false);
            treeReq.setDeviceTypes(deviceTypes);
            treeReq.setChecked(isChecked);
            treeReq.setWebType(webType);
            treeReq.setMonitorType(monitorType);
            treeReq.setStatus(status);
            treeReq.setNeedAccStatus(false);
            treeReq.setNeedQuitPeople(Objects.equals(isIncludeQuitPeople, 1));
            List<String> groupIds = Arrays.asList(assignmentId.split(","));
            JSONArray result = monitorTreeService.getByGroupId(groupIds, treeReq);

            // 压缩数据
            String tree = ZipUtil.compress(result.toJSONString());
            return new JsonResultBean(tree);
        } catch (Exception e) {
            logger.error("根据分组id查询监控对象信息异常", e);
            return null;
        }
    }

    /**
     * 监控对象树模糊搜索
     */
    @RequestMapping(value = "/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String monitorTreeFuzzy(String type, String queryParam, String deviceType, Integer webType,
        String isIncludeQuitPeople) {
        try {
            MonitorTreeReq query = new MonitorTreeReq();
            query.setQueryType("name");
            query.setType(type);
            query.setKeyword(queryParam);
            query.setWebType(webType);
            if (StringUtils.isNotBlank(deviceType)) {
                query.setDeviceTypes(Collections.singletonList(deviceType));
            }
            query.setNeedCarousel(false);
            query.setMonitorType("vehicle");
            query.setChecked(false);
            query.setNeedAccStatus(false);
            query.setNeedQuitPeople(Objects.equals(isIncludeQuitPeople, 1));
            String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();
            return ZipUtil.compress(result);
        } catch (Exception e) {
            logger.error("模糊搜索车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 处理报警
     */
    @RequestMapping(value = "/handleDispatchAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean handleDispatchAlarm(HandleAlarms handleAlarms, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return alarmSearchService.updateDispatchAlarm(handleAlarms, ipAddress);
        } catch (Exception e) {
            logger.error("报警状态存储异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
