package com.zw.platform.controller.common;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.AssignmentImportForm;
import com.zw.platform.domain.basicinfo.form.DeviceImportForm;
import com.zw.platform.domain.basicinfo.form.PersonnelImportForm;
import com.zw.platform.domain.basicinfo.form.ProfessionalsImportForm;
import com.zw.platform.domain.basicinfo.form.SimcardImportForm;
import com.zw.platform.domain.basicinfo.form.ThingInfoImportForm;
import com.zw.platform.domain.basicinfo.form.VehicleForm;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.service.commonimport.CommonImportService;
import com.zw.platform.util.Translator;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ImportErrorUtil;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.dto.ProgressDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 导入控制器
 *
 * @author Zhang Yanhui
 * @since 2020/9/15 15:43
 */

@Slf4j
@Controller
@Api(tags = "通用导入API")
@RequestMapping("m/import")
public class ImportController {

    @Autowired
    private CommonImportService commonImportService;

    private static final Translator<ImportModule, ExcelInfo> ERROR_INFO = Translator
            .<ImportModule, ExcelInfo>builder()
            .add(ImportModule.CONFIG, new ExcelInfo("信息配置导入错误信息", ConfigImportForm.class))
            .add(ImportModule.VEHICLE, new ExcelInfo("车辆导入错误信息", VehicleForm.class))
            .add(ImportModule.PEOPLE, new ExcelInfo("人员导入错误信息", PersonnelImportForm.class))
            .add(ImportModule.THING, new ExcelInfo("物品导入错误信息", ThingInfoImportForm.class))
            .add(ImportModule.DEVICE, new ExcelInfo("终端导入错误信息", DeviceImportForm.class))
            .add(ImportModule.SIM_CARD, new ExcelInfo("SIM卡导入错误信息", SimcardImportForm.class))
            .add(ImportModule.ASSIGNMENT, new ExcelInfo("分组导入错误信息", AssignmentImportForm.class))
            .add(ImportModule.PROFESSIONAL, new ExcelInfo("从业人员导入错误信息", ProfessionalsImportForm.class))
            .build();

    /**
     * 查询导入进度
     */
    @RequestMapping(value = "{module}/progress", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getImportProgress(
            @PathVariable("module") @ApiParam(value = "模块", required = true) ImportModule module) {
        try {
            List<ProgressDTO> result = commonImportService.getImportProgress(module, SystemHelper.getCurrentUsername());
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("查询导入进度异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "{module}/error", method = RequestMethod.GET)
    public void exportDeviceError(
            @PathVariable("module") @ApiParam(value = "模块", required = true) ImportModule module,
            HttpServletResponse response) {
        try {
            final ExcelInfo excelInfo = ERROR_INFO.b2p(module);
            final Class<?> excelClass = excelInfo.getExcelClass();
            final String fileName = excelInfo.getFileName();
            ImportErrorUtil.generateErrorExcel(module, fileName, null, response);
        } catch (Exception e) {
            log.error("导出终端错误信息异常", e);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExcelInfo {
        private String fileName;
        private Class<?> excelClass;
    }
}
