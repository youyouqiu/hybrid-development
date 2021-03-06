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
     * ????????????????????????
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
     * ????????????????????????
     * @param id
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = { "/clbs/app/ocr/professionals/getInfo" })
    public ProfessionalDTO getProfessionalsInfo(String id) {
        return professionalsDao.getProfessionalById(id);
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param info
     * @param oldPhoto
     * @param type     1????????????  2????????????  3??????????????????
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
            result.put("msg", "????????????????????????????????????????????????");
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
            // ??????????????????
            if (StringUtils.isNotEmpty(professionalDTO.getIdentity())) {
                List<ProfessionalDO> professionalsByIdentity =
                    professionalsDao.getProfessionalsByIdentity(professionalDTO.getIdentity());
                if (!professionalsByIdentity.isEmpty()) {
                    result.put("flag", "0");
                    result.put("msg", "????????????????????????");
                    return result;
                }
            }
            addProfessional(professionalDO, bindDTO);
            return result;
        }
        // ??????
        Map<String, String> proInfo = RedisHelper.hgetAll(RedisKeyEnum.PROFESSIONAL_INFO.of(id));
        switch (type) {
            case 1:
                List<ProfessionalDO> professionalsByIdentity =
                    professionalsDao.getProfessionalsByIdentity(professionalDTO.getIdentity());
                for (ProfessionalDO professionalDO1 : professionalsByIdentity) {
                    if (!professionalDO1.getId().equals(id)) {
                        result.put("flag", "0");
                        result.put("msg", "????????????????????????");
                        return result;
                    }
                }

                professionalsDao.updateProfessionalsOCRIdentity(professionalDTO);
                proInfo.put("identity", professionalDTO.getIdentity());
                proInfo.put("name", professionalDTO.getName());
                proInfo.put("gender", professionalDTO.getGender());
                msg = "??????????????????????????????????????????";

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
                msg = "??????????????????????????????????????????";
                break;
            case 3:
                professionalsDao.updateProfessionalsOCRQualification(professionalDTO);
                proInfo.put("cardNumber", professionalDTO.getCardNumber());
                msg = "????????????????????????????????????????????????";
                break;
            default:
                break;
        }
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(id), proInfo);
        // ????????????????????????????????????
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
                result.put("msg", "????????????????????????????????????????????????????????????");
                result.put("id", id);
            } else {
                result.put("flag", "0");
                result.put("msg", "????????????????????????????????????????????????????????????????????????");
            }
            return true;
        }
        return false;
    }

    /**
     * ??????????????????????????????????????????
     * @param id
     * @return
     */
    private boolean checkProfessionalGroup(String id) {
        List<String> orgByUser = userService.getCurrentUserOrgIds();
        String orgId = RedisHelper.hget(RedisKeyEnum.PROFESSIONAL_INFO.of(id), "orgId");
        return orgByUser.contains(orgId);
    }

    /**
     * ??????????????????
     */
    private void addProfessional(ProfessionalDO professionalDO, BindDTO bindDTO) throws Exception {
        professionalDO.setState("0"); // ???????????????????????????
        professionalDO.setId(UUID.randomUUID().toString());
        professionalDO.setFlag(1);
        String orgId = bindDTO.getOrgId();
        professionalDO.setOrgId(orgId);
        professionalsDao.addProfessionals(professionalDO);

        String msg = "??????????????????????????????" + professionalDO.getName() + "???";

        logSearchService.addLog("", msg, "4", "", bindDTO.getName(), bindDTO.getPlateColorStr());

        ProfessionalDTO professionalDTO = new ProfessionalDTO();
        BeanUtils.copyProperties(professionalDO, professionalDTO);

        //????????????
        Map<String, String> valueMap = ProfessionalServiceImpl.setValueToMap(professionalDTO);
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDTO.getId()), valueMap);
        //????????????
        RedisHelper.addToListTop(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), professionalDO.getId());
        //??????????????????
        String hashKey = ProfessionalServiceImpl
            .constructFuzzySearchKey(professionalDO.getName(), professionalDO.getIdentity(), professionalDO.getState());
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), hashKey, professionalDO.getId());
        //??????????????????redis??????
        RedisHelper
            .addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDO.getOrgId()), professionalDO.getId());
        // ?????????????????????????????????
        addProfessionalBindVehicle(bindDTO, professionalDO.getId(), professionalDO.getName());

    }

    /**
     * ????????????????????????????????????????????????????????????
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
        if (result) { // ?????????????????????????????????????????????,????????????
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
     * ??????????????????id?????????
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
     * ??????????????????
     * @param newId     ?????????????????????id
     * @param vehicleId ???
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
            result.put("msg", "????????????????????????????????????????????????");
            return result;
        }
        String professionalsIds = bindDTO.getProfessionalIds();
        String professionalNames = bindDTO.getProfessionalNames();
        if (StringUtils.isNotEmpty(professionalsIds) && (professionalsIds.indexOf(newId) != -1)) {
            result.put("flag", "0");
            result.put("msg", "???????????????????????????????????????????????????");
            return result;
        }
        if (!checkProfessionalGroup(newId)) {
            result.put("flag", "0");
            result.put("msg", "????????????????????????????????????????????????????????????????????????");
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
            result.put("msg", "????????????????????????");
        }
        return result;
    }
}
