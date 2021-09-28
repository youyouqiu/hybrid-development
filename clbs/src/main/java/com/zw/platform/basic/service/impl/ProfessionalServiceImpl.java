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
 @Description 从业人员模块redis优化
 @version 1.0
 **/
@Service
public class ProfessionalServiceImpl implements CacheService, ProfessionalService, IpAddressService {

    private Logger log = LogManager.getLogger(ProfessionalServiceImpl.class);

    private static final int IMPORT_EXCEL_CELL = 34;

    private static final Integer IDENTITY_LENGTH = 18;

    private static final String IC_TYPE = "驾驶员(IC卡)";

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
        log.info("开始进行从业人员的redis初始化.");
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
            //模糊查询缓存
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
        log.info("结束进行从业人员的redis初始化.");
    }

    /**
     * 新增从业人员及关联表
     * @param professionalDTO
     * @return
     * @throws Exception
     */
    @Override
    public boolean add(ProfessionalDTO professionalDTO) throws Exception {

        ProfessionalDO professionalDO = new ProfessionalDO();
        BeanUtils.copyProperties(professionalDTO, professionalDO);
        //数据入库
        professionalDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        // 创建者
        professionalDO.setCreateDataTime(new Date()); // 创建时间
        // 先存到数据库
        newProfessionalsDao.addProfessionals(professionalDO);

        //todo 维护redis缓存
        Map<String, String> valueMap = setValueToMap(professionalDTO);
        RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDTO.getId()), valueMap);
        //排序缓存
        RedisHelper.addToListTop(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), professionalDO.getId());
        //模糊查询缓存
        String hashKey =
            constructFuzzySearchKey(professionalDO.getName(), professionalDO.getIdentity(), professionalDO.getState());
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), hashKey, professionalDO.getId());

        //从业人员组织redis缓存
        RedisHelper
            .addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDO.getOrgId()), professionalDO.getId());

        logSearchService
            .addLog(getIpAddress(), "新增从业人员：" + professionalDTO.getName() + "( @" + professionalDTO.getOrgName() + " )",
                "3", "", "-", "");
        return true;
    }

    /**
     * 上传图片到ftp
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
                throw new RuntimeException("删除文件异常" + fileName);
            }
            return success;
        } catch (Exception e) {
            if (!e.getClass().equals(FileNotFoundException.class)) {
                log.error("从业人员图片上传到ftp异常", e);
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
        //1120新增籍贯和所属区域
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
        valueMap.put("serviceCompany", Optional.ofNullable(professionalDTO.getServiceCompany()).orElse("")); // 服务企业
        valueMap.put("qualificationCategory",
            Optional.ofNullable(professionalDTO.getQualificationCategory()).orElse("")); // 从业资格类别
        if (professionalDTO.getIssueCertificateDate() != null) {
            valueMap.put("issueCertificateDate", DateUtil
                .getDateToString(professionalDTO.getIssueCertificateDate(), DateUtil.DATE_Y_M_D_FORMAT)); // 发证日期
        }
        valueMap.put("address", Optional.ofNullable(professionalDTO.getAddress()).orElse("")); // 地址
        valueMap
            .put("identityCardPhoto", Optional.ofNullable(professionalDTO.getIdentityCardPhoto()).orElse("")); // 身份证照片
        valueMap.put("faceId", Optional.ofNullable(professionalDTO.getFaceId()).orElse(""));//人脸id
        valueMap.put("nationId", Optional.ofNullable(professionalDTO.getNationId()).orElse(""));
        valueMap.put("educationId", Optional.ofNullable(professionalDTO.getEducationId()).orElse(""));
        return valueMap;
    }

    /**
     * 判断从业人员是否绑定监控对象
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
     * 删除从业人员
     */
    @MethodLog(name = "删除从业人员", description = "删除从业人员")
    @Override
    public boolean deleteProfessionalsById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            ProfessionalDTO professionalDTO = newProfessionalsDao.getProfessionalById(id);
            if (professionalDTO == null) {
                return false;
            }
            // 先维护数据库
            if (newProfessionalsDao.deleteProfessionalsById(id)) {
                //删除fastDFS中对应图片
                if (!deleteProPhotos(professionalDTO)) {
                    throw new BusinessException("", "删除从业人员时删除fastDFS上对应的身份证，从业资格证，驾驶证等照片异常");
                }
            }
            //删除ftp里的图片
            if (professionalDTO.getPhotograph() != null) {
                String[] files = professionalDTO.getPhotograph().split("/");
                if (files.length == 0 || !delPic(files[files.length - 1])) {
                    throw new BusinessException("", "删除从业人员图片异常");
                }
            }
            // 从缓存中删除从业人员map和顺序和模糊搜索的key和从业人员和组织的关系
            Map<String, List<String>> orgIdMap = new HashMap<>();
            orgIdMap.put(professionalDTO.getOrgId(), Collections.singletonList(id));
            //模糊查询缓存
            String fuzzyHashKey = constructFuzzySearchKey(professionalDTO.getName(), professionalDTO.getIdentity(),
                professionalDTO.getState());
            removeProFromRedis(Collections.singletonList(id), orgIdMap, Collections.singletonList(fuzzyHashKey));
            OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(professionalDTO.getOrgId());
            String msg =
                "删除从业人员:" + professionalDTO.getName() + "( @" + (organizationLdap != null ? organizationLdap.getName() :
                    "") + " )";
            logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        }
        return true;
    }

    /**
     * 删除从业人员时维护redis缓存
     * @param ids
     * @param orgIdMap
     * @param fuzzyIds
     */
    private void removeProFromRedis(Collection<String> ids, Map<String, List<String>> orgIdMap,
        Collection<String> fuzzyIds) {
        //删除排序的redis缓存
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
     * 删除从业人员时删除fastDFS上对应的身份证，从业资格证，驾驶证等照片
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
            log.error("删除从业人员时删除fastDFS上对应的身份证，从业资格证，驾驶证等照片异常！", e);
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
            log.error("从FTP删除从业人员照片失败", e);
            return false;
        }
    }

    /**
     * 删除绑定了的从业人员
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
     * 处理当信息配置绑定了从业人员时删除逻辑
     * @param id
     */
    private void removeProfessionalBind(String id) {
        List<String> bandVehicleIds = newProfessionalsDao.getBindVehicleIds(id);
        //过滤空数据
        bandVehicleIds = bandVehicleIds.stream().filter(e -> e != null).collect(Collectors.toList());
        if (bandVehicleIds.size() > 0) {
            newProfessionalsDao.deleteBindInfos(id);
            for (String vid : bandVehicleIds) {
                // 修改绑定车辆的缓存的从业人员信息
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
     * 删除从业人员为正在插卡
     * @param id
     */
    private void removeDriver(String id) {
        //查询当前从业人员是否为插卡状态
        String isDrivering = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(id));

        //判断是否现在还插着卡，处理拔卡逻辑
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

            // 根据车id获取绑定信息
            BindDTO bindDTO = configService.getByMonitorId(vid);
            if (bindDTO == null) {
                return;
            }
            // 给f3推送消息，f3推送拔卡指令到finlk
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
     * 批量删除从业人员
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
        //是否是正在插卡的司机
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
                new JsonResultBean("所选从业人员中，部分已关联或绑定车辆，删除此类从业人员将解除IC卡关联以及绑定关系，" + "其余从业人员将直接被删除，确认删除？", jsonObject);
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
            // 先根据id组装key查看缓存中的是否有该从业人员的信息
            OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(professionalDTO.getOrgId());
            String orgId = professionalDTO.getOrgId();
            msg.append("删除从业人员 : ").append(professionalDTO.getName()).append(" ( @")
                .append(organizationLdap != null ? organizationLdap.getName() : "").append(" ) <br/>");
            //删除从业人员fastDFS中对应的照片
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
        // 批量删除redis中的中数据
        removeProFromRedis(isNotBandIds, orgMap, fuzzyIds);

        // 记录日志
        logSearchService.addLog(getIpAddress(), msg.toString(), "3", "batch", "批量删除从业人员");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 批量删除绑定的车业人员
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
     * 获取修改页面的从业人员信息
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
     * 修改从业人员
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
     * 修改从业人员表及关联表
     */
    @MethodLog(name = "修改修改从业人员表及关联表", description = "修改修改从业人员表及关联表")
    @Override
    public boolean updateProGroupByProId(ProfessionalDTO newProfessionalDTO, ProfessionalDTO oldProfessionalDTO)
        throws Exception {
        // 先根据从业人员id组装key取缓存中的从业人员(修改前的信息),以便记录操作日志
        String professionalId = newProfessionalDTO.getId();

        Map<String, String> proMap = RedisHelper.hgetAll(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalId));
        ProfessionalDTO redisProfessionalDTO = JSONObject.parseObject(JSON.toJSONString(proMap), ProfessionalDTO.class);

        //获取LockType 当判定是插卡录入时  不允许修改。
        if (newProfessionalDTO.getLockType() == 1) {
            newProfessionalDTO.setName(redisProfessionalDTO.getName());
            newProfessionalDTO.setCardNumber(redisProfessionalDTO.getCardNumber());
            newProfessionalDTO.setIcCardAgencies(redisProfessionalDTO.getIcCardAgencies());
            newProfessionalDTO.setIcCardEndDate(redisProfessionalDTO.getIcCardEndDate());
            newProfessionalDTO.setIdentity(redisProfessionalDTO.getIdentity());
            newProfessionalDTO.setDrivingLicenseNo(redisProfessionalDTO.getDrivingLicenseNo());
            newProfessionalDTO.setPositionType(redisProfessionalDTO.getPositionType());
        }

        //数据入库
        ProfessionalDO professionalDO = new ProfessionalDO();
        BeanUtils.copyProperties(newProfessionalDTO, professionalDO);
        professionalDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        professionalDO.setUpdateDataTime(new Date());
        boolean prFlag = newProfessionalsDao.updateProfessionals(professionalDO);

        if (!prFlag) {
            return false;
        }

        //修改的从业人员照片信息不能为空， 并且之前的照片信息和修改的照片信息不相等是下发9506
        if (StringUtils.isNotBlank(newProfessionalDTO.getPhotograph()) && !newProfessionalDTO.getPhotograph()
            .equals(oldProfessionalDTO.getPhotograph())) {
            send9506Message(newProfessionalDTO);
        }
        // 维护对象的map集合和模糊搜索的key
        updateProMapAndFuzzySearch(newProfessionalDTO);
        // 维护组织的key
        updateOrgKey(newProfessionalDTO, oldProfessionalDTO);

        //打印修改日志和发送更新信息
        addLogAndSendChange(newProfessionalDTO, oldProfessionalDTO, professionalId);
        return true;
    }

    /**
     * 打印修改日志和发送更新信息
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
            String previousName = oldProfessionalDTO.getName(); // 修改前的名字
            String nowName = newProfessionalDTO.getName(); // 现在的名字
            String previousGroup = oldProfessionalDTO.getOrgName();
            // 现在的组织
            String nowGroup = newProfessionalDTO.getOrgName();
            // 名字不相同
            if (!(previousName.equals(nowName))) {
                //维护config缓存
                updateConfigCache(professionalId, previousName, nowName);
                OrganizationLdap org = organizationService.getOrganizationByUuid(previousGroup);
                msg.append("修改从业人员 : ").append(previousName).append(" ( @").append(org.getName()).append(" ) 修改为 : ")
                    .append(nowName).append(" ( @").append(nowGroup).append(" )");
                logSearchService.addLog(getIpAddress(), msg.toString(), "3", "", "-", "");

                //给finlk推送消息，让其更新该监控对象的从业人员信息
                List<String> bandVehicleIds = newProfessionalsDao.getBindVehicleIds(professionalId);
                for (String vid : bandVehicleIds) {
                    ZMQFencePub.pubChangeFence("1," + vid);
                }
            } else {
                msg.append("修改从业人员 : ").append(nowName).append(" ( @").append(nowGroup).append(" ) ");
                logSearchService.addLog(getIpAddress(), msg.toString(), "3", "", "-", "");
            }
        }
    }

    /**
     * 修改从业人员的模糊搜索缓存
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
     * 修改企业缓存
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
     * 修改了从业人员名字时，修改config的缓存
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
     * 下发9506 指令  人证照片更新通知
     * @param professionalDTO 修改表单
     */
    public void send9506Message(ProfessionalDTO professionalDTO) {
        List<Map<String, String>> configs = newProfessionalsDao.getDeviceIdByPid(professionalDTO.getId());
        //判断是不是插卡从业人员
        String result =
            RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(professionalDTO.getId()));
        if (StringUtils.isNotBlank(result)) {
            String vid = result.split(",")[2];
            // 获取车辆的信息配置信息
            configs.addAll(configService.getConfigByVehicle(vid));
        }
        //从业人员未绑定任何车辆或者不是正在车辆上插卡直接返回。
        if (CollectionUtils.isEmpty(configs)) {
            log.info("0x9506下发错误：从业人员未绑定任何车辆或者不是正在车辆上插卡");
            return;
        }
        if (StringUtils.isNotBlank(professionalDTO.getCardNumber()) && professionalDTO.getCardNumber().length() > 20) {
            log.info("0x9506下发错误：从业人员从业资格证号超长");
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
            //9506 只针对京标协议 非京标协议  掠过~~~
            if (!ProtocolTypeUtil.BEI_JING_PROTOCOL_808_2019.equals(deviceType)) {
                log.info("0x9506下发错误：9506指令只针对京标协议监控对象");
                continue;
            }
            //获取流水号
            Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, "");
            if (msgSN == null) { // 设备未注册
                log.info("0x9506下发错误：设备未上线");
                continue;
            }
            String logMessage =
                "从业人员：" + professionalDTO.getName() + "从业资格证号：" + professionalDTO.getCardNumber() + "修改从业人员照片信息";
            log.info(">====下发京标0x9506====<" + logMessage);
            //发送9506指令  人证照片更新通知
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
                filedStringBuilder.append("正常").append("_");
            } else if ("1".equals(state)) {
                filedStringBuilder.append("离职").append("_");
            } else if ("2".equals(state)) {
                filedStringBuilder.append("停用").append("_");
            }
        }
        return filedStringBuilder.toString().substring(0, filedStringBuilder.toString().length() - 1);
    }


    /**
     * 查询从业人员分页
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
        // 根据企业id获取企业信息
        Map<String, String> orgMap = userService.getCurrentUserOrgIdOrgNameMap();

        List<String> professionals = new ArrayList<>();

        //如果是模糊查询则过滤查询结果
        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            List<Map.Entry<String, String>> searchMap =
                RedisHelper.hscan(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), getFuzztPattern(query.getSimpleQueryParam()));
            Set<String> searchIds = new HashSet<>();
            for (Map.Entry<String, String> entry : searchMap) {
                searchIds.add(entry.getValue());
            }
            orgProfessionals.retainAll(searchIds);
        }

        //排序
        for (String pid : professionalsSort) {
            if (orgProfessionals.contains(pid)) {
                professionals.add(pid);
            }
        }

        //把筛选结果的从业人员ID存入REDIS，用于导出使用
        RedisHelper.addToList(exportKey, professionals);
        int listSize = professionals.size();
        // 当前页
        int curPage = query.getPage().intValue();
        // 每页条数
        int pageSize = query.getLimit().intValue();
        // 遍历开始条数
        int lst = (curPage - 1) * pageSize;
        // 遍历条数
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);

        List<String> pageIds = professionals.subList(lst, ps);
        if (pageIds.isEmpty()) {
            return new Page<>();
        }

        // 查询所有满足条件的从业人员信息,存入redis用于导出
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
     * 组装从业人员分页数据
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
     * 生成模板
     */
    @Override
    @MethodLog(name = "生成模板", description = "生成模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("姓名(必填)");
        // headList.add("所属企业");
        headList.add("服务企业");
        headList.add("岗位类型");
        headList.add("身份证号");
        headList.add("入职时间(yyyy-MM-dd)");
        headList.add("状态");
        headList.add("工号");
        headList.add("从业资格证号");
        headList.add("从业资格类别");
        headList.add("发证机关");
        headList.add("发证日期");
        headList.add("证件有效期");
        headList.add("性别");
        headList.add("生日(yyyy-MM-dd)");
        headList.add("所属地域");
        headList.add("籍贯");
        headList.add("民族");
        headList.add("文化程度");
        headList.add("手机1");
        headList.add("手机2");
        headList.add("手机3");
        headList.add("座机");
        headList.add("紧急联系人");
        headList.add("紧急联系人电话");
        headList.add("邮箱");
        headList.add("地址");

        headList.add("操作证号(长度小等于64)");
        headList.add("操作证发证机关(长度小等于128)");
        headList.add("驾驶证号(长度小等于64)");
        headList.add("驾驶证发证机关(长度小等于128)");

        headList.add("准驾车型");
        headList.add("准驾有效期起(yyyy-MM-dd)");
        headList.add("准驾有效期至(yyyy-MM-dd)");
        headList.add("提前提醒天数(正整数,范围为1-4位)");

        // 必填字段
        requiredList.add("姓名(必填)");
        // requiredList.add("手机1(必填)");
        // requiredList.add("岗位类型");
        // requiredList.add("身份证号");
        // requiredList.add("电话");
        // 默认设置一条数据
        exportList.add("张三");
        // exportList.add(Converter.toBlank(userService.getOrganizationById(userService.getOrgIdByUser()).getName()));
        exportList.add("0101010101010110"); // 服务企业
        // 岗位类型查询数据库,然后取第一条
        List<ProfessionalsTypeDO> professionalsTypeDOS = newProfessionalsDao.findAllProfessionalsType();
        if (!CollectionUtils.isEmpty(professionalsTypeDOS)) {
            exportList.add(professionalsTypeDOS.get(0).getProfessionalstype());
        } else {
            exportList.add("请先添加岗位类型,否则从业人员导入失败!");
        }
        exportList.add("521322198301124354");
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add("正常");
        exportList.add("0001");
        exportList.add("62268962448662445586");
        exportList.add("01001");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("男");
        exportList.add(Converter.toString(new SimpleDateFormat("yyyy-MM-dd").parse("1983-01-12"), "yyyy-MM-dd"));
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("");
        exportList.add("18725719882");
        exportList.add("18725719883");
        exportList.add("18725719884");
        exportList.add("02385556666");
        exportList.add("刘某某");
        exportList.add("18084095168");
        exportList.add("5646859@qq.com");
        exportList.add("中国某地区");

        exportList.add("520131268708761234");
        exportList.add("XXXX技术监督局");
        exportList.add("430121198808081234");
        exportList.add("XX省XX市公安局交通警察支队");

        exportList.add("A1(大型客车)");
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add(Converter.toString(new Date(), "yyyy-MM-dd"));
        exportList.add(5);

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 性别
        String[] sex = { "男", "女" };
        selectMap.put("性别", sex);
        //状态
        String[] state = { "正常", "离职", "停用" };
        selectMap.put("状态", state);
        // 岗位类型
        String[] jobType = new String[professionalsTypeDOS.size() + 1];
        jobType[0] = "请选择岗位类型";
        for (int i = 0; i < professionalsTypeDOS.size(); i++) {
            jobType[i + 1] = professionalsTypeDOS.get(i).getProfessionalstype();
        }
        selectMap.put("岗位类型", jobType);

        List<DictionaryDO> nation = cacheManger.getDictionaryList(DictionaryType.NATION);
        selectMap.put("民族", nation.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        List<DictionaryDO> education = cacheManger.getDictionaryList(DictionaryType.EDUCATION);
        selectMap.put("文化程度", education.stream().map(DictionaryDO::getValue).toArray(String[]::new));

        //生成模板
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 批量导入从业人员
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @Override
    public JsonResultBean importProfessionals(MultipartFile multipartFile) throws Exception {
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        short lastCellNum = importExcel.getRow(0).getLastCellNum();
        if (lastCellNum < IMPORT_EXCEL_CELL) {
            throw new BusinessException("", "从业人员信息导入模板不正确!");
        }
        // excel 转换成 list
        List<ProfessionalImportDTO> excelDataList = importExcel.getDataListNew(ProfessionalImportDTO.class);
        if (CollectionUtils.isEmpty(excelDataList)) {
            throw new BusinessException("", "从业人员列表数据导入为空!");
        }

        List<ProfessionalDO> professionalDOs = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        checkImportData(excelDataList, professionalDOs, message);

        for (ProfessionalImportDTO professional : excelDataList) {
            if (StringUtils.isNotBlank(professional.getErrorMsg())) {
                // 遇到错误则返回
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

        logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "导入从业人员");

        return new JsonResultBean(JsonResultBean.SUCCESS, "导入成功" + excelDataList.size() + "数据.");
    }

    private void checkImportData(List<ProfessionalImportDTO> excelDataList, List<ProfessionalDO> professionalDOs,
        StringBuilder message) throws Exception {
        String currentUsername = SystemHelper.getCurrentUsername();
        // 岗位类型
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

        //座机正则
        String landLineReg = "^(\\d{3}-\\d{8}|\\d{4}-\\d{7,8}|\\d{7,13})?$";
        // 从业人员名称正则
        String nameReg = "^[A-Za-z\u4e00-\u9fa5]{0,8}$";
        // 手机正则
        String phoneReg = "^(\\d{7,13})?$";
        // 邮箱验证
        String emailReg = "^\\s*$|\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        // 身份证正则
        String identityReg = "^(\\d{18}|\\d{15}|\\d{17}[xX])$";
        // 身份证上的前6位以及出生年月日
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
            //姓名
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
                professional.setErrorMsg("姓名必填");
                continue;
            }
            if (importListIsContainsNameAndIdentity) {
                professional.setErrorMsg("姓名重复");
                continue;
            }
            nameAndIdentityMap.put(nameAndIdentity, i);
            // 校验从业人员姓名
            if (!name.matches(nameReg)) {
                professional.setErrorMsg("姓名格式错误，只能输入最多8位的中英文字符");
                continue;
            }
            // 座机
            String landline = professional.getLandline();
            if (StringUtils.isNotBlank(landline) && !landline.matches(landLineReg)) {
                professional.setErrorMsg("座机长度错误,范围: 整数7-13");
                continue;
            }
            // 检验生日
            String birthdayStr = professional.getBirthdayStr();
            if (StringUtils.isNotBlank(birthdayStr)) {
                Date birthDay;
                try {
                    birthDay = yyyyMMdd.parse(birthdayStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("出生日期格式错误");
                    continue;
                }
                if (birthDay.getTime() > nowDate.getTime()) {
                    professional.setErrorMsg("出生日期格式错误，出生日期不能大于今天");
                    continue;
                }
                professional.setBirthday(birthDay);
            }
            //校验工号
            String jobNumber = professional.getJobNumber();
            if (StringUtils.isNotBlank(jobNumber) && jobNumber.length() > 30) {
                professional.setErrorMsg("工号长度错误，工号长度不能超过30");
                continue;
            }
            //校验从业资格证号
            String cardNumber = professional.getCardNumber();
            if (StringUtils.isNotBlank(cardNumber) && cardNumber.length() > 30) {
                professional.setErrorMsg("从业资格证号错误，从业资格证号长度不能超过30");
                continue;
            }
            //检验入职时间
            String hireDateStr = professional.getHiredateStr();
            if (StringUtils.isNotBlank(hireDateStr)) {
                Date hireDate;
                try {
                    hireDate = yyyyMMdd.parse(hireDateStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("入职日期格式错误");
                    continue;
                }
                if (hireDate.getTime() > nowDate.getTime()) {
                    professional.setErrorMsg("入职日期格式错误，入职日期不能大于今天");
                    continue;
                }
                professional.setHiredate(hireDate);
            }
            // 检验手机1
            String phone = professional.getPhone();
            if (StringUtils.isNotEmpty(phone) && !phone.matches(phoneReg)) {
                professional.setErrorMsg("手机1长度错误,范围: 整数7-13");
                continue;
            }
            //校验紧急联系人
            String emergencyContact = professional.getEmergencyContact();
            if (StringUtils.isNotBlank(emergencyContact) && emergencyContact.length() > 20) {
                professional.setErrorMsg("紧急联系人长度错误，紧急联系人长度不能超过");
                continue;
            }
            //校验紧急联系人电话
            String emergencyContactPhone = professional.getEmergencyContactPhone();
            if (StringUtils.isNotBlank(emergencyContactPhone) && !emergencyContactPhone.matches(phoneReg)) {
                professional.setErrorMsg("紧急联系人电话长度错误,范围:7-13<");
                continue;
            }
            //校验邮箱
            String email = professional.getEmail();
            if (StringUtils.isNotBlank(email)) {
                if (email.length() > 50) {
                    professional.setErrorMsg("邮箱长度错误，邮箱长度不能超过50");
                    continue;
                }
                if (!email.matches(emailReg)) {
                    professional.setErrorMsg("邮箱格式错误");
                    continue;
                }
            }
            // 检验手机2
            String phoneTwo = professional.getPhoneTwo();
            if (StringUtils.isNotBlank(phoneTwo) && !phoneTwo.matches(phoneReg)) {
                professional.setErrorMsg("手机2长度错误,范围: 整数7-13");
                continue;
            }
            // 检验手机3
            String phoneThree = professional.getPhoneThree();
            if (StringUtils.isNotBlank(phoneThree) && !phoneThree.matches(phoneReg)) {
                professional.setErrorMsg("手机3长度错误,范围: 整数7-13");
                continue;
            }
            //检验驾驶证号
            String drivingLicenseNo = professional.getDrivingLicenseNo();
            if (StringUtils.isNotBlank(drivingLicenseNo) && drivingLicenseNo.length() > 64) {
                professional.setErrorMsg("驾驶证号长度错误，长度必须小等于64");
                continue;
            }
            // 检验从业人员服务企业
            String serviceCompany = professional.getServiceCompany();
            if (StringUtils.isNotBlank(serviceCompany) && serviceCompany.length() > 20) {
                professional.setErrorMsg("服务企业长度错误，长度不能大于20");
                continue;
            }
            // 检验从业人员从业资格类别
            String qualificationCategory = professional.getQualificationCategory();
            //需求更改，该处只对从业人员资格类别长度50做限制
            //2019-07-24 张强
            if (StringUtils.isNotBlank(qualificationCategory) && qualificationCategory.length() > 50) {
                professional.setErrorMsg("从业资格类别长度错误，长度不能大于50");
                continue;
            }
            //  检验从业人员从业资格证发证日期
            String issueCertificateDateStr = professional.getIssueCertificateDateStr();
            if (StringUtils.isNotBlank(issueCertificateDateStr)) {
                try {
                    professional.setIssueCertificateDate(yyyyMMdd.parse(issueCertificateDateStr));
                } catch (ParseException e) {
                    professional.setErrorMsg("发证日期格式错误");
                    continue;
                }
            }
            // 证件有效期
            String icCardEndDateStr = professional.getIcCardEndDateStr();
            if (StringUtils.isNotBlank(icCardEndDateStr)) {
                try {
                    professional.setIcCardEndDate(yyyyMMdd.parse(icCardEndDateStr));
                } catch (ParseException e) {
                    professional.setErrorMsg("证件有效期格式错误");
                    continue;
                }
            }
            // 检验地址
            String address = professional.getAddress();
            if (StringUtils.isNotBlank(address) && address.length() > 50) {
                professional.setErrorMsg("地址长度错误，长度不能大于50");
                continue;
            }
            //检验驾驶证发证机关
            String drivingAgencies = professional.getDrivingAgencies();
            if (StringUtils.isNotBlank(drivingAgencies) && drivingAgencies.length() > 128) {
                professional.setErrorMsg("驾驶证发证机关长度错误，长度必须小等于128");
                continue;
            }
            //检验操作证号
            String operationNumber = professional.getOperationNumber();
            if (StringUtils.isNotBlank(operationNumber) && operationNumber.length() > 64) {
                professional.setErrorMsg("操作证号长度错误，长度必须小等于64");
                continue;
            }
            //检验操作证发证机关
            String operationAgencies = professional.getOperationAgencies();
            if (StringUtils.isNotBlank(operationAgencies) && operationAgencies.length() > 128) {
                professional.setErrorMsg("操作证发证机关长度错误，长度必须小等于128");
                continue;
            }
            // 校验岗位类型
            String type = professional.getType();
            if (StringUtils.isNotBlank(type)) {
                ProfessionalsTypeForm professionalsType = professionalsTypeMap.get(type);
                if (professionalsType != null) {
                    professional.setPositionType(professionalsType.getId());
                } else {
                    professional.setErrorMsg("岗位类型，数据库中不存在");
                    continue;
                }
                //当岗位类型是驾驶员IC卡时校验身份证是否为空
                if ("驾驶员(IC卡)".equals(type) && StringUtils.isBlank(identity)) {
                    professional.setErrorMsg("当岗位类型是驾驶员IC卡时，身份证不能为空");
                    continue;
                }
            } else {
                professional.setType(null);
                professional.setPositionType(null);
            }
            //准驾车型
            String drivingType = professional.getDrivingType();
            if (StringUtils.isNotBlank(drivingType) && drivingType.length() > 10) {
                professional.setErrorMsg("准驾车型错误，长度必须小等于10");
                continue;
            }
            //校验准驾有效期起
            String drivingStartDateStr = professional.getDrivingStartDateStr();
            Date drivingStartDate = null;
            if (StringUtils.isNotBlank(drivingStartDateStr)) {
                try {
                    drivingStartDate = yyyyMMdd.parse(drivingStartDateStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("准驾有效期起格式错误");
                    continue;
                }
                if (drivingStartDate.getTime() > maxDate.getTime()) {
                    professional.setErrorMsg("准驾有效期起错误，准驾有效期起不能大于9999-12-31");
                    continue;
                }
                professional.setDrivingStartDate(drivingStartDate);
            }
            //校验准驾有效期至
            String drivingEndDateStr = professional.getDrivingEndDateStr();
            Date drivingEndDate = null;
            if (StringUtils.isNotBlank(drivingEndDateStr)) {
                try {
                    drivingEndDate = yyyyMMdd.parse(drivingEndDateStr);
                } catch (ParseException e) {
                    professional.setErrorMsg("准驾有效期至格式错误");
                    continue;
                }
                if (drivingEndDate.getTime() > maxDate.getTime()) {
                    professional.setErrorMsg("准驾有效期至错误，准驾有效期起不能大于9999-12-31");
                    continue;
                }
                professional.setDrivingEndDate(drivingEndDate);
            }
            if (drivingStartDate != null && drivingEndDate != null && drivingStartDate.getTime() > drivingEndDate
                .getTime()) {
                professional.setErrorMsg("准驾有效期起 不能超过 准驾有效期至1");
                continue;
            }
            //校验提前提醒天数
            Integer remindDays = professional.getRemindDays();
            if (Objects.nonNull(remindDays) && (remindDays < 0 || remindDays > 9999)) {
                professional.setErrorMsg("提前提醒天数错误，范围0-9999");
                continue;
            }
            // 验证从业人员身份证号
            if (identityIsNotBlank) {
                if (!identity.matches(identityReg)) {
                    professional.setErrorMsg("身份证号格式不对");
                    continue;
                }
                if (importListIsContainsIdentity) {
                    professional.setErrorMsg("身份证号重复");
                    continue;
                }
                // 提取出身份证中的出生日期
                // 通过Pattern获得Matcher
                Matcher birthDateMather = birthDatePattern.matcher(identity);
                // 通过Matcher获得用户的出生年月日
                if (!birthDateMather.find()) {
                    professional.setErrorMsg("身份证号格式不对");
                    continue;
                }
                String year = birthDateMather.group(1);
                String month = birthDateMather.group(2);
                String date = birthDateMather.group(3);
                GregorianCalendar gc = new GregorianCalendar();
                // 身份证生日不在有效范围
                boolean identityFormatErrorFlag = (gc.get(Calendar.YEAR) - Integer.parseInt(year)) > 150
                    || (gc.getTime().getTime() - yyyyMMdd.parse(year + "-" + month + "-" + date).getTime()) < 0;
                boolean monthFormatErrorFlag = Integer.parseInt(month) > 12 || Integer.parseInt(month) == 0;
                boolean dateFormatErrorFlag = Integer.parseInt(date) > 31 || Integer.parseInt(date) == 0;
                if (dateFormatErrorFlag || monthFormatErrorFlag || identityFormatErrorFlag) {
                    professional.setErrorMsg("身份证号出生日期不在有效范围");
                    continue;
                }

                String nation = professional.getNation();
                if (StringUtils.isNotEmpty(nation)) {
                    if (!nationMap.containsKey(nation)) {
                        professional.setErrorMsg("民族不存在");
                        continue;
                    }
                    professional.setNationId(nationMap.get(nation));
                }

                String education = professional.getEducation();
                if (StringUtils.isNotEmpty(education)) {
                    if (!educationMap.containsKey(education)) {
                        professional.setErrorMsg("文化程度不存在");
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
            // 性别
            String gender = professional.getGender();
            professionalDO.setGender(Objects.equals("女", gender) ? "2" : "1");
            //状态
            String state = professional.getState();
            professionalDO.setState(Objects.equals("停用", state) ? "2" : Objects.equals("离职", state) ? "1" : "0");
            professionalDOs.add(professionalDO);
            message.append("导入从业人员 : ").append(name).append(" ( @").append(orgName).append(" ) <br/> ");
        }
    }


    /**
     * 导出从业人员
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
                if (null != exportDTO.getIssueCertificateDate()) { // 从业人员资格证发证日期
                    exportDTO.setIssueCertificateDateStr(
                            Converter.toString(exportDTO.getIssueCertificateDate(), "yyyy-MM-dd"));
                }
                if (null != exportDTO.getIcCardEndDate()) { // 从业资格证证有效期
                    exportDTO
                            .setIcCardEndDateStr(Converter.toString(exportDTO.getIcCardEndDate(), "yyyy-MM-dd"));
                }
                //生日
                if (null != exportDTO.getBirthday()) {
                    exportDTO
                            .setBirthdayStr(Converter.toString(exportDTO.getBirthday(), "yyyy-MM-dd"));
                }
                //入职日期
                if (null != exportDTO.getHiredate()) {
                    exportDTO
                            .setHiredateStr(Converter.toString(exportDTO.getHiredate(), "yyyy-MM-dd"));
                }
                //准驾有效期起
                if (null != exportDTO.getDrivingStartDate()) {
                    exportDTO.setDrivingStartDateStr(
                            Converter.toString(exportDTO.getDrivingStartDate(), "yyyy-MM-dd"));
                }
                //准驾有效期至
                if (null != exportDTO.getDrivingEndDate()) {
                    exportDTO
                            .setDrivingEndDateStr(Converter.toString(exportDTO.getDrivingEndDate(), "yyyy-MM-dd"));
                }
                // 处理性别数据
                if (StringUtils.isNotEmpty(exportDTO.getGender())) {
                    if ("1".equals(exportDTO.getGender())) {
                        exportDTO.setGender("男");
                    } else if ("2".equals(exportDTO.getGender())) {
                        exportDTO.setGender("女");
                    }
                } else {
                    exportDTO.setGender("");
                }
                if (Converter.toBlank(exportDTO.getState()).equals("0")) {
                    exportDTO.setState("正常");
                } else if (Converter.toBlank(exportDTO.getState()).equals("1")) {
                    exportDTO.setState("离职");
                } else if (Converter.toBlank(exportDTO.getState()).equals("2")) {
                    exportDTO.setState("停用");
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
            // 输出导文件
            out = response.getOutputStream();
            export.write(out);// 将文档对象写入文件输出流
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return true;
    }

    /**
     * 根据企业id查询从业人员
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
     * 获取从业人员树
     * @param isOrg
     * @return
     */
    @Override
    public String getTree(String isOrg) {
        // 获取当前用户所在组织及下级组织
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
     * 根据名字校验从业人员存在异常
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
     * 通过身份证查找从业人员
     * @param identity 身份证号
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
     * @param name 从业人员姓名
     * @return 从业人员list
     * @throws Exception 异常
     */
    @Override
    public List<ProfessionalDO> getProfessionalsByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            return newProfessionalsDao.getProfessionalsByName(name);
        }
        return null;
    }

    /**
     * 根据id获取从业人员
     * @param id
     * @return
     */
    @Override
    public ProfessionalDTO getProfessionalsById(String id) {
        return newProfessionalsDao.getProfessionalById(id);
    }

    /**
     * 驾驶员统计模块的树
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

        // 组装组织树结构
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
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(currentUseOrgList, type));
        obj.put("size", professionalDos.size());
        obj.put("tree", result);
        return obj;
    }

    /**
     * 组装从业人员叶子节点数据
     * @param type
     * @param organization
     * @param icCardDriver
     * @return
     */
    private JSONObject getDriverNode(String type, OrganizationLdap organization, ProfessionalDO icCardDriver) {
        // 组装分组树
        JSONObject proObj = new JSONObject();

        proObj.put("id", icCardDriver.getId());
        proObj.put("pId", organization.getId().toString());
        proObj.put("name", icCardDriver.getName());
        // 有子节点
        proObj.put("cardNumber", icCardDriver.getIdentity());
        proObj.put("photograph", icCardDriver.getPhotograph());
        proObj.put("type", "people");
        proObj.put("iconSkin", "peopleSkin");
        // assignmentObj.put("open", false);
        // 根节点是否可选
        if ("single".equals(type)) {
            proObj.put("nocheck", true);
        }
        // 有子节点
        proObj.put("isParent", false);
        return proObj;
    }

    /**
     * 根据企业
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
        //根据企业id获取 子企业

        Set<String> groupSet = new HashSet<>(organizationService.getChildOrgIdByUuid(parentId));
        Set<String> allProIds = getRedisOrgProfessionalId(groupSet);
        List<ProfessionalDO> professionalDOS =
            allProIds.isEmpty() ? new ArrayList<>() : newProfessionalsDao.findAllIcCarDriver(allProIds, null);

        return professionalDOS.size();
    }

    /**
     * 实时监控
     * @param vehicleId
     * @return
     */
    @Override
    public List<ProfessionalDTO> getRiskProfessionalsInfo(String vehicleId) {
        List<ProfessionalDTO> professionalDTOS = new ArrayList<>();
        String insertCardInfo = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vehicleId));
        if (StrUtil.isNotBlank(insertCardInfo)) {
            //正在驾驶
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

        //已经拔卡
        String lastInsertCardDriverId = lastInsertCardDriverId(vehicleId);
        List<String> professionalIds = getProfessionalIdsByVehicleId(vehicleId);

        ProfessionalDTO form;
        for (String professionalId : professionalIds) {
            //如果是插卡司机则跳过，后面会新增到前面来
            if (professionalId.equals(lastInsertCardDriverId)
                || (form = getAdasProfessionalShow(professionalId)) == null) {
                continue;
            }
            //如果是ic卡司机则添加数据到最前面
            if (form.getType() != null && form.getType().equals(IC_TYPE)) {
                professionalDTOS.add(0, form);
            } else {
                professionalDTOS.add(form);
            }
        }
        if (StrUtil.isNotBlank(lastInsertCardDriverId)) {
            //最后一次拔卡的司机放在最前面
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
     * 给从业人员排序
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
        //获取和计算该车最后一次插拔卡记录
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
