package com.zw.platform.service.basicinfo.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.lkyw.service.videoCarousel.impl.VideoCarouselServiceImpl;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.FuelTypeDO;
import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.domain.VehiclePurposeDO;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.repository.FuelTypeDao;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewVehicleCategoryDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.NewVehicleSubTypeDao;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.basic.repository.PeopleDao;
import com.zw.platform.basic.repository.VehiclePurposeDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.MonitorAccStatus;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.VehiclePurpose;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import com.zw.platform.domain.basicinfo.form.AssignmentUserForm;
import com.zw.platform.domain.basicinfo.form.BrandForm;
import com.zw.platform.domain.basicinfo.form.BrandModelsForm;
import com.zw.platform.domain.basicinfo.form.SynchronizeVehicleForm;
import com.zw.platform.domain.basicinfo.form.VehiclePurposeForm;
import com.zw.platform.domain.basicinfo.query.VehiclePurposeQuery;
import com.zw.platform.domain.core.Operations;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.repository.core.OperationDao;
import com.zw.platform.repository.modules.BrandDao;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.redis.RedisAssignService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.CommonTypeUtils;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.talkback.domain.basicinfo.LeaveJobPersonnel;
import com.zw.ws.common.PublicVariable;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 车辆serviceImpl
 * @author wangying
 */

@Deprecated
@Service("oldVehicleService")
@Log4j2
public class VehicleServiceImpl implements VehicleService {

    private static final String NULL_STR = "";

    private static final int NUMBER_ZERO = 0;

    private static final String SYMBOL_COMMASY = ",";

    private static final Pattern PURPOSE_PATTERN = Pattern.compile("^[0-9a-zA-Z\u4E00-\u9FA5]{2,20}$");

    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private FuelTypeDao fuelTypeDao;
    @Autowired
    private VehiclePurposeDao vehiclePurposeDao;
    @Autowired
    private UserService userService;
    @Autowired
    private NewVehicleTypeDao newVehicleTypeDao;
    @Autowired
    private GroupDao groupDao;
    @Autowired
    private NewVehicleSubTypeDao newVehicleSubTypeDao;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private NewConfigDao newConfigDao;
    private RedisAssignService redisService;
    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;
    @Autowired
    private VideoCarouselServiceImpl videoCarouselService;
    @Autowired
    private NewVehicleCategoryDao newVehicleCategoryDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private RedisVehicleService redisVehicleService;
    @Autowired
    private OperationDao operationDao;
    @Autowired
    private PeopleDao peopleDao;
    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    public void setRedisService(RedisAssignService redisService) {
        this.redisService = redisService;
    }

    /**
     * 根据车牌号查询车辆信息
     */
    @Override
    @MethodLog(name = "根据车牌号查询 车辆", description = "根据车牌号查询 车辆")
    public VehicleInfo findVehicleById(String uuId) {
        VehicleDTO vehicleDTO = newVehicleDao.getDetailById(uuId);
        if (vehicleDTO == null) {
            return null;
        }
        return new VehicleInfo(vehicleDTO);
    }

    /**
     * 生成通用车辆列表模板
     */
    @Override
    @MethodLog(name = "生成通用车辆列表模板", description = "生成通用车辆列表模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        Date date = new Date();
        // 获得当前用户所属企业及其下级企业名称
        List<String> groupNames = userService.getOrgNamesByUser();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("车牌号");
        headList.add("所属企业");
        headList.add("类别标准");
        headList.add("车辆类别");
        headList.add("车辆类型");
        headList.add("车辆别名");
        headList.add("车主");
        headList.add("车主电话");
        headList.add("车辆等级");
        headList.add("电话是否校验");
        headList.add("区域属性");
        headList.add("省、直辖市");
        headList.add("市、区");
        headList.add("县");
        headList.add("燃料类型");
        headList.add("车辆颜色");
        headList.add("车牌颜色");
        headList.add("车辆状态");
        headList.add("核定载人数");
        headList.add("核定载质量");
        headList.add("车辆保险单号");
        headList.add("运营类别");
        headList.add("所属行业");
        headList.add("是否维修");
        headList.add("车辆照片");
        headList.add("车辆技术等级有效期");
        headList.add("道路运输证号");
        headList.add("经营许可证号");
        headList.add("经营范围");
        headList.add("核发机关");
        headList.add("经营权类型");
        headList.add("道路运输证有效期起");
        headList.add("道路运输证有效期至");
        headList.add("线路牌号");
        headList.add("始发地");
        headList.add("途经站名");
        headList.add("终到地");
        headList.add("始发站");
        headList.add("路线入口");
        headList.add("终到站");
        headList.add("路线出口");
        headList.add("每日发班次数");
        headList.add("运输证提前提醒天数");
        headList.add("营运状态");
        headList.add("行驶证号");
        headList.add("车架号");
        headList.add("发动机号");
        headList.add("使用性质");
        headList.add("品牌型号");
        headList.add("行驶证有效期起");
        headList.add("行驶证有效期至");
        headList.add("行驶证发证日期");
        headList.add("行驶证登记日期");
        headList.add("行驶证提前提醒天数");
        headList.add("保养里程数(km)");
        headList.add("保养有效期");
        headList.add("车台安装日期");

        // 必填字段
        requiredList.add("车牌号");
        requiredList.add("所属企业");
        requiredList.add("类别标准");
        requiredList.add("车辆类别");
        requiredList.add("车辆类型");
        // 默认设置一条数据
        exportList.add("渝BBB111");
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        //类别标准
        exportList.add("通用");
        //车辆类别
        List<VehicleCategoryDO> vehicleCategory = newVehicleCategoryDao.findByStandard(0);
        List<String> categoryIds = new ArrayList<>();
        String[] vehicleCategorys = null;
        if (vehicleCategory != null && vehicleCategory.size() > 0) {
            exportList.add(vehicleCategory.get(vehicleCategory.size() - 1).getVehicleCategory());
            vehicleCategorys = new String[vehicleCategory.size()];
            for (int i = 0; i < vehicleCategory.size(); i++) {
                vehicleCategorys[i] = vehicleCategory.get(i).getVehicleCategory();
                categoryIds.add(vehicleCategory.get(i).getId());
            }
        } else {
            exportList.add("请先添加类别标准为通用的车辆类别,否则导入通用车辆将会失败");
        }
        //车辆类型
        String[] vehicleTypes = null;
        if (categoryIds.size() > 0) {
            List<VehicleTypeDO> vehicleType = newVehicleTypeDao.findByCategoryIds(categoryIds);
            if (vehicleType != null && vehicleType.size() > 0) {
                exportList.add(vehicleType.get(vehicleType.size() - 1).getVehicleType());
                vehicleTypes = new String[vehicleType.size()];
                for (int i = 0; i < vehicleType.size(); i++) {
                    vehicleTypes[i] = vehicleType.get(i).getVehicleType();
                }
            } else {
                exportList.add("请先添加类别标准为通用的车辆类型,否则导入通用车辆将会失败");
            }
        } else {
            exportList.add("请先添加类别标准为通用的车辆类别,否则导入通用车辆将会失败");
        }
        exportList.add("红旗");
        exportList.add("张三");
        exportList.add("13658965874");
        exportList.add("高级");
        exportList.add("已校验");
        exportList.add("省内");
        exportList.add("重庆市");
        exportList.add("重庆市市辖区");
        exportList.add("城口县");
        List<String> fuelType = fuelTypeDao.getAll().stream().map(FuelTypeDO::getFuelType).collect(Collectors.toList());
        String[] fuelTypes = null;
        if (CollectionUtils.isNotEmpty(fuelType)) {
            fuelTypes = fuelType.toArray(new String[0]);
            exportList.add(fuelType.get(0));
        } else {
            exportList.add("请先添加燃料类型");
        }
        exportList.add("黑色");
        exportList.add("蓝色");
        exportList.add("启用");
        exportList.add("22");
        exportList.add("99.9");
        exportList.add("51235");
        List<String> purposeList =
            vehiclePurposeDao.getByKeyword(null).stream().map(VehiclePurposeDTO::getPurposeCategory)
                .collect(Collectors.toList());
        String[] vehiclePurposes = null;
        if (CollectionUtils.isNotEmpty(purposeList)) {
            vehiclePurposes = purposeList.toArray(new String[0]);
            exportList.add(purposeList.get(0));
        } else {
            exportList.add("请先添加运营类别");
        }
        List<Operations> trade = operationDao.findAllOperation("");
        String[] allTradeName = null;
        if (CollectionUtils.isEmpty(trade)) {
            exportList.add("");
        } else {
            List<String> tradeNames = trade.stream().map(Operations::getOperationType).collect(Collectors.toList());
            allTradeName = tradeNames.toArray(new String[trade.size()]);
            exportList.add(tradeNames.get(0));
        }
        exportList.add("否");
        exportList.add("照片");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("500123");
        exportList.add("500123");
        exportList.add("道路旅客运输");
        exportList.add("重庆");
        exportList.add("国有");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("123");
        exportList.add("重庆");
        exportList.add("成都");
        exportList.add("绵阳");
        exportList.add("重庆");
        exportList.add("318");
        exportList.add("绵阳");
        exportList.add("518");
        exportList.add("3");
        exportList.add("5");
        exportList.add("营运");
        exportList.add("5123");
        exportList.add("5123");
        exportList.add("5123");
        exportList.add("拉人");
        exportList.add("acz");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("5");
        exportList.add("6666");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属企业", groupNameArr);
        }
        //类型标准
        String[] standards = { "通用" };
        selectMap.put("类别标准", standards);

        //电话是否校验
        String[] phoneChecks = { "未校验", "已校验" };
        selectMap.put("电话是否校验", phoneChecks);
        //通用车辆类别
        if (vehicleCategorys != null) {
            selectMap.put("车辆类别", vehicleCategorys);
        }
        //通用车辆类型
        if (vehicleTypes != null) {
            selectMap.put("车辆类型", vehicleTypes);
        }

        // 区域属性
        String[] areaAttributes = { "省内", "跨省", "进京报备" };
        selectMap.put("区域属性", areaAttributes);

        // 燃料类型
        // {"0#柴油","10#柴油","20#柴油","30#柴油","50#柴油","89#汽油","90#汽油","92#汽油","93#汽油","95#汽油","97#汽油","98#汽油","CNG"};
        if (fuelTypes != null) {
            selectMap.put("燃料类型", fuelTypes);
        }
        // 车牌颜色
        String[] plateColors = PlateColor.getPalteColorNames();
        selectMap.put("车牌颜色", plateColors);
        // 车辆状态
        String[] startStatus = { "启用", "停用" };
        selectMap.put("车辆状态", startStatus);
        // 车辆颜色
        String[] vehicleColor = VehicleColor.getVehicleNames();
        selectMap.put("车辆颜色", vehicleColor);
        // 运营类别
        if (vehiclePurposes != null) {
            selectMap.put("运营类别", vehiclePurposes);
        }
        if (allTradeName != null) {
            selectMap.put("所属行业", allTradeName);
        }
        //是否维修
        String[] stateRepairs = { "否", "是" };
        selectMap.put("是否维修", stateRepairs);
        //经营权类型
        String[] managementTypes = { "国有", "集体", "私营", "个体", "联营", "股份制", "外商投资", "港澳台及其他" };
        selectMap.put("经营权类型", managementTypes);
        //营运状态
        String[] operatingStates = { "营运", "停运", "挂失", "报废", "歇业", "注销", "迁出(过户)", "迁出(转籍)", "其他" };
        selectMap.put("营运状态", operatingStates);

        ExportExcel export;
        export = new ExportExcel(headList, requiredList, selectMap);
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

    @Override
    @MethodLog(name = "生成工程机械列表模板", description = "生成工程机械列表模板")
    public boolean generateTemplateEngineering(HttpServletResponse response) throws Exception {
        Date date = new Date();
        // 获得当前用户所属企业及其下级企业名称
        List<String> groupNames = userService.getOrgNamesByUser();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("车牌号");
        headList.add("所属企业");
        headList.add("类别标准");
        headList.add("车辆类别");
        headList.add("车辆类型");
        headList.add("车辆别名");
        headList.add("车主");
        headList.add("车主电话");
        headList.add("车辆等级");
        headList.add("电话是否校验");
        headList.add("区域属性");
        headList.add("省、直辖市");
        headList.add("市、区");
        headList.add("县");
        headList.add("燃料类型");
        headList.add("车辆颜色");
        headList.add("车牌颜色");
        headList.add("车辆状态");
        headList.add("核定载人数");
        headList.add("核定载质量");
        headList.add("车辆保险单号");
        headList.add("运营类别");
        headList.add("所属行业");
        headList.add("是否维修");
        headList.add("车辆照片");
        headList.add("车辆技术等级有效期");
        headList.add("道路运输证号");
        headList.add("经营许可证号");
        headList.add("经营范围");
        headList.add("核发机关");
        headList.add("经营权类型");
        headList.add("道路运输证有效期起");
        headList.add("道路运输证有效期至");
        headList.add("线路牌号");
        headList.add("始发地");
        headList.add("途经站名");
        headList.add("终到地");
        headList.add("始发站");
        headList.add("路线入口");
        headList.add("终到站");
        headList.add("路线出口");
        headList.add("每日发班次数");
        headList.add("运输证提前提醒天数");
        headList.add("营运状态");
        headList.add("行驶证号");
        headList.add("车架号");
        headList.add("发动机号");
        headList.add("使用性质");
        headList.add("品牌型号");
        headList.add("行驶证有效期起");
        headList.add("行驶证有效期至");
        headList.add("行驶证发证日期");
        headList.add("行驶证登记日期");
        headList.add("行驶证提前提醒天数");

        headList.add("车主姓名");
        headList.add("车主手机1");
        headList.add("车主手机2");
        headList.add("车主手机3");
        headList.add("车主座机");
        headList.add("车辆子类型");
        headList.add("自重(T)");
        headList.add("工作能力(T)");
        headList.add("工作半径(m)");
        headList.add("机龄");
        headList.add("品牌");
        headList.add("机型");
        headList.add("初始里程(km)");
        headList.add("初始工时(h)");
        headList.add("保养里程数(km)");
        headList.add("保养有效期");
        headList.add("车台安装日期");

        // 必填字段
        requiredList.add("车牌号");
        requiredList.add("所属企业");
        requiredList.add("类别标准");
        requiredList.add("车辆类别");
        requiredList.add("车辆类型");
        requiredList.add("车主姓名");
        requiredList.add("车主手机1");
        requiredList.add("车辆子类型");
        // 默认设置一条数据
        exportList.add("渝BBB111");
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        //类别标准
        exportList.add("工程机械");
        //车辆类别
        List<VehicleCategoryDO> vehicleCategory = newVehicleCategoryDao.findByStandard(2);
        List<String> categoryIds = new ArrayList<>();
        String[] vehicleCategorys = null;
        String defaultVehicleCategoryId = null;
        if (vehicleCategory != null && vehicleCategory.size() > 0) {
            VehicleCategoryDO lastVehicleType = vehicleCategory.get(vehicleCategory.size() - 1);
            defaultVehicleCategoryId = lastVehicleType.getId();
            exportList.add(lastVehicleType.getVehicleCategory());
            vehicleCategorys = new String[vehicleCategory.size()];
            for (int i = 0; i < vehicleCategory.size(); i++) {
                vehicleCategorys[i] = vehicleCategory.get(i).getVehicleCategory();
                categoryIds.add(vehicleCategory.get(i).getId());
            }
        } else {
            exportList.add("请先添加类别标准为工程机械的车辆类别,否则导入工程机械车辆将会失败");
        }
        //车辆类型
        String[] vehicleTypes = null;
        String defaultVehicleTypeId = null;
        if (categoryIds.size() > 0) {
            List<VehicleTypeDO> vehicleTypeList = newVehicleTypeDao.findByCategoryIds(categoryIds);
            if (vehicleTypeList != null && vehicleTypeList.size() > 0) {

                if (defaultVehicleCategoryId == null) {
                    exportList.add("");
                } else {
                    String finalDefaultVehicleCategoryId = defaultVehicleCategoryId;
                    Optional<VehicleTypeDO> optionalVehicleType = vehicleTypeList.stream()
                        .filter(type -> type.getVehicleCategory().equals(finalDefaultVehicleCategoryId)).findFirst();
                    if (optionalVehicleType.isPresent()) {
                        VehicleTypeDO vehicleType = optionalVehicleType.get();
                        defaultVehicleTypeId = vehicleType.getId();
                        exportList.add(vehicleType.getVehicleType());
                    } else {
                        exportList.add("");
                    }
                }
                vehicleTypes = new String[vehicleTypeList.size()];
                for (int i = 0; i < vehicleTypeList.size(); i++) {
                    vehicleTypes[i] = vehicleTypeList.get(i).getVehicleType();
                }
            } else {
                exportList.add("请先添加类别标准为工程机械的车辆类型,否则导入工程机械车辆将会失败");
            }
        } else {
            exportList.add("请先添加类别标准为工程机械的车辆类别,否则导入工程机械车辆将会失败");
        }
        exportList.add("红旗");
        exportList.add("张三");
        exportList.add("13658965874");
        exportList.add("高级");
        exportList.add("已校验");
        exportList.add("省内");
        exportList.add("重庆市");
        exportList.add("重庆市市辖区");
        exportList.add("城口县");
        List<String> fuelType = fuelTypeDao.getAll().stream().map(FuelTypeDO::getFuelType).collect(Collectors.toList());
        String[] fuelTypes = null;
        if (CollectionUtils.isNotEmpty(fuelType)) {
            fuelTypes = fuelType.toArray(new String[0]);
            exportList.add(fuelType.get(0));
        } else {
            exportList.add("请先添加燃料类型");
        }
        exportList.add("黑色");
        exportList.add("蓝色");
        exportList.add("启用");
        exportList.add("22");
        exportList.add("99.9");
        exportList.add("51235");
        List<String> purposeList =
            vehiclePurposeDao.getByKeyword(null).stream().map(VehiclePurposeDTO::getPurposeCategory)
                .collect(Collectors.toList());
        String[] vehiclePurposes = null;
        if (CollectionUtils.isNotEmpty(purposeList)) {
            vehiclePurposes = purposeList.toArray(new String[0]);
            exportList.add(purposeList.get(0));
        } else {
            exportList.add("请先添加运营类别");
        }
        List<Operations> trade = operationDao.findAllOperation("");
        String[] allTradeName = null;
        if (CollectionUtils.isEmpty(trade)) {
            exportList.add("");
        } else {
            List<String> tradeNames = trade.stream().map(Operations::getOperationType).collect(Collectors.toList());
            allTradeName = tradeNames.toArray(new String[trade.size()]);
            exportList.add(tradeNames.get(0));
        }
        exportList.add("否");
        exportList.add("照片");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("500123");
        exportList.add("500123");
        exportList.add("道路旅客运输");
        exportList.add("重庆");
        exportList.add("国有");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("123");
        exportList.add("重庆");
        exportList.add("成都");
        exportList.add("绵阳");
        exportList.add("重庆");
        exportList.add("318");
        exportList.add("绵阳");
        exportList.add("518");
        exportList.add("3");
        exportList.add("5");
        exportList.add("营运");
        exportList.add("5123");
        exportList.add("5123");
        exportList.add("5123");
        exportList.add("拉人");
        exportList.add("acz");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("5");

        exportList.add("李四");
        exportList.add("15222334569");
        exportList.add("15222334569");
        exportList.add("15222334569");
        exportList.add("023-88888888");

        //车辆子类型
        String[] vehicleSubTypes = null;
        List<VehicleSubTypeDTO> vehicleSubTypeDTOList = newVehicleSubTypeDao.getByKeyword(null);
        if (CollectionUtils.isNotEmpty(vehicleSubTypeDTOList)) {
            if (defaultVehicleTypeId == null) {
                exportList.add("");
            } else {
                String finalDefaultVehicleTypeId = defaultVehicleTypeId;
                Optional<VehicleSubTypeDTO> optional = vehicleSubTypeDTOList.stream()
                    .filter(subType -> subType.getTypeId().equals(finalDefaultVehicleTypeId)).findFirst();
                if (optional.isPresent()) {
                    exportList.add(optional.get().getSubType());
                } else {
                    exportList.add("");
                }
            }
            vehicleSubTypes = new String[vehicleSubTypeDTOList.size()];
            for (int i = 0; i < vehicleSubTypeDTOList.size(); i++) {
                if (vehicleSubTypeDTOList.get(i).getSubType() != null) {
                    vehicleSubTypes[i] = vehicleSubTypeDTOList.get(i).getSubType();
                }
            }
        } else {
            exportList.add("请先添加车辆子类型,否则将导入失败");
        }
        exportList.add("8.9");
        exportList.add("12.8");
        exportList.add("5.8");
        exportList.add("2010-04");
        List<BrandForm> brand = brandDao.findBrandExport();
        String[] brands = null;
        if (brand != null && brand.size() > 0) {
            exportList.add(brand.get(0).getBrandName());
            brands = new String[brand.size()];
            for (int i = 0; i < brand.size(); i++) {
                brands[i] = brand.get(i).getBrandName();
            }
        } else {
            exportList.add("如果你要选择品牌,请先添加");
        }
        List<BrandModelsForm> brandModel = brandDao.findBrandModelsExport();
        String[] brandModels = null;
        if (brandModel != null && brandModel.size() > 0) {
            exportList.add(brandModel.get(0).getModelName());
            brandModels = new String[brandModel.size()];
            for (int i = 0; i < brandModel.size(); i++) {
                brandModels[i] = brandModel.get(i).getModelName();
            }
        } else {
            exportList.add("如果你要选择机型,请先添加");
        }
        exportList.add("1002.8");
        exportList.add("88.2");
        exportList.add("6666");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属企业", groupNameArr);
        }
        //类型标准
        String[] standards = { "工程机械" };
        selectMap.put("类别标准", standards);
        //品牌
        if (brands != null) {
            selectMap.put("品牌", brands);
        }
        //机型
        if (brandModels != null) {
            selectMap.put("机型", brandModels);
        }
        //电话是否校验
        String[] phoneChecks = { "未校验", "已校验" };
        selectMap.put("电话是否校验", phoneChecks);

        //通用车辆类别
        if (vehicleCategorys != null) {
            selectMap.put("车辆类别", vehicleCategorys);
        }
        //通用车辆类型
        if (vehicleTypes != null) {
            selectMap.put("车辆类型", vehicleTypes);
        }

        // 区域属性
        String[] areaAttributes = { "省内", "跨省", "进京报备" };
        selectMap.put("区域属性", areaAttributes);

        // 燃料类型
        // {"0#柴油","10#柴油","20#柴油","30#柴油","50#柴油","89#汽油","90#汽油","92#汽油","93#汽油","95#汽油","97#汽油","98#汽油","CNG"};
        if (fuelTypes != null) {
            selectMap.put("燃料类型", fuelTypes);
        }
        // 车牌颜色
        String[] plateColors = PlateColor.getPalteColorNames();
        selectMap.put("车牌颜色", plateColors);
        // 车辆状态
        String[] startStatus = { "启用", "停用" };
        selectMap.put("车辆状态", startStatus);
        // 车辆颜色
        String[] vehicleColor = VehicleColor.getVehicleNames();
        selectMap.put("车辆颜色", vehicleColor);
        // 运营类别
        if (vehiclePurposes != null) {
            selectMap.put("运营类别", vehiclePurposes);
        }
        if (allTradeName != null) {
            selectMap.put("所属行业", allTradeName);
        }
        //是否维修
        String[] stateRepairs = { "否", "是" };
        selectMap.put("是否维修", stateRepairs);
        //经营权类型
        String[] managementTypes = { "国有", "集体", "私营", "个体", "联营", "股份制", "外商投资", "港澳台及其他" };
        selectMap.put("经营权类型", managementTypes);
        //营运状态
        String[] operatingStates = { "营运", "停运", "挂失", "报废", "歇业", "注销", "迁出(过户)", "迁出(转籍)", "其他" };
        selectMap.put("营运状态", operatingStates);
        //车辆子类型
        if (vehicleSubTypes != null) {
            selectMap.put("车辆子类型", vehicleSubTypes);
        }

        ExportExcel export;
        export = new ExportExcel(headList, requiredList, selectMap);
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

    @Override
    @MethodLog(name = "生成货运车辆列表模板", description = "生成货运车辆列表模板")
    public boolean generateTemplateFreight(HttpServletResponse response) throws Exception {
        Date date = new Date();
        // 获得当前用户所属企业及其下级企业名称
        List<String> groupNames = userService.getOrgNamesByUser();
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("车牌号");
        headList.add("所属企业");
        headList.add("类别标准");
        headList.add("车辆类别");
        headList.add("车辆类型");
        headList.add("车辆别名");
        headList.add("车主");
        headList.add("车主电话");
        headList.add("车辆等级");
        headList.add("电话是否校验");
        headList.add("区域属性");
        headList.add("省、直辖市");
        headList.add("市、区");
        headList.add("县");
        headList.add("燃料类型");
        headList.add("车辆颜色");
        headList.add("车牌颜色");
        headList.add("车辆状态");
        headList.add("核定载人数");
        headList.add("核定载质量");
        headList.add("车辆保险单号");
        headList.add("运营类别");
        headList.add("所属行业");
        headList.add("是否维修");
        headList.add("车辆照片");
        headList.add("车辆技术等级有效期");
        headList.add("道路运输证号");
        headList.add("经营许可证号");
        headList.add("经营范围");
        headList.add("核发机关");
        headList.add("经营权类型");
        headList.add("道路运输证有效期起");
        headList.add("道路运输证有效期至");
        headList.add("线路牌号");
        headList.add("始发地");
        headList.add("途经站名");
        headList.add("终到地");
        headList.add("始发站");
        headList.add("路线入口");
        headList.add("终到站");
        headList.add("路线出口");
        headList.add("每日发班次数");
        headList.add("运输证提前提醒天数");
        headList.add("营运状态");
        headList.add("行驶证号");
        headList.add("车架号");
        headList.add("发动机号");
        headList.add("使用性质");
        headList.add("品牌型号");
        headList.add("行驶证有效期起");
        headList.add("行驶证有效期至");
        headList.add("行驶证发证日期");
        headList.add("行驶证登记日期");
        headList.add("行驶证提前提醒天数");

        headList.add("车辆品牌");
        headList.add("车辆型号");
        headList.add("车辆出厂日期");
        headList.add("首次上线时间");
        headList.add("车辆购置方式");
        headList.add("校验有效期至");
        headList.add("执照上传数");
        headList.add("总质量(kg)");
        headList.add("准牵引总质量(kg)");
        headList.add("外廓尺寸-长(mm)");
        headList.add("外廓尺寸-宽(mm)");
        headList.add("外廓尺寸-高(mm)");
        headList.add("货厢内部尺寸-长(mm)");
        headList.add("货厢内部尺寸-宽(mm)");
        headList.add("货厢内部尺寸-高(mm)");
        headList.add("轴数");
        headList.add("轮胎数");
        headList.add("轮胎规格");
        headList.add("保养里程数(km)");
        headList.add("保养有效期");
        headList.add("车台安装日期");

        // 必填字段
        requiredList.add("车牌号");
        requiredList.add("所属企业");
        requiredList.add("类别标准");
        requiredList.add("车辆类别");
        requiredList.add("车辆类型");
        // 默认设置一条数据
        exportList.add("渝BBB111");
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        //类别标准
        exportList.add("货运");
        //车辆类别
        List<VehicleCategoryDO> vehicleCategory = newVehicleCategoryDao.findByStandard(1);
        List<String> categoryIds = new ArrayList<>();
        String[] vehicleCategorys = null;
        if (vehicleCategory != null && vehicleCategory.size() > 0) {
            exportList.add(vehicleCategory.get(vehicleCategory.size() - 1).getVehicleCategory());
            vehicleCategorys = new String[vehicleCategory.size()];
            for (int i = 0; i < vehicleCategory.size(); i++) {
                vehicleCategorys[i] = vehicleCategory.get(i).getVehicleCategory();
                categoryIds.add(vehicleCategory.get(i).getId());
            }
        } else {
            exportList.add("请先添加类别标准为货运的车辆类别,否则导入货运车辆将会失败");
        }
        //车辆类型
        String[] vehicleTypes = null;
        if (categoryIds.size() > 0) {
            List<VehicleTypeDO> vehicleType = newVehicleTypeDao.findByCategoryIds(categoryIds);
            if (vehicleType != null && vehicleType.size() > 0) {
                exportList.add(vehicleType.get(vehicleType.size() - 1).getVehicleType());
                vehicleTypes = new String[vehicleType.size()];
                for (int i = 0; i < vehicleType.size(); i++) {
                    vehicleTypes[i] = vehicleType.get(i).getVehicleType();
                }
            } else {
                exportList.add("请先添加类别标准为货运的车辆类型,否则导入货运车辆将会失败");
            }
        } else {
            exportList.add("请先添加类别标准为货运的车辆类别,否则导入货运车辆将会失败");
        }
        exportList.add("红旗");
        exportList.add("张三");
        exportList.add("13658965874");
        exportList.add("高级");
        exportList.add("已校验");
        exportList.add("省内");
        exportList.add("重庆市");
        exportList.add("重庆市市辖区");
        exportList.add("城口县");
        List<String> fuelType = fuelTypeDao.getAll().stream().map(FuelTypeDO::getFuelType).collect(Collectors.toList());
        String[] fuelTypes = null;
        if (CollectionUtils.isNotEmpty(fuelType)) {
            fuelTypes = fuelType.toArray(new String[0]);
            exportList.add(fuelType.get(0));
        } else {
            exportList.add("请先添加燃料类型");
        }

        exportList.add("黑色");
        exportList.add("蓝色");
        exportList.add("启用");
        exportList.add("22");
        exportList.add("99.9");
        exportList.add("51235");
        String[] vehiclePurposes = null;
        List<String> purposeList =
            vehiclePurposeDao.getByKeyword(null).stream().map(VehiclePurposeDTO::getPurposeCategory)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(purposeList)) {
            exportList.add(purposeList.get(0));
            vehiclePurposes = purposeList.toArray(new String[0]);
        } else {
            exportList.add("请先添加运营类别");
        }
        List<Operations> trade = operationDao.findAllOperation("");
        String[] allTradeName = null;
        if (CollectionUtils.isEmpty(trade)) {
            exportList.add("");
        } else {
            List<String> tradeNames = trade.stream().map(Operations::getOperationType).collect(Collectors.toList());
            allTradeName = tradeNames.toArray(new String[trade.size()]);
            exportList.add(tradeNames.get(0));
        }
        exportList.add("否");
        exportList.add("照片");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("500123");
        exportList.add("500123");
        exportList.add("道路旅客运输");
        exportList.add("重庆");
        exportList.add("国有");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("123");
        exportList.add("重庆");
        exportList.add("成都");
        exportList.add("绵阳");
        exportList.add("重庆");
        exportList.add("318");
        exportList.add("绵阳");
        exportList.add("518");
        exportList.add("3");
        exportList.add("5");
        exportList.add("营运");
        exportList.add("5123");
        exportList.add("5123");
        exportList.add("5123");
        exportList.add("拉人");
        exportList.add("acz");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("5");

        exportList.add("品牌名称");
        exportList.add("型号名称");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd HH:mm:ss"));
        exportList.add("分期付款");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add("3");
        exportList.add("1000");
        exportList.add("1200");
        exportList.add("500");
        exportList.add("300");
        exportList.add("200");
        exportList.add("400");
        exportList.add("250");
        exportList.add("150");
        exportList.add("4");
        exportList.add("14");
        exportList.add("175/70R");
        exportList.add("6666");
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));
        exportList.add(Converter.toString(date, "yyyy-MM-dd"));

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();
        //所属企业
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属企业", groupNameArr);
        }
        //类型标准
        String[] standards = { "货运" };
        selectMap.put("类别标准", standards);

        //电话是否校验
        String[] phoneChecks = { "未校验", "已校验" };
        selectMap.put("电话是否校验", phoneChecks);

        //通用车辆类别
        if (vehicleCategorys != null) {
            selectMap.put("车辆类别", vehicleCategorys);
        }
        //通用车辆类型
        if (vehicleTypes != null) {
            selectMap.put("车辆类型", vehicleTypes);
        }

        // 区域属性
        String[] areaAttributes = { "省内", "跨省", "进京报备" };
        selectMap.put("区域属性", areaAttributes);

        // 燃料类型
        // {"0#柴油","10#柴油","20#柴油","30#柴油","50#柴油","89#汽油","90#汽油","92#汽油","93#汽油","95#汽油","97#汽油","98#汽油","CNG"};
        if (fuelTypes != null) {
            selectMap.put("燃料类型", fuelTypes);
        }
        // 车牌颜色
        String[] plateColors = PlateColor.getPalteColorNames();
        selectMap.put("车牌颜色", plateColors);
        // 车辆状态
        String[] startStatus = { "启用", "停用" };
        selectMap.put("车辆状态", startStatus);
        // 车辆颜色
        String[] vehicleColor = VehicleColor.getVehicleNames();
        selectMap.put("车辆颜色", vehicleColor);
        // 运营类别
        if (vehiclePurposes != null) {
            selectMap.put("运营类别", vehiclePurposes);
        }
        if (allTradeName != null) {
            selectMap.put("所属行业", allTradeName);
        }
        //是否维修
        String[] stateRepairs = { "否", "是" };
        selectMap.put("是否维修", stateRepairs);
        //经营权类型
        String[] managementTypes = { "国有", "集体", "私营", "个体", "联营", "股份制", "外商投资", "港澳台及其他" };
        selectMap.put("经营权类型", managementTypes);
        //营运状态
        String[] operatingStates = { "营运", "停运", "挂失", "报废", "歇业", "注销", "迁出(过户)", "迁出(转籍)", "其他" };
        selectMap.put("营运状态", operatingStates);
        //车辆购置方式
        String[] purchaseWays = { "分期付款", "一次性付清" };
        selectMap.put("车辆购置方式", purchaseWays);

        ExportExcel export;
        export = new ExportExcel(headList, requiredList, selectMap);
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
     * 查询用户权限的车+用户所创建的车 并且都绑定了的车
     */
    @Override
    @MethodLog(name = "查询用户权限的监控对象+用户所创建的监控对象", description = "查询用户权限的监控对象+用户所创建的监控对象")
    public List<VehicleInfo> findVehicleByUserAndGroup(String userId, List<String> groupIdList, boolean configFlag) {
        if (userId != null && !"".equals(userId) && groupIdList != null && groupIdList.size() > 0) {
            return newVehicleDao.findVehicleByUserGroupConfig(userId, groupIdList);
        }
        return null;
    }


    /**
     * 根据多个userId删除用户和车的关联
     */
    @Override
    @MethodLog(name = "根据多个userId删除用户和车的关联", description = "根据多个userId删除用户和车的关联")
    public boolean deleteUserVehicleByUsers(Collection<String> userList) {
        return userList != null && userList.size() > 0 && newVehicleDao.deleteUserAssignByUsers(userList);
    }

    /**
     * 根据用户id和车组id删除车组和用户的关联
     */
    @Override
    @MethodLog(name = "根据用户id和车组id删除车组和用户的关联", description = "根据用户id和车组id删除车组和用户的关联")
    public boolean deleteUserAssByUserAndAssign(String userId, List<String> vehicleList) {
        return userId != null && !Objects.equals(userId, "") && vehicleList != null && vehicleList.size() > 0
            && newVehicleDao.deleteUserAssByUserAndAssign(userId, vehicleList);
    }

    /**
     * 根据用户list修改用户和车组的关联
     */
    @Override
    public boolean updateUserAssignByUser(String assignmentId, String userList, String ipAddress) {
        // 当前用户所属组织
        String orgId = userService.getOrgIdByUser();
        // 根据userName获取userId
        // 得到当前用户所属组织及下级组织的user
        List<UserBean> users = userService.getUserList(null, orgId, true);
        List<String> urList = new ArrayList<>();
        for (UserBean user : users) {
            urList.add(user.getUuid());
        }
        // 数据库已有的数据
        List<String> beforeUser;
        beforeUser = newVehicleDao.findUserAssignByAid(assignmentId, urList);
        // 用户所选权限
        List<String> curUser = new ArrayList<>();
        if (userList != null && !"".equals(userList)) {
            String[] userTree = userList.split(";");
            curUser = Arrays.asList(userTree);
        }
        // 需要删除的数据
        List<String> delList = new ArrayList<>(beforeUser);
        delList.removeAll(curUser);
        if (delList.size() > 0) {
            newVehicleDao.deleteUserAssignByUsersAid(assignmentId, delList);
        }
        // 需要新增的数据
        List<String> addList = new ArrayList<>(curUser);
        addList.removeAll(beforeUser);
        List<AssignmentUserForm> assignmentUserForm = new ArrayList<>();
        if (addList.size() > 0) {
            for (String userId : addList) {
                AssignmentUserForm userAssignment = new AssignmentUserForm();
                userAssignment.setUserId(userId);
                userAssignment.setAssignmentId(assignmentId);
                assignmentUserForm.add(userAssignment);
            }
            groupDao.addAssignmentUserByBatch(assignmentUserForm);
        }
        List<String> cacheAddList = users.stream().filter(p -> addList.contains(p.getUuid())).map(UserBean::getUsername)
            .collect(Collectors.toList());
        List<String> cacheDelList = users.stream().filter(p -> delList.contains(p.getUuid())).map(UserBean::getUsername)
            .collect(Collectors.toList());
        redisService.updateUsersByAssignmentId(assignmentId, cacheDelList, cacheAddList);
        Assignment assignment = assignmentService.findAssignmentById(assignmentId);
        String msg =
            "分组管理：" + assignment.getName() + " ( @" + userService.getOrgByUuid(assignment.getGroupId()).getName()
                + " ) 分配监控人员";
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        WebSubscribeManager.getInstance().clearSubUser();
        return true;
    }

    @Override
    public List<String> findUserAssignByAid(String assignmentId, List<String> urList) {
        if (assignmentId != null && !Objects.equals(assignmentId, "") && urList != null && urList.size() > 0) {
            return newVehicleDao.findUserAssignByAid(assignmentId, urList);
        }
        return null;
    }

    @Override
    public List<VehicleType> getVehicleTypeList() {
        return newVehicleTypeDao.getByKeyword(null).stream().map(VehicleType::new).collect(Collectors.toList());
    }

    @Override
    public VehicleInfo findByVehicle(String brand) {
        return newVehicleDao.findByVehicle(brand);
    }

    private String buildAssignmentTree(String type, JSONArray result, Map<String, OrganizationLdap> organizationLdapMap,
        Assignment assign) {
        JSONObject assignmentObj = new JSONObject();
        String pid = organizationLdapMap.get(assign.getGroupId()).getId().toString();
        assignmentObj.put("id", assign.getId());
        assignmentObj.put("pId", pid);
        assignmentObj.put("name", assign.getName());
        assignmentObj.put("type", "assignment");
        assignmentObj.put("iconSkin", "assignmentSkin");
        if ("single".equals(type)) { // 根节点是否可选
            assignmentObj.put("nocheck", true);
        }
        result.add(assignmentObj);
        return pid;
    }

    public JSONArray getVehicleTree(List<VehicleInfo> vehicleList, Set<String> assignmentIds) {
        JSONArray result = new JSONArray();
        if (vehicleList != null && vehicleList.size() > 0) {
            for (VehicleInfo vehicle : vehicleList) {
                JSONObject vehicleObj = new JSONObject();
                vehicleObj.put("id", vehicle.getId());
                vehicleObj.put("type", "vehicle");
                vehicleObj.put("iconSkin", "vehicleSkin");
                vehicleObj.put("pId", vehicle.getAssignmentId());
                vehicleObj.put("name", vehicle.getBrand());
                vehicleObj.put("deviceNumber", vehicle.getDeviceNumber());
                vehicleObj.put("isVideo", vehicle.getIsVideo());
                assignmentIds.add(vehicle.getAssignmentId());
                result.add(vehicleObj);
            }
        }
        return result;
    }

    /**
     * 车辆权限树结构 首先查询组织，再查询组织下的分组，再查询分组下的车和人
     */
    @Override
    public JSONArray monitorTreeFuzzy(String type, String queryParam, String queryType, String queryObjFlag,
        String deviceType, Integer webType, String isIncludeQuitPeople, Integer isCarousel) {
        // 终端通讯类型集合，用于过滤查询
        List<String> deviceTypes = null;
        JSONArray result = new JSONArray();
        if (StringUtils.isBlank(queryType)) {
            queryType = "name";
        }
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        if ("group".equals(queryType)) {
            final String finalQueryParams = queryParam;
            List<OrganizationLdap> filterList =
                orgs.stream().filter(org -> org.getName().contains(finalQueryParams)).collect(Collectors.toList());
            orgs = TreeUtils.getFilterWholeOrgList(orgs, filterList);
            userOrgListId = getOrgUuids(filterList);
        }

        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, userOrgListId);
        // 组装分组树结构
        List<String> assignIdList = assignmentService.putAssignmentTree(assignmentList, result, type, false);
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, type));
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)

        if (assignIdList != null && assignIdList.size() > 0) {
            queryParam = StringUtil.mysqlLikeWildcardTranslation(queryParam);
            List<VehicleInfo> vehicleList = new ArrayList<>();
            if ("monitor".equals(queryObjFlag)) {
                // 实时监控，组装所有数据；实时视频，过滤除808以外的车辆
                if (webType != null && webType == 2) {
                    deviceTypes = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
                }
                vehicleList =
                    groupDao.findMonitorByAssignmentIdsFuzzy(assignIdList, queryParam, queryType, deviceTypes);
            } else if ("vehicle".equals(queryObjFlag)) {
                deviceTypes = getDeviceTypes(deviceType, null);
                vehicleList =
                    groupDao.findVehicleByAssignmentIdsFuzzy(assignIdList, queryParam, deviceTypes, webType);
            }
            // 组装车辆树
            if (vehicleList != null && vehicleList.size() > 0) {
                Map<String, List<VideoChannelSetting>> channelSettingMap = null;
                List<String> vids = vehicleList.stream().map(VehicleInfo::getId).collect(Collectors.toList());
                if (Objects.nonNull(isCarousel) && Objects.equals(1, isCarousel)) {
                    // 轮播树的监控对象附加通道号相关信息
                    List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleIds(vids);
                    if (CollectionUtils.isNotEmpty(vcs)) {
                        channelSettingMap =
                            vcs.stream().collect(Collectors.groupingBy(VideoChannelSetting::getVehicleId));
                    }
                }
                for (VehicleInfo vehicle : vehicleList) {
                    JSONObject vehicleObj = new JSONObject();
                    // 树组装
                    putMonitorTree(vehicle, vehicleObj, false, webType, channelSettingMap, null);
                    // vehicleObj.put("open", true);
                    result.add(vehicleObj);
                }
            }
            if (StringUtils.isNotBlank(isIncludeQuitPeople) && "1".equals(isIncludeQuitPeople)) {
                List<LeaveJobPersonnel> leaveJobPersonnelList = peopleDao.getLeaveJobPersonnelList(assignIdList);
                String finalQueryParam = queryParam;
                installQuitPeople(result,
                    leaveJobPersonnelList.stream().filter(info -> info.getPeopleNumber().contains(finalQueryParam))
                        .collect(Collectors.toList()));
            }
        }
        return result;
    }

    public List<String> getOrgUuids(List<OrganizationLdap> orgs) {
        List<String> result = new ArrayList<>();
        for (OrganizationLdap org : orgs) {
            result.add(org.getUuid());
        }
        return result;
    }

    /**
     * 车辆权限树结构 首先查询组织，再查询组织下的分组，再查询分组下的车和人(返回监控对象数量)
     */
    @Override
    public int monitorTreeFuzzyCount(String type, String queryParam, String queryType, String queryObjFlag,
        String deviceType) {
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        if ("group".equals(queryType)) {
            final String finalQueryParams = queryParam;
            List<OrganizationLdap> filterList =
                orgs.stream().filter(org -> org.getName().contains(finalQueryParams)).collect(Collectors.toList());
            userOrgListId = getOrgUuids(filterList);
        }

        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, userOrgListId);
        List<String> assignIdList = new ArrayList<>();
        if (assignmentList != null && !assignmentList.isEmpty()) {
            for (Assignment a : assignmentList) {
                assignIdList.add(a.getId());
            }
        }
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        int size = 0;
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = new ArrayList<>();
            if ("monitor".equals(queryObjFlag)) {
                vehicleList = groupDao.findMonitorByAssignmentIdsFuzzy(assignIdList, queryParam, queryType, null);
            } else if ("vehicle".equals(queryObjFlag)) {
                List<String> deviceTypes = getDeviceTypes(deviceType, Lists.newArrayList());
                vehicleList =
                    groupDao.findVehicleByAssignmentIdsFuzzy(assignIdList, queryParam, deviceTypes, null);
            }
            size = vehicleList.size();
        }
        return size;
    }

    /**
     * 组装监控对象
     * @param webType           (1:实时监控，2：实时视频，3:809监控对象转发管理)
     * @param channelSettingMap
     * @param accStatusMap
     */
    private void putMonitorTree(VehicleInfo vehicle, JSONObject vehicleObj, boolean isChecked, Integer webType,
        Map<String, List<VideoChannelSetting>> channelSettingMap, Map<String, MonitorAccStatus> accStatusMap) {
        vehicleObj.put("id", vehicle.getId());
        String monitorType = vehicle.getMonitorType();
        if ("0".equals(monitorType)) {
            vehicleObj.put("type", "vehicle");
            vehicleObj.put("iconSkin", "vehicleSkin");
        } else if ("1".equals(monitorType)) {
            vehicleObj.put("type", "people");
            vehicleObj.put("iconSkin", "peopleSkin");
        } else if ("2".equals(monitorType)) {
            vehicleObj.put("type", "thing");
            vehicleObj.put("iconSkin", "thingSkin");
        }
        vehicleObj.put("pId", vehicle.getAssignmentId());
        vehicleObj.put("name", vehicle.getBrand());
        vehicleObj.put("deviceNumber", vehicle.getDeviceNumber());
        vehicleObj.put("isVideo", vehicle.getIsVideo());
        vehicleObj.put("deviceType", vehicle.getDeviceType());
        vehicleObj.put("plateColor", vehicle.getPlateColor());
        vehicleObj.put("simcardNumber", vehicle.getSimcardNumber());
        vehicleObj.put("professional", vehicle.getProfessionalsName());
        vehicleObj.put("assignName", vehicle.getGroupName());
        vehicleObj.put("aliases", vehicle.getAliases());
        if (isChecked) {
            vehicleObj.put("checked", true);
        }
        if (webType != null && webType == 2) {
            vehicleObj.put("isParent", true); // 若为实时视频，则组装为有子节点
        }

        if (MapUtils.isNotEmpty(accStatusMap) && accStatusMap.containsKey(vehicle.getId())) {
            MonitorAccStatus monitorAccStatus = accStatusMap.get(vehicle.getId());
            vehicleObj.put("status", monitorAccStatus.getStatus());
            vehicleObj.put("acc", monitorAccStatus.getAcc());
        }

        if (channelSettingMap != null) {
            // 通道号相关信息
            videoCarouselService.putChannelProperties(vehicle.getId(), vehicleObj, channelSettingMap);
        }
    }

    /**
     * 监控对象权限树结构 首先查询组织，再查询组织下的分组，再查询分组下的车和人
     */
    @Override
    public JSONObject monitorTreeByType(String type, String isIncludeQuitPeople) {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> userOwnOrganizationInfoList = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOwnOrganizationIdList = new ArrayList<>();
        if (userOwnOrganizationInfoList != null && userOwnOrganizationInfoList.size() > 0) {
            for (OrganizationLdap org : userOwnOrganizationInfoList) {
                userOwnOrganizationIdList.add(org.getUuid());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList =
            assignmentService.findUserAssignmentNum(uuid, userOwnOrganizationIdList, null, null);
        List<String> assignIdList = new ArrayList<>();
        // 组装组织树结构
        if (assignmentList != null && assignmentList.size() > 0) {
            List<OrganizationLdap> allOrganization = userService.getOrgChild("ou=organization"); // 所有组织
            // 当前组织
            if (allOrganization != null && !allOrganization.isEmpty()) {
                for (OrganizationLdap organization : allOrganization) {
                    for (Assignment assign : assignmentList) {
                        if (organization.getUuid().equals(assign.getGroupId())) {
                            // 分组id list
                            assignIdList.add(assign.getId());
                            // 组装分组树
                            JSONObject assignmentObj = new JSONObject();
                            // 数量
                            if (assign.getMNum() != null) {
                                assignmentObj.put("count", assign.getMNum());
                            }
                            assignmentObj.put("id", assign.getId());
                            assignmentObj.put("pId", organization.getId().toString());
                            assignmentObj.put("name", assign.getName());
                            assignmentObj.put("type", "assignment");
                            assignmentObj.put("iconSkin", "assignmentSkin");
                            // assignmentObj.put("open", false);
                            if ("single".equals(type)) { // 根节点是否可选
                                assignmentObj.put("nocheck", true);
                            }
                            assignmentObj.put("isParent", true); // 有子节点
                            result.add(assignmentObj);
                        }
                    }
                }
            }
        }

        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(userOwnOrganizationInfoList, type));

        // 查询当前用户权限分组下所有的监控对象总数
        int monitorNumber = getRedisAssignVid().size();
        List<LeaveJobPersonnel> leaveJobPersonnelList = new ArrayList<>();
        if (StringUtils.isNotBlank(isIncludeQuitPeople) && "1".equals(isIncludeQuitPeople)) {
            if (CollectionUtils.isNotEmpty(assignIdList)) {
                leaveJobPersonnelList = peopleDao.getLeaveJobPersonnelList(assignIdList);
                monitorNumber = monitorNumber + leaveJobPersonnelList.size();
            }
        }
        if (monitorNumber > PublicVariable.MONITOR_COUNT) {
            obj.put("size", monitorNumber);
            obj.put("tree", result);
            return obj;
        }
        //组装离职人员
        installQuitPeople(result, leaveJobPersonnelList);
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = groupDao.findMonitorByAssignmentIds(assignIdList, null);
            // 组装车辆树
            if (vehicleList != null && vehicleList.size() > 0) {
                JSONArray vehicleSort = new JSONArray();
                for (VehicleInfo vehicle : vehicleList) {
                    JSONObject vehicleObj = new JSONObject();
                    putMonitorTree(vehicle, vehicleObj, false, null, null, null);
                    // vehicleObj.put("open", true);
                    vehicleSort.add(vehicleObj);
                }

                //监控对象按车牌排序
                vehicleSort.sort(new Comparator<Object>() {
                    final Collator collator = Collator.getInstance(Locale.CHINA);

                    @Override
                    public int compare(Object o1, Object o2) {
                        CollationKey key1 = collator.getCollationKey(((JSONObject) o1).getString("name"));
                        CollationKey key2 = collator.getCollationKey(((JSONObject) o2).getString("name"));
                        return key1.compareTo(key2);
                    }
                });
                result.addAll(vehicleSort);
            }
        }
        obj.put("size", monitorNumber);
        obj.put("tree", result);
        return obj;
    }

    /**
     * 车辆权限树结构 首先查询组织，再查询组织下的分组，再查询分组下的车和人
     */
    @Override
    public JSONArray monitorTreeFoApp(int limit, int page) {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList = PageHelper.startPage(page, limit)
                .doSelectPage(() -> groupDao.findUserAssignment(uuid, userOrgListId));
        List<String> assignIdList = new ArrayList<>();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                OrganizationLdap organization = userService.getOrgByUuid(assign.getGroupId());
                if (organization != null && organization.getId() != null) {
                    // 分组id list
                    assignIdList.add(assign.getId());
                    // 组装分组树
                    JSONObject assignmentObj = new JSONObject();
                    assignmentObj.put("id", assign.getId());
                    assignmentObj.put("pId", organization.getId().toString());
                    assignmentObj.put("name", assign.getName());
                    assignmentObj.put("type", "assignment");
                    assignmentObj.put("iconSkin", "assignmentSkin");
                    assignmentObj.put("open", true);
                    result.add(assignmentObj);
                }
            }
        }
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = groupDao.findMonitorByAssignmentIds(assignIdList, null);
            // 组装车辆树
            if (vehicleList != null && vehicleList.size() > 0) {
                for (VehicleInfo vehicle : vehicleList) {
                    JSONObject vehicleObj = new JSONObject();
                    vehicleObj.put("id", vehicle.getId());
                    if ("0".equals(vehicle.getMonitorType())) {
                        vehicleObj.put("type", "vehicle");
                        vehicleObj.put("iconSkin", "vehicleSkin");
                    } else if ("1".equals(vehicle.getMonitorType())) {
                        vehicleObj.put("type", "people");
                        vehicleObj.put("iconSkin", "peopleSkin");
                    } else if ("2".equals(vehicle.getMonitorType())) {
                        vehicleObj.put("type", "thing");
                        vehicleObj.put("iconSkin", "thingSkin");
                    }
                    vehicleObj.put("pId", vehicle.getAssignmentId());
                    vehicleObj.put("name", vehicle.getBrand());
                    vehicleObj.put("deviceNumber", vehicle.getDeviceNumber());
                    vehicleObj.put("isVideo", vehicle.getIsVideo());
                    vehicleObj.put("simcardNumber", vehicle.getSimcardNumber());
                    vehicleObj.put("professional", vehicle.getProfessionalsName());
                    vehicleObj.put("assignName", vehicle.getGroupName());
                    vehicleObj.put("open", true);
                    result.add(vehicleObj);
                }
            }
        }
        return result;
    }

    /**
     * 车辆权限树结构(除去传入分组) 首先查询组织，再查询组织下的分组，再查询分组下的车
     * @param type         树的类型："single", "multiple"
     * @param assignmentId 分组ID
     * @return 监控对象信息
     */
    @Override
    public JSONArray vehicleTreeForAssign(String type, String assignmentId, String queryParam, String queryType) {
        JSONArray result = new JSONArray();
        // 获取当前用户权限分组
        List<String> assignIdList = listAssignmentsOfCurrentUser(type, assignmentId, result, queryParam, queryType);
        // 查询条件不为空，返回的查询结果不可异步展开分组节点
        if (!queryParam.isEmpty() && "name".equals(queryType)) {
            for (Object obj : result) {
                JSONObject item = (JSONObject) obj;
                item.remove("count");
                item.remove("isParent");
            }
        }
        if (assignIdList.isEmpty()) {
            return result;
        }
        if (!"name".equals(queryType)) {
            queryParam = "";
        }
        queryParam = StringUtil.mysqlLikeWildcardTranslation(queryParam);
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        List<VehicleInfo> vehicleList =
            groupDao.findMonitorByAssignmentIdList(assignIdList, queryParam, null, null);
        JSONArray vehicleArray = assembleVehicleData(vehicleList);
        result.addAll(vehicleArray);
        for (int i = 0; i < result.size(); i++) {
            JSONObject jsonObject = result.getJSONObject(i);
            jsonObject.put("open", false);
        }
        return result;
    }

    @Override
    public int countMonitors(String assignmentId) {
        JSONArray result = new JSONArray();

        // 获取当前用户权限分组
        List<String> assignIdList = listAssignmentsOfCurrentUser("multiple", assignmentId, result, null, null);

        if (assignIdList.isEmpty()) {
            return 0;
        }

        return groupDao.countMonitorByAssignmentIdList(assignIdList);
    }

    @Override
    public JSONArray listMonitorTreeParentNodes(String assignmentId, String queryParam, String queryType) {
        JSONArray result = new JSONArray();

        // 获取当前用户权限分组
        listAssignmentsOfCurrentUser("multiple", assignmentId, result, queryParam, queryType);

        return result;
    }

    /**
     * 获取当前用户的权限分组，排除指定的分组后组装好分组组织树
     * @param type         树的类型："single", "multiple"
     * @param assignmentId 要排除的分组id，如果为空则不排除
     * @param result       组装好的分组组织树数据
     */
    private List<String> listAssignmentsOfCurrentUser(String type, String assignmentId, JSONArray result,
        String queryParam, String queryType) {
        // 根据用户名获取当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> organizations = userService.getOrgChild(userService.getOrgIdByUserId(userId));
        //当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        Set<OrganizationLdap> orgList = new HashSet<>(organizations);
        boolean notEmpty = CollectionUtils.isNotEmpty(organizations);
        if ("groupName".equals(queryType)) {
            orgList.clear();
            List<OrganizationLdap> filterList =
                organizations.stream().filter(org -> org.getName().contains(queryParam)).collect(Collectors.toList());
            for (OrganizationLdap org : filterList) {
                userOrgListId.add(org.getUuid());
            }
            if (notEmpty) {
                if (CollectionUtils.isNotEmpty(filterList)) {
                    for (OrganizationLdap filterOrg : filterList) {
                        TreeUtils.getLowerOrg(orgList, organizations, filterOrg);
                    }
                }
            }
        }
        // 遍历得到当前用户组织及下级组织id的list
        if (notEmpty) {
            if (!"groupName".equals(queryType)) {
                for (OrganizationLdap org : orgList) {
                    userOrgListId.add(org.getUuid());
                }
            }

        }
        // 查询当前用户权限分组
        List<String> assignIdList = new ArrayList<>();
        List<Assignment> assignmentList = assignmentService.findUserAssignmentNum(uuid, userOrgListId, null, null);
        if (assignmentList != null && !assignmentList.isEmpty()) {
            // 所有组织
            List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization");
            for (OrganizationLdap organization : allOrg) {
                for (Assignment assign : assignmentList) {
                    // 排除传入的分组
                    if (Objects.equals(assign.getId(), assignmentId)) {
                        continue;
                    }
                    if ("assignName".equals(queryType)) {
                        if (!assign.getName().contains(queryParam)) {
                            continue;
                        }
                    }
                    if (organization.getUuid().equals(assign.getGroupId())) {
                        // 分组id list
                        assignIdList.add(assign.getId());
                        // 组装分组树
                        JsonUtil.addAssignmentObjNum(assign, organization, type, result);
                    }
                }
            }
        }
        if ("assignName".equals(queryType)) {
            Set<OrganizationLdap> filterOrgs = new HashSet<>();
            List<String> pids = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                pids.add(result.getJSONObject(i).getString("pId"));
            }
            if (notEmpty) {
                for (String pid : pids) {
                    filterGroup(organizations, filterOrgs, pid);
                }
            }
            orgList.clear();
            orgList.addAll(filterOrgs);
        }
        JSONArray jsonArray = JsonUtil.getOrgTree(new ArrayList<>(orgList), type);
        // 组装组织树结构
        result.addAll(jsonArray);
        return assignIdList;
    }

    private void filterGroup(List<OrganizationLdap> orgList, Set<OrganizationLdap> filterOrgs, String parentId) {
        for (OrganizationLdap org : orgList) {
            if (org.getId().toString().equals(parentId)) {
                filterOrgs.add(org);
                filterGroup(orgList, filterOrgs, org.getPid());
            }
        }
    }

    /**
     * 当前用户所在指定的权限分组下面的监控对象(并且已绑定)
     * @param assignmentID 指定的分组ID
     * @return 监控对象
     */
    @Override
    public JSONArray listMonitorsByAssignmentID(String assignmentID) {
        List<VehicleInfo> vehicleList = groupDao.findMonitorByAssignmentId(assignmentID);

        return assembleVehicleData(vehicleList);
    }

    private JSONArray assembleVehicleData(List<VehicleInfo> vehicleList) {
        JSONArray result = new JSONArray();
        // 组装车辆树
        if (vehicleList != null && !vehicleList.isEmpty()) {
            vehicleList.sort(Comparator.comparing(VehicleInfo::getBrand));
            for (VehicleInfo vehicle : vehicleList) {
                JSONObject vehicleObj = JsonUtil.assembleVehicleObject(vehicle);
                result.add(vehicleObj);
            }
        }
        return result;
    }

    @Override
    public JSONArray vehicleTruckTree(String type, boolean configFlag) {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, userOrgListId);
        List<String> assignIdList = new ArrayList<>();
        assembleAssignmentTreeData(type, result, assignmentList, assignIdList);
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = groupDao.vehicleTruckTree(assignIdList);
            // 组装车辆树
            if (vehicleList != null && vehicleList.size() > 0) {
                for (VehicleInfo vehicle : vehicleList) {
                    JSONObject vehicleObj = new JSONObject();
                    vehicleObj.put("id", vehicle.getId());
                    vehicleObj.put("type", "vehicle");
                    vehicleObj.put("iconSkin", "vehicleSkin");
                    vehicleObj.put("pId", vehicle.getAssignmentId());
                    vehicleObj.put("name", vehicle.getBrand());
                    result.add(vehicleObj);
                }
            }
        }
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, type));
        return result;
    }

    /**
     * 同步车辆信息
     * @param form      同步信息
     * @param ipAddress ip地址
     * @return TRUE  OR  FALSE
     */
    @Override
    public boolean updateSynchronizeVehicle(SynchronizeVehicleForm form, String ipAddress) {
        Map<String, String> configMap = newConfigDao.getVehicleByDeviceNumber(form.getDeviceNumber());
        if (StringUtils.isBlank(configMap.get("configId"))) {
            return false;
        }
        form.setId(configMap.get("id"));
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean synchronizeVehicle = newVehicleDao.updateSynchronizeVehicle(form);
        if (synchronizeVehicle) {
            String log =
                "同步车辆信息：" + configMap.get("brand") + "( @" + userService.getOrgByUuid(configMap.get("group_id"))
                    .getName() + " )";
            logSearchServiceImpl
                .addLog(ipAddress, log, "3", "", configMap.get("brand"), String.valueOf(configMap.get("plate_color")));
        }
        return synchronizeVehicle;

    }

    @Override
    public int getIsBandVehicleByBrandModelsId(String id) {
        return newVehicleDao.getIsBandVehicleByBrandModelsId(id);
    }

    @Override
    public List<VehicleInfo> findAllSendVehicle(String userId, List<String> groupList) {
        List<VehicleInfo> list = new ArrayList<>();
        if (StringUtils.isNotBlank(userId) && groupList != null && groupList.size() > 0) {
            list = newVehicleDao.findAllSendVehicle(userId, groupList);
        }
        // 解决bug1436,实时监控：修改用户的权限后，实时监控的车辆数量获取错误
        // if (!list.isEmpty() && (list instanceof Page<?>)) { //
        // 判断查询是否是page,若是page，重新查询一次
        // list = vehicleDao.findAllSendVehicle(userId, groupList);
        // }
        return list;
    }

    @Override
    public String getGroupID(String brand) {
        VehicleDO vehicleDO = newVehicleDao.getByBrand(brand);
        return vehicleDO == null ? null : vehicleDO.getOrgId();
    }

    @Override
    public String getFuelType(String brand) {
        return newVehicleDao.getFuelType(brand);
    }

    @Override
    public JSONArray assignVehicleTreeForApp() {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getId().toString());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, userOrgListId);
        List<String> assignIdList = new ArrayList<>();
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                // 分组id list
                assignIdList.add(assign.getId());
                // 组装分组树
                JSONObject assignmentObj = new JSONObject();
                assignmentObj.put("id", assign.getId());
                // assignmentObj.put("pId", assign.getGroupId());
                assignmentObj.put("name", assign.getName());
                assignmentObj.put("type", "assignment");
                assignmentObj.put("iconSkin", "assignmentSkin");
                result.add(assignmentObj);
            }
        }
        // 查询分组下面的车辆(并且已绑定)
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList = groupDao.findVehicleByAssignmentIds(assignIdList, "", null);
            // 组装车辆树
            result.addAll(JsonUtil.getVehicleTree(vehicleList));
            return result;
        }
        return null;
    }

    @Override
    public List<String> findVehicleByGroupAssign(String groupId) {
        if (StringUtils.isNotBlank(groupId)) {
            return newVehicleDao.findVehicleByGroupAssign(groupId);
        }
        return new ArrayList<>(0);
    }

    @Override
    public List<VehicleInfo> findVehicleByIds(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            return newVehicleDao.findVehicleByIds(ids);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> findParmId(String vid) {
        return newVehicleDao.findParamId(vid);
    }

    @Override
    public List<Map<String, String>> findParmStatus(String simId) {
        return newVehicleDao.findParamStatus(simId);
    }

    @Override
    public boolean addVehiclePurpose(VehiclePurposeForm vehiclePurposes, String ipAddress) {
        VehiclePurposeDTO vehiclePurposeDTO = vehiclePurposes.convert();
        VehiclePurposeDO vehiclePurposeDO = new VehiclePurposeDO();
        vehiclePurposeDTO.setId(UUID.randomUUID().toString());
        BeanUtils.copyProperties(vehiclePurposeDTO, vehiclePurposeDO);
        vehiclePurposeDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = vehiclePurposeDao.insert(vehiclePurposeDO);
        if (flag) {
            String msg = "新增运营类别：" + vehiclePurposes.getPurposeCategory();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "", "");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Page<VehiclePurposeDTO> findVehiclePurposeByPage(VehiclePurposeQuery query) {
        String keyword = StringUtil.mysqlLikeWildcardTranslation(query.getPurposeCategory());
        List<VehiclePurposeDTO> purposeList = vehiclePurposeDao.getByKeyword(keyword);
        return RedisQueryUtil.getListToPage(purposeList, query, purposeList.size());
    }

    @Override
    public VehiclePurposeDO get(String id) {
        return vehiclePurposeDao.getById(id);
    }

    @Override
    public JsonResultBean updateVehiclePurpose(VehiclePurposeForm form, String ipAddress) {
        VehiclePurposeDO vehiclePurpose = get(form.getId()); // 根据运营类别id查询运营类别
        if (vehiclePurpose != null) {
            // 修改前运营类别
            String beforePurposeCategory = vehiclePurpose.getPurposeCategory();
            VehiclePurposeDO vehiclePurposeDO = new VehiclePurposeDO();
            BeanUtils.copyProperties(form.convert(), vehiclePurposeDO);
            vehiclePurposeDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag = vehiclePurposeDao.update(vehiclePurposeDO);
            if (flag) { // 修改成功则记录日志
                String msg;
                if (beforePurposeCategory.equals(form.getPurposeCategory())) {
                    msg = "修改运营类别：" + form.getPurposeCategory();
                } else {
                    msg = "修改运营类别：" + beforePurposeCategory + " 修改为：" + form.getPurposeCategory();
                }
                logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", ""); // 记录日志
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public boolean deletePurpose(String id, String ipAddress) {
        VehiclePurposeDO vehiclePurpose = get(id);
        boolean flag = vehiclePurposeDao.delete(id);;
        if (flag) { // 删除成功则记录日志
            if (vehiclePurpose != null) {
                logSearchServiceImpl
                    .addLog(ipAddress, "删除运营类别：" + vehiclePurpose.getPurposeCategory(), "3", "", "", "");
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * 根据id批量删除运营类别
     */
    @Override
    public boolean deleteVehiclePurposeMuch(List<String> ids, String ipAddress) {
        StringBuilder message = new StringBuilder();
        int delNum = 0;
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                VehiclePurposeDO vehiclePurpose = get(id);
                if (vehiclePurpose != null) {
                    message.append("删除运营类别 : ").append(vehiclePurpose.getPurposeCategory()).append(" <br/>");
                }
            }
            delNum = vehiclePurposeDao.delBatch(ids); // 批量删除运营类别
        }
        if (delNum > 0) { // 删除成功则记录日志
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量删除运营类别");
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean getVehiclePurposeTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("运营类别");
        headList.add("备注");
        // 必填字段
        requiredList.add("运营类别");

        // 默认设置一条数据
        exportList.add("载客车");
        exportList.add("专门用作人员乘坐的汽车");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>();

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
     * 批量导入
     */
    @Override
    @MethodLog(name = "运营类别批量导入", description = "运营类别批量导入")
    public Map importPurpose(MultipartFile file, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        Row row = importExcel.getRow(0);
        String string = importExcel.getCellValue(row, 0).toString();
        StringBuilder errorMsgBuilder = new StringBuilder();
        StringBuilder message = new StringBuilder();
        if (string.equals("运营类别*")) {
            // 将excel转换成list
            List<VehiclePurposeForm> list = importExcel.getDataList(VehiclePurposeForm.class);
            List<VehiclePurposeDO> importList = new ArrayList<>();
            String temp;
            // 检验需要导入的
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    VehiclePurposeForm purposeFrom = list.get(i);
                    String purposeCategory = purposeFrom.getPurposeCategory();
                    if (purposeFrom.isDuplicated()) {
                        // 该行为重复行，跳过
                        continue;
                    }
                    // 检验必填字段
                    if (StringUtils.isBlank(purposeCategory)) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                        continue;
                    }
                    // 检验列表中重复数据
                    for (int j = i + 1; j < list.size(); j++) {
                        final VehiclePurposeForm purposeForm = list.get(j);
                        if (!StringUtils.isBlank(purposeForm.getPurposeCategory()) && purposeForm.getPurposeCategory()
                            .equals(purposeCategory)) {
                            temp = purposeCategory;
                            errorMsgBuilder.append("第").append(i + 1).append("行运营类别和第").append(j + 1)
                                .append("行运营类别重复，值为：").append(temp);
                            // list.remove(j);
                            purposeForm.setDuplicated(true);
                        }
                    }
                    // 检验当前运营类别在数据库是否已存在
                    List<VehiclePurposeDTO> puList = vehiclePurposeDao.getByKeyword(purposeCategory);
                    if (puList.size() != 0) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据,运营类别为“")
                            .append(purposeCategory).append("”已存在！！<br/>");
                        continue;
                    }
                    // 运营类别格式验证
                    Matcher matcher = PURPOSE_PATTERN.matcher(purposeCategory);
                    if (!matcher.matches()) { // 格式不匹配
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据,运营类别名称长度不在2—20之间或包含了特殊符号！<br/>");
                        continue;
                    }

                    // 验证字段长度
                    if (Converter.toBlank(purposeCategory).length() > 20) {
                        purposeFrom.setPurposeCategory("");
                    }
                    String description = purposeFrom.getDescription();
                    if (Converter.toBlank(description).length() > 50) {
                        purposeFrom.setDescription("");
                    }
                    String id = purposeFrom.getId();
                    VehiclePurposeDO vehiclePurposeDO = new VehiclePurposeDO(id, purposeCategory, description, null);
                    vehiclePurposeDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    importList.add(vehiclePurposeDO);
                    message.append("导入运营类别 : ").append(purposeCategory).append(" <br/>");
                }

                // 组装导入结果
                if (importList.size() > 0) {
                    int addNum = vehiclePurposeDao.addBatch(importList);
                    if (addNum > 0) {
                        resultInfo +=
                            "导入成功" + importList.size() + "条数据，导入失败" + (list.size() - importList.size()) + "条数据";
                        resultMap.put("flag", 1);
                        resultMap.put("errorMsg", errorMsgBuilder.toString());
                        resultMap.put("resultInfo", resultInfo);
                        logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "导入运营类别");
                    } else {
                        resultMap.put("flag", 0);
                        resultMap.put("resultInfo", "导入失败！");
                        return resultMap;
                    }
                } else {
                    resultMap.put("flag", 0);
                    resultMap.put("errorMsg", errorMsgBuilder.toString());
                    resultMap.put("resultInfo", "成功导入0条数据。");
                    return resultMap;
                }
            } else {
                resultMap.put("flag", 0);
                resultMap.put("errorMsg", errorMsgBuilder.toString());
                resultMap.put("resultInfo", "成功导入0条数据！");
                return resultMap;
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsgBuilder.toString());
            resultMap.put("resultInfo", "运营类别导入模板不正确！");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public List<VehiclePurpose> findVehiclePurpose(String purposeCategory) {
        return vehiclePurposeDao.getByKeyword(purposeCategory).stream().map(VehiclePurpose::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<VehiclePurpose> findVehicleCategory() {
        return vehiclePurposeDao.findVehicleCategory();
    }

    @Override
    public List<FuelTypeDO> findFuelType() {
        return fuelTypeDao.getAll();
    }

    /**
     * 根据人员编号查询人员监控对象详细信息
     * @param brand 人员编号
     * @return 监控对象信息
     */
    @Override
    public List<Map<String, String>> findPeopleByNumber(String brand) {
        return newVehicleDao.findPeopleByNumber(brand);
    }

    @Override
    public String findColorByBrand(String brand) {
        if (StringUtils.isBlank(brand)) {
            return "";
        }
        VehicleDO vehicleDO = newVehicleDao.getByBrand(brand);
        if (vehicleDO == null) {
            return "";
        }
        Integer plateColor = vehicleDO.getPlateColor();
        return plateColor != null ? plateColor.toString() : "";
    }

    /**
     * 组装分组树的数据到给定的JSONArray中
     * @param type           树的类型，分为single和multiple
     * @param result         组装的JSONArray
     * @param assignmentList 分组列表
     * @param assignIdList   分组ID列表
     */
    private void assembleAssignmentTreeData(String type, JSONArray result, List<Assignment> assignmentList,
        List<String> assignIdList) {
        if (assignmentList != null && assignmentList.size() > 0) {
            for (Assignment assign : assignmentList) {
                OrganizationLdap organization = userService.getOrgByUuid(assign.getGroupId());
                if (organization != null && organization.getId() != null) {
                    // 分组id list
                    assignIdList.add(assign.getId());
                    // 组装分组树
                    JsonUtil.addAssignmentObj(assign, organization, type, result);
                }
            }
        }
    }

    @Override
    public JSONArray vehicleTempSensorPermissionTree(String type, int sensorType, boolean configFlag) {
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // 查询当前用户权限分组
        List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, userOrgListId);
        List<String> assignIdList = new ArrayList<>();
        assembleAssignmentTreeData(type, result, assignmentList, assignIdList);
        // 组装组织树结构
        result.addAll(JsonUtil.getOrgTree(orgs, type));
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
        if (assignIdList.size() > 0) {
            List<VehicleInfo> vehicleList =
                groupDao.findTempSensoreVehicleByAssignmentId(sensorType, assignIdList);
            // 组装车辆树
            result.addAll(JsonUtil.getVehicleTree(vehicleList));
        }
        return result;
    }

    @Override
    public List<String> findBrandsByIds(Collection<String> ids) {
        return CollectionUtils.isEmpty(ids) ? Collections.emptyList() : newVehicleDao.findBrandsByIds(ids);
    }

    @Override
    public List<VehicleInfo> findMonitorByName(String name) {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        List<String> groupList = userService.getOrgUuidsByUser(userId);
        return newVehicleDao.findMonitorByName(uuid, groupList, name);
    }

    @Override
    public Set<String> getRedisAssignVid() {
        return getRedisAssignVid(SystemHelper.getCurrentUser().getUsername());
    }

    @Override
    public Set<String> getRedisAssignVid(String username) {
        final RedisKey userGroupKey = RedisKeyEnum.USER_GROUP.of(username);
        final Set<String> userGroupIds = RedisHelper.getSet(userGroupKey);
        final List<RedisKey> groupMonitorKeys =
                userGroupIds.stream().map(RedisKeyEnum.GROUP_MONITOR::of).collect(Collectors.toList());
        return RedisHelper.batchGetSet(groupMonitorKeys);
    }

    private List<String> getDeviceTypes(String deviceType, List<String> deviceTypes) {
        if (StringUtils.isNotEmpty(deviceType)) {
            if ("1".equals(deviceType)) {
                deviceTypes = Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE);
            } else {
                deviceTypes = new ArrayList<>();
                deviceTypes.add(deviceType);
            }
        }
        return deviceTypes;
    }

    @Override
    public List<BrandInfo> findBrand() {
        return brandDao.findBrand(null);
    }

    @Override
    public Map<String, String> getVehPurposeMap() {
        return vehiclePurposeDao.getByKeyword(null).stream()
            .collect(Collectors.toMap(VehiclePurposeDTO::getId, VehiclePurposeDTO::getPurposeCategory));
    }

    @Override
    public Map<String, String> get809VehicleStaticData(String vehicleId) {
        Map<String, String> config = new HashMap<>();
        final RedisKey monitorKey = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        Map<String, String> vehicleInfo = RedisHelper.getHashMap(monitorKey, "orgId", "vehiclePurpose", "vehicleType");
        String orgId = vehicleInfo.get("orgId");
        // 业户名称
        String ownersName = "";
        // 业户电话
        String ownersTel = "";
        if (StringUtils.isNotBlank(orgId)) {
            //获取车辆所属企业
            OrganizationLdap orgInfo = userService.getOrgByUuid(orgId);
            if (orgInfo != null) {
                // 企业名称
                ownersName = orgInfo.getName();
                // 企业电话
                ownersTel = orgInfo.getPhone();
            }
        }
        config.put("owersName", ownersName);
        config.put("owersTel", ownersTel);
        // 车辆运营类别Id
        String vehiclePurposeId = vehicleInfo.get("vehiclePurpose");
        // 车辆类型Id
        String vehicleTypeId = vehicleInfo.get("vehicleType");
        // 运输行业编码
        config.put("transType", getTransTypeByVehiclePurpose(vehiclePurposeId));
        // 车辆类别信息
        VehicleCategoryDO vehicleCategoryDO = newVehicleCategoryDao.getByVehicleTypeId(vehicleTypeId);
        if (vehicleCategoryDO != null && vehicleCategoryDO.getStandard() == 0) { // 如果是通用标准,才需转换为数字码
            config.put("vehicleTypeCode", vehicleCategoryDO.getCodeNum());
        }
        return config;
    }

    /**
     * 根据车辆运营类别id获取运输行业编号
     */
    private String getTransTypeByVehiclePurpose(String vehiclePurpose) {
        String transType = "";
        if (StringUtils.isNotBlank(vehiclePurpose)) {
            // 根据运营类别id查询运营类别名称
            VehiclePurposeDO vehiclePurposeDO = vehiclePurposeDao.getById(vehiclePurpose);
            if (vehiclePurposeDO != null) {
                transType = getTransTypeByPurposeType(vehiclePurposeDO.getPurposeCategory());
            }
        }
        return transType;
    }

    /**
     * 根据监控对象的运营类别获取运输行业编号
     */
    @Override
    public String getTransTypeByPurposeType(String name) {
        return CommonTypeUtils.getTransTypeByPurposeType(name);
    }

    @Override
    public JSONObject getOilVehicleSettingMonitorTree() throws Exception {
        JSONObject obj = new JSONObject();
        JSONArray result = new JSONArray();
        List<OrganizationLdap> orgs = getUserOrgInfo();
        List<String> orgId = fuzzyGroupNameGetOrgId(NULL_STR, NULL_STR, orgs);
        List<Assignment> assignmentList = getUserHaveAssignmentByFuzzy(orgId, NULL_STR, NULL_STR);
        List<String> assignmentId = assignmentList.stream().map(Assignment::getId).collect(Collectors.toList());
        List<ConfigList> configInfo = getOilVehicleSettingMonitor(NULL_STR, NULL_STR);
        filtrationMonitor(assignmentId, configInfo);
        setAssignmentMonitorNumber(configInfo, assignmentList);
        // 判断用户权限下设置了油量管理设置的监控对象个数是否大于5000
        if (configInfo.size() > PublicVariable.MONITOR_COUNT) { // 只返回分组树
            assignmentService.putAssignmentTree(assignmentList, result, "multiple", true);
            // 组装组织树结构
            result.addAll(JsonUtil.getOrgTree(orgs, "multiple"));
            obj.put("size", configInfo.size());
            obj.put("tree", result);
            return obj;
        } else {
            assignmentService.putAssignmentTree(assignmentList, result, "multiple", false);
            // 组装组织树结构
            result.addAll(JsonUtil.getOrgTree(orgs, "multiple"));
        }
        // 组装车辆树
        if (CollectionUtils.isNotEmpty(configInfo)) {
            for (ConfigList vehicle : configInfo) {
                JSONObject vehicleObj = new JSONObject();
                putOilSettingMonitorTree(vehicle, vehicleObj, false); // 组装监控对象
                vehicleObj.put("id", vehicle.getVehicleId());
                result.add(vehicleObj);
            }
        }
        obj.put("size", configInfo.size());
        obj.put("tree", result);
        return obj;
    }

    private List<ConfigList> getOilVehicleSettingMonitor(String param, String queryPattern) throws Exception {
        // 获取绑定油箱的监控对象
        Map<String, String> oilVehicleSetting =
            redisVehicleService.getVehicleBindByType(RedisKeys.SensorType.SENSOR_OIL_BOX_MONITOR);
        List<String> vehicleIds = new ArrayList<>(oilVehicleSetting.keySet());
        // 获取用户权限的监控对象,取交集
        final RedisKey userGroupKey = RedisKeyEnum.USER_GROUP.of(SystemHelper.getCurrentUsername());
        final Set<String> groupIds = RedisHelper.getSet(userGroupKey);
        final List<RedisKey> groupMonitorKeys =
                groupIds.stream().map(RedisKeyEnum.GROUP_MONITOR::of).collect(Collectors.toList());
        final Set<String> userVehicleIds = Lists.partition(groupMonitorKeys, 10).stream()
                .map(RedisHelper::batchGetSet)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        vehicleIds.retainAll(userVehicleIds);
        // 获取设置了油量管理设置的监控对象的信息
        final Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        List<ConfigList> configList = bindInfoMap.values().stream().map(ConfigList::new).collect(Collectors.toList());
        if (StringUtils.isBlank(param) || !"monitor".equals(queryPattern)) {
            return configList;
        }
        List<ConfigList> fuzzyList = new ArrayList<>();
        configList.forEach(configInfo -> {
            String monitorName = configInfo.getCarLicense().toUpperCase();
            String fuzzyParam = param.toUpperCase();
            if (monitorName.contains(fuzzyParam)) {
                fuzzyList.add(configInfo);
            }
        });
        return fuzzyList;
    }

    /**
     * 组装分组内的监控对象数量
     */
    private void setAssignmentMonitorNumber(List<ConfigList> configInfo, List<Assignment> assignmentList) {
        if (CollectionUtils.isNotEmpty(assignmentList)) {
            Map<String, Integer> assignmentMonitorNumber = getAssignmentMonitorNumber(configInfo);
            for (Assignment assign : assignmentList) {
                Integer count = assignmentMonitorNumber.get(assign.getId());
                assign.setMNum(count == null ? Integer.valueOf(0) : count);
            }
        }
    }

    /**
     * 计算用户权限分组下有多少设置了油量车辆设置的监控对象
     */
    private Map<String, Integer> getAssignmentMonitorNumber(List<ConfigList> configInfo) {
        Map<String, Integer> assignmentMonitorNumber = new HashMap<>();
        if (configInfo != null && configInfo.size() > 0) {
            configInfo.forEach(info -> {
                String vehicleAssignmentId = info.getAssignmentId();
                if (StringUtils.isNotBlank(vehicleAssignmentId)) {
                    List<String> assignments = Arrays.asList(vehicleAssignmentId.split(SYMBOL_COMMASY));
                    assignments.forEach(id -> {
                        Integer number = assignmentMonitorNumber.get(id);
                        if (number == null) {
                            number = 0;
                        }
                        number = number + 1;
                        assignmentMonitorNumber.put(id, number);
                    });
                }
            });
        }
        return assignmentMonitorNumber;
    }

    /**
     * 组装监控对象
     */
    private void putOilSettingMonitorTree(ConfigList vehicle, JSONObject vehicleObj, boolean isChecked) {
        vehicleObj.put("id", vehicle.getVehicleId());
        if ("0".equals(vehicle.getMonitorType())) {
            vehicleObj.put("type", "vehicle");
            vehicleObj.put("iconSkin", "vehicleSkin");
        } else if ("1".equals(vehicle.getMonitorType())) {
            vehicleObj.put("type", "people");
            vehicleObj.put("iconSkin", "peopleSkin");
        } else if ("2".equals(vehicle.getMonitorType())) {
            vehicleObj.put("type", "thing");
            vehicleObj.put("iconSkin", "thingSkin");
        }
        vehicleObj.put("pId", vehicle.getAssignmentId());
        vehicleObj.put("name", vehicle.getCarLicense());
        vehicleObj.put("deviceNumber", vehicle.getDeviceNumber());
        vehicleObj.put("isVideo", vehicle.getIsVideo());
        vehicleObj.put("deviceType", vehicle.getDeviceType());
        vehicleObj.put("plateColor", vehicle.getPlateColor());
        vehicleObj.put("simcardNumber", vehicle.getSimcardNumber());
        vehicleObj.put("professional", vehicle.getProfessionalNames());
        vehicleObj.put("assignName", vehicle.getGroupName());
        if (isChecked) {
            vehicleObj.put("checked", true); //默认选中
        }

    }

    @Override
    public JSONArray getOilVehicleSetMonitorByAssign(String assignmentId, boolean isChecked, String type)
        throws Exception {
        JSONArray result = new JSONArray();
        if (StringUtils.isBlank(assignmentId)) {
            return result;
        }
        if (StringUtils.isBlank(type)) {
            return result;
        }
        List<String> resultAssignList = getAssignmentListByParId(assignmentId, type);
        if (CollectionUtils.isEmpty(resultAssignList)) {
            return result;
        }
        List<ConfigList> configInfo = getOilVehicleSettingMonitor(NULL_STR, NULL_STR);
        filtrationMonitor(resultAssignList, configInfo);
        if (CollectionUtils.isNotEmpty(configInfo)) {
            for (ConfigList vehicle : configInfo) {
                JSONObject vehicleObj = new JSONObject();
                // 组装将空对象数据
                putOilSettingMonitorTree(vehicle, vehicleObj, isChecked);
                result.add(vehicleObj);
            }
        }
        return result;
    }

    /**
     * 油量里程报警组织树模糊搜索
     */
    @Override
    public JSONArray getOilVehicleSetMonitorByFuzzy(String param, String queryPattern) throws Exception {
        JSONArray result = new JSONArray();
        List<OrganizationLdap> orgs = getUserOrgInfo();
        if (CollectionUtils.isEmpty(orgs) && "group".equals(queryPattern)) {
            return result;
        }
        List<String> orgId = fuzzyGroupNameGetOrgId(param, queryPattern, orgs);
        List<Assignment> assignmentList = getUserHaveAssignmentByFuzzy(orgId, param, queryPattern);
        if (CollectionUtils.isEmpty(assignmentList) && "assignment".equals(queryPattern)) {
            return result;
        }
        assignmentService.putAssignmentTree(assignmentList, result, "multiple", true); // 组装树
        // 获取监控对象
        List<ConfigList> configInfo = getOilVehicleSettingMonitor(param, queryPattern);
        if (CollectionUtils.isEmpty(configInfo) && "monitor".equals(queryPattern)) {
            return result;
        }
        if ("assignment".equals(queryPattern)) {
            //模糊查询分组 重新过滤企业
            Set<OrganizationLdap> orgResult = new HashSet<>();

            List<String> pids = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                pids.add(result.getJSONObject(i).getString("pId"));
            }
            for (String pid : pids) {
                filterGroup(orgs, orgResult, pid);
            }
            orgs.clear();
            orgs.addAll(orgResult);
        }
        List<String> assignmentId = assignmentList.stream().map(Assignment::getId).collect(Collectors.toList());
        filtrationMonitor(assignmentId, configInfo);
        result.addAll(JsonUtil.getOrgTree(orgs, "multiple"));
        // 组装车辆树
        if (configInfo.size() > 0) {
            for (ConfigList vehicle : configInfo) {
                JSONObject vehicleObj = new JSONObject();
                putOilSettingMonitorTree(vehicle, vehicleObj, false); // 组装监控对象
                vehicleObj.put("id", vehicle.getVehicleId());
                result.add(vehicleObj);
            }
        }
        return result;
    }

    private List<String> fuzzyGroupNameGetOrgId(String fuzzyParam, String queryPattern,
        List<OrganizationLdap> userAllOrg) {
        if (StringUtils.isBlank(fuzzyParam) || !"group".equals(queryPattern)) {
            return userAllOrg.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        }
        List<OrganizationLdap> fuzzyOrgLdap = new ArrayList<>();
        userAllOrg.forEach(orgLdap -> {
            if (orgLdap == null || StringUtils.isBlank(orgLdap.getName())) {
                return;
            }
            String groupName = orgLdap.getName().toUpperCase();
            String fuzzy = fuzzyParam.toUpperCase();
            if (groupName.contains(fuzzy)) {
                fuzzyOrgLdap.add(orgLdap);
            }
        });
        Set<OrganizationLdap> orgResult = new HashSet<>();
        // 组装当前符合条件的组织的父节点信息
        for (OrganizationLdap org : fuzzyOrgLdap) {
            TreeUtils.getLowerOrg(orgResult, userAllOrg, org);
        }
        userAllOrg.clear();
        userAllOrg.addAll(orgResult);
        return fuzzyOrgLdap.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
    }

    private Map<String, String> getUserAssignmentInfo() {
        Map<String, String> assignName = new HashMap<>();
        String userId = SystemHelper.getCurrentUserId();
        if (userId == null) {
            return assignName;
        }
        List<Assignment> assignList = groupDao.findUserAssignment(userId, null);
        if (CollectionUtils.isEmpty(assignList)) {
            return assignName;
        }
        for (Assignment assignment : assignList) {
            String assignId = assignment.getId();
            if (!assignName.containsKey(assignId)) {
                String name = assignment.getName();
                assignName.put(assignId, name);
            }
        }
        return assignName;
    }

    /**
     * 过滤监控对象
     */
    private void filtrationMonitor(List<String> assignmentId, List<ConfigList> configInfo) {
        // 重新过滤监控对象
        if (CollectionUtils.isNotEmpty(configInfo)) {
            List<ConfigList> vehicleList = new ArrayList<>();
            // 查询用户权限下所有的分组信息
            Map<String, String> userAllAssignment = getUserAssignmentInfo();
            for (ConfigList info : configInfo) {
                String infoAssignmentId = info.getAssignmentId();
                List<String> allAssignmentIds = new ArrayList<>(Arrays.asList(infoAssignmentId.split(SYMBOL_COMMASY)));
                for (String assId : allAssignmentIds) {
                    if (!assignmentId.contains(assId)) {
                        continue;
                    }
                    String assName = userAllAssignment.get(assId);
                    ConfigList configList = new ConfigList();
                    BeanUtils.copyProperties(info, configList);
                    configList.setAssignmentId(assId);
                    configList.setAssignmentName(assName);
                    vehicleList.add(configList);
                }
            }
            configInfo.clear();
            configInfo.addAll(vehicleList);
        }
    }

    /**
     * 获取当前用户组织及下级组织
     */
    private List<OrganizationLdap> getUserOrgInfo() {
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        return userService.getOrgChild(orgId);
    }

    /**
     * 获取用户权限分组并统计分组下有多少设置了油量车辆设置的监控对象
     */
    private List<Assignment> getUserHaveAssignmentByFuzzy(List<String> userOrgListId, String param,
        String queryPattern) {
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String userUuId = userService.getUserUuidById(userId);
        // 查询当前用户权限分组
        List<Assignment> assignmentList = groupDao.findUserAssignment(userUuId, userOrgListId);
        if (StringUtils.isBlank(param) || !"assignment".equals(queryPattern)) {
            return assignmentList;
        }
        List<Assignment> fuzzyAssignment = new ArrayList<>();
        assignmentList.forEach(assignment -> {
            if (assignment == null || StringUtils.isBlank(assignment.getName())) {
                return;
            }
            String groupName = assignment.getName().toUpperCase();
            String fuzzy = param.toUpperCase();
            if (groupName.contains(fuzzy)) {
                fuzzyAssignment.add(assignment);
            }
        });
        return fuzzyAssignment;
    }

    @Override
    public int getSensorVehicleNumberByPid(String parId, String type) throws Exception {
        if (StringUtils.isBlank(parId)) {
            return NUMBER_ZERO;
        }
        if (StringUtils.isBlank(type)) {
            return NUMBER_ZERO;
        }
        List<String> resultAssignList = getAssignmentListByParId(parId, type);
        if (CollectionUtils.isEmpty(resultAssignList)) {
            return NUMBER_ZERO;
        }
        List<ConfigList> configInfo = getOilVehicleSettingMonitor(NULL_STR, NULL_STR);
        Map<String, Integer> assignmentMonitorNumber = getAssignmentMonitorNumber(configInfo);
        int monitorCountNumber = 0;
        for (String assignId : resultAssignList) {
            if (!assignmentMonitorNumber.containsKey(assignId)) {
                continue;

            }
            Integer number = assignmentMonitorNumber.get(assignId);
            if (number != null) {
                monitorCountNumber += number;
            }
        }
        return monitorCountNumber;
    }

    /**
     * 根据父节点和父节点类型获取分组列表
     */
    private List<String> getAssignmentListByParId(String parId, String type) {
        if ("group".equals(type)) {
            List<String> assignList = new ArrayList<>();
            // 根据用户名获取用户id
            String userId = SystemHelper.getCurrentUser().getId().toString();
            String uuid = userService.getUserUuidById(userId);
            List<OrganizationLdap> childGroup = userService.getOrgChild(parId);
            List<String> groupList = new ArrayList<>();
            if (childGroup != null && !childGroup.isEmpty()) {
                for (OrganizationLdap group : childGroup) {
                    groupList.add(group.getUuid());
                }
            }
            // 查询组织下的分组
            List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, groupList);
            if (assignmentList != null && !assignmentList.isEmpty()) {
                for (Assignment anAssignmentList : assignmentList) {
                    assignList.add(anAssignmentList.getId());
                }
            }
            return assignList;
        } else if ("assignment".equals(type)) {
            List<String> assignList = new ArrayList<>();
            assignList.add(parId);
            return assignList;
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, JSONArray> getSensorVehicleByGroupId(String groupId, String type, boolean isChecked)
        throws Exception {
        Map<String, JSONArray> result = new HashMap<>();
        if (StringUtils.isBlank(groupId)) {
            return result;
        }
        if (StringUtils.isBlank(type)) {
            return result;
        }
        List<String> resultAssignList = getAssignmentListByParId(groupId, type);
        if (CollectionUtils.isEmpty(resultAssignList)) {
            return result;
        }
        List<ConfigList> configInfo = getOilVehicleSettingMonitor(NULL_STR, NULL_STR);
        filtrationMonitor(resultAssignList, configInfo);
        if (CollectionUtils.isNotEmpty(configInfo)) {
            for (ConfigList vehicle : configInfo) {
                JSONObject vehicleObj = new JSONObject();
                // 组装将空对象数据
                putOilSettingMonitorTree(vehicle, vehicleObj, isChecked);
                JSONArray array = new JSONArray();
                if (result.containsKey(vehicle.getAssignmentId())) { // 包含
                    array = result.get(vehicle.getAssignmentId());
                }
                array.add(vehicleObj);
                result.put(vehicle.getAssignmentId(), array);
            }
        }
        return result;
    }

    private void installQuitPeople(JSONArray result, List<LeaveJobPersonnel> leaveJobPersonnelList) {
        if (CollectionUtils.isNotEmpty(leaveJobPersonnelList)) {
            for (LeaveJobPersonnel leaveJobPersonnel : leaveJobPersonnelList) {
                JSONObject vehicleObj = new JSONObject();
                String assignmentId = leaveJobPersonnel.getAssignmentId();
                vehicleObj.put("id", leaveJobPersonnel.getPeopleId());
                vehicleObj.put("type", "people");
                vehicleObj.put("iconSkin", "peopleSkin");
                vehicleObj.put("pId", assignmentId);
                vehicleObj.put("name", leaveJobPersonnel.getPeopleNumber());
                vehicleObj.put("assignName", leaveJobPersonnel.getAssignmentName());
                for (int i = 0, len = result.size(); i < len; i++) {
                    JSONObject jsonObject = result.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    if (assignmentId.equals(id)) {
                        Integer count = jsonObject.getInteger("count");
                        if (count != null) {
                            count++;
                            jsonObject.put("count", count);
                        }
                        break;
                    }
                }
                result.add(vehicleObj);
            }
        }
    }


    @Override
    public Map<String, VehicleInfo> getVehicleById(Collection<String> vehicleIds) {

        if (CollectionUtils.isEmpty(vehicleIds)) {
            return new HashMap<>(0);
        }
        List<VehicleInfo> list = newVehicleDao.getVehicleByIds(vehicleIds);
        Set<String> orgIds = Sets.newHashSet();
        String groupId;
        for (VehicleInfo vehicleInfo : list) {
            groupId = vehicleInfo.getGroupId();
            orgIds.add(groupId);
        }
        Map<String, VehicleInfo> result = new HashMap<>(list.size() * 2 + 1);
        Map<String, OrganizationLdap> orgList = userService.getOrgByUuids(orgIds);
        if (orgList.isEmpty()) {
            return result;
        }
        OrganizationLdap organizationLdap;
        for (VehicleInfo vehicleInfo : list) {
            groupId = vehicleInfo.getGroupId();
            organizationLdap = orgList.get(groupId);
            if (organizationLdap == null) {
                continue;
            }
            vehicleInfo.setGroupName(organizationLdap.getName());
            result.put(vehicleInfo.getId(), vehicleInfo);
        }
        return result;
    }

}
