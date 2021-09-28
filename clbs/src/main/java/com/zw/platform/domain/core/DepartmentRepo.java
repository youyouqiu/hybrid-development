package com.zw.platform.domain.core;

import java.util.List;
import java.util.Map;

public interface DepartmentRepo {
    Map<String, List<String>> getDepartmentMap();
}
