package com.zw.api.repository.mysql;

import java.util.List;

public interface SwaggerGroupDao {
    List<String> getGroupIdsByUserId(String userId);
}
