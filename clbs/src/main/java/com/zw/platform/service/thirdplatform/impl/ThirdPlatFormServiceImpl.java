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
            String msg = "新增808转发平台 : " + form.getDescription() + " IP为 : " + form.getPlatformIp();
            logSearchService.log(msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public JsonResultBean deleteById(List<String> ids, String ipAddress) throws Exception {
        if (null != ids && 0 < ids.size()) {
            //先删除平台下所有转发绑定关系
            List<String> cids = dao.findConFigUuidByPIds(ids);
            deleteConfigById(cids, ipAddress);
            //查询即将要删除的平台信息
            List<ThirdPlatForm> thirdPlatForms = dao.findByIds(ids);
            //删除平台
            boolean flag = dao.deleteByIds(ids);
            StringBuilder message = new StringBuilder();
            if (flag) { //若删除成功则下发删除平台信息至F3
                thirdPlatForms.forEach(t -> {
                    WebSubscribeManager.getInstance().sendMsgToAll(t.getId(), ConstantUtil.WEB_808_TRANSMIT_DEL);
                    message.append("删除808转发平台 : ").append(t.getDescription()).append(" IP为 : ")
                        .append(t.getPlatformIp()).append(" <br/>");
                });
                //日志记录
                if (ids.size() == 1) {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "808转发平台IP管理", "-", "");
                } else {
                    logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除808转发平台IP");
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
            //若修改的平台状态为开启则下发新增平台和对应绑定关系
            if (1 == form.getStatus()) {
                //2秒后再发送新增平台指令
                Timer timer1 = new Timer();
                timer1.schedule(new TimerTask() {
                    public void run() {
                        WebSubscribeManager.getInstance().sendMsgToAll(form, ConstantUtil.WEB_808_TRANSMIT_ADD);
                    }
                }, 2000);
                //再2秒后发送原有车辆绑定关系
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
                    message = "修改808转发平台IP : " + beforIp + " 为 :" + nowIp;
                } else {
                    message = "修改IP为 :" + nowIp + "的808转发平台的信息";
                }
                logSearchService.addLog(ipAddress, message, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 查询属于用户的车
     */
    @Override
    @Deprecated
    public List<VehicleInfo> getVehicleList() throws Exception {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<String>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList =
            assignmentService.findUserAssignment(userService.getUserUuidById(userId), userOrgListId);
        List<String> assignIdList = new ArrayList<String>();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                // 分组id list
                assignIdList.add(assign.getId());
            }
        }
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
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
        //查询出该平台下已绑定的所有车id
        List<String> values = dao.findVehiclesOfPlatform(thirdPlatformId);
        //平台中没有被绑定的车id
        List<String> unbindVids = new ArrayList<String>();
        //前端传过来的需要绑定的车id
        List<String> vids = Arrays.asList(vehicleIds.split(","));
        if (values == null || values.size() == 0) { //平台下没有绑定任何车辆
            unbindVids = vids;
        } else {
            //过滤已绑定的车辆
            for (int i = 0; i < vids.size(); i++) {
                if (!values.contains(vids.get(i))) {
                    unbindVids.add(vids.get(i));
                }
            }

        }

        if (unbindVids.size() <= 0) { //若车都被绑定则不做下面处理
            return;
        }

        List<String> configIds = this.findConFigIdByVIds(unbindVids);
        StringBuilder message = new StringBuilder();
        //所有需要绑定的信息集合
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
        //储存成功后再向F3发送808转发绑定车辆信息
        List<String> simCards = findSimCardByVids(unbindVids);
        ThirdPlatForm form = this.findById(thirdPlatformId);
        ThirdPlatFormSubscribe subscribe = new ThirdPlatFormSubscribe();
        subscribe.setPlatId(form.getId());
        subscribe.setIdentifications(simCards);
        WebSubscribeManager.getInstance().sendMsgToAll(subscribe, ConstantUtil.WEB_808_TRANSMIT_DEVICE_ADD);
        message.append("新增808监控对象转发绑定关系").append(num).append("条");
        logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
    }

    @Override
    public JsonResultBean deleteConfigById(List<String> ids, String ipAddress) {
        if (null != ids && !ids.isEmpty()) {
            StringBuilder message = new StringBuilder();
            //查询即将要删除的转发绑定关系信息
            List<ThirdPlatFormSubscribe> view = dao.findConfigByConfigUuid(ids);
            //删除转发绑定关系
            dao.deleteConfigById(ids);
            //下发删除的绑定关系信息至F3
            for (ThirdPlatFormSubscribe t : view) {
                WebSubscribeManager.getInstance().sendMsgToAll(t, ConstantUtil.WEB_808_TRANSMIT_DEVICE_DEL);
            }
            message.append("解除808监控对象转发绑定关系").append(ids.size()).append("条");
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
        // 若有808转发绑定关系则删除
        List<String> config808List = findConfigIdByVconfigIds(configIds);
        if (CollectionUtils.isNotEmpty(config808List)) {
            deleteConfigForUnBind(config808List, unBindEvent);
        }
    }

    private JsonResultBean deleteConfigForUnBind(List<String> ids, ConfigUnBindEvent unBindEvent) {
        if (null != ids && !ids.isEmpty()) {
            StringBuilder message = new StringBuilder();
            //查询即将要删除的转发绑定关系信息
            List<ThirdPlatFormSubscribe> view = dao.findConfigByConfigUuid(ids);
            //删除转发绑定关系
            dao.deleteConfigById(ids);
            //下发删除的绑定关系信息至F3
            for (ThirdPlatFormSubscribe t : view) {
                WebSubscribeManager.getInstance().sendMsgToAll(t, ConstantUtil.WEB_808_TRANSMIT_DEVICE_DEL);
            }
            message.append("解除808监控对象转发绑定关系").append(ids.size()).append("条");
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
