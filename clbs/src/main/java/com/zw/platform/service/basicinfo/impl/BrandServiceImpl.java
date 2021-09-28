package com.zw.platform.service.basicinfo.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.BrandInfo;
import com.zw.platform.domain.basicinfo.BrandModelsInfo;
import com.zw.platform.domain.basicinfo.form.BrandForm;
import com.zw.platform.domain.basicinfo.form.BrandModelsForm;
import com.zw.platform.domain.basicinfo.query.BrandModelsQuery;
import com.zw.platform.domain.basicinfo.query.BrandQuery;
import com.zw.platform.repository.modules.BrandDao;
import com.zw.platform.service.basicinfo.BrandService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    private static final Logger log = LogManager.getLogger(BrandServiceImpl.class);

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private UserService userService;

    @Override
    @MethodLog(name = "添加品牌", description = "添加品牌")
    public boolean addBrand(BrandForm brandForm) throws Exception {
        boolean flag;
        brandForm.setCreateDataUsername(userService.getCurrentUserInfo().getUsername());
        flag = brandDao.addBrand(brandForm);
        if (flag) {
            logSearchServiceImpl.addLog(getIpAddress(), "新增品牌:" + brandForm.getBrandName(), "3", "", "-", "");
        }
        return flag;
    }

    @Override
    @MethodLog(name = "添加品牌机型", description = "添加品牌机型")
    public boolean addBrandModels(BrandModelsForm brandModelsForm) throws Exception {
        boolean flag;
        brandModelsForm.setCreateDataUsername(userService.getCurrentUserInfo().getUsername());
        flag = brandDao.addBrandModels(brandModelsForm);
        if (flag) {
            logSearchServiceImpl.addLog(getIpAddress(), "新增品牌机型:" + brandModelsForm.getModelName(), "3", "", "-", "");
        }
        return flag;
    }

    @Override
    public BrandInfo findBrandByName(String name) throws Exception {
        if (StringUtils.isNotBlank(name)) {
            return brandDao.findBrandByName(name);
        }
        return null;
    }

    @Override
    public List<BrandModelsInfo> findBrandModelsByName(String name) throws Exception {
        if (StringUtils.isNotBlank(name)) {
            return brandDao.findBrandModelsByName(name);
        }
        return null;
    }

    @Override
    public BrandInfo getBrand(String id) throws Exception {
        return brandDao.getBrand(id);
    }

    @Override
    public BrandModelsInfo getBrandModels(String id) throws Exception {
        return brandDao.getBrandModels(id);
    }

    @Override
    @MethodLog(name = "修改品牌", description = "修改品牌")
    public JsonResultBean updateBrand(BrandForm form) throws Exception {
        //修改前的品牌信息
        BrandInfo brandInfo = getBrand(form.getId());
        form.setUpdateDataUsername(userService.getCurrentUserInfo().getUsername());
        boolean flag = brandDao.updateBrand(form);
        if (flag) {
            String msg;
            // 如果修改前的品牌名称和修改后的品牌名称相同,说明并没有修改过品牌名称字段,修改的是其他
            if (brandInfo.getBrandName().equals(form.getBrandName())) {
                msg = "修改品牌：" + form.getBrandName();
            } else {
                msg = "修改品牌名称：" + brandInfo.getBrandName() + " 修改为：" + form.getBrandName();
            }
            logSearchServiceImpl.addLog(getIpAddress(), msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    @MethodLog(name = "修改机型", description = "修改机型")
    public JsonResultBean updateBrandModels(BrandModelsForm form) throws Exception {
        //修改前的机型信息
        BrandModelsInfo brandModelsInfo = getBrandModels(form.getId());
        if (brandModelsInfo != null) {
            //修改前的品牌id
            String brandIdAfter = brandModelsInfo.getBrandId();
            //现在的品牌id
            String brandIdNow = form.getBrandId();
            if (brandIdAfter != null && (!brandIdAfter.equals(brandIdNow))) {
                return new JsonResultBean(JsonResultBean.FAULT, "品牌不能修改");
            }
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        form.setUpdateDataUsername(userService.getCurrentUserInfo().getUsername());
        boolean flag = brandDao.updateBrandModels(form);
        if (flag) {
            String msg;
            // 如果修改前的机型名称和修改后的机型名称相同,说明并没有修改过机型牌名称字段,修改的是其他
            if (brandModelsInfo.getModelName().equals(form.getModelName())) {
                msg = "修改机型：" + form.getModelName();
            } else {
                msg = "修改机型名称：" + brandModelsInfo.getModelName() + " 修改为：" + form.getModelName();
            }
            logSearchServiceImpl.addLog(getIpAddress(), msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public int getIsBandModel(String id) throws Exception {
        return brandDao.getIsBandModel(id);
    }

    @Override
    public int getIsBandModelByBatch(List<String> ids) throws Exception {
        if (ids != null && !ids.isEmpty()) {
            return brandDao.getIsBandModelByBatch(ids);
        }
        return 0;
    }

    @Override
    @MethodLog(name = "删除品牌", description = "删除品牌")
    public JsonResultBean deleteBrandById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            BrandInfo brandInfo = brandDao.getBrand(id);
            if (brandInfo != null) {
                brandDao.deleteBrandById(id);
                String msg = "删除品牌:" + brandInfo.getBrandName();
                logSearchServiceImpl.addLog(getIpAddress(), msg, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    @MethodLog(name = "批量删除品牌", description = "批量删除品牌")
    public JsonResultBean deleteBrandByBatch(List<String> ids) throws Exception {
        if (ids != null && !ids.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (String id : ids) {
                BrandInfo brand = brandDao.getBrand(id);
                if (brand != null) {
                    msg.append("删除品牌 : ").append(brand.getBrandName()).append("<br/>");
                }
            }
            boolean flag = brandDao.deleteBrandByBatch(ids);
            if (flag) { // 记录日志
                logSearchServiceImpl.addLog(getIpAddress(), msg.toString(), "3", "batch", "批量删除品牌");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    @MethodLog(name = "删除机型", description = "删除机型")
    public JsonResultBean deleteBrandModelsById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            BrandModelsInfo brandModelsInfo = brandDao.getBrandModels(id);
            if (brandModelsInfo != null) {
                brandDao.deleteBrandModelsById(id);
                String msg = "删除机型:" + brandModelsInfo.getModelName();
                logSearchServiceImpl.addLog(getIpAddress(), msg, "3", "", "-", "");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    @MethodLog(name = "批量删除机型", description = "批量删除机型")
    public JsonResultBean deleteBrandModelsByBatch(List<String> ids) throws Exception {
        if (ids != null && !ids.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (String id : ids) {
                BrandModelsInfo models = brandDao.getBrandModels(id);
                if (models != null) {
                    msg.append("删除机型 : ").append(models.getModelName()).append("<br/>");
                }
            }
            boolean flag = brandDao.deleteBrandModelsByBatch(ids);
            if (flag) { // 记录日志
                logSearchServiceImpl.addLog(getIpAddress(), msg.toString(), "3", "batch", "批量删除机型");
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Page<BrandInfo> findBrandByPage(BrandQuery query) throws Exception {
        if (query != null && query.getBrandName() != null) {
            query.setBrandName(StringUtil.mysqlLikeWildcardTranslation(query.getBrandName()));
        }
        return brandDao.findBrand(query);
    }

    @Override
    public Page<BrandModelsInfo> findBrandModelsByPage(BrandModelsQuery query) throws Exception {
        if (query != null && query.getSimpleQueryParam() != null) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        return brandDao.findBrandModels(query);
    }

    @Override
    @MethodLog(name = "导出品牌", description = "导出品牌")
    public boolean exportBrand(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, BrandForm.class, 1, null);
        List<BrandForm> exportList = brandDao.findBrandExport();
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    @MethodLog(name = "导出机型", description = "导出机型")
    public boolean exportBrandModels(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, BrandModelsForm.class, 1, null);
        List<BrandModelsForm> exportList = brandDao.findBrandModelsExport();
        export.setDataList(exportList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 品牌模板
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public boolean generateTemplateBrand(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("品牌(必填)");
        headList.add("备注");
        // 必填字段
        requiredList.add("品牌(必填)");
        // 默认设置一条数据
        exportList.add("大众");
        exportList.add("大众备注");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出模板文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 机型模板
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public boolean generateTemplateBrandModels(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        List<BrandForm> brandForms = brandDao.findBrandExport();
        // 表头
        headList.add("品牌(必选)");
        headList.add("机型(必填)");
        headList.add("备注");
        // 必填字段
        requiredList.add("品牌(必选)");
        requiredList.add("机型(必填)");
        // 默认设置一条数据
        if (brandForms != null && brandForms.size() > 0) {
            exportList.add(brandForms.get(0).getBrandName());
        } else {
            exportList.add("先添加品牌,否则机型会导入失败!");
        }
        exportList.add("机型名称");
        exportList.add("机型备注");
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        if (brandForms != null && brandForms.size() > 0) {
            String[] brands = new String[brandForms.size()];
            for (int i = 0; i < brandForms.size(); i++) {
                brands[i] = brandForms.get(i).getBrandName();
            }
            selectMap.put("品牌(必选)", brands);
        }
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出模板文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    @MethodLog(name = "批量导入品牌", description = "批量导入品牌")
    public Map<String, Object> importBrand(MultipartFile multipartFile) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //用于检验文件中品牌名称是否重复
        Map<String, Integer> checkRepeat = new HashMap<String, Integer>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<BrandForm> list = importExcel.getDataList(BrandForm.class, null);
        List<BrandForm> importList = new ArrayList<BrandForm>();
        String username = userService.getCurrentUserInfo().getUsername();
        // 日志记录导入品牌
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            BrandForm brandForm = list.get(i);
            //品牌名称
            String brandName = brandForm.getBrandName();
            if (brandName != null && (!"".equals(brandName)) && brandName.length() > 32) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据,品牌长度错误,应小等于32<br/>");
                continue;
            }
            //检验文件中品牌名称是否重复
            if (checkRepeat != null && checkRepeat.size() > 0 && checkRepeat.containsKey(brandForm.getBrandName())) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("行跟第")
                        .append(checkRepeat.get(brandForm.getBrandName()) + 1)
                        .append("行品牌重复，值是：").append(brandForm.getBrandName()).append("<br/>");
                continue;
            }
            // 校验必填字段
            if (brandName == null || "".equals(brandName)) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                continue;
            }
            BrandInfo brandInfo = brandDao.findBrandByName(brandForm.getBrandName());
            // 品牌名称重复
            if (brandInfo != null) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据品牌已存在<br/>");
                continue;
            }
            // 创建者
            brandForm.setCreateDataUsername(username);
            // 创建时间
            brandForm.setCreateDataTime(new Date());
            importList.add(brandForm);
            checkRepeat.put(brandForm.getBrandName(), i);
            msg.append("导入品牌 : ").append(brandForm.getBrandName()).append(" <br/>");
        }
        // 组装导入结果
        if (importList.size() > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = brandDao.addBrandMore(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg.toString());
                resultMap.put("resultInfo", resultInfo);
                if (!"".contentEquals(msg)) {
                    logSearchServiceImpl.addLog(getIpAddress(), msg.toString(), "3", "batch", "导入品牌");
                }
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg.toString());
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }
        return resultMap;
    }

    @Override
    @MethodLog(name = "批量导入机型", description = "批量导入机型")
    public Map<String, Object> importBrandModels(MultipartFile multipartFile) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //用于检验文件中机型名称是否重复
        Map<String, Integer> checkRepeat = new HashMap<String, Integer>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<BrandModelsForm> list = importExcel.getDataList(BrandModelsForm.class, null);
        List<BrandModelsForm> importList = new ArrayList<BrandModelsForm>();
        // 日志记录导入品牌
        StringBuilder msg = new StringBuilder();
        String username = userService.getCurrentUserInfo().getUsername();
        for (int i = 0; i < list.size(); i++) {
            BrandModelsForm brandModelsForm = list.get(i);
            String modelName = brandModelsForm.getModelName();
            String brandName = brandModelsForm.getBrandName();
            if (modelName != null && (!"".equals(modelName)) && modelName.length() > 32) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据,机型长度错误,应小等于32<br/>");
                continue;
            }
            //检验文件中机型名称是否重复
            if (checkRepeat.size() > 0 && checkRepeat.containsKey(brandName + "," + modelName)) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("行跟第")
                        .append(checkRepeat.get(brandName + "," + modelName) + 1)
                        .append("行品牌机型重复，值是：").append(brandName).append(",").append(modelName).append("<br/>");
                continue;
            }
            // 校验必填字段
            if (modelName == null || "".equals(modelName)) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据 机型(必填) 字段未填<br/>");
                continue;
            }
            // 校验必选字段
            if (brandName == null || "".equals(brandName)) {
                resultMap.put("flag", 0);
                errorMsg.append("第").append(i + 1).append("条数据 品牌(必选) 字段未选<br/>");
                continue;
            } else {
                BrandInfo brandInfo = brandDao.findBrandByName(brandName);
                if (brandInfo != null) {
                    brandModelsForm.setBrandId(brandInfo.getId());
                } else {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据,数据库不存在该品牌名称或者已经被删除,请先添加或者下载最新模板<br/>");
                    continue;
                }
            }
            BrandInfo brandInfo = brandDao.findBrandByName(brandName);
            List<BrandModelsInfo> brandModelsInfo = brandDao.findBrandModelsByName(modelName);
            // 机型名称重复
            if (brandModelsInfo != null && brandModelsInfo.size() > 0) {
                boolean isb = true;
                for (BrandModelsInfo modelsInfo : brandModelsInfo) {
                    if (modelsInfo.getBrandId().equals(brandInfo.getId())) {
                        isb = false;
                        break;
                    }
                }
                if (!isb) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据,机型已存在<br/>");
                    continue;
                }
            }

            // 创建者
            brandModelsForm.setCreateDataUsername(username);
            // 创建时间
            brandModelsForm.setCreateDataTime(new Date());
            importList.add(brandModelsForm);
            checkRepeat.put(brandName + "," + modelName, i);
            msg.append("导入机型 : ").append(brandModelsForm.getModelName()).append(" <br/>");
        }
        // 组装导入结果
        if (importList.size() > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = brandDao.addBrandModelsMore(importList);
            if (flag) {
                resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg.toString());
                resultMap.put("resultInfo", resultInfo);
                if (!"".contentEquals(msg)) {
                    logSearchServiceImpl.addLog(getIpAddress(), msg.toString(), "3", "batch", "导入机型");
                }
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg.toString());
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }
        return resultMap;
    }


    @Override
    public List<BrandModelsInfo> findBrandModelsByBrandId(String id) {
        return brandDao.findBrandModelsByBrandId(id);
    }
}
