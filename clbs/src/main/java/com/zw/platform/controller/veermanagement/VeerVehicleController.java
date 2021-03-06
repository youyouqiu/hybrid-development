package com.zw.platform.controller.veermanagement;

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
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/v/veerManagement/veerVehicleSet")
public class VeerVehicleController {
    private static Logger log = LogManager.getLogger(VeerVehicleController.class);

    private static final String LIST_PAGE = "vas/veerManagement/veerVehicleSet/list";

    private static final String BIND_PAGE = "vas/veerManagement/veerVehicleSet/bind";

    private static final String EDIT_PAGE = "vas/veerManagement/veerVehicleSet/edit";

    private static final String DETAIL_PAGE = "vas/veerManagement/veerVehicleSet/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;
    @Autowired
    private AlarmSettingService alarmSettingService;
    @Autowired
    private SensorSettingsService sensorSettingsService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private TransduserService transduserService;

    @Autowired
    private HttpServletRequest request;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/detail_{id}" }, method = RequestMethod.GET)
    public ModelAndView detailPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            TransdusermonitorSet transdusermonitorSet = sensorSettingsService.findTransdusermonitorSetById(id);
            // ?????????
            VehicleInfo vehicle = vehicleService.findVehicleById(transdusermonitorSet.getVehicleId());
            mav.addObject("vehicle", vehicle);
            mav.addObject("result", transdusermonitorSet);
            return mav;
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     * @return
     */
    @RequestMapping(value = { "/edit_{id}" }, method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable("id") final String vehicleId, HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // ?????????????????????
            BindDTO configList = VehicleUtil.getBindInfoByRedis(vehicleId);
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.findVehicleBrandByType(3, protocols);
            // ??????????????????????????????
            Page<TransduserManage> sensorList = transduserService.getTransduserManage(3, null);
            // ????????????id??????????????????????????????
            List<TransdusermonitorSet> veerVehicleList = sensorSettingsService.findByVehicleId(3, vehicleId);
            TransdusermonitorSet veerVehicle = veerVehicleList.get(0);
            // findFuelVehicleById
            if (veerVehicle != null) {
                mav.addObject("sensorList", JSON.toJSONString(sensorList));
                mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
                mav.addObject("result", veerVehicle);
                return mav;
            } else {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('??????????????????????????????');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * TODO ???????????????????????????
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws @Title: edit
     * @author
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(final TransdusermonitorSet form, final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                String ip = new GetIpAddr().getIpAddr(request); // ????????????ip
                // ???????????????
                return sensorSettingsService.updateSensorVehicle(form, ip);
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @param vehicleId
     * @return
     */
    @RequestMapping(value = { "/bind_{id}" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true) // ????????????????????????
    public ModelAndView bindPage(@PathVariable("id") final String vehicleId) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // ?????????
            BindDTO configList = VehicleUtil.getBindInfoByRedis(vehicleId);
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setId(configList.getId());
            vehicle.setBrand(configList.getName());
            String deviceType = configList.getDeviceType();
            List<Integer> protocols = ProtocolEnum.getProtocols(Integer.valueOf(deviceType));
            // ?????????????????????
            List<TransdusermonitorSet> vehicleList = sensorSettingsService.findVehicleBrandByType(3, protocols);
            // ??????????????????????????????
            Page<TransduserManage> sensorList = transduserService.getTransduserManage(3, null);
            mav.addObject("vehicle", vehicle);
            mav.addObject("sensorList", JSON.toJSONString(sensorList));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }
}
