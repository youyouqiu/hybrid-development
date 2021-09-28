package com.zw.platform.domain.reportManagement.query;

import java.time.LocalDateTime;
import java.util.TreeMap;

import javax.validation.constraints.NotNull;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 离线位移日报表
 * @date 2020/10/2010:49
 */
@Data
public class OfflineDisplacementQuery  extends BaseQueryBean {

    /**
     * 	监控对象id(多个以逗号隔开)
     */
    @NotNull(message = "监控对象不能为空")
    private String monitorIds;
    /**
     * 日期(格式:yyyyMMdd)
     */
    @NotNull(message = "日期不能为空")
    private String date;
    /**
     * 移动距离(单位:米, 默认50000米)
     */
    private int moveDistance = 50000;
    /**
     * 离线时长(单位:秒,默认1800秒)
     */
    private long offlineTime = 180;

    private static final String  OFFLINE_DISPLACEMENT_MODULE = "离线位移日报表";

    public OfflineExportInfo getOfflineExportInfo() {
        //默认名称
        String fileName = OFFLINE_DISPLACEMENT_MODULE + Date8Utils.getValToDay(LocalDateTime.now());
        OfflineExportInfo info = OfflineExportInfo.getInstance(OFFLINE_DISPLACEMENT_MODULE, fileName + ".xls");
        TreeMap<String, String> param = new TreeMap<>();
        param.put("monitorIds", this.monitorIds);
        param.put("date", this.date);
        param.put("moveDistance", String.valueOf(this.moveDistance));
        param.put("offlineTime", String.valueOf(this.offlineTime));
        param.put("flag", date);
        info.assembleCondition(param, OffLineExportBusinessId.OFFLINE_DISPLACEMENT);
        return info;
    }
}
