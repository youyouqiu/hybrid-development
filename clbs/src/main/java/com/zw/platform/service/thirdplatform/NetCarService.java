package com.zw.platform.service.thirdplatform;

import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.intercomplatform.IntercomPlatFormConfigQuery;

import java.util.List;

public interface NetCarService {
    List<T809ForwardConfig> list(IntercomPlatFormConfigQuery query);

    void add(String vehicleIds, String platformId);

    boolean delete(String vehicleIds);
}
