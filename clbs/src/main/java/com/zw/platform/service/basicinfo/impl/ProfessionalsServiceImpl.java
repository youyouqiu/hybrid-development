package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.domain.ProfessionalsTypeDO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsGroupForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsTypeForm;
import com.zw.platform.domain.basicinfo.query.ProfessionalsQuery;
import com.zw.platform.domain.basicinfo.query.ProfessionalsTypeQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.repository.modules.ProfessionalsGroupDao;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.talkback.domain.basicinfo.BasicInfo;
import com.zw.talkback.repository.mysql.PeopleBasicInfoDao;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wangying
 */
@Service
public class ProfessionalsServiceImpl implements ProfessionalsService {
    private static final Logger log = LogManager.getLogger(ProfessionalsService.class);

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    private ProfessionalsGroupDao professionalsGroupDao;

    @Autowired
    PeopleBasicInfoDao peopleBasicInfoDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public List<ProfessionalDO> selectLicense(List<String> list) {
        return newProfessionalsDao.getByIds(list);
    }

    /**
     * 修改从业人员
     */
    @MethodLog(name = "修改从业人员", description = "修改从业人员")
    @Override
    public boolean updateProfessionals(ProfessionalsForm professionalsForm) throws Exception {
        // 修改人
        professionalsForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // 修改时间
        professionalsForm.setUpdateDataTime(new Date());
        ProfessionalDO professionalDO = new ProfessionalDO();
        BeanUtils.copyProperties(professionalsForm, professionalDO);
        return newProfessionalsDao.updateProfessionals(professionalDO);
    }

    /**
     * 导出从业人员
     */
    @Override
    @MethodLog(name = "导出从业人员", description = "导出从业人员")
    public boolean exportProfessionals(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, ProfessionalsForm.class, 1);
        List<ProfessionalsForm> exportList = new ArrayList<ProfessionalsForm>();
        // 查询所有的设备
        List<OrganizationLdap> orgLdapList = userService.getAllOrganization(); // 获取分组信息
        List<BasicInfo> allEducation = peopleBasicInfoDao.getAllEducation();
        List<BasicInfo> allNation = peopleBasicInfoDao.getAllNation();
        Map<String, String> nationMap =
            allNation.stream().collect(Collectors.toMap(BasicInfo::getId, BasicInfo::getName));
        Map<String, String> educationMap =
            allEducation.stream().collect(Collectors.toMap(BasicInfo::getId, BasicInfo::getName));

        List<Map<String, Object>> list = professionalsGroupDao
            .findProfessionalsWithGroup(userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString()),
                null);
        for (Map<String, Object> map : list) {
            ProfessionalsForm form = new ProfessionalsForm();

            final DateConverter converter = new DateConverter(null);
            org.apache.commons.beanutils.ConvertUtils.register(converter, java.util.Date.class);
            org.apache.commons.beanutils.BeanUtils.populate(form, map); // map转bean
            // 给form中的groupName赋值
            for (OrganizationLdap ol : orgLdapList) {
                if (Converter.toBlank(form.getGroupName()).equals(Converter.toBlank(ol.getUuid()))) {
                    form.setGroupName(ol.getName());
                    break;
                }
            }
            if (null != form.getIssueCertificateDate()) { // 从业人员资格证发证日期
                form.setIssueCertificateDateStr(Converter.toString(form.getIssueCertificateDate(), "yyyy-MM-dd"));
            }
            if (null != form.getIcCardEndDate()) { // 从业资格证证有效期
                form.setIcCardEndDateStr(Converter.toString(form.getIcCardEndDate(), "yyyy-MM-dd"));
            }
            //生日
            if (null != form.getBirthday()) {
                form.setBirthdayStr(Converter.toString(form.getBirthday(), "yyyy-MM-dd"));
            }
            //入职日期
            if (null != form.getHiredate()) {
                form.setHiredateStr(Converter.toString(form.getHiredate(), "yyyy-MM-dd"));
            }
            //准驾有效期起
            if (null != form.getDrivingStartDate()) {
                form.setDrivingStartDateStr(Converter.toString(form.getDrivingStartDate(), "yyyy-MM-dd"));
            }
            //准驾有效期至
            if (null != form.getDrivingEndDate()) {
                form.setDrivingEndDateStr(Converter.toString(form.getDrivingEndDate(), "yyyy-MM-dd"));
            }
            // 处理性别数据
            if (StringUtils.isNotEmpty(form.getGender())) {
                if ("1".equals(form.getGender())) {
                    form.setGender("男");
                } else if ("2".equals(form.getGender())) {
                    form.setGender("女");
                }
            } else {
                form.setGender("");
            }
            if (Converter.toBlank(form.getState()).equals("0")) {
                form.setState("正常");
            } else if (Converter.toBlank(form.getState()).equals("1")) {
                form.setState("离职");
            } else if (Converter.toBlank(form.getState()).equals("2")) {
                form.setState("停用");
            } else {
                form.setState("");
            }
            //民族和文化程度
            form.setNation(nationMap.get(form.getNationId()));
            form.setEducation(educationMap.get(form.getEducationId()));

            // if (form.getDrivingType() != null) {
            //     form.setDrivingTypeForExport(form.getDrivingType());
            // }
            exportList.add(form);
        }

        export.setDataList(exportList);
        // 输出导文件
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            export.write(out);// 将文档对象写入文件输出流
        } finally {
            IOUtils.closeQuietly(out);
        }
        return true;
    }

    //获取准驾车型
    // public String getDrivingType(Integer drivingType) {
    //     String drivingModel = "";
    //     if (drivingType == null) {
    //         return drivingModel;
    //     }
    //     switch (drivingType) {
    //         case 0:
    //             drivingModel = "A1(大型客车)";
    //             break;
    //         case 1:
    //             drivingModel = "A2(牵引车)";
    //             break;
    //         case 2:
    //             drivingModel = "A3(城市公交车)";
    //             break;
    //         case 3:
    //             drivingModel = "B1(中型客车)";
    //             break;
    //         case 4:
    //             drivingModel = "B2(大型货车)";
    //             break;
    //         case 5:
    //             drivingModel = "C1(小型汽车)";
    //             break;
    //         case 6:
    //             drivingModel = "C2(小型自动挡汽车)";
    //             break;
    //         case 7:
    //             drivingModel = "C3(低速载货汽车)";
    //             break;
    //         case 8:
    //             drivingModel = "C4(三轮汽车)";
    //             break;
    //         case 9:
    //             drivingModel = "D(普通三轮摩托车)";
    //             break;
    //         case 10:
    //             drivingModel = "E(普通二轮摩托车)";
    //             break;
    //         case 11:
    //             drivingModel = "F(轻便摩托车)";
    //             break;
    //         case 12:
    //             drivingModel = "M(轮式自行机械车)";
    //             break;
    //         case 13:
    //             drivingModel = "N(无轨电车)";
    //             break;
    //         case 14:
    //             drivingModel = "P(有轨电车)";
    //             break;
    //         default:
    //             drivingModel = "";
    //     }
    //     return drivingModel;
    // }

    /**
     * 生成模板
     */
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
        List<ProfessionalsTypeDO> professionalsTypeFormList = newProfessionalsDao.findAllProfessionalsType();
        if (!CollectionUtils.isEmpty(professionalsTypeFormList)) {
            exportList.add(professionalsTypeFormList.get(0).getProfessionalstype());
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
        List<ProfessionalsTypeDO> list = newProfessionalsDao.findAllProfessionalsType();
        String[] jobType = new String[list.size() + 1];
        jobType[0] = "请选择岗位类型";
        for (int i = 0; i < list.size(); i++) {
            jobType[i + 1] = list.get(i).getProfessionalstype();
        }
        selectMap.put("岗位类型", jobType);
        //准驾车型
        // String[] drivingType =
        //     {"A1(大型客车)", "A2(牵引车)", "A3(城市公交车)", "B1(中型客车)", "B2(大型货车)", "C1(小型汽车)", "C2(小型自动挡汽车)", "C3(低速载货汽车)",
        //         "C4(三轮汽车)", "D(普通三轮摩托车)", "E(普通二轮摩托车)", "F(轻便摩托车)", "M(轮式自行机械车)", "N(无轨电车)", "P(有轨电车)"};
        // selectMap.put("准驾车型", drivingType);

        // 民族
        List<BasicInfo> allNation = peopleBasicInfoDao.getAllNation();
        if (CollectionUtils.isNotEmpty(allNation)) {
            String[] nations = allNation.stream().map(BasicInfo::getName).toArray(String[]::new);
            selectMap.put("民族", nations);
        }

        // 文化程度
        List<BasicInfo> allEducation = peopleBasicInfoDao.getAllEducation();
        if (CollectionUtils.isNotEmpty(allEducation)) {
            String[] educations = allEducation.stream().map(BasicInfo::getName).toArray(String[]::new);
            selectMap.put("文化程度", educations);
        }

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
     * 根据id查询从业人员
     */
    @MethodLog(name = "根据id查询从业人员", description = "根据id查询从业人员")
    @Override
    public ProfessionalDTO findProfessionalsById(String id) {
        return newProfessionalsDao.getProfessionalById(id);
    }

    /**
     * 查询从业人员(关联group)
     */
    @MethodLog(name = "查询从业人员", description = "查询从业人员")
    @Override
    public Page<Map<String, Object>> findProfessionalsWithGroup(ProfessionalsQuery query) throws Exception {
        if (query.getSimpleQueryParam().equals("正常")) {
            query.setSimpleQueryParam(null);
            query.setState("0");
        } else if (query.getSimpleQueryParam().equals("离职")) {
            query.setSimpleQueryParam(null);
            query.setState("1");
        } else if (query.getSimpleQueryParam().equals("停用")) {
            query.setSimpleQueryParam(null);
            query.setState("2");
        }

        List<String> orgUuidsByUser = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());

        return PageHelperUtil
            .doSelect(query, () -> professionalsGroupDao.findProfessionalsWithGroup(orgUuidsByUser, query));
    }

    /**
     * 根据从业人员id查询
     */
    @MethodLog(name = "根据从业人员id查询", description = "根据从业人员id查询")
    @Override
    public Map<String, Object> findProGroupById(String id) {
        Map<String, Object> result = professionalsGroupDao.findProGroupById(id);
        return result;
    }

    /**
     * 修改从业人员表及关联表
     */
    @MethodLog(name = "修改修改从业人员表及关联表", description = "修改修改从业人员表及关联表")
    @Override
    public boolean updateProGroupByProId(ProfessionalsForm proForm, ProfessionalsGroupForm form) throws Exception {
        boolean prFlag = updateProfessionals(proForm);
        boolean flag = false;
        if (prFlag) {
            form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            flag = professionalsGroupDao.updateProGroupByProId(form);
        }
        return flag;
    }

    /**
     * 查询从业人员和车
     */
    @MethodLog(name = "查询从业人员和车", description = "查询从业人员和车")
    @Override
    public List<Map<String, Object>> findProfessionalAndVehicle() throws Exception {
        return professionalsGroupDao.findProfessionalsAndVehicle(getOrgs());
    }

    private List<String> getOrgs() throws Exception {
        String userId = "uid=admin,ou=organization"; // 客户端暂时没有session,写死为admin
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
        return userOrgListId;
    }

    @Override
    public ProfessionalDO findByProfessionalsInfo(String identity) throws Exception {
        return newProfessionalsDao.findByIdentityOrDrivingLicenseNo(identity);
    }

    @Override
    public int getIsBandGroup(String id) throws Exception {
        return newProfessionalsDao.getBindIds(Collections.singletonList(id)).size();
    }

    @Override
    public int getIsBandGroupByBatch(List<String> ids) throws Exception {
        if (ids != null && !ids.isEmpty()) {
            return newProfessionalsDao.getBindIds(ids).size();
        }
        return 0;
    }

    @Override
    public ProfessionalDO findProfessionalsByName(String name) throws Exception {
        if (StringUtils.isNotBlank(name)) {
            String typeId = newProfessionalsDao.getIcTypeId();
            return newProfessionalsDao.findByNameAndPositionType(name, typeId);
        }
        return null;
    }

    @Override
    public ProfessionalDTO findProfessionalsForNameRep(String id, String name) throws Exception {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(name)) {
            ProfessionalDTO professionalDTO = newProfessionalsDao.getProfessionalById(id);
            if (professionalDTO == null) {
                return null;
            }
            return Objects.equals(professionalDTO.getName(), name) ? professionalDTO : null;
        }
        return null;
    }

    @Override
    public void add(ProfessionalsTypeDO professionalsTypeDO, String ipAddress) throws Exception {
        professionalsTypeDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        professionalsTypeDO.setCreateDataTime(new Date());
        newProfessionalsDao.addProfessionalsType(professionalsTypeDO);
        String msg = "新增 岗位类型：" + professionalsTypeDO.getProfessionalstype();
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
    }

    @Override
    public Page<ProfessionalsTypeDO> findByPage(ProfessionalsTypeQuery query) throws Exception {
        query.setProfessionalstype(StringUtil.mysqlLikeWildcardTranslation(query.getProfessionalstype()));
        return newProfessionalsDao.findProfessionalsTypeDO(query);
    }

    @Override
    public boolean deletePostType(String id, String ipAddress) throws Exception {
        ProfessionalsTypeDO professionalsType = get(id);
        if (professionalsType != null) {
            newProfessionalsDao.deleteProfessionalsById(id);
            String msg = "删除岗位类型：" + professionalsType.getProfessionalstype();
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ProfessionalsTypeDO get(String id) throws Exception {
        return newProfessionalsDao.getProfessionalsType(id);
    }

    @Override
    public JsonResultBean update(ProfessionalsTypeForm form, String ipAddress) throws Exception {
        ProfessionalsTypeDO professionalsType = get(form.getId());// 根据岗位类型id查询岗位类型信息
        String beforeVehicleType = professionalsType.getProfessionalstype();// 修改前的岗位类型
        String nowVehicleType = form.getProfessionalstype();// 现在的岗位类型
        ProfessionalsTypeDO professionalsTypea = findTypeByType(nowVehicleType); // 根据前端传来的岗位类型查询数据库是否有相同的岗位类型
        if (professionalsTypea != null && !nowVehicleType.equals(beforeVehicleType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "修改失败,该岗位类型已经存在");
        }
        ProfessionalsTypeDO newProfessionalsTypeDO = new ProfessionalsTypeDO(form);
        newProfessionalsTypeDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = newProfessionalsDao.updateProfessionalsType(newProfessionalsTypeDO);
        if (flag) {
            String msg = "";
            // 如果修改前的岗位类型和修改后的岗位类型相同,说明并没有修改过岗位类型字段,修改的是其他
            if (professionalsType.getProfessionalstype().equals(form.getProfessionalstype())) {
                msg = "修改岗位类型：" + form.getProfessionalstype();
            } else {
                msg = "修改岗位类型：" + beforeVehicleType + " 修改为：" + form.getProfessionalstype();
            }
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @Override
    public boolean deleteMore(List<String> ids, String ipAddress) throws Exception {
        StringBuilder msg = new StringBuilder();
        List<String> pids = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) { // 此循环用来查询记录被删除的岗位类型
                ProfessionalsTypeDO professionalsType = get(id);
                if (professionalsType != null) {
                    msg.append("删除岗位类型 : ").append(professionalsType.getProfessionalstype()).append("<br/>");
                }
                List<String> professId = newProfessionalsDao.findProfessionalIdByJobType(id);
                pids.addAll(professId);
            }
            boolean flag = newProfessionalsDao.deleteProfessionalsTypeByBatch(ids); // 批量删除岗位类型
            if (flag) { // 如果批量删除成功
                if (ids.size() > 1) {
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "批量删除岗位类型"); // 记录日志
                } else {
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "", "-", "");
                }
                if (pids.size() > 0) {
                    // 更新数据库数据
                    newProfessionalsDao.updateProfessionPositionType(pids);
                    // 更新缓存数据
                    List<RedisKey> redisKeys =
                        pids.stream().map(RedisKeyEnum.PROFESSIONAL_INFO::of).collect(Collectors.toList());
                    Map<String, String> hashMap = new HashMap<>(2);
                    hashMap.put("type", "");
                    RedisHelper.batchAddToHash(redisKeys, hashMap);
                }
                return true;
            }
        }
        return false;

    }

    @MethodLog(name = "导出", description = "导出")
    @Override
    public boolean exportType(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, ProfessionalsTypeForm.class, 1, null);
        List<ProfessionalsTypeDO> allProfessionalsType = newProfessionalsDao.findAllProfessionalsType();
        List<ProfessionalsTypeForm> exportList = new ArrayList<>();
        for (ProfessionalsTypeDO professionalsTypeDO : allProfessionalsType) {
            ProfessionalsTypeForm professionalsTypeForm = new ProfessionalsTypeForm();
            professionalsTypeForm.setProfessionalstype(professionalsTypeDO.getProfessionalstype());
            professionalsTypeForm.setDescription(professionalsTypeDO.getDescription());
            exportList.add(professionalsTypeForm);
        }
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        // 将文档对象写入文件输出流
        export.write(out);
        out.close();
        return true;
    }

    @Override
    @MethodLog(name = "批量导入", description = "批量导入")
    public Map importType(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        StringBuilder resultInfo = new StringBuilder();
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<ProfessionalsTypeForm> list = importExcel.getDataList(ProfessionalsTypeForm.class, null);
        List<ProfessionalsTypeDO> importList = new ArrayList<>();
        String temp;
        // 日志记录导入的从业人员
        StringBuilder msg = new StringBuilder();
        int num = 0;
        String reg = "^[a-zA-Z0-9\u4e00-\u9fa5-_]+$";
        for (int i = 0; i < list.size(); i++) {
            ProfessionalsTypeForm typeList = list.get(i);
            ProfessionalsTypeDO professionalsType =
                newProfessionalsDao.findProfessionalsTypeByType(typeList.getProfessionalstype());
            // 校验必填字段
            if (typeList.getProfessionalstype() == null || typeList.getProfessionalstype() == "") {
                resultMap.put("flag", 0);
                errorMsg.append("第").append((i + 1)).append("条数据必填字段未填<br/>");
                continue;
            }
            if (typeList.getProfessionalstype().length() >= 20 || typeList.getProfessionalstype().length() < 2) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append((i + 1)).append("条数据岗位类型长度不属于2-20位范围<br/>");
                continue;
            }
            if (!typeList.getProfessionalstype().matches(reg)) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append((i + 1)).append("条数据岗位类型不合法<br/>");
                continue;
            }
            // 从业人员名称重复
            if (professionalsType != null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append((i + 1)).append("条数据岗位类型已存在<br/>");
                continue;
            }
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).getProfessionalstype().equals(typeList.getProfessionalstype())) {
                    temp = list.get(i).getProfessionalstype();
                    errorMsg.append("第").append((i + 1 + num)).append("行跟第").append("行重复，值是：").append(temp)
                        .append("<br/>");
                    num++;
                    list.remove(j);
                }
            }
            // 创建者
            typeList.setCreateDataUsername(SystemHelper.getCurrentUsername());
            // 创建时间
            typeList.setCreateDataTime(new Date());
            ProfessionalsTypeDO professionalsTypeDO = new ProfessionalsTypeDO(typeList);
            importList.add(professionalsTypeDO);
            msg.append("导入岗位类型 : ").append(typeList.getProfessionalstype()).append(" <br/>");
        }

        // 组装导入结果
        if (importList.size() > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = newProfessionalsDao.addProfessionalsTypeByBatch(importList);
            if (flag) {
                resultInfo.append("导入成功").append(importList.size()).append("条数据,导入失败")
                    .append((list.size() - importList.size())).append("条数据。");
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
                if (!"".equals(msg.toString())) {
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "导入岗位类型");
                }
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }

        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public boolean generateTemplateType(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头

        headList.add("岗位类型");
        headList.add("类型描述");
        // 必填字段
        requiredList.add("岗位类型");
        // 默认设置一条数据
        exportList.add("技术人员");
        exportList.add("主要负责互联网技术方面的工作人员");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
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

    @Override
    public Set<String> getRedisGroupProfessionalId(String groupId) {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户的下级组织id
        List<String> orgList = userService.getOrgUuidsByUser(userId);
        // 如果是查询当个组织
        if (StrUtil.isNotBlank(groupId) && orgList.contains(groupId)) {
            orgList.clear();
            orgList.add(groupId);
        }
        //通过redis获取该组织下面的所有从业人员id
        List<RedisKey> keys = RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.ofs(orgList);
        return RedisHelper.batchGetSet(keys);
    }

    @Override
    public List<String> getGroupProfessional() {
        List<String> cvs = RedisHelper.getList(RedisKeyEnum.PROFESSIONAL_SORT_ID.of());
        Set<String> groupDevice = getRedisGroupProfessionalId(null);
        // 筛选权限数据，并排序
        List<String> sortGroupProfessional = new ArrayList<>(cvs.size());
        if (CollectionUtils.isEmpty(cvs)) {
            return sortGroupProfessional;
        }
        for (String did : cvs) {
            if (groupDevice.contains(did)) {
                sortGroupProfessional.add(did);
            }
        }
        return sortGroupProfessional;
    }

    /**
     * 根据岗位类型查询数据库中是否存在相同岗位类型
     */
    @Override
    public ProfessionalsTypeDO findTypeByType(String postType) throws Exception {
        return newProfessionalsDao.findProfessionalsTypeByType(postType);
    }

    @Override
    public String getIcCardDriverIdByIdentityAndName(String cardIdAndName) {
        String[] cardIdAndNameArr = cardIdAndName.split("_");
        return newProfessionalsDao.getIcCardDriverIdByIdentityAndName(cardIdAndNameArr[0], cardIdAndNameArr[1]);
    }

}
