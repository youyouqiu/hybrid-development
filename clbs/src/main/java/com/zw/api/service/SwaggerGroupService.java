package com.zw.api.service;

import java.util.List;

public interface SwaggerGroupService {
    List<String> getGroupIdsByUserId(String orgUuid);
}
