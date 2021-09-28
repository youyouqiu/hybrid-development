package com.zw.platform.controller.singleVehicle;


import com.zw.app.controller.personalCenter.PersonalCenterController;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.basicinfo.form.SynchronizeVehicleForm;
import com.zw.platform.domain.reportManagement.TerminalMileageDailyDetails;
import com.zw.platform.domain.singleVehicle.Brand;
import com.zw.platform.domain.singleVehicle.SingleVehicleAcount;
import com.zw.platform.domain.vas.alram.query.SingleVehicleAlarmSearchQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.singleVehicle.SingleVehicleService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 单车登录小程序
 * 2020/05/08
 *
 * @author XK
 */
@Controller
@RequestMapping("/single/vehicle")
public class SingleVehicleController {

    private static Logger logger = LogManager.getLogger(SingleVehicleController.class);

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private SingleVehicleService singleVehicleService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PositionalService positionalService;

    private static Logger log = LogManager.getLogger(PersonalCenterController.class);

    private static final Pattern BRAND_CHECK = Pattern.compile("^[A-Za-z0-9\\u4e00-\\u9fa5\\-]+$");

    private static final Pattern PASSWORD_CHECK = Pattern.compile("^[a-zA-Z0-9]+$");

    private static final Integer BIGGEST_BRAND_LENGTH = 20;

    private static final Integer BIGGEST_PASSWORD_LENGTH = 6;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;


    /**
     * 单车登录小程序登陆
     *
     * @author xuekun
     * @date 2020/05/08 14:21
     */
    @RequestMapping(value = {"/user/login"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateUserPassword(@RequestParam("brand") final String brand,
                                             @RequestParam("vehiclePassword") final String vehiclePassword) {
        try {
            if (StringUtils.isNotBlank(brand)) {
                if (!BRAND_CHECK.matcher(brand).matches()) {
                    return new JsonResultBean(JsonResultBean.FAULT, "账号只能输入汉字、字母、数字或短横杠");
                }
                if (brand.length() > BIGGEST_BRAND_LENGTH) {
                    return new JsonResultBean(JsonResultBean.FAULT, "账号长度超过20个字符");
                }
                if (!PASSWORD_CHECK.matcher(vehiclePassword).matches()) {
                    return new JsonResultBean(JsonResultBean.FAULT, "密码只能输入大小写字母或0-9数字");
                }
                if (vehiclePassword.length() > BIGGEST_PASSWORD_LENGTH) {
                    return new JsonResultBean(JsonResultBean.FAULT, "密码长度超过6个字符");
                }
                return singleVehicleService.addLogAndSingleVehicleLogin(brand, vehiclePassword, request);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "账号不能为空");
            }
        } catch (Exception e) {
            log.error("单车登录小程序用户登录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysError);
        }
    }

    @RequestMapping(value = {"/location"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLocation(Brand vehicleBrand) {
        if (checkAuthAndBrand(vehicleBrand)) {
            return new JsonResultBean(JsonResultBean.FAULT, "认证失败,请先登录");
        }
        String brand = vehicleBrand.getBrand();
        if (!BRAND_CHECK.matcher(brand).matches()) {
            return new JsonResultBean(JsonResultBean.FAULT, "车牌号不符合平台要求");
        }
        // 获得访问ip
        String ipAddress = new GetIpAddr().getIpAddr(request);
        JsonResultBean result = singleVehicleService.getSingleVehicleLocation(brand);
        if (result.isSuccess()) {
            singleVehicleService.addLog(brand, ipAddress, "单车登录小程序: 获取位置信息");
        }
        return result;
    }

    @RequestMapping(value = {"/getMonitorHistoryData"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLocation(Brand vehicleBrand, String startTime, String endTime,
                                      HttpServletRequest request, Integer sensorFlag) {
        if (checkAuthAndBrand(vehicleBrand)) {
            return new JsonResultBean(JsonResultBean.FAULT, "认证失败,请先登录");
        }
        String brand = vehicleBrand.getBrand();
        if (checkParameter(brand, startTime, endTime) && !BRAND_CHECK.matcher(brand).matches()) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        // 获得访问ip
        String ipAddress = new GetIpAddr().getIpAddr(request);
        try {
            JsonResultBean result = singleVehicleService.getSingleVehicleHistoryData(
                    brand, startTime, endTime, sensorFlag);
            if (result.isSuccess()) {
                singleVehicleService.addLog(brand, ipAddress, "单车登录：获取轨迹回放数据");
            }
            return result;
        } catch (Exception e) {
            log.error("轨迹回放查询历史数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 校验参数是否符合要求
     *
     * @param brand     车牌号
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return boolean
     */
    private boolean checkParameter(String brand, String startTime, String endTime) {
        return StringUtils.isEmpty(brand) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime);
    }

    @RequestMapping(value = {"/getAlarmList"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmList(SingleVehicleAlarmSearchQuery query, Brand vehicleBrand) {
        if (checkAuthAndBrand(vehicleBrand)) {
            return new JsonResultBean(JsonResultBean.FAULT, "认证失败,请先登录");
        }
        String brand = vehicleBrand.getBrand();
        if (checkParameter(query)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        // 获得访问ip
        String ipAddress = new GetIpAddr().getIpAddr(request);
        JsonResultBean jsonResultBean = singleVehicleService.queryAlarmList(query, brand);

        if (jsonResultBean.isSuccess()) {
            singleVehicleService.addLog(brand, ipAddress, "单车登录小程序：查询报警记录");
        }
        return jsonResultBean;
    }

    @RequestMapping(value = {"/getAlarmPage"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getAlarmListByPage(SingleVehicleAlarmSearchQuery query, Brand vehicleBrand) {
        if (checkAuthAndBrand(vehicleBrand)) {
            return new PageGridBean(PageGridBean.FAULT, "认证失败,请先登录");
        }
        String brand = vehicleBrand.getBrand();
        try {
            return singleVehicleService.getPage(query, brand);
        } catch (Exception e) {
            logger.error("分页查询报警列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    private boolean checkParameter(SingleVehicleAlarmSearchQuery query) {
        return StringUtils.isEmpty(query.getAlarmStartTime())
                || StringUtils.isEmpty(query.getAlarmEndTime()) || StringUtils.isEmpty(query.getAlarmType());
    }

    @RequestMapping(value = {"/getMaintenanceReminder"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMaintenanceReminder(Brand vehicleBrand) {
        if (checkAuthAndBrand(vehicleBrand)) {
            return new JsonResultBean(JsonResultBean.FAULT, "认证失败,请先登录");
        }
        String brand = vehicleBrand.getBrand();
        return singleVehicleService.getMaintenanceReminder(brand);
    }

    /**
     * 修改车辆密码信息
     *
     * @param acount        车辆信息
     * @param bindingResult 绑定信息
     * @return JsonResultBean
     * @author XK
     */
    @RequestMapping(value = {"/updatePassword"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updatePassword(@Validated({ValidGroupAdd.class}) @ModelAttribute("acount") final
        SingleVehicleAcount acount, final BindingResult bindingResult, Brand vehicleBrand, String token) {
        try {
            if (acount != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                            SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (checkAuthAndBrand(vehicleBrand)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "认证失败,请先登录");
                    }
                    String brand = vehicleBrand.getBrand();
                    // 获得访问ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    if (!acount.getNewVehiclePassword().equals(acount.getConfirmNewVehiclePassword())) {
                        return new JsonResultBean(JsonResultBean.FAULT, "新密码和确认密码不一致！");
                    }
                    JsonResultBean result = singleVehicleService.updatePassword(acount, brand, token);
                    if (result.isSuccess()) {
                        singleVehicleService.addLog(brand, ipAddress, "单车登录小程序：修改车辆密码");
                    }
                    return result;
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("单车登录小程序：修改密码异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = {"/logOut"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean logOut(Brand vehicleBrand, @RequestParam(value = "token") String token) {
        if (checkAuthAndBrand(vehicleBrand)) {
            return new JsonResultBean(JsonResultBean.FAULT, "认证失败,请先登录");
        }
        String brand = vehicleBrand.getBrand();
        // 获得访问ip
        String ipAddress = new GetIpAddr().getIpAddr(request);
        JsonResultBean result = singleVehicleService.logOut(token, brand);
        if (result.isSuccess()) {
            singleVehicleService.addLog(brand, ipAddress, "单车登录小程序：登出");
        }
        return result;
    }

    private boolean checkAuthAndBrand(Brand brand) {
        return StringUtils.isEmpty(brand.getBrand());
    }

    /**
     * 单个逆地址编码
     *
     * @param addressReverse 经纬度
     * @return String 地址
     */
    @RequestMapping(value = {"/address"}, method = RequestMethod.POST)
    @ResponseBody
    public String address(String[] addressReverse) {

        if (addressReverse == null) {
            return "未定位";
        }
        try {
            if (addressReverse[1] != null && addressReverse[0] != null && !"0.0".equals(addressReverse[1])
                    && !"0".equals(addressReverse[1]) && !"".equals(addressReverse[1]) && !"0".equals(addressReverse[0])
                    && !"0.0".equals(addressReverse[0]) && !"".equals(addressReverse[0])
                    && addressReverse[1].length() >= 7 && addressReverse[0].length() >= 6) {
                String longitude = addressReverse[1].substring(0, 7);
                String latitude = addressReverse[0].substring(0, 6);
                return positionalService.getAddress(longitude, latitude);
            }
        } catch (Exception e) {
            log.error("获取单个逆地址编码异常", e);
        }
        return "未定位";
    }

    @RequestMapping(value = { "/synchronizeVehicle" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean synchronizeVehicle(@Validated({ ValidGroupAdd.class })
            @ModelAttribute("form") final SynchronizeVehicleForm form, final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new AppResultBean(AppResultBean.PARAM_ERROR,
                            SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    // 获得访问ip
                    String ipAddress = new GetIpAddr().getIpAddr(request);
                    boolean flag = vehicleService.updateSynchronizeVehicle(form, ipAddress);
                    if (flag) {
                        return new AppResultBean(AppResultBean.SUCCESS);
                    }
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("同步车辆信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = "monthMileageDetail", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean listMonthTerminalMileageDetail(Brand vehicleBrand, String month) {
        try {
            final YearMonth yearMonth = YearMonth.parse(month);
            final List<TerminalMileageDailyDetails> result =
                    singleVehicleService.listMonthTerminalMileageDetail(vehicleBrand.getBrand(), yearMonth);
            return new AppResultBean(result);
        } catch (DateTimeParseException e) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, "月份格式yyyy-MM");
        } catch (Exception e) {
            log.error("查询单车单月里程报表每日明细失败", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, e.getMessage());
        }
    }

}

