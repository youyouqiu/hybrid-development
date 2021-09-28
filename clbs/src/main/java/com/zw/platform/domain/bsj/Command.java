package com.zw.platform.domain.bsj;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Title:
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月12日 8:54
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Command  extends BaseFormBean {
    private  String vehicleNumber;//车辆编号
    private  String password;//密码
    private  String hostUrl;//ip和端口
    private  String apn;//APN
    private  String hostNumber;//本机号码
    private  String clockTime;//闹钟时间
    private  String timing;//校时时间
    private  String settingTime;//星期与对应的时间点设置
    private  String intervalTimes;//设置蓝牙报警次数与间隔
    private  String domainName;//域名
    private  String backupIp;//备份IP
    private  Integer switchSetting;//星期与对应的时间点开关设置
    private  Integer type;//1单选项  2多选项
    private  Integer alarmSetting;//防拆报警设置
    private  Integer switchTime;//蓝牙报警开关设置
    private  Integer sendCount;//设置发送条数
    private  Integer backTime;//设置定时回传时间
}
