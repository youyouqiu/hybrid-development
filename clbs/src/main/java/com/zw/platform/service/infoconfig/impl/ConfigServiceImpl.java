package com.zw.platform.service.infoconfig.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.AssignmentInfo;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.PeopleInfo;
import com.zw.platform.domain.basicinfo.PersonnelInfo;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.builder.BindInfoBuilder;
import com.zw.platform.domain.infoconfig.builder.MonitorInfoBuilder;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigForm;
import com.zw.platform.domain.infoconfig.form.GroupForConfigForm;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.infoconfig.form.ProfessionalForConfigFrom;
import com.zw.platform.domain.infoconfig.query.ConfigDetailsQuery;
import com.zw.platform.domain.infoconfig.query.ConfigQuery;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.domain.topspeed_entering.DeviceRegister;
import com.zw.platform.domain.vas.f3.SensorPolling;
import com.zw.platform.repository.modules.DeviceRegisterDao;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.sensor.SensorPollingService;
import com.zw.platform.util.BSJFakeIPUtil;
import com.zw.platform.util.BindSendUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ????????????Service????????? <p> Title: ConfigServiceImpl.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei
 * </p> <p> team: ZhongWeiTeam </p>
 * @author Liubangquan
 * @version 1.0
 */
@Service("oldConfigService")
public class ConfigServiceImpl implements ConfigService {

    /**
     * ????????????????????????????????????
     */
    @Value("${max.number.assignment.monitor:100}")
    private Integer maxNumberAssignmentMonitor;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Autowired
    private SimCardNewDao simCardNewDao;

    @Autowired
    private UserService userService;


    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SensorPollingService sensorPollingService;


    @Autowired
    private AssignmentService assignmentService;


    @Autowired
    private DeviceRegisterDao deviceRegisterDao;


    private static final String VEHICLE = "vehicle";

    private static final String ORG = "org";


    @Autowired
    private BindSendUtil bindSendUtil;

    @Override
    public ConfigDO get(String id) {
        return newConfigDao.getById(id);
    }

    @Override
    public MonitorInfo handleMonitorInfo(Object data, int monitorType) {
        MonitorInfo monitorInfo = new MonitorInfo();
        if (data != null) {
            switch (monitorType) {
                case 0: //???
                    VehicleInfo vi = (VehicleInfo) data;
                    BeanUtils.copyProperties(vi, monitorInfo);
                    monitorInfo.setMonitorName(vi.getBrand());
                    monitorInfo.setDeviceNumber(vi.getDeviceNumber());
                    monitorInfo.setMonitorType(monitorType);
                    monitorInfo.setMonitorId(vi.getId());
                    monitorInfo.setVehicleNumber(vi.getBrand());
                    //???????????????????????????????????????
                    if (StringUtils.isNotBlank(vi.getInstallTime())) {
                        monitorInfo.setInstallTime(DateUtil.getStringToLong(vi.getInstallTime(), "yyyy-MM-dd"));
                    }
                    monitorInfo.setInstallCompany(vi.getInstallCompany());
                    monitorInfo.setTelephone(vi.getTelephone());
                    monitorInfo.setComplianceRequirements(vi.getComplianceRequirements());
                    monitorInfo.setContacts(vi.getContacts());
                    break;
                case 1: // ???
                    PeopleInfo pi = (PeopleInfo) data;
                    BeanUtils.copyProperties(pi, monitorInfo);
                    monitorInfo.setMonitorName(pi.getPeopleNumber());
                    monitorInfo.setMonitorType(monitorType);
                    monitorInfo.setMonitorId(pi.getId());
                    break;
                case 2: //???
                    ThingInfo ti = (ThingInfo) data;
                    BeanUtils.copyProperties(ti, monitorInfo);
                    monitorInfo.setMonitorName(ti.getThingNumber());
                    monitorInfo.setMonitorType(monitorType);
                    monitorInfo.setMonitorId(ti.getId());
                    break;
                default:
                    break;
            }
        }
        return monitorInfo;
    }

    @Override
    public List<VehicleInfo> getPeopleInfoList(String id) {
        // ??????userName??????userId
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        List<VehicleInfo> result = new ArrayList<>();
        if (userId != null) {
            result = newConfigDao.getPersonelInfoList(userService.getUserUuidById(userId), userOrgListId, id);
        }
        // ????????????????????????
        List<VehicleInfo> vlist = newConfigDao.getVehicleInfoByConfigId(id);
        if (null != vlist && vlist.size() > 0) {
            result.add(vlist.get(0));
        }
        return result;
    }

    @Override
    public List<ThingInfo> getThingInfoList() {
        return newConfigDao.getThingInfoList();
    }

    @Override
    public List<DeviceInfo> getDeviceInfoList(String id) {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return newConfigDao.getDeviceInfoList(list, id);
    }

    @Override
    public List<DeviceInfo> getDeviceInfoListForPeople(String id) {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return newConfigDao.getDeviceInfoListForPeople(list, id);
    }

    @Override
    public List<SimcardInfo> getSimcardInfoList(String id) {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return newConfigDao.getSimcardInfoList(list, id);
    }

    @Override
    public List<ProfessionalsInfo> getProfessionalsInfoList() {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return newConfigDao.getProfessionalsInfoList(list);
    }

    @Override
    public void addBindInfo(String deviceId, Config1Form config1Form) {
        BindInfo bindInfo = new BindInfo();
        String authCode = simCardNewDao.getAuthCodeBySimId(config1Form.getSimID());
        if (StringUtils.isNotEmpty(authCode)) {
            bindInfo.setAuthCode(authCode);
        }
        if (StringUtils.isNotBlank(config1Form.getUniqueNumber())) {
            String uniqueNumber = config1Form.getUniqueNumber();
            String[] sign = uniqueNumber.split("???");
            if (sign.length == 2) {
                // ?????????????????????????????????id???????????????
                DeviceRegister drInfo = deviceRegisterDao.getRegisterInfo(sign[0].trim());
                if (drInfo != null && StringUtils.isNotBlank(drInfo.getManufacturerId()) && StringUtils
                    .isNotBlank(drInfo.getDeviceModelNumber())) {
                    bindInfo.setManufacturerId(drInfo.getManufacturerId()); // ????????????ID
                    bindInfo.setDeviceModelNumber(drInfo.getDeviceModelNumber()); // ????????????
                }
            }
        }
        //??????????????????
        MonitorInfo monitorInfo;
        //????????????
        VehicleInfo vehicleInfo = null;
        if (config1Form.getMonitorType().equals("0")) {
            //????????????
            vehicleInfo = getVehicleByDevice(config1Form.getDevices());
            vehicleInfo.setPlateColor(config1Form.getPlateColor());
            //????????????????????????
            monitorInfo = handleMonitorInfo(vehicleInfo, 0);
            MonitorInfoBuilder
                .buildVehicleStaticData(vehicleService.get809VehicleStaticData(vehicleInfo.getId()), monitorInfo);
        } else if ("1".equals(config1Form.getMonitorType())) {
            //????????????
            PeopleInfo peopleInfo = getPeopleInfoByDevice(config1Form.getDevices());
            //????????????????????????
            monitorInfo = handleMonitorInfo(peopleInfo, 1);
        } else {
            //????????????
            ThingInfo thingInfo = getThingInfoByDevice(config1Form.getDevices());
            //????????????????????????
            monitorInfo = handleMonitorInfo(thingInfo, 2);
        }
        //?????????????????????????????????????????????????????????
        monitorInfo.setAuthCode(authCode);
        monitorInfo.setGroupName(config1Form.getGroupName());
        monitorInfo.setTerminalManufacturer(config1Form.getTerminalManufacturer());
        monitorInfo.setTerminalType(config1Form.getTerminalType());
        //??????????????????????????????

        bindInfo.setOldDeviceId(deviceId);
        bindInfo.setDeviceId(deviceId);
        String deviceType = config1Form.getDeviceType();
        BindInfoBuilder.buildIdentify(bindInfo, config1Form);
        MonitorInfoBuilder.buildFakeIp(deviceType, bindInfo.getIdentification(), monitorInfo);
        bindInfo.setOldDeviceType(deviceType);
        bindInfo.setDeviceType(deviceType);
        // ????????????????????????
        bindSendUtil.sendBindInfo(bindInfo, monitorInfo);
        // ??????????????????
        if (vehicleInfo == null) {
            return;
        }
        Set<String> subVehicle = new HashSet<>();
        subVehicle.add(monitorInfo.getMonitorId());
        WebSubscribeManager.getInstance().updateSubStatus(subVehicle);
    }

    @Override
    public Page<ConfigList> findByPage(final ConfigQuery query) {
        // ??????userName??????userId
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(",");
        // ????????????id(????????????id????????????????????????)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        if (null != orgs && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }

        return PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> newConfigDao.find(query, userService.getUserUuidById(userId), userOrgListId));
    }


    /**
     * ??????????????????
     */
    @Override
    @MethodLog(name = "??????????????????", description = "??????????????????")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<>(16);
        // ??????
        headList.add("????????????");
        headList.add("??????????????????");
        headList.add("????????????(??????????????????)");
        headList.add("????????????");
        headList.add("??????(???????????????????????????)");
        headList.add("?????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("???????????????");
        headList.add("??????SIM??????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????(?????????????????????????????????)");
        // ????????????
        requiredList.add("????????????");
        requiredList.add("??????????????????");
        requiredList.add("????????????(??????????????????)");
        requiredList.add("????????????");
        requiredList.add("?????????");
        requiredList.add("????????????");
        requiredList.add("????????????");
        requiredList.add("???????????????");
        // ????????????????????????
        exportList.add("??????");
        exportList.add("???");
        exportList.add("??????");
        // ?????????????????????????????????????????????
        List<String> groupNames = userService.getOrgNamesByUser();
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("????????????", groupNameArr);
            String groupName = groupNames.get(0);
            exportList.add(groupName);
        } else {
            exportList.add("zwkj");
        }
        exportList.add("????????????");
        exportList.add("20160808001");
        exportList.add("?????????JT/T808-2013");
        exportList.add("[f]F3");
        exportList.add("F3-default");
        exportList.add("???????????????");
        exportList.add("13562584562");
        exportList.add("13562584566");
        exportList.add("2016-08-01");
        exportList.add("2016-08-31");
        exportList.add("??????");

        // ?????????????????????Bug #5654???????????????
        Set<String> deviceTypeNameList = ProtocolEnum.DEVICE_TYPE_NAMES;
        String[] deviceType = new String[deviceTypeNameList.size()];
        deviceTypeNameList.toArray(deviceType);
        selectMap.put("????????????", deviceType);
        String[] monitorType = { "???", "???", "???" };
        selectMap.put("??????????????????", monitorType);
        // ????????????
        String[] functionalType = { "???????????????", "???????????????", "????????????", "????????????", "??????????????????", "????????????" };
        selectMap.put("????????????", functionalType);

        // ????????????????????????????????????
        List<TerminalTypeInfo> terminalTypeInfoList = terminalTypeDao.getAllTerminalType();
        List<String> terminalManufacturers = terminalTypeDao.getTerminalManufacturer();
        if (CollectionUtils.isNotEmpty(terminalTypeInfoList)) {
            selectMap.put("????????????",
                terminalTypeInfoList.stream().map(TerminalTypeInfo::getTerminalType).distinct().toArray(String[]::new));
        }
        if (CollectionUtils.isNotEmpty(terminalManufacturers)) {
            selectMap.put("????????????", terminalManufacturers.toArray(new String[0]));
        }
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);
        out.close();

        return true;
    }


    @Override
    public List<ConfigDetailsQuery> configDetailsall() {
        return newConfigDao.configDetailsall();
    }

    @Override
    public VehicleInfo getVehicleByDevice(String id) {
        final Map<String, String> vehicleInfo = newConfigDao.getVehicleByDeviceNumber(id);
        final List<String> assignmentNames = newConfigDao.getAssignmentNamesByVehicleId(vehicleInfo.get("id"));
        final List<Map<String, String>> professionalInfos =
            newConfigDao.getProfessionalInfoByConfigId(vehicleInfo.get("configId"));
        professionalInfos.removeIf(Objects::isNull);
        final String categoryIcon = newConfigDao.getCategoryIconByCategoryId(vehicleInfo.get("vehicleCategory"));
        final VehicleInfo resp = new VehicleInfo();

        // ????????????
        try {
            ConvertUtils.register(resp, Date.class);
            org.apache.commons.beanutils.BeanUtils.populate(resp, vehicleInfo);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        final String assignmentName = StringUtils.join(assignmentNames, ",");
        resp.setAssignmentId(assignmentName);
        resp.setAssignmentName(assignmentName);

        StringBuilder builder = new StringBuilder();
        professionalInfos.forEach(map -> builder.append(map.get("name")).append(","));
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        resp.setProfessionalsName(builder.toString());

        final String cardNumber = professionalInfos.isEmpty() ? "" : professionalInfos.get(0).get("cardNumber");
        resp.setCardNumber(cardNumber);

        resp.setCategoryIcon(categoryIcon);
        return resp;
    }

    @Override
    public VehicleInfo getVehicleByDeviceNew(String id) {
        return newConfigDao.getVehicleByDeviceNew(id);
    }

    @Override
    public PeopleInfo getPeopleInfoByDevice(String id) {
        return newConfigDao.getPeopleInfoByDevice(id);
    }

    @Override
    public ThingInfo getThingInfoByDevice(String id) {
        return newConfigDao.getThingInfoByDevice(id);
    }

    @Override
    public PersonnelInfo getPeopleByDevice(String id) {
        return newConfigDao.getPeopleByDevice(id);
    }

    @Override
    public ConfigForm getIsBand(String vid, String did, String cid, String pid) {
        return newConfigDao.getIsBand(vid, did, cid, pid);
    }

    @Override
    public List<ConfigForm> getIsBands(String vid, String did, String cid, String pid) {
        return newConfigDao.getIsBands(vid, did, cid, pid);
    }

    @Override
    public List<GroupForConfigForm> isBnadP(String id) {
        return newConfigDao.isBnadP(id);
    }


    @Override
    public List<GroupForConfigForm> isBnadG(String id) {
        return newConfigDao.isBnadG(id);
    }

    @Override
    public ConfigDetailsQuery configDetails(final String id) {
        return newConfigDao.configDetails(id);
    }

    @Override
    public ConfigDetailsQuery peopleConfigDetails(final String id) {
        return newConfigDao.peopleConfigDetails(id);
    }

    @Override
    public ConfigDetailsQuery thingConfigDetails(final String id) {
        return newConfigDao.thingConfigDetails(id);
    }

    @Override
    public List<ProfessionalForConfigFrom> getProfessionalForConfigListByConfigId(String id) {
        return newConfigDao.getProfessionalForConfigListByConfigId(id);
    }

    @Override
    public int isBandDevice(String id) {
        return newConfigDao.isBandDevice(id);
    }

    @Override
    public int isBandSimcard(String id) {
        return newConfigDao.isBandSimcard(id);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     * @param username ?????????
     * @return ????????????
     * @author FanLu
     */
    @Override
    public Map<String, Object> getOrgAndVehicle(String username) {
        Map<String, Object> map = new HashMap<>();
        // ??????userName??????userId
        String userId = userService.getUserDetails(username).getId().toString();
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(",");
        // ????????????id(????????????id????????????????????????)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        if (null != orgs && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        map.put(ORG, orgs);
        // ????????????id???????????? ROLE_ADMIN
        Name name = LdapUtils.newLdapName(userId + "," + userService.getBaseLdapPath().toString());
        List<Group> userGroup = (List<Group>) userService.findByMember(name);
        // ?????????????????????admin??????
        boolean adminflag = false;
        if (userGroup != null && userGroup.size() > 0) {
            for (Group group : userGroup) {
                if ("ROLE_ADMIN".equals(group.getName()) || "POWER_USER".equals(group.getName())) {
                    adminflag = true;
                    break;
                }
            }
        }
        List<ConfigDetailsQuery> configList = newConfigDao.getOrgAndVehicle(adminflag, userId, username, userOrgListId);
        map.put(VEHICLE, configList);
        return map;
    }

    @Override
    public String[] getCurOrgId() {
        String[] orgInfo = new String[2];
        boolean isAdmin = SystemHelper.isAdmin();
        String orgId = userService.getOrgUuidByUser();
        OrganizationLdap ol = userService.getOrgByUuid(orgId);
        String orgName = ol.getName();
        if (isAdmin) {
            final int maxCount = 2;
            List<OrganizationLdap> list = userService.getChildUuidAndName(userService.getOrgIdByUser(), maxCount);
            if (CollectionUtils.isNotEmpty(list)) {
                final OrganizationLdap child = list.size() > 1 ? list.get(1) : list.get(0);
                // todo ????????????????????????
                orgInfo[0] = child.getUuid();
                orgInfo[1] = child.getName();
            }
        } else {
            orgInfo[0] = orgId;
            orgInfo[1] = orgName;
        }
        return orgInfo;
    }

    @Override
    public List<Assignment> getAssignmentByConfigId(String configId) {
        return newConfigDao.getAssignmentByConfigId(configId);
    }

    @Override
    public List<Assignment> getPeopleAssignmentByConfigId(String configId) {
        return newConfigDao.getPeopleAssignmentByConfigId(configId);
    }

    @Override
    public List<Assignment> getThingAssignmentByConfigId(String configId) {
        return newConfigDao.getThingAssignmentByConfigId(configId);
    }

    @Override
    public int isBandAssignment(String id) {
        return newConfigDao.isBandAssignment(id);
    }


    @Override
    public JsonResultBean isVehicleCountExceedMaxNumber(String assignmentId, String assignmentName) {
        boolean flag = newVehicleDao.findVehicleCountByAssignment(assignmentId) >= maxNumberAssignmentMonitor;
        JSONObject msg = new JSONObject();
        if (flag) {
            msg.put("success", false);
            msg.put("msg", "???" + assignmentName + "?????????????????????????????????????????????" + maxNumberAssignmentMonitor + "???????????????????????????");
        } else {
            msg.put("success", true);
        }
        return new JsonResultBean(msg);
    }

    @Override
    public String getConfigIdByVehicleId(String vehicleId) {
        return newConfigDao.getConfigIdByVehicleId(vehicleId);
    }

    @Override
    public List<ConfigDetailsQuery> getConfigByConfigIds(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            return newConfigDao.getConfigByConfigIds(ids);
        }
        return null;
    }

    @Override
    public String findMonitorTypeById(String configId) {
        return newConfigDao.findMonitorTypeById(configId);
    }


    @Override
    public Set<String> getDeviceIdsByVIds(Set<String> vids) {
        return newConfigDao.getDeviceIdsByVIds(vids);
    }


    @Override
    public JSONObject getAllAssignmentVehicleNumber(String id, int type) {
        JSONObject result = new JSONObject();
        List<String> under = new ArrayList<>();
        List<String> overLimitAssignmentName = new ArrayList<>();
        if (StringUtils.isNotBlank(id)) {
            //??????ids
            List<String> aids = new ArrayList<>();
            if (type == 2) { //????????????
                //??????????????????
                UserLdap userId = SystemHelper.getCurrentUser();
                String uuid = userService.getUserUuidById(userId.getId().toString());
                //?????????????????????????????????
                List<OrganizationLdap> childGroup = userService.getOrgChild(id);
                List<String> groupList = new ArrayList<>();
                if (childGroup != null && !childGroup.isEmpty()) {
                    for (OrganizationLdap group : childGroup) {
                        groupList.add(group.getUuid());
                    }
                }
                //???????????????????????????????????????????????????
                List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, groupList);
                if (assignmentList != null && !assignmentList.isEmpty()) {
                    for (Assignment anAssignmentList : assignmentList) {
                        aids.add(anAssignmentList.getId());
                    }
                }
            } else { //????????????
                aids.addAll(Arrays.asList(id.split(",")));
            }
            if (aids.size() > 0) {
                // ??????????????????id??????????????????????????????
                List<AssignmentInfo> ais = newVehicleDao.getAllAssignmentVehicleNumber(aids);
                // ?????????????????????
                for (AssignmentInfo ai : ais) {
                    if (ai.getVehicleNumber() < maxNumberAssignmentMonitor) {
                        under.add(ai.getId());
                        continue;
                    }
                    overLimitAssignmentName.add(ai.getName());
                }
            }
        }
        result.put("ais", under);
        result.put("overLimitAssignmentName", overLimitAssignmentName);
        return result;
    }


    @Override
    public String getPeripherals(String vehicleId) {
        List<SensorPolling> spList = sensorPollingService.findByVehicleId(vehicleId);
        StringBuilder name = new StringBuilder();
        if (spList.size() != 0) {
            for (SensorPolling sp : spList) {
                name.append(sp.getPollingName()).append("<br>");
            }
        }
        return name.toString();
    }


    /**
     * ????????????????????????,???????????????????????????F3(????????????????????????????????????,????????????????????????????????????)
     */
    @Override
    public void sendBindToF3(String vehicleId) {
        BindDTO config = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (config != null) {
            String deviceType = config.getDeviceType();
            String simCardNumber = config.getSimCardNumber();
            String deviceNumber = config.getDeviceNumber();
            String simCardId = config.getSimCardId();
            String monitorType = config.getMonitorType();
            String deviceId = config.getDeviceNumber();
            String groupName = config.getOrgName();
            Integer plateColor = config.getPlateColor();
            // ???????????????????????????????????????F3??????????????????
            String oldIdentification;
            String identification;
            if ("8".equals(deviceType)) {
                oldIdentification = BSJFakeIPUtil.integerMobileIPAddress(simCardNumber);
                identification = BSJFakeIPUtil.integerMobileIPAddress(simCardNumber);
            } else if (ProtocolTypeUtil.checkAllDeviceType(deviceType)) {
                oldIdentification = simCardNumber;
                identification = simCardNumber;
            } else {
                oldIdentification = deviceNumber;
                identification = deviceNumber;
            }
            BindInfo bindInfo = new BindInfo();
            bindInfo.setOldDeviceId(deviceId);
            bindInfo.setDeviceId(deviceId);
            bindInfo.setOldIdentification(oldIdentification);
            bindInfo.setIdentification(identification);
            bindInfo.setDeviceType(deviceType);
            String authCode = simCardNewDao.getAuthCodeBySimId(simCardId);
            if (StringUtils.isNotEmpty(authCode)) {
                bindInfo.setAuthCode(authCode);
            }
            //??????????????????
            MonitorInfo monitorInfo;
            if ("0".equals(monitorType)) {
                //????????????
                VehicleInfo vehicleInfo = getVehicleByDevice(deviceNumber);
                //????????????????????????
                monitorInfo = handleMonitorInfo(vehicleInfo, 0);
            } else if ("1".equals(monitorType)) {
                //????????????
                PeopleInfo peopleInfo = getPeopleInfoByDevice(deviceNumber);
                //????????????????????????
                monitorInfo = handleMonitorInfo(peopleInfo, 1);
            } else {
                //????????????
                ThingInfo thingInfo = getThingInfoByDevice(deviceNumber);
                //????????????????????????
                monitorInfo = handleMonitorInfo(thingInfo, 2);
            }
            //?????????????????????????????????????????????????????????
            monitorInfo.setAuthCode(authCode);
            monitorInfo.setGroupName(groupName);
            monitorInfo.setPlateColor(plateColor); // ????????????
            MonitorInfoBuilder.buildVehicleStaticData(vehicleService.get809VehicleStaticData(vehicleId), monitorInfo);
            monitorInfo.setTerminalManufacturer(config.getTerminalManufacturer());
            monitorInfo.setTerminalType(config.getTerminalType());

            bindInfo.setOldDeviceType(deviceType);
            bindInfo.setDeviceType(deviceType);
            bindSendUtil.sendBindInfo(bindInfo, monitorInfo);
        }
    }

    @Override
    public void assembleNetWork(List<BindInfo> bindInfos) {
        List<String> deviceIds = bindInfos.stream().map(BindInfo::getDeviceId).collect(Collectors.toList());
        Map<String, BaseKvDo<String, Integer>> netWorkMap = newConfigDao.getNetWorkByDeviceIds(deviceIds);
        if (MapUtils.isEmpty(netWorkMap)) {
            return;
        }
        for (BindInfo bindInfo : bindInfos) {
            JSONObject monitorInfo = bindInfo.getMonitorInfo();
            String deviceId = monitorInfo.getString("deviceId");
            monitorInfo.put("accessNetwork", netWorkMap.get(deviceId).getFirstVal());
        }
    }

}