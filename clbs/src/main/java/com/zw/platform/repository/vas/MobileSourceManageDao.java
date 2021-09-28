package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceManageForm;

import java.util.List;

/**
 * Created by Administrator on 2017/2/23.
 */
public interface MobileSourceManageDao {

    boolean addMobile(MobileSourceManageForm mobileSourceManageform);

    String find(String band,String startTime,String endTime);

    List<MobileSourceManageForm> findList(String brand);

    boolean del(String band,String startTime,String endTime);
}
