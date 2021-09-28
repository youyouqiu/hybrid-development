package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.MessageConfig;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.query.PeopleQuery;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.form.PersonnelForm;
import com.zw.platform.domain.basicinfo.query.PersonnelQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.ZwImportException;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Map;
import java.util.Objects;

/**
 * 人员监控Controller Created by Tdz on 2016/7/20.
 */
@Controller
@RequestMapping("/m/basicinfo/monitoring/personnel")
public class PeopleController {
    private static Logger log = LogManager.getLogger(PeopleController.class);

    private static final String LIST_PAGE = "modules/basicinfo/monitoring/personnel/list";

    private static final String ADD_PAGE = "modules/basicinfo/monitoring/personnel/add";

    private static final String EDIT_PAGE = "modules/basicinfo/monitoring/personnel/edit";

    private static final String IMPORT_PAGE = "modules/basicinfo/monitoring/personnel/import";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    MessageConfig messageConfig;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    PeopleService peopleService;

    @Autowired
    @Qualifier("peopleService")
    CacheService cacheService;

    @RequestMapping(value = { "/test" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean test() {
        cacheService.initCache();
        return new JsonResultBean();
    }

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

            Page<Map<String, Object>> re = new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
            re.setTotal(result.getTotal());
            List<Map<String, Object>> mapList = new ArrayList<>();
            result.stream().forEach(o -> {
                Map map = JSON.parseObject(JSON.toJSONString(o), Map.class);
                map.put("groupName", o.getOrgName());
                map.put("peopleNumber", o.getName());
                map.put("name", o.getAlias());
                map.put("assign", o.getGroupName());
                map.put("simcardNumber", o.getSimCardNumber());
                mapList.add(map);
            });
            re.addAll(mapList);

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
    public JsonResultBean add(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("form") final PersonnelForm form,
        final BindingResult bindingResult) {
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        PeopleDTO peopleDTO = form.getPeopleDTO();
        return ControllerTemplate.getBooleanResult(() -> peopleService.add(peopleDTO));
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

                boolean flag = peopleService.delete(id);
                if (flag) {
                    return new JsonResultBean(msg);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除人员信息异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
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
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * 根据id获取人员详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/getPeopleById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getPeopleById(String id) {
        try {
            PeopleDTO peopleDTO = peopleService.getById(id);
            Personnel personnel = new Personnel();
            BeanUtils.copyProperties(peopleDTO, personnel);
            personnel.setGroupName(peopleDTO.getOrgName());
            personnel.setGroupId(peopleDTO.getOrgId());
            personnel.setPeopleNumber(peopleDTO.getName());
            personnel.setName(peopleDTO.getAlias());
            return new JsonResultBean(personnel);
        } catch (Exception e) {
            log.error("获取人员详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * 修改Personnl
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            PeopleDTO peopleDTO = peopleService.getById(id);
            Personnel personnel = new Personnel();
            BeanUtils.copyProperties(peopleDTO, personnel);
            personnel.setGroupName(peopleDTO.getOrgName());
            personnel.setGroupId(peopleDTO.getOrgId());
            personnel.setPeopleNumber(peopleDTO.getName());
            personnel.setName(peopleDTO.getAlias());
            personnel.setBindId(peopleDTO.getConfigId());
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
    @AvoidRepeatSubmitToken(removeToken = true)
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
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/export.gsp", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response) {
        try {
            peopleService.export(response);
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
            JsonResultBean jsonResultBean = peopleService.importExcel(file);
            return jsonResultBean;
        } catch (Exception e) {
            log.error("导入人员信息异常", e);
            if (e instanceof ZwImportException) {
                return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, messageConfig.getSysErrorMsg());
        }
    }

    @RequestMapping(value = "/exportError", method = RequestMethod.GET)
    public void exportPeopleError(HttpServletResponse response) {
        try {
            ImportErrorUtil.generateErrorExcel(ImportModule.PEOPLE, "人员导入错误信息", null, response);
        } catch (Exception e) {
            log.error("导出人员错误信息异常", e);
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "人员列表模板");
            peopleService.generateTemplate(response);
        } catch (Exception e) {
            log.error("下载人员列表模板异常", e);
        }
    }

    @RequestMapping(value = "/repetitionAdd", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("peopleNumber") String peopleNumber) {
        try {
            PeopleDTO peopleDTO = peopleService.getByName(peopleNumber);
            if (peopleDTO == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员编号查询人员异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitionEdit", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(@RequestParam("peopleNumber") String peopleNumber, @RequestParam("id") String id) {
        try {
            boolean existNumber = peopleService.isExistNumber(id, peopleNumber);
            if (!existNumber) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员编号id查询人员异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitionIdentity", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionIdentity(@RequestParam("identity") String identity) {
        try {
            PeopleDTO peopleDTO = peopleService.getPeopleByIdentity(identity);
            if (peopleDTO == null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员身份证号查询人员异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/repetitionIdentityEdit", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetitionIdentityEdit(@RequestParam("identity") String identity, @RequestParam("id") String id) {
        try {
            boolean existIdentity = peopleService.isExistIdentity(id, identity);
            if (!existIdentity) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据人员身份证号和id查询人员异常", e);
            return false;
        }
    }

}
