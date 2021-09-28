package com.zw.platform.controller.mileageDetection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.mileageSensor.MileageSensor;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import com.zw.platform.domain.vas.mileageSensor.TyreSize;
import com.zw.platform.service.mileageSensor.MileageSensorConfigService;
import com.zw.platform.service.mileageSensor.MileageSensorService;
import com.zw.platform.service.mileageSensor.TyreSizeService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p> Title:里程传感器配置Service <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:16
 */
@Controller
@RequestMapping("/v/meleMonitor/mileMonitorSet")
public class MilegeDetectionSettingController {
    private static Logger log = LogManager.getLogger(MilegeDetectionSettingController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${poll.param.null}")
    private String pollParamNull;

    @Value("${down.fail}")
    private String downFail;

    @Value("${set.success}")
    private String setSuccess;

    @Autowired
    private MileageSensorConfigService service;

    @Autowired
    private MileageSensorService mileageSensorService;

    @Autowired
    private TyreSizeService tyreSizeService;

    @Autowired
    private HttpServletRequest request;

    private static final String LIST_PAGE = "vas/meleMonitor/mileMonitorSet/list";

    private static final String ADD_PAGE = "vas/meleMonitor/mileMonitorSet/add";

    private static final String EDIT_PAGE = "vas/meleMonitor/mileMonitorSet/edit";

    private static final String DETAIL_PAGE = "vas/meleMonitor/mileMonitorSet/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 根据车辆id查询里程监测设置信息
     * @param vehicleid
     * @return
     */
    @RequestMapping(value = { "/getInfo_{vehicleid}" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getInfoByVehicleid(@PathVariable("vehicleid") String vehicleid) {
        try {
            if (vehicleid != null && !"".equals(vehicleid)) {
                MileageSensorConfig form = service.findByVehicleId(vehicleid);
                return new JsonResultBean(JsonResultBean.SUCCESS, JSON.toJSONString(form));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 返回新增里程监测界面
     * @param vehicleid
     * @return
     */
    @RequestMapping(value = { "/add_{vehicleid}" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView addPage(@PathVariable("vehicleid") String vehicleid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            getModelAndViewDetail(vehicleid, mav);
            return mav;
        } catch (Exception e) {
            log.error("弹出新增里程监测设置界面时异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/detail_{vehicleid}" }, method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable("vehicleid") String vehicleid) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            MileageSensorConfig form = service.findByVehicleId(vehicleid);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("详情界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/edit_{vehicleid}" }, method = RequestMethod.GET)
    public ModelAndView editMobileSourceBaseInfo(@PathVariable("vehicleid") String vehicleid) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            MileageSensorConfig form = service.findByVehicleId(vehicleid);
            //VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(vehicleid);
            getModelAndViewDetail(vehicleid, mav);
            mav.addObject("result", form);
            return mav;
        } catch (Exception e) {
            log.error("修改界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    private void getModelAndViewDetail(String vehicleid, ModelAndView mav) throws Exception {
        VehicleInfo vehicle = new VehicleInfo();
        BindDTO configList = VehicleUtil.getBindInfoByRedis(vehicleid);
        String deviceType = configList.getDeviceType();
        List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
        vehicle.setId(configList.getId());
        vehicle.setBrand(configList.getName());
        mav.addObject("vehicle", vehicle);
        // 里程传感器
        List<MileageSensor> mileageSensorlist = mileageSensorService.findAll();
        String mileageSensorlistJsonStr = JSON.toJSONString(mileageSensorlist);
        mav.addObject("sensorList", JSON.parseArray(mileageSensorlistJsonStr));
        // 里程传感器
        List<TyreSize> tyreSizelist = tyreSizeService.findAll();
        String tyreSizelistJsonStr = JSON.toJSONString(tyreSizelist);
        mav.addObject("tyreSizelist", JSON.parseArray(tyreSizelistJsonStr));
        // 查询参考车牌下拉列表
        List<MileageSensorConfig> referVehicleList = service.findVehicleSensorSetByProtocols(protocols);
        String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);
        mav.addObject("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
    }

    /**
     * 新增里程传感器设置
     * @param form
     * @return
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true) // 防止表单重复提交
    public JsonResultBean savePage(final MileageSensorConfig form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return service.addMileageSensorConfig(form, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增里程传感器设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改里程传感器设置
     * @param form
     * @return
     */
    @RequestMapping(value = { "/edit" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updatePage(final MileageSensorConfig form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return service.updateMileageSensorConfig(form, true, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改里程传感器设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据监控对象id删除里程传感器配置信息
     * @param vehicleid 监控对象id
     * @return
     */
    @RequestMapping(value = "/delete_{vehicleid}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("vehicleid") final String vehicleid) {
        try {
            if (!vehicleid.isEmpty()) {
                List<String> vehicleids = new ArrayList<>();
                vehicleids.add(vehicleid);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return service.deleteBatchMileageSensorConfig(vehicleids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解绑里程传感器设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据监控对象id集合批量删除里程监测设置
     * @return
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null) {
                String[] item = items.split(",");
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                List<String> ids = Arrays.asList(item);
                return service.deleteBatchMileageSensorConfig(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解绑里程传感器设置", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 分页查询
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final MileageSensorConfigQuery query) {
        try {
            Page<MileageSensorConfig> result = service.findByQuery(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询分组（findByQuery）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 刷新参数下发状态
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = { "/refreshSendStatus" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean refreshSendStatus(String vehicleId) {
        try {
            MileageSensorConfig mileageSensorConfig = service.findByVehicleId(vehicleId, true);
            if (mileageSensorConfig != null) {
                return new JsonResultBean(mileageSensorConfig);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("里程刷新参数下发状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }


    /**
     * 根据id下发参数设置
     */
    @RequestMapping(value = "/sendAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendAlarm(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            if (paramList == null || paramList.size() == 0) {
                return new JsonResultBean(JsonResultBean.FAULT, downFail);
            }
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return service.sendParam(paramList, ip);
        } catch (Exception e) {
            log.error("下发参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

}
