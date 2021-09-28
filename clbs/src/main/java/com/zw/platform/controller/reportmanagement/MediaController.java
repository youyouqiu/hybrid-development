package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.multimedia.Media;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.multimedia.query.MediaQuery;
import com.zw.platform.service.reportManagement.MediaService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * 多媒体Controller
 * @author wangying
 */
@Controller
@RequestMapping("/m/reportManagement/media")
public class MediaController {
    @Autowired
    private MediaService mediaService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    private static final String mediaUrl = "/clbs/resources/img/media";

    @Resource
    private HttpServletRequest request;

    private static final Logger log = LogManager.getLogger(MediaController.class);

    private static final String LIST_PAGE = "modules/reportManagement/media";
    private static final String UPDATE_PAGE = "modules/reportManagement/updateMedia";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final MediaQuery query) {
        try {
            if (StringUtils.isBlank(query.getVehicleId())) {
                return new PageGridBean(new Page<Media>(), true);
            }
            Page<Media> result = mediaService.findMedia(query);
            String path;
            for (Media media: result) {
                path = media.getMediaUrlNew();
                if (path != null && !path.equals("")) {
                    if (sslEnabled) {
                        webServerUrl = "/";
                    }
                    media.setMediaUrlNew(path.startsWith("/") ? mediaUrl + path : webServerUrl + path);
                }
            }
            return new PageGridBean(result, true);

        } catch (Exception e) {
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 根据id删除多媒体
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(final String id) {
        try {
            boolean flag = StringUtils.isNotBlank(id) ? mediaService.deleteById(id) : false;
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("多媒体管理页面删除多媒体信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 根据id删除多媒体
     */
    @RequestMapping(value = "/downMedia", method = RequestMethod.POST)
    @ResponseBody
    public void downMedia(String mediaUrl, String fileName, HttpServletResponse response) {
        mediaService.downMedia(mediaUrl, fileName, response);
    }




    @RequestMapping(value = { "/save_img" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject saveImg(MultipartFile file, String vehicleId) {
        String newName = "";
        JSONObject resultMap = new JSONObject();
        try {
            // 图片
            String filePath = "";
            if (!file.isEmpty()) {
                // 文件保存路径
                filePath =
                    request.getSession().getServletContext().getRealPath("/") + "resources/img/media/" + vehicleId
                        + "/";
                File saveFile = new File(filePath);
                if (!saveFile.exists()) {
                    saveFile.mkdirs();
                }
                // 获取文件后缀名
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                newName = vehicleId + "_0" + suffix;
                // 转存文件
                file.transferTo(new File(filePath + newName));
            }
            resultMap.put("media_name", newName);
            resultMap.put("media_url", filePath + newName);
            return resultMap;
        } catch (Exception e) {
            log.error("加载图片异常", e);
            return resultMap;
        }
    }

    @RequestMapping(value = { "/addMedia" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addMedia(final MediaForm form) {
        try {
            boolean flag = mediaService.addMedia(form);
            return new JsonResultBean(flag);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/getMediaDescriptionPage_{id}", method = RequestMethod.GET)
    public ModelAndView getMediaDescriptionPage(@PathVariable("id") String id) {
        try {
            ModelAndView modelAndView = new ModelAndView(UPDATE_PAGE);
            Media media = mediaService.getMedia(id);
            modelAndView.addObject("media", media);
            return modelAndView;
        } catch (Exception e) {
            log.error("修改多媒体管理页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/updateMediaDescription", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateMediaDescription(String id, String description) {
        try {
            if (StringUtils.isNotBlank(id)) {
                return mediaService.updateMediaDescription(id, description);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "查询条件不能为空");
        } catch (Exception e) {
            log.error("修改多媒体管理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
