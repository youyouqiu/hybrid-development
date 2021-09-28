package com.zw.platform.functional;

import java.time.LocalDateTime;
import java.util.List;


@FunctionalInterface
public interface OrgShowQueryData<T> {
    List<T> queryData(String groupId, LocalDateTime dateTime, boolean isToday);
}
