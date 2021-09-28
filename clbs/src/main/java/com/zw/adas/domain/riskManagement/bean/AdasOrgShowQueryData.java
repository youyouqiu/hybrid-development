package com.zw.adas.domain.riskManagement.bean;

import java.time.LocalDateTime;
import java.util.List;


@FunctionalInterface
public interface AdasOrgShowQueryData<T> {
    List<T> queryData(String groupId, LocalDateTime dateTime, boolean isToday);
}
