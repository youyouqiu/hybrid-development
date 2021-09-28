package com.zw.platform.domain.param.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommandParametersForm extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String vehicleId;  //车id

    /**
     * 下发状态
     */
    private Integer status;

    /**
     * 下发表id
     */
    private String paramId;

    /**
     * 设置表id
     */
    private String settingParamId;

    /**
     * 指令类型
     * //通讯参数 "11":  zw_m_communication_param
     * 	//终端参数 "12":  zw_m_device_param
     * 	//终端控制 "13":
     * 	//无线升级 "131":  zw_m_wireless_update
     * 	//无线外设升级 "1310":
     * 	//控制终端连接指定服务器 "132": zw_m_device_connect_param
     * 	//位置汇报参数 "14":  zw_m_position_param
     * 	//终端查询 "15":
     * 	//电话参数 "16":  zw_m_phone_param
     * 	//视频拍照参数 "17":  zw_m_camera_param
     * 	//GNSS参数 "18":  zw_m_gnss_param
     * 	//事件设置 "19":  zw_m_event_set_param
     * 	//"20":电话本设置  zw_m_phone_book_param
     * 	// "21":信息点播菜单  zw_m_information_param
     */
    private String commandType;

    private String brand;

    private String groupName;

    /**
     * 分组名
     */
    private String assignmentName;

    private Integer monitorType;
}
