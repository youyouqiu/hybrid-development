package com.zw.app.controller.webMaster.personalized;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.personalized.AppPersonalized;
import com.zw.app.service.webMaster.personalized.AppPersonalizedService;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * app后台信息管理
 * @author lijie
 * @date 2018/8/22 09:08
 */
@Controller
@RequestMapping("/m/app/personalized")
@Api(tags = { "app后台个性化配置" }, description = "app后台配置相关接口")
public class WebMasterPersonalizedController {
    private static Logger log = LogManager.getLogger(WebMasterPersonalizedController.class);
    @Autowired
    AppPersonalizedService appPersonalizedService;

    @Value("${sys.error.msg}")
    private String sysError;

    private static final String APP_CONFIG_PAGE = "modules/intercomplatform/app/appSetting";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView feedbackList() {
        try {
            ModelAndView mav = new ModelAndView(APP_CONFIG_PAGE);
            return mav;
        } catch (Exception e) {
            log.error("获取app信息配置界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取app后台个性化信息管理
     * @author lijie
     * @date 2018/8/22 11:08
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getAppPersonalizedConfig() {
        try {
            AppResultBean appPersonalized = appPersonalizedService.find();
            if (appPersonalized != null) {
                return appPersonalized;
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("获取app后台配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改登录页标题
     * @author lijie
     * @date 2018/8/23 10:55
     */
    @Auth
    @RequestMapping(value = { "/title" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateLoginTitle(String title) {
        try {
            if (title != null) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setLoginTitle(title);
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改登录页标题异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复登录页标题为默认
     * @author lijie
     * @date 2018/8/23 10:55
     */
    @Auth
    @RequestMapping(value = { "/title/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetLoginTitle() {
        try {
            boolean success = appPersonalizedService.resetLoginTitle();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复登录页标题异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置当前登录页标题为组织默认
     * @author lijie
     * @date 2018/8/23 15:15
     */
    @Auth
    @RequestMapping(value = { "/title/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultLoginTitle(String title) {
        try {
            boolean success = appPersonalizedService.defaultLoginTitle(title);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置当前登录页标题为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改平台网址
     * @author lijie
     * @date 2018/8/23 16:36
     */
    @Auth
    @RequestMapping(value = { "/url" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateLoginUrl(String url) {
        try {
            if (url != null) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setWebsiteName(url);
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改平台网址异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复平台网址为组织默认
     * @author lijie
     * @date 2018/8/23 16:55
     */
    @Auth
    @RequestMapping(value = { "/url/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetLoginUrl() {
        try {
            boolean success = appPersonalizedService.resetLoginUrl();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复平台网址为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置当前平台网址为组织默认
     * @author lijie
     * @date 2018/8/23 16:55
     */
    @Auth
    @RequestMapping(value = { "/url/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultLoginUrl(String url) {
        try {
            boolean success = appPersonalizedService.defaultLoginUrl(url);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复平台网址为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改关于登录提示
     * @author lijie
     * @date 2018/8/23 17:19
     */
    @Auth
    @RequestMapping(value = { "/aboutLogin" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateAboutLogin(String aboutLogin) {
        try {
            if (aboutLogin != null) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setLoginPrompt(aboutLogin);
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改关于登录提示异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复关于登录提示为组织默认
     * @author lijie
     * @date 2018/8/23 16:55
     */
    @Auth
    @RequestMapping(value = { "/aboutLogin/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetAboutLogin() {
        try {
            boolean success = appPersonalizedService.resetAboutLogin();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复关于登录提示为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置关于登录提示为组织默认
     * @author lijie
     * @date 2018/8/23 16:55
     */
    @Auth
    @RequestMapping(value = { "/aboutLogin/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAboutLogin(String aboutLogin) {
        try {
            boolean success = appPersonalizedService.defaultAboutLogin(aboutLogin);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复关于登录提示为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改忘记密码提示
     * @author lijie
     * @date 2018/8/24 09:19
     */
    @Auth
    @RequestMapping(value = { "/pwdComment" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updatePwdComment(String pwdComment) {
        try {
            if (pwdComment != null) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setPasswordPrompt(pwdComment);
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改忘记密码提示异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复忘记密码提示为组织默认
     * @author lijie
     * @date 2018/8/24 09:40
     */
    @Auth
    @RequestMapping(value = { "/pwdComment/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetPwdComment() {
        try {
            boolean success = appPersonalizedService.resetPwdComment();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复忘记密码提示为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置忘记密码提示为组织默认
     * @author lijie
     * @date 2018/8/24 09:50
     */
    @Auth
    @RequestMapping(value = { "/pwdComment/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultPwdComment(String pwdComment) {
        try {
            boolean success = appPersonalizedService.defaultPwdComment(pwdComment);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复忘记密码提示为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改关于我们显示内容
     * @author lijie
     * @date 2018/8/24 09:19
     */
    @Auth
    @RequestMapping(value = { "/aboutUs" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateAboutUs(String aboutUs) {
        try {
            if (aboutUs != null) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setAboutPlatform(aboutUs);
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改关于我们显示内容异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复关于我们显示内容为组织默认
     * @author lijie
     * @date 2018/8/24 10:08
     */
    @Auth
    @RequestMapping(value = { "/aboutUs/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetAboutUs() {
        try {
            boolean success = appPersonalizedService.resetAboutUs();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复关于我们显示内容为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置当前关于我们显示内容为组织默认
     * @author lijie
     * @date 2018/8/24 09:50
     */
    @Auth
    @RequestMapping(value = { "/aboutUs/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAboutUs(String aboutUs) {
        try {
            boolean success = appPersonalizedService.defaultAboutUs(aboutUs);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置当前关于我们显示内容为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改开始聚合对象数量
     * @author lijie
     * @date 2018/8/24 10:54
     */
    @Auth
    @RequestMapping(value = { "/aggrNum" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateAggrNum(String aggrNum) {
        try {
            if (StringUtils.isNotBlank(aggrNum)) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setAggregationNumber(Integer.parseInt(aggrNum));
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改开始聚合对象数量异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复开始聚合对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 10:08
     */
    @Auth
    @RequestMapping(value = { "/aggrNum/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetAggrNum() {
        try {
            boolean success = appPersonalizedService.resetAggrNum();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复开始聚合对象数量为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置开始聚合对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 11:00
     */
    @Auth
    @RequestMapping(value = { "/aggrNum/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAggrNum(String aggrNum) {
        try {
            boolean success = appPersonalizedService.defaultAggrNum(Integer.parseInt(aggrNum));
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置当前开始聚合对象数量为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改历史数据最大查询时间范围
     * @author lijie
     * @date 2018/8/24 11:23
     */
    @Auth
    @RequestMapping(value = { "/historyPeriod" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateHistoryPeriod(String historyPeriod) {
        try {
            if (StringUtils.isNotBlank(historyPeriod)) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setHistoryTimeLimit(Integer.parseInt(historyPeriod));
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改历史数据最大查询时间范围异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复历史数据最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:24
     */
    @Auth
    @RequestMapping(value = { "/historyPeriod/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetHistoryPeriod() {
        try {
            boolean success = appPersonalizedService.resetHistoryPeriod();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复历史数据最大查询时间范围为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置历史数据最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:00
     */
    @Auth
    @RequestMapping(value = { "/historyPeriod/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultHistoryPeriod(String historyPeriod) {
        try {
            boolean success = appPersonalizedService.defaultHistoryPeriod(Integer.parseInt(historyPeriod));
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置历史数据最大查询时间范围为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改报警最大查询时间范围
     * @author lijie
     * @date 2018/8/24 11:23
     */
    @Auth
    @RequestMapping(value = { "/alarmPeriod" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateAlarmPeriod(String alarmPeriod) {
        try {
            if (StringUtils.isNotBlank(alarmPeriod)) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setAlarmTimeLimit(Integer.parseInt(alarmPeriod));
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改报警最大查询时间范围异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复报警最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:24
     */
    @Auth
    @RequestMapping(value = { "/alarmPeriod/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetAlarmPeriod() {
        try {
            boolean success = appPersonalizedService.resetAlarmPeriod();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复报警最大查询时间范围为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置报警最大查询时间范围为组织默认
     * @author lijie
     * @date 2018/8/24 11:55
     */
    @Auth
    @RequestMapping(value = { "/alarmPeriod/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAlarmPeriod(String alarmPeriod) {
        try {
            boolean success = appPersonalizedService.defaultAlarmPeriod(Integer.parseInt(alarmPeriod));
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置报警最大查询时间范围为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改统计最多选择对象数量
     * @author lijie
     * @date 2018/8/24 13:40
     */
    @Auth
    @RequestMapping(value = { "/maxStatObjNum" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateMaxStatObjNum(String maxStatObjNum) {
        try {
            if (StringUtils.isNotBlank(maxStatObjNum)) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setMaxObjectnumber(Integer.parseInt(maxStatObjNum));
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改统计最多选择对象数量异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复统计最多选择对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 13:50
     */
    @Auth
    @RequestMapping(value = { "/maxStatObjNum/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetMaxStatObjNum() {
        try {
            boolean success = appPersonalizedService.resetMaxStatObjNum();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复统计最多选择对象数量为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置当前统计最多选择对象数量为组织默认
     * @author lijie
     * @date 2018/8/24 13:55
     */
    @Auth
    @RequestMapping(value = { "/maxStatObjNum/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultMaxStatObjNum(String maxStatObjNum) {
        try {
            boolean success = appPersonalizedService.defaultMaxStatObjNum(Integer.parseInt(maxStatObjNum));
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置当前统计最多选择对象数量为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改登录页logo
     * @author lijie
     * @date 2018/8/24 17:23
     */
    @Auth
    @RequestMapping(value = { "/logo" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateLoginLogo(MultipartFile imageFile, HttpServletRequest request) {
        try {
            if (!imageFile.isEmpty()) {
                if (imageFile.getSize() <= 1024 * 400) {
                    String logoName = appPersonalizedService.updateLoginLogo(imageFile, request);
                    if (logoName != null) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("image", logoName);
                        return new AppResultBean(jsonObject);
                    } else {
                        return new AppResultBean(AppResultBean.PARAM_ERROR, "上传的图片格式只支持png或jpg格式");
                    }
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR, "最大只能上传400K的图片");
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR, "未获取到上传的图片文件");
            }
        } catch (Exception e) {
            log.error("修改登录页logo异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复登录页logo为组织默认
     * @author lijie
     * @date 2018/8/27 10:50
     */
    @Auth
    @RequestMapping(value = { "/logo/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetLoginLogo(HttpServletRequest request) {
        try {
            boolean success = appPersonalizedService.resetLoginLogo(request);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复登录页logo为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置当前登录页logo为组织默认
     * @author lijie
     * @date 2018/8/27 11:05
     */
    @Auth
    @RequestMapping(value = { "/logo/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultLoginLogo(String image, HttpServletRequest request) {
        try {
            boolean success = appPersonalizedService.defaultLoginLogo(image, request);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置当前登录页logo为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改用户组织头像
     * @author lijie
     * @date 2018/8/27 14:40
     */
    @Auth
    @RequestMapping(value = { "/groupAvatar" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateGroupAvatar(MultipartFile avatarFile, HttpServletRequest request) {
        try {
            if (!avatarFile.isEmpty()) {
                if (avatarFile.getSize() <= 1024 * 200) {
                    String avatarName = appPersonalizedService.updateGroupAvatar(avatarFile, request);
                    if (avatarName != null) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("avatar", avatarName);
                        return new AppResultBean(jsonObject);
                    } else {
                        return new AppResultBean(AppResultBean.PARAM_ERROR, "上传的图片格式只支持png或jpg格式");
                    }
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR, "最大只能上传200K的图片");
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR, "未获取到上传的图片文件");
            }
        } catch (Exception e) {
            log.error("修改用户组织头像异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复用户组织头像为组织默认
     * @author lijie
     * @date 2018/8/27 15:50
     */
    @Auth
    @RequestMapping(value = { "/groupAvatar/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetGroupAvatar(HttpServletRequest request) {
        try {
            boolean success = appPersonalizedService.resetGroupAvatar(request);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复登录页logo为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置当前用户组织头像为组织默认
     * @author lijie
     * @date 2018/8/27 15:45
     */
    @Auth
    @RequestMapping(value = { "/groupAvatar/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultGroupAvatar(String avatar, HttpServletRequest request) {
        try {
            boolean success = appPersonalizedService.defaultGroupAvatar(avatar, request);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置当前登录页logo为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 根据当前用户获取其所属企业信息
     * @return
     */
    @Auth
    @RequestMapping(value = { "/groupInfoByUser" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getGroupInfoByUser() {
        try {
            OrganizationLdap org = appPersonalizedService.getGroupInfoByUser();
            if (org != null) {
                return new AppResultBean(org);
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("根据当前用户获取其所属企业信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改是否开启adas
     * @author lijie
     * @date 2018/12/06 10:40
     */
    @Auth
    @RequestMapping(value = { "/adasFlag" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateAdasFlag(String adasFlag) {
        try {
            if (StringUtils.isNotBlank(adasFlag)) {
                AppPersonalized appPersonalized = new AppPersonalized();
                appPersonalized.setAdasFlag(adasFlag);
                appPersonalized.setGroupDefault(0);
                boolean success = appPersonalizedService.updateAppPersonalized(appPersonalized);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("修改是否开启adas异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复是否开启adas为组织默认
     * @author lijie
     * @date 2018/8/24 13:50
     */
    @Auth
    @RequestMapping(value = { "/adasFlag/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetAdasFlag() {
        try {
            boolean success = appPersonalizedService.resetAdasFlag();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复是否开启adas为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置是否开启adas为组织默认
     * @author lijie
     * @date 2018/8/24 13:55
     */
    @Auth
    @RequestMapping(value = { "/adasFlag/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAdasFlag(String adasFlag) {
        try {
            boolean success = appPersonalizedService.defaultAdasFlag(adasFlag);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置是否开启adas为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}
