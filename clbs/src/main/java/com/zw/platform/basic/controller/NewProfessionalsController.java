package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.zw.adas.utils.FastDFSClient;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.ProfessionalPageDTO;
import com.zw.platform.basic.dto.query.NewProfessionalsQuery;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.FileUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.talkback.domain.basicinfo.BasicInfo;
import com.zw.talkback.service.baseinfo.PeopleBasicInfoService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p> Title: ????????????controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016???7???26?????????4:44:08
 */
@Controller
@RequestMapping("/m/basicinfo/enterprise/professionals")
public class NewProfessionalsController {
    private static Logger log = LogManager.getLogger(NewProfessionalsController.class);

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    ProfessionalService professionalService;

    @Autowired
    PeopleBasicInfoService peopleBasicInfoService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;


    @Autowired
    ConfigHelper configHelper;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    private static final String LIST_PAGE = "modules/basicinfo/enterprise/professionals/list";

    private static final String ADD_PAGE = "modules/basicinfo/enterprise/professionals/add";

    private static final String EDIT_PAGE = "modules/basicinfo/enterprise/professionals/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/enterprise/professionals/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * ???????????????token??????
     */
    @RequestMapping("/generateFormToken")
    @AvoidRepeatSubmitToken(setToken = true)
    @ResponseBody
    public JsonResultBean generateFormToken(HttpServletRequest request) {
        String formKey = (String) request.getSession().getAttribute("avoidRepeatSubmitToken");
        return new JsonResultBean(true, formKey);
    }

    /**
     * ????????????
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final ProfessionalsQuery query) {
        try {
            if (query != null) {
                NewProfessionalsQuery newProfessionalsQuery = new NewProfessionalsQuery();
                BeanUtils.copyProperties(query, newProfessionalsQuery);
                newProfessionalsQuery.setOrgId(query.getGroupName());
                Page<ProfessionalPageDTO> listPage = professionalService.getListPage(newProfessionalsQuery);
                Page<Map<String, Object>> re =
                    new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
                re.setTotal(listPage.getTotal());
                listPage.stream().forEach(o -> {
                    Map map = JSON.parseObject(JSON.toJSONString(o), Map.class);
                    Object drivingStartDate = map.get("drivingStartDate");
                    Object drivingEndDate = map.get("drivingEndDate");
                    map.put("drivingStartDate", drivingStartDate == null ? null :
                        DateUtil.getStringToLong(drivingStartDate.toString(), DateUtil.DATE_Y_M_D_FORMAT));
                    map.put("drivingEndDate", drivingEndDate == null ? null :
                        DateUtil.getStringToLong(drivingEndDate.toString(), DateUtil.DATE_Y_M_D_FORMAT));
                    map.put("groupName", o.getOrgName());
                    re.add(map);
                });
                return new PageGridBean(newProfessionalsQuery, re, true);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView initNewUser(@RequestParam("uuid") String uuid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            OrganizationLdap organization = organizationService.getOrganizationByUuid(uuid);
            mav.addObject("orgId", uuid);
            mav.addObject("groupName", organization.getName());
            List<BasicInfo> allEducation = peopleBasicInfoService.getAllEducation();
            mav.addObject("allEducation", allEducation);
            List<BasicInfo> allNation = peopleBasicInfoService.getAllNation();
            mav.addObject("allNation", allNation);
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
         * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @Title: ??????????????????
     * @author , @RequestParam(value = "image_file", required = false) MultipartFile file, wangying
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addProfessionals(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final ProfessionalsForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            if (form == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            // // ????????????
            if (form.getPhotograph() != null && !("").equals(form.getPhotograph()) && !form.getPhotograph()
                .equals("0")) {
                if (!professionalService.editImg(form.getPhotograph())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????!,?????????");
                }
                form.setPhotograph(form.getPhotograph()
                    .split(configHelper.getMediaServer() + configHelper.getProfessionalFtpPath())[1]);
            }

            ProfessionalDTO professionalDTO = new ProfessionalDTO();
            BeanUtils.copyProperties(form, professionalDTO);
            professionalDTO.setOrgName(form.getGroupName());
            professionalDTO.setOrgId(form.getGroupId());
            if (professionalService.add(professionalDTO)) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????id?????? ????????????
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                // ??????????????????????????????
                if (professionalService.isBind(id)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????????????????????????????????????????IC?????????????????????????????????????????????");
                }
                boolean success = professionalService.deleteProfessionalsById(id);
                return new JsonResultBean(success);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????id?????? ????????????
     */
    @RequestMapping(value = "/confirmDelete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean confirmDelete(String id) {
        try {
            if (id == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            boolean success = professionalService.deleteBindProfessional(id);
            return new JsonResultBean(success);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            if (items == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return professionalService.deleteProfessionalsByBatch(items);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????id?????? ?????????????????????
     */
    @RequestMapping(value = "/confirmDeleteMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean confirmDeleteMore(String bandPids) {
        try {
            if (bandPids == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return professionalService.deleteMoreBindProfessional(bandPids);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            ProfessionalsForm professionalsForm = new ProfessionalsForm();
            ProfessionalDTO professionalDTO = professionalService.editPageData(id);
            BeanUtils.copyProperties(professionalDTO, professionalsForm);
            professionalsForm.setGroupId(professionalDTO.getOrgId());
            professionalsForm.setGroupName(professionalDTO.getOrgName());
            List<BasicInfo> allEducation = peopleBasicInfoService.getAllEducation();
            List<BasicInfo> allNation = peopleBasicInfoService.getAllNation();
            professionalsForm.setNation(Optional.ofNullable(professionalsForm.getNationId())
                .map(o -> cacheManger.getDictionaryValue(professionalsForm.getNationId())).orElse(null));
            professionalsForm.setEducation(Optional.ofNullable(professionalsForm.getEducationId())
                .map(o -> cacheManger.getDictionaryValue(professionalsForm.getEducationId())).orElse(null));
            mav.addObject("result", professionalsForm);
            mav.addObject("allEducation", allEducation);
            mav.addObject("allNation", allNation);
            return mav;
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????
     * @param form
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final ProfessionalsForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            if (form == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            // ????????????
            ProfessionalDTO newProfessionalDTO = new ProfessionalDTO();
            BeanUtils.copyProperties(form, newProfessionalDTO);
            newProfessionalDTO.setOrgId(form.getGroupId());
            newProfessionalDTO.setOrgName(form.getGroupName());

            if (!professionalService.checkEditProfessional(newProfessionalDTO)) {
                return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????!,?????????");
            }
            ProfessionalDTO oldProfessionalDTO = professionalService.editPageData(newProfessionalDTO.getId());
            return new JsonResultBean(
                professionalService.updateProGroupByProId(newProfessionalDTO, oldProfessionalDTO));
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ??????
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export() {
        try {
            professionalService.exportProfessionals("??????????????????");
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            professionalService.generateTemplate(response);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
         * @return String
     * @Title: ??????
     * @author wangying
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public ModelAndView importPage() {
        return new ModelAndView(IMPORT_PAGE);
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            return professionalService.importProfessionals(file);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportDeviceError(HttpServletResponse response) {
        try {
            ImportErrorUtil.generateErrorExcel(ImportModule.PROFESSIONAL, "??????????????????????????????", null, response);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
        }
    }

    /**
     * ????????????????????? @Title: list @return List<Group>
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    public String getTree(String isOrg) {
        try {
            return professionalService.getTree(isOrg);
        } catch (Exception e) {
            log.error("?????????????????????", e);
            return null;
        }
    }

    /**
     * ??????????????????????????????????????????
     * @param name
     * @param identity
     * @return
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("name") String name, @RequestParam("identity") String identity) {
        try {
            return professionalService.repetition(name, identity);
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitions", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitions(String id, String identity) {
        try {
            return professionalService.repetitions(id, identity);
        } catch (Exception e) {
            log.error("??????????????????id??????????????????????????????", e);
            return false;
        }
    }

    @RequestMapping(value = { "/upload_img" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadImg(MultipartFile file) {
        String newName = "";
        JSONObject resultMap = new JSONObject();
        try {
            // ??????
            if (!file.isEmpty()) {
                // ??????????????????
                String fileUrl = fastDFSClient.uploadFile(file);
                // ?????????????????????
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                if (FileUtil.checkPicSuffix(suffix)) {
                    newName = fileUrl.split(fdfsWebServer.getWebServerUrl())[1];
                    resultMap.put("imgName", newName);
                } else {
                    // ????????????
                    fastDFSClient.deleteFile(newName);
                    // ??????0 ????????????????????????
                    resultMap.put("imgName", "0");
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return resultMap;
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = { "/uploadImg" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadProfessionalImg(MultipartFile file, String id, HttpServletRequest request) {
        String newName = "";
        JSONObject resultMap = new JSONObject();
        try {
            // ??????
            if (!file.isEmpty() && StrUtil.isNotBlank(id)) {
                // ??????????????????
                String filePath = request.getSession().getServletContext().getRealPath("/") + "upload/";
                File saveFile = new File(filePath);
                if (!saveFile.exists() && !saveFile.mkdirs()) {
                    throw new RuntimeException("?????????????????????????????????????????????" + filePath);
                }

                //???????????????????????????
                InputStream inputStream = file.getInputStream();
                String type = FileUtil.getFileType(inputStream);
                // ?????????????????????
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                if (FileUtil.checkPicSuffix(suffix) && FileUtil.checkPicSuffix(type)) {
                    String time = DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT);
                    newName = id + "_" + time + suffix;
                    // ????????????
                    file.transferTo(new File(filePath + newName));
                    String mediaServer = configHelper.getMediaServer();
                    if (configHelper.isSslEnabled()) {
                        mediaServer = "/mediaserver";
                    }
                    resultMap.put("imgName", mediaServer + configHelper.getProfessionalFtpPath() + newName);
                } else {
                    // ????????????
                    File deleteFile = new File(filePath + newName);
                    if (!deleteFile.delete()) {
                        throw new RuntimeException("??????????????????!");
                    }
                    // ??????0 ????????????????????????
                    resultMap.put("imgName", "0");
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("??????????????????", e);
            return resultMap;
        }
    }

    /**
     * ???????????????ICard???????????????
     */
    @RequestMapping(value = "/getIcCardTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getIcCardTree(String type, String name) {
        try {
            String result = professionalService.getIcCardTree(type, name).toJSONString();
            // ????????????
            return new JsonResultBean(ZipUtil.compress(result));
        } catch (Exception e) {
            log.error("???ic????????????????????????", e);
            return null;
        }
    }

    /**
     * ???????????????(????????????????????????????????????????????????)
     */
    @RequestMapping(value = "/getProTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProTree(String type, String name) {
        try {
            String result = professionalService.getProTree(type, name).toJSONString();
            // ????????????
            return new JsonResultBean(ZipUtil.compress(result));
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return null;
        }
    }

    /**
     * IC??????????????????????????????????????????
     */
    @RequestMapping(value = "/getProfessionalCountByPid", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getProfessionalCountByPid(String parentId) {
        return AdasControllerTemplate
            .getResultBean(() -> professionalService.getProfessionalCountByPid(parentId), "??????IC????????????????????????");
    }

}
