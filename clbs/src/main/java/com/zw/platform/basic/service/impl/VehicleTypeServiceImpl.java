package com.zw.platform.basic.service.impl;

import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.query.VehicleTypePageQuery;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.basic.service.VehicleTypeService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 车辆类型逻辑实现类
 * @author zhangjuan
 */
@Service
public class VehicleTypeServiceImpl implements VehicleTypeService, CacheService {
    private static final Logger log = LogManager.getLogger(VehicleTypeServiceImpl.class);
    private static final String[] EXCEL_HEAD_FIELD = { "车辆类别", "车辆类型", "保养里程间隔(KM)", "备注" };
    private static final Pattern PATTERN = Pattern.compile("^[0-9a-zA-Z_\u4E00-\u9FA5]{0,20}$");
    private static final Integer BIGEST_SERVICE_CYCLE = 99999;

    private static final Integer LEAST_SERVICE_CYCLE = 0;
    @Autowired
    private NewVehicleTypeDao vehicleTypeDao;

    @Autowired
    private LogSearchService logService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @PostConstruct
    @Override
    public void initCache() {
        log.info("开始进行车辆类型的本地缓存初始化.");
        List<VehicleTypeDTO> vehicleTypes = vehicleTypeDao.getByKeyword(null);
        TypeCacheManger.getInstance().clearVehicleType();
        if (vehicleTypes.isEmpty()) {
            return;
        }

        for (VehicleTypeDTO vehicleType : vehicleTypes) {
            TypeCacheManger.getInstance().saveVehicleType(vehicleType);
        }
        log.info("结束车辆类型的本地缓存初始化.");
    }

    @Override
    public boolean add(VehicleTypeDTO typeDTO) throws BusinessException {
        VehicleCategoryDTO vehicleCategory = TypeCacheManger.getInstance().getVehicleCategory(typeDTO.getCategoryId());
        if (Objects.isNull(vehicleCategory)) {
            throw new BusinessException("", "该类型的类别不存在,请检查");
        }
        if (isExistType(null, vehicleCategory.getCategory(), typeDTO.getType())) {
            throw new BusinessException("", "该类型已经存在");
        }

        VehicleTypeDO vehicleTypeDO = new VehicleTypeDO(typeDTO);
        boolean isSuccess = vehicleTypeDao.insert(vehicleTypeDO);
        if (!isSuccess) {
            return false;
        }

        //维护本地缓存
        typeDTO.setId(vehicleTypeDO.getId());
        typeDTO.setCategory(typeDTO.getCategory());
        TypeCacheManger.getInstance().saveVehicleType(typeDTO);

        //记录日志
        logService.addLog(getIp(), "新增车型：" + typeDTO.getType(), "3", "", "-", "");
        return true;
    }

    @Override
    public boolean isExistType(String id, String category, String type) {
        VehicleTypeDTO vehicleType = vehicleTypeDao.getByName(category, type);
        return Objects.nonNull(vehicleType) && !Objects.equals(vehicleType.getId(), id);
    }

    @Override
    public boolean update(VehicleTypeDTO typeDTO) throws BusinessException {
        VehicleTypeDTO oldType = vehicleTypeDao.getById(typeDTO.getId());
        if (Objects.isNull(oldType)) {
            throw new BusinessException("", "对象不存在");
        }

        VehicleCategoryDTO vehicleCategory = TypeCacheManger.getInstance().getVehicleCategory(typeDTO.getCategoryId());
        if (Objects.isNull(vehicleCategory)) {
            throw new BusinessException("", "该类型的类别不存在,请检查");
        }

        if (isExistType(typeDTO.getId(), vehicleCategory.getCategory(), typeDTO.getType())) {
            throw new BusinessException("", "该类型已经存在");
        }

        VehicleTypeDO vehicleTypeDO = new VehicleTypeDO(typeDTO);
        boolean isSuccess = vehicleTypeDao.update(vehicleTypeDO);
        if (!isSuccess) {
            return false;
        }

        //维护本地缓存
        typeDTO.setCategory(vehicleCategory.getCategory());
        TypeCacheManger.getInstance().saveVehicleType(vehicleTypeDao.getById(typeDTO.getId()));
        //若车辆类型的名称发生改变，更新车辆信息缓存的车辆类型信息
        if (!Objects.equals(oldType.getType(), typeDTO.getType())) {
            Set<String> vehicleIds = newVehicleDao.getByVehicleTypeId(typeDTO.getId());
            if (!vehicleIds.isEmpty()) {
                List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_INFO.ofs(vehicleIds);
                Map<String, String> updateMap = ImmutableMap.of("vehicleTypeName", typeDTO.getType());
                RedisHelper.batchAddToHash(redisKeys, updateMap);
            }
        }

        String msg = "修改车型：" + oldType.getType();
        msg = Objects.equals(typeDTO.getType(), oldType.getType()) ? msg : msg + " 修改为：" + typeDTO.getType();
        logService.addLog(getIp(), msg, "3", "", "-", "");
        return true;
    }

    @Override
    public boolean delete(String id) throws BusinessException {
        VehicleTypeDTO typeDTO = vehicleTypeDao.getById(id);
        if (Objects.isNull(typeDTO)) {
            throw new BusinessException("", "对象不存在");
        }

        //绑定了子类型
        if (isBindSubType(id)) {
            throw new BusinessException("", "车辆类型" + typeDTO.getType() + "已绑定子类型,请先解除绑定再删除!");
        }

        //绑定了车辆的车型
        Set<String> vehicleBindTypes = getVehicleBindTypes(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(vehicleBindTypes)) {
            throw new BusinessException("", "车辆类型" + typeDTO.getType() + "已绑定车辆,请先解除绑定再删除!");
        }

        //删除车型
        vehicleTypeDao.deleteByBatch(Collections.singletonList(id));
        TypeCacheManger.getInstance().removeVehicleType(id);

        logService.addLog(getIp(), "删除车型：" + typeDTO.getType(), "3", "", "-", "");
        return true;
    }

    @Override
    public String deleteBatch(Collection<String> ids) {
        List<VehicleTypeDTO> typeList = vehicleTypeDao.getByIds(ids);
        if (typeList.isEmpty()) {
            return "选择删除的车型不存在，请确认";
        }

        //获取已经绑定的车辆的车型
        Set<String> bindVehicleSet = getVehicleBindTypes(ids);

        List<String> deleteIds = new ArrayList<>();
        List<String> bindSubTypeTypes = new ArrayList<>();
        List<String> bindVehicleTypes = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        for (VehicleTypeDTO typeDTO : typeList) {
            if (bindVehicleSet.contains(typeDTO.getId())) {
                bindVehicleTypes.add(typeDTO.getType());
                continue;
            }

            if (isBindSubType(typeDTO.getId())) {
                bindSubTypeTypes.add(typeDTO.getType());
                continue;
            }
            deleteIds.add(typeDTO.getId());
            msg.append("删除车型：").append(typeDTO.getType()).append(" <br/>");
            TypeCacheManger.getInstance().removeVehicleType(typeDTO.getId());
        }

        if (!deleteIds.isEmpty()) {
            vehicleTypeDao.deleteByBatch(deleteIds);
            logService.addLog(getIp(), msg.toString(), "3", "batch", "批量删除车型");
        }

        String buildMsg = "";
        if (!bindSubTypeTypes.isEmpty()) {
            buildMsg = "车辆类型" + StringUtils.join(bindSubTypeTypes, ",") + "已绑定子类型,请先解除绑定再删除!<br/>";
        }

        if (!bindVehicleTypes.isEmpty()) {
            buildMsg = buildMsg + "车辆类型" + StringUtils.join(bindVehicleTypes, ",") + "已绑定车辆,请先解除绑定再删除!";
        }

        return buildMsg;
    }

    @Override
    public boolean isBindSubType(String id) {
        List<VehicleSubTypeDTO> subTypes = TypeCacheManger.getInstance().getVehicleSubTypes(id);
        return !subTypes.isEmpty();
    }

    @Override
    public Set<String> getVehicleBindTypes(Collection<String> ids) {
        return vehicleTypeDao.getVehicleBindTypeList(ids);
    }

    @Override
    public VehicleTypeDTO getById(String id) {
        return vehicleTypeDao.getById(id);
    }

    @Override
    public Page<VehicleTypeDTO> getByPage(VehicleTypePageQuery query) {
        String keyword = StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam());
        query.setSimpleQueryParam(keyword);
        return PageHelperUtil.doSelect(query, () -> vehicleTypeDao.getByPage(query));
    }

    @Override
    public List<VehicleTypeDTO> getListByKeyword(String keyword) {
        return vehicleTypeDao.getByKeyword(StringUtil.mysqlLikeWildcardTranslation(keyword));
    }

    @Override
    public boolean export(HttpServletResponse response) throws Exception {
        List<VehicleTypeDTO> typeList = vehicleTypeDao.getByKeyword(null);
        ExportExcel export = new ExportExcel(null, VehicleTypeDTO.class, 1);
        export.setDataList(typeList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
        return true;
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        // 表头
        List<String> headList = new ArrayList<>(Arrays.asList(EXCEL_HEAD_FIELD));
        // 必填字段
        List<String> requiredList = new ArrayList<>();
        requiredList.add("车辆类别");
        requiredList.add("车辆类型");

        Map<String, String[]> selectMap = new HashMap<>(16);
        List<VehicleCategoryDTO> categoryList = TypeCacheManger.getInstance().getVehicleCategories(null);
        String[] categoryArr = new String[categoryList.size()];
        for (int i = 0; i < categoryList.size(); i++) {
            categoryArr[i] = categoryList.get(i).getCategory();
        }
        selectMap.put("车辆类别", categoryArr);

        List<Object> exportList = new ArrayList<>();
        exportList.add(categoryArr[0]);
        exportList.add("类型");
        exportList.add(10000);
        exportList.add("");
        ExportExcelUtil.writeTemplateToFile(headList, requiredList, selectMap, exportList, response);
        return true;
    }

    @Override
    public Map<String, Object> importExcel(MultipartFile file) throws Exception {
        //解析excel文件，获取数据列表值
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        //校验模板是否正确
        String firstHeadField = importExcel.getCellValue(importExcel.getRow(0), 0).toString();
        if (firstHeadField != null && !firstHeadField.startsWith(EXCEL_HEAD_FIELD[0])) {
            throw new BusinessException("", "车辆类型导入模板不正确！");
        }

        List<VehicleTypeDTO> dataList = importExcel.getDataList(VehicleTypeDTO.class);
        Map<String, String> categoryMap = getCategoryNameIdMap();
        Set<String> typeNames =
            getListByKeyword("").stream().map(o -> o.getCategory() + o.getType()).collect(Collectors.toSet());

        Map<String, Integer> repeatMap = new HashMap<>((int) (dataList.size() / 0.75) + 1);
        StringBuilder errMsg = new StringBuilder();
        StringBuilder msg = new StringBuilder();
        List<VehicleTypeDO> vehicleTypeList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            VehicleTypeDTO typeDTO = dataList.get(i);
            //校验重复性
            String checkMsg = checkRepeat(typeDTO, repeatMap, i + 1);
            if (StringUtils.isNotBlank(checkMsg)) {
                errMsg.append(checkMsg);
                continue;
            }

            //校验车辆类别
            if (StringUtils.isBlank(typeDTO.getCategory())) {
                errMsg.append("第").append(i + 1).append("条数据车辆类别字段未填<br/>");
                continue;
            }
            if (StringUtils.isBlank(typeDTO.getType())) {
                errMsg.append("第").append(i + 1).append("条数据车辆类型字段未填<br/>");
                continue;
            }

            String categoryId = categoryMap.get(typeDTO.getCategory());
            if (StringUtils.isBlank(categoryId)) {
                errMsg.append("第").append(i + 1).append("条数据车辆类别(").append(typeDTO.getCategory()).append(")不存在<br/>");
                continue;
            }

            Matcher matcher = PATTERN.matcher(typeDTO.getType());
            if (!matcher.matches()) {
                errMsg.append("第").append(i + 1).append("条数据车辆类型(").append(typeDTO.getType())
                    .append("包含特殊符号或超长，请输入1-20位中文、英文、数字和下划线组成的字符串<br/>");
                continue;
            }

            Integer serviceCycle = typeDTO.getServiceCycle();
            if (Objects.nonNull(serviceCycle)) {
                if (serviceCycle > BIGEST_SERVICE_CYCLE || serviceCycle <= LEAST_SERVICE_CYCLE) {
                    errMsg.append("第").append(i + 1).append("条，保养里程间隔(KM)“").append(serviceCycle)
                        .append("”最大请输入五位正整数<br/>");
                    continue;
                }
            }

            if (typeNames.contains(typeDTO.getCategory() + typeDTO.getType())) {
                errMsg.append("第").append(i + 1).append("条,类型").append(typeDTO.getType()).append("在车辆类别")
                    .append(typeDTO.getCategory()).append("下已经存在<br/>");
                continue;
            }

            typeDTO.setCategoryId(categoryId);
            vehicleTypeList.add(new VehicleTypeDO(typeDTO));
            TypeCacheManger.getInstance().saveVehicleType(typeDTO);
            msg.append("导入车辆类型 : ").append(typeDTO.getType()).append(" <br/>");
        }

        if (vehicleTypeList.isEmpty()) {
            return buildImportResult(false, errMsg.toString(), "成功导入0条数据。");
        }

        int count = vehicleTypeDao.addBatch(vehicleTypeList);
        logService.addLog(getIp(), msg.toString(), "3", "batch", "导入车辆类型");

        String resultInfo = "导入成功" + count + "条数据,导入失败" + (dataList.size() - count) + "条数据。";
        return buildImportResult(true, errMsg.toString(), resultInfo);
    }

    @Override
    public VehicleTypeDTO getByName(String category, String type) {
        return vehicleTypeDao.getByName(category, type);
    }

    @Override
    public List<VehicleTypeDTO> getByCategoryId(String categoryId) {
        return vehicleTypeDao.getByCategoryId(categoryId);
    }

    @Override
    public List<VehicleTypeDTO> getByStandard(Integer standard) {
        return vehicleTypeDao.getByStandard(standard);
    }

    private Map<String, Object> buildImportResult(boolean isSuccess, String errorMsg, String resultInfo) {
        Map<String, Object> result = new HashMap<>(16);
        result.put("errorMsg", errorMsg);
        result.put("resultInfo", resultInfo);
        result.put("success", isSuccess);
        return result;
    }

    private String checkRepeat(VehicleTypeDTO type, Map<String, Integer> repeatMap, int row) {
        String key = type.getCategory() + type.getType() + "";
        if (!repeatMap.containsKey(key)) {
            return null;
        }
        return "第" + row + "行跟第" + repeatMap.get(key) + "行重复，值是：" + type.getType() + "<br/>";
    }

    private Map<String, String> getCategoryNameIdMap() {
        List<VehicleCategoryDTO> categoryList = TypeCacheManger.getInstance().getVehicleCategories(null);
        Map<String, String> nameIdMap = new HashMap<>((int) (categoryList.size() / 0.75) + 1);
        for (VehicleCategoryDTO category : categoryList) {
            nameIdMap.put(category.getCategory(), category.getId());
        }
        return nameIdMap;
    }

    private String getIp() {
        return new GetIpAddr().getIpAddr(request);
    }
}
