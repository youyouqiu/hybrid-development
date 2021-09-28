package com.zw.platform.repository.modules;

import com.zw.platform.domain.connectionparamsset_809.T809ForwardConfig;
import com.zw.platform.domain.forwardplatform.ForwardMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NetCarDao {
    List<T809ForwardConfig> list(@Param("userId") String userId, @Param("query") String query,
        @Param("platformId") int platformId);

    int add(@Param("vehicleList") List<String> vehicleList, @Param("creator") String creator,
        @Param("platformId") String platformId);

    int delete(@Param("vehicleList") List<ForwardMapping> vehicleList);
}
