package com.zw.platform.domain.reportManagement.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.validation.OmissionAlarmDetailCondition;
import com.zw.platform.validation.OmissionAlarmPageCondition;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * @author wanxing
 * @Title: 漏报报警查询类
 * @date 2021/1/1915:59
 */
@Data
public class OmissionAlarmQuery extends BaseQueryBean {


    private static final String  OFFLINE_DISPLACEMENT_MODULE = "报警漏报报表";

    /**
     * 企业ids
     */
    @NotEmpty(message = "组织不能为空", groups = { OmissionAlarmPageCondition.class })
    private String organizationIds;

    /**
     * 企业id
     */
    @NotEmpty(message = "组织不能为空", groups = { OmissionAlarmDetailCondition.class })
    private String organizationId;

    /**
     * 查询月份(格式:yyyyMM)
     */
    @NotEmpty(message = "月份不能为空", groups = { OmissionAlarmPageCondition.class, OmissionAlarmDetailCondition.class })
    private String month;

    /**
     * 组装离线报表实体
     * @return
     */
    public OfflineExportInfo getOfflineExportInfo() {
        //默认名称
        String fileName = OFFLINE_DISPLACEMENT_MODULE + Date8Utils.getValToDay(LocalDateTime.now());
        OfflineExportInfo info = OfflineExportInfo.getInstance(OFFLINE_DISPLACEMENT_MODULE, fileName + ".xls");
        TreeMap<String, String> param = new TreeMap<>();
        param.put("organizationIds", this.organizationIds);
        param.put("month", this.month);
        param.put("flag", CommonUtil.getFlag(Integer.parseInt(this.month)));
        info.assembleCondition(param, OffLineExportBusinessId.OMISSION_ALARM);
        return info;
    }
}
