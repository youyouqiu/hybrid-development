package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import com.zw.platform.domain.basicinfo.enums.PlateColor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/10/14 15:38
 */
@Data
public class OilPlatData {

    /**
     * 对接码
     */
    private String passcode;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 省编码
     */
    private String sheng;
    /**
     * 市编码
     */
    private String shi;
    /**
     * 县编码
     */
    private String xian;
    /**
     * 1 验证成功并返回数据
     * 0 验证成功但是没有查询到数据
     * -1 验证失败
     */
    private String result;
    private List<Buses> buses;

    public List<OilForwardVehicleForm> getResultData(OilDownloadUrlInfo urlInfo) {
        List<OilForwardVehicleForm> resultData = new ArrayList<>();
        for (Buses bus : buses) {
            OilForwardVehicleForm data = new OilForwardVehicleForm();
            data.setProvinceCode(sheng);
            data.setCityCode(shi);
            data.setCountyCode(xian);

            data.setBrand(bus.getCphm());
            if ("其它".equals(bus.getCpys())) {
                bus.setCpys("其他");
            }
            data.setPlateColor(PlateColor.getCodeByName(bus.getCpys()));

            data.setOrgCode(bus.getDwid());
            data.setVehicleCode(bus.getClid());
            data.setFrameNumber(bus.getCjh());
            data.setIndustryCategory(bus.getHylb());
            data.setVehicleStatus(bus.getClzt());
            //设置平台相关信息
            data.setDockingCodeOrgId(urlInfo.getDockingCodeOrgId());
            data.setDockingCode(urlInfo.getDockingCode());
            data.setForwardingPlatformId(urlInfo.getForwardingPlatformId());
            data.setId(UUID.randomUUID().toString());
            data.setFlag(1);
            resultData.add(data);
        }
        return resultData;
    }

}
