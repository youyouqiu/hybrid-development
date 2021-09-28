package com.zw.platform.controller.intercomplatform;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.Personalized;
import com.zw.platform.service.intercomplatform.PersonalizedService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/m/intercomplatform/personalized")
public class PersonalizedController {
    private static Logger log = LogManager.getLogger(PersonalizedController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private PersonalizedService personalizedService;

    private static final String LIST_PAGE = "modules/intercomplatform/personalized/list";

    private static final List<String> validImageSuffix = Arrays.asList("png", "jpg", "jpeg", "gif", "svg");

    private static final List<String> validIcoSuffix = Collections.singletonList("ico");

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 个性化配置修改
     */
    @RequestMapping(value = { "/update" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateLogo(Personalized personalized, String groupId, String name,
        HttpServletRequest request) {
        try {
            String rootPath = request.getSession().getServletContext().getRealPath("/");
            boolean result = personalizedService.update(personalized, rootPath, groupId, name);

            if (result) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("平台信息设置页面个性化配置修改异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询个性化配置详情
     */
    @RequestMapping(value = { "/find" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean find(String uuid) {
        try {
            JSONObject msg = new JSONObject();
            Personalized personalize = personalizedService.findOrDefault(uuid);
            msg.put("list", personalize);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("平台信息设置页面查询个性化配置详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 图片上传 登录页logo修改
     * @author yangyi
     */
    @RequestMapping(value = { "/upload_img" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadImg(HttpServletRequest request, MultipartFile file) {
        try {
            JSONObject resultMap = new JSONObject();
            // 图片
            if (file.isEmpty()) {
                return resultMap;
            }
            // 文件保存路径
            String filePath = request.getSession().getServletContext().getRealPath("/") + "resources/img/logo/";
            String newFileName = personalizedService.uploadImage(filePath, file, validImageSuffix);
            resultMap.put("imgName", newFileName);
            return resultMap;
        } catch (Exception e) {
            log.error("平台信息设置页面上传图片异常", e);
            return null;
        }
    }

    /**
     * 图片上传 登录页背景图修改
     * @author tianzhangxu
     */
    @RequestMapping(value = { "/upload_bgimg" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadBGImg(HttpServletRequest request, MultipartFile file) {
        try {
            JSONObject resultMap = new JSONObject();
            // 图片
            if (file.isEmpty()) {
                return resultMap;
            }
            // 文件保存路径
            String filePath = request.getSession().getServletContext().getRealPath("/") + "resources/img/home/";
            String newFileName = personalizedService.uploadImage(filePath, file, validImageSuffix);
            resultMap.put("imgName", newFileName);
            return resultMap;
        } catch (Exception e) {
            log.error("平台信息设置登录页背景图上传图片异常", e);
            return null;
        }
    }

    /**
     * 图片上传 平台网页标题ICO
     * @author yangyi
     */
    @RequestMapping(value = { "/upload_ico" }, method = RequestMethod.POST)
    @ResponseBody
    public JSONObject uploadIco(HttpServletRequest request, MultipartFile file) {
        try {
            JSONObject resultMap = new JSONObject();
            // 图片
            if (file.isEmpty()) {
                return resultMap;
            }
            // 文件保存路径
            String filePath = request.getSession().getServletContext().getRealPath("/") + "resources/img/logo/";
            String newFileName = personalizedService.uploadImage(filePath, file, validIcoSuffix);
            resultMap.put("imgName", newFileName);
            return resultMap;
        } catch (Exception e) {
            log.error("平台信息设置页面平台网页标题ico异常", e);
            return null;
        }
    }

    /**
     * 默认个性化配置
     */
    @RequestMapping(value = { "/default" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean defaultPersonalized(Personalized personalized, String type) {
        try {
            boolean flag = false;
            Personalized personalize = personalizedService.find("defult");
            switch (type) {
                case "0":  // 默认登录页个性化设置
                    personalized.setLoginPersonalization(personalize.getLoginPersonalization());
                    personalized.setLoginBackground(personalize.getLoginBackground());
                    personalized.setLoginLogo(personalize.getLoginLogo());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "1":  // 默认首页logo
                    personalized.setHomeLogo(personalize.getHomeLogo());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "2":  // 默认顶部标题
                    personalized.setTopTitle(personalize.getTopTitle());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "3":  // 默认底部标题
                    personalized.setCopyright(personalize.getCopyright());
                    personalized.setWebsiteName(personalize.getWebsiteName());
                    personalized.setRecordNumber(personalize.getRecordNumber());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "4":  // 默认首页名称
                    personalized.setFrontPage(personalize.getFrontPage() == null ? "" : personalize.getFrontPage());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "5":  // 默认首页Ico
                    personalized.setWebIco(personalize.getWebIco());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "6":
                    Integer serviceExpireReminder = personalize.getServiceExpireReminder();
                    personalized.setServiceExpireReminder(serviceExpireReminder == null ? 30 : serviceExpireReminder);
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "7":  //默认平台网址
                    personalized
                        .setPlatformSite(personalize.getPlatformSite() == null ? "" : personalize.getPlatformSite());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                case "8":
                    personalized.setVideoBackground(personalize.getVideoBackground());
                    flag = personalizedService.updateLogo(personalized);
                    break;
                default:
                    break;
            }
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("平台信息设置页面默认个性化配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 将当前个性化配置设为默认
     * @param type 对应具体点的哪一个设为默认按钮
     */
    @RequestMapping(value = { "/makeDefault" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean makeDefault(Personalized personalized, String type, HttpServletRequest request) {
        try {
            boolean flag = false;
            Personalized personalize = personalizedService.find("defult");
            switch (type) {
                case "0":  // 设为默认登录页个性化设置
                    personalize.setLoginPersonalization(personalized.getLoginPersonalization());
                    String newNameOne = saveDefaultPic(personalized.getLoginBackground(), 1, request);
                    personalize.setLoginBackground(newNameOne);
                    String newNameTwo = saveDefaultPic(personalized.getLoginLogo(), 2, request);
                    personalize.setLoginLogo(newNameTwo);
                    flag = personalizedService.updateLogo(personalize);
                    break;
                case "1": { // 设为默认首页logo
                    String newName = saveDefaultPic(personalized.getHomeLogo(), 2, request);
                    personalize.setHomeLogo(newName);
                    flag = personalizedService.updateLogo(personalize);
                    break;
                }
                case "2":  // 设为默认顶部标题
                    personalize.setTopTitle(personalized.getTopTitle());
                    flag = personalizedService.updateLogo(personalize);
                    break;
                case "3":  // 设为默认底部标题
                    personalize.setCopyright(personalized.getCopyright());
                    personalize.setWebsiteName(personalized.getWebsiteName());
                    personalize.setRecordNumber(personalized.getRecordNumber());
                    flag = personalizedService.updateLogo(personalize);
                    break;
                case "4":  // 设为默认首页名称
                    personalize.setFrontPage(personalized.getFrontPage());
                    flag = personalizedService.updateLogo(personalize);
                    break;
                case "5": { // 设为默认首页Ico
                    String newName = saveDefaultPic(personalized.getWebIco(), 2, request);
                    personalize.setWebIco(newName);
                    flag = personalizedService.updateLogo(personalize);
                    break;
                }
                case "6":
                    personalize.setServiceExpireReminder(
                        personalized.getServiceExpireReminder() == null ? 30 : personalized.getServiceExpireReminder());
                    flag = personalizedService.updateLogo(personalize);
                    break;
                case "7":  //设为默认平台网址
                    personalize
                        .setPlatformSite(personalized.getPlatformSite() == null ? "" : personalized.getPlatformSite());
                    flag = personalizedService.updateLogo(personalize);
                    break;
                case "8"://设置默认视屏背景图
                    String newName = saveDefaultPic(personalized.getVideoBackground(), 2, request);
                    personalize.setVideoBackground(newName);
                    flag = personalizedService.updateLogo(personalize);
                    break;
                default:
                    break;
            }
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("平台信息设置页面设为默认个性化配置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 对设为默认的图片进行复制留存
     * @param picName 图片名称
     * @param pathType 对应图片存储路径
     */
    private String saveDefaultPic(String picName, Integer pathType, HttpServletRequest request) throws Exception {
        String filePath = request.getSession().getServletContext().getRealPath("/");
        if (pathType == 1) {
            filePath += "resources/img/home/";
        } else if (pathType == 2) {
            filePath += "resources/img/logo/";
        }
        //当未设置图片时，点击设为默认，直接返回空字符串作为当前字段值
        if (null == picName || "".equals(picName)) {
            return "";
        }
        //将需要设为默认的图片复制一份，防止修改时被删除
        File file = new File(filePath + picName);
        if (!file.exists()) {
            return "";
        }
        String suffix = file.getName().substring(file.getName().lastIndexOf("."));
        String newName = System.currentTimeMillis() + new Random().nextInt(100) + suffix;
        File file1 = new File(filePath + newName);
        Files.copy(file.toPath(), file1.toPath());
        return newName;
    }
}
