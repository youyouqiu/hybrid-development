package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.InputTypeEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.impl.MonitorFactory;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.service.topspeed.TopSpeedService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.talkback.common.ControllerTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 信息录入
 * @author zhangjuan
 */
@Slf4j
@Controller
@RequestMapping("/m/infoconfig/infoFastInput")
public class FastInputController {
    private static final String ADD_PAGE = "modules/infoconfig/infoFastInput/add";
    @Autowired
    private ConfigService configService;

    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private TopSpeedService topSpeedService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SimCardService simCardService;

    /**
     * 信息配置-快速录入界面
     */
    @Auth
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String getPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean pageBean() {
        JSONObject msg = new JSONObject();
        //未绑定车辆信息
        msg.put("vehicleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.VEHICLE.getType()));
        //未绑定人员信息
        msg.put("peopleInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.PEOPLE.getType()));
        //未绑定 物品信息
        msg.put("thingInfoList", monitorFactory.getUbBindSelectList(MonitorTypeEnum.THING.getType()));

        //未绑定终端信息
        msg.put("deviceInfoList", deviceService.getUbBindSelectList(null, null));

        //未绑定sim卡信息
        msg.put("simCardInfoList", simCardService.getUbBindSelectList(null));

        // 极速录入终端信息
        msg.put("speedDeviceInfoList", topSpeedService.findDeviceData());

        OrganizationLdap organization = userService.getCurrentUserOrg();
        msg.put("orgId", Objects.isNull(organization) ? "" : organization.getUuid());
        msg.put("orgName", Objects.isNull(organization) ? "" : organization.getName());
        return new JsonResultBean(msg);
    }

    /**
     * 监控对象模糊搜索
     * @param search      关键字
     * @param monitorType 监控对象类型
     * @return 模糊搜索的监控对象
     */
    @RequestMapping(value = { "/fuzzyMonitor" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean fuzzyMonitor(String search, Integer monitorType) {
        if (monitorType == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象类型不能为空");
        }
        List<Map<String, Object>> monitors = monitorFactory.getUbBindSelectList(String.valueOf(monitorType), search);
        return new JsonResultBean(ImmutableMap.of("fuzzyMonitor", monitors));
    }

    /**
     * 终端模糊搜索
     * @param search
     * @return
     */
    @RequestMapping(value = { "/fuzzyDevice" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean fuzzyDevice(String search, Integer deviceType) {
        List<Map<String, String>> devices = deviceService.getUbBindSelectList(search, deviceType);
        return new JsonResultBean(ImmutableMap.of("fuzzyDevice", devices));
    }

    /**
     * sim卡模糊搜索
     * @param search
     * @return
     */
    @RequestMapping(value = { "/fuzzySimCard" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean fuzzySimCard(String search) {
        return new JsonResultBean(ImmutableMap.of("fuzzySimCard", simCardService.getUbBindSelectList(search)));
    }

    /**
     * 快速录入
     * @return JsonResultBean
     * @throws @Title: submits
     * @author wangjianyu
     */
    @RequestMapping(value = { "/submits" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("addForm1") final Config1Form form,
        final BindingResult bindingResult) {
        log.info("信息配置快速录入-原始入参{}", JSON.toJSONString(form));
        ConfigDTO bindDTO = form.convertAddConfig();
        bindDTO.setInputType(InputTypeEnum.FAST_INPUT);
        return ControllerTemplate.getBooleanResult(() -> configService.add(bindDTO));
    }

    /**
     * 极速录入
     * @return JsonResultBean
     * @throws @Title: submits
     * @author
     */
    @RequestMapping(value = { "/saveTopspeed" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean saveTopspeed(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("addForm1") final Config1Form form,
        final BindingResult bindingResult) {
        log.info("信息配置极速录入-原始入参{}", JSON.toJSONString(form));
        ConfigDTO bindDTO = form.convertAddConfig();
        bindDTO.setInputType(InputTypeEnum.TOP_SPEED_INPUT);
        return ControllerTemplate.getBooleanResult(() -> configService.add(bindDTO));
    }

    /**
     * 扫码录入
     * @return JsonResultBean
     * @throws @Title: submits
     * @author
     */
    @RequestMapping(value = { "/scanningRecord" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean scanningRecord(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("addForm1") final Config1Form form,
        final BindingResult bindingResult) {
        log.info("信息配置扫码录入-原始入参{}", JSON.toJSONString(form));
        ConfigDTO bindDTO = form.convertAddConfig();
        bindDTO.setInputType(InputTypeEnum.SCAN_INPUT);
        return ControllerTemplate.getBooleanResult(() -> configService.add(bindDTO));
    }

    /**
     * 生成随机监控对象编号
     * @return JsonResultBean
     * @throws @Title: getRandomNumbers
     * @author hujun
     */
    @RequestMapping(value = { "/getRandomNumbers" }, method = RequestMethod.POST)
    @ResponseBody
    public String getRandomNumbers(String sim, int monitorType) {
        return configService.getRandomMonitorName(sim, String.valueOf(monitorType));
    }
}
