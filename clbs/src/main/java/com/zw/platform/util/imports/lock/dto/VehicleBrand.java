package com.zw.platform.util.imports.lock.dto;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author create by zhouzongbo on 2020/9/8.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleBrand extends ImportErrorData {
    private static final long serialVersionUID = 2457118907066580462L;
    private String brand;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
