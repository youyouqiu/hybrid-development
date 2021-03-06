package com.zw.platform.basic.service.impl;

import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.ErrorMsg;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.BaseKvtDo;
import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.domain.SimCardInfoDo;
import com.zw.platform.basic.domain.SimCardListDO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.F3SimCardDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.SimCardListDTO;
import com.zw.platform.basic.dto.export.SimCardExportDTO;
import com.zw.platform.basic.dto.imports.SimCardImportDTO;
import com.zw.platform.basic.dto.query.SimCardQuery;
import com.zw.platform.basic.helper.SimCardImportHelper;
import com.zw.platform.basic.imports.handler.SimCardImportHandler;
import com.zw.platform.basic.rediscache.SimCardRedisCache;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.DbUtils;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.SendSimCard;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.annotation.ExcelImportHelper;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.lock.ImportLock;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import com.zw.ws.entity.t808.simcard.SimCardParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/4 16:00
 */
@Service
@Order(4)
public class SimCardServiceImpl implements SimCardService, CacheService, IpAddressService {
    private static final Logger log = LogManager.getLogger(SimCardServiceImpl.class);
    @Autowired
    private SimCardNewDao simCardNewDao;
    @Autowired
    private UserService userService;

    @Autowired
    private ParamSendingCache paramSendingCache;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private LogSearchService logSearchService;
    @Value("${sim.number.bound}")
    private String simNumberBound;
    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private MonitorService monitorService;
    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public void initCache() {
        log.info("????????????sim????????????redis?????????.");
        //??????sim???????????????
        List<String> sortIds = simCardNewDao.getSortList();
        //??????SIM?????????????????????
        SimCardRedisCache.clearCache();
        if (CollectionUtils.isEmpty(sortIds)) {
            return;
        }

        //???????????????SIM?????????
        List<SimCardDTO> simCards = simCardNewDao.getByNumbers(null);
        SimCardRedisCache.initCache(sortIds, simCards);
        log.info("??????sim????????????redis?????????.");
    }

    @Override
    public boolean add(SimCardDTO simCardDTO) throws BusinessException {
        isRepeatSimCardNumber(simCardDTO.getSimcardNumber(), null);
        SimCardDO simCardDO = SimCardDO.getAddInstance(simCardDTO, SystemHelper.getCurrentUsername());
        //???????????????
        simCardNewDao.add(simCardDO);
        //????????????
        SimCardRedisCache.addSimCardCache(simCardDTO);
        //????????????
        addLog(simCardDTO);
        return true;
    }

    private void addLog(SimCardDTO simCardDTO) {
        String groupName = organizationService.getOrgNameByUuid(simCardDTO.getOrgId());
        String msg = String.format("????????????????????? : %s ( @%s)", simCardDTO.getSimcardNumber(), groupName);
        logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
    }

    @Override
    public boolean updateNumber(SimCardDTO simCardDTO) throws BusinessException {
        isRepeatSimCardNumber(simCardDTO.getSimcardNumber(), simCardDTO.getId());
        SimCardDO simCardDO = SimCardDO.getUpdateInstance(simCardDTO, SystemHelper.getCurrentUsername());
        // ????????????sim?????????
        SimCardInfoDo beforeSimCard = simCardNewDao.getById(simCardDTO.getId());
        simCardNewDao.updateSimCard(simCardDO);
        //???????????????sim???????????????id
        String bindMonitorId = simCardNewDao.getBindMonitorId(simCardDO.getId());

        //???????????????????????????????????????????????????
        SimCardRedisCache.updateSimCardCache(simCardDTO, bindMonitorId, beforeSimCard);
        //????????????
        addUpdateLog(simCardDTO);
        return true;
    }

    private void addUpdateLog(SimCardDTO simCardDTO) {
        String orgName = organizationService.getOrgNameByUuid(simCardDTO.getOrgId());
        String updateLog = String.format("????????????????????? : %s ( @%s )", simCardDTO.getDeviceNumber(), orgName);
        logSearchService.addLog(getIpAddress(), updateLog, "3", "", "-", "");
    }

    @Override
    public boolean delete(String id) throws BusinessException {

        if (simCardNewDao.getBindMonitorId(id) != null) {
            throw new BusinessException(simNumberBound);
        }
        SimCardInfoDo simCardInfoDo = simCardNewDao.getById(id);
        simCardNewDao.deleteById(id);
        SimCardRedisCache.deleteCache(id, simCardInfoDo);
        addDeleteLog(simCardInfoDo);
        return true;
    }

    private void addDeleteLog(SimCardInfoDo simCardInfoDo) {
        String orgName = organizationService.getOrgNameByUuid(simCardInfoDo.getOrgId());
        String message = String.format("????????????????????????%s ( @%s)", simCardInfoDo.getSimCardNumber(), orgName);
        logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
    }

    @Override
    public Page<SimCardListDTO> getListByKeyWord(SimCardQuery simCardQuery) {
        Page<SimCardListDTO> result;

        List<String> finalSimCardId = RedisHelper.getList(RedisKeyEnum.SIM_CARD_SORT_LIST.of());

        //???????????????
        simCardQuery.paramInit();
        //??????????????????????????????????????????
        List<String> userOrgListId = userService.getCurrentUserOrgIds();
        if (StrUtil.isNotBlank(simCardQuery.getOrgId())) {
            userOrgListId.retainAll(Collections.singleton(simCardQuery.getOrgId()));
        }
        List<RedisKey> orgSimCardKeys =
            userOrgListId.stream().map(RedisKeyEnum.ORG_SIM_CARD::of).collect(Collectors.toList());
        finalSimCardId.retainAll(RedisHelper.batchGetSet(orgSimCardKeys));
        //?????????????????????????????????
        if (simCardQuery.containsFuzzyQuery()) {
            Set<String> fuzzySimCardSet = new HashSet<>(simCardNewDao.findReal(simCardQuery.getSimpleQueryParam()));
            fuzzySimCardSet.addAll(FuzzySearchUtil.getFuzzySearchSimCardId(simCardQuery.getSimpleQueryParam()));
            finalSimCardId.retainAll(fuzzySimCardSet);
        }
        List<SimCardListDTO> deviceListDTOList = getSimCardList(simCardQuery, finalSimCardId);
        result = RedisQueryUtil.getListToPage(deviceListDTOList, simCardQuery, finalSimCardId.size());
        return result;
    }

    private List<SimCardListDTO> getSimCardList(SimCardQuery simCardQuery, List<String> finalSimCard) {
        Map<String, String> userOrgNameMap = userService.getCurrentUserOrgIdOrgNameMap();
        List<SimCardListDTO> simCardList = new ArrayList<>();
        // ?????????????????????sim???ID?????????Redis????????????
        RedisKey exportKey = RedisKeyEnum.USER_SIM_CARD_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        RedisHelper.addToList(exportKey, finalSimCard);

        //???????????????????????????
        List<String> simCardIds = RedisQueryUtil.getPageListIds(finalSimCard, simCardQuery);
        if (CollectionUtils.isEmpty(simCardIds)) {
            return simCardList;
        }
        List<SimCardListDO> simCardListDoList =
            DbUtils.partitionSortQuery(simCardIds, simCardNewDao::getSimCardList, SimCardListDO::getId);
        Map<String, BaseKvtDo<String, String, Integer>> sendStatusMap =
            simCardNewDao.findSendStatusMapByIds(simCardIds);
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = getMonitorIdNameMap(simCardListDoList);
        for (SimCardListDO listDO : simCardListDoList) {
            simCardList.add(SimCardListDTO.buildList(listDO, monitorIdNameMap, userOrgNameMap, sendStatusMap));
        }
        return simCardList;
    }

    //??????????????????
    private Map<String, BaseKvDo<String, String>> getMonitorIdNameMap(List<SimCardListDO> simCardListDoList) {
        Set<String> monitorIds =
            simCardListDoList.stream().filter(e -> e.getMonitorId() != null).map(SimCardListDO::getMonitorId)
                .collect(Collectors.toSet());
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = new HashMap<>();
        if (CollectionUtils.isEmpty(monitorIds)) {
            return monitorIdNameMap;
        }
        monitorIdNameMap = monitorService.getMonitorIdNameMap(monitorIds, null);
        return monitorIdNameMap;
    }

    @Override
    public SimCardDTO getDefaultInfo(ConfigDTO bindDTO) {
        SimCardDTO simCardDTO = new SimCardDTO();
        simCardDTO.setRealId(bindDTO.getRealSimCardNumber());
        simCardDTO.setSimcardNumber(bindDTO.getSimCardNumber());
        if (StringUtils.isBlank(bindDTO.getSimCardOrgId())) {
            OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
            simCardDTO.setOrgId(currentUserOrg.getUuid());
        } else {
            simCardDTO.setOrgId(bindDTO.getSimCardOrgId());
        }
        simCardDTO.setIsStart(Objects.isNull(bindDTO.getSimIsStart()) ? 1 : bindDTO.getSimIsStart());
        simCardDTO.setFlag(1);
        simCardDTO.setOperator(Objects.isNull(bindDTO.getOperator()) ? "????????????" : bindDTO.getOperator());
        simCardDTO.setMonthlyStatement("01");
        simCardDTO.setCorrectionCoefficient("100");
        simCardDTO.setForewarningCoefficient("90");
        simCardDTO.setIccid(bindDTO.getIccid());
        simCardDTO.setImsi(bindDTO.getImsi());
        simCardDTO.setSimFlow(bindDTO.getSimFlow());
        simCardDTO.setEndTime(bindDTO.getSimEndTime());
        return simCardDTO;
    }

    @Override
    public SimCardDTO getByNumber(String number) {
        SimCardDTO simCardDTO = simCardNewDao.getByNumber(number);
        if (Objects.isNull(simCardDTO)) {
            return null;
        }
        simCardDTO.setOrgName(organizationService.getOrgNameByUuid(simCardDTO.getOrgId()));
        return simCardDTO;
    }

    @Override
    public boolean updateNumber(String id, String number, String realNum) {
        return simCardNewDao.updateNumber(id, number, realNum);
    }

    @Override
    public boolean checkIsExist(String number, String id) {
        return simCardNewDao.getNoRepeatNumber(number, id) != null;
    }

    @Override
    public SimCardDTO getById(String id) {
        SimCardInfoDo simCardInfoDo = simCardNewDao.getById(id);
        String orgName = organizationService.getOrgNameByUuid(simCardInfoDo.getOrgId());
        return SimCardDTO.getInfo(simCardInfoDo, orgName);
    }

    @Override
    @MethodLog(name = "????????????", description = "????????????")
    @ImportLock(value = ImportModule.SIM_CARD)
    public JsonResultBean importData(MultipartFile multipartFile) throws Exception {

        ExcelImportHelper excelImportHelper = new SimCardImportHelper(simCardNewDao, new ImportExcel(multipartFile));
        //????????? excel ????????? list
        excelImportHelper.init(SimCardImportDTO.class);

        excelImportHelper.validate(userService.getCurrentUserOrgNameOrgIdMap());
        List<SimCardImportDTO> list = excelImportHelper.getExcelData();

        // ????????????
        final SimCardImportHandler handler = new SimCardImportHandler(simCardNewDao, excelImportHelper);
        try (ImportCache ignored = new ImportCache(ImportModule.SIM_CARD, SystemHelper.getCurrentUsername(), handler)) {
            final JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                ImportErrorUtil.putDataToRedis(list, ImportModule.SIM_CARD);
                return jsonResultBean;
            }
        }
        addImportLog(list);
        return new JsonResultBean(true, String.format("??????????????? ????????????%d?????????<br/>", list.size()));
    }

    private void addImportLog(List<SimCardImportDTO> list) {
        StringBuilder message = new StringBuilder();
        String logTemplate = "????????????????????? : %s ( @%s ) <br/>";
        for (SimCardImportDTO device : list) {
            message.append(String.format(logTemplate, device.getSimCardNumber(), device.getOrgName()));
        }

        // ????????????
        if (!message.toString().isEmpty()) {
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "???????????????????????????");
        }
    }

    private void isRepeatSimCardNumber(String simCardNumber, String id) throws BusinessException {
        if (simCardNewDao.getNoRepeatNumber(simCardNumber, id) != null) {
            throw new BusinessException(ErrorMsg.SIM_CARD_EXIST.getMsg());
        }
    }

    @Override
    public boolean checkIsBind(String number) {
        return simCardNewDao.getMonitorIdByNumber(number) != null;
    }

    @Override
    public void exportSimCard() throws Exception {
        Map<String, String> userOrgMap = userService.getCurrentUserOrgIdOrgNameMap();
        RedisKey exportKey = RedisKeyEnum.USER_SIM_CARD_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        List<String> simCardIds = RedisHelper.getList(exportKey);
        List<SimCardListDO> simCardList =
                DbUtils.partitionSortQuery(simCardIds, simCardNewDao::getSimCardList, SimCardListDO::getId);
        Map<String, BaseKvDo<String, String>> monitorIdNameMap = getMonitorIdNameMap(simCardList);
        List<SimCardExportDTO> exportDTOS = new ArrayList<>();
        //?????????????????????????????????
        for (SimCardListDO simCardListDO : simCardList) {
            exportDTOS.add(SimCardExportDTO.build(simCardListDO, userOrgMap, monitorIdNameMap));
        }
        HttpServletResponse response = getResponse();
        try (OutputStream out = response.getOutputStream()) {
            ExportExcel export = new ExportExcel(null, SimCardExportDTO.class, 1);
            export.setDataList(exportDTOS);
            // ????????????????????????????????????
            export.write(out);
        }
    }

    @Override
    public boolean deleteBatch(List<String> simCardIds) throws BusinessException {
        List<String> bindMonitorIds = simCardNewDao.getBindMonitorIds(simCardIds);
        if (CollectionUtils.isNotEmpty(bindMonitorIds)) {
            throw new BusinessException(simNumberBound);
        }
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();
        List<SimCardListDO> simCardList = simCardNewDao.getSimCardList(simCardIds);
        simCardNewDao.deleteByBatch(simCardIds);
        SimCardRedisCache.deleteSimCardsCache(simCardList);
        addDeleteMoreLog(orgMap, simCardList);
        return true;
    }

    @Override
    public Set<String> getOrgSimCardIds(String orgId) {
        return simCardNewDao.getOrgSimCardIds(orgId);
    }

    @Override
    public List<Map<String, String>> getUbBindSelectList(String keyword) {
        //??????????????????SIM?????????
        List<String> sortIds = SimCardRedisCache.getUnbind(userService.getCurrentUserOrgIds(), keyword, true);
        if (CollectionUtils.isEmpty(sortIds)) {
            return new ArrayList<>();
        }
        List<SimCardListDO> simCardList = simCardNewDao.getSimCardList(sortIds);

        //???????????????????????????
        Map<String, SimCardListDO> unbindMap = AssembleUtil.collectionToMap(simCardList, SimCardListDO::getId);
        List<Map<String, String>> unbindList = new ArrayList<>();
        for (String id : sortIds) {
            SimCardListDO simCard = unbindMap.get(id);
            if (Objects.isNull(simCard)) {
                continue;
            }
            unbindList.add(ImmutableMap.of("id", id, "simcardNumber", simCard.getSimcardNumber()));
        }
        return unbindList;
    }

    @Override
    public List<SimCardDTO> getByDeviceIds(Collection<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return new ArrayList<>();
        }
        return simCardNewDao.getByDeviceIds(deviceIds);
    }

    private void addDeleteMoreLog(Map<String, String> orgMap, List<SimCardListDO> simcardList) {
        StringBuilder message = new StringBuilder();
        String logTemplate = "??????????????????????????? : %s ( @%s ) <br/>";
        for (SimCardListDO info : simcardList) {
            message.append(String.format(logTemplate, info.getSimcardNumber(), orgMap.get(info.getOrgId())));
        }
        logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "???????????????????????????");
    }

    @Override
    public F3SimCardDTO getF3SimInfo(String id) {
        return simCardNewDao.getF3SimInfo(id);
    }

    /**
     * ??????sim?????????
     * @param response response
     * @return boolean
     * @throws Exception e
     */
    @Override
    @MethodLog(name = "??????sim?????????", description = " ??????sim?????????")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // ??????????????????????????????????????????????????????
        List<String> groupNames = userService.getCurrentUserOrgNames();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ??????
        headList.add("ICCID");
        headList.add("IMEI");
        headList.add("IMSI");
        headList.add("???????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("?????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("???????????????");
        headList.add("???????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("??????SIM??????");
        headList.add("??????");
        // ????????????
        requiredList.add("???????????????");
        requiredList.add("????????????");
        String dateString = DateFormatUtils.format(new Date(), DATE_FORMAT);
        // ????????????????????????
        exportList.add("5798663004753");
        exportList.add("??????");
        exportList.add("????????????");
        exportList.add(dateString);
        exportList.add("1024");
        exportList.add(dateString);
        //????????????
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("5798663004753");
        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<>();
        //????????????
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("????????????", groupNameArr);
        }
        // ????????????
        String[] startStatus = { "??????", "??????" };
        selectMap.put("????????????", startStatus);
        // ?????????
        String[] operator = { "????????????", "????????????", "????????????" };
        selectMap.put("?????????", operator);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        export.addCell(row, 3, exportList.get(0));
        export.addCell(row, 4, exportList.get(6));
        export.addCell(row, 5, exportList.get(1));
        export.addCell(row, 6, exportList.get(2));
        export.addCell(row, 8, exportList.get(4));
        export.addCell(row, 14, exportList.get(3));
        export.addCell(row, 15, exportList.get(5));
        export.addCell(row, 16, exportList.get(7));
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);
        out.close();

        return true;
    }

    @Override
    public JsonResultBean sendSimCard(SendSimCard sendSimCard) {
        String sid = sendSimCard.getSimId();
        String upTime = sendSimCard.getUpTime();
        String vid = sendSimCard.getVehicleId();
        String cid = sendSimCard.getParameterName();
        Integer type = sendSimCard.getType();
        if (!StringUtils.isNotBlank(sid) && !StringUtils.isNotBlank(vid) && !StringUtils.isNotBlank(cid)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        SimCardInfoDo simCardInfoDo = simCardNewDao.getById(sid);
        if (!StringUtil.isNullOrBlank(upTime)) {
            simCardInfoDo.setMonthTrafficDeadline(upTime.substring(8, 10));
        } else if (simCardInfoDo.getMonthTrafficDeadline() != null && !""
            .equals(simCardInfoDo.getMonthTrafficDeadline())) {
            simCardInfoDo.setMonthTrafficDeadline(simCardInfoDo.getMonthTrafficDeadline().substring(8, 10));
        }
        // Sim???????????????
        String paramType = "8";
        final Map<String, String> vehicleMap =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vid, "deviceId", "deviceNumber", "deviceType"));
        String deviceNumber = ObjectUtils.defaultIfNull(vehicleMap.get("deviceNumber"), "");
        String deviceId = ObjectUtils.defaultIfNull(vehicleMap.get("deviceId"), "");
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vid, deviceNumber);
        if (msgSN != null && !deviceId.isEmpty()) {
            // ??????????????????
            // ????????????
            sendSimCommand(deviceId, simCardInfoDo, msgSN, type, vehicleMap);
            // ?????????
            int status = 4;
            // ????????????
            List<String> paramIds = monitorService.findSendParmId(vid);
            if (paramIds.size() != 0) {
                sendHelper.updateParameterStatus(paramIds.get(0), msgSN, status, vid, paramType, cid);
            } else {
                sendHelper.updateParameterStatus("", msgSN, status, vid, paramType, cid);
            }
            return new JsonResultBean(true, String.valueOf(msgSN));
        } else {
            // ???????????????
            int status = 5;
            msgSN = 0;
            // ????????????
            List<String> paramIds = monitorService.findSendParmId(vid);
            if (paramIds.size() != 0) {
                sendHelper.updateParameterStatus(paramIds.get(0), msgSN, status, vid, paramType, cid);
            } else {
                sendHelper.updateParameterStatus("", msgSN, status, vid, paramType, cid);
            }
            return new JsonResultBean(false, String.valueOf(msgSN));
        }
    }

    private void sendSimCommand(String deviceId, SimCardInfoDo simCard, Integer transNo, Integer type,
        Map<String, String> vehicleMap) {
        try {
            T808_0x8103 benchmark = new T808_0x8103();
            ParamItem paramItem = new ParamItem();
            paramItem.setParamLength(31);
            SimCardParam simCardParam = new SimCardParam();
            if (simCard == null) {
                return;
            }
            if (type == 0) {
                // ????????????????????????
                simCardParam.setCorrectionCoefficient(getIntOrDefault(simCard.getCorrectionCoefficient(), 0, 1));
                simCardParam.setForewarningCoefficient(getIntOrDefault(simCard.getForewarningCoefficient(), 0, 1));
                simCardParam.setMonthThresholdValue(getIntOrDefault(simCard.getMonthThresholdValue(), 0, 10));
                simCardParam.setDayThresholdValue(getIntOrDefault(simCard.getDayThresholdValue(), 0, 10));
                simCardParam.setHourThresholdValue(getIntOrDefault(simCard.getHourThresholdValue(), 0, 10));

                simCardParam.setMonthlyStatement(getIntOrDefault(simCard.getMonthlyStatement(), 1));
                simCardParam.setMonthTrafficDeadline(getIntOrDefault(simCard.getMonthTrafficDeadline(), 0));
                simCardParam.setMonthRealValue(getIntOrDefault(simCard.getMonthRealValue(), 0, 10));
                simCardParam.setDayRealValue(getIntOrDefault(simCard.getDayRealValue(), 0, 10));
            }
            // ??????sim?????????????????????
            if (type == 1) {
                simCardParam.setCorrectionCoefficient(0xFFFF);
                simCardParam.setForewarningCoefficient(0xFFFF);
                simCardParam.setMonthThresholdValue(0xFFFFFFFF);
                simCardParam.setDayThresholdValue(0xFFFFFFFF);
                simCardParam.setHourThresholdValue(0xFFFF);
                simCardParam.setMonthlyStatement(1);
                simCardParam.setMonthTrafficDeadline(0);
                simCardParam.setSimcardNumber(0L);
                simCardParam.setMonthRealValue(0);
                simCardParam.setDayRealValue(0);
                simCardParam.setType(01);
            }
            // ???????????????SIM?????????
            if (type == 2) {
                simCardParam.setCorrectionCoefficient(getIntOrDefault(simCard.getCorrectionCoefficient(), 0, 1));
                simCardParam.setForewarningCoefficient(getIntOrDefault(simCard.getForewarningCoefficient(), 0, 1));
                simCardParam.setMonthThresholdValue(getIntOrDefault(simCard.getMonthThresholdValue(), 0, 10));
                simCardParam.setDayThresholdValue(getIntOrDefault(simCard.getDayThresholdValue(), 0, 10));
                simCardParam.setHourThresholdValue(getIntOrDefault(simCard.getHourThresholdValue(), 0, 10));
                simCardParam.setMonthlyStatement(1);
                simCardParam.setMonthTrafficDeadline(0);
                simCardParam.setSimcardNumber(0L);
                simCardParam.setMonthRealValue(0);
                simCardParam.setDayRealValue(0);
                simCardParam.setType(0);
                String logs = "??????????????????????????????" + simCard.getSimCardNumber() + " ( @" + organizationService
                    .getOrgNameByUuid(simCard.getOrgId()) + " )";
                logSearchService.addLog(getIpAddress(), logs, "2", "", "-", "");
            }
            // ??????????????????????????????
            if (type == 3) {
                simCardParam.setCorrectionCoefficient(0xFFFF);
                simCardParam.setForewarningCoefficient(0xFFFF);
                simCardParam.setMonthThresholdValue(0xFFFFFFFF);
                simCardParam.setDayThresholdValue(0xFFFFFFFF);
                simCardParam.setHourThresholdValue(0xFFFF);
                simCardParam.setMonthlyStatement(1);
                simCardParam.setMonthTrafficDeadline(0);
                simCardParam.setSimcardNumber(0L);
                simCardParam.setMonthRealValue(0);
                simCardParam.setDayRealValue(0);
                simCardParam.setType(0);
            }

            paramItem.setParamValue(simCardParam);
            paramItem.setParamId(0xF31A);
            benchmark.getParamItems().add(paramItem);
            benchmark.setParametersCount(benchmark.getParamItems().size());
            String userName = SystemHelper.getCurrentUsername();
            // ??????????????????
            SubscibeInfo info = new SubscibeInfo(userName, deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            paramSendingCache.put(userName, transNo, Objects.requireNonNull(simCard).getSimCardNumber(),
                SendTarget.getInstance(SendModule.ALARM_PARAMETER_SETTING));
            T808Message message = MsgUtil
                .get808Message(simCard.getSimCardNumber(), ConstantUtil.T808_SET_PARAM, transNo, benchmark, vehicleMap);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
        }
    }

    /**
     * ???????????????double?????????????????????int????????????
     * @param val        double???????????????
     * @param defaultVal ????????????????????????????????????
     * @param multiVal   ????????????????????????????????????
     * @return int
     */
    private int getIntOrDefault(String val, int defaultVal, int multiVal) {
        if (StrUtil.isBlank(val)) {
            return defaultVal;
        } else {
            Double data = Double.valueOf(val) * multiVal;
            return data.intValue();
        }
    }

    /**
     * ???????????????int?????????????????????int????????????
     * @param val        double???????????????
     * @param defaultVal ????????????????????????????????????
     * @return
     */
    private int getIntOrDefault(String val, int defaultVal) {
        if (StrUtil.isBlank(val)) {
            return defaultVal;
        } else {
            Double data = Double.valueOf(val);
            return data.intValue();
        }
    }

}
