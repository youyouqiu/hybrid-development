package com.zw.platform.repository.core;

import com.zw.platform.domain.vas.carbonmgt.TimingStored;

import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */
public interface TimingStoredDao {

  void add(String id,String province,String type,String price,String time);

  List<TimingStored> list (String time);
}
