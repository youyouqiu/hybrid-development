package com.zw.talkback.controller.basicinfo;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.basicinfo.form.JobManagementFromData;
import com.zw.talkback.domain.basicinfo.query.JobManagementQuery;
import com.zw.talkback.service.baseinfo.JobManagementService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 职位管理
 */
@Controller
@RequestMapping("/talkback/basicinfo/monitoring/job")
public class JobManagementController {
    private static final Logger log = LogManager.getLogger(JobManagementController.class);

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    private static final String DEFAULT_ID = "default";

    private static final String LIST_PAGE = "talkback/basicinfo/monitoring/job/list";

    private static final String ADD_PAGE = "talkback/basicinfo/monitoring/job/add";

    private static final String EDIT_PAGE = "talkback/basicinfo/monitoring/job/edit";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private JobManagementService jobManagementService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public String getAddPage() {
        return ADD_PAGE;
    }

    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    public ModelAndView getEditPage(@PathVariable String id) {
        try {
            ModelAndView modelAndView = new ModelAndView(EDIT_PAGE);
            JobInfoData jobInfoData = jobManagementService.findJobById(id);
            if (sslEnabled) {
                mediaServer = "/mediaserver";
            }
            jobInfoData.setJobIconName(mediaServer + professionalFtpPath + jobInfoData.getJobIconName());
            modelAndView.addObject("result", jobInfoData);
            return modelAndView;
        } catch (Exception e) {
            log.error("弹出修改页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }

    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final JobManagementQuery query) {
        try {
            Page<JobInfoData> result = jobManagementService.findByPage(query);
            if (CollectionUtils.isEmpty(result)) {
                return new PageGridBean(new Page<JobInfoData>(), true);
            }

            if (sslEnabled) {
                mediaServer = "/mediaserver/";
            }
            String newIconName;
            for (JobInfoData jobInfoData : result) {
                newIconName = mediaServer + professionalFtpPath + jobInfoData.getJobIconName();
                jobInfoData.setJobIconName(newIconName);
            }

            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询职位管理信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add(@Validated(ValidGroupAdd.class) final JobManagementFromData form,
        final BindingResult bindingResult, HttpServletRequest request) {
        try {
            if (form != null) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                }
                if (!jobManagementService.checkJobName(form.getJobName(), null)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该职位已存在");
                }
                String fileName = form.getJobIconName();
                if (!saveJobIconName(fileName, request)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "职位图片存储失败");
                }
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                jobManagementService.addJobInfo(form, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("新增职位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = { "/upload_img" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadImg(MultipartFile file, HttpServletRequest request) {
        try {
            return jobManagementService.uploadImg(file, request);
        } catch (Exception e) {
            log.error("加载图片异常", e);
            return new JSONObject();
        }
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(@Validated(ValidGroupAdd.class) final JobManagementFromData form,
        final BindingResult bindingResult, HttpServletRequest request) {
        try {
            if (form != null) {
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                }
                if (!jobManagementService.checkJobName(form.getJobName(), form.getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该职位已存在");
                }
                FTPClient ftpClient =
                    FtpClientUtil.getFTPClient(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, professionalFtpPath);
                if (jobManagementService.isChangeJobIcon(form, ftpClient)) {
                    if (!saveJobIconName(form.getJobIconName(), request)) {
                        return new JsonResultBean(JsonResultBean.FAULT, "职位图片存储失败");
                    }
                }
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                jobManagementService.updateJobInfo(form, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改职位信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = { "/checkJobName" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean checkJobName(String name, String id) {
        try {
            if (name != null) {
                return jobManagementService.checkJobName(name, id);
            }
        } catch (Exception e) {
            log.error("检查职位名字重复异常", e);
        }
        return false;
    }

    @RequestMapping(value = { "/delete" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(String id, HttpServletRequest request) {
        try {
            if (DEFAULT_ID.equals(id)) {
                return new JsonResultBean(JsonResultBean.FAULT, "默认职位，不能删除！");
            }
            if (jobManagementService.checkBinding(id)) {
                return new JsonResultBean(JsonResultBean.FAULT, "已有人员绑定了该职位，不能删除！");
            }
            FTPClient ftpClient =
                FtpClientUtil.getFTPClient(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, professionalFtpPath);
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            jobManagementService.deleteJob(id, ftpClient, ip);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除职位异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    private boolean saveJobIconName(String fileName, HttpServletRequest request) throws Exception {
        if (fileName == null || "".equals(fileName)) {
            return false;
        }
        String filePath = request.getSession().getServletContext().getRealPath("/") + "upload/";
        FileInputStream fis = new FileInputStream(filePath + fileName);
        boolean success = FtpClientUtil
            .uploadFile(ftpHostClbs, ftpPortClbs, ftpUserName, ftpPassword, professionalFtpPath, fileName, fis);
        Files.deleteIfExists(Paths.get(filePath + fileName));
        return success;
    }
}
