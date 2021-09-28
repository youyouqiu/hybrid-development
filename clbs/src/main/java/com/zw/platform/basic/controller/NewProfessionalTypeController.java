package com.zw.platform.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.domain.ProfessionalsTypeDO;
import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsTypeQuery;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.talkback.service.baseinfo.PeopleBasicInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月26日下午4:44:08
 */
@Controller
@RequestMapping("/m/basicinfo/enterprise/professionals")
public class NewProfessionalTypeController {
    private static Logger log = LogManager.getLogger(NewProfessionalTypeController.class);


    @Autowired
    ProfessionalsService professionalsService;

    @Autowired
    PeopleBasicInfoService peopleBasicInfoService;

    @Autowired
    ConfigHelper configHelper;

    @Resource
    private HttpServletRequest request;

    private static final String IMPORTWO_PAGE = "modules/basicinfo/enterprise/professionals/importTwo";


    /**
     * 新增 岗位类型
     */
    @RequestMapping(value = "/addType", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true)
    @ResponseBody
    public JsonResultBean add(String professionalstype, String addDescription) {
        try {
            if (professionalstype != null && !"".equals(professionalstype)) { // 如果岗位类型不为空
                ProfessionalsTypeDO professionalsTypeDO = new ProfessionalsTypeDO();
                professionalsTypeDO.setProfessionalstype(professionalstype);
                professionalsTypeDO.setDescription(addDescription);
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                professionalsService.add(professionalsTypeDO, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "该岗位类型已存在，请重新输入");
        } catch (Exception e) {
            log.error("新增岗位类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 分页查询用户
     */
    @ResponseBody
    @RequestMapping(value = "/listType", method = RequestMethod.POST)
    public PageGridBean list(final ProfessionalsTypeQuery query) {
        try {
            Page<ProfessionalsTypeDO> result = professionalsService.findByPage(query);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询岗位类型异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 根据id删除 岗位类型
     */
    @RequestMapping(value = "/deleteType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteType(final String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                boolean flag = professionalsService.deletePostType(id, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除岗位类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id批量删除 岗位类型
     */
    @RequestMapping(value = "/deleteTypeMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteTypeMore(final String ids) {
        try {
            if (ids != null) {
                String[] item = ids.split(",");
                List<String> list = Arrays.asList(item);
                // 获得访问ip
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean flag = professionalsService.deleteMore(list, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除岗位类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 通过ID得到岗位类型
     */
    @RequestMapping(value = "/findTypeById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findTypeById(String id) {
        try {
            JSONObject msg = new JSONObject();
            ProfessionalsTypeDO professionalsType = null;
            if (id != null && !id.isEmpty()) {
                professionalsType = professionalsService.get(id);
            }
            msg.put("operation", professionalsType);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询岗位类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改岗位类型
     */
    @RequestMapping(value = "/editJobType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(String id, String jobType, String jobDescription) {
        try {
            if (jobType != null && !"".equals(jobType) && id != null && !"".equals(id)) {
                // 获取操作用户的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                ProfessionalsTypeForm professionalsTypeForm = new ProfessionalsTypeForm();
                professionalsTypeForm.setId(id);
                professionalsTypeForm.setProfessionalstype(jobType);
                professionalsTypeForm.setDescription(jobDescription);
                return professionalsService.update(professionalsTypeForm, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改岗位类型类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/exportType", method = RequestMethod.GET)
    @ResponseBody
    public void exportType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "岗位类型列表");
            professionalsService.exportType(null, 1, response);
        } catch (Exception e) {
            log.error("导出岗位类型列表异常", e);
        }
    }

    /**
         * @return String
     * @Title: 导入
     * @author yangyi
     */
    @RequestMapping(value = { "/importTwo" }, method = RequestMethod.GET)
    public String importTwoPage() {
        return IMPORTWO_PAGE;
    }

    @RequestMapping(value = "/importType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importType(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // 获取操作用户的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = professionalsService.importType(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入岗位类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/downloadType", method = RequestMethod.GET)
    public void downloadType(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "岗位类型模板");
            professionalsService.generateTemplateType(response);
        } catch (Exception e) {
            log.error("下载岗位类型模板异常", e);
        }
    }

    /**
     * 根据岗位类型查询数据库中是否有相同岗位类型
     */
    @RequestMapping(value = "/comparisonType", method = RequestMethod.POST)
    @ResponseBody
    public boolean findTypeMessage(String type) {
        try {
            ProfessionalsTypeDO professionalsType = professionalsService.findTypeByType(type);
            if (professionalsType == null) { // 如果对象为空,则数据库中没有这条数据记录,可以添加
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("根据岗位类型查询数据库中是否有相同岗位类型异常", e);
        }

        return true;
    }

    /**
     * 根据岗位类型查询岗位类型（用于修改时的比较）
     */
    @RequestMapping(value = "/findPostTypeCompare", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOperationCompare(String type, String startpostType) {
        try {
            // 先检查type是否存在
            ProfessionalsTypeDO professionalsType = professionalsService.findTypeByType(type);
            if (professionalsType == null) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                if (type.equals(startpostType)) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
        } catch (Exception e) {
            log.error("从业人员管理页面修改岗位类型数据验证时出错", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }



}
