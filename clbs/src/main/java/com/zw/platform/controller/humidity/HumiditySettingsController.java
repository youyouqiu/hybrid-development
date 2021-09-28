package com.zw.platform.controller.humidity;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.service.transdu.TransduserService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by yangyi on 2017/7/6
 */
@Controller
@RequestMapping("/v/humidity/settings")
public class HumiditySettingsController {
    private static Logger log = LogManager.getLogger(HumiditySettingsController.class);

    private static final String LIST_PAGE = "vas/humidity/humiditySettings/list";

    private static final String BIND_PAGE = "vas/humidity/humiditySettings/bind";

    private static final String EDIT_PAGE = "vas/humidity/humiditySettings/edit";

    private static final String DETAIL_PAGE = "vas/humidity/humiditySettings/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Autowired
    private TransduserService transduserService;

    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private AlarmSettingService alarmSettingService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/bind" }, method = RequestMethod.GET)
    public String bindPage() throws BusinessException {
        return BIND_PAGE;
    }

    /**
     * 设置 TODO
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws @Title:           bindPage
     * @author wangying
     */
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView bindPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // 查询车/人/物信息
            BindDTO configList = VehicleUtil.getBindInfoByRedis(id);
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setId(configList.getId());
            vehicle.setBrand(configList.getName());
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.consultVehicle(2, protocols);
            Page<TransduserManage> result = transduserService.getTransduserManage(2, null);
            mav.addObject("vehicle", vehicle);
            mav.addObject("TransduserManage", JSON.toJSONString(result));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("绑定界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") final String id, String outId, HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            BindDTO configList = VehicleUtil.getBindInfoByRedis(id);
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            List<TransdusermonitorSet> list = sensorSettingsService.findByVehicleId(2, id);
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.consultVehicle(2, protocols);
            Page<TransduserManage> result = transduserService.getTransduserManage(2, null);
            mav.addObject("vehicle", JSON.toJSONString(list));
            mav.addObject("TransduserManage", JSON.toJSONString(result));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            mav.addObject("id", outId);
            return mav;
        } catch (Exception e) {
            log.error("修改界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 详情界面
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/detail_{id}" }, method = RequestMethod.GET)
    public ModelAndView detailPage(@PathVariable("id") String id) {
        try {
            ModelAndView m = new ModelAndView(DETAIL_PAGE);
            TransdusermonitorSet tms = sensorSettingsService.findTransdusermonitorSetById(id);
            m.addObject("result", tms);
            return m;
        } catch (Exception e) {
            log.error("详情界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

}
