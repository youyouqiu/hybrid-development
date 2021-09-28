package com.zw.platform.domain.param;

import com.zw.platform.domain.vas.sensorUpgrade.SensorUpgrade;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.vas.SensorUpgradeDao;
import com.zw.platform.service.sensor.RemoteUpgradeInstance;
import com.zw.platform.service.sensor.RemoteUpgradeTaskSend;
import com.zw.platform.service.sensor.RemoteUpgradeToWeb;
import com.zw.platform.service.sensor.impl.SensorRemoteUpgradeSend;
import com.zw.platform.util.RemoteUpgradeUtil;
import com.zw.platform.util.SendHelper;
import io.netty.util.Timeout;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/**
 * 远程升级Task
 * @author zhouzongbo on 2019/1/15 18:50
 */
@Data
public class RemoteUpgradeTask implements Serializable {
    private static final long serialVersionUID = -9096600542451445037L;

    private static Logger logger = LogManager.getLogger(RemoteUpgradeTask.class);
    /**
     * 最大重发次数
     */
    private static final int MAX_SEND_SIZE = 3;
    /**
     * 二进制信号量
     */
    public transient Semaphore semaphore = new Semaphore(1);
    /**
     * 任务完成标识
     */
    // private CountDownLatch countDownLatch;

    /**
     * 线程池对象
     */
    private ExecutorService executorService;
    private SendHelper sendHelper;
    private SensorUpgradeDao sensorUpgradeDao;
    /**
     * 包集合
     */
    private List<byte[]> packageList;

    /**
     * 当前所处阶段0: 平台往f3下发包; 1: 终端向外设下发;
     */
    private Integer currentStatus = 0;

    /**
     * 平台->f3:总包数
     */
    private Integer totalPackageSize = 0;

    /**
     * 完成包数
     */
    private Integer successPackageSize = 0;

    /**
     * f3 -> 外设: 总包数
     */
    private Integer f3ToPeripheralTotalPackageSize = 0;

    /**
     * f3 -> 外设: 成功包数
     */
    private Integer f3ToPeripheralSuccessPackageSize = 0;

    /**
     * 监控对象
     */
    private String monitorId;

    private String simCardNumber;

    private String plateNumber;

    /**
     ******日志添加相关需要字段*****
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 外设类型名称
     */
    private String peripheralName;

    /**
     * ip
     */
    private String ipAddress;

    /**
     * 车辆所属企业ID
     * **************************
     */
    private String orgId;

    /**
     * 终端编号
     */
    private String deviceNumber;

    private String deviceId;

    /**
     * 终止线程标识
     */
    private volatile boolean abort = false;

    /**
     * 总数据升级指令是否下发成功: true: 收到终端回复; false: 未收到终端回复
     */
    private volatile boolean totalUpgradeDataCheckInstruction = false;

    /**
     * 开始升级指令是否下发成功: true: 收到终端回复; false: 未收到终端回复
     */
    private volatile boolean startUpgradeCommand = false;

    /**
     * 校验擦出终端升级数据存储： true: 收到终端回复; false: 未收到终端回复
     */
    private volatile boolean eraseTerminal = false;

    /**
     * 外设升级是否成功: true:成功; false: 失败, 用于判断是否执行时间轮
     */
    private volatile boolean peripheralUpgradeSuccess = false;

    /**
     * 是否正在升级, 用于展示使用
     */
    private Boolean isStartUpgrade = false;

    /**
     * 0：擦出终端升级数据阶段 1：平台下发升级包阶段 2：总包数校验指令下发阶段 3:开始升级指令下发阶段
     */
    private volatile int stageStatus = 0;

    /**
     * 流水号
     */
    private Integer serialNumber;

    /**
     * 当前状态: 0: 传输中;1: 传输完成;2: 设备已经在升级中;3: 传输失败(下发包); 4: 终端离线
     */
    private Integer platformToF3Status;

    /**
     * 当前状态: 0: 进行中;1: 完成;2: 升级失败
     */
    private Integer f3ToPeripheralStatus;

    /**
     * 终端升级状态: 0: 进行中;1: 完成;2: 升级失败, 3: 终端存储区擦出失败; 4: 数据校验失败; 5: 下发升级指令失败; 6: 外设升级超时
     */
    private Integer sensorUpgradeStatus;

    /**
     * 下发参数ID,用于下发
     */
    private String paramId;

    private String parameterName;

    private String paramType = "SENSOR_UPGRADE_F8";

    /**
     * 外设ID
     */
    private Integer peripheralId;

    /**
     * 外层循环的信号量
     */
    private Semaphore superSemaphore;

    /**
     * 用户ID
     */
    private String userId;
    private String userName;
    /**
     * 终端类型
     */
    private String deviceType;

    /**
     * webSocket推送工具类
     */
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    private SensorRemoteUpgradeSend upgradeSend;

    private SensorRemoteUpgrade endSensorRemoteUpgrade;

    private Timeout timeout;

    /**
     * 终端到外设阶段: 收到终端应答收, 取消之前的超时时间轮, 并重新生成一个
     */
    public void cancelledAndCreateTimeout() {
        if (timeout == null) {
            return;
        }
        // 取消成功后, 重新创建timer
        timeout.cancel();
        timeout = UpgradeWheelTimer.getInstance().waitUpgradeSuccessOrFault(this);
    }

    /**
     * 收到f3回复的消息释放可用的凭证数
     */
    public void releaseSemaphore() {
        this.semaphore.release();
    }

    /**
     * 释放外层循环信号量
     * @param deviceId deviceId
     */
    public void releaseSuperSemaphore(String deviceId) {
        isStartUpgrade = false;
        this.superSemaphore.release();
        RemoteUpgradeInstance.getInstance().remove(userId, deviceId);
    }

    /**
     * 获取总数据校验是否成功
     */
    public boolean getTotalUpgradeDataCheckInstruction() {
        return this.totalUpgradeDataCheckInstruction;
    }

    /**
     * 释放"门闩"
     * */
    // public void releaseCountDownLatch() {
    //     this.countDownLatch.countDown();
    //     logger.info("当前的门闩数量 = " + this.countDownLatch.getCount());
    // }

    /**
     * 收到终端回复成功: 设置= true, 失败设置为 true 并且 abort = true
     */
    public void setTotalUpgradeDataCheckInstruction() {
        this.totalUpgradeDataCheckInstruction = true;
    }

    public boolean getStartUpgradeCommand() {
        return this.startUpgradeCommand;
    }

    public boolean getPeripheralUpgradeSuccess() {
        return this.peripheralUpgradeSuccess;
    }

    public boolean getEraseTerminal() {
        return this.eraseTerminal;
    }

    public RemoteUpgradeTask() {

    }

    public RemoteUpgradeTask(ExecutorService executorService, List<byte[]> packageList, Semaphore semaphore,
                             SendHelper sendHelper, SensorUpgradeDao sensorUpgradeDao) {
        this.executorService = executorService;
        this.packageList = packageList;
        this.totalPackageSize = packageList.size();
        this.superSemaphore = semaphore;
        this.sendHelper = sendHelper;
        this.sensorUpgradeDao = sensorUpgradeDao;
    }

    /**
     * 收到终端上传的消息
     * @param remoteUpgradeTaskSend remoteUpgradeTaskSend
     * @param isSuccess             isSuccess
     */
    public void onMessage(RemoteUpgradeTaskSend remoteUpgradeTaskSend, boolean isSuccess,
                          RemoteUpgradeTask remoteUpgradeTask) {
        remoteUpgradeTaskSend.onSend(simpMessagingTemplateUtil, remoteUpgradeTask);
        // 业务逻辑: 如果收到f3的回复超时or失败;
        if (!isSuccess) {
            //  TODO 这里貌似都没有用到
            timeOutOrSendFault();
        }
    }

    /**
     * 终止线程: 终端or平台
     */
    public void setAbort() {
        this.abort = true;
    }

    public boolean getAbort() {
        return this.abort;
    }

    /**
     * 当前是第几个包
     */
    private int currentPackageNum = 0;

    /**
     * 失败次数: 超时 or 下发失败
     */
    private int faultTimes = 0;

    /**
     * 数据校验超时 or 开始升级指令超时
     */
    public void addFaultTimes() {
        faultTimes++;
        this.semaphore.release();
    }

    /**
     * 超时or下发失败
     */
    public void timeOutOrSendFault() {
        this.isTimeOut = true;
        this.faultTimes++;
        this.currentPackageNum--;
        this.semaphore.release();
    }

    /**
     * 是否超时
     */
    private boolean isTimeOut = false;

    /**
     * 下发包
     * @param validationOrder validationOrder
     * @param upgradeSend     upgradeSend
     * @param sensorId        外设ID
     */
    public void sendPackage(TotalDataValidationOrder validationOrder, SensorRemoteUpgradeSend upgradeSend,
                            Integer sensorId) {
        executorService.execute(() -> {
            // this.countDownLatch = upgradeCountDownLatch;
            this.upgradeSend = upgradeSend;
            this.peripheralId = sensorId;
            this.parameterName = "SENSOR_UPGRADE_F8_" + peripheralId + monitorId;
            this.endSensorRemoteUpgrade = getEndSensorRemoteUpgrade(peripheralId);
            if (checkEraseTerminal()) {
                return;
            }

            try {
                UpgradeDataCommand upgradeDataCommand = getUpgradeDataCommand(peripheralId);
                while (currentPackageNum < packageList.size()) {
                    // 阻塞
                    this.semaphore.acquire();
                    if (this.abort) {
                        // 终止升级
                        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                        endOfTheUpgrade(serialNumber);
                        break;
                    }
                    if (stageStatus == 0) {
                        stageStatus = 1;
                        faultTimes = 0;
                    }
                    if (faultTimes >= MAX_SEND_SIZE) {
                        logger.info("REMOTE UPGRADE重新下发3次后失败");
                        // 发送结束指令
                        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                        platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_FAILED;
                        sensorUpgradeStatus = RemoteUpgradeUtil.F3_STATUS_FAILED;
                        endOfTheUpgrade(serialNumber);
                        break;
                    } else if (isTimeOut) {
                        isTimeOut = false;
                        logger.info("REMOTE UPGRADE timeOut超时");
                    }
                    if (this.abort) {
                        logger.info(deviceNumber + "REMOTE UPGRADE 终止升级.......................");
                        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                        endOfTheUpgrade(serialNumber);
                        break;
                    }
                    // 如果失败,则获取失败的包，继续下发
                    sendIssueUpgrade(upgradeSend, upgradeDataCommand);
                }

                // 如果中止或者超时了,则结束此处升级
                if (abort || faultTimes >= MAX_SEND_SIZE) {
                    return;
                }
                logger.info("REMOTE UPGRADE 升级包下发完成");
                // 3.平台下发总升级数据校验指令
                if (sendUpgrade(validationOrder, upgradeDataCommand)) {
                    return;
                }

                // 4.平台下发开始升级指令
                if (sendUpgrade(upgradeDataCommand)) {
                    // 暂时处理为, 只要接受到终端应答, 就执行下一个升级
                    //releaseSuperSemaphore();
                    return;
                }
                currentStatus = 1;
                // 等待终端回复, 如果超过1分钟未回复升级成功, 则结束此次升级
                timeout = UpgradeWheelTimer.getInstance().waitUpgradeSuccessOrFault(this);
            } catch (Exception e) {
                logger.error(String.format("REMOTE UPGRADE 监控对象: %s,远程升级下发异常", plateNumber), e);
                // endOfTheUpgrade(upgradeSend, endSensorRemoteUpgrade, serialNumber);
            } finally {
                logger.info(plateNumber + " :REMOTE UPGRADE 第一阶段下发完成, 等待终端升级完成");
            }
        });
    }

    /**
     * 下发开始升级指令
     * @param upgradeDataCommand upgradeDataCommand
     */
    private boolean sendUpgrade(UpgradeDataCommand upgradeDataCommand) throws Exception {
        // 重置失败次数
        faultTimes = 0;
        for (int i = 0; i <= 3; i++) {
            this.semaphore.acquire();
            // 开始升级指令下发阶段
            stageStatus = 3;
            if (abort) {
                // 终止升级
                serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                endOfTheUpgrade(serialNumber);
                return true;
            }

            if (i == 3) {
                // 失败次数已达三次，结束本次升级
                if (faultTimes >= MAX_SEND_SIZE && !startUpgradeCommand) {
                    serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                    platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_FAILED;
                    sensorUpgradeStatus = RemoteUpgradeUtil.SEND_UPGRADE_COMMAND_FAILED;
                    endOfTheUpgrade(serialNumber);
                    logger.info("REMOTE UPGRADE 平台下发开始升级指令失败" + faultTimes);
                    return true;
                }
            }

            if (startUpgradeCommand) {
                // 下发成功,则结束此次下发
                this.semaphore.release();
                break;
            }
            logger.info("REMOTE UPGRADE 平台下发开始升级指令");
            if (sendStartUpgrade()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 下发总升级数据校验指令
     * @param validationOrder    validationOrder
     * @param upgradeDataCommand upgradeDataCommand
     */
    private boolean sendUpgrade(TotalDataValidationOrder validationOrder, UpgradeDataCommand upgradeDataCommand)
        throws Exception {
        faultTimes = 0;
        for (int i = 0; i <= 3; i++) {
            this.semaphore.acquire();
            stageStatus = 2;
            if (abort) {
                serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                endOfTheUpgrade(serialNumber);
                return true;
            }

            if (i == 3) {
                // 失败次数已达三次，结束本次升级
                if ((faultTimes >= MAX_SEND_SIZE && !totalUpgradeDataCheckInstruction)) {
                    serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                    platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_FAILED;
                    sensorUpgradeStatus = RemoteUpgradeUtil.DATA_VALIDATION_FAILED;
                    endOfTheUpgrade(serialNumber);
                    logger.info("REMOTE UPGRADE 平台下发总升级数据校验指令失败" + faultTimes);
                    return true;
                }
            }

            if (totalUpgradeDataCheckInstruction) {
                // 下发成功,则结束此次下发
                this.semaphore.release();
                break;
            }
            logger.info("REMOTE UPGRADE 平台下发总升级数据校验指令");
            if (sendValidation(validationOrder, upgradeSend, upgradeDataCommand)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 下发开始升级指令
     * @return b
     */
    private boolean sendStartUpgrade() {
        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
        if (serialNumber != null) {
            logger.info("start upgrade serialNumber: " + serialNumber);
            // copy一份数据
            SensorRemoteUpgrade sensorRemoteUpgrade = new SensorRemoteUpgrade();
            BeanUtils.copyProperties(endSensorRemoteUpgrade, sensorRemoteUpgrade);
            sensorRemoteUpgrade.setControl(SensorRemoteUpgrade.START_PERIPHERAL_UPGRADE_COMMAND);
            upgradeSend.sendStartPerpheralUpgrade(sensorRemoteUpgrade, deviceId, simCardNumber, serialNumber);
            UpgradeWheelTimer.getInstance().addStartUpgradeCommandTask(this);
            return false;
        } else {
            platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_DEVICE_OFFLINE;
            sensorUpgradeStatus = RemoteUpgradeUtil.F3_STATUS_FAILED;
            endOfTheUpgrade(null);
            return true;
        }

    }

    /**
     * 结束下发实体
     * @param peripheralId 外设ID
     * @return SensorRemoteUpgrade
     */
    private SensorRemoteUpgrade getEndSensorRemoteUpgrade(Integer peripheralId) {
        SensorRemoteUpgrade endUpgradeFileIssue = new SensorRemoteUpgrade();
        endUpgradeFileIssue.setId(peripheralId);
        endUpgradeFileIssue.setLen(1);
        endUpgradeFileIssue.setControl(SensorRemoteUpgrade.END_UPGRADE_FILE_ISSUE);
        endUpgradeFileIssue.setDeviceType(deviceType);
        return endUpgradeFileIssue;
    }

    /**
     * 升级数据实体
     * @param peripheralId peripheralId
     * @return UpgradeDataCommand
     */
    private UpgradeDataCommand getUpgradeDataCommand(Integer peripheralId) {
        UpgradeDataCommand upgradeDataCommand = new UpgradeDataCommand();
        upgradeDataCommand.setId(peripheralId);
        upgradeDataCommand.setControl(SensorRemoteUpgrade.ISSUE_UPGRADE_DATA_COMMAND);
        upgradeDataCommand.setAllPage(totalPackageSize);
        upgradeDataCommand.setDeviceType(deviceType);
        return upgradeDataCommand;
    }

    /**
     * 平台下发总升级数据校验指令
     * @param validationOrder    validationOrder
     * @param upgradeSend        upgradeSend
     * @param upgradeDataCommand upgradeDataCommand
     * @return true: 结束下发; false: 下发成功
     */
    private boolean sendValidation(TotalDataValidationOrder validationOrder, SensorRemoteUpgradeSend upgradeSend,
                                   UpgradeDataCommand upgradeDataCommand) {
        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
        if (totalPackageSize.intValue() == successPackageSize.intValue() && serialNumber != null) {
            upgradeSend.sendTotalDataValidation(validationOrder, deviceId, simCardNumber, serialNumber);
            UpgradeWheelTimer.getInstance().addTotalUpgradeTask(this);
            return false;
        } else {
            platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_DEVICE_OFFLINE;
            sensorUpgradeStatus = RemoteUpgradeUtil.F3_STATUS_FAILED;
            endOfTheUpgrade(null);
            return true;
        }
    }

    /**
     * 结束升级
     */
    public void endOfTheUpgrade(Integer serialNumber) {
        setAbort();
        sendEndUpgrade(serialNumber);
        onMessage(new RemoteUpgradeToWeb(currentStatus, totalPackageSize, successPackageSize, monitorId), true, this);
        addOrUpdateSensorUpgrade();
        RemoteUpgradeInstance.getInstance().removeUpgradeTask(deviceId);
        releaseSuperSemaphore(deviceId);
        releaseSemaphore();
    }

    public void stopUpgrade() {
        setAbort();
        RemoteUpgradeInstance.getInstance().removeUpgradeTask(deviceId);
        addOrUpdateSensorUpgrade();
        releaseSuperSemaphore(deviceId);
        releaseSemaphore();
    }

    public void sendEndUpgrade(Integer serialNumber) {
        upgradeSend
            .sendEndUpgradeFile(endSensorRemoteUpgrade, monitorId, deviceId, simCardNumber, parameterName, paramType,
                serialNumber, paramId);
    }

    /**
     * 下发升级
     * @param upgradeSend        upgradeSend
     * @param upgradeDataCommand upgradeDataCommand
     */
    private void sendIssueUpgrade(SensorRemoteUpgradeSend upgradeSend, UpgradeDataCommand upgradeDataCommand)
        throws Exception {
        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
        if (serialNumber != null) {
            byte[] packageData = packageList.get(currentPackageNum);
            currentPackageNum++;
            // 下发逻辑： 2.下发升级数据指令
            int length = packageData.length;
            upgradeDataCommand.setLen(7 + length);
            upgradeDataCommand.setDataLen(length);
            upgradeDataCommand.setData(packageData);
            upgradeDataCommand.setSum(currentPackageNum);
            upgradeSend
                .sendIssueUpgradeData(upgradeDataCommand, monitorId, deviceId, simCardNumber, parameterName, paramType,
                    serialNumber);
            // 监听时长
            UpgradeWheelTimer.getInstance().addTask(this);
        } else {
            platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_DEVICE_OFFLINE;
            sensorUpgradeStatus = RemoteUpgradeUtil.F3_STATUS_FAILED;
            endOfTheUpgrade(null);
        }

    }

    /**
     * 校验擦出终端升级数据存储
     * @return true: 设备不在线; false: 在线
     */
    private boolean checkEraseTerminal() {
        try {
            isStartUpgrade = true;
            SensorRemoteUpgrade sensorRemoteUpgrade = new SensorRemoteUpgrade();
            sensorRemoteUpgrade.setDeviceType(deviceType);
            // 1.平台发送擦除终端升级数据存储区指令
            sensorRemoteUpgrade.setId(peripheralId);
            sensorRemoteUpgrade.setLen(1);
            sensorRemoteUpgrade.setControl(SensorRemoteUpgrade.ERASE_TERMINAL_UPGRADE_DATA);
            for (int i = 0; i <= 3; i++) {
                this.semaphore.acquire();
                if (abort) {
                    // 用户主动终止升级, 结束下发
                    serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                    endOfTheUpgrade(serialNumber);
                    return true;
                }

                if (i == 3) {
                    // 失败次数已达三次，结束本次升级
                    if ((faultTimes >= MAX_SEND_SIZE && !eraseTerminal)) {
                        serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                        platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_FAILED;
                        sensorUpgradeStatus = RemoteUpgradeUtil.TERMINAL_STORADE_ERASE_FAILED;
                        endOfTheUpgrade(serialNumber);
                        logger.info("REMOTE UPGRADE 校验擦出终端升级数据存储指令失败" + faultTimes);
                        return true;
                    }
                }

                if (eraseTerminal) {
                    // 下发成功, 释放当前信号量, 进行下一步下发
                    this.semaphore.release();
                    break;
                }

                this.serialNumber = DeviceHelper.getRegisterDevice(monitorId, "");
                if (serialNumber == null) {
                    logger.info("REMOTE UPGRADE 终端不在线");
                    // 如果设备不在线, 则该次升级失败, 并调用推送到前端, 并存一条数据在directive中，表示升级失败
                    // releaseSuperSemaphore(deviceId);
                    sendHelper.updateParameterStatus(null, 0, 9, monitorId, paramType, parameterName);
                    platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_DEVICE_OFFLINE;
                    sensorUpgradeStatus = RemoteUpgradeUtil.F3_STATUS_FAILED;
                    endOfTheUpgrade(serialNumber);
                    return true;
                }

                platformToF3Status = RemoteUpgradeUtil.PLATFORM_STATUS_UNDERWAY;
                paramId = upgradeSend
                    .sendEraseTerminalUpgradeData(sensorRemoteUpgrade, monitorId, deviceId, simCardNumber,
                        parameterName, paramType, serialNumber);
                UpgradeWheelTimer.getInstance().addEraseTerminalTask(this);
            }
        } catch (Exception e) {
            logger.error(String.format("REMOTE UPGRADE 监控对象: %s,下发平台擦出终端升级数据存储指令异常", plateNumber), e);
            return true;
        }
        return false;
    }

    /**
     * 新增传感器升级状态
     */
    public void addOrUpdateSensorUpgrade() {
        String sensorId = "0x" + Integer.toHexString(peripheralId);
        SensorUpgrade sensorUpgrade = sensorUpgradeDao.getSensorUpgradeBy(monitorId, sensorId);
        if (sensorUpgrade == null) {
            sensorUpgrade = new SensorUpgrade();
            sensorUpgrade.setVehicleId(monitorId);
            sensorUpgrade.setSensorId(sensorId);
            sensorUpgrade.setSensorUpgradeDate(new Date());
            sensorUpgrade.setSensorUpgradeStatus(sensorUpgradeStatus);
            sensorUpgrade.setCreateDataUsername(userName);
            sensorUpgrade.setDeviceType(deviceType);
            sensorUpgradeDao.addSensorUpgrade(sensorUpgrade);
        } else {
            sensorUpgrade.setSensorUpgradeDate(new Date());
            sensorUpgrade.setSensorUpgradeStatus(sensorUpgradeStatus);
            sensorUpgrade.setUpdateDataUsername(userName);
            sensorUpgradeDao.updateSensorUpgrade(sensorUpgrade);
        }
    }

    public SensorRemoteUpgrade getEndSensorRemoteUpgrade() {
        return endSensorRemoteUpgrade;
    }

    public SensorRemoteUpgradeSend getSensorRemoteUpgradeSend() {
        return upgradeSend;
    }
}