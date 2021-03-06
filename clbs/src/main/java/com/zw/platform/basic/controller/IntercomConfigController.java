package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.dto.FriendDTO;
import com.zw.platform.basic.dto.IntercomDTO;
import com.zw.platform.basic.dto.query.IntercomQuery;
import com.zw.platform.basic.service.IntercomService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.impl.MonitorFactory;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.talkback.common.ControllerTemplate;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.InConfigInfoForm;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.service.baseinfo.OriginalModelService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ??????????????????
 * @author zhangjuan
 */
@Controller
@RequestMapping("/talkback/inconfig")
public class IntercomConfigController {
    private static final Logger logger = LogManager.getLogger(IntercomConfigController.class);
    private static final String LIST_PAGE = "talkback/basicinfo/inconfig/list";

    private static final String FAST_INPUT_PAGE = "talkback/basicinfo/inconfig/infoFastInput/add";

    private static final String EDIT_PAGE = "talkback/basicinfo/inconfig/edit";

    private static final String IMPORT_PAGE = "talkback/basicinfo/inconfig/import";

    private static final String DETAILS_PAGE = "talkback/basicinfo/inconfig/details";

    private static final String FRIENDS_PAGE = "talkback/basicinfo/inconfig/addFriends";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private IntercomService intercomService;

    @Autowired
    private OriginalModelService originalModelService;

    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private SimCardService simCardService;

    @Autowired
    private MessageConfig messageConfig;

    /**
     * ??????????????????????????????????????????????????????
     * @param inputId    sims ??????SIM??????devices ???????????????brands ??????????????????
     * @param inputValue ??????/?????????/????????????
     * @return ????????????
     */
    @RequestMapping(value = "/infoinput/checkIsBound", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBound(String inputId, String inputValue, String monitorType) {
        Map<String, Object> result = intercomService.checkIsBind(inputId, inputValue, monitorType);
        return new JsonResultBean(result);
    }

    /**
     * ??????????????????
     * @param fastInputForm ??????????????????????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/infoFastInput/submits", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("fastInputForm") final InConfigInfoForm fastInputForm,
        final BindingResult bindingResult) {
        if (fastInputForm == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        IntercomDTO intercomDTO = fastInputForm.convert();

        //??????????????????????????????sim???
        Map<String, Object> checkBound = intercomService.checkIsBind(intercomDTO);
        if (Objects.nonNull(checkBound.get("msg"))) {
            return new JsonResultBean(JsonResultBean.FAULT, String.valueOf(checkBound.get("msg")));
        }
        boolean isBindLocateObject = (boolean) checkBound.get("isBindLocateObject");
        if (!isBindLocateObject) {
            return ControllerTemplate.getBooleanResult(() -> intercomService.add(intercomDTO), "????????????????????????");
        }
        // todo ????????????????????????????????????
        return null;
    }

    /**
     * ???????????????????????????????????????????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/infoFastInput/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean pageBean() {
        return ControllerTemplate.getResultBean(() -> intercomService.getAddPageInitData());
    }

    /**
     * ??????????????????????????????
     * @param query query
     * @return ????????????????????????????????????
     */
    @RequestMapping(value = { "/infoinput/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final ConfigQuery query) {
        if (query == null) {
            return new PageGridBean(PageGridBean.FAULT);
        }
        IntercomQuery intercomQuery = new IntercomQuery();
        BeanUtils.copyProperties(query, intercomQuery);
        Page<IntercomDTO> intercomList = intercomService.getByKeyword(intercomQuery);
        List<IntercomObjectInfo> intercomObjectList = new ArrayList<>();
        for (IntercomDTO intercomDTO : intercomList) {
            IntercomObjectInfo intercomObject = new IntercomObjectInfo(intercomDTO);
            intercomObject.setIntercomDeviceId(intercomDTO.getIntercomDeviceNumber());
            intercomObjectList.add(intercomObject);
        }

        int total = Integer.parseInt(String.valueOf(intercomList.getTotal()));
        return new PageGridBean(RedisQueryUtil.getListToPage(intercomObjectList, query, total), true);
    }

    /**
     * ??????????????????
     * @param configId configId
     * @return JsonResultBean
     */
    @RequestMapping(value = "/intercomObject/generatorIntercomInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addIntercomInfoToIntercomPlatform(String configId) {
        if (StringUtils.isBlank(configId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return intercomService.addToIntercomPlatform(Collections.singletonList(configId));
    }

    /**
     * ??????????????????
     * @param configIds configIds
     * @return JsonResultBean
     */
    @RequestMapping(value = "/intercomObject/generatorIntercomInfoBatch", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean generatorIntercomInfoBatch(String configIds) {
        if (StringUtils.isBlank(configIds)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<String> configList = Arrays.asList(configIds.split(","));
        return intercomService.addToIntercomPlatform(configList);
    }

    /**
     * ???????????????????????????????????????
     * @param queryParam ?????????????????????
     * @param type       ????????????????????? single ?????? multiple?????? ?????????
     * @return ????????????????????????
     */
    @RequestMapping(value = "/intercomObject/getIntercomObjectFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getIntercomObjectFuzzy(String type, String queryParam) {
        try {
            JSONArray treeNodes = intercomService
                .getIntercomBaseTree("monitor", type, queryParam, IntercomDTO.Status.SUCCESS_STATUS, true);
            return ZipUtil.compress(treeNodes.toJSONString());
        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????????????????", e);
            return null;
        }
    }

    /**
     * ????????????????????????????????????
     * @return ???????????????????????????
     */
    @RequestMapping(value = "/intercomObject/getAssignmentIntercomObject", method = RequestMethod.POST)
    @ResponseBody
    public String getIntercomObjectByAssignmentId(String assignmentId) {
        try {
            JSONArray treeNodes = intercomService.getTreeNodeByGroupId(assignmentId);
            return ZipUtil.compress(treeNodes.toJSONString());
        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????", e);
            return null;
        }
    }

    /**
     * ??????????????????????????????
     * @return ModelAndView
     */
    @RequestMapping(value = "/intercomObject/getDispatcherFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getDispatcherFuzzy(String queryParam) {
        try {
            JSONArray treeNodes = intercomService.getDispatcherTree(queryParam);
            return ZipUtil.compress(treeNodes.toJSONString());
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return null;
        }
    }

    /**
     * ????????????
     * @param userForm    [{"friendId":1,"type"1,"userId":1}]
     * @param monitorName ??????????????????
     * @param userId      ??????????????????ID
     * @return ????????????
     */
    @RequestMapping(value = "/intercomObject/addFriends", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addFriends(String userForm, String monitorName, Long userId) {
        if (StringUtils.isBlank(userForm) || Objects.isNull(userId) || StringUtils.isBlank(monitorName)) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        return ControllerTemplate.getBooleanResult(() -> intercomService.addFriend(userForm, monitorName, userId));
    }

    @RequestMapping(value = "/intercomObject/updateRecordStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateRecordStatus(String configId, Integer recordEnable) {
        return ControllerTemplate.getBooleanResult(() -> intercomService.updateRecordStatus(configId, recordEnable));
    }

    /**
     * ??????????????????
     * @param configIds configIds
     * @return JsonResultBean
     */
    @RequestMapping(value = "/intercomObject/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteIntercomObject(String configIds) {
        if (StringUtils.isBlank(configIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        }
        List<String> configList = Arrays.asList(configIds.split(","));
        return ControllerTemplate.getResult(() -> intercomService.unbindByConfigId(configList));
    }

    /**
     * ???????????????????????????excel
     * @param response
     */
    @RequestMapping(value = "/infoinput/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> intercomService.export(response), "??????????????????", response, "????????????????????????????????????");
    }

    /**
     * ????????????
     * @param response ??????
     */
    @RequestMapping(value = "/infoinput/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> intercomService.generateTemplate(response), "????????????????????????", response, "????????????????????????????????????");
    }

    /**
     * ??????????????????
     * @param file file
     * @return JsonResultBean
     */
    @RequestMapping(value = "/infoinput/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importExcel(@RequestParam(value = "file", required = false) MultipartFile file) {
        JsonResultBean resultBean;
        try {
            resultBean = intercomService.importFile(file);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            resultBean = new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
        return resultBean;
    }

    /**
     * ??????????????????
     * @param editInputForm editInputForm
     * @param bindingResult bindingResult
     * @return ????????????
     */
    @RequestMapping(value = "/infoinput/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final InConfigInfoForm editInputForm,
        final BindingResult bindingResult) {
        if (Objects.isNull(editInputForm)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        IntercomDTO intercomDTO = editInputForm.convert();

        return ControllerTemplate.getBooleanResult(() -> intercomService.update(intercomDTO));
    }

    /**
     * ????????????????????????????????????????????????
     * @param id        ??????ID?????????ID
     * @param type      1 ?????? 2 ??????
     * @param monitorId ????????????ID ?????????
     * @return ?????????
     */
    @RequestMapping(value = "todo/infoinput/getAssignmentCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllAssignmentVehicleNumber(String id, int type, String monitorId) {
        return null;
    }

    /**
     * ??????????????????????????????
     * @return String
     */
    @Auth
    @RequestMapping(value = { "/infoinput/list" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * ??????????????????????????????
     * @return ?????????????????????
     */
    @Auth
    @RequestMapping(value = { "/infoFastInput/add" }, method = RequestMethod.GET)
    public String fastInputPage() {
        return FAST_INPUT_PAGE;
    }

    /**
     * ????????????????????????
     * @return ????????????????????????
     */
    @Auth
    @RequestMapping(value = { "/infoinput/edit" }, method = RequestMethod.GET)
    public String editPage() {
        return EDIT_PAGE;
    }

    /**
     * ??????????????????
     * @return ??????????????????
     */
    @Auth
    @RequestMapping(value = { "/infoinput/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * ??????????????????????????????
     * @return ??????????????????????????????
     */
    @Auth
    @RequestMapping(value = { "/infoinput/details" }, method = RequestMethod.GET)
    public String detailsPage() {
        return DETAILS_PAGE;
    }

    /**
     * ????????????
     * @return ModelAndView
     */
    @RequestMapping(value = "/infoinput/getAddFriendsPage_{type}_{configId}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView getAddFriendsPage(@PathVariable("type") String type,
        @PathVariable("configId") String configId) {
        try {
            ModelAndView modelAndView = new ModelAndView(FRIENDS_PAGE);
            IntercomDTO intercomDTO = intercomService.getDetailByConfigId(configId);
            modelAndView.addObject("maxFriendNum", intercomDTO.getMaxFriendNum());
            modelAndView.addObject("monitorName", intercomDTO.getName());
            modelAndView.addObject("userId", intercomDTO.getUserId());
            List<FriendDTO> friends = intercomService.getFriends(intercomDTO.getUserId());
            modelAndView.addObject("friends", JSON.toJSONString(friends));
            return modelAndView;
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/infoinput/edit_{configId}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String configId) {
        try {

            IntercomDTO intercomDTO = intercomService.getDetailByConfigId(configId);
            IntercomObjectInfo intercom = Objects.isNull(intercomDTO) ? null : new IntercomObjectInfo(intercomDTO);
            List<OriginalModelInfo> originalModelList = originalModelService.getAllOriginalModel();
            List<Map<String, Object>> vehicleInfoList =
                monitorFactory.getUbBindSelectList(MonitorTypeEnum.VEHICLE.getType());
            List<Map<String, Object>> peopleInfoList =
                monitorFactory.getUbBindSelectList(MonitorTypeEnum.PEOPLE.getType());
            List<Map<String, Object>> thingInfoList =
                monitorFactory.getUbBindSelectList(MonitorTypeEnum.THING.getType());
            List<Map<String, String>> simCardList = simCardService.getUbBindSelectList(null);
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("intercomObject", intercom);
            mav.addObject("vehicleInfoList", vehicleInfoList == null ? null : JSON.toJSONString(vehicleInfoList));
            mav.addObject("peopleInfoList", peopleInfoList == null ? null : JSON.toJSONString(peopleInfoList));
            mav.addObject("thingInfoList", thingInfoList == null ? null : JSON.toJSONString(thingInfoList));
            mav.addObject("simCardInfoList", simCardList == null ? null : JSON.toJSONString(simCardList));
            mav.addObject("originalModelList", originalModelList == null ? null : JSON.toJSONString(originalModelList));
            return mav;
        } catch (Exception e) {
            return new ModelAndView(ERROR_PAGE);
        }
    }

}
