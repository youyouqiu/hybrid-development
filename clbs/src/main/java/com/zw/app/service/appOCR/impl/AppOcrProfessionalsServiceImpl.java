package com.zw.app.service.appOCR.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.utils.FastDFSClient;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.appOCR.AppOcrProfessionalsService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.domain.ConfigProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.impl.ProfessionalServiceImpl;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AppServerVersion
public class AppOcrProfessionalsServiceImpl implements AppOcrProfessionalsService {

    private Logger logger = LogManager.getLogger(AppOcrProfessionalsServiceImpl.class);

    @Autowired
    private NewProfessionalsDao professionalsDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private NewConfigDao configDao;

    @Autowired
    private UserService userService;

    /**
     * 获取从业人员列表
     * @param id
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = { "/clbs/app/ocr/professionals/getList" })
    public List<JSONObject> getProfessionalsList(String id) {
        List<JSONObject> result = new ArrayList<>();
        Map<String, String> configMap =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(id), Arrays.asList("professionalIds"));
        if (configMap.isEmpty()) {
            return result;
        }
        String professionalsIds = configMap.get("professionalIds");
        if (StringUtils.isEmpty(professionalsIds)) {
            return result;
        }
        String[] ids = professionalsIds.split(",");
        for (int i = 0; i < ids.length; i++) {
            String professionalsId = ids[i];
            Map<String, String> proInfo =
                RedisHelper.getHashMap(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalsId), Arrays.asList("name"));
            JSONObject object = new JSONObject();
            object.put("id", professionalsId);
            object.put("name", proInfo.get("name"));
            result.add(object);
        }
        return result;
    }

    /**
     * 获取从业人员信息
     * @param id
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = { "/clbs/app/ocr/professionals/getInfo" })
    public ProfessionalDTO getProfessionalsInfo(String id) {
        return professionalsDao.getProfessionalById(id);
    }

    /**
     * 保存从业人员信息并删除服务器之前图片
     * @param info
     * @param oldPhoto
     * @param type     1、身份证  2、驾驶证  3、从业资格证
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = { "/clbs/app/ocr/professionals/saveInfo" })
    public Map<String, String> saveProfessionalsInfo(String info, String vehicleId, String oldPhoto, Integer type)
        throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("flag", "1");
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (bindDTO == null) {
            result.put("flag", "0");
            result.put("msg", "监控对象不存在绑定关系，请确认！");
            return result;
        }
        ProfessionalDO professionalDO = JSON.parseObject(info, ProfessionalDO.class);
        JSONObject jsonObject = JSONObject.parseObject(info);
        String id = jsonObject.getString("id");
        professionalDO.setId(id);
        ProfessionalDTO professionalDTO = new ProfessionalDTO();
        BeanUtils.copyProperties(professionalDO, professionalDTO);

        ProfessionalDO professional =
            professionalsDao.findByNameExistIdentity(professionalDO.getName(), professionalDO.getIdentity());
        if (getResult(result, professional)) {
            return result;
        }
        String msg = "";
        if (StringUtils.isEmpty(id)) {
            // 新增从业人员
            if (StringUtils.isNotEmpty(professionalDTO.getIdentity())) {
                List<ProfessionalDO> professionalsByIdentity =
                    professionalsDao.getProfessionalsByIdentity(professionalDTO.getIdentity());
                if (!professionalsByIdentity.isEmpty()) {
                    result.put("flag", "0");
                    result.put("msg", "该身份证号已存在");
                    return result;
                }
            }
            addProfessional(professionalDO, bindDTO);
            return result;
        }
        // 修改
        Map<String, String> proInfo = RedisHelper.hgetAll(RedisKeyEnum.PROFESSIONAL_INFO.of(id));
        switch (type) {
            case 1:
                List<ProfessionalDO> professionalsByIdentity =
                    professionalsDao.getProfessionalsByIdentity(professionalDTO.getIdentity());
                for (ProfessionalDO professionalDO1 : professionalsByIdentity) {
                    if (!professionalDO1.getId().equals(id)) {
                        result.put("flag", "0");
                        result.put("msg", "该身份证号已存在");
                        return result;
                    }
                }

                professionalsDao.updateProfessionalsOCRIdentity(professionalDTO);
                proInfo.put("identity", professionalDTO.getIdentity());
                proInfo.put("name", professionalDTO.getName());
                proInfo.put("gender", professionalDTO.getGender());
                msg = "用户：上传从业人员身份证信息";

                List<String> professionalIds =
                    Arrays.asList(StrUtil.getOrBlank(bindDTO.getProfessionalIds()).split(","));
                List<String> professionalNames =
                    Arrays.asList(StrUtil.getOrBlank(bindDTO.getProfessionalNames()).split(","));
                int index = professionalIds.indexOf(id);
                if (index == -1) {
                    break;
                }
                professionalNames.set(index, professionalDTO.getName());
                RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "professionalNames",
                    StringUtils.join(professionalNames, ","));

                break;
            case 2:
                professionalsDao.updateProfessionalsOCRDriver(professionalDTO);
                proInfo.put("drivingLicenseNo", professionalDTO.getDrivingLicenseNo());
                proInfo.put("drivingType", professionalDTO.getDrivingType());
                proInfo.put("drivingStartDate",
                    DateUtil.getDateToString(professionalDTO.getDrivingStartDate(), DateUtil.DATE_Y_M_D_FORMAT));
                proInfo.put("drivingEndDate",
                    DateUtil.getDateToString(professionalDTO.getDrivingEndDate(), DateUtil.DATE_Y_M_D_FORMAT));
                msg = "用户：上传从业人员驾驶证信息";
                break;
            case 3:
                professionalsDao.updateProfessionalsOCRQualification(professionalDTO);
                proInfo.put("cardNumber", professionalDTO.getCardNumber());
                msg = "用户：上传从业人员从业资格证信息";
                break;
            default:
                break;
        }
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(id), proInfo);
        // 删除之前保存服务器的图片
        if (StringUtils.isNotEmpty(oldPhoto)) {
            fastDFSClient.deleteFile(oldPhoto);
        }
        logSearchService.addLog("", msg, "4", "", bindDTO.getName(), bindDTO.getPlateColorStr());
        return result;
    }

    private boolean getResult(Map<String, String> result, ProfessionalDO professional) {
        if (professional != null) {
            String id = professional.getId();
            if (checkProfessionalGroup(id)) {
                result.put("flag", "0");
                result.put("msg", "该从业人员已存在，是否与当前监控对象绑定");
                result.put("id", id);
            } else {
                result.put("flag", "0");
                result.put("msg", "该从业人员已存在于其他企业，当前用户无操作权限！");
            }
            return true;
        }
        return false;
    }

    /**
     * 校验从业人员是否在用户权限下
     * @param id
     * @return
     */
    private boolean checkProfessionalGroup(String id) {
        List<String> orgByUser = userService.getCurrentUserOrgIds();
        String orgId = RedisHelper.hget(RedisKeyEnum.PROFESSIONAL_INFO.of(id), "orgId");
        return orgByUser.contains(orgId);
    }

    /**
     * 新增从业人员
     */
    private void addProfessional(ProfessionalDO professionalDO, BindDTO bindDTO) throws Exception {
        professionalDO.setState("0"); // 设置职位状态为正常
        professionalDO.setId(UUID.randomUUID().toString());
        professionalDO.setFlag(1);
        String orgId = bindDTO.getOrgId();
        professionalDO.setOrgId(orgId);
        professionalsDao.addProfessionals(professionalDO);

        String msg = "用户：新增从业人员（" + professionalDO.getName() + "）";

        logSearchService.addLog("", msg, "4", "", bindDTO.getName(), bindDTO.getPlateColorStr());

        ProfessionalDTO professionalDTO = new ProfessionalDTO();
        BeanUtils.copyProperties(professionalDO, professionalDTO);

        //维护缓存
        Map<String, String> valueMap = ProfessionalServiceImpl.setValueToMap(professionalDTO);
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDTO.getId()), valueMap);
        //排序缓存
        RedisHelper.addToListTop(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), professionalDO.getId());
        //模糊查询缓存
        String hashKey = ProfessionalServiceImpl
            .constructFuzzySearchKey(professionalDO.getName(), professionalDO.getIdentity(), professionalDO.getState());
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), hashKey, professionalDO.getId());
        //从业人员组织redis缓存
        RedisHelper
            .addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDO.getOrgId()), professionalDO.getId());
        // 绑定从业人员到监控对象
        addProfessionalBindVehicle(bindDTO, professionalDO.getId(), professionalDO.getName());

    }

    /**
     * 增加监控对象与从业人员的绑定并维护至缓存
     */
    private void addProfessionalBindVehicle(BindDTO bindDTO, String professionalsId, String professionalsName) {
        if (bindDTO == null || StringUtils.isBlank(professionalsId)) {
            return;
        }
        String configId = bindDTO.getConfigId();
        if (StringUtils.isBlank(configId)) {
            return;
        }
        ConfigProfessionalDO from = new ConfigProfessionalDO();
        from.setId(UUID.randomUUID().toString());
        from.setConfigId(configId);
        from.setProfessionalsId(professionalsId);
        from.setCreateDataUsername(SystemHelper.getCurrentUsername());
        from.setFlag(1);
        boolean result = configDao.bindSingleProfessional(from);
        if (result) { // 新增从业人员与车辆绑定关系成功,维护缓存
            String monitorBindProfessionalId =
                monitorProfessionalInfoJoint(bindDTO.getProfessionalIds(), professionalsId);
            String monitorBindProfessionalName =
                monitorProfessionalInfoJoint(bindDTO.getProfessionalNames(), professionalsName);
            Map<String, String> proMap = new HashMap<>();
            proMap.put("professionalIds", monitorBindProfessionalId);
            proMap.put("professionalNames", monitorBindProfessionalName);
            RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(bindDTO.getId()), proMap);
        }
    }

    /**
     * 拼装从业人员id和姓名
     */
    private String monitorProfessionalInfoJoint(String sourceStr, String contentStr) {
        if (StringUtils.isEmpty(sourceStr)) {
            return contentStr;
        }
        if (sourceStr.endsWith(",")) {
            return sourceStr + contentStr;
        } else {
            return sourceStr + "," + contentStr;
        }
    }

    /**
     * 绑定从业人员
     * @param newId     从业人员新绑定id
     * @param vehicleId 车
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = { "/clbs/app/ocr/professionals/bind" })
    public Map<String, String> getProfessionalsInfo(String newId, String vehicleId) {
        Map<String, String> result = new HashMap<>();
        result.put("flag", "1");
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "professionalIds", "professionalNames");
        if (bindDTO == null) {
            result.put("flag", "0");
            result.put("msg", "监控对象不存在绑定关系，请确认！");
            return result;
        }
        String professionalsIds = bindDTO.getProfessionalIds();
        String professionalNames = bindDTO.getProfessionalNames();
        if (StringUtils.isNotEmpty(professionalsIds) && (professionalsIds.indexOf(newId) != -1)) {
            result.put("flag", "0");
            result.put("msg", "监控对象已绑定该从业人员，请确认！");
            return result;
        }
        if (!checkProfessionalGroup(newId)) {
            result.put("flag", "0");
            result.put("msg", "该从业人员已存在于其他企业，当前用户无操作权限！");
            return result;
        }
        String configId = configDao.getByMonitorId(vehicleId).getId();
        ConfigProfessionalDO professionalForConfigFrom = new ConfigProfessionalDO();
        professionalForConfigFrom.setId(UUID.randomUUID().toString());
        professionalForConfigFrom.setConfigId(configId);
        professionalForConfigFrom.setProfessionalsId(newId);
        professionalForConfigFrom.setCreateDataUsername(SystemHelper.getCurrentUsername());
        professionalForConfigFrom.setFlag(1);
        boolean flag = configDao.bindSingleProfessional(professionalForConfigFrom);
        if (flag) {
            String name = professionalsDao.getProfessionalById(newId).getName();
            Map<String, String> bindMap = new HashMap<>();
            bindMap.put("professionalIds", monitorProfessionalInfoJoint(professionalsIds, newId));
            bindMap.put("professionalNames", monitorProfessionalInfoJoint(professionalNames, name));

            RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(vehicleId), bindMap);
        } else {
            result.put("flag", "0");
            result.put("msg", "绑定从业人员异常");
        }
        return result;
    }
}
