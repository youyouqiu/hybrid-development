package com.zw.lkyw.controller.positioningStatistics;

import com.zw.lkyw.domain.positioningStatistics.AllEnterpriseInfo;
import com.zw.lkyw.domain.positioningStatistics.ExceptionInfoQueryParam;
import com.zw.lkyw.domain.positioningStatistics.ExceptionInfoResult;
import com.zw.lkyw.domain.positioningStatistics.ExceptionListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.ExceptionPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.GroupListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.GroupPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.MonitorInterruptDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorOfflineDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorPositioningInfo;
import com.zw.lkyw.domain.positioningStatistics.MonthListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.MonthPositioningResult;
import com.zw.lkyw.service.positioningStatistics.PositioningStatisticsService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lkyw/vehicle/positioning/statistics")
public class PositioningStatisticsController {

    private Logger log = LogManager.getLogger(PositioningStatisticsController.class);

    private static final String LIST_PAGE = "lkyw/positioningStatistics/list";
    @Autowired
    PositioningStatisticsService positioningStatisticsService;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 企业车辆定位统计报表查询
     * @param param
     * @return
     */
    @RequestMapping(value = { "/group/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(GroupListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getGroupIds())) {
                return positioningStatisticsService.enterpriseList(param);
            }
        } catch (Exception e) {
            log.error("获取企业车辆定位统计数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 企业车辆定位统计报表详情查询
     * @param param
     * @return
     */
    @RequestMapping(value = { "/group/locationInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean locationInfo(GroupListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getGroupIds())) {
                return positioningStatisticsService.enterpriseLocationInfo(param);
            }
        } catch (Exception e) {
            log.error("获取企业车辆定位统计报表详情查询数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 企业车辆定位中断统计报表详情查询
     * @param param
     * @return
     */
    @RequestMapping(value = { "/group/interruptInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean interruptInfo(GroupListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getGroupIds())) {
                return positioningStatisticsService.enterpriseInterruptInfo(param);
            }
        } catch (Exception e) {
            log.error("获取企业车辆定位中断统计报表详情查询数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 企业车辆无定位统计报表详情查询
     * @param param
     * @return
     */
    @RequestMapping(value = { "/group/unLocationInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean unLocationInfo(GroupListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getGroupIds())) {
                return positioningStatisticsService.enterpriseUnLocationInfo(param);
            }
        } catch (Exception e) {
            log.error("获取企业车辆无定位统计报表详情查询数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 企业车辆定位离线位移统计报表详情查询
     * @param param
     * @return
     */
    @RequestMapping(value = { "/group/offlineInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean offlineInfo(GroupListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getGroupIds())) {
                return positioningStatisticsService.enterpriseOfflineInfo(param);
            }
        } catch (Exception e) {
            log.error("获取企业车辆离线位移统计报表详情查询数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 车辆月度定位统计报表
     * @param param
     * @return
     */
    @RequestMapping(value = { "/month/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean monthPositioningList(MonthListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getMonitorIds())) {
                return positioningStatisticsService.monthPositioningList(param);
            }
        } catch (Exception e) {
            log.error("获取车辆月度定位统计报表数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 异常定位统计报表
     * @param param
     * @return
     */
    @RequestMapping(value = { "/exception/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean exceptionPositioningList(ExceptionListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getMonitorIds())) {
                return positioningStatisticsService.exceptionPositioningList(param);
            }
        } catch (Exception e) {
            log.error("获取异常定位统计报表数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 异常定位统计报表详情
     * @param param
     * @return
     */
    @RequestMapping(value = { "/exception/info" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean exceptionPositioningInfo(ExceptionInfoQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getMonitorId())) {
                return positioningStatisticsService.exceptionPositioningInfo(param);
            }
        } catch (Exception e) {
            log.error("获取异常定位统计报表数据异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT);
    }

    /**
     * 导出企业车辆定位统计报表统计列表(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/exportGroupList", method = RequestMethod.POST)
    @ResponseBody
    public void exportGroupList(HttpServletResponse res, GroupListQueryParam param) {
        try {
            List<GroupPositioningResult> exportGroupList = positioningStatisticsService.exportGroupList(param);
            Map<String, Object> data = new HashMap<>();
            data.put("exportGroupList", exportGroupList);
            String fileName = "企业车辆定位统计";
            templateExportExcel.templateExportExcel("/file/cargoReport/groupPositioning.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出监控对象评分统计列表异常", e);
        }
    }

    /**
     * 批量导出企业车辆定位统计报表详情(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/exportAllGroupPositioning", method = RequestMethod.POST)
    @ResponseBody
    public void exportAllGroupPositioning(HttpServletResponse res, GroupListQueryParam param) {
        try {
            AllEnterpriseInfo allEnterpriseInfo = positioningStatisticsService.exportAllGroupPositioning(param);
            Map<String, Object> data = new HashMap<>();
            data.put("locationResultList", allEnterpriseInfo.getLocationResultList());
            data.put("unLocationResultList", allEnterpriseInfo.getUnLocationResultList());
            data.put("enterpriseInterruptList", allEnterpriseInfo.getEnterpriseInterruptList());
            data.put("offlineDetailResultList", allEnterpriseInfo.getOfflineDetailResultList());
            String fileName = "企业车辆定位统计(明细)";
            templateExportExcel.templateExportExcel("/file/cargoReport/allEnterpriseInfo.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("批量导出企业车辆定位统计报表详情异常", e);
        }
    }

    /**
     * 导出定位统计报表详情(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/exportLocationPositioning", method = RequestMethod.POST)
    @ResponseBody
    public void exportLocationPositioning(HttpServletResponse res, GroupListQueryParam param) {
        try {
            List<MonitorPositioningInfo> locationResultList =
                positioningStatisticsService.exportLocationPositioning(param);
            Map<String, Object> data = new HashMap<>();
            data.put("locationResultList", locationResultList);
            String fileName = "定位统计明细";
            templateExportExcel.templateExportExcel("/file/cargoReport/locationInfo.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出定位统计明细详情异常", e);
        }
    }

    /**
     * 导出无定位统计报表详情(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/exportUnLocationPositioning", method = RequestMethod.POST)
    @ResponseBody
    public void exportUnLocationPositioning(HttpServletResponse res, GroupListQueryParam param) {
        try {
            List<MonitorPositioningInfo> unLocationResultList =
                positioningStatisticsService.exportUnLocationPositioning(param);
            Map<String, Object> data = new HashMap<>();
            data.put("unLocationResultList", unLocationResultList);
            String fileName = "无定位统计明细";
            templateExportExcel.templateExportExcel("/file/cargoReport/unLocationInfo.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出无定位统计明细详情异常", e);
        }
    }

    /**
     * 导出定位中断统计明细报表详情(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/exportInterruptInfo", method = RequestMethod.POST)
    @ResponseBody
    public void exportInterruptInfo(HttpServletResponse res, GroupListQueryParam param) {
        try {
            List<MonitorInterruptDetailInfo> enterpriseInterruptList =
                positioningStatisticsService.exportInterruptInfo(param);
            Map<String, Object> data = new HashMap<>();
            data.put("enterpriseInterruptList", enterpriseInterruptList);
            String fileName = "定位中断统计明细";
            templateExportExcel.templateExportExcel("/file/cargoReport/enterpriseInterrupt.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出定位中断统计明细详情异常", e);
        }
    }

    /**
     * 导出离线位移统计报表详情(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/exportOfflineInfo", method = RequestMethod.POST)
    @ResponseBody
    public void exportOfflineInfo(HttpServletResponse res, GroupListQueryParam param) {
        try {
            List<MonitorOfflineDetailInfo> offlineDetailResultList =
                positioningStatisticsService.exportOfflineInfo(param);
            Map<String, Object> data = new HashMap<>();
            data.put("offlineDetailResultList", offlineDetailResultList);
            String fileName = "离线位移统计明细";
            templateExportExcel.templateExportExcel("/file/cargoReport/offlineDetail.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出离线位移统计明细详情异常", e);
        }
    }

    /**
     * 车辆月度定位统计报表
     * @param param
     * @return
     */
    @RequestMapping(value = { "/exportMonthPositioningList" }, method = RequestMethod.POST)
    @ResponseBody
    public void exportMonthPositioningList(HttpServletResponse res, MonthListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getMonitorIds())) {
                List<MonthPositioningResult> monthPositioningList =
                    positioningStatisticsService.exportMonthPositioningList(param);
                Map<String, Object> data = new HashMap<>();
                data.put("monthPositioningList", monthPositioningList);
                data.put("days", monthPositioningList.get(0).getMonthDetailInfoList());
                String fileName = "车辆月度定位统计";
                templateExportExcel.templateExportExcel(
                    "/file/cargoReport/monthPositioningList" + monthPositioningList.get(0).getMonthDetailInfoList()
                        .size() + ".xls", res, data, fileName);
            }
        } catch (Exception e) {
            log.error("导出车辆月度定位统计报表异常", e);
        }
    }

    /**
     * 导出异常定位统计报表
     * @param param
     * @return
     */
    @RequestMapping(value = { "/exportExceptionList" }, method = RequestMethod.POST)
    @ResponseBody
    public void exportExceptionList(HttpServletResponse res, ExceptionListQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getMonitorIds())) {
                List<ExceptionPositioningResult> exceptionPositioning =
                    positioningStatisticsService.exportExceptionList(param);
                Map<String, Object> data = new HashMap<>();
                data.put("exceptionPositioningList", exceptionPositioning);
                String fileName = "异常定位统计报表";
                templateExportExcel.templateExportExcel("/file/cargoReport/exceptionList.xls", res, data, fileName);
            }
        } catch (Exception e) {
            log.error("导出异常定位统计报表异常", e);
        }
    }

    /**
     * 导出异常定位统计报表详情明细
     * @param param
     * @return
     */
    @RequestMapping(value = { "/exportExceptionInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public void exportExceptionInfo(HttpServletResponse res, ExceptionInfoQueryParam param) {
        try {
            if (!StringUtil.isNullOrEmpty(param.getMonitorId())) {
                List<ExceptionInfoResult> exceptionInfoList = positioningStatisticsService.exportExceptionInfo(param);
                Map<String, Object> data = new HashMap<>();
                data.put("exceptionInfoList", exceptionInfoList);
                String fileName = "异常定位统计(明细)";
                templateExportExcel.templateExportExcel("/file/cargoReport/exceptionInfoList.xls", res, data, fileName);
            }
        } catch (Exception e) {
            log.error("导出异常定位统计报表详情明细异常", e);
        }
    }

}
