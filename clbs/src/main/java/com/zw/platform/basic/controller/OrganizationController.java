package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import com.zw.platform.basic.dto.OrganizationUpsertDTO;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.domain.core.OperationForm;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.exception.OrganizationDeleteException;
import com.zw.platform.service.core.OperationService;
import com.zw.platform.service.reportManagement.InspectionAndSupervisionService;
import com.zw.platform.service.reportManagement.InspectionAndSupervisionService.OpType;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author wanxing
 * @Title: ??????controller
 * @date 2021/1/1415:14
 */
@Controller
@RequestMapping("/c/group")
@Slf4j
public class OrganizationController {

    private static final String ADD_PAGE = "core/uum/group/add";

    private static final String INSERT_PAGE = "core/uum/group/insert";

    private static final String EDIT_PAGE = "core/uum/group/edit";

    private static final String DETAIL_PAGE = "core/uum/group/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${experience.id}")
    private String experienceId;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private BusinessScopeService businessScopeService;

    @Autowired
    private InspectionAndSupervisionService inspectionAndSupervisionService;

    @RequestMapping(value = "/newGroup", method = GET)
    public String initNewGroup() {
        return "newGroup";
    }

    /**
     * ????????????
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/add.gsp", method = RequestMethod.GET)
    public ModelAndView addPage(@RequestParam("id") final String id, @RequestParam("pid") final String pid) {

        if (StringUtils.isEmpty(pid)) {
            return new ModelAndView(ERROR_PAGE);
        }
        try {
            OrganizationLdap gf = new OrganizationLdap();
            gf.setCid(id);
            gf.setPid(pid);
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            mav.addObject("result", gf);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     */
    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = "/insert.gsp", method = RequestMethod.GET)
    public ModelAndView insertPage(@RequestParam("pid") final String pid) {

        if (StringUtils.isEmpty(pid)) {
            return new ModelAndView(ERROR_PAGE);
        }
        try {
            OrganizationLdap org = organizationService.getOrgByEntryDn(pid);
            org.setCid(org.getUuid());
            org.setPid(pid);
            ModelAndView mav = new ModelAndView(INSERT_PAGE);
            mav.addObject("result", org);
            mav.addObject("isInsert", "true");
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/insertGroup", method = POST)
    @ResponseBody
    public JsonResultBean insertGroup(@Valid OrganizationUpsertDTO org, final BindingResult bindingResult) {

        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT,
                SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            final OrganizationLdap organizationLdap = org.toLdapDO();
            organizationLdap.setPid(java.net.URLDecoder.decode(organizationLdap.getPid(), "UTF-8"));
            organizationService.insert(organizationLdap);

            inspectionAndSupervisionService.setExtraInspectionReceivers(
                    organizationLdap, org.getExtraInspectionReceivers(), OpType.INSERT);
            return new JsonResultBean(organizationLdap.getOu());
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/newgroup", method = POST)
    @ResponseBody
    public JsonResultBean add(@Valid OrganizationUpsertDTO org, final BindingResult bindingResult) {

        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT,
                SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            final OrganizationLdap organizationLdap = org.toLdapDO();
            organizationLdap.setPid(java.net.URLDecoder.decode(organizationLdap.getPid(), "UTF-8"));
            organizationService.add(organizationLdap);

            inspectionAndSupervisionService.setExtraInspectionReceivers(
                    organizationLdap, org.getExtraInspectionReceivers(), OpType.ADD);
            return new JsonResultBean(organizationLdap.getOu());
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/addGroupRedis", method = POST)
    @ResponseBody
    public JsonResultBean addGroupRedis(String ou) {
        try {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(final String id) {

        if (StringUtils.isEmpty(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        //??????????????????????????????????????????
        if (experienceId.equals(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????");
        }
        try {
            final OrganizationLdap deletedOrg = organizationService.delete(id);
            if (deletedOrg != null) {
                inspectionAndSupervisionService.setExtraInspectionReceivers(
                        deletedOrg, new ArrayList<>(), OpType.DELETE);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (OrganizationDeleteException e) {
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@RequestParam("pid") final String id) {
        if (StringUtils.isEmpty(id)) {
            return new ModelAndView(ERROR_PAGE);
        }
        try {
            OrganizationLdap org = organizationService.getOrgByEntryDn(java.net.URLDecoder.decode(id, "UTF-8"));
            generateOrganizationLdap(org);

            final List<String> extraInspectionReceivers =
                    inspectionAndSupervisionService.getExtraInspectionReceivers(org);

            final OrganizationUpsertDTO result = new OrganizationUpsertDTO(org);
            result.setExtraInspectionReceivers(extraInspectionReceivers);

            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", result);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ?????????????????????????????????
     * @param org
     */
    private void generateOrganizationLdap(OrganizationLdap org) throws Exception {
        if (org == null) {
            return;
        }
        org.setPid(org.getId().toString());
        Operations operations = operationService.findOperationByOperation(org.getOperation());
        if (operations == null) {
            org.setOperation("");
        }
        List<BusinessScopeDTO> businessScope = businessScopeService.getBusinessScope(org.getUuid());
        if (CollectionUtils.isEmpty(businessScope)) {
            org.setScopeOfOperation("");
            return;
        }
        String businessScopeIds =
            businessScope.stream().map(BusinessScopeDTO::getBusinessScopeId).collect(Collectors.joining(","));
        String businessScopes =
            businessScope.stream().map(BusinessScopeDTO::getBusinessScope).collect(Collectors.joining(","));
        String businessScopeCodes =
            businessScope.stream().map(BusinessScopeDTO::getBusinessScopeCode).collect(Collectors.joining(","));
        org.setScopeOfOperation(businessScopes);
        org.setScopeOfOperationIds(businessScopeIds);
        org.setScopeOfOperationCodes(businessScopeCodes);
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/edits.gsp", method = POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final OrganizationUpsertDTO org,
        final BindingResult bindingResult) {
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            final OrganizationLdap orgLdap = org.toLdapDO();
            // ????????????
            organizationService.update(orgLdap);

            inspectionAndSupervisionService.setExtraInspectionReceivers(
                    orgLdap, org.getExtraInspectionReceivers(), OpType.UPDATE);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/template", method = GET)
    public void greatTemplate(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "??????????????????");
            organizationService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/import", method = POST)
    @ResponseBody
    public JsonResultBean importGroup(@RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam(value = "pid") String pid) {
        try {
            Map<String, Object> resultMap = organizationService.importOrg(file, pid);
            String msg = "???????????????" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");

            @SuppressWarnings("unchecked")
            final List<String> newOrgIds = (List<String>) resultMap.remove("newOrgIds");
            if (CollectionUtils.isNotEmpty(newOrgIds)) {
                inspectionAndSupervisionService.batchCopySuperiorReceivers(newOrgIds, pid);
            }
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/detail.gsp", method = RequestMethod.GET)
    public ModelAndView detailPage(@RequestParam("pid") final String orgDn) {

        if (StringUtils.isEmpty(orgDn)) {
            return new ModelAndView(ERROR_PAGE);
        }
        try {
            OrganizationLdap org = organizationService.getOrgByEntryDn(java.net.URLDecoder.decode(orgDn, "UTF-8"));
            generateOrganizationLdap(org);
            final List<String> extraInspectionReceivers =
                    inspectionAndSupervisionService.getExtraInspectionReceivers(org);
            final OrganizationUpsertDTO result = new OrganizationUpsertDTO(org);
            result.setExtraInspectionReceivers(extraInspectionReceivers == null
                    ? Collections.emptyList()
                    : extraInspectionReceivers);
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            mav.addObject("result", result);
            return mav;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ????????????????????????
     * @param addproperationtype ??????????????????
     * @param adddescription     ??????
     * @return
     */
    @RequestMapping(value = "/addOperational", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addOperational(String addproperationtype, String adddescription,
        HttpServletRequest request) {

        if (StringUtils.isEmpty(adddescription) && StringUtils.isEmpty(addproperationtype)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        try {
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return operationService.addOperation(addproperationtype, adddescription, ipAddress);
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @return
     */
    @RequestMapping(value = "/findOperations", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperations(String type) {

        if (type == null) {
            type = "";
        }
        try {
            JSONObject msg = new JSONObject();
            List<Operations> operation = operationService.findOperation(type);
            msg.put("operation", operation);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteOperation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteOperation(String id, HttpServletRequest request) {

        if (StringUtils.isEmpty(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        try {
            // ?????????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            List<String> ids = Collections.singletonList(id);
            return operationService.deleteOperation(ids, ipAddress);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id????????????????????????
     * @param id
     * @return
     */
    @RequestMapping(value = "/findOperationById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperationById(String id) {
        if (StringUtils.isEmpty(id)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        try {
            JSONObject msg = new JSONObject();
            Operations operation = operationService.findOperationById(id);
            msg.put("operation", operation);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     * @param id
     * @param operationType
     * @param explains
     * @return
     */
    @RequestMapping(value = "/updateOperation", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateOperations(String id, String operationType,
        String explains, HttpServletRequest request) {

        if (StringUtils.isEmpty(id) && StringUtils.isEmpty(operationType) && StringUtils.isEmpty(explains)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        try {
            OperationForm operationForm = new OperationForm();
            // ??????????????????IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            operationForm.setId(id);
            operationForm.setOperationType(operationType);
            operationForm.setExplains(explains);
            return operationService.updateOperation(operationForm, ipAddress);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????????????????????????????(?????????????????????????????????)
     * @return
     */
    @RequestMapping(value = "/findOperationByoperation", method = RequestMethod.POST)
    @ResponseBody
    public boolean findOperationByOperation(String type) {
        if (StringUtils.isEmpty(type)) {
            return false;
        }
        try {
            Operations operation = operationService.findOperationByOperation(type);
            // ?????????????????????????????????????????????
            return operation == null;
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return false;
        }

    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    @RequestMapping(value = "/findOperationCompare", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperationCompare(String type, String recomposeType) {

        if (StringUtils.isEmpty(type)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        try {
            // ?????????type????????????
            Operations operation = operationService.findOperationByOperation(type);
            // ????????????????????????????????????????????????,????????????????????????????????????,?????????true
            if (operation == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else { // ??????,??????????????????????????????
                if (type.equals(recomposeType)) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("??????????????????????????????????????????????????????validate.remote????????????");
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteOperationMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteOperations(String ids, HttpServletRequest request) {
        if (StringUtils.isEmpty(ids)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        try {
            List<String> operationId = Arrays.asList(ids.split(","));
            // ????????????????????????????????????ip??????
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return operationService.deleteOperation(operationId, ipAddress);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????????????????
     * @param organizationCode
     * @return
     */
    @RequestMapping(value = "/uniquenessOrganizationCode", method = RequestMethod.POST)
    @ResponseBody
    public boolean codeUniqueness(String organizationCode) {

        if (StringUtils.isEmpty(organizationCode)) {
            return false;
        }
        try {
            List<OrganizationLdap> list = organizationService.getAllOrganization();
            boolean flag = true;
            for (OrganizationLdap organizationLdap : list) {
                if (organizationCode.equals(organizationLdap.getOrganizationCode())) {
                    flag = false;
                    break;
                }
            }
            return flag;
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return false;
        }
    }

    /**
     * ???????????????????????????????????????
     * @param license
     * @return
     */
    @RequestMapping(value = "/uniquenessLicense", method = RequestMethod.POST)
    @ResponseBody
    public boolean licenseUniqueness(String license) {

        if (StringUtils.isEmpty(license)) {
            return false;
        }
        try {
            List<OrganizationLdap> list = organizationService.getAllOrganization();
            boolean flag = true;
            for (OrganizationLdap organizationLdap : list) {
                if (license.equals(organizationLdap.getLicense())) {
                    flag = false;
                    break;
                }
            }
            return flag;
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????", e);
            return false;
        }
    }
}
