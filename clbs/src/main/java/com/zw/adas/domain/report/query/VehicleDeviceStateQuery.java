package com.zw.adas.domain.report.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TreeMap;

/**
 * 车辆与终端运行状态查询条件
 *
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VehicleDeviceStateQuery extends BaseQueryBean {
    /**
     * 组织Id
     */
    @NotEmpty(message = "【组织】不能为空！")
    private String orgId;

    @NotNull(message = "【时间】不能为空！")
    private Date date;

    /**
     * 模块名称
     */
    private static final String MODULE = "车辆与终端运行状态";

    public OfflineExportInfo getOffLineExportMsg(TreeMap<String, String> queryCondition) {
        String fileName = MODULE + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo offlineExport = OfflineExportInfo.getInstance(MODULE, fileName + ".xls");
        offlineExport.assembleCondition(queryCondition, OffLineExportBusinessId.VEHICLE_DEVICE_STATE);
        return offlineExport;
    }
}
