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
 * <p> Title: sim???controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
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
     * ????????????
     * @return ??????
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
            log.error("????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     * @param query query
     * @return ????????????
     * @throws BusinessException exception
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final SimCardQuery query) {
        return ControllerTemplate.getPageGridBean(() -> simCardService.getListByKeyWord(query), query, "????????????SIM???????????????");
    }

    /**
     * ??????????????????
     * @return ????????????
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ??????sim?????????
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
            .getResultBean(() -> simCardService.add(SimCardDTO.getAddInstance(form)), "??????sim???????????????", bindingResult);
    }

    /**
     * ??????id??????sim???
     * @param id SIM???Id
     * @return result
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        return ControllerTemplate.getResultBean(() -> simCardService.delete(id), "??????sim???????????????");
    }

    /**
     * ????????????
     * @param request request
     * @return result
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        return ControllerTemplate
            .getResultBean(() -> simCardService.deleteBatch(Arrays.asList(request.getParameter("deltems").split(","))),
                "????????????sim???????????????");
    }

    /**
     * ??????sim???
     * @param id sim???Id
     * @return ????????????
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        return ControllerTemplate.editPage(EDIT_PAGE, () -> simCardService.getById(id));
    }

    /**
     * ??????sim???
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
            .getResultBean(() -> simCardService.updateNumber(SimCardDTO.getUpdateInstance(form)), "??????sim???????????????",
                bindingResult);

    }

    /**
     * ??????
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> simCardService.exportSimCard(), "SIM?????????", response, "??????sim???????????????");
    }

    /**
     * ????????????
     * @param response response
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> simCardService.generateTemplate(response), "???????????????????????????", response, "???????????????????????????????????????");

    }

    /**
     * @return String
     * @author wangying
     * @Title: ??????
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * ??????
     * @param file ??????
     * @return result
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importSimCard(@RequestParam(value = "file", required = false) MultipartFile file) {
        return ControllerTemplate.getResultBean(() -> simCardService.importData(file), "??????sim???????????????");
    }

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportDeviceError(HttpServletResponse response) {
        try {
            ImportErrorUtil.generateErrorExcel(ImportModule.SIM_CARD, "?????????????????????????????????", null, response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * repetition
     * @param simcardNumber sim??????
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
            log.error("??????sim?????????????????????", e);
            return false;
        }
    }

    /**
     * ??????id??????????????????
     * @return result
     */
    @RequestMapping(value = "/sendSimP", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendSimP(SendSimCard sendSimCard) {
        try {
            return simCardService.sendSimCard(sendSimCard);
        } catch (Exception e) {
            log.error("SIM??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ??????????????????SIM???
     * @param form sim?????????
     * @return ????????????
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
            log.error("??????????????????SIM???????????????", e);
            return false;
        }
    }

    /**
     * ????????????????????????
     * @param vehicleId ??????Id
     * @return ??????
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
                String logs = "???????????????" + brand + " ??????SIM?????????";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????SIM???????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????sim???????????????
     * @param vid ????????????id
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/simIssueLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean simIssueLog(String vid) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);
            BindDTO bindDTO = MonitorUtils.getBindDTO(vid);
            if (Objects.nonNull(bindDTO) && StringUtils.isNotBlank(bindDTO.getId())) {
                String logs = String.format("???????????????%s ( @%s ) SIM?????????????????????", bindDTO.getName(), bindDTO.getOrgName());
                String plateColor = bindDTO.getPlateColor() != null ? bindDTO.getPlateColor().toString() : "";
                logSearchService.addLog(ip, logs, "2", "", bindDTO.getName(), plateColor);

            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("??????????????????sim?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ????????????sim???????????????
     * @param vehicleId ??????Id
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
                String logs = "???????????????" + brand + " sim???????????????";
                logSearchService.addLog(ip, logs, "3", "MONITORING", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("sim?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * ????????????????????????-??????SIM?????????,????????????????????????SIM????????????,?????????????????????SIM??????
     * ?????????F3???????????????sim???????????????SIM???????????????
     */
    @RequestMapping(value = { "/updateRealId" }, method = RequestMethod.POST)
    @ResponseBody
    public void saveRealSimCard(String simCardId, String realCard) {
        try {
            simCardService.updateNumber(simCardId, null, realCard);
        } catch (Exception e) {
            log.error("????????????SIM???????????????", e);
        }

    }
}
