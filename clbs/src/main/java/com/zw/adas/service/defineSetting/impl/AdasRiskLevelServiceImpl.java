package com.zw.adas.service.defineSetting.impl;

import com.zw.adas.domain.riskManagement.bean.AdasRiskLevelFromBean;
import com.zw.adas.domain.riskManagement.query.AdasRiskLevelQuery;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskLevelDao;
import com.zw.adas.service.defineSetting.AdasRiskLevelService;
import com.zw.platform.commons.SystemHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by PengFeng on 2017/8/16  16:05
 */
@Service
public class AdasRiskLevelServiceImpl implements AdasRiskLevelService {
    @Autowired
    private AdasRiskLevelDao adasRiskLevelDao;

    @Override
    public List<Map<String, String>> getRiskLevel(AdasRiskLevelQuery query) {
        return adasRiskLevelDao.findRiskLevel(query);
    }

    @Override
    public void deleteLevels(String[] levelIds) {
        adasRiskLevelDao.deleteRiskLevel(Arrays.asList(levelIds));
    }

    @Override
    public void addRiskLevel(AdasRiskLevelFromBean bean) {
        bean.setCreateDataUsername(SystemHelper.getCurrentUsername());
        adasRiskLevelDao.insertRiskLevel(bean);
    }

    @Override
    public void updateRiskLevel(AdasRiskLevelFromBean bean) {
        bean.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        adasRiskLevelDao.updateRiskLevel(bean);
    }

    @Override
    public boolean isNotExsit(AdasRiskLevelFromBean bean) {
        return adasRiskLevelDao.isNotExist(bean) == 0;
    }

    @Override
    public List<String> getAllLevelName() {
        return adasRiskLevelDao.getAllLevelName();
    }
}
