package com.zw.platform.domain.customenum;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouzongbo on 2018/9/4 11:33
 */
public enum RecordCollectionEnum {
    /**
     *
     */
    RECORD_SIGN_00H("0H", "采集记录仪执行标准版本"),
    RECORD_SIGN_01H("1H", "采集当前驾驶人信息"),
    RECORD_SIGN_02H("2H", "采集记录仪实时时间"),
    RECORD_SIGN_03H("3H", "采集累计行驶里程"),
    RECORD_SIGN_04H("4H", "采集记录仪脉冲系数"),
    RECORD_SIGN_05H("5H", "采集车辆信息"),
    RECORD_SIGN_06H("6H", "采集记录仪状态信号配置信息"),
    RECORD_SIGN_07H("7H", "采集记录仪唯一性编号"),
    RECORD_SIGN_08H("8H", "采集指定的行驶速度记录"),
    RECORD_SIGN_09H("9H", "采集指定的位置信息记录"),
    RECORD_SIGN_10H("10H", "采集指定的事故疑点记录"),
    RECORD_SIGN_11H("11H", "采集指定的超时驾驶记录"),
    RECORD_SIGN_12H("12H", "采集指定的驾驶人身份记录"),
    RECORD_SIGN_13H("13H", "采集指定的外部供电记录"),
    RECORD_SIGN_14H("14H", "采集指定的参数修改记录"),
    RECORD_SIGN_15H("15H", "采集指定的速度状态日志")
    ;

    //包含时间的采集参数
    public static final List<String> TIME_RECORDCOLLECTIONENUM_LIST =
        Lists.newArrayList("8H", "9H", "10H", "11H", "12H", "13H", "14H", "15H");
    /**
     * 命令字
     */
    private String commandSign;
    /**
     * 命令字内容
     */
    private String signContent;

    RecordCollectionEnum(String signCommand, String signContent) {
        this.commandSign = signCommand;
        this.signContent = signContent;
    }

    public String getCommandSign() {
        return commandSign;
    }

    public String getSignContent() {
        return signContent;
    }

    public static String getSignContentBy(final String signCommand) {
        RecordCollectionEnum[] values = RecordCollectionEnum.values();
        for (RecordCollectionEnum value : values) {
            if (value.getCommandSign().equals(signCommand)) {
                return value.getSignContent();
            }
        }
        return "";
    }

    public static List<String> getBeforeEightCommandSigns() {
        List<String> beforeSignEightList = new ArrayList<>();
        RecordCollectionEnum[] values = RecordCollectionEnum.values();
        for (int i = 0; i < 8; i++) {
            beforeSignEightList.add(values[i].getCommandSign());
        }
        return beforeSignEightList;
    }
}
