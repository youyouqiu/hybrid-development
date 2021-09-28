package com.zw.app.service.appOCR;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.ProfessionalDTO;

import java.util.List;
import java.util.Map;

public interface AppOcrProfessionalsService {

    List<JSONObject> getProfessionalsList(String id);

    ProfessionalDTO getProfessionalsInfo(String id);

    Map<String, String> saveProfessionalsInfo(String info, String vehicleId, String oldPhoto, Integer type)
        throws Exception;

    Map<String, String> getProfessionalsInfo(String newId, String vehicleId);
}
