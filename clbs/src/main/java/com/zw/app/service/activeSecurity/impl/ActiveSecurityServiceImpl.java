package com.zw.app.service.activeSecurity.impl;

import com.alibaba.fastjson.JSON;
import com.zw.adas.domain.common.AdasRiskType;
import com.zw.adas.domain.riskManagement.AdasRiskItem;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.activeSecurity.DayRiskNum;
import com.zw.app.domain.activeSecurity.DealInfo;
import com.zw.app.domain.activeSecurity.MediaInfo;
import com.zw.app.domain.activeSecurity.Risk;
import com.zw.app.domain.activeSecurity.RiskEvent;
import com.zw.app.entity.methodParameter.DayRiskDetailEntity;
import com.zw.app.entity.methodParameter.DayRiskEntity;
import com.zw.app.service.activeSecurity.ActiveSecurityService;
import com.zw.app.util.RiskCommonUtil;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.leaderboard.RiskStatus;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AppServerVersion
public class ActiveSecurityServiceImpl implements ActiveSecurityService {

    @Autowired
    private AdasElasticSearchService esService;


    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${fdfs.webServerUrl}")
    private String fastDFSMediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    @Autowired
    private AdasRiskService adasRiskService;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = { "/clbs/app/risk/security/getRiskList" })
    public List<Risk> getRiskList(long pageNum, long pageSize, String riskIds) {
        Set<String> vehicleList = userPrivilegeUtil.getCurrentUserVehicles();
        List<String> riskIdList = adasElasticSearchUtil.getTodayUntreatedRisk(vehicleList, riskIds);
        if (vehicleList == null) {
            return null;
        }
        if (riskIdList.size() <= 0) {
            return null;
        }
        //得到当前分页的idList
        List<String> riskIdsInPage = riskIdList.stream()
                .limit(pageSize)
                .collect(Collectors.toList());

        if (riskIdsInPage.isEmpty()) {
            return null;
        }
        return getRisks(riskIdsInPage);
    }

    @Override
    public List<Risk> getRisks(List<String> riskIds) {
        final List<AdasRiskItem> riskList = adasRiskService.getRiskList(riskIds);
        return riskList.stream().map(o -> {
            final Risk risk = new Risk();
            risk.setRiskId(o.getRiskId());
            risk.setId(UuidUtils.getUUIDStrFromBytes(o.getRiskId()));
            risk.setPicFlag(o.getPicFlag() == null ? 0 : o.getPicFlag());
            risk.setVideoFlag(o.getVideoFlag());
            risk.setBrand(o.getBrand());
            risk.setWarningTime(o.getWarningTime());
            risk.setRiskLevel(Integer.parseInt(o.getRiskLevel()));
            risk.setRiskType(AdasRiskType.getAppRiskType(o.getRiskType()));
            final String riskStatus = o.getRiskStatus();
            int status = null != riskStatus && !"null".equals(riskStatus)
                    && RiskStatus.ARCHIVE.toString().equals(riskStatus)
                            ? RiskStatus.TREATED.getCode()
                            : RiskStatus.UNTREATED.getCode();
            risk.setRiskStatus(RiskCommonUtil.getRiskStatus(status).getName());
            return risk;
        }).collect(Collectors.toList());
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {
        "/clbs/app/risk/security/getRiskEventByRiskId" })
    public List<RiskEvent> getRiskEventByRiskId(String riskId) {
        List<AdasRiskEventEsBean> riskEventEsBeans = esService.getEventEsBeanByRiskId(riskId);
        return RiskEvent.getRiskEventResult(riskEventEsBeans, adasCommonHelper);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = { "/clbs/app/risk/security/dealInfo" })
    public DealInfo getDealInfo() {
        return adasElasticSearchUtil.getTodayDealInfo(userPrivilegeUtil.getCurrentUserVehicles());
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = { "/clbs/app/risk/security/getMediaInfo" })
    public List<MediaInfo> getMediaInfo(String riskId, int mediaType) {
        List<MediaInfo> medias = new ArrayList<>();
        List<String> mediaIds = esService.esGetMediaIdsByRiskId(mediaType, true, false, false, riskId);
        if (mediaIds.size() > 0) {
            medias = setMediaPath(this.listAdasMediaById(mediaIds));
        }
        return medias;
    }

    private List<MediaInfo> listAdasMediaById(List<String> mediaIds) {
        if (CollectionUtils.isEmpty(mediaIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("mediaIds", JSON.toJSONString(mediaIds));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_RISK_MEDIA, params);
        return PaasCloudUrlUtil.getResultListData(str, MediaInfo.class);
    }

    private List<MediaInfo> setMediaPath(List<MediaInfo> medias) {
        List<MediaInfo> newMedias = new ArrayList<>();
        for (MediaInfo media : medias) {
            AdasRiskEventEsBean riskEventEsBean = esService.esGetRiskEventById(media.getRiskEventId());
            boolean isStreamMedia = adasCommonHelper.isStreamMedia(media.getProtocolType());
            String prefix = isStreamMedia ? fastDFSMediaServer : mediaServer;
            String newPath = prefix + media.getMediaUrl();
            media.setMediaUrl(newPath);
            media.setEventName(adasCommonHelper.geEventName(riskEventEsBean.getEventType() + ""));
            media.setEventTime(riskEventEsBean.getEventTime());
            media.setEventId(media.getRiskEventId());
            newMedias.add(media);
        }
        return newMedias;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = { "/clbs/app/risk/security/dealRisk" })
    public boolean saveRiskDealInfo(String riskId, Integer riskResult) throws Exception {
        //归档的状态标志
        int archive = RiskStatus.ARCHIVE.getCode();
        AdasDealRiskForm adasDealRiskForm = new AdasDealRiskForm();
        adasDealRiskForm.setStatus(archive);
        adasDealRiskForm.setRiskId(riskId);
        adasDealRiskForm.setRiskResult(riskResult);
        return adasRiskService.saveRiskDealInfo(adasDealRiskForm);

    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_TWO, url = { "/clbs/app/risk/security/getDayRiskNum" })
    public List<DayRiskNum> getDayRiskNum(DayRiskEntity dayRiskEntity) {
        return adasElasticSearchUtil.getDayRiskNum(dayRiskEntity);
    }

    /**
     * 功能描述:查询每天车辆的报警详情数据
     * 注意：
     * 1.因为我们的报警的等级一直在改变，使用分页时有可能前面已经查询出来的报警被后面的报警顶到后面重复查询出来，所以每次回把已经存在的风险id带过来进行过滤掉
     * 2.过滤掉机会存在分页之后查询结果的数量变少的情况，所以这里直接取前面pageSize数据即可
     * @return * @return : java.util.List<com.zw.app.domain.activeSecurity.Risk>
     * @author zhengjc
     */

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_TWO, url = {
        "/clbs/app/risk/security/getDayRiskDetail" })
    public List<Risk> getDayRiskDetail(DayRiskDetailEntity dayRiskDetailEntity) {
        int pageSize = dayRiskDetailEntity.getPageSize();
        List<String> riskIdList = adasElasticSearchUtil.getDayRiskDetail(dayRiskDetailEntity);

        //得到当前分页的idList
        List<String> riskIdBytes = riskIdList.stream()
                .skip(0)
                .limit(pageSize)
                .collect(Collectors.toList());

        if (riskIdBytes.isEmpty()) {
            return null;
        }
        return getRisks(riskIdBytes);
    }

}
