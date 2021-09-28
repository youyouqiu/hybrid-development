package com.zw.platform.domain.multimedia;

import lombok.Data;

import java.util.List;

/**
 * 处理多条报警入参
 *
 * @author Zhang Yanhui
 * @since 2020/10/10 13:33
 */

@Data
public class HandleMultiAlarms {

    private String recordsStr;

    /**
     * 报警主键 逗号分隔
     */
    private String primaryKeyStr;

    private List<Record> records;
    /**
     * 处理方式
     */
    private String handleType;
    /**
     * 备注
     */
    private String remark;
    /**
     * 下发监听：电话号码
     */
    private String monitorPhone;
    /**
     * 主干4.1.1新增报表处理短信内容
     */
    private String dealOfMsg;

    @Data
    public static class Record {

        /**
         * 监控对象id
         */
        private String vehicleId;

        /**
         * 车牌号
         */
        private String plateNumber;

        /**
         * 报警标识字符串
         */
        private String alarm;

        /**
         * 报警开始时间
         */
        private String startTime;

        /**
         * 报警结束时间
         */
        private String endTime;
    }
}
