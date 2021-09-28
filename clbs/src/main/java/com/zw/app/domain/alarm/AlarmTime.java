package com.zw.app.domain.alarm;

import lombok.Data;


@Data
public class AlarmTime {
    private byte[] vehicleId;

    private Long alarmTime;
}
