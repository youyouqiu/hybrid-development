package com.zw.talkback.service.baseinfo.impl;

import com.zw.talkback.domain.basicinfo.BasicInfo;
import com.zw.talkback.domain.basicinfo.PeopleBasicInfo;
import com.zw.talkback.repository.mysql.PeopleBasicInfoDao;
import com.zw.talkback.service.baseinfo.PeopleBasicInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeopleBasicInfoServiceImpl implements PeopleBasicInfoService {

    @Autowired
    private PeopleBasicInfoDao peopleBasicInfoDao;

    @Override
    public List<BasicInfo> getAllDriverType() {
        return peopleBasicInfoDao.getAllDriverType();
    }

    @Override
    public List<BasicInfo> getAllQualification() {
        return peopleBasicInfoDao.getAllQualification();
    }

    @Override
    public List<BasicInfo> getAllBloodType() {
        return peopleBasicInfoDao.getAllBloodType();
    }

    @Override
    public List<BasicInfo> getAllNation() {
        return peopleBasicInfoDao.getAllNation();
    }

    /**
     * 文化程度（学历）
     * @return
     */
    @Override
    public List<BasicInfo> getAllEducation() {
        return peopleBasicInfoDao.getAllEducation();
    }

    @Override
    public boolean addPeopleBasicInfo(List<PeopleBasicInfo> list) {
        return peopleBasicInfoDao.addPeopleBasicInfo(list);
    }
}
