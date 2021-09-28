package com.zw.adas.domain.riskManagement.query;

import com.zw.adas.domain.riskManagement.bean.AdasOrgShowQueryData;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.privilege.OrgShowUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class AdasOrgShowQuery<T> {
    private String module;

    private String groupId;

    private List<T> result;

    LocalDateTime dateTime;

    private boolean isToday;

    private AdasOrgShowQueryData orgShowQueryData;

    public static AdasOrgShowQuery parseOrgShowQuery(String groupId, boolean isToday, String module,
        AdasOrgShowQueryData orgShowQueryData) {
        AdasOrgShowQuery orgShowQuery = new AdasOrgShowQuery();
        orgShowQuery.setModule(module);
        orgShowQuery.setGroupId(groupId);
        orgShowQuery.setToday(isToday);
        orgShowQuery.setOrgShowQueryData(orgShowQueryData);
        return orgShowQuery;
    }

    public long getTimeKey() {
        return OrgShowUtils.getValToHour(dateTime, isToday);
    }

    public long getDayKey() {
        return Date8Utils.getValToDay(dateTime);
    }

    public List<T> queryAndSetResult() {
        result = orgShowQueryData.queryData(groupId, dateTime, isToday);
        return result;
    }

}
