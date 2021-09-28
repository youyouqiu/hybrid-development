package com.zw.platform.controller.accessPlatform;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.accessPlatform.AccessPlatform;
import com.zw.platform.domain.accessPlatform.AccessPlatformQuery;
import com.zw.platform.service.accessPlatform.AccessPlatformService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author LiaoYuecai
 * @create 2018-01-05 13:42
 * @desc
 */

@Controller
@RequestMapping("/access/platform")
public class AccessPlatformServiceController {

    public AccessPlatformServiceController() {
        super();
    }

    private static Logger log = LogManager.getLogger(AccessPlatformServiceController.class);

    @Autowired
    private AccessPlatformService accessPlatformService;

    private static final String LIST_PAGE = "modules/forwardplatform/inmgt/list";

    private static final String ADD_PAGE = "modules/forwardplatform/inmgt/add";

    private static final String EDIT_PAGE = "modules/forwardplatform/inmgt/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String IMPORT_PAGE = "modules/forwardplatform/inmgt/import";

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(AccessPlatformQuery query) {
        try {
            Page<AccessPlatform> accessPlatforms = accessPlatformService.find(query);
            return new PageGridBean(query, accessPlatforms, true);
        } catch (Exception e) {
            log.error("分页查询接入平台IP异常(findList)", e);
            return new PageGridBean(false);
        }

    }

    @AvoidRepeatSubmitToken(setToken = true)
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        try {
            return ADD_PAGE;
        } catch (Exception e) {
            log.error("新增接入平台IP界面弹出异常", e);
            return ERROR_PAGE;
        }
    }

    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            mav.addObject("result", accessPlatformService.getByID(id));
            return mav;
        } catch (Exception e) {
            log.error("修改接入平台IP界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(AccessPlatform accessPlatform) {
        try {
            if (accessPlatform != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 获取访问服务器的客户端的IP地址
                accessPlatformService.update(accessPlatform, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改接入平台IP异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addAccessPlatform(AccessPlatform accessPlatform) {
        try {
            if (accessPlatform != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                Integer result = accessPlatformService.add(accessPlatform, ipAddress);
                if (result == 1) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增接入平台IP异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/delete_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable final String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean result = accessPlatformService.deleteById(id, ipAddress);
                if (result) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除接入平台IP异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/deleteMore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(String id) {
        try {
            if (id != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                boolean result = accessPlatformService.deleteById(id, ipAddress);
                if (result) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除接入平台IP异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 校验808接入平台名称唯一性
     * @param platFormName
     * @param pid
     * @return
     * @author hujun
     * @Date 创建时间：2018年4月9日 上午11:24:12
     */
    @RequestMapping(value = { "/check808InputPlatFormSole" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check808InputPlatFormSole(String platFormName, String pid) {
        try {
            return accessPlatformService.check808InputPlatFormSole(platFormName, pid);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 请求导入文件页面
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public String importHtml() {
        return IMPORT_PAGE;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importJoinUpIp(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
            Map resultMap = accessPlatformService.importJoinUpPlateformIp(file, ipAddress);
            String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入接入平台信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "接入平台IP列表模板");
            accessPlatformService.downLoadFileTemplate(response);
        } catch (Exception e) {
            log.error("下载接入平台IP列表模板异常", e);
        }
    }

    /**
     * 导出接入平台IP
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void fileExport(HttpServletResponse response) {
        try {
            accessPlatformService.exportFile(null, 1, response);
        } catch (Exception e) {
            log.error("导出接入平台IP异常", e);
        }

    }

}
