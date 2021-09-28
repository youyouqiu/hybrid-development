package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.query.PeopleQuery;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.domain.basicinfo.BasicInfo;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.basicinfo.form.Personnel;
import com.zw.talkback.domain.basicinfo.form.PersonnelForm;
import com.zw.talkback.domain.basicinfo.query.PersonnelQuery;
import com.zw.talkback.service.baseinfo.JobManagementService;
import com.zw.talkback.service.baseinfo.PeopleBasicInfoService;
import com.zw.talkback.service.baseinfo.SkillService;
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
import java.util.List;
import java.util.Objects;

/**
 * 人员监控Controller Created by Tdz on 2016/7/20.
 */
@Controller
@RequestMapping("/talkback/basicinfo/monitoring/personnel")
public class TalkbackPeopleController {
    private static Logger log = LogManager.getLogger(TalkbackPeopleController.class);

    private static final String LIST_PAGE = "talkback/basicinfo/monitoring/personnel/list";

    private static final String ADD_PAGE = "talkback/basicinfo/monitoring/personnel/add";

    private static final String EDIT_PAGE = "talkback/basicinfo/monitoring/personnel/edit";

    private static final String IMPORT_PAGE = "talkback/basicinfo/monitoring/personnel/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private JobManagementService jobManagementService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private PeopleBasicInfoService peopleBasicInfoService;

    @Autowired
    PeopleService peopleService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    MessageConfig messageConfig;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final PersonnelQuery query) {
        try {
            PeopleQuery peopleQuery = new PeopleQuery();
            BeanUtils.copyProperties(query, peopleQuery);
            peopleQuery.setOrgId(query.getGroupId());
            peopleQuery.setGroupId(query.getAssignId());
            Page<PeopleDTO> result = peopleService.getPeopleList(peopleQuery);

            Page<Personnel> re = new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
            re.setTotal(result.getTotal());
            List<Personnel> personnels = new ArrayList<>();
            result.stream().forEach(o -> {
                Personnel personnel = new Personnel();
                BeanUtils.copyProperties(o, personnel);
                personnel.setGroupName(o.getOrgName());
                personnel.setPeopleNumber(o.getName());
                personnel.setName(o.getAlias());
                personnels.add(personnel);
            });
            re.addAll(personnels);
            return new PageGridBean(query, re, true);
        } catch (Exception e) {
            log.error("分页查询失败人员信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增
     */

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView initNewUser(@RequestParam("uuid") final String uuid) {
        try {
            ModelAndView mav = new ModelAndView(ADD_PAGE);
            List<JobInfoData> all = jobManagementService.findAll();
            mav.addObject("allJob", JSON.toJSONString(all));
            String skillTree = skillService.getSkillTree();
            mav.addObject("skillTree", skillTree);
            List<BasicInfo> allDriverType = peopleBasicInfoService.getAllDriverType();
            mav.addObject("allDriverType", JSON.toJSONString(allDriverType));
            List<BasicInfo> allBloodType = peopleBasicInfoService.getAllBloodType();
            mav.addObject("allBloodType", allBloodType);
            List<BasicInfo> allNation = peopleBasicInfoService.getAllNation();
            mav.addObject("allNation", allNation);
            List<BasicInfo> allQualification = peopleBasicInfoService.getAllQualification();
            mav.addObject("allQualification", allQualification);
            if (!uuid.equals("") && !uuid.equals("ou=organization")) {
                OrganizationLdap org = organizationService.getOrgByEntryDn(uuid);
                mav.addObject("orgId", org.getUuid());
                mav.addObject("groupName", org.getName());
            }
            return mav;
        } catch (Exception e) {
            log.error("新增人员界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 新增 人员
     */
    @AvoidRepeatSubmitToken(removeToken = true)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(@Validated({
        ValidGroupAdd.class }) @ModelAttribute("form") final com.zw.talkback.domain.basicinfo.form.PersonnelForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    PeopleDTO peopleDTO = form.getPeopleDTO();
                    if (peopleService.add(peopleDTO)) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除 Personnl
     */
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null) {
                JSONObject msg = new JSONObject();
                msg.put("peopleId", id);
                PeopleDTO peopleDTO = peopleService.getById(id);
                if (peopleDTO != null && Objects.equals(peopleDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                    msg.put("infoMsg", messageConfig.getVehicleBrandBound());
                    return new JsonResultBean(msg);
                }

                // 根据id删除人员信息
                boolean flag = peopleService.delete(id);
                if (flag) {
                    return new JsonResultBean(msg);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String checkedList) {
        try {
            if (StringUtils.isNotEmpty(checkedList)) {
                List<String> deleteIds = Arrays.asList(checkedList.split(","));
                return new JsonResultBean(peopleService.batchDel(deleteIds));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "id不能为空！");
        } catch (Exception e) {
            log.error("批量删除人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    // /**
    //  * 根据id获取人员详情
    //  * @param id
    //  * @return
    //  */
    // @RequestMapping(value = "/getPeopleById", method = RequestMethod.POST)
    // @ResponseBody
    // public JsonResultBean getPeopleById(String id) {
    //     try {
    //         return new JsonResultBean(personnelService.get(id));
    //     } catch (Exception e) {
    //         log.error("获取人员详情异常", e);
    //         return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
    //     }
    // }

    /**
     * 修改Personnl
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            List<JobInfoData> all = jobManagementService.findAll();
            mav.addObject("allJob", JSON.toJSONString(all));
            String skillTree = skillService.getSkillTree();
            mav.addObject("skillTree", skillTree);
            List<BasicInfo> allDriverType = peopleBasicInfoService.getAllDriverType();
            mav.addObject("allDriverType", JSON.toJSONString(allDriverType));
            List<BasicInfo> allBloodType = peopleBasicInfoService.getAllBloodType();
            mav.addObject("allBloodType", allBloodType);
            List<BasicInfo> allNation = peopleBasicInfoService.getAllNation();
            mav.addObject("allNation", allNation);
            List<BasicInfo> allQualification = peopleBasicInfoService.getAllQualification();
            mav.addObject("allQualification", allQualification);
            PeopleDTO peopleDTO = peopleService.getById(id);
            Personnel personnel = new Personnel();
            BeanUtils.copyProperties(peopleDTO, personnel);
            personnel.setGroupName(peopleDTO.getOrgName());
            personnel.setGroupId(peopleDTO.getOrgId());
            personnel.setPeopleNumber(peopleDTO.getName());
            personnel.setName(peopleDTO.getAlias());
            mav.addObject("result", personnel);
            return mav;
        } catch (Exception e) {
            log.error("修改人员信息弹出界面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改Personnl
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final PersonnelForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    PeopleDTO peopleDTO = form.getPeopleDTO();
                    boolean flag = peopleService.update(peopleDTO);
                    if (flag) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            peopleService.exportIntercomPeople(response);
        } catch (Exception e) {
            log.error("导出人员信息异常", e);
        }
    }

    /**
     * @return String
     * @Title: 导入
     * @author
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importPage() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importPersonnel(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            JsonResultBean jsonResultBean = peopleService.importIntercomExcel(file);
            return jsonResultBean;
        } catch (Exception e) {
            log.error("导入人员信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "人员列表模板");
            peopleService.generateIntercomTemplate(response);
        } catch (Exception e) {
            log.error("下载人员列表模板异常", e);
        }
    }

    // @RequestMapping(value = "/repetitionAdd", method = RequestMethod.POST)
    // @ResponseBody
    // public boolean repetition(@RequestParam("peopleNumber") String peopleNumber) {
    //     try {
    //         com.zw.talkback.domain.basicinfo.form.Personnel vt = personnelService.findByNumber(peopleNumber);
    //         if (vt == null) {
    //             return true;
    //         } else {
    //             return false;
    //         }
    //     } catch (Exception e) {
    //         log.error("根据人员编号查询人员异常", e);
    //         return false;
    //     }
    // }
    //
    // @RequestMapping(value = "/repetitionEdit", method = RequestMethod.POST)
    // @ResponseBody
    // public boolean repetition(@RequestParam("peopleNumber") String peopleNumber, @RequestParam("id") String id) {
    //     try {
    //         com.zw.talkback.domain.basicinfo.form.Personnel vt = personnelService.findByNumberId(id, peopleNumber);
    //         if (vt == null) {
    //             return true;
    //         } else {
    //             return false;
    //         }
    //     } catch (Exception e) {
    //         log.error("根据人员编号id查询人员异常", e);
    //         return false;
    //     }
    // }
    //
    // @RequestMapping(value = "/repetitionIdentity", method = RequestMethod.POST)
    // @ResponseBody
    // public boolean repetitionIdentity(@RequestParam("identity") String identity) {
    //     try {
    //         com.zw.talkback.domain.basicinfo.form.Personnel vt = personnelService.findByPersonnel(identity);
    //         if (vt == null) {
    //             return true;
    //         } else {
    //             return false;
    //         }
    //     } catch (Exception e) {
    //         log.error("根据人员身份证号查询人员异常", e);
    //         return false;
    //     }
    // }
    //
    // @RequestMapping(value = "/repetitionIdentityEdit", method = RequestMethod.POST)
    // @ResponseBody
    // public boolean repetitionIdentityEdit(@RequestParam("identity") String identyty, @RequestParam("id") String id) {
    //     try {
    //         Personnel vt = personnelService.findByPersonnel(id, identyty);
    //         if (vt == null) {
    //             return true;
    //         } else {
    //             return false;
    //         }
    //     } catch (Exception e) {
    //         log.error("根据人员身份证号和id查询人员异常", e);
    //         return false;
    //     }
    // }

    /**
     * 改变工作状态 (离职)
     * @param id
     * @return
     */
    @RequestMapping(value = "/upDateWorkStae", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean upDateWorkStae(String id) {
        try {
            PeopleDTO peopleDTO = peopleService.getById(id);
            if (peopleDTO != null && Objects.equals(peopleDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                return new JsonResultBean(peopleService.updateIncumbency(id, PeopleDTO.IS_INCUMBENCY.b2p("离职")));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("人员列表离职异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

}
