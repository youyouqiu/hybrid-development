package com.zw.adas.domain.equipmentrepair.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 终端设备报修列表查询条件
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceRepairQuery extends BaseQueryBean {
    /**
     * 组织ID
     */
    @NotNull(message = "【组织】不能为空")
    private String orgId;

    /**
     * 报修开始时间
     */
    @NotNull(message = "【报修开始时间】不能为空")
    private Date reportStartTime;

    /**
     * 报修结束时间
     */
    @NotNull(message = "【报修结束时间】不能为空")
    private Date reportEndTime;

    /**
     * 故障类型
     * -1或空:全部
     * 0:主存储器异常
     * 1:备用存储器异常
     * 2:卫星信号异常
     * 3:通信信息号异常
     * 4:备用电池欠压
     * 5:备用电池失效
     * 6:IC卡从业资格证模块故障
     */
    private Integer faultType;

    /**
     * 故障处理状态 空：全部 -1：全部 0:未确认 1:已确认 2:已完成 3:误报
     */
    private Integer handleStatus;

    /**
     * 维修开始时间
     */
    private Date repairStartTime;

    /**
     * 维修结束时间
     */
    private Date repairEndTime;

    /**
     * 模块名称
     */
    private static final String MODULE = "设备报修";

    public OfflineExportInfo getOffLineExport() {
        Map<String, String> param = getQueryCondition(false);
        String fileName = MODULE + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo offlineExport = OfflineExportInfo.getInstance(MODULE, fileName + ".xls");
        offlineExport.assembleCondition(new TreeMap<>(param), OffLineExportBusinessId.EQUIPMENT_REPAIR);
        return offlineExport;
    }

    public Map<String, String> getQueryCondition(boolean isPage) {
        Map<String, String> param = new HashMap<>(16);
        param.put("organizationId", this.orgId);
        param.put("startTime", DateUtil.formatDate(this.reportStartTime, DateFormatKey.YYYYMMDDHHMMSS));
        param.put("endTime", DateUtil.formatDate(this.reportEndTime, DateFormatKey.YYYYMMDDHHMMSS));
        if (Objects.nonNull(this.repairStartTime) && Objects.nonNull(this.repairEndTime)) {
            param.put("repairStartTime", DateUtil.formatDate(this.repairStartTime, DateFormatKey.YYYYMMDD));
            param.put("repairEndTime", DateUtil.formatDate(this.repairEndTime, DateFormatKey.YYYYMMDD));
        }
        if (Objects.nonNull(this.faultType) && !Objects.equals(-1, this.faultType)) {
            param.put("malfunctionType", String.valueOf(this.faultType));
        }
        if (Objects.nonNull(this.handleStatus) && !Objects.equals(-1, this.handleStatus)) {
            param.put("malfunctionHandleStatus", String.valueOf(this.handleStatus));
        }
        if (isPage) {
            param.put("page", String.valueOf(this.getPage()));
            param.put("pageSize", String.valueOf(this.getLength()));
        }
        if (StringUtils.isNotBlank(this.getSimpleQueryParam())) {
            param.put("fuzzyQueryParam", this.getSimpleQueryParam());
        }
        return param;
    }
}
