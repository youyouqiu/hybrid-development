package com.zw.ws.entity.adas;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:主动安全参数(川冀标) (激烈驾驶参数设置)
 */
@Data
public class SetIntenseDriving implements T808MsgBody, AdasParamCommonMethod {
    /**
     * 激烈驾驶报警使能
     */
    private Integer alarmEnable = 0;

    /**
     * 急加速报警时间阈值
     */
    private Integer speedUpTime = 0xFFFF;

    /**
     * 急加速重力速度阈值
     */
    private Integer speedUpGravity = 0xFFFF;

    /**
     * 预留1
     */
    private byte[] keep1 = new byte[2];

    /**
     * 急减速报警时间阈值
     */
    private Integer speedDownTime = 0xFFFF;

    /**
     * 急减速重力速度阈值
     */
    private Integer speedDownGravity = 0xFFFF;

    /**
     * 预留2
     */
    private byte[] keep2 = new byte[2];

    /**
     * 急转弯报警时间阈值
     */
    private Integer swerveTime = 0xFFFF;

    /**
     * 急转弯重力速度阈值
     */
    private Integer swerveGravity = 0xFFFF;

    /**
     * 预留3
     */
    private byte[] keep3 = new byte[2];

    /**
     * 怠速报警时间阈值
     */
    private Integer idlingTime = 0xFFFF;

    /**
     * 怠速车速阈值
     */
    private Integer idlingSpeed = 0xFFFF;

    /**
     * 怠速发动机阈值
     */
    private Integer idlingEngine = 0xFFFF;

    /**
     * 异常熄火报警时间阈值
     */
    private Integer abnormalTime = 0xFFFF;

    /**
     * 异常熄火报警车速阈值
     */
    private Integer abnormalSpeed = 0xFFFF;

    /**
     * 异常熄火报警发动机转速阈值
     */
    private Integer abnormalEngine = 0xFFFF;

    /**
     * 空挡滑行报警时间阈值
     */
    private Integer neutralGearTime = 0xFFFF;

    /**
     * 空挡滑行报警车速阈值
     */
    private Integer neutralGearSpeed = 0xFFFF;

    /**
     * 空挡滑行发动机转速阈值
     */
    private Integer neutralGearEngine = 0xFFFF;

    /**
     * 发动机超转报警时间阈值
     */
    private Integer engineTime = 0xFFFF;

    /**
     * 发动机超转报警发动机超转报警车速阈值
     */
    private Integer engineSpeed = 0xFFFF;

    /**
     * 发动机超转报警发动机转速阈值
     */
    private Integer engineOverspeed = 0xFFFF;

    /**
     * 预留4
     */
    private byte[] keep4 = new byte[8];

    /**
     * 1264081 急加速
     * 1264082 急减速
     * 1264083 急转弯
     * 127001 怠速报警
     * 127002 异常熄火
     * 127003 空挡滑行
     * 127004 发动机超转
     */
    private static Map<String, Object> intenseAlarmEnableMap = new HashMap();

    private static Map<String, Object> intenseAlarmParamMap = new HashMap();

    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder =
            { { "1264081", "0" }, { "1264082", "1" }, { "1264083", "2" }, { "127001", "3" }, { "127002", "4" },
                { "127003", "5" }, { "127004", "6" } };
        for (String[] ints : alarmEnableOrder) {
            intenseAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] intenseAlarmParamOrder =
            { { "1264081", "speedUpTime,speedUpGravity" }, { "1264082", "speedDownTime,speedDownGravity" },
                { "1264083", "swerveTime,swerveGravity" }, { "127001", "idlingTime,idlingSpeed,idlingEngine" },
                { "127002", "abnormalTime,abnormalSpeed,abnormalEngine" },
                { "127003", "neutralGearTime,neutralGearSpeed,neutralGearEngine" },
                { "127004", "engineTime,engineSpeed,engineOverspeed" } };
        for (String[] ints : intenseAlarmParamOrder) {
            intenseAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public SetIntenseDriving(AdasParamSettingForm paramSettingForm) {
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String key = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, key);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, key);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        String[] params = intenseAlarmParamMap.get(key).toString().split(",");
        //时间阈值
        setValIfPresent(params[0], paramSetting.getTimeThreshold());
        //急加急减急转弯(只需要设置时间阈值和重力加速度阈值)
        if (params.length == 2) {
            //报警重力加速度
            setValIfPresent(params[1], StringUtils.isNotEmpty(paramSetting.getGravityAccelerationThreshold())
                ?
                Integer.parseInt(paramSetting.getGravityAccelerationThreshold()) : 0xFFFF);
        }
        //其他激烈驾驶需要设置车速阈值和发动机转速阈值（无重力加速度阈值）
        if (params.length == 3) {
            //车速阈值
            setValIfPresent(params[1], StringUtils.isNotEmpty(paramSetting.getSpeedThreshold())
                ?
                Integer.parseInt(paramSetting.getSpeedThreshold()) : 0xFFFF);
            //发动机转速阈值
            setValIfPresent(params[2], StringUtils.isNotEmpty(paramSetting.getEngineThreshold())
                ?
                Integer.parseInt(paramSetting.getEngineThreshold()) : 0xFFFF);
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getAlarmEnable(),
            Integer.parseInt(intenseAlarmEnableMap.get(key).toString()));
    }

}
