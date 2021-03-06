package com.zw.platform.service.oilsubsidy.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.connectionparamsset_809.T809PlatFormSubscribe;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.DownLoadStatus;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.FailReason;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.MatchStatus;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlForm;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilDownloadUrlQuery;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleForm;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleInfo;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleQuery;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilPlatData;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilRequestCode;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilVehicleInfo;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.oilsubsidy.LineManageDao;
import com.zw.platform.repository.vas.ForwardVehicleManageDao;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.oilsubsidy.ForwardVehicleManageService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CipherEnDecodeUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/30 10:10
 */
@Service
public class ForwardVehicleManageServiceImpl implements ForwardVehicleManageService {

    private static Logger logger = LogManager.getLogger(ForwardVehicleManageServiceImpl.class);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private ForwardVehicleManageDao forwardVehicleManageDao;

    @Autowired

    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private ConnectionParamsConfigDao connectionParamsConfigDao;

    @Autowired
    private LineManageDao lineManageDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    ServerParamList serverParamList;

    /**
     * ????????????
     */
    private static final int OIL_SUBSIDY_PROTOCOL = 1603;

    private static final String SUCCESS = "??????";

    private static final String FAIL = "??????";

    private static final String OIL_WSDL_REQUEST_PARAM =
        "<Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\">\n" + "    <Body>\n"
            + "        <getBusesInfo xmlns=\"http://service.ws.gjds.tiamaes.com/\">\n"
            + "            <arg0 xmlns=\"\">%s</arg0>\n" + "            <arg1 xmlns=\"\">%s</arg1>\n"
            + "            <arg2 xmlns=\"\">%s</arg2>\n" + "        </getBusesInfo>\n" + "    </Body>\n"
            + "</Envelope>";

    @Override
    public boolean add(OilDownloadUrlForm oilDownloadUrl, HttpServletRequest request) throws BusinessException {

        //???????????????
        if (isRepeatDockingCode(null, oilDownloadUrl.getDockingCode())) {
            throw new BusinessException("???????????????????????????????????????????????????");
        }
        oilDownloadUrl.setCreateDataUsername(SystemHelper.getCurrentUsername());

        //??????????????????
        addLog(request, "??????????????????????????????????????????%s???", oilDownloadUrl.getDockingCode());
        return forwardVehicleManageDao.add(oilDownloadUrl) == 1;
    }

    private boolean isRepeatDockingCode(String id, String dockingCode) {
        return forwardVehicleManageDao.getIdByDockingCode(id, dockingCode) != null;
    }

    @Override
    public boolean update(OilDownloadUrlForm oilDownloadUrl, HttpServletRequest request) throws BusinessException {

        OilDownloadUrlInfo oilDownloadUrlInfo = forwardVehicleManageDao.findById(oilDownloadUrl.getId());
        String forwardVehicle =
            forwardVehicleManageDao.getForwardVehicleByDockingCode(oilDownloadUrlInfo.getDockingCode());
        if (forwardVehicle != null) {
            throw new BusinessException("???????????????????????????????????????????????????????????????????????????");
        }
        //???????????????
        if (isRepeatDockingCode(oilDownloadUrl.getId(), oilDownloadUrl.getDockingCode())) {
            throw new BusinessException("???????????????????????????????????????????????????");
        }
        oilDownloadUrl.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        //??????????????????
        addLog(request, "??????????????????????????????????????????%s???", oilDownloadUrl.getDockingCode());
        return forwardVehicleManageDao.update(oilDownloadUrl) == 1;
    }

    @Override
    public boolean delete(String id, HttpServletRequest request) {
        OilDownloadUrlInfo oilDownloadUrlInfo = forwardVehicleManageDao.findById(id);
        List<String> vehicleIds =
            forwardVehicleManageDao.getForwardVehicleIdByDockingCode(oilDownloadUrlInfo.getDockingCode());

        //????????????id??????????????????id,?????????809???????????????????????????
        delete809Vehicle(vehicleIds);
        //???????????????????????????
        forwardVehicleManageDao.deleteForwardVehicleByDockingCode(oilDownloadUrlInfo.getDockingCode());
        //??????????????????
        addLog(request, "??????????????????????????????????????????%s???", oilDownloadUrlInfo.getDockingCode());
        return forwardVehicleManageDao.delete(id) == 1;
    }

    @Override
    public List<OilDownloadUrlInfo> queryInfos(OilDownloadUrlQuery query) {
        List<String> userOrgIds = userService.fuzzSearchUserOrgIdsByOrgName(query.getDockingCodeOrgName());
        List<OilDownloadUrlInfo> oilDownloadUrlInfos = forwardVehicleManageDao.queryInfos(userOrgIds);
        Set<String> orgIds = new HashSet<>();
        Set<String> platIds = new HashSet<>();
        for (OilDownloadUrlInfo oilDownloadUrlInfo : oilDownloadUrlInfos) {
            orgIds.add(oilDownloadUrlInfo.getDockingCodeOrgId());
            platIds.add(oilDownloadUrlInfo.getForwardingPlatformId());
        }
        Map<String, OrganizationLdap> orgInfos = organizationService.getOrgByUuids(orgIds);
        Map<String, String> platIdNameMap = getPlatIdNameMap(platIds);
        for (OilDownloadUrlInfo info : oilDownloadUrlInfos) {
            OrganizationLdap orgInfo = orgInfos.get(info.getDockingCodeOrgId());
            if (orgInfo != null) {
                info.setDockingCodeOrg(orgInfo.getName());
            }
            info.setForwardingPlatform(platIdNameMap.get(info.getForwardingPlatformId()));
            if (info.getDownloadTime() != null) {
                info.setDownloadTimeStr(DateUtil.formatDate(info.getDownloadTime(), DateUtil.DATE_FORMAT_SHORT));
            }

        }

        return oilDownloadUrlInfos;
    }

    private Map<String, String> getPlatIdNameMap(Set<String> platIds) {
        if (CollectionUtils.isEmpty(platIds)) {
            return new HashMap<>();
        }
        List<PlantParam> plats = connectionParamsSetDao.get809ConnectionParamsByIds(platIds);
        return plats.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getPlatformName()));
    }

    @Override
    public OilDownloadUrlInfo findById(String id) {
        OilDownloadUrlInfo urlInfo = forwardVehicleManageDao.findById(id);
        String orgId = urlInfo.getDockingCodeOrgId();
        String orgName = organizationService.getOrgNameByUuid(orgId);
        if (StringUtils.isNotBlank(orgName)) {
            urlInfo.setDockingCodeOrg(orgName);
        }
        Map<String, String> platIdNameMap = getPlatIdNameMap(ImmutableSet.of(urlInfo.getForwardingPlatformId()));
        urlInfo.setForwardingPlatform(platIdNameMap.get(urlInfo.getForwardingPlatformId()));
        return urlInfo;

    }

    @Override
    public List<Map<String, String>> findOilSubsidyPlat(String orgId) {

        List<Map<String, String>> result = new ArrayList<>();
        List<String> orgIds = userService.getCurrentUserOrgIds();
        if (!StringUtil.isNullOrBlank(orgId) && orgIds.contains(orgId)) {
            orgIds = Collections.singletonList(orgId);
        }
        List<PlantParam> plantParams =
            connectionParamsSetDao.get809ByProtocolTypeAndOrgId(OIL_SUBSIDY_PROTOCOL, orgIds);
        for (PlantParam plantParam : plantParams) {
            result.add(ImmutableMap.of("name", plantParam.getPlatformName(), "id", plantParam.getId()));
        }
        return result;
    }

    @Override
    public boolean canEdit(String id) {
        OilDownloadUrlInfo oilDownloadUrlInfo = forwardVehicleManageDao.findById(id);
        return forwardVehicleManageDao.getForwardVehicleByDockingCode(oilDownloadUrlInfo.getDockingCode()) == null;
    }

    /**
     * ??????????????????????????????
     * @param query
     * @return
     */
    @Override
    public Page<OilForwardVehicleInfo> queryVehicleInfos(OilForwardVehicleQuery query) {
        query.setDockingCodeOrgIds(userService.getCurrentUserOrgIds());
        if (StringUtils.isNotEmpty(query.getSearchParam())) {
            if (query.getSearchParam().contains(SUCCESS)) {
                query.setMatchStatus(1);
            }
            if (query.getSearchParam().contains(FAIL)) {
                query.setMatchStatus(0);
            }
            query.setSearchDockingCodeOrgIds(userService.fuzzSearchUserOrgIdsByOrgName(query.getSearchParam()));
        }

        Page<OilForwardVehicleInfo> oilForwardVehicleInfos =
                PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                        .doSelectPage(() -> forwardVehicleManageDao.queryVehicleInfos(query));

        if (oilForwardVehicleInfos.size() == 0) {
            return oilForwardVehicleInfos;
        }

        Set<String> orgIds = new HashSet<>();
        Set<String> platIds = new HashSet<>();
        Set<String> lineIds = new HashSet<>();
        for (OilForwardVehicleInfo oilForwardVehicleInfo : oilForwardVehicleInfos) {
            orgIds.add(oilForwardVehicleInfo.getDockingCodeOrgId());
            orgIds.add(oilForwardVehicleInfo.getVehicleOrgId());
            platIds.add(oilForwardVehicleInfo.getForwardingPlatformId());
            lineIds.add(oilForwardVehicleInfo.getLineId());
        }
        Map<String, OrganizationLdap> orgInfos = organizationService.getOrgByUuids(orgIds);
        Map<String, String> platIdNameMap = getPlatIdNameMap(platIds);

        Map<String, Map<String, Object>> lineNameMap = lineManageDao.getListMapByIds(lineIds);

        for (OilForwardVehicleInfo oilForwardVehicleInfo : oilForwardVehicleInfos) {
            oilForwardVehicleInfo.setPlateColor(PlateColor.getNameOrBlankByCode(oilForwardVehicleInfo.getPlateColor()));
            oilForwardVehicleInfo.setVehicleStatusStr(oilForwardVehicleInfo.getVehicleStatus());
            oilForwardVehicleInfo.setFailedReasonStr(oilForwardVehicleInfo.getFailedReason());
            oilForwardVehicleInfo.setIndustryCategoryStr(oilForwardVehicleInfo.getIndustryCategory());
            oilForwardVehicleInfo.setMatchStatusStr(oilForwardVehicleInfo.getMatchStatus());
            oilForwardVehicleInfo.setMatchTimeStr(
                DateUtil.getDateToString(oilForwardVehicleInfo.getMatchTime(), DateUtil.DATE_FORMAT_SHORT));
            OrganizationLdap orgInfo = orgInfos.get(oilForwardVehicleInfo.getDockingCodeOrgId());
            if (orgInfo != null) {
                oilForwardVehicleInfo.setDockingCodeOrg(orgInfo.getName());
            } else {
                oilForwardVehicleInfo.setDockingCodeOrg("");
            }

            OrganizationLdap vehicleOrgInfo = orgInfos.get(oilForwardVehicleInfo.getVehicleOrgId());
            if (vehicleOrgInfo != null) {
                oilForwardVehicleInfo.setVehicleOrg(vehicleOrgInfo.getName());
            } else {
                oilForwardVehicleInfo.setVehicleOrg("");
            }
            oilForwardVehicleInfo.setForwardingPlatform(
                Optional.ofNullable(platIdNameMap.get(oilForwardVehicleInfo.getForwardingPlatformId())).orElse(""));
            Map<String, Object> line = lineNameMap.get(oilForwardVehicleInfo.getLineId());
            if (line != null) {
                oilForwardVehicleInfo.setLineName(Optional.ofNullable(line.get("name").toString()).orElse(""));
            } else {
                oilForwardVehicleInfo.setLineName("");
            }
        }
        return oilForwardVehicleInfos;
    }

    /**
     * ??????id????????????????????????
     * @param id
     * @return
     */
    @Override
    public OilForwardVehicleInfo findVehicleById(String id) {
        OilForwardVehicleForm oilForwardVehicleForm = forwardVehicleManageDao.getOilForwardVehicleById(id);
        Set<String> groupIds = new HashSet<>();
        groupIds.add(oilForwardVehicleForm.getDockingCodeOrgId());
        groupIds.add(oilForwardVehicleForm.getVehicleOrgId());
        Map<String, OrganizationLdap> orgInfos = organizationService.getOrgByUuids(groupIds);
        Map<String, String> platIdNameMap =
            getPlatIdNameMap(Collections.singleton(oilForwardVehicleForm.getForwardingPlatformId()));
        OilForwardVehicleInfo oilForwardVehicleInfo = new OilForwardVehicleInfo();
        try {
            BeanUtils.copyProperties(oilForwardVehicleForm, oilForwardVehicleInfo);
            oilForwardVehicleInfo.setPlateColor(PlateColor.getNameOrBlankByCode(oilForwardVehicleForm.getPlateColor()));
            oilForwardVehicleInfo.setVehicleStatusStr(oilForwardVehicleForm.getVehicleStatus());
            oilForwardVehicleInfo.setFailedReasonStr(oilForwardVehicleForm.getFailedReason());
            oilForwardVehicleInfo.setIndustryCategoryStr(oilForwardVehicleForm.getIndustryCategory());
            oilForwardVehicleInfo.setMatchStatusStr(oilForwardVehicleForm.getMatchStatus());
        } catch (Exception e) {
            logger.error("beanUtils copy ?????????", e);
        }
        oilForwardVehicleInfo.setMatchTimeStr(
            DateUtil.getDateToString(oilForwardVehicleForm.getMatchTime(), DateUtil.DATE_FORMAT_SHORT));
        OrganizationLdap orgInfo = orgInfos.get(oilForwardVehicleForm.getDockingCodeOrgId());
        if (orgInfo != null) {
            oilForwardVehicleInfo.setDockingCodeOrg(orgInfo.getName());
        } else {
            oilForwardVehicleInfo.setDockingCodeOrg("");
        }

        OrganizationLdap vehicleOrgInfo = orgInfos.get(oilForwardVehicleForm.getVehicleOrgId());
        if (vehicleOrgInfo != null) {
            oilForwardVehicleInfo.setVehicleOrg(vehicleOrgInfo.getName());
        } else {
            oilForwardVehicleInfo.setVehicleOrg("");
        }

        oilForwardVehicleInfo.setForwardingPlatform(
            Optional.ofNullable(platIdNameMap.get(oilForwardVehicleForm.getForwardingPlatformId())).orElse(""));

        return oilForwardVehicleInfo;
    }

    /**
     * ????????????
     * @param id
     * @param lineId
     * @return
     */
    @Override
    public Boolean saveBindLine(String id, String lineId, String ipAddress) {
        OilForwardVehicleForm oilForwardVehicleForm = forwardVehicleManageDao.getOilForwardVehicleById(id);
        forwardVehicleManageDao.bindLine(id, lineId);
        if (oilForwardVehicleForm.getMatchVehicleId() != null) {
            send809Message(Collections.singleton(oilForwardVehicleForm.getMatchVehicleId()));
        }
        //????????????
        String message = "????????????????????????????????????" + oilForwardVehicleForm.getBrand() + "???";
        logSearchService.addLog(ipAddress, message, "3", "??????????????????", oilForwardVehicleForm.getBrand(),
            oilForwardVehicleForm.getPlateColor().toString());
        return true;
    }

    /**
     * ????????????
     * @param ids
     * @return
     */
    @Override
    public Boolean saveCheckVehicle(String ids, String ipAddress) {
        List<String> list;
        if (StringUtils.isNotEmpty(ids)) {
            list = Arrays.asList(ids.split(","));
        } else {
            return false;
        }

        List<OilForwardVehicleForm> oilForwardVehicleForms = forwardVehicleManageDao.getOilForwardVehicleByIds(list);
        List<OilForwardVehicleForm> success = new ArrayList<>();
        List<OilForwardVehicleForm> fail = new ArrayList<>();

        List<String> successBefore = new ArrayList<>();
        Map<String, String> successAfter = new HashMap<>();
        List<String> failBefore = new ArrayList<>();
        Map<String, List<String>> orgIdMap = new HashMap<>();
        for (OilForwardVehicleForm oilForwardVehicleForm : oilForwardVehicleForms) {
            if (oilForwardVehicleForm.getFailedReason() != null && (oilForwardVehicleForm.getFailedReason() == 1
                || oilForwardVehicleForm.getFailedReason() == 2)) {
                continue;
            }
            List<String> orgChildByOrgUuids = orgIdMap.get(oilForwardVehicleForm.getDockingCodeOrgId());
            if (orgChildByOrgUuids == null) {
                OrganizationLdap org =
                    organizationService.getOrganizationByUuid(oilForwardVehicleForm.getDockingCodeOrgId());
                List<OrganizationLdap> orgChildList = organizationService.getOrgChildList(org.getId().toString());
                orgChildByOrgUuids =
                    orgChildList.stream().map(OrganizationLdap::getUuid).distinct().collect(Collectors.toList());
                orgIdMap.put(oilForwardVehicleForm.getDockingCodeOrgId(), orgChildByOrgUuids);
            }

            List<String> vids = forwardVehicleManageDao
                .checkVehicle(orgChildByOrgUuids, oilForwardVehicleForm.getBrand(),
                    oilForwardVehicleForm.getPlateColor());
            if (vids.size() > 0) {
                BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vids.get(0));
                if (bindInfo != null && Vehicle.BindType.HAS_BIND.equals(bindInfo.getBindType())) {
                    oilForwardVehicleForm.setMatchVehicleId(vids.get(0));
                    successBefore.add(oilForwardVehicleForm.getMatchVehicleId());
                    successAfter.put(vids.get(0), oilForwardVehicleForm.getForwardingPlatformId());
                    success.add(oilForwardVehicleForm);
                }
            } else {
                failBefore.add(oilForwardVehicleForm.getMatchVehicleId());
                fail.add(oilForwardVehicleForm);
            }
        }

        //????????????????????????
        if (success.size() > 0) {
            forwardVehicleManageDao.changeSuccessBindStatus(success, new Date(), SystemHelper.getCurrentUsername());
        }
        if (fail.size() > 0) {
            forwardVehicleManageDao.changeFailBindStatus(fail, new Date(), SystemHelper.getCurrentUsername());
        }

        //todo ????????????????????????f3
        //???????????????
        send809Message(successAfter.keySet());
        update809Vehicle(successBefore, successAfter);
        //???????????????
        delete809Vehicle(failBefore);

        //????????????
        addBindVehicleLog(oilForwardVehicleForms, ipAddress);
        return true;
    }

    private void addBindVehicleLog(List<OilForwardVehicleForm> oilForwardVehicleForms, String ipAddress) {
        for (OilForwardVehicleForm oilForwardVehicleForm : oilForwardVehicleForms) {
            String message = "????????????????????????????????? ???" + oilForwardVehicleForm.getBrand() + "???";
            logSearchService.addLog(ipAddress, message, "3", "??????????????????", oilForwardVehicleForm.getBrand(),
                oilForwardVehicleForm.getPlateColor().toString());
        }
    }

    /**
     * ?????????????????????809?????????
     * @param ids
     */
    private void delete809Vehicle(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        //????????????id??????????????????id,?????????809???????????????????????????
        List<String> configIds = RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(ids), "configId");
        if (CollectionUtils.isNotEmpty(configIds)) {
            List<T809PlatFormSubscribe> t809PlatFormSubscribes =
                connectionParamsConfigDao.findConfigByConfigId(new ArrayList<>(configIds), OIL_SUBSIDY_PROTOCOL + "");
            connectionParamsConfigDao.deleteOilSubsidyConfigByConfigIds(configIds, OIL_SUBSIDY_PROTOCOL + "");
            for (T809PlatFormSubscribe t : t809PlatFormSubscribes) {
                T809Message t809Message =
                    MsgUtil.getT809Message(ConstantUtil.T809_FORWARD_DEVICE_DELETE, null, null, t);
                logger.info("??????0x0D0C" + JSONObject.toJSONString(t809Message));
                WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                    .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_FORWARD_DEVICE_DELETE, t809Message));
            }
        }
    }

    /**
     * ?????????????????????809?????????
     * @param ids
     */
    private void update809Vehicle(Collection<String> ids, Map<String, String> vehPlatMap) {

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        //????????????id??????????????????id,?????????809???????????????????????????
        Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(ids);
        Map<String, BindDTO> configMap =
            bindInfos.values().stream().collect(Collectors.toMap(BindDTO::getConfigId, Function.identity()));
        if (MapUtils.isNotEmpty(bindInfos)) {
            List<T809PlatFormSubscribe> t809PlatFormSubscribes = connectionParamsConfigDao
                .findConfigByConfigId(new ArrayList<>(configMap.keySet()), OIL_SUBSIDY_PROTOCOL + "");
            connectionParamsConfigDao.deleteOilSubsidyConfigByConfigIds(configMap.keySet(), OIL_SUBSIDY_PROTOCOL + "");
            for (T809PlatFormSubscribe t : t809PlatFormSubscribes) {
                T809Message t809Message =
                    MsgUtil.getT809Message(ConstantUtil.T809_FORWARD_DEVICE_DELETE, null, null, t);
                logger.info("??????0x0D0C" + JSONObject.toJSONString(t809Message));
                WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                    .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_FORWARD_DEVICE_DELETE, t809Message));
            }
        }

        List<T809ForwardConfig> t809ForwardConfigs = new ArrayList<>();
        for (String configId : configMap.keySet()) {
            BindDTO config = configMap.get(configId);
            T809ForwardConfig t809ForwardConfig = new T809ForwardConfig();
            t809ForwardConfig.setConfigId(configId);
            t809ForwardConfig.setPlantFormId(vehPlatMap.get(config.getId()));
            t809ForwardConfig.setProtocolType(OIL_SUBSIDY_PROTOCOL + "");
            t809ForwardConfigs.add(t809ForwardConfig);
            T809PlatFormSubscribe subscribe = new T809PlatFormSubscribe();
            subscribe.setIdentification(config.getSimCardNumber());
            subscribe.setSettingIds(Collections.singletonList(t809ForwardConfig.getPlantFormId()));
            subscribe.setProtocolType(t809ForwardConfig.getProtocolType());
            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_FORWARD_DEVICE_ADD, null, null, subscribe);
            logger.info("??????0x0C0C" + JSONObject.toJSONString(t809Message));
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                .writeAndFlush(MsgUtil.getMsg(ConstantUtil.T809_FORWARD_DEVICE_ADD, t809Message));
        }
        connectionParamsConfigDao.addConfig(t809ForwardConfigs);
    }

    @Override
    public void initOilBindInfos(List<BindInfo> bindInfos) {
        Set<String> deviceIds = new HashSet<>();
        Set<String> vehicleIds = new HashSet<>();
        Set<String> lineIds = new HashSet<>();
        Map<String, String> vehicleDeviceMap = new HashMap<>();
        for (BindInfo bindInfo : bindInfos) {
            String monitorId = bindInfo.getMonitorInfo().getString("monitorId");
            vehicleIds.add(monitorId);
            vehicleDeviceMap.put(monitorId, bindInfo.getMonitorInfo().getString("deviceId"));
        }
        //?????????????????????
        List<OilVehicleInfo> oilVehicles = forwardVehicleManageDao.getOilVehicles(vehicleIds);
        if (CollectionUtils.isEmpty(oilVehicles)) {
            return;
        }
        Map<String, OilVehicleInfo> oilVehicleInfoMap = new HashMap<>();
        Map<String, OilVehicleInfo> deviceVehicleInfoMap = new HashMap<>();
        Map<String, List<OilVehicleInfo>> lineVehicleInfoMap = new HashMap<>();
        for (OilVehicleInfo ov : oilVehicles) {
            ov.setDeviceId(vehicleDeviceMap.get(ov.getVehicleId()));
            //????????????id?????????
            vehicleIds.add(ov.getVehicleId());
            deviceIds.add(ov.getDeviceId());
            lineIds.add(ov.getLineId());
            //????????????map
            oilVehicleInfoMap.put(ov.getVehicleId(), ov);
            deviceVehicleInfoMap.put(ov.getDeviceId(), ov);
            List<OilVehicleInfo> lineVehicleInfos =
                Optional.ofNullable(lineVehicleInfoMap.get(ov.getLineId())).orElse(new ArrayList<>());
            lineVehicleInfos.add(ov);
            lineVehicleInfoMap.put(ov.getLineId(), lineVehicleInfos);
        }
        List<OilVehicleInfo> vehicles = forwardVehicleManageDao.getVehicles(vehicleIds);
        List<OilVehicleInfo> devices = forwardVehicleManageDao.getDevices(deviceIds);
        List<OilVehicleInfo> lines = forwardVehicleManageDao.getLines(lineIds);
        //????????????????????????
        for (OilVehicleInfo vehicle : vehicles) {
            OilVehicleInfo oilVehicleInfo = oilVehicleInfoMap.get(vehicle.getVehicleId());
            if (oilVehicleInfo == null) {
                continue;
            }
            oilVehicleInfo.setProvinceId(vehicle.getProvinceId());
            oilVehicleInfo.setCityId(vehicle.getCityId());
            oilVehicleInfo.setVehicleVin(vehicle.getVehicleVin());
        }
        //????????????????????????
        for (OilVehicleInfo device : devices) {
            OilVehicleInfo oilVehicleInfo = deviceVehicleInfoMap.get(device.getDeviceId());
            if (oilVehicleInfo == null) {
                continue;
            }
            oilVehicleInfo.setManufacturerId(device.getManufacturerId());
            oilVehicleInfo.setDeviceModelNumber(device.getDeviceModelNumber());
        }
        //??????????????????
        for (OilVehicleInfo line : lines) {
            List<OilVehicleInfo> lineVehicleInfos = lineVehicleInfoMap.get(line.getLineId());
            if (CollectionUtils.isEmpty(lineVehicleInfos)) {
                continue;
            }
            for (OilVehicleInfo oilVehicleInfo : lineVehicleInfos) {
                oilVehicleInfo.setLineNo(line.getLineNo());
            }
        }
        //????????????????????????
        for (BindInfo bindInfo : bindInfos) {
            String monitorId = bindInfo.getMonitorInfo().getString("monitorId");
            OilVehicleInfo oilVehicleInfo = oilVehicleInfoMap.get(monitorId);
            if (oilVehicleInfo == null) {
                continue;
            }

            JSONObject monitorInfo = bindInfo.getMonitorInfo();

            //??????????????????
            monitorInfo.put("lineNo", oilVehicleInfo.getLineNo());
            //?????????????????????????????????????????????????????????
            if (StringUtil.isNullOrBlank(bindInfo.getManufacturerId())) {
                monitorInfo.put("manufacturerId", oilVehicleInfo.getManufacturerId());
            }
            if (StringUtil.isNullOrBlank(bindInfo.getDeviceModelNumber())) {
                monitorInfo.put("deviceModelNumber", oilVehicleInfo.getDeviceModelNumber());
            }

            //????????????????????????
            monitorInfo.put("provinceId", oilVehicleInfo.getProvinceId());
            monitorInfo.put("cityId", oilVehicleInfo.getCityId());
            monitorInfo.put("vehicleVin", oilVehicleInfo.getVehicleVin());
            monitorInfo.put("externalVehicleId", oilVehicleInfo.getExternalVehicleId());
            monitorInfo.put("companyId", oilVehicleInfo.getCompanyId());
            monitorInfo.put("subCompanyId", "0");
        }

    }

    @Override
    public void send809Message(Collection<String> vehicleIds) {
        //????????????????????????
        for (String vid : vehicleIds) {
            configService.sendBindToF3(vid);
        }
    }

    @Override
    public boolean updateDownloadVehicles(String id, HttpServletRequest request) throws BusinessException {
        RedisKey redisKey = HistoryRedisKeyEnum.OIL_DOWNLOAD_KEY.of(id);
        if (RedisHelper.isContainsKey(redisKey)) {
            Long second = RedisHelper.ttl(redisKey);
            long minute = (second / 60) + 1;
            throw new BusinessException(String.format("????????????,%d??????????????????????????????", minute));
        }
        OilDownloadUrlInfo urlInfo = forwardVehicleManageDao.findById(id);
        urlInfo.setDownloadTime(new Date());
        urlInfo.setDownloadStatus(DownLoadStatus.DOWNLOADING.getCode());

        updateDownloadStatus(urlInfo);

        List<OilForwardVehicleForm> oilVehicleInfos = new ArrayList<>();
        OilPlatData oilPlatData = requestRealOilPlatData(urlInfo);

        oilVehicleInfos.addAll(oilPlatData.getResultData(urlInfo));
        String username = SystemHelper.getCurrentUsername();
        //????????????????????????
        taskExecutor.execute(() -> downloadVehicleInfo(urlInfo, oilVehicleInfos, username));
        RedisHelper.setStringNx(redisKey, id, 5 * 60);
        addLog(request, "??????????????????????????????????????????%s???", urlInfo.getDockingCode());
        return true;
    }

    private void addLog(HttpServletRequest request, String s, String dockingCode) {
        //??????????????????
        String message = String.format(s, dockingCode);
        logSearchService.addLog(IPAddrUtil.getClientIp(request), message, "3", "????????????????????????");
    }

    private OilPlatData requestOilPlatData(OilDownloadUrlInfo urlInfo) throws BusinessException {
        Map<String, String> param = new HashMap<>();
        param.put("username", urlInfo.getUserName());
        param.put("password", urlInfo.getPassword());
        param.put("passcode", urlInfo.getDockingCode());
        OilPlatData oilPlatData =
            HttpClientUtil.doHttPost(urlInfo.getUrl(), JSONObject.toJSONString(param), OilPlatData.class);
        if (oilPlatData == null) {
            throw new BusinessException("?????????????????????????????????????????????");
        }
        int result = Integer.parseInt(oilPlatData.getResult());
        if (OilRequestCode.SUCCESS.getCode() != result) {

            throw new BusinessException(OilRequestCode.getNameByCode(result));
        }
        return oilPlatData;
    }

    private OilPlatData requestRealOilPlatData(OilDownloadUrlInfo urlInfo) throws BusinessException {
        String userName = urlInfo.getUserName();
        String password = urlInfo.getPassword();
        String dockingCode = urlInfo.getDockingCode();
        String url = urlInfo.getUrl();
        String xmlInfo = String.format(OIL_WSDL_REQUEST_PARAM, userName, password, dockingCode);
        String resultStr = HttpClientUtil.doWsdlHttPost(url, xmlInfo);
        if (StringUtil.isNullOrBlank(resultStr)) {
            throw new BusinessException("?????????????????????????????????????????????");
        }
        int startIndex = resultStr.indexOf("<return>") + 8;
        int endIndex = resultStr.indexOf("</return>");
        String resultData = CipherEnDecodeUtil.decodeHexToStr(resultStr.substring(startIndex, endIndex));
        OilPlatData oilPlatData = JSONObject.parseObject(resultData, OilPlatData.class);
        int result = Integer.parseInt(oilPlatData.getResult());
        if (OilRequestCode.SUCCESS.getCode() != result) {
            throw new BusinessException(OilRequestCode.getNameByCode(result));
        }
        return oilPlatData;
    }

    private void updateDownloadStatus(OilDownloadUrlInfo urlInfo) {
        OilDownloadUrlForm updateInfo = new OilDownloadUrlForm();
        BeanUtils.copyProperties(urlInfo, updateInfo);
        //??????????????????
        forwardVehicleManageDao.updateDownloadStatus(updateInfo);
    }

    private void downloadVehicleInfo(OilDownloadUrlInfo urlInfo, List<OilForwardVehicleForm> oilVehicleInfos,
        String userName) {

        try {

            //?????????????????????????????????????????????
            if (CollectionUtils.isEmpty(oilVehicleInfos)) {
                //????????????????????????
                urlInfo.setDownloadStatus(DownLoadStatus.SUCCESS.getCode());
                updateDownloadStatus(urlInfo);
                return;
            }

            //?????????????????????????????????
            List<OilForwardVehicleForm> forwardVehicles =
                forwardVehicleManageDao.getForwardVehiclesByDockingCode(urlInfo.getDockingCode());
            //?????????????????????????????????????????????
            Map<String, String> platVehMap = getUserOrgChildMap(urlInfo);

            if (CollectionUtils.isEmpty(forwardVehicles)) {
                firstDownload(urlInfo, oilVehicleInfos, platVehMap, userName);
            } else {
                nextDownload(urlInfo, oilVehicleInfos, forwardVehicles, platVehMap, userName);
            }
        } catch (Exception e) {
            urlInfo.setDownloadStatus(DownLoadStatus.FAILED.getCode());
            updateDownloadStatus(urlInfo);
            return;
        }
        urlInfo.setDownloadStatus(DownLoadStatus.SUCCESS.getCode());
        updateDownloadStatus(urlInfo);

    }

    private Map<String, String> getUserOrgChildMap(OilDownloadUrlInfo urlInfo) {
        Map<String, String> platVehMap = new HashMap<>();
        OrganizationLdap organization = organizationService.getOrganizationByUuid(urlInfo.getDockingCodeOrgId());
        List<OrganizationLdap> orgChildList = organizationService.getOrgChildList(organization.getId().toString());
        Set<String> orgChildByOrgIds = orgChildList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toSet());
        List<OilForwardVehicleForm> platVehicleInfos = forwardVehicleManageDao.getVehicleByOrgIds(orgChildByOrgIds);
        for (OilForwardVehicleForm veh : platVehicleInfos) {
            platVehMap.put(getVehKey(veh), veh.getMatchVehicleId());
        }
        return platVehMap;
    }

    private void firstDownload(OilDownloadUrlInfo urlInfo, List<OilForwardVehicleForm> oilVehicleInfos,
        Map<String, String> platVehMap, String userName) {
        Set<String> updateConfigInfoVids = new HashSet<>();
        //????????????????????????????????????
        List<OilForwardVehicleForm> addSuccessList = new ArrayList<>();
        //????????????????????????????????????
        List<OilForwardVehicleForm> addFailureList = new ArrayList<>();
        //??????id??????id??????
        Map<String, String> vehPlatMap = new HashMap<>();
        //?????????????????????809???????????????
        Set<String> add809VehicleIds = new HashSet<>();
        //?????????????????????????????????????????????????????????
        for (OilForwardVehicleForm veh : oilVehicleInfos) {
            veh.setMatchTime(new Date());
            veh.setCreateDataUsername(userName);
            String vehicleId = platVehMap.get(getVehKey(veh));
            if (vehicleId != null) {
                //????????????
                veh.setMatchVehicleId(vehicleId);
                veh.setMatchStatus(MatchStatus.SUCCESS.getCode());
                addSuccessList.add(veh);
                //??????809config???????????????
                add809Forward(urlInfo, vehPlatMap, add809VehicleIds, vehicleId);
                updateConfigInfoVids.add(vehicleId);
            } else {
                //????????????
                veh.setMatchStatus(MatchStatus.Failed.getCode());
                veh.setFailedReason(FailReason.PLAT_NOT_EXIST.getCode());
                addFailureList.add(veh);
            }
        }
        //?????????????????????
        List<OilForwardVehicleForm> addList = new LinkedList<>();
        addList.addAll(addSuccessList);
        addList.addAll(addFailureList);
        if (CollectionUtils.isNotEmpty(addList)) {
            forwardVehicleManageDao.addForwardVehicles(addList);
        }
        send809Message(updateConfigInfoVids);
        //??????809??????????????????????????????????????????
        update809Vehicle(add809VehicleIds, vehPlatMap);
    }

    private void nextDownload(OilDownloadUrlInfo urlInfo, List<OilForwardVehicleForm> oilVehicleInfos,
        List<OilForwardVehicleForm> forwardVehicles, Map<String, String> platVehMap, String userName) {
        //??????????????????????????????
        Set<String> updateConfigInfoVids = new HashSet<>();
        //?????????????????????
        //????????????????????????????????????
        List<OilForwardVehicleForm> addSuccessList = new ArrayList<>();
        //????????????????????????????????????
        List<OilForwardVehicleForm> addFailureList = new ArrayList<>();
        //?????????????????????_??????????????????
        Set<String> oilVehSet = new HashSet<>();
        //???????????????map
        Map<String, OilForwardVehicleForm> forwardVehMap = new HashMap<>();
        //????????????????????????????????????
        List<OilForwardVehicleForm> updateSuccessList = new ArrayList<>();
        //????????????????????????????????????
        List<OilForwardVehicleForm> updateFailureList = new ArrayList<>();
        //?????????809config??????????????????
        Set<String> delete809VehicleIds = new HashSet<>();
        //??????id??????id???map
        Map<String, String> vehPlatMap = new HashMap<>();
        //????????????????????????809config??????????????????
        Set<String> update809VehicleIds = new HashSet<>();

        for (OilForwardVehicleForm veh : forwardVehicles) {
            forwardVehMap.put(getVehKey(veh), veh);

        }
        for (OilForwardVehicleForm veh : oilVehicleInfos) {
            oilVehSet.add(getVehKey(veh));
        }
        //?????????????????????????????????????????????
        for (OilForwardVehicleForm veh : oilVehicleInfos) {
            veh.setMatchTime(new Date());
            //????????????
            String vehicleId = platVehMap.get(getVehKey(veh));
            updateConfigInfoVids.add(vehicleId);
            if (vehicleId != null) {
                veh.setMatchVehicleId(vehicleId);
                add809Forward(urlInfo, vehPlatMap, update809VehicleIds, vehicleId);
                //????????????
                veh.setMatchStatus(MatchStatus.SUCCESS.getCode());
                //??????????????????????????????
                OilForwardVehicleForm forwardVeh = forwardVehMap.get(getVehKey(veh));
                if (forwardVeh != null) {
                    veh.setUpdateDataUsername(userName);
                    veh.setMatchTime(new Date());
                    veh.setId(forwardVeh.getId());
                    veh.setLineId(forwardVeh.getLineId());
                    veh.setCreateDataUsername(forwardVeh.getCreateDataUsername());
                    veh.setCreateDataTime(forwardVeh.getCreateDataTime());
                    //????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    forwardVehMap.remove(getVehKey(veh));
                    updateSuccessList.add(veh);
                } else {
                    //???????????????,?????????
                    veh.setCreateDataUsername(userName);
                    addSuccessList.add(veh);
                }

            } else {
                //????????????
                veh.setMatchStatus(MatchStatus.Failed.getCode());
                veh.setFailedReason(FailReason.PLAT_NOT_EXIST.getCode());
                //??????????????????????????????
                OilForwardVehicleForm forwardVeh = forwardVehMap.get(getVehKey(veh));
                if (forwardVeh != null) {
                    veh.setId(forwardVeh.getId());
                    veh.setUpdateDataUsername(userName);
                    veh.setMatchTime(new Date());
                    //???????????????????????????????????????????????????????????????809????????????
                    delete809VehicleIds.add(forwardVeh.getMatchVehicleId());
                    //????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    forwardVehMap.remove(getVehKey(veh));
                    updateFailureList.add(veh);
                } else {
                    veh.setCreateDataUsername(userName);
                    addFailureList.add(veh);
                }
            }

        }
        //????????????????????????????????????????????????????????????????????????????????????
        for (OilForwardVehicleForm veh : forwardVehMap.values()) {
            String key = getVehKey(veh);
            veh.setMatchTime(new Date());
            veh.setUpdateDataUsername(userName);
            veh.setMatchStatus(MatchStatus.Failed.getCode());
            delete809VehicleIds.add(veh.getMatchVehicleId());
            if (platVehMap.get(key) != null) {
                //????????????????????????????????????
                veh.setFailedReason(FailReason.OIL_NOT_EXIST.getCode());
            } else {
                //??????????????????????????????????????????
                veh.setFailedReason(FailReason.OIL_PLAT_NOT_EXIST.getCode());
            }
            //?????????????????????????????????????????????????????????????????????????????????
            updateFailureList.add(veh);
        }
        //?????????????????????
        List<OilForwardVehicleForm> addList = new LinkedList<>();
        addList.addAll(addSuccessList);
        addList.addAll(addFailureList);
        if (CollectionUtils.isNotEmpty(addList)) {
            forwardVehicleManageDao.addForwardVehicles(addList);
        }
        //?????????????????????
        List<OilForwardVehicleForm> updateList = new LinkedList<>();
        updateList.addAll(updateSuccessList);
        updateList.addAll(updateFailureList);
        if (CollectionUtils.isNotEmpty(updateList)) {
            forwardVehicleManageDao.updateForwardVehicles(updateList);
        }

        for (OilForwardVehicleForm form : updateFailureList) {
            add809Forward(urlInfo, vehPlatMap, update809VehicleIds, form.getMatchVehicleId());
        }
        //??????????????????809???????????????
        update809Vehicle(update809VehicleIds, vehPlatMap);

        for (OilForwardVehicleForm form : updateFailureList) {
            delete809VehicleIds.add(form.getMatchVehicleId());
        }
        //?????????????????????????????????????????????
        delete809Vehicle(delete809VehicleIds);
        //???????????????????????????????????????
        send809Message(updateConfigInfoVids);
    }

    /**
     * ??????809??????
     * @param urlInfo
     * @param vehPlatMap
     * @param update809VehicleIds
     * @param vehicleId
     */
    private void add809Forward(OilDownloadUrlInfo urlInfo, Map<String, String> vehPlatMap,
        Set<String> update809VehicleIds, String vehicleId) {
        //??????809????????????
        vehPlatMap.put(vehicleId, urlInfo.getForwardingPlatformId());
        update809VehicleIds.add(vehicleId);
    }

    private String getVehKey(OilForwardVehicleForm veh) {
        return veh.getBrand() + "_" + veh.getPlateColor();
    }

    /**
     * ????????????
     * @param ids
     * @return
     */
    @Override
    public boolean deleteVehicle(String ids, String ipAddress) {
        if (StringUtils.isEmpty(ids)) {
            return false;
        }
        List<String> list = Arrays.asList(ids.split(","));
        List<OilForwardVehicleForm> oilForwardVehicleForms = forwardVehicleManageDao.getOilForwardVehicleByIds(list);
        Set<String> vids = forwardVehicleManageDao.getBindVehicleId(list);
        delete809Vehicle(vids);
        boolean re = forwardVehicleManageDao.deleteVehicleByIds(list);
        for (OilForwardVehicleForm oilForwardVehicleForm : oilForwardVehicleForms) {
            String message = "??????????????????:???????????????" + oilForwardVehicleForm.getBrand() + "???";
            logSearchService.addLog(ipAddress, message, "3", "??????????????????", oilForwardVehicleForm.getBrand(),
                oilForwardVehicleForm.getPlateColor().toString());
        }
        return re;
    }
}
