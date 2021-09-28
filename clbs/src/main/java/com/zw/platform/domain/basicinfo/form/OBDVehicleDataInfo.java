package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

import static com.zw.platform.basic.constant.ObdEnum.DisplayName.*;

/**
 * @author penghj
 * @version 1.0
 */
@Data
@FieldNameConstants
@SuppressWarnings("checkstyle:MemberName")
public class OBDVehicleDataInfo implements Serializable {

    private static final long serialVersionUID = -8774518494257331262L;

    /**
     * 车id
     */
    private String id;

    /**
     * 时间
     */
    private Long vtime = 0L;

    private String vtimeStr;

    /**
     * {"streamSum":2,"streamList":[{"id":1607,"value":0},{"id":1608,"value":0}]}
     * 主动安全OBD原车数据(0xE5)
     */
    private String obdOriginalVehicleData;

    /**
     * {"streamSum":2,"streamList":[{"id":1607,"value":0},{"id":1608,"value":0}]}
     * 主动安全OBD原车数据(0xE5)
     */
    private String obdObj;

    /**
     * 地址
     */
    private String address;

    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象")
    private String plateNumber;

    /**
     * 定位时间(yyMMddHHmmss)
     */
    @ExcelField(title = "定位时间")
    private String gpsTime;

    /**
     * 系统接收时间(yyMMddHHmmss)
     */
    @ExcelField(title = "服务器时间")
    private String uploadtime;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = _0x0290)
    private String obdTotalMileage;

    @ExcelField(title = _0x051A)
    private String obdAccumulatedMileage;

    @ExcelField(title = _0x0512)
    private String obdTotalOilConsumption;

    @ExcelField(title = _0x0513)
    private String obdInstantOilConsumption;

    @ExcelField(title = _0x030B)
    private String obdInstrumentSpeed;

    @ExcelField(title = _0x0300)
    private String obdRotationRate;

    @ExcelField(title = _0x0293)
    private String obdOilPressure;

    @ExcelField(title = _0x01F0)
    private String obdBatteryVoltage;

    @ExcelField(title = _0x0305)
    private String obdWaterTemperature;

    @ExcelField(title = _0x0517)
    private String obdOilQuantity;

    @ExcelField(title = _0x0633)
    private String obdOilTankLevelHeight;

    @ExcelField(title = _0x0511)
    private String obdShortDistanceMileage;

    @ExcelField(title = _0x0645)
    private String obdEngineRunningTime;

    @ExcelField(title = _0x0624)
    private String obdTorque;

    @ExcelField(title = _0x0629)
    private String obdUreaLevel;

    @ExcelField(title = _0x0360)
    private String obdHandBrakeStatus;

    @ExcelField(title = _0x0647)
    private String obdHighBeamStatus;

    @ExcelField(title = _0x0648)
    private String obdDippedHeadlightStatus;

    @ExcelField(title = _0x0646)
    private String obdSmallLampStatus;

    @ExcelField(title = _0x0008)
    private String obdIndicatorLampStatus;

    @ExcelField(title = _0x02A5)
    private String obdFogLampStatus;

    @ExcelField(title = _0x0509)
    private String obdLeftTurnLampStatus;

    @ExcelField(title = _0x050A)
    private String obdRightTurnLampStatus;

    @ExcelField(title = _0x050B)
    private String obdEmergencyLampStatus;

    @ExcelField(title = _0x0180)
    private String obdLeftFrontDoorStatus;

    @ExcelField(title = _0x0188)
    private String obdRightFrontDoorStatus;

    @ExcelField(title = _0x0190)
    private String obdLeftRearDoorStatus;

    @ExcelField(title = _0x0198)
    private String obdRightRearDoorStatus;

    @ExcelField(title = _0x01E0)
    private String obdTailBoxDoorStatus;

    @ExcelField(title = _0x050C)
    private String obdFullVehicleLock;

    @ExcelField(title = _0x0181)
    private String obdLeftFrontDoorLock;

    @ExcelField(title = _0x0189)
    private String obdRightFrontDoorLock;

    @ExcelField(title = _0x0191)
    private String obdLeftRearDoorLock;

    @ExcelField(title = _0x0199)
    private String obdRightRearDoorLock;

    @ExcelField(title = _0x01B0)
    private String obdLeftFrontWindowStatus;

    @ExcelField(title = _0x01B8)
    private String obdRightFrontWindowStatus;

    @ExcelField(title = _0x01C0)
    private String obdLeftRearWindowStatus;

    @ExcelField(title = _0x01C8)
    private String obdRightRearWindowStatus;

    @ExcelField(title = _0x02A1)
    private String obdFaultSignalECM;

    @ExcelField(title = _0x0295)
    private String obdFaultSignalABS;

    @ExcelField(title = _0x029D)
    private String obdFaultSignalSRS;

    @ExcelField(title = _0x029A)
    private String obdAlarmSignalEngineOil;

    @ExcelField(title = _0x0508)
    private String obdAlarmSignalTirePressure;

    @ExcelField(title = _0x02AA)
    private String obdAlarmSignalMaintain;

    @ExcelField(title = _0x0240)
    private String obdSafetyAirBagStatus;

    @ExcelField(title = _0x0015)
    private String obdFootBrakeStatus;

    @ExcelField(title = _0x0602)
    private String obdClutchStatus;

    @ExcelField(title = _0x02C0)
    private String obdSafetyBeltStatusDriver;

    @ExcelField(title = _0x02C4)
    private String obdSafetyBeltStatusDeputyDriving;

    @ExcelField(title = _0x050E)
    private String obdACCSignal;

    @ExcelField(title = _0x0342)
    private String obdKeyStatus;

    @ExcelField(title = _0x0510)
    private String obdWiperStatus;

    @ExcelField(title = _0x0370)
    private String obdAirConditionerStatus;

    @ExcelField(title = _0x0516)
    private String obdAcceleratorPedal;

    @ExcelField(title = _0x0351)
    private String obdSteeringWheelAngleStatus;

    @ExcelField(title = _0x0623)
    private String obdEnergyType;

    @ExcelField(title = _0x0632)
    private String obdMILFaultLamp;

    @ExcelField(title = _0x040D)
    private String obdPercentageOfOil;

    @ExcelField(title = _0x0514)
    private String obdInstant100KmOilConsumption;

    @ExcelField(title = _0x040F)
    private String obdAverage100KmOilConsumption;

    @ExcelField(title = _0x0303)
    private String obdEngineIntakeTemperature;

    @ExcelField(title = _0x0373)
    private String obdAirConditioningTemperature;

    @ExcelField(title = _0x0609)
    private String obdMotorTemperature;

    @ExcelField(title = _0x0610)
    private String obdControllerTemperature;

    @ExcelField(title = _0x0628)
    private String obdTernaryCatalystTemperature;

    @ExcelField(title = _0x0636)
    private String obdEngineOilTemperature;

    @ExcelField(title = _0x0643)
    private String obdFuelTemperature;

    @ExcelField(title = _0x0644)
    private String obdSuperchargedAirTemperature;

    @ExcelField(title = _0x0639)
    private String obdSpeedByRotationalSpeedCalculation;

    @ExcelField(title = _0x041E)
    private String obdAirFlowRate;

    @ExcelField(title = _0x041F)
    private String obdIntakePressure;

    @ExcelField(title = _0x0411)
    private String obdFuelInjectionQuantity;

    @ExcelField(title = _0x0515)
    private String obdRelativePositionOfThrottlePedal;

    @ExcelField(title = _0x0350)
    private String obdSteeringWheelAngle;

    @ExcelField(title = _0x0608)
    private String obdBatteryRemainingElectricity;

    @ExcelField(title = _0x0600)
    private String obdVehicleTravelFuelConsumption;

    @ExcelField(title = _0x0603)
    private String obdNumberOfClutchesDuringTravel;

    @ExcelField(title = _0x0604)
    private String obdNumberOfFootBrakesDuringTravel;

    @ExcelField(title = _0x0605)
    private String obdNumberOfHandBrakesDuringTravel;

    @ExcelField(title = _0x0601)
    private String obdEngineLoad;

    @ExcelField(title = _0x0641)
    private String obdTorquePercentage;

    @ExcelField(title = _0x0642)
    private String obdAtmosphericPressure;

    @ExcelField(title = _0x0626)
    private String obdFrontOxygenSensorValue;

    @ExcelField(title = _0x0627)
    private String obdRearOxygenSensorValue;

    @ExcelField(title = _0x0631)
    private String obdNOxConcentrationRange;

    @ExcelField(title = _0x0700)
    private String obdVin;

    @ExcelField(title = _0xF000)
    private String obdEngineFuelFlow;

    @ExcelField(title = _0xF001)
    private String obdScrUpNoxOutput;

    @ExcelField(title = _0xF002)
    private String obdScrDownNoxOutput;

    @ExcelField(title = _0xF003)
    private String obdIntakeVolume;

    @ExcelField(title = _0xF004)
    private String obdScrInletTemperature;

    @ExcelField(title = _0xF005)
    private String obdScrOutletTemperature;

    @ExcelField(title = _0xF006)
    private String obdDpfDifferentialPressure;

    @ExcelField(title = _0xF007)
    private String obdEngineCoolantTemperature;

    @ExcelField(title = _0xF008)
    private String obdFrictionTorque;

    @ExcelField(title = _0xF009)
    private String obdEngineTorqueMode;

    @ExcelField(title = _0xF00A)
    private String obdUreaTankTemperature;

    @ExcelField(title = _0xF00B)
    private String obdActualUreaInjection;

    @ExcelField(title = _0xF00C)
    private String obdCumulativeUreaConsumption;

    @ExcelField(title = _0xF00D)
    private String obdDpfExhaustTemperature;

    @ExcelField(title = _0xF00E)
    private String obdDiagnostic;

    @ExcelField(title = _0xF00F)
    private String obdDiagnosticSupportState;

    @ExcelField(title = _0xF010)
    private String obdDiagnosticReadyState;

    @ExcelField(title = _0xF011)
    private String obdVersion;

    @ExcelField(title = _0xF012)
    private String obdCvn;

    @ExcelField(title = _0xF013)
    private String obdIupr;

    @ExcelField(title = _0xF0141)
    private String obdTroubleCodeNum;

    @ExcelField(title = _0xF014)
    private String obdTroubleCodes;

    /**
     * 报警信息
     */
    @ExcelField(title = _0xF0FF)
    private String obdAlarmInfo;
}
