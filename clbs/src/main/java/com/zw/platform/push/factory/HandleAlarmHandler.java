package com.zw.platform.push.factory;

import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.push.command.AlarmMessageDTO;
import com.zw.platform.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理报警
 *
 * @author Zhang Yanhui
 * @since 2020/9/28 16:40
 */

@Slf4j
@Component
public class HandleAlarmHandler implements AlarmChainHandler {

    @Autowired
    private AlarmFactory alarmFactory;

    @Override
    public void handle(AlarmMessageDTO alarmMessageDTO) {
        alarmFactory.dealAlarm(convertToHandleAlarm(alarmMessageDTO));
    }

    private static HandleAlarms convertToHandleAlarm(AlarmMessageDTO alarmMessageDTO) {
        final String startTime = DateUtil.YMD_HMS.format(DateUtil.fromTimestamp(alarmMessageDTO.getStartAlarmTime()))
                .orElseThrow(RuntimeException::new);

        final HandleAlarms handleAlarms = new HandleAlarms();
        handleAlarms.setVehicleId(alarmMessageDTO.getMonitorId());
        handleAlarms.setAlarm(String.valueOf(alarmMessageDTO.getAlarmType()));
        handleAlarms.setStartTime(startTime);
        handleAlarms.setIsAutoDeal(1);
        return handleAlarms;
    }
}
