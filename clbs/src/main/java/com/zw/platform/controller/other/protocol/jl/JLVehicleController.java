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
 * ????????????
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
     * ??????????????????????????????
     */
    private static final String VIOLATE_SINGLE_VEHICLE_UPLOAD_PAGE = "modules/jl/vehicle/violate/single-upload";
    /**
     * ??????????????????????????????
     */
    private static final String VIOLATE_BATCH_VEHICLE_UPLOAD_PAGE = "modules/jl/vehicle/violate/batch-upload";

    /**
     * ??????????????????
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
     * ????????????
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/stopped/upload", method = RequestMethod.GET)
    public ModelAndView getUploadStopVehicle(String monitor) {
        ModelAndView mav = new ModelAndView(UPLOAD_STOP_VEHICLE);
        mav.addObject("monitor", monitor);
        return mav;
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/stopped/uploadBatch", method = RequestMethod.GET)
    public ModelAndView getUploadStopVehicleBatch(String monitor) {
        ModelAndView mav = new ModelAndView(UPLOAD_STOP_VEHICLE_BATCH);
        mav.addObject("monitor", monitor);
        return mav;
    }

    /**
     * ??????????????????
     * @return
     */
    @RequestMapping(value = "/stopped/page", method = RequestMethod.GET)
    @ResponseBody
    public PageGridBean stoppedPage(StoppedVehicleQuery query) {
        return ControllerTemplate.getPassPageBean(() -> stoppedVehicleService.page(query), "??????????????????");
    }

    /**
     * ????????????????????????-809????????????
     * @return
     */
    @RequestMapping(value = "/stopped/plateform", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getStoppedPlatformInfo() {
        try {
            return new JsonResultBean(stoppedVehicleService.getStoppedPlatformInfo());
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @return
     */
    @RequestMapping(value = "/stopped/upload", method = RequestMethod.POST)
    @ResponseBody

    public JsonResultBean stoppedUpload(String str) {
        try {
            List<StoppedVehicleRecordDto> list = JSONObject.parseArray(str, StoppedVehicleRecordDto.class);
            return stoppedVehicleService.upload(list);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     * @return
     */
    @RequestMapping(value = "/stopped/recordPage", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean stoppedRecordPage(StoppedVehicleRecordQuery query) {
        try {
            // ?????????id????????????????????????
            if (StringUtils.isNotBlank(query.getIds())) {
                List<String> vehicleIds = Arrays.asList(query.getIds().split(","));
                query.setVehicleIds(vehicleIds);
            }
            return new PageGridBean(stoppedVehicleService.recordPage(query), true);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    @RequestMapping(value = "/export/stopped/recordPage", method = RequestMethod.POST)
    public void exportStoppedRecord(StoppedVehicleRecordQuery query, HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "????????????????????????");
            stoppedVehicleService.exportStoppedRecord(query, response);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????
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
            logger.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????????????????????????????
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
            logger.error("????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????/????????????-??????????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/violate/single/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertViolateUpload(@Validated @RequestBody ViolateVehicleReq vehicleReq) {
        try {
            return violateVehicleService.insertViolateUpload(vehicleReq);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????(????????????)??????????????????
     * @return JsonResultBean
     */
    @RequestMapping(value = "/violate/batch/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertBatchViolateUpload(
        @Validated @RequestBody List<SingleViolateVehicleReq> vehicleReqList) {
        try {
            return violateVehicleService.insertBatchViolateUpload(vehicleReqList);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????????????????
     * @return PageGridBean
     */
    @RequestMapping(value = "/violate/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean listViolateVehicle(ViolateVehiclePageReq violateVehiclePageReq) {
        try {
            if (violateVehiclePageReq == null) {
                return new PageGridBean(PageGridBean.FAULT, "????????????????????????!");
            }

            Page<ViolateVehicleDTO> page = violateVehicleService.listViolateVehicle(violateVehiclePageReq);
            return new PageGridBean(violateVehiclePageReq, page, true);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * ????????????????????????????????????
     * @param res res
     */
    @RequestMapping(value = "/violate/export", method = RequestMethod.POST)
    public void export(HttpServletResponse res, ViolateVehicleExportReq req) {
        try {
            ExportExcelUtil.setResponseHead(res, "????????????????????????");
            violateVehicleService.exportViolateList(res, req);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
        }
    }

    @RequestMapping(value = "/organization")
    @ResponseBody
    public JsonResultBean getAloneCorpInfo(@RequestParam("orgId") String orgId) {
        try {
            return violateVehicleService.getAloneCorpInfo(orgId);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    @RequestMapping(value = "/organization/export", method = RequestMethod.POST)
    public void exportAloneCorpInfo(HttpServletResponse response, AloneCorpInfoResp corpInfo) {
        try {
            final Map<String, Object> data = new HashMap<>(1);
            data.put("corpInfo", corpInfo);
            final String path = "/file/jlProtocol/??????????????????.xlsx";
            String fileName =
                corpInfo.getCorpName() + "(????????????)" + (corpInfo.getSendTime().replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel(path, response, data, fileName);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
        }
    }

    @RequestMapping(value = "/query/vehicle/info", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean queryVehicleInfo(QueryAloneVehicleInfo info) {
        try {
            return queryVehicleInfo.query(info);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????????????????
     * @param info
     * @param res
     */
    @RequestMapping(value = "/export/vehicle/info", method = RequestMethod.POST)
    public void exportVehicleInfo(AloneVehicleInfoResp info, HttpServletResponse res) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("info", info);
            String fileName =
                info.getVehicleNo() + "(????????????)" + info.getSendTime().trim().replaceAll(":", "").replaceAll(" ", "-");
            templateExportExcel.templateExportExcel("/file/jlProtocol/??????????????????.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = "/809/dataInteractiveManage/platformNameList", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getPlatformNameList() {
        try {
            return new JsonResultBean(jiLinVehicleService
                .get809ConnectionParamSetsByProtocolType(Integer.valueOf(T809_JI_LIN_PROTOCOL_809_2013)));
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 809??????????????????????????????
     * @param id         809??????id
     * @param queryType  ???????????????????????? vehicle:??????; organization:??????; assignment:??????
     * @param queryParam ??????????????????
     */
    @RequestMapping(value = "/809/dataInteractiveManage/tree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTree(@Valid @NotBlank String id, String queryType, String queryParam) {
        try {
            // ????????????
            Object result = ZipUtil.compress(jiLinVehicleService.getTree(id, queryType, queryParam).toJSONString());
            return new JsonResultBean(result);
        } catch (Exception e) {
            logger.error("??????809????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * queryType        ???????????? 1:????????????; 2:????????????
     * vehicleIds       ??????id ????????????
     * simpleQueryParam ????????????
     */
    @RequestMapping(value = "/809/dataInteractiveManage/vehicleSetList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getVehicleSetList(@Valid JiLinVehicleSetListQuery query) {
        try {
            return new PageGridBean(query, jiLinVehicleService.getVehicleSetList(query), true);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * ??????????????????
     * @param info ????????????
     * @return ??????????????????
     */
    @RequestMapping(value = "/platform/dataReleased", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean dataReleased(QueryPlatformCheckInfo info) {
        try {
            return platformCheckService.dataReleased(info);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/platform/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            PlatformCheckInfoResp platformCheckInfoResp = platformCheckService.exportPlatformCheckInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("platformCheckInfo", platformCheckInfoResp);
            String fileName = platformCheckInfoResp.getPlatformId() + "(????????????)" + (platformCheckInfoResp.getSendTime()
                .replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel("/file/jlProtocol/??????????????????.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
        }

    }

    /**
     * ??????????????????
     * @param info ????????????
     * @return ??????????????????
     */
    @RequestMapping(value = "/corpCheck/dataReleased", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean corpCheckDataReleased(CorpCheckReq info) {
        try {
            return corpCheckService.corpCheckDataReleased(info);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/corpCheck/export", method = RequestMethod.GET)
    public void exportCorpCheckData(HttpServletResponse res) {
        try {
            CorpCheckInfoResp corpCheckInfoResp = corpCheckService.exportCorpCheckInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("corpCheckInfo", corpCheckInfoResp);
            String fileName =
                corpCheckInfoResp.getCorpName() + "(????????????)" + (corpCheckInfoResp.getSendTime().replaceAll(" ", "-")
                    .replaceAll(":", ""));
            templateExportExcel.templateExportExcel("/file/jlProtocol/??????????????????.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
        }

    }

    /**
     * ??????????????????????????????
     * @param info ????????????
     * @return ??????????????????????????????
     */
    @RequestMapping(value = "/corpAlarmCheck/dataReleased", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean corpAlarmCheckDataReleased(CorpCheckReq info) {
        try {
            return corpAlarmCheckService.corpAlarmCheckDataReleased(info);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(value = "/corpAlarmCheck/export", method = RequestMethod.GET)
    public void exportCorpAlarmCheckData(HttpServletResponse res) {
        try {
            CorpAlarmCheckInfoResp corpAlarmCheckInfoResp = corpAlarmCheckService.exportCorpAlarmCheckInfo();
            Map<String, Object> data = new HashMap<>();
            data.put("info", corpAlarmCheckInfoResp);
            String fileName =
                corpAlarmCheckInfoResp.getCorpName() + "(????????????????????????)" + (corpAlarmCheckInfoResp.getSendTime()
                    .replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel("/file/jlProtocol/??????????????????????????????.xlsx", res, data, fileName);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
        }

    }

    /**
     * ??????/????????????-??????????????????
     */
    @RequestMapping(value = "/alarm/single/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertAlarmUpload(@Valid @RequestBody AlarmVehicleReq vehicleReq) {
        try {
            return jiLinAlarmVehicleDataService.insertAlarmUpload(vehicleReq);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????(????????????)??????????????????
     */
    @RequestMapping(value = "/alarm/batch/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean insertBatchAlarmUpload(@RequestBody @Validated List<SingleAlarmVehicleReq> vehicleReqList) {
        try {
            return jiLinAlarmVehicleDataService.insertBatchAlarmUpload(vehicleReqList);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????????????????
     */
    @RequestMapping(value = "/alarm/page", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean listAlarmVehicle(AlarmVehiclePageReq alarmVehiclePageReq) {
        try {
            return new PageGridBean(alarmVehiclePageReq,
                jiLinAlarmVehicleDataService.listAlarmVehicle(alarmVehiclePageReq), true);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(value = "/alarm/export", method = RequestMethod.POST)
    public void exportAlarm(HttpServletResponse res, AlarmVehiclePageReq alarmVehiclePageReq) {
        try {
            ExportExcelUtil.setResponseHead(res, "????????????????????????");
            jiLinAlarmVehicleDataService.exportAlarmList(res, alarmVehiclePageReq);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
        }
    }

    /**
     * ????????????????????????
     */
    @RequestMapping(value = "/operationStatus/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean listOperationStatus(@Valid @NotBlank String vehicleId) {
        try {
            return jiLinVehicleOperationStatusService.listOperationStatus(vehicleId);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????????????????
     */
    @RequestMapping(value = "/operationStatus/export", method = RequestMethod.POST)
    public void exportOperationStatus(HttpServletResponse response, ExportOperationStatusReq req) {
        try {
            final Map<String, Object> data = new HashMap<>(1);
            data.put("operationStatusInfo", req);
            final String path = "/file/jlProtocol/????????????????????????.xlsx";
            String fileName =
                req.getVehicleNo() + "(??????????????????)" + (req.getReturnTimeStr().replaceAll(" ", "-").replaceAll(":", ""));
            templateExportExcel.templateExportExcel(path, response, data, fileName);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
        }
    }

}
