package com.zw.platform.util.privilege;

import com.zw.platform.functional.OrgShowQueryData;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrgShowQuery<T> {
    private String module;

    private String groupId;

    private List result;

    LocalDateTime dateTime;

    private boolean isToday;

    private OrgShowQueryData<T> orgShowQueryData;

    public static <T> OrgShowQuery<T> parseOrgShowQuery(String groupId, boolean isToday, String module,
                                                 OrgShowQueryData<T> orgShowQueryData) {
        OrgShowQuery<T> orgShowQuery = new OrgShowQuery<>();
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
        return orgShowQueryData.queryData(groupId, dateTime, isToday);
    }

}
