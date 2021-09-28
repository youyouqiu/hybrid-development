package com.zw.talkback.common;

import com.cb.platform.util.page.PageResultBean;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.imports.ZwImportException;
import com.zw.talkback.util.common.QueryFunction;
import com.zw.talkback.util.common.QueryResultBeanFunction;
import com.zw.talkback.util.common.VoidFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 功能描述:controller模板工具方法
 */
public class ControllerTemplate {

    private static final Logger log = LogManager.getLogger(ControllerTemplate.class);

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 通用返回结果的查询方法
     */
    public static void execute(VoidFunction function, String errMsg) {
        try {
            function.execute();
        } catch (Exception e) {
            log.error(errMsg, e);

        }
    }

    /**
     * 直接返回的情况
     */
    public static <T> T execute(QueryFunction<T> function, String errMsg, T defaultVal) {
        try {
            return function.execute();
        } catch (Exception e) {
            log.error(errMsg, e);

        }
        return defaultVal;
    }



    /**
     * 通用返回结果的查询方法
     */
    public static <T> JsonResultBean getResultBean(QueryFunction<T> function, String errMsg,
        BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            T result = function.execute();
            return new JsonResultBean(result);
        } catch (BusinessException e1) {
            return new JsonResultBean(JsonResultBean.FAULT, e1.getCode());
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT, errMsg);

        }
    }

    /**
     * 通用返回结果的查询方法
     */
    public static <T> JsonResultBean getResultBean(QueryFunction<T> function, String errMsg) {
        try {
            T result = function.execute();
            return new JsonResultBean(result);
        } catch (BusinessException e1) {
            return new JsonResultBean(JsonResultBean.FAULT, e1.getCode());
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT, errMsg);

        }
    }

    /**
     * 通用返回结果的查询方法
     */
    public static <T> JsonResultBean getResultBean(QueryFunction<T> function) {
        try {
            T result = function.execute();
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e instanceof BusinessException) {
                return new JsonResultBean(JsonResultBean.FAULT, ((BusinessException) e).getDetailMsg());
            }
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 通用返回分页结果的查询方法
     * @deprecated 职责不清晰，controller不负责分页（而是service调用dao层时），该实现要求service第一条MyBatis访问时分页，属于滥用
     */
    public static <T> PageGridBean getResultBean(QueryFunction<List<T>> function, BaseQueryBean baseQueryBean,
        String errMsg) {
        PageHelper.startPage(baseQueryBean.getPage().intValue(), baseQueryBean.getLimit().intValue());
        try {
            Page<T> pageInfo = (Page<T>) function.execute();
            return new PageGridBean(baseQueryBean, pageInfo, JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error(errMsg, e);
            if (e instanceof BusinessException) {
                BusinessException exception = (BusinessException) e;
                String msg = StringUtils.isBlank(exception.getCode()) ? exception.getDetailMsg() : exception.getCode();
                return new PageGridBean(JsonResultBean.FAULT, msg);
            }
            return new PageGridBean(JsonResultBean.FAULT, e.getMessage());
        } finally {
            PageHelper.clearPage();
        }
    }

    /**
     * 通用返回分页结果的查询方法,基于redis
     */
    public static <T> PageGridBean getPageGridBean(QueryFunction<List<T>> function, BaseQueryBean baseQueryBean,
        String errMsg) {

        try {
            Page<T> pageInfo = (Page<T>) function.execute();
            return new PageGridBean(baseQueryBean, pageInfo, JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error(errMsg, e);
            if (e instanceof BusinessException) {
                BusinessException exception = (BusinessException) e;
                String msg = StringUtils.isBlank(exception.getCode()) ? exception.getDetailMsg() : exception.getCode();
                return new PageGridBean(JsonResultBean.FAULT, msg);
            }
            return new PageGridBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 通用返回分页结果,调用pass端接口
     */
    public static PageGridBean getPassPageBean(QueryFunction<?> function, String errMsg) {
        try {
            PassCloudResultBean passCloudResultBean = (PassCloudResultBean) function.execute();
            if (!passCloudResultBean.isSuccess()) {
                return new PageGridBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
            }
            if (passCloudResultBean.getData() == null) {
                return new PageGridBean(JsonResultBean.SUCCESS);
            }
            PageResultBean pageResultBean = (PageResultBean) passCloudResultBean.getData();
            if (CollectionUtils.isEmpty(pageResultBean.getItems())) {
                return new PageGridBean(JsonResultBean.SUCCESS);
            }
            return new PageGridBean(pageResultBean);
        } catch (Exception e) {
            log.error(errMsg, e);
            return new PageGridBean();
        }

    }

    /**
     * 通用返回结果的查询方法,调用pass端接口
     */
    public static JsonResultBean getPassResultBean(QueryFunction<?> function, String errMsg) {
        try {
            PassCloudResultBean passCloudResultBean = (PassCloudResultBean) function.execute();
            if (!passCloudResultBean.isSuccess()) {
                return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
            }
            return new JsonResultBean(passCloudResultBean.getData());
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT);

        }
    }

    /**
     * 分页处理
     * @param list      显示数据
     * @param query     页面传入参数
     * @param totalSize 总条数
     * @return page 返回page对象
     */
    private static <T> Page<T> getListToPage(List<T> list, BaseQueryBean query, int totalSize) {

        Page<T> result = new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
        if (list != null && list.size() > 0) {
            result.addAll(list);
            result.setTotal(totalSize);
        } else {
            result.setTotal(0);
        }
        return result;
    }

    /**
     * 通用新增数据方法
     */
    public static JsonResultBean getResultBean(VoidFunction function, String errMsg) {
        try {
            function.execute();
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                BusinessException exception = (BusinessException) e;
                String msg = StringUtils.isBlank(exception.getCode()) ? exception.getDetailMsg() : exception.getCode();
                return new JsonResultBean(JsonResultBean.FAULT, msg);
            }
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 专门用户离线导出报表并判断文件是否导出完成就立即推送的方法
     */
    public static JsonResultBean addExportOffline(OfflineExportService exportService, OfflineExportInfo exportInfo,
        String errMsg) {
        try {
            Map<String, String> sendMap = exportService.addOfflineExport(exportInfo);
            if (sendMap != null) {
                exportService.senExportResultMsg(sendMap);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT, errMsg);
        }
    }

    public static JsonResultBean getResultBean(QueryResultBeanFunction function, String errMsg) {
        try {
            return function.execute();
        } catch (Exception e) {
            log.error(errMsg, e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    public static ModelAndView editPage(String editPage, QueryFunction function) {
        try {
            ModelAndView mav = new ModelAndView(editPage);
            mav.addObject("result", function.execute());
            return mav;
        } catch (Exception e) {
            return new ModelAndView(ERROR_PAGE);
        }
    }

    public static ModelAndView editPage(ModelAndView modelAndView) {
        try {
            return modelAndView;
        } catch (Exception e) {
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 导出的通用方法
     */
    public static void export(VoidFunction function, String fileName, HttpServletResponse response, String errMsg) {
        try {
            ExportExcelUtil.setResponseHead(response, fileName);
            function.execute();
        } catch (Exception e) {
            log.error(errMsg, e);
        }
    }

    public static <T> JsonResultBean getResult(QueryFunction<T> function) {
        try {
            T result = function.execute();
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e instanceof BusinessException) {
                BusinessException exception = (BusinessException) e;
                String msg = StringUtils.isBlank(exception.getCode()) ? exception.getDetailMsg() : exception.getCode();
                return new JsonResultBean(JsonResultBean.FAULT, msg);
            }
            if (e instanceof ZwImportException) {
                ZwImportException exception = (ZwImportException) e;
                return new JsonResultBean(JsonResultBean.FAULT, exception.getMessage());
            }
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");

        }
    }

    public static JsonResultBean getBooleanResult(QueryFunction<Boolean> function) {
        return getBooleanResult(function, null);
    }

    public static JsonResultBean getBooleanResult(QueryFunction<Boolean> function, String successMsg) {
        try {
            boolean result = function.execute();
            return new JsonResultBean(result, successMsg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e instanceof BusinessException) {
                BusinessException exception = (BusinessException) e;
                String msg = StringUtils.isBlank(exception.getCode()) ? exception.getDetailMsg() : exception.getCode();
                return new JsonResultBean(JsonResultBean.FAULT, msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");

        }
    }

    public static PageGridBean getPageResult(QueryFunction<PageGridBean> function) {
        try {
            return function.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (e instanceof BusinessException) {
                BusinessException exception = (BusinessException) e;
                String msg = StringUtils.isBlank(exception.getCode()) ? exception.getDetailMsg() : exception.getCode();
                return new PageGridBean(JsonResultBean.FAULT, msg);
            }
            return new PageGridBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");

        }
    }
}
