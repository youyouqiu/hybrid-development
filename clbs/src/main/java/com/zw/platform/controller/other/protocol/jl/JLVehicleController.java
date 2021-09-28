package com.zw.platform.controller.other.protocol.jl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleQuery;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordDto;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordQuery;
import com.zw.platform.domain.other.protocol.jl.dto.ViolateVehicleDTO;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehiclePageReq;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.CorpCheckReq;
import com.zw.platform.domain.other.protocol.jl.query.ExportOperationStatusReq;
import com.zw.platform.domain.other.protocol.jl.query.JiLinVehicleSetListQuery;
import com.zw.platform.domain.other.protocol.jl.query.QueryAloneVehicleInfo;
import com.zw.platform.domain.other.protocol.jl.query.QueryPlatformCheckInfo;
import com.zw.platform.domain.other.protocol.jl.query.SingleAlarmVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.SingleViolateVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehicleExportReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehiclePageReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehicleReq;
import com.zw.platform.domain.other.protocol.jl.resp.AloneCorpInfoResp;
import com.zw.platform.domain.other.protocol.jl.resp.AloneVehicleInfoResp;
import com.zw.platform.domain.other.protocol.jl.resp.CorpAlarmCheckInfoResp;
import com.zw.platform.domain.other.protocol.jl.resp.CorpCheckInfoResp;
import com.zw.platform.domain.other.protocol.jl.resp.PlatformCheckInfoResp;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.other.protocol.jl.CorpAlarmCheckService;
import com.zw.platform.service.other.protocol.jl.CorpCheckService;
import com.zw.platform.service.other.protocol.jl.JiLinAlarmVehicleDataService;
import com.zw.platform.service.other.protocol.jl.JiLinVehicleOperationStatusService;
import com.zw.platform.service.other.protocol.jl.JiLinVehicleService;
import com.zw.platform.service.other.protocol.jl.PlatformCheckService;
import com.zw.platform.service.other.protocol.jl.QueryVehicleInfoService;
import com.zw.platform.service.other.protocol.jl.StoppedVehicleService;
import com.zw.platform.service.other.protocol.jl.ViolateVehicleService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.TemplateExportExcel;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.zw.protocol.util.ProtocolTypeUtil.T809_JI_LIN_PROTOCOL_809_2013;

/**
 * 停运车辆
 * @author create by zhouzongbo
 */
@Controller
@RequestMapping("/jl/vehicle")
public class JLVehicleController {
    private static final Logger logger = LoggerFactory.getLogger(JLVehicleController.class);

    private static final String LIST_PAGE = "modules/jl/vehicle/list";
    private static final String UPLOAD_STOP_VEHICLE = "modules/jl/vehicle/uploadStopVehicle";
    private static final String UPLOAD_STOP_VEHICLE_BATCH = "modules/jl/vehicle/uploadStopVehicleBatch";
    /**
     * 违规车辆单车上传页面
     */
    private static final String VIOLATE_SINGLE_VEHICLE_UPLOAD_PAGE = "modules/jl/vehicle/violate/single-upload";
    /**
     * 违规车辆批量上传页面
     */
    private static final String VIOLATE_BATCH_VEHICLE_UPLOAD_PAGE = "modules/jl/vehicle/violate/batch-upload";

    /**
     * 默认错误页面
     */
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Autowired
    private StoppedVehicleService stoppedVehicleService;

    @Autowired
    private ViolateVehicleService violateVehicleService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private QueryVehicleInfoService queryVehicleInfo;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Autowired
    private PlatformCheckService platformCheckService;

    @Autowired
    private CorpCheckService corpCheckService;

    @Autowired
    private CorpAlarmCheckService corpAlarmCheckService;

    @Autowired
    private JiLinVehicleService jiLinVehicleService;

    @Autowired
    private JiLinAlarmVehicleDataService jiLinAlarmVehicleDataService;

    @Autowired
    private JiLinVehicleOperationStatusService jiLinVehicleOperationStatusService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 停运页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 单个上传停运页面
     */
    @RequestMapping(value = "/stopped/upload", method = RequestMethod.GET)
    public ModelAndView getUploadStopVehicle(String monitor) {
        ModelAndView mav = new ModelAndView(UPLOAD_STOP_VEHICLE);
        mav.addObject("monitor", monitor);
        return mav;
    }

    /**
     * 批量上传停运页面
     */
    @RequestMapping(value = "/stopped/uploadBatch", method = RequestMethod.GET)
    public ModelAndView getUploadStopVehicleBatch(String monitor) {
        ModelAndView mav = new ModelAndView(UPLOAD_STOP_VEHICLE_BATCH);
        mav.addObject("monitor", monitor);
        return mav;
    }

    /**
     * 停运车辆列表
     * @return
     */
    @RequestMapping(value = "/stopped/page", method = RequestMethod.GET)
    @ResponseBody
    public PageGridBean stoppedPage(StoppedVehicleQuery query) {
        return ControllerTemplate.getPassPageBean(() -> stoppedVehicleService.page(query), "停运车辆列表");
    }

    /**
     * 协议类型为“吉林-809”的平台
     * @return
     */
    @RequestMapping(value = "/stopped/plateform", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getStoppedPlatformInfo() {
        try {
            return new JsonResultBean(stoppedVehicleService.getStoppedPlatformInfo());
        } catch (Exception e) {
            logger.error("上传停运车辆异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 上传停运车辆
     * @return
     */
    @RequestMapping(value = "/stopped/upload", method = RequestMethod.POST)
    @ResponseBody

    public JsonResultBean stoppedUpload(String str) {
        try {
            List<StoppedVehicleRecordDto> list = JSONObject.parseArray(str, StoppedVehicleRecordDto.class);
            return stoppedVehicleService.upload(list);
        } catch (Exception e) {
            logger.error("上传停运车辆异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 停运车辆上传记录
     * @return
     */
    @RequestMapping(value = "/stopped/recordPage", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean stoppedRecordPage(StoppedVehicleRecordQuery query) {
        try {
            // 没传车id默认查权限下全部
            if (StringUtils.isNotBlank(query.getIds())) {
                List<String> vehicleIds = Arrays.asList(query.getIds().split(","));
                query.setVehicleIds(vehicleIds);
            }
            return new PageGridBean(stoppedVehicleService.recordPage(query), true);
        } catch (Exception e) {
            logger.error("上传停运车辆异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/export/stopped/recordPage", method = RequestMethod.POST)
    public void exportStoppedRecord(StoppedVehicleRecordQuery query, HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "停运车辆上传记录");
            stoppedVehicleService.exportStoppedRecord(query, response);
        } catch (Exception e) {
            logger.error("导出停运车辆上传记录异常", e);
        }
    }

    /**
     * 单个违规车辆上传页面
     * @return ModelAndView
     */
    @RequestMapping(value = { "/violate/single/upload/page/{monitorId}" }, method = RequestMethod.GET)
    public ModelAndView singleViolateUploadPage(@PathVariable String monitorId) {
        try {
            ModelAndView mav = new ModelAndView(VIOLATE_SINGLE_VEHICLE_UPLOAD_PAGE);
            final String plateNumber = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(monitorId), "name");
            mav.addObject("monitorId", monitorId);
            mav.addObject("monitorName", plateNumber);
            mav.addObject("violateTime", LocalDateUtils.dateTimeFormat(LocalDateTime.now()));
            return mav;
        } catch (Exception e) {
            logger.error("新增车辆信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 批量违规车辆上传页面
     * @return ModelAndView
     */
    @RequestMapping(value = { "/violate/batch/upload/page" }, method = RequestMethod.POST)
    public ModelAndView batchViolateUploadPage(@RequestBody Set<String> monitorIds) {
        try {
            ModelAndView mav = new ModelAndView(VIOLATE_BATCH_VEHICLE_UPLOAD_PAGE);
            final List<String> brandList = vehicleService.findBrandsByIds(monitorIds);
            mav.addObject("monitorIds", monitorIds);
            mav.addObject("monitorNames", String.join(",", brandList));
            mav.addObject("violateTime", LocalDateUtils.dateTimeFormat(LocalDateTime.now()));
            return mav;
        } catch (Exception e) {
            logger.error("新增车辆信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 单个/统一设置-违规车辆上传
     * @return JsonResultBean
     */
    @RequestMapping(value = "/violate/single/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertViolateUpload(@Validated @RequestBody ViolateVehicleReq vehicleReq) {
        try {
            return violateVehicleService.insertViolateUpload(vehicleReq);
        } catch (Exception e) {
            logger.error("违规车辆上传异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量(分别设置)违规车辆上传
     * @return JsonResultBean
     */
    @RequestMapping(value = "/violate/batch/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertBatchViolateUpload(
        @Validated @RequestBody List<SingleViolateVehicleReq> vehicleReqList) {
        try {
            return violateVehicleService.insertBatchViolateUpload(vehicleReqList);
        } catch (Exception e) {
            logger.error("违规车辆上传异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 违规车辆上传记录列表
     * @return PageGridBean
     */
    @RequestMapping(value = "/violate/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean listViolateVehicle(ViolateVehiclePageReq violateVehiclePageReq) {
        try {
            if (violateVehiclePageReq == null) {
                return new PageGridBean(PageGridBean.FAULT, "查询参数不能为空!");
            }

            Page<ViolateVehicleDTO> page = violateVehicleService.listViolateVehicle(violateVehiclePageReq);
            return new PageGridBean(violateVehiclePageReq, page, true);
        } catch (Exception e) {
            logger.error("违规车辆上传记录分页查询异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 违规车辆上传记录列表导出
     * @param res res
     */
    @RequestMapping(value = "/violate/export", method = RequestMethod.POST)
    public void export(HttpServletResponse res, ViolateVehicleExportReq req) {
        try {
            ExportExcelUtil.setResponseHead(res, "违规车辆上传记录");
            violateVehicleService.exportViolateList(res, req);
        } catch (Exception e) {
            logger.error("违规车辆上传记录导出数据异常", e);
        }
    }

    @RequestMapping(value = "/organization")
    @ResponseBody
    public JsonResultBean getAloneCorpInfo(@RequestParam("orgId") String orgId) {
        try {
            return violateVehicleService.getAloneCorpInfo(orgId);
        } catch (Exception e) {
            logger.error("请求上级平台企业信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    @RequestMapping(value = "/organization/export", method = RequestMethod.POST)
    public void exportAloneCorpInfo(HttpServletResponse response, AloneCorpInfoResp corpInfo) {
        try {
            final Map<String, Object> data = new HashMap<>(1);
            data.put("corpInfo", corpInfo);
            final String path = "/file/jlProtocol/企业信息数据.xlsx";
            String fileName =
                corpInfo.getCorpName() + "(企业信息)" + (corpInfo.getSendTime().replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel(path, response, data, fileName);
        } catch (Exception e) {
            logger.error("吉林导出企业信息异常", e);
        }
    }

    @RequestMapping(value = "/query/vehicle/info", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean queryVehicleInfo(QueryAloneVehicleInfo info) {
        try {
            return queryVehicleInfo.query(info);
        } catch (Exception e) {
            logger.error("请求车辆信息数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出车辆信息数据
     * @param info
     * @param res
     */
    @RequestMapping(value = "/export/vehicle/info", method = RequestMethod.POST)
    public void exportVehicleInfo(AloneVehicleInfoResp info, HttpServletResponse res) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("info", info);
            String fileName =
                info.getVehicleNo() + "(车辆信息)" + info.getSendTime().trim().replaceAll(":", "").replaceAll(" ", "-");
            templateExportExcel.templateExportExcel("/file/jlProtocol/车辆信息数据.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("导出车辆信息数据异常", e);
        }
    }

    /**
     * 平台名称列表
     */
    @RequestMapping(value = "/809/dataInteractiveManage/platformNameList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getPlatformNameList() {
        try {
            return new JsonResultBean(jiLinVehicleService
                .get809ConnectionParamSetsByProtocolType(Integer.valueOf(T809_JI_LIN_PROTOCOL_809_2013)));
        } catch (Exception e) {
            logger.error("获取平台名称列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 809平台数据交互管理监树
     * @param id         809设置id
     * @param queryType  模糊查询查询类型 vehicle:车辆; organization:企业; assignment:分组
     * @param queryParam 模糊查询参数
     */
    @RequestMapping(value = "/809/dataInteractiveManage/tree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTree(@Valid @NotBlank String id, String queryType, String queryParam) {
        try {
            // 压缩数据
            Object result = ZipUtil.compress(jiLinVehicleService.getTree(id, queryType, queryParam).toJSONString());
            return new JsonResultBean(result);
        } catch (Exception e) {
            logger.error("获取809平台数据交互管理监树异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 车辆设置列表
     * queryType        列表类型 1:违规车辆; 2:报警车辆
     * vehicleIds       车辆id 逗号分隔
     * simpleQueryParam 模糊查询
     */
    @RequestMapping(value = "/809/dataInteractiveManage/vehicleSetList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getVehicleSetList(@Valid JiLinVehicleSetListQuery query) {
        try {
            return new PageGridBean(query, jiLinVehicleService.getVehicleSetList(query), true);
        } catch (Exception e) {
            logger.error("获取车辆设置列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 平台考核数据
     * @param info 查询条件
     * @return 平台考核数据
     */
    @RequestMapping(value = "/platform/dataReleased", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean dataReleased(QueryPlatformCheckInfo info) {
        try {
            return platformCheckService.dataReleased(info);
        } catch (Exception e) {
            logger.error("获取平台考核数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出平台考核数据
     */
    @RequestMapping(value = "/platform/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            PlatformCheckInfoResp platformCheckInfoResp = platformCheckService.exportPlatformCheckInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("platformCheckInfo", platformCheckInfoResp);
            String fileName = platformCheckInfoResp.getPlatformId() + "(平台考核)" + (platformCheckInfoResp.getSendTime()
                .replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel("/file/jlProtocol/平台考核数据.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("导出平台考核数据异常", e);
        }

    }

    /**
     * 企业考核数据
     * @param info 查询条件
     * @return 企业考核数据
     */
    @RequestMapping(value = "/corpCheck/dataReleased", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean corpCheckDataReleased(CorpCheckReq info) {
        try {
            return corpCheckService.corpCheckDataReleased(info);
        } catch (Exception e) {
            logger.error("获取企业考核数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 企业考核数据导出
     */
    @RequestMapping(value = "/corpCheck/export", method = RequestMethod.GET)
    public void exportCorpCheckData(HttpServletResponse res) {
        try {
            CorpCheckInfoResp corpCheckInfoResp = corpCheckService.exportCorpCheckInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("corpCheckInfo", corpCheckInfoResp);
            String fileName =
                corpCheckInfoResp.getCorpName() + "(企业考核)" + (corpCheckInfoResp.getSendTime().replaceAll(" ", "-")
                    .replaceAll(":", ""));
            templateExportExcel.templateExportExcel("/file/jlProtocol/企业考核数据.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("导出企业考核数据异常", e);
        }

    }

    /**
     * 企业车辆违规考核数据
     * @param info 查询条件
     * @return 企业车辆违规考核数据
     */
    @RequestMapping(value = "/corpAlarmCheck/dataReleased", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean corpAlarmCheckDataReleased(CorpCheckReq info) {
        try {
            return corpAlarmCheckService.corpAlarmCheckDataReleased(info);
        } catch (Exception e) {
            logger.error("获取企业车辆违规考核数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 企业车辆违规考核数据导出
     */
    @RequestMapping(value = "/corpAlarmCheck/export", method = RequestMethod.GET)
    public void exportCorpAlarmCheckData(HttpServletResponse res) {
        try {
            CorpAlarmCheckInfoResp corpAlarmCheckInfoResp = corpAlarmCheckService.exportCorpAlarmCheckInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("info", corpAlarmCheckInfoResp);
            String fileName =
                corpAlarmCheckInfoResp.getCorpName() + "(企业车辆违规考核)" + (corpAlarmCheckInfoResp.getSendTime()
                    .replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel("/file/jlProtocol/企业车辆违规考核数据.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("导出企业车辆违规考核数据异常", e);
        }

    }

    /**
     * 单个/统一设置-报警车辆上传
     */
    @RequestMapping(value = "/alarm/single/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertAlarmUpload(@Valid @RequestBody AlarmVehicleReq vehicleReq) {
        try {
            return jiLinAlarmVehicleDataService.insertAlarmUpload(vehicleReq);
        } catch (Exception e) {
            logger.error("报警车辆上传异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量(分别设置)报警车辆上传
     */
    @RequestMapping(value = "/alarm/batch/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertBatchAlarmUpload(@RequestBody @Validated List<SingleAlarmVehicleReq> vehicleReqList) {
        try {
            return jiLinAlarmVehicleDataService.insertBatchAlarmUpload(vehicleReqList);
        } catch (Exception e) {
            logger.error("报警车辆上传异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 报警车辆上传记录列表
     */
    @RequestMapping(value = "/alarm/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean listAlarmVehicle(AlarmVehiclePageReq alarmVehiclePageReq) {
        try {
            return new PageGridBean(alarmVehiclePageReq,
                jiLinAlarmVehicleDataService.listAlarmVehicle(alarmVehiclePageReq), true);
        } catch (Exception e) {
            logger.error("报警车辆上传记录列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 报警车辆上传记录列表导出
     */
    @RequestMapping(value = "/alarm/export", method = RequestMethod.POST)
    public void exportAlarm(HttpServletResponse res, AlarmVehiclePageReq alarmVehiclePageReq) {
        try {
            ExportExcelUtil.setResponseHead(res, "报警车辆上传记录");
            jiLinAlarmVehicleDataService.exportAlarmList(res, alarmVehiclePageReq);
        } catch (Exception e) {
            logger.error("报警车辆上传记录导出数据异常", e);
        }
    }

    /**
     * 运营状态数据列表
     */
    @RequestMapping(value = "/operationStatus/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean listOperationStatus(@Valid @NotBlank String vehicleId) {
        try {
            return jiLinVehicleOperationStatusService.listOperationStatus(vehicleId);
        } catch (Exception e) {
            logger.error("获取运营状态数据列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 运营状态数据列表导出
     */
    @RequestMapping(value = "/operationStatus/export", method = RequestMethod.POST)
    public void exportOperationStatus(HttpServletResponse response, ExportOperationStatusReq req) {
        try {
            final Map<String, Object> data = new HashMap<>(1);
            data.put("operationStatusInfo", req);
            final String path = "/file/jlProtocol/车辆运营状态数据.xlsx";
            String fileName =
                req.getVehicleNo() + "(车辆运营状态)" + (req.getReturnTimeStr().replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel(path, response, data, fileName);
        } catch (Exception e) {
            logger.error("运营状态数据列表导出异常", e);
        }
    }

}
