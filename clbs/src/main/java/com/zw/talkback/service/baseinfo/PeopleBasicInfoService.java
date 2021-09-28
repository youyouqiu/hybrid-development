package com.zw.talkback.service.baseinfo;

import com.zw.talkback.domain.basicinfo.BasicInfo;
import com.zw.talkback.domain.basicinfo.PeopleBasicInfo;

import java.util.List;

public interface PeopleBasicInfoService {

    /**
     * 获取驾照类别
     * @return
     */
    List<BasicInfo> getAllDriverType();

    /**
     * 获取资格证
     * @return
     */
    List<BasicInfo> getAllQualification();

    /**
     * 获取血型
     * @return
     */
    List<BasicInfo> getAllBloodType();

    /**
     * 获取民族
     * @return
     */
    List<BasicInfo> getAllNation();

    /**
     * 文化程度（学历）
     * @return
     */
    List<BasicInfo> getAllEducation();

    boolean addPeopleBasicInfo(List<PeopleBasicInfo> list);
}
