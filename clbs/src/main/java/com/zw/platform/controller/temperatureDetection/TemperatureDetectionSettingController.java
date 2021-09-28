package com.zw.platform.controller.temperatureDetection;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
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

import java.util.List;
import java.util.UUID;

/**
 * 温度监测设置Controller
 * @author hujun 2017/7/6
 */
@Controller
@RequestMapping("/v/temperatureDetection/temperatureDetectionSetting")
public class TemperatureDetectionSettingController {
    private static Logger log = LogManager.getLogger(TemperatureDetectionSettingController.class);

    private static final String LIST_PAGE = "vas/temperatureDetection/temperatureDetectionSetting/list";

    private static final String BIND_PAGE = "vas/temperatureDetection/temperatureDetectionSetting/bind";

    private static final String EDIT_PAGE = "vas/temperatureDetection/temperatureDetectionSetting/edit";

    private static final String DETAIL_PAGE = "vas/temperatureDetection/temperatureDetectionSetting/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private TransduserService transduserService;

    @Autowired
    private SensorSettingsService sensorSettingsService;

    /**
     * 显示界面
     * @return
     * @throws BusinessException
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 绑定界面
     * @param vid
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/bind_{id}" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // 防止表单重复提交
    public ModelAndView bindPage(@PathVariable("id") String vid) throws BusinessException {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // 根据id查询车辆、人、物品信息
            BindDTO configList = VehicleUtil.getBindInfoByRedis(vid);
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setId(configList.getId());
            vehicle.setBrand(configList.getName());
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            // 查询参考车信息
            List<TransdusermonitorSet> list = sensorSettingsService.consultVehicle(1, protocols);
            // 查询温度传感器信息
            Page<TransduserManage> result = transduserService.getTransduserManage(1, null);

            mav.addObject("vehicle", vehicle);
            mav.addObject("TransduserManage", JSON.toJSONString(result));
            mav.addObject("vehicleList", JSON.toJSONString(list));
            mav.addObject("id", UUID.randomUUID().toString());
            mav.addObject("id2", UUID.randomUUID().toString());
            return mav;
        } catch (Exception e) {
            log.error("温度传感器绑定界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改界面
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = { "/edit_{id}" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") String id, String sensorOutId) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 查询当前要修改的车辆与温度传感器的绑定关系
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.findByVehicleId(1, id);
            // 查询所有与温度传感器绑定的车的绑定关系
            List<TransdusermonitorSet> list = sensorSettingsService.consultVehicle(1, null);
            // 查询所有温度传感器
            Page<TransduserManage> result = transduserService.getTransduserManage(1, null);
            mav.addObject("vehicle", JSON.toJSONString(vehicleList));
            mav.addObject("TransduserManage", JSON.toJSONString(result));
            mav.addObject("vehicleList", JSON.toJSONString(list));
            mav.addObject("id", sensorOutId);
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
