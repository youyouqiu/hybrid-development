package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.query.SimCardQuery;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.SendSimCard;
import com.zw.platform.domain.basicinfo.form.SimcardForm;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p> Title: sim卡controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 */
@Controller
@RequestMapping("/m/basicinfo/equipment/simcard")
public class NewSimCardController {
    private static final Logger log = LogManager.getLogger(NewSimCardController.class);

    @Autowired
    private SimCardService simCardService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private LogSearchService logSearchService;

    private static final String LIST_PAGE = "modules/basicinfo/equipment/simcard/list";

    private static final String ADD_PAGE = "modules/basicinfo/equipment/simcard/add";

    private static final String EDIT_PAGE = "modules/basicinfo/equipment/simcard/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/equipment/simcard/import";

    private static final String PROOFREADING_PAGE = "vas/monitoring/proofreading";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 分页页面
     * @return 地址
     * @throws BusinessException exception
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * proofreadingPage
     * @param id id
     * @return modelandview
     */
    @Auth
    @RequestMapping(value = { "/proofreading_{id}" }, method = RequestMethod.GET)
    public ModelAndView proofreadingPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PROOFREADING_PAGE);
            mav.addObject("result", simCardService.getF3SimInfo(id));
            return mav;
        } catch (Exception e) {
            log.error("下发界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 分页查询
     * @param query query
     * @return 分页结果
     * @throws BusinessException exception
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final SimCardQuery query) {
        return ControllerTemplate.getPageGridBean(() -> simCardService.getListByKeyWord(query), query, "分页查询SIM卡信息异常");
    }

    /**
     * 跳转新增页面
     * @return 页面路径
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增sim卡信息
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final SimcardForm form,
        final BindingResult bindingResult) {
        return ControllerTemplate
            .getResultBean(() -> simCardService.add(SimCardDTO.getAddInstance(form)), "新增sim卡信息异常", bindingResult);
    }

    /**
     * 根据id删除sim卡
     * @param id SIM卡Id
     * @return result
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        return ControllerTemplate.getResultBean(() -> simCardService.delete(id), "删除sim卡信息异常");
    }

    /**
     * 批量删除
     * @param request request
     * @return result
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> simCardService.deleteBatch(Arrays.asList(request.getParameter("deltems").split(","))),
                "批量删除sim卡信息异常");
    }

    /**
     * 修改sim卡
     * @param id sim卡Id
     * @return 修改页面
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        return ControllerTemplate.editPage(EDIT_PAGE, () -> simCardService.getById(id));
    }

    /**
     * 修改sim卡
     * @param form          form
     * @param bindingResult bindingResult
     * @return JsonResultBean
     * @author wangying
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SimcardForm form,
        final BindingResult bindingResult) {
        return ControllerTemplate
            .getResultBean(() -> simCardService.updateNumber(SimCardDTO.getUpdateInstance(form)), "修改sim卡信息异常",
                bindingResult);

    }

    /**
     * 导出
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> simCardService.exportSimCard(), "SIM卡列表", response, "导出sim卡信息异常");
    }

    /**
     * 下载模板
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> simCardService.generateTemplate(response), "终端手机号列表模板", response, "下载终端手机号列表模板异常");

    }

    /**
     * @return String
     * @author wangying
     * @Title: 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 导入
     * @param file 文件
     * @return result
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSimCard(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ControllerTemplate.getResultBean(() -> simCardService.importData(file), "导入sim卡信息异常");
    }

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportDeviceError(HttpServletResponse response) {
        try {
            ImportErrorUtil.generateErrorExcel(ImportModule.SIM_CARD, "终端手机号导入错误信息", null, response);
        } catch (Exception e) {
            log.error("导出终端错误信息异常", e);
        }
    }

    /**
     * repetition
     * @param simcardNumber sim卡号
     * @return result
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("simcardNumber") String simcardNumber) {
        try {
            if (simcardNumber != null && simcardNumber.length() == 13 && simcardNumber.startsWith("106")) {
                simcardNumber = "1" + simcardNumber.substring(3);
            }
            return simCardService.getByNumber(simcardNumber) == null;
        } catch (Exception e) {
            log.error("校验sim卡信息存在异常", e);
            return false;
        }
    }

    /**
     * 根据id下发参数设置
     * @return result
     */
    @RequestMapping(value = "/sendSimP", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendSimP(SendSimCard sendSimCard) {
        try {
            return simCardService.sendSimCard(sendSimCard);
        } catch (Exception e) {
            log.error("SIM下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 实时监控修改SIM卡
     * @param form sim卡信息
     * @return 是否成功
     */
    @RequestMapping(value = "/updataSimCradInfo", method = RequestMethod.POST)
    @ResponseBody
    public Boolean updateSimCardInfo(String form) {
        try {
            SimcardForm simcardForm = JSON.parseObject(form, SimcardForm.class);
            simcardForm.setId(simcardForm.getSid());
            simcardForm.setMonthlyStatement("01");
            simCardService.updateNumber(SimCardDTO.getUpdateInstance(simcardForm));
            return true;
        } catch (Exception e) {
            log.error("实时监控修改SIM卡信息异常", e);
            return false;
        }
    }

    /**
     * 查看历史轨迹日志
     * @param vehicleId 车辆Id
     * @return 日志
     */
    @RequestMapping(value = { "/simLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean simLog(String vehicleId) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logs = "监控对象：" + brand + " 获得SIM卡信息";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("获得SIM卡信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 实时监控查看sim卡下发日志
     * @param vid 监控对象id
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/simIssueLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean simIssueLog(String vid) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            BindDTO bindDTO = MonitorUtils.getBindDTO(vid);
            if (Objects.nonNull(bindDTO) && StringUtils.isNotBlank(bindDTO.getId())) {
                String logs = String.format("监控对象：%s ( @%s ) SIM卡信息修正下发", bindDTO.getName(), bindDTO.getOrgName());
                String plateColor = bindDTO.getPlateColor() != null ? bindDTO.getPlateColor().toString() : "";
                logSearchService.addLog(ip, logs, "2", "", bindDTO.getName(), plateColor);

            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("实时监控查看sim卡下发日志异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 实时监控sim卡修正日志
     * @param vehicleId 车辆Id
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/csimLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean csimLog(String vehicleId) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logs = "监控对象：" + brand + " sim卡信息修正";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("sim卡信息修正异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 实时监控右键指令-获取SIM卡信息,物联网卡平台返回SIM卡数据后,将物联网卡平台SIM卡号
     * 存储到F3平台对应的sim卡号的真实SIM卡号字段中
     */
    @RequestMapping(value = { "/updateRealId" }, method = RequestMethod.POST)
    @ResponseBody
    public void saveRealSimCard(String simCardId, String realCard) {
        try {
            simCardService.updateNumber(simCardId, null, realCard);
        } catch (Exception e) {
            log.error("更新真实SIM卡信息异常", e);
        }

    }
}
