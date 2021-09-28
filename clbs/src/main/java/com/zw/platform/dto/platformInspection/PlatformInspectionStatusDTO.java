package com.zw.platform.dto.platformInspection;

import com.zw.platform.util.Translator;
import lombok.Data;

import static com.zw.platform.service.platformInspection.impl.PlatformInspectionServiceImpl.IDENTIFY_INSPECTION;

/***
 @Author lijie
 @Date 2020/11/20 16:06
 @Description 平台巡检下发状态
 @version 1.0
 **/
@Data
public class PlatformInspectionStatusDTO {

    private String vehicleId;

    private String brand;

    //巡检类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检 3.驾驶员身份识别巡检）
    private Integer inspectionType;

    public static final Translator<String, Integer> IDENTIFICATION_RESULT = Translator
        .of("匹配成功", 0, "匹配失败，人证不符", 1, "匹配失败，比对超时", 2, "匹配失败，无指定人脸信息", 3, "平台匹配失败", 4, "下发失败", 5,
            Translator.Pair.of("指令已下发", 6), Translator.Pair.of("终端未插卡，请插卡", 7), Translator.Pair.of("终端响应超时", 8),
            Translator.Pair.of("终端离线,未下发", 9));

    public static final Translator<String, Integer> INSPECTION_TYPE =
        Translator.of("下发中", 1, "下发成功", 2, "终端响应超时", 3, "终端离线,未下发", 4);

    /**
     * 身份识别下发状态 （0 匹配成功 1 匹配失败，人证不符 2 匹配失败，比对超时 3 匹配失败，无指定人脸信息  4 平台匹配失败
     * 5 下发失败， 6 指令已下发 7 终端未插卡，请插卡 8 终端响应超时 9 终端离线）
     * <p>
     * 0710巡检（1下发中，2下发成功，3终端响应超时，4终端离线）
     */
    private Integer inspectionStatus;

    private String inspectionStatusStr;

    public void setInspectionStatus(Integer inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
        if (this.inspectionType.equals(IDENTIFY_INSPECTION)) {
            this.inspectionStatusStr = IDENTIFICATION_RESULT.p2b(inspectionStatus);
        } else {
            this.inspectionStatusStr = INSPECTION_TYPE.p2b(inspectionStatus);
        }
    }
}
