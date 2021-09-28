package com.cb.platform.service.impl;

import com.cb.platform.domain.ItemNameEntity;
import com.cb.platform.domain.TransportTimesEntity;
import com.cb.platform.domain.TransportTimesExportEntity;
import com.cb.platform.domain.TransportTimesImportEntity;
import com.cb.platform.domain.TransportTimesQuery;
import com.cb.platform.repository.mysqlDao.ItemNameDao;
import com.cb.platform.repository.mysqlDao.TransportTimesDao;
import com.cb.platform.service.TransportTimesService;
import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.event.VehicleDeleteEvent;
import com.zw.platform.basic.event.VehicleUpdateEvent;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class TransportTimesServiceImpl implements TransportTimesService {

    @Autowired
    private TransportTimesDao transportTimesDao;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private ItemNameDao itemNameDao;
    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;
    @Autowired
    private NewProfessionalsDao newProfessionalsDao;
    @Autowired
    private ProfessionalsService professionalsService;
    @Autowired
    private UserService userService;

    /**
     * 添加危险货物运输趟次
     */
    @Override
    public boolean addTransport(TransportTimesEntity transportTimesEntity) {
        String ipAddress = getIpAddress();
        transportTimesEntity.setId(UUID.randomUUID().toString());
        transportTimesEntity.setCreateDataTime(new Date());
        transportTimesEntity.setCreateDataUsername(userService.getCurrentUserInfo().getUsername());
        transportTimesEntity.setFlag(1);
        try {
            List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("危险品运输车");
            String brand = null;
            for (MonitorBaseDTO baseDTO : vehicleList) {
                if (transportTimesEntity.getVehicleId().equals(baseDTO.getId())) {
                    brand = baseDTO.getName() == null ? "" : baseDTO.getName();
                    break;
                }
            }
            logSearchServiceImpl.addLog(ipAddress, "监控对象：" + brand + " 新增危货运输信息", "3", "", "新增危货运输信息");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transportTimesDao.addTransport(transportTimesEntity);
    }

    /**
     * 根据id查询危险货物运输趟次
     */
    @Override
    public TransportTimesEntity findById(String id) {
        return transportTimesDao.findById(id);
    }

    @Override
    public List<TransportTimesEntity> findByVids(List<String> list) {
        return transportTimesDao.findByVehiclesId(list);
    }

    /**
     * 根据车牌号查询危险货物运输趟次
     */
    @Override
    public Page<TransportTimesEntity> searchTransport(TransportTimesQuery query) {
        //获取权限内的车辆ID
        List<String> sortAssignVehicle = vehicleService.getUserOwnIds(null, null);
        if (sortAssignVehicle == null || sortAssignVehicle.size() == 0) {
            return new Page<>();
        }

        Page<TransportTimesEntity> list =
            PageHelperUtil.doSelect(query, () -> transportTimesDao.searchTransport(query, sortAssignVehicle));
        //查询所有的从业人员id
        List<String> sortGroupProfessional = professionalsService.getGroupProfessional();
        if (sortGroupProfessional.size() > 0) {
            //查询所属权限内的从业人员
            List<ProfessionalDO> professionalsList = newProfessionalsDao.getByIds(sortGroupProfessional);
            //数据库查出来的是押运人员的ID,根据ID转成人的名称
            for (TransportTimesEntity transportTimesEntity : list) {
                if (StringUtils.isNotBlank(transportTimesEntity.getProfessinoalId())) {
                    //如果查不到押运员则为false,押运员ID清空
                    boolean flag = false;
                    for (ProfessionalDO professionalsInfo : professionalsList) {
                        if (transportTimesEntity.getProfessinoalId().equals(professionalsInfo.getId())) {
                            transportTimesEntity.setProfessinoalId(professionalsInfo.getName());
                            transportTimesEntity.setProfessinoalNumber(professionalsInfo.getCardNumber());
                            transportTimesEntity.setPhone(professionalsInfo.getPhone());
                            flag = true;
                        }
                    }
                    if (!flag) {
                        //权限内查不出就为空,有押运人员修改后可能不在权限内或者被删除,那就为空
                        transportTimesEntity.setProfessinoalNumber(null);
                        transportTimesEntity.setProfessinoalId(null);
                        transportTimesEntity.setPhone(null);
                    }
                }
            }
        } else {
            for (TransportTimesEntity entity : list) {
                entity.setProfessinoalNumber(null);
                entity.setProfessinoalId(null);
                entity.setPhone(null);
            }
        }
        return list;
    }

    /**
     * 修改危险货物运输趟次
     */
    @Override
    public boolean updateTransport(TransportTimesEntity entity, String ipAddress) {
        try {
            List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("危险品运输车");
            String brand = null;
            for (MonitorBaseDTO baseDTO : vehicleList) {
                if (entity.getVehicleId().equals(baseDTO.getId())) {
                    brand = baseDTO.getName();
                    break;
                }
            }
            logSearchServiceImpl.addLog(ipAddress, "" + brand + " 修改危货运输信息", "3", "", "修改危货运输信息");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transportTimesDao.updateTransport(entity);
    }

    /**
     * 删除危险货物运输趟次
     */
    @Override
    public boolean deleteTransport(List<String> list, String ipAddress) {
        boolean result;
        try {
            result = transportTimesDao.deleteTransport(list);
            logSearchServiceImpl.addLog(ipAddress, "批量删除危货运输信息", "3", "", "批量删除危货运输信息");
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    @Override
    public boolean deleteTransportById(TransportTimesEntity entity, String ipAddress) {
        List<String> list = new ArrayList<>(1);
        list.add(entity.getId());
        boolean flag = transportTimesDao.deleteTransport(list);
        if (flag) {
            List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("危险品运输车");
            String brand = null;
            for (MonitorBaseDTO baseDTO : vehicleList) {
                if (entity.getVehicleId().equals(baseDTO.getId())) {
                    brand = baseDTO.getName();
                    break;
                }
            }
            logSearchServiceImpl.addLog(ipAddress, "监控对象：" + brand + "删除危货运输信息", "3", "", "删除危货运输信息");
        }
        return flag;
    }

    /**
     * 批量添加危险货物运输趟次
     */
    @Override
    public boolean insertList(List<TransportTimesEntity> list) {
        return transportTimesDao.insertList(list);
    }

    @Override
    public boolean deleteByVehicleId(String vehicleId, String ip, String brand) {
        Integer dnum = transportTimesDao.deleteByVehicleId(vehicleId);
        return dnum > 0;
    }

    @EventListener
    public void listenVehicleDeleteEvent(VehicleDeleteEvent event) {
        transportTimesDao.deleteTransportByVid(event.getIds());
    }

    @Override
    public List<String> findByItemName(List<String> list) {
        return transportTimesDao.findByItemName(list);
    }

    /**
     * 导入趟次管理
     */
    @Override
    public Map<String, Object> importTransport(MultipartFile file) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        resultMap.get("");
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        Row row = importExcel.getRow(0);
        String string = importExcel.getCellValue(row, 0).toString();
        StringBuilder errorMsgBuilder = new StringBuilder();
        if (string.contains("车牌号")) {
            //将excel转成list
            List<TransportTimesImportEntity> list = importExcel.getDataList(TransportTimesImportEntity.class);
            List<TransportTimesEntity> importList = new ArrayList<>(list.size());
            //查询所有的从业人员id
            List<String> sortGroupProfessional = professionalsService.getGroupProfessional();
            //查询所属权限内的从业人员
            List<ProfessionalDO> professionalsInfos = new ArrayList<>();
            if (sortGroupProfessional != null && sortGroupProfessional.size() > 0) {
                professionalsInfos = newProfessionalsDao.getByIds(sortGroupProfessional);
            }
            //数据不能为空
            if (list.size() > 0) {
                //查询车辆类别为 危险品运输车 的车辆
                List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("危险品运输车");
                if (vehicleList.size() == 0) {
                    resultMap.put("flag", 0);
                    resultMap.put("errorMsg", errorMsgBuilder.toString());
                    resultMap.put("resultInfo", "您权限内暂无车辆类别为 危险品运输车 的车辆！");
                    return resultMap;
                }
                //查询品名，如果品名在数据库里面没有，则直接返回
                List<ItemNameEntity> itemList = itemNameDao.findList();
                if (itemList.size() == 0) {
                    resultMap.put("flag", 0);
                    resultMap.put("errorMsg", errorMsgBuilder.toString());
                    resultMap.put("resultInfo", "系统暂无品名,请先添加品名在上传");
                    return resultMap;
                }
                //遍历传过来的数据
                for (int i = 0, size = list.size(); i < size; i++) {
                    TransportTimesImportEntity transportTimesImportEntity = list.get(i);
                    if (StringUtils.isBlank(transportTimesImportEntity.getBrand())) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                        continue;
                    }
                    //获取车辆ID
                    String brandID = getVehicleId(transportTimesImportEntity.getBrand(), vehicleList);
                    if (null == brandID) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据权限内暂无车辆类别为 危险品运输车 的车辆<br/>");
                        continue;
                    }
                    //品名不能为空
                    if (StringUtils.isBlank(transportTimesImportEntity.getName())) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                        continue;
                    }
                    String itemID = getItemNameId(transportTimesImportEntity.getName(), itemList);
                    if (null == itemID) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据暂无此品名<br/>");
                        continue;
                    }
                    String professionalID = null;
                    //押运员名称不为空
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getProfessinoal())) {
                        if (professionalsInfos.size() == 0) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("您权限内暂无从业人员<br/>");
                            continue;
                        }
                        boolean flag = true;
                        for (ProfessionalDO info : professionalsInfos) {
                            if (transportTimesImportEntity.getProfessinoal().equals(info.getName())) {
                                professionalID = info.getId();
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条数据从业人员不存在<br/>");
                            continue;
                        }
                    }
                    //运输类型
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getTransportType())) {
                        if (!"营运性危险货物运输".equals(transportTimesImportEntity.getTransportType())) {
                            if (!"非营运性危险货物运输".equals(transportTimesImportEntity.getTransportType())) {
                                resultMap.put("flag", 0);
                                errorMsgBuilder.append("第").append(i + 1).append("条数据运输类型不正确<br/>");
                                continue;
                            }
                        }
                    }
                    //数量范围
                    if (transportTimesImportEntity.getCount() != null) {
                        if (transportTimesImportEntity.getCount().toString().length() > 9) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条数量范围1-9位<br/>");
                            continue;
                        }
                    }
                    //起始地点
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getStartSite())) {
                        if (transportTimesImportEntity.getStartSite().length() > 20) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条起始地点数量范围1-20位<br/>");
                            continue;
                        }
                    }
                    //途径地点
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getViaSite())) {
                        if (transportTimesImportEntity.getViaSite().length() > 20) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条途径地点数量范围1-20位<br/>");
                            continue;
                        }
                    }
                    //目的地点
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getAimSite())) {
                        if (transportTimesImportEntity.getAimSite().length() > 20) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条目的地点数量范围1-20位<br/>");
                            continue;
                        }
                    }
                    //校验时间
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getTransportDate())) {
                        if (null == DateUtil.getStringToDate(transportTimesImportEntity.getTransportDate(), "")) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条运输日期格式不正确<br/>");
                            continue;
                        }
                    }
                    //备注
                    if (StringUtils.isNotBlank(transportTimesImportEntity.getRemark())) {
                        if (transportTimesImportEntity.getRemark().length() > 50) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条备注数量范围1-50位<br/>");
                            continue;
                        }
                    }
                    TransportTimesEntity transportTimesEntity =
                        exportToEntity(transportTimesImportEntity, brandID, itemID, professionalID);
                    importList.add(transportTimesEntity);
                }
                if (importList.size() > 0) {
                    //添加数据
                    boolean flag = transportTimesDao.insertList(importList);
                    if (flag) {
                        resultInfo +=
                            "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                        resultMap.put("flag", 1);
                        resultMap.put("errorMsg", errorMsgBuilder.toString());
                        resultMap.put("resultInfo", resultInfo);

                        logSearchServiceImpl.addLog(getIpAddress(), "导入危货运输信息", "3", "", "导入危货运输信息");
                    } else {
                        resultMap.put("flag", 0);
                        resultMap.put("resultInfo", "导入失败！");
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
                resultMap.put("resultInfo", "成功导入0条数据！");
                return resultMap;
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsgBuilder.toString());
            resultMap.put("resultInfo", "危险货物运输趟次管理导入模板不正确！");
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 根据车辆ID删除趟次
     */
    @Override
    public boolean deleteTransportByVid(List<String> list) {
        return transportTimesDao.deleteTransportByVid(list);
    }

    @EventListener
    public void listenVehicleUpdateEvent(VehicleUpdateEvent vehicleUpdateEvent) {
        List<VehicleDTO> oldVehicleList = vehicleUpdateEvent.getOldVehicleList();
        String categoryName = "危险品运输车";
        List<VehicleDTO> dangerousTransportList =
            oldVehicleList.stream().filter(o -> Objects.equals(o.getVehicleCategoryName(), categoryName))
                .collect(Collectors.toList());
        if (dangerousTransportList.isEmpty()) {
            return;
        }

        Map<String, VehicleDTO> newVehicleMap =
            AssembleUtil.collectionToMap(vehicleUpdateEvent.getCurVehicleList(), VehicleDTO::getId);
        List<String> vehicleIds = new ArrayList<>();
        for (VehicleDTO beforeVehicle : dangerousTransportList) {
            VehicleDTO curVehicle = newVehicleMap.get(beforeVehicle.getId());
            //若车辆类别从“危险品运输车”变更成其他的需要删除趟次信息
            if (!Objects.equals(beforeVehicle.getVehicleCategoryName(), curVehicle.getVehicleCategoryName())) {
                vehicleIds.add(curVehicle.getId());
            }
        }
        if (!vehicleIds.isEmpty()) {
            transportTimesDao.deleteTransportByVid(vehicleIds);
        }

    }

    /**
     * 导出
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse res, String brand) throws Exception {

        //获取权限内的车辆ID
        List<String> sortAssignVehicle = vehicleService.getUserOwnIds(null, null);
        String ipAddress = getIpAddress();
        //权限内0条的话直接导出0条
        if (sortAssignVehicle == null || sortAssignVehicle.size() == 0) {
            boolean flag = ExportExcelUtil.export(
                new ExportExcelParam(title, type, new ArrayList<TransportTimesExportEntity>(1),
                    TransportTimesExportEntity.class, null, res.getOutputStream()));
            if (flag) {
                logSearchServiceImpl.addLog(ipAddress, "导出危货运输信息:", "3", "", "导出危货运输信息");
            }
            return flag;
        }
        List<TransportTimesExportEntity> pis = transportTimesDao.exportTransport(brand, sortAssignVehicle);
        pis.forEach(o -> {
            if (Objects.nonNull(o.getCount())) {
                o.setCountStr(o.getCount().toString());
            }
        });

        //查询所有的从业人员id
        List<String> sortGroupProfessional = professionalsService.getGroupProfessional();
        //日志记录
        //        StringBuilder sb = new StringBuilder();
        if (sortGroupProfessional.size() > 0) {
            //查询所属权限内的从业人员
            List<ProfessionalDO> professionalsInfos = newProfessionalsDao.getByIds(sortGroupProfessional);
            //数据库查出来的是押运人员的ID,根据ID转成人的名称
            for (TransportTimesExportEntity entity : pis) {
                if (StringUtils.isNotBlank(entity.getProfessinoalId())) {
                    for (ProfessionalDO info : professionalsInfos) {
                        if (entity.getProfessinoalId().equals(info.getId())) {
                            entity.setProfessinoal(info.getName());
                            entity.setProfessinoalNumber(info.getCardNumber());
                            entity.setPhone(info.getPhone());
                        }
                    }
                } else {
                    //权限内查不出就为空,有押运人员修改后可能不在权限内或者被删除,那就为空
                    entity.setProfessinoal(null);
                    entity.setPhone(null);
                    entity.setProfessinoalNumber(null);
                }
            }
        }

        boolean flag = ExportExcelUtil.export(
            new ExportExcelParam(title, type, pis, TransportTimesExportEntity.class, null, res.getOutputStream()));
        if (flag) {
            try {
                logSearchServiceImpl.addLog(ipAddress, "导出危货运输信息", "3", "", "导出危货运输信息");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 模板导出
     */
    @Override
    public boolean download(HttpServletResponse response) throws IOException {

        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("车牌号");
        headList.add("品名");
        headList.add("数量");
        headList.add("运输类型");
        headList.add("运输日期");
        headList.add("起始地点");
        headList.add("途径地点");
        headList.add("目的地点");
        headList.add("押运员");
        headList.add("备注");

        //必填字段
        requiredList.add("车牌号");
        requiredList.add("品名");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(1);
        String[] types = { "营运性危险货物运输", "非营运性危险货物运输" };
        selectMap.put("运输类型", types);

        //组装车牌号
        List<MonitorBaseDTO> vehicleList = vehicleService.getByCategoryName("危险品运输车");
        if (vehicleList.size() > 0) {
            String[] vehicles = new String[vehicleList.size()];
            for (int i = 0; i < vehicleList.size(); i++) {
                vehicles[i] = vehicleList.get(i).getName() == null ? "" : vehicleList.get(i).getName();
            }
            selectMap.put("车牌号", vehicles);
            exportList.add(vehicles[0]);
        } else {
            exportList.add("渝BBB111");
        }

        //组装品名
        List<ItemNameEntity> itemList = itemNameDao.findList();
        if (itemList.size() > 0) {
            String[] items = new String[itemList.size()];
            for (int i = 0; i < itemList.size(); i++) {
                items[i] = itemList.get(i).getName();
            }
            selectMap.put("品名", items);
            exportList.add(items[0]);
        } else {
            exportList.add("品名");
        }
        exportList.add("1");
        exportList.add("营运性危险货物运输");
        exportList.add("2018-05-18 06:05:01");
        exportList.add("重庆");
        exportList.add("遂宁，成都");
        exportList.add("绵阳");
        //组装押运员
        //查询所有的从业人员id
        List<String> sortGroupProfessional = professionalsService.getGroupProfessional();
        if (sortGroupProfessional.size() > 0) {
            //查询所属权限内的从业人员
            List<ProfessionalDO> professionalsInfos = newProfessionalsDao.getByIds(sortGroupProfessional);
            String[] profess = new String[professionalsInfos.size()];
            for (int i = 0; i < professionalsInfos.size(); i++) {
                profess[i] = professionalsInfos.get(i).getName();
            }
            selectMap.put("押运员", profess);
            exportList.add(profess[0]);
        } else {
            exportList.add("张三");
        }
        exportList.add("备注");
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
     * 根据车牌号查询车辆ID,条件是已存在且车辆类型为危险车
     */
    public String getVehicleId(String brand, List<MonitorBaseDTO> list) {
        for (MonitorBaseDTO baseDTO : list) {
            if (brand.equals(baseDTO.getName())) {
                return baseDTO.getId();
            }
        }
        return null;
    }

    /**
     * 根据品名获取品名id
     */
    private String getItemNameId(String name, List<ItemNameEntity> itemList) {
        for (ItemNameEntity item : itemList) {
            if (name.equals(item.getName())) {
                return item.getId();
            }
        }
        return null;
    }

    /**
     * 上传的实体类转成插入数据库的实体类
     */
    private TransportTimesEntity exportToEntity(TransportTimesImportEntity entity, String vehicleId, String itemID,
        String professinoalID) {
        TransportTimesEntity transportTimesEntity = new TransportTimesEntity();
        transportTimesEntity.setId(UUID.randomUUID().toString());
        transportTimesEntity.setCreateDataTime(new Date());
        transportTimesEntity.setCreateDataUsername(SystemHelper.getCurrentUsername());
        transportTimesEntity.setVehicleId(vehicleId);
        transportTimesEntity.setItemNameId(itemID);
        transportTimesEntity.setAimSite(entity.getAimSite());
        transportTimesEntity.setCount(entity.getCount());
        transportTimesEntity.setFlag(1);
        transportTimesEntity.setRemark(entity.getRemark());
        transportTimesEntity.setStartSite(entity.getStartSite());
        transportTimesEntity.setViaSite(entity.getViaSite());
        //营运性危险货物运输1非营运性危险货物运输2
        if (StringUtils.isNotBlank(entity.getTransportType())) {
            if ("营运性危险货物运输".equals(entity.getTransportType())) {
                transportTimesEntity.setTransportType(1);
            } else {
                transportTimesEntity.setTransportType(2);
            }
        }
        String transportDate = entity.getTransportDate();
        if (StringUtils.isNotBlank(transportDate)) {
            transportTimesEntity.setTransportDate(DateUtil.getStringToDate(transportDate, ""));
        }
        transportTimesEntity.setProfessinoalId(professinoalID);
        return transportTimesEntity;
    }

}
