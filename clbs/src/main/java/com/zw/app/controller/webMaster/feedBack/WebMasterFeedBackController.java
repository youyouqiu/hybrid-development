package com.zw.app.controller.webMaster.feedBack;

import com.github.pagehelper.Page;
import com.zw.app.domain.webMaster.feedBack.FeedBack;
import com.zw.app.domain.webMaster.feedBack.FeedBackQuery;
import com.zw.app.service.webMaster.feedBack.AppFeedBackService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * app后台信息反馈管理
 * @author lijie
 * @date 2018/8/29 15:59
 */
@Controller
@RequestMapping("/m/app")
@Api(tags = { "app后台信息反馈管理" }, description = "app后台信息反馈相关接口")
public class WebMasterFeedBackController {
    @Autowired
    AppFeedBackService appFeedBackService;

    @Value("${sys.error.msg}")
    private String sysError;

    private static Logger log = LogManager.getLogger(WebMasterFeedBackController.class);

    private static final String LIST_PAGE = "modules/intercomplatform/app/feedback";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Auth
    @RequestMapping(value = "/feedback/page", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView feedbackList() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            return mav;
        } catch (Exception e) {
            log.error("查询app意见反馈信息界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询app意见反馈信息
     * @author lijie
     * @date 2018/8/29 15:59
     */
    @Auth
    @RequestMapping(value = { "/feedback" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean searchFeedBack(FeedBackQuery feedBackQuery) {
        try {
            if (StringUtils.isNotBlank(feedBackQuery.getStartTime()) && StringUtils
                .isNotBlank(feedBackQuery.getEndTime()) && feedBackQuery.getLength() != 0
                && feedBackQuery.getStart() >= 0) {
                Page<FeedBack> feedBacks = (Page<FeedBack>) appFeedBackService.searchFeedBack(feedBackQuery, true);
                return new PageGridBean(feedBackQuery, feedBacks, true);

            } else {
                return new PageGridBean(false);
            }
        } catch (Exception e) {
            log.error("查询app意见反馈信息异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/feedbackSearch/export", method = RequestMethod.POST)
    @ResponseBody
    public boolean export(FeedBackQuery feedBackQuery, HttpServletResponse res) {
        try {
            return appFeedBackService.listExport(feedBackQuery, res);
        } catch (Exception e) {
            log.error("查询app意见反馈信息异常", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/feedbackSearch/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "APP用户意见反馈");
            appFeedBackService.export(null, 1, res);
        } catch (Exception e) {
            log.error("APP意见反馈导出数据异常", e);
        }
    }
}