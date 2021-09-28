package com.zw.platform.service.functionconfig.impl;

import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.repository.modules.AdministrationDao;
import com.zw.platform.service.functionconfig.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdministrationServiceImpl implements AdministrationService {

    @Autowired
    private AdministrationDao administrationDao;

    public Administration findAdministrationById(String id) {
        return administrationDao.findAdministrationByIds(id);
    }

    /**
     * 通过ID查询坐标点并组装，成二维数组返回给web端
     */
    public List<List<List<String>>> getAdministrationByID(String id) {
        List<Administration> list = administrationDao.getAdministrationByID(id);
        List<List<String>> strs = new ArrayList<>();
        List<List<List<String>>> strsList = new ArrayList<>();
        int a = 0;
        int b = 0;
        for (int i = 0; i < list.size(); i++) {
            a = list.get(i).getRegionCount();
            if (i == list.size() - 1) {
                List<String> lngLat = new ArrayList<>();
                lngLat.add(list.get(i).getLongitude().toString());
                lngLat.add(list.get(i).getLatitude().toString());
                strs.add(lngLat);
                strsList.add(strs);
            } else {
                if (a == b) {
                    List<String> lngLat = new ArrayList<>();
                    lngLat.add(list.get(i).getLongitude().toString());
                    lngLat.add(list.get(i).getLatitude().toString());
                    strs.add(lngLat);
                } else {
                    b = a;
                    strsList.add(strs);
                    strs = new ArrayList<>();
                    List<String> lngLat = new ArrayList<>();
                    lngLat.add(list.get(i).getLongitude().toString());
                    lngLat.add(list.get(i).getLatitude().toString());
                    strs.add(lngLat);
                }
            }

        }
        return strsList;
    }

    @Override
    public List<Administration> findAdministrationByName(String name) {

        return administrationDao.findAdministrationByName(name);
    }

}
