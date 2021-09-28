package com.zw.adas.domain.riskManagement.show;

import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/3/4 10:44
 @Description 风险监管司机信息
 @version 1.0
 **/
@Data
public class AdasDriverShow {

    /**
     * 司机id
     */
    private String id;

    /**
     * 司机名称
     */
    private String driverName;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 发证机关
     */
    private String icCardAgencies;

    /**
     * 有效期至
     */
    private String icCardEndDate;

    /**
     * 司机图片
     */
    private String pic;

    /**
     * 住址：暂未启用
     */
    private String address;

    /**
     * 从业资格证类别：暂未启用
     */
    private String icCardType;

    /**
     * 是不是插卡
     */
    private Integer lockType;

    public static AdasDriverShow getInstance() {
        AdasDriverShow driverShow = new AdasDriverShow();
        driverShow.id = "6934e676-f6d1-47f7-b12f-3d2d80230931";
        driverShow.driverName = "小李";
        driverShow.cardNumber = "1256565";
        driverShow.icCardAgencies = "中位";
        driverShow.icCardEndDate = "2019-02-12";
        driverShow.pic = "http://113.204.5.58:8799/mediaserver/profesionalpic/89b24596-9400-4f92-af8f-78dfa3678b1f.png";
        driverShow.address = "";
        driverShow.icCardType = "";
        return driverShow;
    }

    public static AdasDriverShow getInstance(ProfessionalDO pro, String mediaServer) {
        AdasDriverShow driverShow = new AdasDriverShow();
        driverShow.id = pro.getId();
        driverShow.driverName = pro.getName();
        driverShow.cardNumber = pro.getCardNumber();
        driverShow.icCardAgencies = pro.getIcCardAgencies();
        driverShow.icCardEndDate = DateUtil.getDayStr(pro.getIcCardEndDate());
        if (StringUtils.isNotEmpty(pro.getPhotograph())) {
            driverShow.pic = getPicServer(mediaServer, pro.getPhotograph());
        }
        driverShow.address = pro.getAddress();
        driverShow.icCardType = pro.getQualificationCategory();
        driverShow.lockType = pro.getLockType();
        return driverShow;
    }

    public static List<AdasDriverShow> getDrivers(List<Map<String, String>> driverInfos, String mediaServer) {
        List<AdasDriverShow> result = new ArrayList<>();
        for (Map<String, String> driverInfo : driverInfos) {
            result.add(init(mediaServer, driverInfo));
        }
        return result;
    }

    private static AdasDriverShow init(String mediaServer, Map<String, String> driver) {
        AdasDriverShow driverShow = new AdasDriverShow();

        driverShow.id = driver.get("id");
        driverShow.driverName = driver.get("name");
        driverShow.cardNumber = driver.get("cardNumber");
        driverShow.icCardAgencies = driver.get("icCardAgencies");
        driverShow.icCardEndDate = driver.get("icCardEndDate");
        driverShow.pic = getPicServer(mediaServer, driver.get("photograph"));
        driverShow.address = driver.get("address");
        driverShow.icCardType = driver.get("qualificationCategory");
        driverShow.lockType = Integer.valueOf(driver.get("lockType"));
        return driverShow;
    }

    private static String getPicServer(String mediaServer, String pic) {
        return mediaServer + "/profesionalpic/" + pic;
    }

}
