package com.zw.platform.repository.vas;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.vas.f3.TransdusermonitorSet;

public interface VeerStatisticalDao {
//  List<Positional> getWinchInfo();
  List<TransdusermonitorSet> getVehiceInfo(@Param("userId") String userId,@Param("groupId") String groupId);
}
