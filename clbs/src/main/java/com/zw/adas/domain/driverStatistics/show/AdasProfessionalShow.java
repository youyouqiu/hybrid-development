package com.zw.adas.domain.driverStatistics.show;

import com.zw.platform.basic.domain.ProfessionalShowDTO;
import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.DateUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/7/16 17:28
 @Description 实时监控页面从业人员展现数据
 @version 1.0
 **/
@Data
public class AdasProfessionalShow implements ConverterDateUtil {
    /**
     * id
     */
    private String id;
    /**
     * 从业人员名称
     */
    private String name;
    /**
     * 企业名称
     */
    private String groupName;
    /**
     * 岗位类型
     */
    private String type;

    /**
     * 从业资格证号
     */
    private String cardNumber;
    /**
     * 从业资格证类别
     */
    private String qualificationCategory;

    /**
     * 发证机关
     */
    private String icCardAgencies;
    /**
     * 有效期
     */

    private transient Date icCardEndDate;

    /**
     * 企业id
     */
    private transient String groupId;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 为1时候代表是ic卡绑定的从业人员
     */
    private Integer lockType;

    private String icCardEndDateStr;

    /**
     * 住址
     */
    private String address;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 身份证号
     */
    private String identity;
    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;

    public static AdasProfessionalShow getData(ProfessionalShowDTO professionalShowDTO) {
        AdasProfessionalShow data = new AdasProfessionalShow();
        BeanUtils.copyProperties(professionalShowDTO, data);
        data.groupId = professionalShowDTO.getOrgId();
        data.groupName = professionalShowDTO.getOrgName();
        return data;
    }

    public static Map<String, AdasProfessionalShow> convertProfessionalMaps(
        Map<String, ProfessionalShowDTO> professionalShowMap) {
        Map<String, AdasProfessionalShow> driverInfoMap = new HashMap<>();
        for (Map.Entry<String, ProfessionalShowDTO> entry : professionalShowMap.entrySet()) {
            driverInfoMap.put(entry.getKey(), AdasProfessionalShow.getData(entry.getValue()));
        }
        return driverInfoMap;

    }

    public void assembleData() {
        if (icCardEndDate != null) {
            icCardEndDateStr = DateUtil.getDateToString(icCardEndDate, "yyyy-MM-dd");
        }
    }

}
