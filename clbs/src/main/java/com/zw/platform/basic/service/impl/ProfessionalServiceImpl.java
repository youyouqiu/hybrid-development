package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.adas.domain.driverStatistics.IcDriverMessages;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalShowDTO;
import com.zw.platform.basic.domain.ProfessionalsTypeDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.ProfessionalPageDTO;
import com.zw.platform.basic.dto.export.ProfessionalsExportDTO;
import com.zw.platform.basic.dto.imports.ProfessionalImportDTO;
import com.zw.platform.basic.dto.query.NewProfessionalsQuery;
import com.zw.platform.basic.imports.handler.ProfessionalsImportHandler;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import com.zw.platform.domain.basicinfo.query.IcCardDriverQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.sendTxt.ProfessionalsPhotoUpdateAck;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/***
 @Author lijie
 @Date 2020/9/28 9:45
 @Description ??????????????????redis??????
 @version 1.0
 **/
@Service
public class ProfessionalServiceImpl implements CacheService, ProfessionalService, IpAddressService {

    private Logger log = LogManager.getLogger(ProfessionalServiceImpl.class);

    private static final int IMPORT_EXCEL_CELL = 34;

    private static final Integer IDENTITY_LENGTH = 18;

    private static final String IC_TYPE = "?????????(IC???)";

    @Autowired
    NewProfessionalsDao newProfessionalsDao;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    UserService userService;

    @Autowired
    ConfigHelper configHelper;

    @Autowired
    WebClientHandleCom webClientHandleCom;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ConfigService configService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Override
    public void initCache() {
        log.info("???????????????????????????redis?????????.");
        List<ProfessionalDTO> allProfessionals = newProfessionalsDao.findAllProfessionals();
        Map<RedisKey, Map<String, String>> valueMaps = new HashMap<>();
        List<String> sortIds = new ArrayList<>();
        Map<String, String> fuzzyMap = new HashMap<>();
        Map<RedisKey, Collection<String>> orgPro = new HashMap<>();

        for (ProfessionalDTO professionalDTO : allProfessionals) {
            String id = professionalDTO.getId();
            Map<String, String> valueMap = setValueToMap(professionalDTO);
            valueMaps.put(RedisKeyEnum.PROFESSIONAL_INFO.of(id), valueMap);
            sortIds.add(id);
            //??????????????????
            String hashKey = constructFuzzySearchKey(professionalDTO.getName(), professionalDTO.getIdentity(),
                professionalDTO.getState());
            fuzzyMap.put(hashKey, id);
            RedisKey orgKey = RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDTO.getOrgId());
            if (orgPro.containsKey(orgKey)) {
                orgPro.get(orgKey).add(id);
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(id);
                orgPro.put(orgKey, ids);
            }
        }

        RedisHelper.delete(new ArrayList<>(valueMaps.keySet()));
        RedisHelper.batchAddToHash(valueMaps);
        RedisHelper.delete(new ArrayList<>(orgPro.keySet()));
        RedisHelper.batchAddToSet(orgPro);
        RedisHelper.delete(RedisKeyEnum.PROFESSIONAL_SORT_ID.of());
        RedisHelper.addToListTail(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), sortIds);
        RedisHelper.delete(RedisKeyEnum.FUZZY_PROFESSIONAL.of());
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), fuzzyMap);
        log.info("???????????????????????????redis?????????.");
    }

    /**
     * ??????????????????????????????
     * @param professionalDTO
     * @return
     * @throws Exception
     */
    @Override
    public boolean add(ProfessionalDTO professionalDTO) throws Exception {

        ProfessionalDO professionalDO = new ProfessionalDO();
        BeanUtils.copyProperties(professionalDTO, professionalDO);
        //????????????
        professionalDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        // ?????????
        professionalDO.setCreateDataTime(new Date()); // ????????????
        // ??????????????????
        newProfessionalsDao.addProfessionals(professionalDO);

        //todo ??????redis??????
        Map<String, String> valueMap = setValueToMap(professionalDTO);
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDTO.getId()), valueMap);
        //????????????
        RedisHelper.addToListTop(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), professionalDO.getId());
        //??????????????????
        String hashKey =
            constructFuzzySearchKey(professionalDO.getName(), professionalDO.getIdentity(), professionalDO.getState());
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), hashKey, professionalDO.getId());

        //??????????????????redis??????
        RedisHelper
            .addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDO.getOrgId()), professionalDO.getId());

        logSearchService
            .addLog(getIpAddress(), "?????????????????????" + professionalDTO.getName() + "( @" + professionalDTO.getOrgName() + " )",
                "3", "", "-", "");
        return true;
    }

    /**
     * ???????????????ftp
     * @param fileUrl
     * @return
     */
    @Override
    public boolean editImg(String fileUrl) {
        try {
            String[] fu = fileUrl.split("/");
            String fileName = "";
            if (fu.length > 0) {
                fileName = fu[fu.length - 1];
            }
            String filePath = getHttpServletRequest().getSession().getServletContext().getRealPath("/") + "upload/";
            FileInputStream fis = new FileInputStream(filePath + fileName);
            boolean success = FtpClientUtil
                .uploadFile(configHelper.getFtpHostClbs(), configHelper.getFtpPortClbs(), configHelper.getFtpUserName(),
                    configHelper.getFtpPassword(), configHelper.getProfessionalFtpPath(), fileName, fis);
            File picFile = new File(filePath + fileName);
            if (picFile.exists() && !picFile.delete()) {
                throw new RuntimeException("??????????????????" + fileName);
            }
            return success;
        } catch (Exception e) {
            if (!e.getClass().equals(FileNotFoundException.class)) {
                log.error("???????????????????????????ftp??????", e);
                return false;
            } else {
                return true;
            }
        }
    }

    public static Map<String, String> setValueToMap(ProfessionalDTO professionalDTO) {

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("orgId", Optional.ofNullable(professionalDTO.getOrgId()).orElse(""));
        valueMap.put("id", Optional.ofNullable(professionalDTO.getId()).orElse(""));
        valueMap.put("name", Optional.ofNullable(professionalDTO.getName()).orElse(""));
        valueMap.put("positionType", Optional.ofNullable(professionalDTO.getPositionType()).orElse(""));
        valueMap.put("identity", Optional.ofNullable(professionalDTO.getIdentity()).orElse(""));
        valueMap.put("jobNumber", Optional.ofNullable(professionalDTO.getJobNumber()).orElse(""));
        valueMap.put("cardNumber", Optional.ofNullable(professionalDTO.getCardNumber()).orElse(""));
        valueMap.put("gender", Optional.ofNullable(professionalDTO.getGender()).orElse(""));
        valueMap.put("state", Optional.ofNullable(professionalDTO.getState()).orElse(""));
        valueMap.put("photograph", Optional.ofNullable(professionalDTO.getPhotograph()).orElse(""));
        valueMap.put("phone", Optional.ofNullable(professionalDTO.getPhone()).orElse(""));
        valueMap.put("emergencyContact", Optional.ofNullable(professionalDTO.getEmergencyContact()).orElse(""));
        valueMap
            .put("emergencyContactPhone", Optional.ofNullable(professionalDTO.getEmergencyContactPhone()).orElse(""));
        valueMap.put("email", Optional.ofNullable(professionalDTO.getEmail()).orElse(""));
        if (professionalDTO.getIcCardEndDate() != null) {
            valueMap.put("icCardEndDate",
                DateUtil.formatDate(professionalDTO.getIcCardEndDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        valueMap.put("icCardAgencies", Optional.ofNullable(professionalDTO.getIcCardAgencies()).orElse(""));
        //1120???????????????????????????
        valueMap.put("nativePlace", Optional.ofNullable(professionalDTO.getNativePlace()).orElse(""));
        valueMap.put("regional", Optional.ofNullable(professionalDTO.getRegional()).orElse(""));
        valueMap.put("type", Optional.ofNullable(professionalDTO.getType()).orElse(""));
        if (professionalDTO.getBirthday() != null) {
            valueMap.put("birthday", DateUtil.formatDate(professionalDTO.getBirthday(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getHiredate() != null) {
            valueMap
                .put("hiredate", DateUtil.getDateToString(professionalDTO.getHiredate(), DateUtil.DATE_Y_M_D_FORMAT));
        }

        valueMap.put("lockType", Optional.ofNullable(professionalDTO.getLockType()).orElse(0).toString());

        valueMap.put("phoneTwo", Optional.ofNullable(professionalDTO.getPhoneTwo()).orElse(""));
        valueMap.put("phoneThree", Optional.ofNullable(professionalDTO.getPhoneThree()).orElse(""));
        valueMap.put("landline", Optional.ofNullable(professionalDTO.getLandline()).orElse(""));
        valueMap.put("drivingLicenseNo", Optional.ofNullable(professionalDTO.getDrivingLicenseNo()).orElse(""));
        valueMap.put("drivingAgencies", Optional.ofNullable(professionalDTO.getDrivingAgencies()).orElse(""));
        valueMap.put("operationNumber", Optional.ofNullable(professionalDTO.getOperationNumber()).orElse(""));
        valueMap.put("operationAgencies", Optional.ofNullable(professionalDTO.getOperationAgencies()).orElse(""));
        valueMap.put("drivingType", Optional.ofNullable(professionalDTO.getDrivingType()).orElse(""));
        if (professionalDTO.getDrivingStartDate() != null) {
            valueMap.put("drivingStartDate",
                DateUtil.getDateToString(professionalDTO.getDrivingStartDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getDrivingEndDate() != null) {
            valueMap.put("drivingEndDate",
                DateUtil.getDateToString(professionalDTO.getDrivingEndDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        valueMap.put("remindDays",
            professionalDTO.getRemindDays() == null ? "" : String.valueOf(professionalDTO.getRemindDays()));
        valueMap.put("serviceCompany", Optional.ofNullable(professionalDTO.getServiceCompany()).orElse("")); // ????????????
        valueMap.put("qualificationCategory",
            Optional.ofNullable(professionalDTO.getQualificationCategory()).orElse("")); // ??????????????????
        if (professionalDTO.getIssueCertificateDate() != null) {
            valueMap.put("issueCertificateDate", DateUtil
                .getDateToString(professionalDTO.getIssueCertificateDate(), DateUtil.DATE_Y_M_D_FORMAT)); // ????????????
        }
        valueMap.put("address", Optional.ofNullable(professionalDTO.getAddress()).orElse("")); // ??????
        valueMap
            .put("identityCardPhoto", Optional.ofNullable(professionalDTO.getIdentityCardPhoto()).orElse("")); // ???????????????
        valueMap.put("faceId", Optional.ofNullable(professionalDTO.getFaceId()).orElse(""));//??????id
        valueMap.put("nationId", Optional.ofNullable(professionalDTO.getNationId()).orElse(""));
        valueMap.put("educationId", Optional.ofNullable(professionalDTO.getEducationId()).orElse(""));
        return valueMap;
    }

    /**
     * ??????????????????????????????????????????
     * @param id
     * @return
     */
    @Override
    public boolean isBind(String id) {
        Boolean containsKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(id));
        List<String> bandIds = newProfessionalsDao.getBindVehicleIds(id);
        if (containsKey || !bandIds.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * ??????????????????
     */
    @MethodLog(name = "??????????????????", description = "??????????????????")
    @Override
    public boolean deleteProfessionalsById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            ProfessionalDTO professionalDTO = newProfessionalsDao.getProfessionalById(id);
            if (professionalDTO == null) {
                return false;
            }
            // ??????????????????
            if (newProfessionalsDao.deleteProfessionalsById(id)) {
                //??????fastDFS???????????????
                if (!deleteProPhotos(professionalDTO)) {
                    throw new BusinessException("", "???????????????????????????fastDFS??????????????????????????????????????????????????????????????????");
                }
            }
            //??????ftp????????????
            if (professionalDTO.getPhotograph() != null) {
                String[] files = professionalDTO.getPhotograph().split("/");
                if (files.length == 0 || !delPic(files[files.length - 1])) {
                    throw new BusinessException("", "??????????????????????????????");
                }
            }
            // ??????????????????????????????map???????????????????????????key?????????????????????????????????
            Map<String, List<String>> orgIdMap = new HashMap<>();
            orgIdMap.put(professionalDTO.getOrgId(), Collections.singletonList(id));
            //??????????????????
            String fuzzyHashKey = constructFuzzySearchKey(professionalDTO.getName(), professionalDTO.getIdentity(),
                professionalDTO.getState());
            removeProFromRedis(Collections.singletonList(id), orgIdMap, Collections.singletonList(fuzzyHashKey));
            OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(professionalDTO.getOrgId());
            String msg =
                "??????????????????:" + professionalDTO.getName() + "( @" + (organizationLdap != null ? organizationLdap.getName() :
                    "") + " )";
            logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        }
        return true;
    }

    /**
     * ???????????????????????????redis??????
     * @param ids
     * @param orgIdMap
     * @param fuzzyIds
     */
    private void removeProFromRedis(Collection<String> ids, Map<String, List<String>> orgIdMap,
        Collection<String> fuzzyIds) {
        //???????????????redis??????
        for (String id : ids) {
            RedisHelper.delete(RedisKeyEnum.PROFESSIONAL_INFO.of(id));
        }
        RedisHelper.delListItem(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), ids);
        RedisHelper.hdel(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), new ArrayList<>(fuzzyIds));
        for (Map.Entry<String, List<String>> entry : orgIdMap.entrySet()) {
            RedisHelper.delSetItem(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(entry.getKey()), entry.getValue());
        }
    }

    /**
     * ???????????????????????????fastDFS????????????????????????????????????????????????????????????
     */
    private boolean deleteProPhotos(ProfessionalDTO professionalDTO) {
        try {
            if (professionalDTO != null) {
                String identityCardPhoto = professionalDTO.getIdentityCardPhoto();
                if (identityCardPhoto != null && !("").equals(identityCardPhoto)) {
                    fastDFSClient.deleteFile(identityCardPhoto);
                }
                String qualificationCertificatePhoto = professionalDTO.getQualificationCertificatePhoto();
                if (qualificationCertificatePhoto != null && !("").equals(qualificationCertificatePhoto)) {
                    fastDFSClient.deleteFile(qualificationCertificatePhoto);
                }
                String driverLicensePhoto = professionalDTO.getDriverLicensePhoto();
                if (driverLicensePhoto != null && !("").equals(driverLicensePhoto)) {
                    fastDFSClient.deleteFile(driverLicensePhoto);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("???????????????????????????fastDFS?????????????????????????????????????????????????????????????????????", e);
            return false;
        }
    }

    private boolean delPic(String fileName) {
        try {
            FTPClient ftpClient = FtpClientUtil
                .getFTPClient(configHelper.getFtpUserName(), configHelper.getFtpPassword(),
                    configHelper.getFtpHostClbs(), configHelper.getFtpPortClbs(),
                    configHelper.getProfessionalFtpPath());
            ftpClient.deleteFile(fileName);
            return true;
        } catch (Exception e) {
            log.error("???FTP??????????????????????????????", e);
            return false;
        }
    }

    /**
     * ??????????????????????????????
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteBindProfessional(String id) throws Exception {

        if (!deleteProfessionalsById(id)) {
            return false;
        }

        removeProfessionalBind(id);
        removeDriver(id);
        return true;
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param id
     */
    private void removeProfessionalBind(String id) {
        List<String> bandVehicleIds = newProfessionalsDao.getBindVehicleIds(id);
        //???????????????
        bandVehicleIds = bandVehicleIds.stream().filter(e -> e != null).collect(Collectors.toList());
        if (bandVehicleIds.size() > 0) {
            newProfessionalsDao.deleteBindInfos(id);
            for (String vid : bandVehicleIds) {
                // ????????????????????????????????????????????????
                BindDTO oldConfig = MonitorUtils.getBindDTO(vid);
                if (oldConfig == null) {
                    continue;
                }
                String[] professionalIds = oldConfig.getProfessionalIds().split(",");
                String[] professionalNames = oldConfig.getProfessionalNames().split(",");
                StringBuilder ids = new StringBuilder();
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < professionalIds.length; i++) {
                    if (professionalIds[i].equals(id)) {
                        continue;
                    }
                    ids.append(professionalIds[i]).append(",");
                    names.append(professionalNames[i]).append(",");
                }

                Map<String, String> proMap = new HashMap<>();
                proMap.put("professionalIds", ids.toString());
                proMap.put("professionalNames", names.toString());
                RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(vid), proMap);

            }
            for (String bandVehicleId : bandVehicleIds) {
                ZMQFencePub.pubChangeFence("1," + bandVehicleId);
            }
        }
    }

    /**
     * ?????????????????????????????????
     * @param id
     */
    private void removeDriver(String id) {
        //?????????????????????????????????????????????
        String isDrivering = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(id));

        //???????????????????????????????????????????????????
        if (isDrivering != null) {
            String[] res = isDrivering.split(",");
            String vid = res[2];
            String cardIdAndName = res[0];
            String lastDriverValue =
                RedisHelper.getString(HistoryRedisKeyEnum.LAST_DRIVER.of(vid)) + ",c_" + cardIdAndName + ",t_" + System
                    .currentTimeMillis();
            RedisHelper.setString(HistoryRedisKeyEnum.LAST_DRIVER.of(vid), lastDriverValue);

            RedisHelper.delete(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vid));
            RedisHelper.delete(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(id));

            // ?????????id??????????????????
            BindDTO bindDTO = configService.getByMonitorId(vid);
            if (bindDTO == null) {
                return;
            }
            // ???f3???????????????f3?????????????????????finlk
            IcDriverMessages icDriverMessages = new IcDriverMessages();
            icDriverMessages.setTime(DateUtil.getDateToString(new Date(), "yyMMddHHmmss"));
            icDriverMessages.setStatus(2);
            T808Message messages =
                MsgUtil.get808Message(bindDTO.getSimCardNumber(), ConstantUtil.T808_DRIVER_INFO, 0, icDriverMessages);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(messages, ConstantUtil.T808_DRIVER_INFO, bindDTO.getDeviceId());
        }
    }

    /**
     * ????????????????????????
     * @param ids
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteProfessionalsByBatch(String ids) throws Exception {
        if (StringUtils.isEmpty(ids)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

        List<String> proIds = Arrays.asList(ids.split(","));
        Set<String> configBindIds = newProfessionalsDao.getBindIds(proIds);
        List<String> bindIds = new ArrayList<>();
        List<String> isNotBandIds = new ArrayList<>();
        int before = proIds.size();
        Iterator<String> it = proIds.iterator();
        //??????????????????????????????
        boolean isDriver;
        while (it.hasNext()) {
            String next = it.next();
            isDriver = RedisHelper.isContainsKey(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(next));
            if (isDriver || configBindIds.contains(next)) {
                bindIds.add(next);
            } else {
                isNotBandIds.add(next);
            }
        }
        if (isNotBandIds.size() == before) {
            return deleteNotBindProfessionals(isNotBandIds);
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bandPids", JSON.toJSONString(bindIds));
            JsonResultBean re =
                new JsonResultBean("??????????????????????????????????????????????????????????????????????????????????????????IC??????????????????????????????" + "??????????????????????????????????????????????????????", jsonObject);
            if (isNotBandIds.size() != 0) {
                deleteNotBindProfessionals(isNotBandIds);
            }
            return re;
        }

    }

    private JsonResultBean deleteNotBindProfessionals(List<String> isNotBandIds) {
        StringBuilder msg = new StringBuilder();
        Map<String, List<String>> orgMap = new HashMap<>();
        List<String> fuzzyIds = new ArrayList<>();
        List<ProfessionalDTO> professionalDTOs = newProfessionalsDao.findProfessionalsByIds(isNotBandIds);
        newProfessionalsDao.deleteProfessionalsByBatch(isNotBandIds);
        for (ProfessionalDTO professionalDTO : professionalDTOs) {
            // ?????????id??????key???????????????????????????????????????????????????
            OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(professionalDTO.getOrgId());
            String orgId = professionalDTO.getOrgId();
            msg.append("?????????????????? : ").append(professionalDTO.getName()).append(" ( @")
                .append(organizationLdap != null ? organizationLdap.getName() : "").append(" ) <br/>");
            //??????????????????fastDFS??????????????????
            deleteProPhotos(professionalDTO);

            if (orgMap.containsKey(orgId)) {
                List<String> ids = orgMap.get(orgId);
                ids.add(professionalDTO.getId());
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(professionalDTO.getId());
                orgMap.put(orgId, ids);
            }
            fuzzyIds.add(constructFuzzySearchKey(professionalDTO.getName(), professionalDTO.getIdentity(),
                professionalDTO.getState()));
        }
        // ????????????redis???????????????
        removeProFromRedis(isNotBandIds, orgMap, fuzzyIds);

        // ????????????
        logSearchService.addLog(getIpAddress(), msg.toString(), "3", "batch", "????????????????????????");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ?????????????????????????????????
     * @param ids
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean deleteMoreBindProfessional(String ids) throws Exception {
        List<String> proIds = JSONObject.parseArray(ids, String.class);
        JsonResultBean re = deleteNotBindProfessionals(proIds);
        for (String id : proIds) {
            removeProfessionalBind(id);
            removeDriver(id);
        }
        return re;
    }

    /**
     * ???????????????????????????????????????
     * @param id
     * @return
     */
    @Override
    public ProfessionalDTO editPageData(String id) {
        ProfessionalDTO professionalDTO = newProfessionalsDao.getProfessionalById(id);
        if (professionalDTO.getPhotograph() != null && !("").equals(professionalDTO.getPhotograph()) && !professionalDTO
            .getPhotograph().equals("0")) {
            professionalDTO.setPhotograph(configHelper.getProfessionalUrl(professionalDTO.getPhotograph()));
        }

        professionalDTO.setIdentityCardPhoto(fastDFSClient.getAccessUrl(professionalDTO.getIdentityCardPhoto()));

        professionalDTO.setDriverLicensePhoto(fastDFSClient.getAccessUrl(professionalDTO.getDriverLicensePhoto()));

        professionalDTO.setQualificationCertificatePhoto(
            fastDFSClient.getAccessUrl(professionalDTO.getQualificationCertificatePhoto()));

        OrganizationLdap organization = organizationService.getOrganizationByUuid(professionalDTO.getOrgId());
        professionalDTO.setOrgName(organization == null ? "" : organization.getName());
        return professionalDTO;
    }

    /**
     * ??????????????????
     * @param professionalDTO
     * @return
     */
    @Override
    public boolean checkEditProfessional(ProfessionalDTO professionalDTO) throws Exception {
        if (professionalDTO.getPhotograph() != null && !("").equals(professionalDTO.getPhotograph()) && !professionalDTO
            .getPhotograph().equals("0")) {
            if (editImg(professionalDTO.getPhotograph())) {
                String mediaServer = "";
                if (configHelper.isSslEnabled()) {
                    mediaServer = "/mediaserver";
                }
                professionalDTO.setPhotoAddress(professionalDTO.getPhotograph());
                professionalDTO.setPhotograph(
                    professionalDTO.getPhotograph().split(mediaServer + configHelper.getProfessionalFtpPath())[1]);
            } else {
                return false;
            }
        }

        ProfessionalDTO oldProfessionalDTO = newProfessionalsDao.getProfessionalById(professionalDTO.getId());
        if (professionalDTO.getIdentityCardPhoto() != null && !("").equals(professionalDTO.getIdentityCardPhoto())) {
            professionalDTO.setIdentityCardPhoto(oldProfessionalDTO.getIdentityCardPhoto());
        }
        if (professionalDTO.getDriverLicensePhoto() != null && !("").equals(professionalDTO.getDriverLicensePhoto())) {
            professionalDTO.setDriverLicensePhoto(oldProfessionalDTO.getDriverLicensePhoto());
        }
        if (professionalDTO.getQualificationCertificatePhoto() != null && !("")
            .equals(professionalDTO.getQualificationCertificatePhoto())) {
            professionalDTO.setQualificationCertificatePhoto(oldProfessionalDTO.getQualificationCertificatePhoto());
        }
        return true;
    }

    /**
     * ?????????????????????????????????
     */
    @MethodLog(name = "???????????????????????????????????????", description = "???????????????????????????????????????")
    @Override
    public boolean updateProGroupByProId(ProfessionalDTO newProfessionalDTO, ProfessionalDTO oldProfessionalDTO)
        throws Exception {
        // ?????????????????????id??????key???????????????????????????(??????????????????),????????????????????????
        String professionalId = newProfessionalDTO.getId();

        Map<String, String> proMap = RedisHelper.hgetAll(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalId));
        ProfessionalDTO redisProfessionalDTO = JSONObject.parseObject(JSON.toJSONString(proMap), ProfessionalDTO.class);

        //??????LockType ???????????????????????????  ??????????????????
        if (newProfessionalDTO.getLockType() == 1) {
            newProfessionalDTO.setName(redisProfessionalDTO.getName());
            newProfessionalDTO.setCardNumber(redisProfessionalDTO.getCardNumber());
            newProfessionalDTO.setIcCardAgencies(redisProfessionalDTO.getIcCardAgencies());
            newProfessionalDTO.setIcCardEndDate(redisProfessionalDTO.getIcCardEndDate());
            newProfessionalDTO.setIdentity(redisProfessionalDTO.getIdentity());
            newProfessionalDTO.setDrivingLicenseNo(redisProfessionalDTO.getDrivingLicenseNo());
            newProfessionalDTO.setPositionType(redisProfessionalDTO.getPositionType());
        }

        //????????????
        ProfessionalDO professionalDO = new ProfessionalDO();
        BeanUtils.copyProperties(newProfessionalDTO, professionalDO);
        professionalDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        professionalDO.setUpdateDataTime(new Date());
        boolean prFlag = newProfessionalsDao.updateProfessionals(professionalDO);

        if (!prFlag) {
            return false;
        }

        //???????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????9506
        if (StringUtils.isNotBlank(newProfessionalDTO.getPhotograph()) && !newProfessionalDTO.getPhotograph()
            .equals(oldProfessionalDTO.getPhotograph())) {
            send9506Message(newProfessionalDTO);
        }
        // ???????????????map????????????????????????key
        updateProMapAndFuzzySearch(newProfessionalDTO);
        // ???????????????key
        updateOrgKey(newProfessionalDTO, oldProfessionalDTO);

        //???????????????????????????????????????
        addLogAndSendChange(newProfessionalDTO, oldProfessionalDTO, professionalId);
        return true;
    }

    /**
     * ???????????????????????????????????????
     * @param newProfessionalDTO
     * @param oldProfessionalDTO
     * @param professionalId
     */
    private void addLogAndSendChange(ProfessionalDTO newProfessionalDTO, ProfessionalDTO oldProfessionalDTO,
        String professionalId) {
        List<String> bindVehicleIds = newProfessionalsDao.getBindVehicleIds(professionalId);
        webClientHandleCom.send1607ByUpdateProfessionalBySiChuanProtocol(bindVehicleIds, newProfessionalDTO);
        StringBuilder msg = new StringBuilder();
        if (newProfessionalDTO != null) {
            String previousName = oldProfessionalDTO.getName(); // ??????????????????
            String nowName = newProfessionalDTO.getName(); // ???????????????
            String previousGroup = oldProfessionalDTO.getOrgName();
            // ???????????????
            String nowGroup = newProfessionalDTO.getOrgName();
            // ???????????????
            if (!(previousName.equals(nowName))) {
                //??????config??????
                updateConfigCache(professionalId, previousName, nowName);
                OrganizationLdap org = organizationService.getOrganizationByUuid(previousGroup);
                msg.append("?????????????????? : ").append(previousName).append(" ( @").append(org.getName()).append(" ) ????????? : ")
                    .append(nowName).append(" ( @").append(nowGroup).append(" )");
                logSearchService.addLog(getIpAddress(), msg.toString(), "3", "", "-", "");

                //???finlk???????????????????????????????????????????????????????????????
                List<String> bandVehicleIds = newProfessionalsDao.getBindVehicleIds(professionalId);
                for (String vid : bandVehicleIds) {
                    ZMQFencePub.pubChangeFence("1," + vid);
                }
            } else {
                msg.append("?????????????????? : ").append(nowName).append(" ( @").append(nowGroup).append(" ) ");
                logSearchService.addLog(getIpAddress(), msg.toString(), "3", "", "-", "");
            }
        }
    }

    /**
     * ???????????????????????????????????????
     * @param newProfessionalDTO
     */
    public static void updateProMapAndFuzzySearch(ProfessionalDTO newProfessionalDTO) {
        Map<String, String> oldProfessionalMap =
            RedisHelper.hgetAll(RedisKeyEnum.PROFESSIONAL_INFO.of(newProfessionalDTO.getId()));
        RedisHelper.delete(RedisKeyEnum.PROFESSIONAL_INFO.of(newProfessionalDTO.getId()));
        Map<String, String> professionalMap = setValueToMap(newProfessionalDTO);
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(newProfessionalDTO.getId()), professionalMap);

        String oldName = oldProfessionalMap.get("name");
        String oldIdentity = oldProfessionalMap.get("identity");
        String oldState = oldProfessionalMap.get("state");
        String oldFuzzyKey = constructFuzzySearchKey(oldName, oldIdentity, oldState);

        String newName = newProfessionalDTO.getName();
        String newIdentity = newProfessionalDTO.getIdentity();
        String newState = newProfessionalDTO.getState();
        String newFuzzyKey = constructFuzzySearchKey(newName, newIdentity, newState);

        if (!oldFuzzyKey.equals(newFuzzyKey)) {
            RedisHelper.hdel(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), oldFuzzyKey);
            RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), newFuzzyKey, newProfessionalDTO.getId());
        }
    }

    /**
     * ??????????????????
     * @param newProfessionalDTO
     * @param oldProfessionalDTO
     */
    private void updateOrgKey(ProfessionalDTO newProfessionalDTO, ProfessionalDTO oldProfessionalDTO) {

        if (!newProfessionalDTO.getOrgId().equals(oldProfessionalDTO.getOrgId())) {
            RedisHelper.delSetItem(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(oldProfessionalDTO.getOrgId()),
                oldProfessionalDTO.getId());
            RedisHelper.addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(newProfessionalDTO.getOrgId()),
                newProfessionalDTO.getId());
        }
    }

    /**
     * ???????????????????????????????????????config?????????
     * @param professionalId
     * @param previousName
     * @param nowName
     */
    private void updateConfigCache(String professionalId, String previousName, String nowName) {
        Set<String> monitorIdSet = newProfessionalsDao.findBandMonitorIdByProfessionalId(professionalId);
        if (CollectionUtils.isEmpty(monitorIdSet)) {
            return;
        }
        List<Map<String, String>> configs = RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(monitorIdSet));
        Map<RedisKey, Map<String, String>> updateConfigs = new HashMap<>(16);
        for (Map<String, String> configMap : configs) {
            String monitorId = configMap.get("id");
            StringBuilder professionalNames = new StringBuilder(configMap.get("professionalNames"));
            if (StringUtils.isBlank(professionalNames.toString())) {
                continue;
            }
            String[] professionalNameList = professionalNames.toString().split(",");
            professionalNames = new StringBuilder();
            for (String professionalName : professionalNameList) {
                if (professionalName.equals(previousName)) {
                    professionalNames.append(nowName).append(",");
                    continue;
                }
                professionalNames.append(professionalName).append(",");
            }
            configMap.put("professionalNames", professionalNames.substring(0, professionalNames.length() - 1));
            updateConfigs.put(RedisKeyEnum.MONITOR_INFO.of(monitorId), configMap);
        }
        RedisHelper.batchAddToHash(updateConfigs);
    }

    /**
     * ??????9506 ??????  ????????????????????????
     * @param professionalDTO ????????????
     */
    public void send9506Message(ProfessionalDTO professionalDTO) {
        List<Map<String, String>> configs = newProfessionalsDao.getDeviceIdByPid(professionalDTO.getId());
        //?????????????????????????????????
        String result =
            RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(professionalDTO.getId()));
        if (StringUtils.isNotBlank(result)) {
            String vid = result.split(",")[2];
            // ?????????????????????????????????
            configs.addAll(configService.getConfigByVehicle(vid));
        }
        //?????????????????????????????????????????????????????????????????????????????????
        if (CollectionUtils.isEmpty(configs)) {
            log.info("0x9506?????????????????????????????????????????????????????????????????????????????????");
            return;
        }
        if (StringUtils.isNotBlank(professionalDTO.getCardNumber()) && professionalDTO.getCardNumber().length() > 20) {
            log.info("0x9506???????????????????????????????????????????????????");
            return;
        }
        String version = professionalDTO.getPhotograph().split("_")[1].split("\\.")[0];
        ProfessionalsPhotoUpdateAck ack = ProfessionalsPhotoUpdateAck
            .getInstance(professionalDTO.getCardNumber(), version, professionalDTO.getPhotoAddress());
        for (Map<String, String> map : configs) {
            String vehicleId = map.get("vehicleId");
            String deviceId = map.get("deviceId");
            String simCardNumber = map.get("simCardNumber");
            String deviceType = map.get("deviceType");
            //9506 ????????????????????? ???????????????  ??????~~~
            if (!ProtocolTypeUtil.BEI_JING_PROTOCOL_808_2019.equals(deviceType)) {
                log.info("0x9506???????????????9506???????????????????????????????????????");
                continue;
            }
            //???????????????
            Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, "");
            if (msgSN == null) { // ???????????????
                log.info("0x9506??????????????????????????????");
                continue;
            }
            String logMessage =
                "???????????????" + professionalDTO.getName() + "?????????????????????" + professionalDTO.getCardNumber() + "??????????????????????????????";
            log.info(">====????????????0x9506====<" + logMessage);
            //??????9506??????  ????????????????????????
            T808Message message =
                MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_UP_CTRL_MSG_PHOTO_UPDATE_REQ, msgSN, ack);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(message, ConstantUtil.T808_UP_CTRL_MSG_PHOTO_UPDATE_REQ, deviceId);
        }
    }

    public static String constructFuzzySearchKey(String name, String identity, String state) {
        StringBuilder filedStringBuilder = new StringBuilder();
        if (!StringUtil.isNullOrBlank(name)) {
            filedStringBuilder.append(name).append("_");
        }
        if (!StringUtil.isNullOrBlank(identity)) {
            filedStringBuilder.append(identity).append("_");
        }
        if (!StringUtil.isNullOrBlank(state)) {
            if ("0".equals(state)) {
                filedStringBuilder.append("??????").append("_");
            } else if ("1".equals(state)) {
                filedStringBuilder.append("??????").append("_");
            } else if ("2".equals(state)) {
                filedStringBuilder.append("??????").append("_");
            }
        }
        return filedStringBuilder.toString().substring(0, filedStringBuilder.toString().length() - 1);
    }


    /**
     * ????????????????????????
     * @param query
     * @return
     * @throws Exception
     */
    @Override
    public Page<ProfessionalPageDTO> getListPage(NewProfessionalsQuery query) throws Exception {

        List<String> professionalsSort = RedisHelper.getList(RedisKeyEnum.PROFESSIONAL_SORT_ID.of());
        List<String> orgList = new ArrayList<>();
        RedisKey exportKey = RedisKeyEnum.USER_PROFESSIONAL_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        RedisHelper.delete(exportKey);
        if (StringUtils.isNotEmpty(query.getOrgId())) {
            orgList.add(query.getOrgId());
        } else {
            String userId = SystemHelper.getCurrentUser().getId().toString();
            orgList.addAll(organizationService.getOrgUuidsByUser(userId));
        }
        Set<String> orgProfessionals = RedisHelper.batchGetSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.ofs(orgList));
        // ????????????id??????????????????
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();

        List<String> professionals = new ArrayList<>();

        //??????????????????????????????????????????
        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            List<Map.Entry<String, String>> searchMap =
                RedisHelper.hscan(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), getFuzztPattern(query.getSimpleQueryParam()));
            Set<String> searchIds = new HashSet<>();
            for (Map.Entry<String, String> entry : searchMap) {
                searchIds.add(entry.getValue());
            }
            orgProfessionals.retainAll(searchIds);
        }

        //??????
        for (String pid : professionalsSort) {
            if (orgProfessionals.contains(pid)) {
                professionals.add(pid);
            }
        }

        //??????????????????????????????ID??????REDIS?????????????????????
        RedisHelper.addToList(exportKey, professionals);
        int listSize = professionals.size();
        // ?????????
        int curPage = query.getPage().intValue();
        // ????????????
        int pageSize = query.getLimit().intValue();
        // ??????????????????
        int lst = (curPage - 1) * pageSize;
        // ????????????
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);

        List<String> pageIds = professionals.subList(lst, ps);
        if (pageIds.isEmpty()) {
            return new Page<>();
        }

        // ?????????????????????????????????????????????,??????redis????????????
        List<ProfessionalDTO> pageList = newProfessionalsDao.findProfessionalsByIds(pageIds);
        List<ProfessionalPageDTO> professionalPageDTOS = new ArrayList<>();

        for (ProfessionalDTO professionalDTO : pageList) {
            professionalDTO.setOrgName(orgMap.get(professionalDTO.getOrgId()));
            professionalPageDTOS.add(setPageProfessionalDTO(professionalDTO));
        }
        Page<ProfessionalPageDTO> page = RedisQueryUtil.getListToPage(professionalPageDTOS, query, listSize);
        return page;
    }

    public static String getFuzztPattern(String search) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("*").append(search).append("*");
        return stringBuffer.toString();
    }

    /**
     * ??????????????????????????????
     * @param professionalDTO
     * @return
     */
    private ProfessionalPageDTO setPageProfessionalDTO(ProfessionalDTO professionalDTO) {
        ProfessionalPageDTO professionalPageDTO = new ProfessionalPageDTO();
        BeanUtils.copyProperties(professionalDTO, professionalPageDTO);

        if (professionalDTO.getHiredate() != null) {
            professionalPageDTO
                .setHiredate(DateUtil.formatDate(professionalDTO.getHiredate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getIssueCertificateDate() != null) {
            professionalPageDTO.setIssueCertificateDate(
                DateUtil.formatDate(professionalDTO.getIssueCertificateDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getIcCardEndDate() != null) {
            professionalPageDTO
                .setIcCardEndDate(DateUtil.formatDate(professionalDTO.getIcCardEndDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getBirthday() != null) {
            professionalPageDTO
                .setBirthday(DateUtil.formatDate(professionalDTO.getBirthday(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getDrivingStartDate() != null) {
            professionalPageDTO.setDrivingStartDate(
                DateUtil.formatDate(professionalDTO.getDrivingStartDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }
        if (professionalDTO.getDrivingEndDate() != null) {
            professionalPageDTO.setDrivingEndDate(
                DateUtil.formatDate(professionalDTO.getDrivingEndDate(), DateUtil.DATE_Y_M_D_FORMAT));
        }

        if (professionalDTO.getPhotograph() != null && !professionalDTO.getPhotograph().equals("")) {
            String fileName = professionalDTO.getPhotograph();
            String mediaServer = configHelper.getMediaServer();
            if (configHelper.isSslEnabled()) {
                mediaServer = "/mediaserver";
            }
            professionalPageDTO.setPhotograph(mediaServer + configHelper.getProfessionalFtpPath() + fileName);
        }

        professionalPageDTO.setIdentityCardPhoto(fastDFSClient.getAccessUrl(professionalDTO.getIdentityCardPhoto()));

        professionalPageDTO.setQualificationCertificatePhoto(
            fastDFSClient.getAccessUrl(professionalDTO.getQualificationCertificatePhoto()));

        professionalPageDTO.setDriverLicensePhoto(fastDFSClient.getAccessUrl(professionalDTO.getDriverLicensePhoto()));

        if (StringUtils.isNotEmpty(professionalDTO.getNationId())) {
            professionalPageDTO.setNation(cacheManger.getDictionaryValue(professionalDTO.getNationId()));
        }

        if (StringUtils.isNotEmpty(professionalDTO.getEducationId())) {
            professionalPageDTO.setEducation(cacheManger.getDictionaryValue(professionalDTO.getEducationId()));
        }

        return professionalPageDTO;
    }

    /**
     * ????????????
     */
    @Override
    @MethodLog(name = "????????????", description = "????????????")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // ??????
        headList.add("??????(??????)");
        // headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????(yyyy-MM-dd)");
        headList.add("??????");
        headList.add("??????");
        headList.add("??????????????????");
        headList.add("??????????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("???????????????");
        headList.add("??????");
        headList.add("??????(yyyy-MM-dd)");
        headList.add("????????????");
        headList.add("??????");
        headList.add("??????");
        headList.add("????????????");
        headList.add("??????1");
        headList.add("??????2");
        headList.add("??????3");
        headList.add("??????");
        headList.add("???????????????");
        headList.add("?????????????????????");
        headList.add("??????");
        headList.add("??????");

        headList.add("????????????(???????????????64)");
        headList.add("?????????????????????(???????????????128)");
        headList.add("????????????(???????????????64)");
        headList.add("?????????????????????(???????????????128)");

        headList.add("????????????");
        headList.add("??????????????????(yyyy-MM-dd)");
        headList.add("??????????????????(yyyy-MM-dd)");
        headList.add("??????????????????(?????????,?????????1-4???)");

        // ????????????
        requiredList.add("??????(??????)");
        // requiredList.add("??????1(??????)");
        // requiredList.add("????????????");
        // requiredList.add("????????????");
        // requiredList.add("??????");
        // ????????????????????????
        exportList.add("??????");
        // exportList.add(Converter.toBlank(userService.getOrganizationById(userService.getOrgIdByUser()).getName()));
        exportList.add("0101010101010110"); // ????????????
        // ???????????????????????????,??????????????????
        List<ProfessionalsTypeDO> professionalsTypeDOS = newProfessionalsDao.findAllProfessionalsType();
        if (!CollectionUtils.isEmpty(professionalsTypeDOS)) {
            exportList.add(professionalsTypeDOS.get(0).getProfessionalstype());
        } else {
            exportList.add("????????????????????????,??????????????????????????????!");
        }
        exportList.add("521322198301124354");
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add("??????");
        exportList.add("0001");
        exportList.add("62268962448662445586");
        exportList.add("01001");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("???");
        exportList.add(Converter.toString(new SimpleDateFormat("yyyy-MM-dd").parse("1983-01-12"), "yyyy-MM-dd"));
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("18725719882");
        exportList.add("18725719883");
        exportList.add("18725719884");
        exportList.add("02385556666");
        exportList.add("?????????");
        exportList.add("18084095168");
        exportList.add("5646859@qq.com");
        exportList.add("???????????????");

        exportList.add("520131268708761234");
        exportList.add("XXXX???????????????");
        exportList.add("430121198808081234");
        exportList.add("XX???XX??????????????????????????????");

        exportList.add("A1(????????????)");
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add(5);

        // ?????????????????????map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // ??????
        String[] sex = { "???", "???" };
        selectMap.put("??????", sex);
        //??????
        String[] state = { "??????", "??????", "??????" };
        selectMap.put("??????", state);
        // ????????????
        String[] jobType = new String[professionalsTypeDOS.size() + 1];
        jobType[0] = "?????????????????????";
        for (int i = 0; i < professionalsTypeDOS.size(); i++) {
            jobType[i + 1] = professionalsTypeDOS.get(i).getProfessionalstype();
        }
        selectMap.put("????????????", jobType);

        List<DictionaryDO> nation = cacheManger.getDictionaryList(DictionaryType.NATION);
        selectMap.put("??????", nation.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        List<DictionaryDO> education = cacheManger.getDictionaryList(DictionaryType.EDUCATION);
        selectMap.put("????????????", education.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        //????????????
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        out.close();
        return true;
    }

    /**
     * ????????????????????????
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean importProfessionals(MultipartFile multipartFile) throws Exception {
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        short lastCellNum = importExcel.getRow(0).getLastCellNum();
        if (lastCellNum < IMPORT_EXCEL_CELL) {
            throw new BusinessException("", "???????????????????????????????????????!");
        }
        // excel ????????? list
        List<ProfessionalImportDTO> excelDataList = importExcel.getDataListNew(ProfessionalImportDTO.class);
        if (CollectionUtils.isEmpty(excelDataList)) {
            throw new BusinessException("", "????????????????????????????????????!");
        }

        List<ProfessionalDO> professionalDOs = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        checkImportData(excelDataList, professionalDOs, message);

        for (ProfessionalImportDTO professional : excelDataList) {
            if (StringUtils.isNotBlank(professional.getErrorMsg())) {
                // ?????????????????????
                ImportErrorUtil.putDataToRedis(excelDataList, ImportModule.PROFESSIONAL);
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        }

        ProfessionalsImportHandler handler =
            new ProfessionalsImportHandler(newProfessionalsDao, excelDataList, professionalDOs);

        final String username = SystemHelper.getCurrentUsername();
        try (ImportCache ignored = new ImportCache(ImportModule.PROFESSIONAL, username, handler)) {
            final JsonResultBean jsonResultBean = handler.execute();
            if (!jsonResultBean.isSuccess()) {
                ImportErrorUtil.putDataToRedis(excelDataList, ImportModule.PROFESSIONAL);
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        }

        logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "??????????????????");

        return new JsonResultBean(JsonResultBean.SUCCESS, "????????????" + excelDataList.size() + "??????.");
    }

    private void checkImportData(List<ProfessionalImportDTO> excelDataList, List<ProfessionalDO> professionalDOs,
        StringBuilder message) throws Exception {
        String currentUsername = SystemHelper.getCurrentUsername();
        // ????????????
        Set<String> typeSet = new HashSet<>();
        for (ProfessionalImportDTO professional : excelDataList) {
            String type = professional.getType();
            if (StringUtils.isNotBlank(type)) {
                typeSet.add(type);
            }
        }
        Map<String, ProfessionalsTypeForm> professionalsTypeMap = CollectionUtils.isEmpty(typeSet) ? new HashMap<>() :
            newProfessionalsDao.getProfessionalsTypes(typeSet).stream()
                .collect(Collectors.toMap(ProfessionalsTypeForm::getProfessionalstype, Function.identity()));
        String groupUuid = userService.getOrgIdExceptAdmin();
        String orgName = organizationService.getOrganizationByUuid(groupUuid).getName();

        //????????????
        String landLineReg = "^(\\d{3}-\\d{8}|\\d{4}-\\d{7,8}|\\d{7,13})?$";
        // ????????????????????????
        String nameReg = "^[A-Za-z\u4e00-\u9fa5]{0,8}$";
        // ????????????
        String phoneReg = "^(\\d{7,13})?$";
        // ????????????
        String emailReg = "^\\s*$|\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        // ???????????????
        String identityReg = "^(\\d{18}|\\d{15}|\\d{17}[xX])$";
        // ??????????????????6????????????????????????
        Pattern birthDatePattern = Pattern.compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*");

        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDate = new Date();
        Date maxDate = yyyyMMdd.parse("9999-12-31");

        Map<String, Integer> nameAndIdentityMap = new HashMap<>(16);
        Map<String, Integer> identityMap = new HashMap<>(16);
        Map<String, String> nationMap = cacheManger.getDictValueIdMap(DictionaryType.NATION);
        Map<String, String> educationMap = cacheManger.getDictValueIdMap(DictionaryType.EDUCATION);
        for (int i = 0, len = excelDataList.size(); i < len; i++) {
            ProfessionalImportDTO professional = excelDataList.get(i);
            //??????
            String name = professional.getName();
            String identity = professional.getIdentity();
            String nameAndIdentity = name + identity;
            boolean nameIsNotBlank = StringUtils.isNotBlank(name);
            boolean identityIsNotBlank = StringUtils.isNotBlank(identity);
            boolean importListIsContainsNameAndIdentity = nameAndIdentityMap.containsKey(nameAndIdentity);
            boolean importListIsContainsIdentity = identityMap.containsKey(identity);
            if (identityIsNotBlank && !importListIsContainsIdentity) {
                identityMap.put(identity, i);
            }
            if (!nameIsNotBlank) {
                professional.setErrorMsg("????????????");
                continue;
            }
            if (importListIsContainsNameAndIdentity) {
                professional.setErrorMsg("????????????");
                continue;
            }
            nameAndIdentityMap.put(nameAndIdentity, i);
            // ????????????????????????
            if (!name.matches(nameReg)) {
                professional.setErrorMsg("???????????????????????????????????????8?????????????????????");
                continue;
            }
            // ??????
            String landline = professional.getLandline();
            if (StringUtils.isNotBlank(landline) && !landline.matches(landLineReg)) {
                professional.setErrorMsg("??????????????????,??????: ??????7-13");
                continue;
            }
            // ????????????
            String birthdayStr = professional.getBirthdayStr();
            if (StringUtils.isNotBlank(birthdayStr)) {
                Date birthDay;
                try {
                    birthDay = yyyyMMdd.parse(birthdayStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("????????????????????????");
                    continue;
                }
                if (birthDay.getTime() > nowDate.getTime()) {
                    professional.setErrorMsg("?????????????????????????????????????????????????????????");
                    continue;
                }
                professional.setBirthday(birthDay);
            }
            //????????????
            String jobNumber = professional.getJobNumber();
            if (StringUtils.isNotBlank(jobNumber) && jobNumber.length() > 30) {
                professional.setErrorMsg("?????????????????????????????????????????????30");
                continue;
            }
            //????????????????????????
            String cardNumber = professional.getCardNumber();
            if (StringUtils.isNotBlank(cardNumber) && cardNumber.length() > 30) {
                professional.setErrorMsg("???????????????????????????????????????????????????????????????30");
                continue;
            }
            //??????????????????
            String hireDateStr = professional.getHiredateStr();
            if (StringUtils.isNotBlank(hireDateStr)) {
                Date hireDate;
                try {
                    hireDate = yyyyMMdd.parse(hireDateStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("????????????????????????");
                    continue;
                }
                if (hireDate.getTime() > nowDate.getTime()) {
                    professional.setErrorMsg("?????????????????????????????????????????????????????????");
                    continue;
                }
                professional.setHiredate(hireDate);
            }
            // ????????????1
            String phone = professional.getPhone();
            if (StringUtils.isNotEmpty(phone) && !phone.matches(phoneReg)) {
                professional.setErrorMsg("??????1????????????,??????: ??????7-13");
                continue;
            }
            //?????????????????????
            String emergencyContact = professional.getEmergencyContact();
            if (StringUtils.isNotBlank(emergencyContact) && emergencyContact.length() > 20) {
                professional.setErrorMsg("???????????????????????????????????????????????????????????????");
                continue;
            }
            //???????????????????????????
            String emergencyContactPhone = professional.getEmergencyContactPhone();
            if (StringUtils.isNotBlank(emergencyContactPhone) && !emergencyContactPhone.matches(phoneReg)) {
                professional.setErrorMsg("?????????????????????????????????,??????:7-13<");
                continue;
            }
            //????????????
            String email = professional.getEmail();
            if (StringUtils.isNotBlank(email)) {
                if (email.length() > 50) {
                    professional.setErrorMsg("?????????????????????????????????????????????50");
                    continue;
                }
                if (!email.matches(emailReg)) {
                    professional.setErrorMsg("??????????????????");
                    continue;
                }
            }
            // ????????????2
            String phoneTwo = professional.getPhoneTwo();
            if (StringUtils.isNotBlank(phoneTwo) && !phoneTwo.matches(phoneReg)) {
                professional.setErrorMsg("??????2????????????,??????: ??????7-13");
                continue;
            }
            // ????????????3
            String phoneThree = professional.getPhoneThree();
            if (StringUtils.isNotBlank(phoneThree) && !phoneThree.matches(phoneReg)) {
                professional.setErrorMsg("??????3????????????,??????: ??????7-13");
                continue;
            }
            //??????????????????
            String drivingLicenseNo = professional.getDrivingLicenseNo();
            if (StringUtils.isNotBlank(drivingLicenseNo) && drivingLicenseNo.length() > 64) {
                professional.setErrorMsg("????????????????????????????????????????????????64");
                continue;
            }
            // ??????????????????????????????
            String serviceCompany = professional.getServiceCompany();
            if (StringUtils.isNotBlank(serviceCompany) && serviceCompany.length() > 20) {
                professional.setErrorMsg("?????????????????????????????????????????????20");
                continue;
            }
            // ????????????????????????????????????
            String qualificationCategory = professional.getQualificationCategory();
            //?????????????????????????????????????????????????????????50?????????
            //2019-07-24 ??????
            if (StringUtils.isNotBlank(qualificationCategory) && qualificationCategory.length() > 50) {
                professional.setErrorMsg("???????????????????????????????????????????????????50");
                continue;
            }
            //  ?????????????????????????????????????????????
            String issueCertificateDateStr = professional.getIssueCertificateDateStr();
            if (StringUtils.isNotBlank(issueCertificateDateStr)) {
                try {
                    professional.setIssueCertificateDate(yyyyMMdd.parse(issueCertificateDateStr));
                } catch (ParseException e) {
                    professional.setErrorMsg("????????????????????????");
                    continue;
                }
            }
            // ???????????????
            String icCardEndDateStr = professional.getIcCardEndDateStr();
            if (StringUtils.isNotBlank(icCardEndDateStr)) {
                try {
                    professional.setIcCardEndDate(yyyyMMdd.parse(icCardEndDateStr));
                } catch (ParseException e) {
                    professional.setErrorMsg("???????????????????????????");
                    continue;
                }
            }
            // ????????????
            String address = professional.getAddress();
            if (StringUtils.isNotBlank(address) && address.length() > 50) {
                professional.setErrorMsg("???????????????????????????????????????50");
                continue;
            }
            //???????????????????????????
            String drivingAgencies = professional.getDrivingAgencies();
            if (StringUtils.isNotBlank(drivingAgencies) && drivingAgencies.length() > 128) {
                professional.setErrorMsg("?????????????????????????????????????????????????????????128");
                continue;
            }
            //??????????????????
            String operationNumber = professional.getOperationNumber();
            if (StringUtils.isNotBlank(operationNumber) && operationNumber.length() > 64) {
                professional.setErrorMsg("????????????????????????????????????????????????64");
                continue;
            }
            //???????????????????????????
            String operationAgencies = professional.getOperationAgencies();
            if (StringUtils.isNotBlank(operationAgencies) && operationAgencies.length() > 128) {
                professional.setErrorMsg("?????????????????????????????????????????????????????????128");
                continue;
            }
            // ??????????????????
            String type = professional.getType();
            if (StringUtils.isNotBlank(type)) {
                ProfessionalsTypeForm professionalsType = professionalsTypeMap.get(type);
                if (professionalsType != null) {
                    professional.setPositionType(professionalsType.getId());
                } else {
                    professional.setErrorMsg("????????????????????????????????????");
                    continue;
                }
                //???????????????????????????IC?????????????????????????????????
                if ("?????????(IC???)".equals(type) && StringUtils.isBlank(identity)) {
                    professional.setErrorMsg("???????????????????????????IC??????????????????????????????");
                    continue;
                }
            } else {
                professional.setType(null);
                professional.setPositionType(null);
            }
            //????????????
            String drivingType = professional.getDrivingType();
            if (StringUtils.isNotBlank(drivingType) && drivingType.length() > 10) {
                professional.setErrorMsg("??????????????????????????????????????????10");
                continue;
            }
            //????????????????????????
            String drivingStartDateStr = professional.getDrivingStartDateStr();
            Date drivingStartDate = null;
            if (StringUtils.isNotBlank(drivingStartDateStr)) {
                try {
                    drivingStartDate = yyyyMMdd.parse(drivingStartDateStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("??????????????????????????????");
                    continue;
                }
                if (drivingStartDate.getTime() > maxDate.getTime()) {
                    professional.setErrorMsg("?????????????????????????????????????????????????????????9999-12-31");
                    continue;
                }
                professional.setDrivingStartDate(drivingStartDate);
            }
            //????????????????????????
            String drivingEndDateStr = professional.getDrivingEndDateStr();
            Date drivingEndDate = null;
            if (StringUtils.isNotBlank(drivingEndDateStr)) {
                try {
                    drivingEndDate = yyyyMMdd.parse(drivingEndDateStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("??????????????????????????????");
                    continue;
                }
                if (drivingEndDate.getTime() > maxDate.getTime()) {
                    professional.setErrorMsg("?????????????????????????????????????????????????????????9999-12-31");
                    continue;
                }
                professional.setDrivingEndDate(drivingEndDate);
            }
            if (drivingStartDate != null && drivingEndDate != null && drivingStartDate.getTime() > drivingEndDate
                .getTime()) {
                professional.setErrorMsg("?????????????????? ???????????? ??????????????????1");
                continue;
            }
            //????????????????????????
            Integer remindDays = professional.getRemindDays();
            if (Objects.nonNull(remindDays) && (remindDays < 0 || remindDays > 9999)) {
                professional.setErrorMsg("?????????????????????????????????0-9999");
                continue;
            }
            // ??????????????????????????????
            if (identityIsNotBlank) {
                if (!identity.matches(identityReg)) {
                    professional.setErrorMsg("????????????????????????");
                    continue;
                }
                if (importListIsContainsIdentity) {
                    professional.setErrorMsg("??????????????????");
                    continue;
                }
                // ????????????????????????????????????
                // ??????Pattern??????Matcher
                Matcher birthDateMather = birthDatePattern.matcher(identity);
                // ??????Matcher??????????????????????????????
                if (!birthDateMather.find()) {
                    professional.setErrorMsg("????????????????????????");
                    continue;
                }
                String year = birthDateMather.group(1);
                String month = birthDateMather.group(2);
                String date = birthDateMather.group(3);
                GregorianCalendar gc = new GregorianCalendar();
                // ?????????????????????????????????
                boolean identityFormatErrorFlag = (gc.get(Calendar.YEAR) - Integer.parseInt(year)) > 150
                    || (gc.getTime().getTime() - yyyyMMdd.parse(year + "-" + month + "-" + date).getTime()) < 0;
                boolean monthFormatErrorFlag = Integer.parseInt(month) > 12 || Integer.parseInt(month) == 0;
                boolean dateFormatErrorFlag = Integer.parseInt(date) > 31 || Integer.parseInt(date) == 0;
                if (dateFormatErrorFlag || monthFormatErrorFlag || identityFormatErrorFlag) {
                    professional.setErrorMsg("??????????????????????????????????????????");
                    continue;
                }

                String nation = professional.getNation();
                if (StringUtils.isNotEmpty(nation)) {
                    if (!nationMap.containsKey(nation)) {
                        professional.setErrorMsg("???????????????");
                        continue;
                    }
                    professional.setNationId(nationMap.get(nation));
                }

                String education = professional.getEducation();
                if (StringUtils.isNotEmpty(education)) {
                    if (!educationMap.containsKey(education)) {
                        professional.setErrorMsg("?????????????????????");
                        continue;
                    }
                    professional.setEducationId(educationMap.get(education));
                }
            }

            professional.setOrgId(groupUuid);
            professional.setOrgName(orgName);

            ProfessionalDO professionalDO = new ProfessionalDO();
            BeanUtils.copyProperties(professional, professionalDO);
            professionalDO.setCreateDataUsername(currentUsername);
            professionalDO.setCreateDataTime(new Date());
            // ??????
            String gender = professional.getGender();
            professionalDO.setGender(Objects.equals("???", gender) ? "2" : "1");
            //??????
            String state = professional.getState();
            professionalDO.setState(Objects.equals("??????", state) ? "2" : Objects.equals("??????", state) ? "1" : "0");
            professionalDOs.add(professionalDO);
            message.append("?????????????????? : ").append(name).append(" ( @").append(orgName).append(" ) <br/> ");
        }
    }


    /**
     * ??????????????????
     * @param title
     * @return
     * @throws Exception
     */
    @Override
    public boolean exportProfessionals(String title) throws Exception {
        HttpServletResponse response = getHttpServletResponse();
        ExportExcelUtil.setResponseHead(response, title);
        ExportExcel export = new ExportExcel(title, ProfessionalsExportDTO.class, 1);
        RedisKey exportKey = RedisKeyEnum.USER_PROFESSIONAL_EXPORT.of(userService.getCurrentUserInfo().getUsername());
        List<String> professionalIds = RedisHelper.getList(exportKey);
        List<ProfessionalDTO> exportDtos = newProfessionalsDao.findProfessionalsByIds(professionalIds);
        List<ProfessionalsExportDTO> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(exportDtos)) {
            for (ProfessionalDTO professionalDTO : exportDtos) {
                ProfessionalsExportDTO exportDTO = new ProfessionalsExportDTO();
                BeanUtils.copyProperties(professionalDTO, exportDTO);
                if (null != exportDTO.getIssueCertificateDate()) { // ?????????????????????????????????
                    exportDTO.setIssueCertificateDateStr(
                            Converter.toString(exportDTO.getIssueCertificateDate(), "yyyy-MM-dd"));
                }
                if (null != exportDTO.getIcCardEndDate()) { // ???????????????????????????
                    exportDTO
                            .setIcCardEndDateStr(Converter.toString(exportDTO.getIcCardEndDate(), "yyyy-MM-dd"));
                }
                //??????
                if (null != exportDTO.getBirthday()) {
                    exportDTO
                            .setBirthdayStr(Converter.toString(exportDTO.getBirthday(), "yyyy-MM-dd"));
                }
                //????????????
                if (null != exportDTO.getHiredate()) {
                    exportDTO
                            .setHiredateStr(Converter.toString(exportDTO.getHiredate(), "yyyy-MM-dd"));
                }
                //??????????????????
                if (null != exportDTO.getDrivingStartDate()) {
                    exportDTO.setDrivingStartDateStr(
                            Converter.toString(exportDTO.getDrivingStartDate(), "yyyy-MM-dd"));
                }
                //??????????????????
                if (null != exportDTO.getDrivingEndDate()) {
                    exportDTO
                            .setDrivingEndDateStr(Converter.toString(exportDTO.getDrivingEndDate(), "yyyy-MM-dd"));
                }
                // ??????????????????
                if (StringUtils.isNotEmpty(exportDTO.getGender())) {
                    if ("1".equals(exportDTO.getGender())) {
                        exportDTO.setGender("???");
                    } else if ("2".equals(exportDTO.getGender())) {
                        exportDTO.setGender("???");
                    }
                } else {
                    exportDTO.setGender("");
                }
                if (Converter.toBlank(exportDTO.getState()).equals("0")) {
                    exportDTO.setState("??????");
                } else if (Converter.toBlank(exportDTO.getState()).equals("1")) {
                    exportDTO.setState("??????");
                } else if (Converter.toBlank(exportDTO.getState()).equals("2")) {
                    exportDTO.setState("??????");
                } else {
                    exportDTO.setState("");
                }

                exportDTO.setNation(Optional.ofNullable(exportDTO.getNationId())
                        .map(o -> cacheManger.getDictionaryValue(exportDTO.getNationId())).orElse(null));
                exportDTO.setEducation(Optional.ofNullable(exportDTO.getEducationId())
                        .map(o -> cacheManger.getDictionaryValue(exportDTO.getEducationId())).orElse(null));
                result.add(exportDTO);
            }
        }

        export.setDataList(result);
        OutputStream out = null;
        try {
            // ???????????????
            out = response.getOutputStream();
            export.write(out);// ????????????????????????????????????
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return true;
    }

    /**
     * ????????????id??????????????????
     * @param orgId
     * @return
     */
    @Override
    public List<ProfessionalDO> getProfessionalsByOrgId(String orgId) {
        return newProfessionalsDao.getProfessionalsByOrgId(orgId);
    }

    @Override
    public List<ProfessionalDTO> getProfessionalByIds(Collection<String> ids) {
        List<ProfessionalDTO> professionalList = newProfessionalsDao.findProfessionalsByIds(ids);
        List<OrganizationLdap> orgs = organizationService.getAllOrganization();
        Map<String, String> orgMap =
            AssembleUtil.collectionToMap(orgs, OrganizationLdap::getUuid, OrganizationLdap::getName);
        professionalList.forEach(o -> o.setOrgName(orgMap.get(o.getOrgId())));
        return professionalList;
    }

    /**
     * ?????????????????????
     * @param isOrg
     * @return
     */
    @Override
    public String getTree(String isOrg) {
        // ?????????????????????????????????????????????
        List<OrganizationLdap> orgs = userService.getCurrentUseOrgList();
        JSONArray result = new JSONArray();
        for (OrganizationLdap group : orgs) {
            if ((isOrg == null || "0".equals(isOrg)) && "ou=organization".equals(group.getCid())) {
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.put("id", group.getCid());
            obj.put("pId", group.getPid());
            obj.put("name", group.getName());
            obj.put("uuid", group.getUuid());
            obj.put("type", "group");
            obj.put("adCode", group.getCountyCode());
            obj.put("isarea", group.getIsArea());
            result.add(obj);
        }
        return result.toJSONString();
    }

    /**
     * ??????????????????????????????????????????
     * @param name
     * @param identity
     * @return
     */
    @Override
    public boolean repetition(String name, String identity) {
        List<ProfessionalDO> professionalDOS = new ArrayList<>();
        if (identity != null && identity.length() == IDENTITY_LENGTH) {
            ProfessionalDO  professionalDO = findByNameExistIdentity(name, identity);
            if (professionalDO != null) {
                professionalDOS.add(professionalDO);
            }
        } else {
            professionalDOS = getProfessionalsByName(name);
        }
        if (professionalDOS == null || professionalDOS.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean repetitions(String id, String identity) {
        List<ProfessionalDO> professionalDOS = getProfessionalsByIdentity(identity);
        boolean flag = false;
        if (id != null) {
            ProfessionalDTO pif = getProfessionalsById(id);
            if (pif.getIdentity() != null && pif.getIdentity().equals(identity)) {
                flag = true;
            } else if (professionalDOS == null || professionalDOS.size() == 0) {
                return true;
            }
            return flag;
        } else if (professionalDOS == null || professionalDOS.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param name
     * @param identity
     * @return
     */
    @Override
    public ProfessionalDO findByNameExistIdentity(String name, String identity) {
        if (StringUtils.isNotBlank(name)) {
            return newProfessionalsDao.findByNameExistIdentity(name, identity);
        }
        return null;
    }

    /**
     * ?????????????????????????????????
     * @param identity ????????????
     * @return list
     * @throws Exception Exception
     */
    @Override
    public List<ProfessionalDO> getProfessionalsByIdentity(String identity) {
        if (StringUtils.isNotBlank(identity)) {
            return newProfessionalsDao.getProfessionalsByIdentity(identity);
        }
        return null;
    }

    /**
     * @param name ??????????????????
     * @return ????????????list
     * @throws Exception ??????
     */
    @Override
    public List<ProfessionalDO> getProfessionalsByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            return newProfessionalsDao.getProfessionalsByName(name);
        }
        return null;
    }

    /**
     * ??????id??????????????????
     * @param id
     * @return
     */
    @Override
    public ProfessionalDTO getProfessionalsById(String id) {
        return newProfessionalsDao.getProfessionalById(id);
    }

    /**
     * ???????????????????????????
     * @param type
     * @param name
     * @return
     */
    @Override
    public JSONObject getIcCardTree(String type, String name) {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        List<OrganizationLdap> currentUseOrgList = userService.getCurrentUseOrgList();
        Set<String> allProIds = getRedisOrgProfessionalId(userService.getCurrentUserOrgIds());
        List<ProfessionalDO> professionalDOS =
            allProIds.isEmpty() ? new ArrayList<>() : newProfessionalsDao.findAllIcCarDriver(allProIds, name);
        Set<String> proGroupIds = professionalDOS.stream().map(pro -> pro.getOrgId()).collect(Collectors.toSet());
        List<OrganizationLdap> filterList =
            currentUseOrgList.stream().filter(org -> proGroupIds.contains(org.getUuid())).collect(Collectors.toList());
        currentUseOrgList = TreeUtils.getFilterWholeOrgList(currentUseOrgList, filterList);
        if (CollectionUtils.isNotEmpty(professionalDOS)) {
            for (OrganizationLdap organization : currentUseOrgList) {
                for (ProfessionalDO pro : professionalDOS) {
                    if (!organization.getUuid().equals(pro.getOrgId())) {
                        continue;
                    }
                    JSONObject proObj = getDriverNode(type, organization, pro);
                    result.add(proObj);
                }
            }
        }

        // ?????????????????????
        result.addAll(JsonUtil.getOrgTree(currentUseOrgList, type));

        obj.put("size", professionalDOS.size());
        obj.put("tree", result);
        return obj;
    }

    @Override
    public JSONObject getProTree(String type, String name) {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        List<OrganizationLdap> currentUseOrgList = userService.getCurrentUseOrgList();
        Set<String> allProIds = getRedisOrgProfessionalId(userService.getCurrentUserOrgIds());
        List<ProfessionalDO> professionalDos =
            allProIds.isEmpty() ? new ArrayList<>() : newProfessionalsDao.findAllDriver(allProIds, name);
        Set<String> proGroupIds = professionalDos.stream().map(ProfessionalDO::getOrgId).collect(Collectors.toSet());
        List<OrganizationLdap> filterList =
            currentUseOrgList.stream().filter(org -> proGroupIds.contains(org.getUuid())).collect(Collectors.toList());
        currentUseOrgList = TreeUtils.getFilterWholeOrgList(currentUseOrgList, filterList);
        if (CollectionUtils.isNotEmpty(professionalDos)) {
            for (OrganizationLdap organization : currentUseOrgList) {
                for (ProfessionalDO pro : professionalDos) {
                    if (!organization.getUuid().equals(pro.getOrgId())) {
                        continue;
                    }
                    JSONObject proObj = getDriverNode(type, organization, pro);
                    result.add(proObj);
                }
            }
        }
        // ?????????????????????
        result.addAll(JsonUtil.getOrgTree(currentUseOrgList, type));
        obj.put("size", professionalDos.size());
        obj.put("tree", result);
        return obj;
    }

    /**
     * ????????????????????????????????????
     * @param type
     * @param organization
     * @param icCardDriver
     * @return
     */
    private JSONObject getDriverNode(String type, OrganizationLdap organization, ProfessionalDO icCardDriver) {
        // ???????????????
        JSONObject proObj = new JSONObject();

        proObj.put("id", icCardDriver.getId());
        proObj.put("pId", organization.getId().toString());
        proObj.put("name", icCardDriver.getName());
        // ????????????
        proObj.put("cardNumber", icCardDriver.getIdentity());
        proObj.put("photograph", icCardDriver.getPhotograph());
        proObj.put("type", "people");
        proObj.put("iconSkin", "peopleSkin");
        // assignmentObj.put("open", false);
        // ?????????????????????
        if ("single".equals(type)) {
            proObj.put("nocheck", true);
        }
        // ????????????
        proObj.put("isParent", false);
        return proObj;
    }

    /**
     * ????????????
     * @param orgIds
     * @return
     */
    @Override
    public Set<String> getRedisOrgProfessionalId(Collection<String> orgIds) {
        List<RedisKey> keys = RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.ofs(orgIds);
        Set<String> values = RedisHelper.batchGetSet(keys);
        return values;
    }

    @Override
    public int getProfessionalCountByPid(String parentId) {
        //????????????id?????? ?????????

        Set<String> groupSet = new HashSet<>(organizationService.getChildOrgIdByUuid(parentId));
        Set<String> allProIds = getRedisOrgProfessionalId(groupSet);
        List<ProfessionalDO> professionalDOS =
            allProIds.isEmpty() ? new ArrayList<>() : newProfessionalsDao.findAllIcCarDriver(allProIds, null);

        return professionalDOS.size();
    }

    /**
     * ????????????
     * @param vehicleId
     * @return
     */
    @Override
    public List<ProfessionalDTO> getRiskProfessionalsInfo(String vehicleId) {
        List<ProfessionalDTO> professionalDTOS = new ArrayList<>();
        String insertCardInfo = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vehicleId));
        if (StrUtil.isNotBlank(insertCardInfo)) {
            //????????????
            String insertIdentityId = insertCardInfo.split(",")[0];
            String[] cardIdAndNameArr = insertIdentityId.split("_");
            String insertCardDriverId = newProfessionalsDao
                .getIcCardDriverIdByIdentityAndName(cardIdAndNameArr[0], cardIdAndNameArr[1]);
            ProfessionalDTO professionalDTO = getAdasProfessionalShow(insertCardDriverId);
            if (professionalDTO != null) {
                professionalDTOS.add(professionalDTO);
            }
            return professionalDTOS;
        }

        //????????????
        String lastInsertCardDriverId = lastInsertCardDriverId(vehicleId);
        List<String> professionalIds = getProfessionalIdsByVehicleId(vehicleId);

        ProfessionalDTO form;
        for (String professionalId : professionalIds) {
            //????????????????????????????????????????????????????????????
            if (professionalId.equals(lastInsertCardDriverId)
                || (form = getAdasProfessionalShow(professionalId)) == null) {
                continue;
            }
            //?????????ic????????????????????????????????????
            if (form.getType() != null && form.getType().equals(IC_TYPE)) {
                professionalDTOS.add(0, form);
            } else {
                professionalDTOS.add(form);
            }
        }
        if (StrUtil.isNotBlank(lastInsertCardDriverId)) {
            //??????????????????????????????????????????
            professionalDTOS.add(0, getAdasProfessionalShow(lastInsertCardDriverId));
        }

        return professionalDTOS;
    }

    @Override
    public List<Map<String, String>> getSelectList(String keyword) {
        List<Map<String, String>> result = new ArrayList<>();
        Set<String> allProIds = getRedisOrgProfessionalId(userService.getCurrentUserOrgIds());
        if (CollectionUtils.isEmpty(allProIds)) {
            return result;
        }
        if (StringUtils.isNotBlank(keyword)) {
            List<ProfessionalDO> professionalListByFuzzySearch =
                newProfessionalsDao.fuzzySearchByName(StringUtil.mysqlLikeWildcardTranslation(keyword));
            if (CollectionUtils.isEmpty(professionalListByFuzzySearch)) {
                return result;
            }
            Map<String, ProfessionalDO> professionalMap = professionalListByFuzzySearch.stream()
                .collect(Collectors.toMap(ProfessionalDO::getId, Function.identity()));
            List<String> professionalSortIdList = RedisHelper.getList(RedisKeyEnum.PROFESSIONAL_SORT_ID.of());
            for (String professionalSortId : professionalSortIdList) {
                ProfessionalDO professionalDO = professionalMap.get(professionalSortId);
                if (professionalDO == null || !allProIds.contains(professionalSortId)) {
                    continue;
                }
                Map<String, String> map = new HashMap<>();
                map.put("id", professionalDO.getId());
                map.put("name", professionalDO.getName());
                map.put("identity", professionalDO.getIdentity());
                result.add(map);
            }
            return result.stream().limit(Vehicle.UNBIND_SELECT_SHOW_NUMBER).collect(Collectors.toList());
        }
        int endIndex = Math.min(allProIds.size(), Vehicle.UNBIND_SELECT_SHOW_NUMBER);
        List<String> cutIds = Lists.newArrayList(allProIds).subList(0, endIndex);
        Map<String, ProfessionalDTO> filterProMap = newProfessionalsDao.findProfessionalsByIds(cutIds).stream()
            .collect(Collectors.toMap(ProfessionalDTO::getId, Function.identity()));
        sortSelectList(filterProMap, result);
        return result;
    }

    /**
     * ?????????????????????
     * @param filterProMap
     * @param result
     */
    private void sortSelectList(Map<String, ProfessionalDTO> filterProMap, List<Map<String, String>> result) {
        List<String> sortList = RedisHelper.getList(RedisKeyEnum.PROFESSIONAL_SORT_ID.of());
        for (String id : sortList) {
            if (filterProMap.containsKey(id)) {
                Map<String, String> map = new HashMap<>();
                ProfessionalDTO professionalDTO = filterProMap.get(id);
                if (professionalDTO == null) {
                    continue;
                }
                map.put("id", professionalDTO.getId());
                map.put("name", professionalDTO.getName());
                map.put("identity", professionalDTO.getIdentity());
                result.add(map);
            }
        }
    }

    @Override
    public Map<String, ProfessionalShowDTO> getProfessionalShowMaps(
        Collection<IcCardDriverQuery> icCardDriverQueryList) {
        List<ProfessionalShowDTO> icCardDriverInfos = newProfessionalsDao.getIcCardDriverInfos(icCardDriverQueryList);
        return assembleProfessionalData(icCardDriverInfos);
    }

    @Override
    public Map<String, ProfessionalShowDTO> getProfessionalShowMap(Collection<String> ids) {

        List<ProfessionalShowDTO> icCardDriverInfos = newProfessionalsDao.getIcCardDriverInfoByIds(ids);
        return assembleProfessionalData(icCardDriverInfos);
    }

    private Map<String, ProfessionalShowDTO> assembleProfessionalData(List<ProfessionalShowDTO> icCardDriverInfos) {
        Map<String, ProfessionalShowDTO> driverInfoMaps = new HashMap<>();
        Set<String> driverOrgIdSet = new HashSet<>();
        for (ProfessionalShowDTO icCardDriverInfo : icCardDriverInfos) {
            driverOrgIdSet.add(icCardDriverInfo.getOrgId());
            icCardDriverInfo.setPhotograph(configHelper.getProfessionalUrl(icCardDriverInfo.getPhotograph()));
            Date icCardEndDate = icCardDriverInfo.getIcCardEndDate();
            if (icCardEndDate != null) {
                icCardDriverInfo.setIcCardEndDateStr(DateUtil.getDateToString(icCardEndDate, "yyyy-MM-dd"));
            }
            driverInfoMaps.put(icCardDriverInfo.getIdentity() + "_" + icCardDriverInfo.getName(), icCardDriverInfo);
        }
        List<RedisKey> orgIdKeys = driverOrgIdSet.stream()
                .map(RedisKeyEnum.ORGANIZATION_INFO::of)
                .collect(Collectors.toList());

        Map<String, String> orgInfoMap = RedisHelper.batchGetHashMap(orgIdKeys, "id", "name");
        for (ProfessionalShowDTO driverShow : driverInfoMaps.values()) {
            driverShow.setOrgName(Optional.ofNullable(orgInfoMap.get(driverShow.getOrgId())).orElse("-"));
        }
        return driverInfoMaps;
    }

    private String lastInsertCardDriverId(String vehicleId) {
        String result = "";
        //????????????????????????????????????????????????
        String lastInsertCardTimeInfo = RedisHelper.getString(HistoryRedisKeyEnum.LAST_DRIVER.of(vehicleId));
        if (StrUtil.isNotBlank(lastInsertCardTimeInfo)) {
            String lastInsertCardNumberAndName = lastInsertCardTimeInfo.split(",")[0].split("c_")[1];
            String[] cardIdAndNameArr = lastInsertCardNumberAndName.split("_");
            result = newProfessionalsDao
                .getIcCardDriverIdByIdentityAndName(cardIdAndNameArr[0], cardIdAndNameArr[1]);
        }
        return result;
    }

    private List<String> getProfessionalIdsByVehicleId(String vehicleId) {
        Map<String, String> config = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(vehicleId));
        if (config == null) {
            return Collections.emptyList();
        }

        List<String> professionalIds = new ArrayList<>();
        if (StrUtil.isNotBlank(config.get("professionalIds"))) {
            professionalIds = Arrays.asList(config.get("professionalIds").split(","));
        }
        return professionalIds;
    }

    ProfessionalDTO getAdasProfessionalShow(String professionalId) {
        ProfessionalDTO professionalDTO = newProfessionalsDao.getProfessionalById(professionalId);
        if (professionalDTO.getPhotograph() != null && !professionalDTO.getPhotograph().equals("")) {
            String fileName = professionalDTO.getPhotograph();
            String mediaServer = configHelper.getMediaServer();
            if (configHelper.isSslEnabled()) {
                mediaServer = "/mediaserver";
            }
            professionalDTO.setPhotograph(mediaServer + configHelper.getProfessionalFtpPath() + fileName);
        }
        professionalDTO.setOrgName(organizationService.getOrganizationByUuid(professionalDTO.getOrgId()).getName());
        Date icCardEndDate = professionalDTO.getIcCardEndDate();
        if (icCardEndDate != null) {
            professionalDTO.setIcCardEndDateStr(DateUtil.getDateToString(icCardEndDate, DateUtil.DATE_FORMAT_SHORT));
        }
        return professionalDTO;
    }

}
