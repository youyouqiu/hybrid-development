package com.zw.platform.domain.vas.switching;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;


/**
 * <p> Title:开关信号管理 <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 13:46
 */
@Data
public class SwitchingSignal extends BaseFormBean {

    private String vehicleId;// 车辆ID

    private String brand;// 车牌号

    private String vehicleType;// 车辆类型

    private String groups;// 车辆分组

    private String signalZero;// 信号位0

    private String signalZeroName;// 信号位0

    private Integer zeroType;// 1常开,2常关

    private String zeroId; // 功能ID 唯一

    private String signalOne;// 信号位1

    private String signalOneName;// 信号位

    private Integer oneType;// 1常开,2常关

    private String oneId; // 功能ID 唯一

    private String signalTwo; // 信号位2

    private String signalTwoName; // 信号位

    private Integer twoType;// 1常开,2常关

    private String twoId; // 功能ID 唯一

    private String signalThree;// 信号位3

    private String signalThreeName;// 信号位

    private Integer threeType;// 1常开,2常关

    private String threeId; // 功能ID 唯一

    private String setingId;// 当前设置ID，用于判定是否已设置

    private Integer monitorType;//对象类型

    /**
     * 信号位0： one:状态1; two: 状态2
     */
    private String zeroStateOne;
    private String zeroStateTwo;

    private String oneStateOne;
    private String oneStateTwo;

    private String twoStateOne;
    private String twoStateTwo;

    private String threeStateOne;
    private String threeStateTwo;

    private String fourStateOne;
    private String fourStateTwo;

    private String fiveStateOne;
    private String fiveStateTwo;

    private String sixStateOne;
    private String sixStateTwo;

    private String sevenStateOne;
    private String sevenStateTwo;

    private String eightStateOne;
    private String eightStateTwo;

    private String nineStateOne;
    private String nineStateTwo;

    private String tenStateOne;
    private String tenStateTwo;

    private String elevenStateOne;
    private String elevenStateTwo;

    private String twelveStateOne;
    private String twelveStateTwo;

    private String thirteenStateOne;
    private String thirteenStateTwo;

    private String fourteenStateOne;
    private String fourteenStateTwo;

    private String fifteenStateOne;
    private String fifteenStateTwo;

    private String sixteenStateOne;
    private String sixteenStateTwo;

    private String seventeenStateOne;
    private String seventeenStateTwo;

    private String eighteenStateOne;
    private String eighteenStateTwo;

    private String nineteenStateOne;
    private String nineteenStateTwo;

    private String twentyStateOne;
    private String twentyStateTwo;

    private String twentyOneStateOne;
    private String twentyOneStateTwo;

    private String twentyTwoStateOne;
    private String twentyTwoStateTwo;

    private String twentyThreeStateOne;
    private String twentyThreeStateTwo;

    private String twentyFourStateOne;
    private String twentyFourStateTwo;

    private String twentyFiveStateOne;
    private String twentyFiveStateTwo;

    private String twentySixStateOne;
    private String twentySixStateTwo;

    private String twentySevenStateOne;
    private String twentySevenStateTwo;

    private String twentyEightStateOne;
    private String twentyEightStateTwo;

    private String twentyNineStateOne;
    private String twentyNineStateTwo;

    private String thirtyStateOne;
    private String thirtyStateTwo;

    private String thirtyOneStateOne;
    private String thirtyOneStateTwo;
}
