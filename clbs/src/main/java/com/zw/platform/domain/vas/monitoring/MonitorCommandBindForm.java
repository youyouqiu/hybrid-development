package com.zw.platform.domain.vas.monitoring;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MonitorCommandBindForm extends BaseFormBean {
    private static final long serialVersionUID = 1L;
    private String vid;//车辆id
    private String paramId;  //参数id
    //通讯参数 "11":
    //终端参数 "12":
    //终端控制 "13":
    //无线升级 "131":
    //无线外设升级 "1310":
    //控制终端连接指定服务器 "132":
    //位置汇报参数 "14":
    //终端查询 "15":
    //电话参数 "16":
    //视频拍照参数 "17":
    //GNSS参数 "18":
    //事件设置 "19":
    //"20":电话本设置
    // "21":信息点播菜单
    //24:RS232串口参数;
    //25:RS485串口参数;
    //26:CAN总线参数;
    private int commandType;   //指令类型
    private String brand;   //车牌号
    private String upgradeType; //批量升级终端参数需要
}
