package com.cb.platform.service.impl;

import com.cb.platform.domain.ItemNameEntity;
import com.cb.platform.domain.ItemNameExportEntity;
import com.cb.platform.domain.ItemNameQuery;
import com.cb.platform.repository.mysqlDao.ItemNameDao;
import com.cb.platform.repository.mysqlDao.TransportTimesDao;
import com.cb.platform.service.ItemNameService;
import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ItemNameServiceImpl implements ItemNameService {

    @Autowired
    private ItemNameDao itemNameDao;

    @Autowired
    private TransportTimesDao transportTimesDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public boolean addItemName(ItemNameEntity itemNameEntity, String ipAddress) {
        itemNameEntity.setId(UUID.randomUUID().toString());
        itemNameEntity.setCreateDataTime(new Date());
        itemNameEntity.setCreateDataUsername(SystemHelper.getCurrentUsername());
        itemNameEntity.setFlag(1);
        try {
            logSearchServiceImpl.addLog(ipAddress, "新增品名：" + itemNameEntity.getName(), "3", "", "新增品名");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemNameDao.addItemName(itemNameEntity);
    }

    /**
     * 获取不分页品名
     * @return
     */
    @Override
    public List<ItemNameEntity> findList() {
        return itemNameDao.findList();
    }

    /**
     * 分页查询
     * @param query
     * @return
     * @throws Exception
     */
    @Override
    public Page<ItemNameEntity> searchItemName(ItemNameQuery query) throws Exception {
        return PageHelperUtil.doSelect(query, () -> itemNameDao.searchItemName(query));
    }

    @Override
    public boolean updateItemName(ItemNameEntity itemNameEntity, String ipAddress) {
        itemNameEntity.setUpdateDataTime(new Date());
        itemNameEntity.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        try {
            logSearchServiceImpl.addLog(ipAddress, "修改品名：" + itemNameEntity.getName(), "3", "", "修改品名");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemNameDao.updateItemName(itemNameEntity);
    }

    @Override
    public boolean deleteItemName(List<String> list, String ipAddress) {
        boolean flag = false;
        try {
            List<ItemNameEntity> result = itemNameDao.findByIdList(list);
            flag = itemNameDao.deleteItemName(list);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.size(); i++) {
                if (i == result.size() - 1) {
                    sb.append(result.get(i).getName());
                } else {
                    sb.append(result.get(i).getName() + ",");
                }
            }
            logSearchServiceImpl.addLog(ipAddress, "批量删除品名", "3", "", "批量删除品名");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean deleteById(ItemNameEntity itemNameEntity, String ipAddress) throws Exception {
        List<String> list = new ArrayList<>(1);
        list.add(itemNameEntity.getId());
        boolean flag = itemNameDao.deleteItemName(list);
        if (flag) {
            logSearchServiceImpl.addLog(ipAddress, "删除品名：" + itemNameEntity.getName(), "3", "", "删除品名");
        }
        return flag;
    }

    /**
     * 根据名称获取不分页列表
     * @param name
     * @return
     */
    @Override
    public List<ItemNameEntity> findByName(String name) {

        return itemNameDao.findByName(name);
    }

    @Override
    public ItemNameEntity findById(String id) throws Exception {
        return itemNameDao.findById(id);
    }

    /**
     * 根据ID获取品名
     * @param list
     * @return
     * @throws Exception
     */
    @Override
    public List<ItemNameEntity> findByIdList(List<String> list) throws Exception {
        return itemNameDao.findByIdList(list);
    }

    /**
     * 查询危险品类别
     * @return
     */
    @Override
    public List<Map<String, Object>> selectType() {
        return itemNameDao.selectType();
    }

    /**
     * 导入
     * @param file
     * @param ipAddress
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> importItemName(MultipartFile file, String ipAddress) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        Row row = importExcel.getRow(0);
        String string = importExcel.getCellValue(row, 0).toString();
        StringBuilder errorMsgBuilder = new StringBuilder();
        StringBuilder message = new StringBuilder();
        if (string.contains("品名")) {
            //将excel转成list
            List<ItemNameExportEntity> list = importExcel.getDataList(ItemNameExportEntity.class, null);
            List<ItemNameEntity> importList = new ArrayList<>(list.size());
            //校验品名
            List<ItemNameEntity> checkList = itemNameDao.findList();
            //校验危险品类别
            List<Map<String, Object>> typeList = itemNameDao.selectType();

            //校验上传的品名不能重复
            if (list.size() > 0) {
                Set<String> set = new HashSet<String>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    ItemNameExportEntity itemNameExportEntity = list.get(i);
                    if (StringUtils.isBlank(itemNameExportEntity.getName())) {
                        continue;
                    }
                    boolean flag = set.add(itemNameExportEntity.getName());
                    if (!flag) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据品名重复<br/>");
                        continue;
                    }
                }
            }

            //校验
            for (int i = 0; i < list.size(); i++) {
                ItemNameExportEntity itemNameExportEntity = list.get(i);
                if (StringUtils.isBlank(itemNameExportEntity.getName())) {
                    resultMap.put("flag", 0);
                    errorMsgBuilder.append("第").append(i + 1).append("条数据品名不能为空<br/>");
                    continue;
                }
                //校验单位
                if (StringUtils.isNotBlank(itemNameExportEntity.getUnit())) {
                    if (!"L".equalsIgnoreCase(itemNameExportEntity.getUnit().toLowerCase()) && !"KG"
                        .equalsIgnoreCase(itemNameExportEntity.getUnit().toLowerCase())) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据单位只能为kg或L<br/>");
                        continue;
                    }
                }
                /**
                 * 校验品名不能有重复
                 */
                boolean nameflag = false;
                for (ItemNameEntity item : checkList) {
                    if (itemNameExportEntity.getName().equals(item.getName())) {
                        nameflag = true;
                        break;
                    }
                }
                if (nameflag) {
                    resultMap.put("flag", 0);
                    errorMsgBuilder.append("第").append(i + 1).append("条数据品名不能重复<br/>");
                    continue;
                }
                if (itemNameExportEntity.getName().length() > 20) {
                    resultMap.put("flag", 0);
                    errorMsgBuilder.append("第").append(i + 1).append("条数据品名长度不能大于20<br/>");
                    continue;
                }
                //校验危险品类别
                if (StringUtils.isNotBlank(itemNameExportEntity.getDangerType())) {
                    if (typeList.size() > 0) {
                        boolean flag = false;
                        for (Map<String, Object> typemap : typeList) {
                            if (itemNameExportEntity.getDangerType().equals(typemap.get("value").toString())) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            resultMap.put("flag", 0);
                            errorMsgBuilder.append("第").append(i + 1).append("条数据危险品类别不存在<br/>");
                            continue;
                        }
                    } else {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据危险品类别不存在<br/>");
                        continue;
                    }
                }
                if (StringUtils.isNotBlank(itemNameExportEntity.getRemark())) {
                    if (itemNameExportEntity.getRemark().length() > 50) {
                        resultMap.put("flag", 0);
                        errorMsgBuilder.append("第").append(i + 1).append("条数据备注的长度不能大于50<br/>");
                        continue;
                    }
                }

                ItemNameEntity entity = exportToEntity(itemNameExportEntity, typeList);
                importList.add(entity);
            }

            if (importList.size() > 0) {
                //添加数据
                boolean flag = itemNameDao.insertList(importList);
                if (flag) {
                    resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                    resultMap.put("flag", 1);
                    resultMap.put("errorMsg", errorMsgBuilder.toString());
                    resultMap.put("resultInfo", resultInfo);
                    for (int i = 0; i < importList.size(); i++) {
                        if (i == importList.size() - 1) {
                            message.append(importList.get(i).getName());
                        } else {
                            message.append(importList.get(i).getName() + ",");
                        }
                    }
                    logSearchServiceImpl.addLog(ipAddress, "导入品名", "3", "", "导入品名管理");

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
            resultMap.put("resultInfo", "品名管理模板不正确！");
            return resultMap;
        }

        return resultMap;
    }

    /**
     * 导出
     * @param title
     * @param type
     * @param res
     * @param name
     * @return
     * @throws IOException
     */
    @Override
    public boolean export(String title, int type, HttpServletResponse res, String name, String ipAddress)
        throws IOException {
        List<ItemNameExportEntity> list = itemNameDao.findExportByName(name);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                sb.append(list.get(i).getName());
            } else {
                sb.append(list.get(i).getName() + ",");
            }
        }
        try {
            logSearchServiceImpl.addLog(ipAddress, "导出品名", "3", "", "导出品名");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, list, ItemNameExportEntity.class, null, res.getOutputStream()));
    }

    /**
     * 导出模板
     * @param title
     * @param type
     * @param response
     * @return
     * @throws IOException
     */
    @Override
    public boolean download(String title, int type, HttpServletResponse response) throws IOException {

        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 表头
        headList.add("品名");
        headList.add("危险品类别");
        headList.add("单位");
        headList.add("备注");
        //必填
        requiredList.add("品名");

        exportList.add("测试品名");
        exportList.add("危险货物5类1项");
        exportList.add("kg");
        exportList.add("备注");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(1);
        String[] unit = { "kg", "L" };
        selectMap.put("单位", unit);
        //获取字典表中的危险品类别
        List<String> dangerTypeList = transportTimesDao.findDangerTypeList();
        String[] dangerType = new String[dangerTypeList.size()];
        for (int i = 0; i < dangerTypeList.size(); i++) {
            dangerType[i] = dangerTypeList.get(i);
        }
        selectMap.put("危险品类别", dangerType);
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

    private ItemNameEntity exportToEntity(ItemNameExportEntity entity, List<Map<String, Object>> typeList) {
        ItemNameEntity itemNameEntity = new ItemNameEntity();
        itemNameEntity.setId(UUID.randomUUID().toString());
        itemNameEntity.setCreateDataUsername(SystemHelper.getCurrentUsername());
        itemNameEntity.setCreateDataTime(new Date());
        itemNameEntity.setName(entity.getName());
        itemNameEntity.setRemark(entity.getRemark());
        itemNameEntity.setFlag(1);
        if (StringUtils.isNotBlank(entity.getDangerType())) {
            for (Map<String, Object> typemap : typeList) {
                if (entity.getDangerType().equals(typemap.get("value").toString())) {
                    itemNameEntity.setDangerType(Integer.parseInt(typemap.get("code").toString()));
                }
            }
        }
        if (StringUtils.isNotBlank(entity.getUnit())) {
            if ("KG".equalsIgnoreCase(entity.getUnit().toLowerCase())) {
                itemNameEntity.setUnit(1);
            } else {
                itemNameEntity.setUnit(2);
            }
        }
        return itemNameEntity;
    }

}
