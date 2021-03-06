package com.zw.platform.service.thirdplatform.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.forwardplatform.ThirdPlatForm;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfig;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormConfigView;
import com.zw.platform.domain.forwardplatform.ThirdPlatFormSubscribe;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormQuery;
import com.zw.platform.repository.modules.IntercomPlatFormDao;
import com.zw.platform.repository.modules.ThirdPlatFormDao;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.thirdplatform.ThirdPlatFormService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by LiaoYuecai on 2017/3/3.
 */
@Service
public class ThirdPlatFormServiceImpl implements ThirdPlatFormService {

    @Autowired
    private ThirdPlatFormDao dao;

    @Autowired
    private IntercomPlatFormDao platFormDao;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public List<ThirdPlatForm> findList(IntercomPlatFormQuery query, boolean doPage) {
        return doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                        .doSelectPage(() -> dao.findList(query))
                : dao.findList(query);
    }

    @Override
    public JsonResultBean add(ThirdPlatForm form, String ipAddress) throws Exception {
        form.setId(UUID.randomUUID().toString());
        form.setCreateDataTime(new Date());
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean result = dao.add(form);
        if (result) {
            if (form.getStatus() == 1) {
                WebSubscribeManager.getInstance().sendMsgToAll(form, ConstantUtil.WEB_808_TRANSMIT_ADD);
            }
            String msg = "??????808???????????? : " + form.getDescription() + " IP??? : " + form.getPlatformIp();
            logSearchService.log(msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public JsonResultBean deleteById(List<String> ids, String ipAddress) throws Exception {
        if (null != ids && 0 < ids.size()) {
            //??????????????????????????????????????????
            List<String> cids = dao.findConFigUuidByPIds(ids);
            deleteConfigById(cids, ipAddress);
            //????????????????????????????????????
            List<ThirdPlatForm> thirdPlatForms = dao.findByIds(ids);
            //????????????
            boolean flag = dao.deleteByIds(ids);
            StringBuilder message = new StringBuilder();
            if (flag) { //?????????????????????????????????????????????F3
                thirdPlatForms.forEach(t -> {
                    WebSubscribeManager.getInstance().sendMsgToAll(t.getId(), ConstantUtil.WEB_808_TRANSMIT_DEL);
                    message.append("??????808???????????? : ").append(t.getDescription()).append(" IP??? : ")
                        .append(t.getPlatformIp()).append(" <br/>");
                });
                //????????????
                if (ids.size() == 1) {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "808????????????IP??????", "-", "");
                } else {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "????????????808????????????IP");
                }
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean update(ThirdPlatForm form, String ipAddress) throws Exception {
        ThirdPlatForm thirdPlatForm = findById(form.getId());
        form.setUpdateDataTime(new Date());
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = dao.update(form);
        if (flag) {
            WebSubscribeManager.getInstance().sendMsgToAll(form.getId(), ConstantUtil.WEB_808_TRANSMIT_DEL);
            //???????????????????????????????????????????????????????????????????????????
            if (1 == form.getStatus()) {
                //2?????????????????????????????????
                Timer timer1 = new Timer();
                timer1.schedule(new TimerTask() {
                    public void run() {
                        WebSubscribeManager.getInstance().sendMsgToAll(form, ConstantUtil.WEB_808_TRANSMIT_ADD);
                    }
                }, 2000);
                //???2????????????????????????????????????
                //List<String> devices = dao.findDeviceNumberByFormId(form.getId());
                //ThirdPlatFormSubscribe subscribe = new ThirdPlatFormSubscribe();
                //subscribe.setPlatId(form.getId());
                //subscribe.setDeviceNumbers(devices);
                //Timer timer2 = new Timer();
                //timer2.schedule(new TimerTask() {
                //      public void run() {
                //          WebSubscribeManager.getInstance()
                //              .sendMsgToAll(subscribe, ConstantUtil.WEB_808_TRANSMIT_DEVICE_ADD);
                //      }
                //}, 2000);
            }

            if (thirdPlatForm != null) {
                String beforIp = thirdPlatForm.getPlatformIp();
                String nowIp = form.getPlatformIp();
                String message = "";
                if (!beforIp.equals(nowIp)) {
                    message = "??????808????????????IP : " + beforIp + " ??? :" + nowIp;
                } else {
                    message = "??????IP??? :" + nowIp + "???808?????????????????????";
                }
                logSearchService.addLog(ipAddress, message, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ????????????????????????
     */
    @Override
    @Deprecated
    public List<VehicleInfo> getVehicleList() throws Exception {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(","); // ????????????id(????????????id????????????????????????)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<String>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        // ??????????????????????????????
        List<Assignment> assignmentList =
            assignmentService.findUserAssignment(userService.getUserUuidById(userId), userOrgListId);
        List<String> assignIdList = new ArrayList<String>();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                // ??????id list
                assignIdList.add(assign.getId());
            }
        }
        // ?????????????????????????????????????????????????????????(???????????????)
        if (assignIdList != null && assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = platFormDao.findVehicleTreeByThirdPlatform(assignIdList);
            return vehicleList;
        }
        return null;
    }

    @Override
    public void updateConfigById(ThirdPlatFormConfig config) {
        List<String> configIds = this.findConFigIdByVIds(Arrays.asList(config.getConfigId()));
        config.setConfigId(configIds.get(0));
        config.setUpdateDataTime(new Date());
        config.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        dao.updateConfigById(config);
    }

    @Override
    public List<String> findConFigUuidByPIds(List<String> pids) {
        return dao.findConFigUuidByPIds(pids);
    }

    @Override
    public ThirdPlatForm findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Page<ThirdPlatFormConfigView> findConfigViewList(IntercomPlatFormConfigQuery query) {
        final Page<ThirdPlatFormConfigView> thirdPlatFormConfigViews =
                PageHelperUtil.doSelect(query, () -> dao.findConfigViewList(query));
        final List<RedisKey> vehicleIds = thirdPlatFormConfigViews.stream()
                .map(ThirdPlatFormConfigView::getVehicleId)
                .map(RedisKeyEnum.MONITOR_INFO::of)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(vehicleIds)) {
            final Map<String, String> idOrgNameMap = RedisHelper.batchGetHashMap(vehicleIds, "id", "orgName");
            thirdPlatFormConfigViews.forEach(o -> o.setOrgName(idOrgNameMap.get(o.getVehicleId())));
        }
        return thirdPlatFormConfigViews;
    }

    @Override
    public List<ThirdPlatFormConfigView> findConfigViewListByVehicleId(String vehicleId) {
        return dao.findConfigViewListByVehicleId(vehicleId);
    }

    @Override
    public void addConfig(String thirdPlatformId, String vehicleIds, String ipAddress) throws Exception {
        //??????????????????????????????????????????id
        List<String> values = dao.findVehiclesOfPlatform(thirdPlatformId);
        //??????????????????????????????id
        List<String> unbindVids = new ArrayList<String>();
        //????????????????????????????????????id
        List<String> vids = Arrays.asList(vehicleIds.split(","));
        if (values == null || values.size() == 0) { //?????????????????????????????????
            unbindVids = vids;
        } else {
            //????????????????????????
            for (int i = 0; i < vids.size(); i++) {
                if (!values.contains(vids.get(i))) {
                    unbindVids.add(vids.get(i));
                }
            }

        }

        if (unbindVids.size() <= 0) { //???????????????????????????????????????
            return;
        }

        List<String> configIds = this.findConFigIdByVIds(unbindVids);
        StringBuilder message = new StringBuilder();
        //?????????????????????????????????
        List<ThirdPlatFormConfig> tpcf = new ArrayList<ThirdPlatFormConfig>();
        int num = 0;
        for (int i = 0; i < configIds.size(); i++) {
            ThirdPlatFormConfig config = new ThirdPlatFormConfig();
            config.setThirdPlatformId(thirdPlatformId);
            config.setCreateDataUsername(SystemHelper.getCurrentUsername());
            config.setConfigId(configIds.get(i));
            tpcf.add(config);
            num++;
        }
        dao.addConfigByBatch(tpcf);
        //?????????????????????F3??????808????????????????????????
        List<String> simCards = findSimCardByVids(unbindVids);
        ThirdPlatForm form = this.findById(thirdPlatformId);
        ThirdPlatFormSubscribe subscribe = new ThirdPlatFormSubscribe();
        subscribe.setPlatId(form.getId());
        subscribe.setIdentifications(simCards);
        WebSubscribeManager.getInstance().sendMsgToAll(subscribe, ConstantUtil.WEB_808_TRANSMIT_DEVICE_ADD);
        message.append("??????808??????????????????????????????").append(num).append("???");
        logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
    }

    @Override
    public JsonResultBean deleteConfigById(List<String> ids, String ipAddress) {
        if (null != ids && !ids.isEmpty()) {
            StringBuilder message = new StringBuilder();
            //????????????????????????????????????????????????
            List<ThirdPlatFormSubscribe> view = dao.findConfigByConfigUuid(ids);
            //????????????????????????
            dao.deleteConfigById(ids);
            //????????????????????????????????????F3
            for (ThirdPlatFormSubscribe t : view) {
                WebSubscribeManager.getInstance().sendMsgToAll(t, ConstantUtil.WEB_808_TRANSMIT_DEVICE_DEL);
            }
            message.append("??????808??????????????????????????????").append(ids.size()).append("???");
            if (!message.toString().isEmpty()) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent unBindEvent) {
        if (Objects.equals("update", unBindEvent.getOperation())) {
            return;
        }
        List<String> configIds =
            unBindEvent.getUnbindList().stream().map(BindDTO::getConfigId).collect(Collectors.toList());
        // ??????808???????????????????????????
        List<String> config808List = findConfigIdByVconfigIds(configIds);
        if (CollectionUtils.isNotEmpty(config808List)) {
            deleteConfigForUnBind(config808List, unBindEvent);
        }
    }

    private JsonResultBean deleteConfigForUnBind(List<String> ids, ConfigUnBindEvent unBindEvent) {
        if (null != ids && !ids.isEmpty()) {
            StringBuilder message = new StringBuilder();
            //????????????????????????????????????????????????
            List<ThirdPlatFormSubscribe> view = dao.findConfigByConfigUuid(ids);
            //????????????????????????
            dao.deleteConfigById(ids);
            //????????????????????????????????????F3
            for (ThirdPlatFormSubscribe t : view) {
                WebSubscribeManager.getInstance().sendMsgToAll(t, ConstantUtil.WEB_808_TRANSMIT_DEVICE_DEL);
            }
            message.append("??????808??????????????????????????????").append(ids.size()).append("???");
            if (!message.toString().isEmpty()) {
                logSearchService.addLogByUserNameAndOrgId(unBindEvent.getIpAddress(), message.toString(), "3", "", "-",
                        unBindEvent.getUserName(), unBindEvent.getOrgId());
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public ThirdPlatFormConfig findConfigById(String id) {
        return dao.findConfigById(id);
    }

    @Override
    public ThirdPlatFormConfigView findConfigViewByConfigId(String configId) {
        return dao.findConfigViewByConfigId(configId);
    }

    @Override
    public Integer findConfigViewListByIds(String vehicleId, String platFormId) {
        return dao.findConfigViewListByIds(vehicleId, platFormId);
    }

    @Override
    public List<String> findConFigIdByVIds(List<String> vehicleIds) {
        return dao.findConFigIdByVIds(vehicleIds);
    }

    @Override
    public List<String> findDeviceNumberByVIds(List<String> vehicleIds) {
        return dao.findDeviceNumberByVIds(vehicleIds);
    }

    @Override
    public List<ThirdPlatFormConfigView> findConfigViewByConfigIds(List<String> configIds) {
        return dao.findConfigViewByConfigIds(configIds);
    }

    @Override
    public boolean check808PlatFormSole(String platFormName, String pid) throws Exception {
        String id = dao.check808PlatFormSole(platFormName);
        if (StringUtils.isEmpty(id) || pid.equals(id)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<String> findSimCardByVids(List<String> vehicleIds) {
        return dao.findSimCardByVids(vehicleIds);
    }

    @Override
    public List<String> findConfigIdByVconfigIds(List<String> vcids) {
        return dao.findConfigIdByVconfigIds(vcids);
    }

}
