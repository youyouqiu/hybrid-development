package com.zw.platform.domain.param;

import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.sensor.RemoteUpgradeToWeb;
import com.zw.platform.util.RemoteUpgradeUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * 监控下发是否完成
 *
 * @author zhouzongbo on 2019/1/15 18:50
 */
public class UpgradeWheelTimer {
    private static final Logger logger = LogManager.getLogger(UpgradeWheelTimer.class);
    private final HashedWheelTimer timer = new HashedWheelTimer();
    /**
     * 秒
     */
    private static final long DELAY_TIME = 30L;
    /**
     * 终端到外设升级超时时间60s
     */
    private static final long DELAY_TIME_OUT = 60L;

    private static UpgradeWheelTimer wheelTimer;

    private UpgradeWheelTimer() {
    }

    public static synchronized UpgradeWheelTimer getInstance() {
        if (wheelTimer == null) {
            wheelTimer = new UpgradeWheelTimer();
        }
        return wheelTimer;
    }

    /**
     * 升级任务
     *
     * @param task task
     */
    public void addTask(RemoteUpgradeTask task) {
        int taskCurrentPackageNum = task.getCurrentPackageNum();
        timer.newTimeout(timeout -> {
            if (task.getCurrentPackageNum() != taskCurrentPackageNum) {
                logger.info(String.format("REMOTE UPGRADE第%d个任务已经被执行完成", taskCurrentPackageNum));
                return;
            }
            if (task.getAbort()) {
                logger.info(task.getPlateNumber() + ": REMOTE UPGRADE的升级任务已被中断");
                return;
            }
            if (task.getTotalPackageSize().intValue() == task.getSuccessPackageSize().intValue()) {
                logger.info(task.getPlateNumber() + ": REMOTE UPGRADE下发升级包完成");
                return;
            }
            logger.info(task.getPlateNumber() + ": REMOTE UPGRADE执行时间轮");
            task.timeOutOrSendFault();
        }, DELAY_TIME, TimeUnit.SECONDS);
    }

    /**
     * 平台下发总升级数据校验指令
     *
     * @param task task
     */
    public void addTotalUpgradeTask(RemoteUpgradeTask task) {
        int faultTimes = task.getFaultTimes();
        timer.newTimeout(timeout -> {
            if (task.getAbort()) {
                return;
            }
            // 收到终端应答成功 or 已经失败
            if (task.getTotalUpgradeDataCheckInstruction()  || faultTimes != task.getFaultTimes()) {
                // 终端应答成功,退出
                task.setTotalUpgradeDataCheckInstruction();
            }
            task.addFaultTimes();
        }, DELAY_TIME, TimeUnit.SECONDS);
    }

    /**
     * 平台下发开始升级指令
     *
     * @param task task
     */
    public void addStartUpgradeCommandTask(RemoteUpgradeTask task) {
        int faultTimes = task.getFaultTimes();
        timer.newTimeout(timeout -> {
            if (task.getAbort()) {
                // 下发失败中止升级
                return;
            }
            // 收到终端应答成功 or 已经失败
            if (task.getStartUpgradeCommand() || faultTimes != task.getFaultTimes()) {
                // 终端应答成功,退出
                return;
            }
            task.addFaultTimes();
        }, DELAY_TIME, TimeUnit.SECONDS);
    }

    /**
     * 监听终端擦除数
     * @param task task
     */
    public void addEraseTerminalTask(RemoteUpgradeTask task) {
        int faultTimes = task.getFaultTimes();
        timer.newTimeout(timeout -> {
            if (task.getAbort()) {
                logger.info("REMOTE UPGRADE中止擦出终端");
                return;
            }
            if (task.getEraseTerminal() || faultTimes != task.getFaultTimes()) {
                // 终端应答成功,退出
                logger.info("REMOTE UPGRADE终端应答成功");
                return;
            }
            task.addFaultTimes();
        }, DELAY_TIME, TimeUnit.SECONDS);
    }

    /**
     * 终端-> 外设, 每收到一个终端应答, 就重置时间轮, 进行新一次的监听
     *
     * @param task task
     */
    public Timeout waitUpgradeSuccessOrFault(RemoteUpgradeTask task) {
        return timer.newTimeout(timeout -> {
            if (task.getPeripheralUpgradeSuccess()) {
                return;
            }
            // 下发结束升级指令
            Integer serialNumber = DeviceHelper.getRegisterDevice(task.getMonitorId(), "");
            task.sendEndUpgrade(serialNumber);
            // 推送数据给前端
            task.setF3ToPeripheralStatus(RemoteUpgradeUtil.F3_STATUS_FAILED);
            task.setSensorUpgradeStatus(RemoteUpgradeUtil.PERIPHERAL_UPGRADE_TIME_OUT);
            // 等待终端响应超过60s, 则结束此次升级
            task.stopUpgrade();
            task.onMessage(new RemoteUpgradeToWeb(task.getCurrentStatus(), task.getF3ToPeripheralTotalPackageSize(),
                task.getF3ToPeripheralSuccessPackageSize(), task.getMonitorId()), true, task);
        }, DELAY_TIME_OUT, TimeUnit.SECONDS);
    }
}