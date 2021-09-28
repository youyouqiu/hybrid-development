package com.zw.platform.basic.service.impl;

import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.VehiclePurposeDO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.VehiclePurposeDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.VehiclePurposeService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.VehiclePurposeForm;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 车辆运营类别逻辑实现类
 * @author zhangjuan 2020-10-14
 */
@Service
public class VehiclePurposeServiceImpl implements VehiclePurposeService, CacheService {
    private static final Logger log = LogManager.getLogger(VehiclePurposeServiceImpl.class);
    private static final String[] EXCEL_HEAD_FIELD = { "运营类别", "备注" };
    /**
     * 运营类别的正则表达式
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z\u4E00-\u9FA5]{2,20}$");
    @Autowired
    private VehiclePurposeDao vehiclePurposeDao;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Override
    public boolean add(VehiclePurposeDTO vehiclePurposeDTO) throws BusinessException {
        if (isExistPurpose(null, vehiclePurposeDTO.getPurposeCategory())) {
            throw new BusinessException("", "sorry！您输入的运营类别已存在，请重新输入...");
        }

        //封装数据库插入实体
        VehiclePurposeDO vehiclePurposeDO = new VehiclePurposeDO();
        vehiclePurposeDTO.setId(UUID.randomUUID().toString());
        BeanUtils.copyProperties(vehiclePurposeDTO, vehiclePurposeDO);
        vehiclePurposeDO.setCreateDataUsername(SystemHelper.getCurrentUsername());

        boolean isSuccess = vehiclePurposeDao.insert(vehiclePurposeDO);
        if (!isSuccess) {
            throw new BusinessException("", "添加失败！");
        }

        // 维护车辆运营类别的缓存
        TypeCacheManger.getInstance().saveVehiclePurpose(vehiclePurposeDTO);

        // 记录操作日志
        String msg = "新增运营类别：" + vehiclePurposeDTO.getPurposeCategory();
        String ipAddress = new GetIpAddr().getIpAddr(request);
        logService.addLog(ipAddress, msg, "3", "", "", "");

        return true;
    }

    @Override
    public boolean update(VehiclePurposeDTO vehiclePurposeDTO) throws BusinessException {
        //获取历史的运营类别信息
        VehiclePurposeDO oldPurpose = vehiclePurposeDao.getById(vehiclePurposeDTO.getId());
        if (Objects.isNull(oldPurpose)) {
            throw new BusinessException("", "对象不存在！");
        }

        String purposeName = vehiclePurposeDTO.getPurposeCategory();
        if (isExistPurpose(vehiclePurposeDTO.getId(), purposeName)) {
            throw new BusinessException("", "sorry！您输入的运营类别已存在，请重新输入...");
        }

        //封装数据库更新实体
        VehiclePurposeDO vehiclePurposeDO = new VehiclePurposeDO();
        BeanUtils.copyProperties(vehiclePurposeDTO, vehiclePurposeDO);
        vehiclePurposeDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());

        boolean isSuccess = vehiclePurposeDao.update(vehiclePurposeDO);
        if (!isSuccess) {
            throw new BusinessException("", "更新失败！");
        }

        //若修改名称后，需要同步修改车辆缓存中的名称
        if (!Objects.equals(oldPurpose.getPurposeCategory(), purposeName)) {
            Set<String> vehicleIds = newVehicleDao.getByVehiclePurposeId(vehiclePurposeDTO.getId());
            List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_INFO.ofs(vehicleIds);
            Map<String, String> updateMap = ImmutableMap.of("vehiclePurposeName", purposeName);
            RedisHelper.batchAddToHash(redisKeys, updateMap);
        }
        // 维护车辆运营类别的缓存
        TypeCacheManger.getInstance().saveVehiclePurpose(vehiclePurposeDTO);

        // 记录操作日志
        String msg = "修改运营类别：" + oldPurpose.getPurposeCategory();
        if (!Objects.equals(oldPurpose.getPurposeCategory(), vehiclePurposeDTO.getPurposeCategory())) {
            msg = msg + " 修改为：" + vehiclePurposeDTO.getPurposeCategory();
        }
        String ipAddress = new GetIpAddr().getIpAddr(request);
        logService.addLog(ipAddress, msg, "3", "", "-", "");

        return true;
    }

    @Override
    public boolean delete(String id) throws BusinessException {
        VehiclePurposeDO vehiclePurposeDO = vehiclePurposeDao.getById(id);
        if (Objects.isNull(vehiclePurposeDO)) {
            throw new BusinessException("", "对象不存在！");
        }

        VehiclePurposeDO deletePurpose = new VehiclePurposeDO();
        deletePurpose.setId(id);
        deletePurpose.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        deletePurpose.setFlag(0);

        boolean isSuccess = vehiclePurposeDao.update(deletePurpose);
        if (!isSuccess) {
            throw new BusinessException("", "删除失败！");
        }

        TypeCacheManger.getInstance().removeVehiclePurpose(id);
        String msg = "删除运营类别：" + vehiclePurposeDO.getPurposeCategory();
        String ipAddress = new GetIpAddr().getIpAddr(request);
        logService.addLog(ipAddress, msg, "3", "", "-", "");
        return false;
    }

    @Override
    public int delBatch(Collection<String> ids) {
        List<VehiclePurposeDO> purposeList = vehiclePurposeDao.getByIds(ids);
        if (purposeList.isEmpty()) {
            return 0;
        }
        int count = vehiclePurposeDao.delBatch(ids);

        if (Objects.equals(0, count)) {
            return 0;
        }
        //记录日志
        StringBuilder message = new StringBuilder();
        for (VehiclePurposeDO vehiclePurposeDO : purposeList) {
            TypeCacheManger.getInstance().removeVehiclePurpose(vehiclePurposeDO.getId());
            message.append("删除运营类别 : ").append(vehiclePurposeDO.getPurposeCategory()).append(" <br/>");
        }
        String ipAddress = new GetIpAddr().getIpAddr(request);
        logService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除运营类别");

        return count;
    }

    @Override
    public VehiclePurposeDTO getById(String id) {
        VehiclePurposeDO vehiclePurposeDO = vehiclePurposeDao.getById(id);
        if (Objects.isNull(vehiclePurposeDO)) {
            return null;
        }
        VehiclePurposeDTO vehiclePurposeDTO = new VehiclePurposeDTO();
        BeanUtils.copyProperties(vehiclePurposeDO, vehiclePurposeDTO);
        return vehiclePurposeDTO;
    }

    @Override
    public VehiclePurposeDTO getByName(String name) {
        VehiclePurposeDO vehiclePurposeDO = vehiclePurposeDao.getByName(name);
        if (vehiclePurposeDO == null) {
            return null;
        }
        VehiclePurposeDTO vehiclePurposeDTO = new VehiclePurposeDTO();
        BeanUtils.copyProperties(vehiclePurposeDO, vehiclePurposeDTO);
        return vehiclePurposeDTO;
    }

    @Override
    public boolean isExistPurpose(String id, String purposeCategory) {
        VehiclePurposeDO vehiclePurposeDO = vehiclePurposeDao.getByName(purposeCategory);
        return vehiclePurposeDO != null && !Objects.equals(vehiclePurposeDO.getId(), id);
    }

    @Override
    public Page<VehiclePurposeDTO> getListByKeyWord(VehiclePurposeQuery query) {
        String keyword = StringUtil.mysqlLikeWildcardTranslation(query.getPurposeCategory());
        List<VehiclePurposeDTO> purposeList = vehiclePurposeDao.getByKeyword(keyword);
        return RedisQueryUtil.getListToPage(purposeList, query, purposeList.size());
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // 表头
        List<String> headList = new ArrayList<>(Arrays.asList(EXCEL_HEAD_FIELD));
        // 必填字段
        List<String> requiredList = new ArrayList<>();
        requiredList.add(EXCEL_HEAD_FIELD[0]);

        // 默认设置一条数据
        List<Object> exportList = new ArrayList<>();
        exportList.add("载客车");
        exportList.add("专门用作人员乘坐的汽车");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(16);
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    @Override
    public Map<String, Object> importExcel(MultipartFile file) throws Exception {
        //解析excel文件，获取数据列表值
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        //校验模板是否正确
        String firstHeadField = importExcel.getCellValue(importExcel.getRow(0), 0).toString();
        if (firstHeadField == null || !firstHeadField.startsWith(EXCEL_HEAD_FIELD[0])) {
            throw new BusinessException("", "运营类别导入模板不正确！");
        }

        List<VehiclePurposeDTO> purposeList = importExcel.getDataList(VehiclePurposeDTO.class);
        if (CollectionUtils.isEmpty(purposeList)) {
            throw new BusinessException("", "运营类别导入数据为空");
        }

        //获取所有的车辆运营类别
        List<VehiclePurposeDTO> existList = vehiclePurposeDao.getByKeyword(null);
        Set<String> nameSet = existList.stream().map(VehiclePurposeDTO::getPurposeCategory).collect(Collectors.toSet());

        StringBuilder errorMsgBuilder = new StringBuilder();
        StringBuilder messageBuilder = new StringBuilder();
        List<VehiclePurposeDO> importList = new ArrayList<>();
        Map<String, Integer> nameRowMap = new HashMap<>(16);
        List<VehiclePurposeDTO> cacheList = new ArrayList<>(16);
        //进行导入参数校验
        for (int i = 0; i < purposeList.size(); i++) {
            VehiclePurposeDTO purpose = purposeList.get(i);
            String name = purpose.getPurposeCategory();

            String errorMsg = checkName(name, nameSet, nameRowMap, i + 1);
            if (StringUtils.isNotBlank(errorMsg)) {
                errorMsgBuilder.append(errorMsg);
                continue;
            }

            String remark = purpose.getDescription();
            if (Converter.toBlank(remark).length() > 50) {
                errorMsgBuilder.append("第").append(i + 1).append("条数据,运营详情长度长度超过了50！<br/>");
                continue;
            }

            //封装插入数据的实体
            String id = UUID.randomUUID().toString();
            VehiclePurposeDO vehiclePurposeDO = new VehiclePurposeDO(id, name, remark, null);
            vehiclePurposeDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
            importList.add(vehiclePurposeDO);

            purpose.setId(id);
            cacheList.add(purpose);

            messageBuilder.append("导入运营类别 : ").append(name).append(" <br/>");
        }

        //维护本地缓存
        if (importList.isEmpty()) {
            return buildImportResult(false, errorMsgBuilder.toString(), "成功导入0条数据。");
        }

        //车辆运营类型数据进行批量入库
        int count = vehiclePurposeDao.addBatch(importList);

        for (VehiclePurposeDTO purpose : cacheList) {
            TypeCacheManger.getInstance().saveVehiclePurpose(purpose);
        }

        //记录日志
        String ipAddress = new GetIpAddr().getIpAddr(request);
        logService.addLog(ipAddress, messageBuilder.toString(), "3", "batch", "导入运营类别");

        String msg = "导入成功" + count + "条数据，导入失败" + (purposeList.size() - count) + "条数据";
        return buildImportResult(true, errorMsgBuilder.toString(), msg);
    }

    private Map<String, Object> buildImportResult(boolean isSuccess, String errorMsg, String resultInfo) {
        Map<String, Object> result = new HashMap<>(16);
        result.put("success", isSuccess);
        result.put("errorMsg", errorMsg);
        result.put("resultInfo", resultInfo);
        return result;
    }

    private String checkName(String name, Set<String> existNames, Map<String, Integer> nameRowMap, int row) {
        //校验导入数据中是否有重复的
        if (nameRowMap.containsKey(name)) {
            return "第" + row + "行运营类别和第" + nameRowMap.get(name) + "行运营类别重复，值为：" + name;
        }
        nameRowMap.put(name, row);

        //检验当前运营类别在数据库是否已存在
        if (existNames.contains(name)) {
            return "第" + row + "条数据,运营类别为“" + name + "”已存在！！<br/>";
        }

        //校验运营类别名称的格式
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return "第" + row + "条数据,运营类别名称长度不在2—20之间或包含了特殊符号！<br/>";
        }
        return null;
    }

    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        List<VehiclePurposeDTO> purposeList = vehiclePurposeDao.getByKeyword(null);

        ExportExcel export = new ExportExcel(null, VehiclePurposeForm.class, 1);
        export.setDataList(purposeList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
        return true;
    }

    @Override
    public List<VehiclePurposeDTO> getAllByKeyWord(String keyword) {
        return vehiclePurposeDao.getByKeyword(null);
    }

    @Override
    @PostConstruct
    public void initCache() {
        log.info("开始进行车辆运营类别的本地缓存初始化.");
        List<VehiclePurposeDTO> purposeList = vehiclePurposeDao.getByKeyword(null);
        //清除缓存
        TypeCacheManger.getInstance().clearVehiclePurpose();

        //进行缓存
        for (VehiclePurposeDTO vehiclePurposeDTO : purposeList) {
            TypeCacheManger.getInstance().saveVehiclePurpose(vehiclePurposeDTO);
        }
        log.info("结束车辆运营类别的本地缓存初始化.");
    }
}
