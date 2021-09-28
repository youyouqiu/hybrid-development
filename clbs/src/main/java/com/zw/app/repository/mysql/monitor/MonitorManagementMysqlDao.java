package com.zw.app.repository.mysql.monitor;

/**
 * @author hujun
 * @date 2018/8/20 15:22
 */
public interface MonitorManagementMysqlDao {

    /**
     * 获取监控对象绑定的油箱类型（主、副）
     * @param mid
     * @return
     */
    String getOilBoxType(String mid);

    /**
     * 获取监控对象绑定的油耗传感器id
     * @param mid
     * @return
     */
    String getOilWearId(String mid);

}
