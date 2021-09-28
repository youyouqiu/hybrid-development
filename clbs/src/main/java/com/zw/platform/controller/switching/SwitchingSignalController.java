package com.zw.platform.controller.switching;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.switching.IoVehicleConfigService;
import com.zw.platform.service.switching.SwitchingSignalService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author nixiangqian
 * @date 2017年06月21日 13:53
 */
@Controller
@RequestMapping("/m/switching/signal")
public class SwitchingSignalController {
    private static Logger log = LogManager.getLogger(SwitchingSignalController.class);

    private static final String LIST_PAGE = "vas/switching/signal/list";

    private static final String BIND_PAGE = "vas/switching/signal/bindIo";

    private static final String EDIT_PAGE = "vas/switching/signal/editIo";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${have.been.set.vechile.param.fail}")
    private String vechileParamFail;

    @Autowired
    private SwitchingSignalService signalService;

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private IoVehicleConfigService ioVehicleConfigService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public PageGridBean list(final SensorConfigQuery query) {
        try {
            Page<SwitchingSignal> result = signalService.findByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询（findByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = { "/bind" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true) // 防止表单重复提交
    public JsonResultBean addPage(SwitchingSignal signal) {
        try {
            if (signal != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return signalService.addSwitchingSignal(signal, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("设置信号位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/edit_{vechileId}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView editPage(@PathVariable("vechileId") String vehicleId) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            List<Map> deviceIos = ioVehicleConfigService.getVehicleBindIos(vehicleId, 1);
            List<Map> collectionOneIos = ioVehicleConfigService.getVehicleBindIos(vehicleId, 2);
            List<Map> collectionTwoIos = ioVehicleConfigService.getVehicleBindIos(vehicleId, 3);
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
            String deviceType = Optional.ofNullable(bindInfo).map(BindDTO::getDeviceType).orElse("");
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            List<SwitchingSignal> list = signalService.findVehicleSensorSetting(protocols);
            String listJsonStr = JSON.toJSONString(list);
            VehicleInfo vehicleInfo = alarmSettingService.findPeopleOrVehicleOrThingById(vehicleId);
            vehicleInfo.setId(vehicleId);
            if (deviceIos.size() > 0 || collectionOneIos.size() > 0 || collectionTwoIos.size() > 0) {
                mav = new ModelAndView(EDIT_PAGE);
                mav.addObject("deviceIos", deviceIos);
                mav.addObject("collectionOneIos", collectionOneIos);
                mav.addObject("collectionTwoIos", collectionTwoIos);
            }
            mav.addObject("vehicle", vehicleInfo);
            mav.addObject("referVehicleList", JSON.parseArray(listJsonStr));
            return mav;
        } catch (Exception e) {
            log.error("修改界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取已设置车辆的参数
     * @param vechileId
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/getParameter_{vechileId}.gsp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getParameter_(@PathVariable("vechileId") String vechileId) {
        try {
            SwitchingSignal form = signalService.findByVehicleId(vechileId);
            String pollingsListJsonStr = JSON.toJSONString(form);
            return new JsonResultBean(JsonResultBean.SUCCESS, pollingsListJsonStr);
        } catch (Exception e) {
            log.error("获取已设置车辆的参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, vechileParamFail);
        }
    }

    @RequestMapping(value = { "/edit" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean editPage(SwitchingSignal signal) {
        try {
            if (signal != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean flag = signalService.updateSwitchingSignal(signal, ip);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改信号位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id下发参数设置
     */
    @RequestMapping(value = "/sendPosition", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendPosition(String vehicleId) {
        try {
            if (vehicleId != null && !vehicleId.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return signalService.sendPosition(vehicleId, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 单个删除
     * @param id
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/delete_{id}.gsp" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteByVechileId(@PathVariable("id") String id) {
        try {
            if (id != null && !id.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean flag = ioVehicleConfigService.deleteById(id, ip);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除监控开关信号设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/deletemore" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteByVechileId() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return ioVehicleConfigService.deleteBatchByIds(ids, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除监控对象开关信号设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
