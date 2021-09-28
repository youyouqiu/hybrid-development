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
 * 对讲信息列表
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
     * 检查卡号，设备号，监控对象是否绑定过
     * @param inputId    sims 检查SIM卡，devices 检查设备，brands 检查监控对象
     * @param inputValue 卡号/设备号/监控对象
     * @return 绑定详情
     */
    @RequestMapping(value = "/infoinput/checkIsBound", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBound(String inputId, String inputValue, String monitorType) {
        Map<String, Object> result = intercomService.checkIsBind(inputId, inputValue, monitorType);
        return new JsonResultBean(result);
    }

    /**
     * 对讲信息录入
     * @param fastInputForm 对讲信息快速录入表单
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

        //判断监控对象、终端、sim卡
        Map<String, Object> checkBound = intercomService.checkIsBind(intercomDTO);
        if (Objects.nonNull(checkBound.get("msg"))) {
            return new JsonResultBean(JsonResultBean.FAULT, String.valueOf(checkBound.get("msg")));
        }
        boolean isBindLocateObject = (boolean) checkBound.get("isBindLocateObject");
        if (!isBindLocateObject) {
            return ControllerTemplate.getBooleanResult(() -> intercomService.add(intercomDTO), "对讲信息录入成功");
        }
        // todo 把定位对象转换成对讲对象
        return null;
    }

    /**
     * 对讲信息列表快速录入添加初始化信息
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/infoFastInput/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean pageBean() {
        return ControllerTemplate.getResultBean(() -> intercomService.getAddPageInitData());
    }

    /**
     * 查询对讲信息列表数据
     * @param query query
     * @return 分页返回对讲信息列表数据
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
     * 生成对讲对象
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
     * 生成对讲对象
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
     * 好友设置对讲对象树模糊查询
     * @param queryParam 对讲对象关键字
     * @param type       根节点是否可选 single 可选 multiple或空 不可选
     * @return 被压缩的树形结构
     */
    @RequestMapping(value = "/intercomObject/getIntercomObjectFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getIntercomObjectFuzzy(String type, String queryParam) {
        try {
            JSONArray treeNodes = intercomService
                .getIntercomBaseTree("monitor", type, queryParam, IntercomDTO.Status.SUCCESS_STATUS, true);
            return ZipUtil.compress(treeNodes.toJSONString());
        } catch (Exception e) {
            logger.error("模糊搜索对讲对象树结构压缩字符串时发生异常", e);
            return null;
        }
    }

    /**
     * 查询分组下的查询对讲对象
     * @return 被压缩的树节点列表
     */
    @RequestMapping(value = "/intercomObject/getAssignmentIntercomObject", method = RequestMethod.POST)
    @ResponseBody
    public String getIntercomObjectByAssignmentId(String assignmentId) {
        try {
            JSONArray treeNodes = intercomService.getTreeNodeByGroupId(assignmentId);
            return ZipUtil.compress(treeNodes.toJSONString());
        } catch (Exception e) {
            logger.error("根据群组获取对讲对象树节点压缩异常", e);
            return null;
        }
    }

    /**
     * 好友设置调度员树结构
     * @return ModelAndView
     */
    @RequestMapping(value = "/intercomObject/getDispatcherFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getDispatcherFuzzy(String queryParam) {
        try {
            JSONArray treeNodes = intercomService.getDispatcherTree(queryParam);
            return ZipUtil.compress(treeNodes.toJSONString());
        } catch (Exception e) {
            logger.error("模糊搜索调度员树结构压缩异常", e);
            return null;
        }
    }

    /**
     * 好友添加
     * @param userForm    [{"friendId":1,"type"1,"userId":1}]
     * @param monitorName 监控对象名称
     * @param userId      对讲对象平台ID
     * @return 添加结果
     */
    @RequestMapping(value = "/intercomObject/addFriends", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addFriends(String userForm, String monitorName, Long userId) {
        if (StringUtils.isBlank(userForm) || Objects.isNull(userId) || StringUtils.isBlank(monitorName)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数异常");
        }
        return ControllerTemplate.getBooleanResult(() -> intercomService.addFriend(userForm, monitorName, userId));
    }

    @RequestMapping(value = "/intercomObject/updateRecordStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateRecordStatus(String configId, Integer recordEnable) {
        return ControllerTemplate.getBooleanResult(() -> intercomService.updateRecordStatus(configId, recordEnable));
    }

    /**
     * 移除对讲对象
     * @param configIds configIds
     * @return JsonResultBean
     */
    @RequestMapping(value = "/intercomObject/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteIntercomObject(String configIds) {
        if (StringUtils.isBlank(configIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "请至少选择一条记录进行删除");
        }
        List<String> configList = Arrays.asList(configIds.split(","));
        return ControllerTemplate.getResult(() -> intercomService.unbindByConfigId(configList));
    }

    /**
     * 导出信息配置列表到excel
     * @param response
     */
    @RequestMapping(value = "/infoinput/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        ControllerTemplate.export(() -> intercomService.export(response), "对讲信息列表", response, "导出对讲信息配置列表异常");
    }

    /**
     * 模板下载
     * @param response 响应
     */
    @RequestMapping(value = "/infoinput/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        ControllerTemplate
            .export(() -> intercomService.generateTemplate(response), "对讲信息列表模板", response, "下载对讲信息列表模板异常");
    }

    /**
     * 导入对讲对象
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
            logger.error("导入对讲信息配置信息异常", e);
            resultBean = new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
        return resultBean;
    }

    /**
     * 修改对讲信息
     * @param editInputForm editInputForm
     * @param bindingResult bindingResult
     * @return 更新结果
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
     * 判断群组下绑定对象的数量是否超限
     * @param id        群组ID或分组ID
     * @param type      1 群组 2 组织
     * @param monitorId 监控对象ID 可为空
     * @return 响应体
     */
    @RequestMapping(value = "todo/infoinput/getAssignmentCount", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllAssignmentVehicleNumber(String id, int type, String monitorId) {
        return null;
    }

    /**
     * 对讲信息配置列表页面
     * @return String
     */
    @Auth
    @RequestMapping(value = { "/infoinput/list" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 对讲信息快速录入页面
     * @return 快速如录入页面
     */
    @Auth
    @RequestMapping(value = { "/infoFastInput/add" }, method = RequestMethod.GET)
    public String fastInputPage() {
        return FAST_INPUT_PAGE;
    }

    /**
     * 信息录入修改页面
     * @return 信息修改录入页面
     */
    @Auth
    @RequestMapping(value = { "/infoinput/edit" }, method = RequestMethod.GET)
    public String editPage() {
        return EDIT_PAGE;
    }

    /**
     * 信息导入页面
     * @return 信息导入页面
     */
    @Auth
    @RequestMapping(value = { "/infoinput/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    /**
     * 获取绑定信息详情页面
     * @return 获取绑定信息详情页面
     */
    @Auth
    @RequestMapping(value = { "/infoinput/details" }, method = RequestMethod.GET)
    public String detailsPage() {
        return DETAILS_PAGE;
    }

    /**
     * 好友设置
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
            logger.error("好友设置页面异常", e);
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
