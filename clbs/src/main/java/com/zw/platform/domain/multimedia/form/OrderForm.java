package com.zw.platform.domain.multimedia.form;


import com.zw.platform.domain.vas.alram.PhotoParam;
import com.zw.platform.util.common.Customer;
import lombok.Data;

import java.util.List;


/**
 * 指令信息 @author  Tdz
 **/

@Data
public class OrderForm {
    private String vid;// 车辆id


    private Integer orderType;// 指令类型

    /**
     * 拍照视频
     */
    private Integer wayID;// 通道ID

    private Integer command;// 拍摄命令

    private Integer time;// 拍照间隔/录像时间

    private Integer saveSign;// 保存标志

    private Integer distinguishability;// 分辨率

    private Integer quality;// 图像/视频质量

    private Integer luminance;// 亮度

    private Integer contrast;// 对比度

    private Integer saturability;// 饱和度

    private Integer chroma;// 色度

    private String marks;

    /**
     * 位置汇报
     */
    private Integer positionUpTactics;

    private Integer positionUpScheme;

    private Integer driverLoggingOutUpTimeSpace;

    private Integer dormancyUpTimeSpace;

    private Integer emergencyAlarmUpTimeSpace;

    private Integer defaultTimeUpSpace;

    private Integer defaultDistanceUpSpace;

    private Integer driverLoggingOutUpDistanceSpace;

    private Integer dormancyUpDistanceSpace;

    private Integer emergencyAlarmUpDistanceSpace;

    /**
     * 电话回拨
     */
    private Integer sign;

    private String regRet;

    /**
     * 文本信息
     */
    private String txt;// 文本信息

    /**
     * 问题
     */
    private String question;

    private String value;

    private Integer emergency; // 紧急

    private Integer tts;

    private Integer screen;// 广告屏显示

    private Integer cw;// 命令字

    private String param;// 命令参数

    /**
     * 超速设置
     */
    private Integer masSpeed;// 最高速度

    private Integer speedTime;// 超速持续时间

    /**
     * 多媒体检索
     */
    private Integer type;

    private Integer eventCode;

    /**
     * 多媒体上传
     */
    private Integer deleteSign;

    /**
     * 录音
     */
    private Integer frequency;// 音频采样率

    private Integer voiceCommand;// 录音命令

    /**
     * 透传消息
     */
    private String data;

    /**
     * 流水号
     */
    private Integer serialNumber;

    /**
     * 断油电
     */
    private String oilElectricMsg;
    
    /**
     * 界面类型 1：实时监控 2：实时视频
     */
    private Integer webType;

    private Integer isAutoDeal = 0;

    private Integer flag; // 1 通电 0断电

    public static OrderForm fromPhotoParam(PhotoParam photoParam, String wayId) {
        OrderForm orderForm = new OrderForm();
        orderForm.setSerialNumber(Integer.valueOf(new Customer().getCustomerID()));
        orderForm.setChroma(photoParam.getChroma());
        orderForm.setCommand(photoParam.getCommand());
        orderForm.setContrast(photoParam.getContrast());
        orderForm.setDistinguishability(photoParam.getResolution());
        orderForm.setLuminance(photoParam.getLuminance());
        orderForm.setQuality(photoParam.getQuality());
        orderForm.setSaturability(photoParam.getSaturability());
        orderForm.setWayID(Integer.valueOf(wayId));
        orderForm.setSaveSign(photoParam.getSaveSign());
        orderForm.setTime(photoParam.getTime());
        return orderForm;
    }

    /**
     * 行驶记录数据下传
     */
    private String vin;

    private String plateNumber;

    private String plateType;

    /**
     * 行驶记录数据采集 命令值
     */
    private String commandSign;

    private String startTime;

    private String endTime;

    /**
     * 最大单位数据块个数N(高字节0~255)
     */
    private int maxSum = 1;


    private String device; // 终端号

    private String simcard; // SIM卡号

    private Integer sno;

    private String handleType; // 报警类型

    private String alarm; // 报警标识字符串

    /**
     * 联动页面下发短信的标识
     * 是联动页面的下发  改为true
     */
    private Boolean linkageMsg = false;

    /**
     * 备注
     */
    private String remark;

    /**
     *  报警类型描述
     */
    private String description;


    private String vehicleId;

    private String oldNumber;


    /**
     * 车型ID
     */
    private Long vehicleTypeId;
    /**
     * 单位 ms；默认1000ms  范围：0x0001-0xFFFF
     0xFFFFFFFF表示不修改
     */
    private Integer uploadTime;

    private String brand;

    private String obdVehicleTypeId;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 省域ID
     */
    private String provinceId;

    /**
     * 市域ID
     */
    private String cityId;

    /**
     * 文本下发: 1:通知;2:服务
     */
    private Integer textType;

    /**
     * 1:通知;2:服务;3:紧急;
     */
    private Integer messageTypeOne = 1;

    /**
     * 0: 中心导航信息; 1: CAN故障码信息
     */
    private Integer messageTypeTwo = 0;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 围栏查询的类型：
     * 1=查询圆形区域数据，
     * 2=查询矩形区域数据，
     * 3=查询多边形区域数据，
     * 4=查询线路数据
     */
    Integer queryType;

    /**
     * 查询区域总数
     */
    private Integer countId = 0;

    /**
     * 区域ID集合
     */
    List<Integer> queryId;

    /**
     * 川冀标主动安全报警event_id
     */
    private String riskEventId;

    /**
     * 川冀标主动安全报警risk_id
     */
    private String riskId;


    /**
     * 是否是只处理主动安全的报警
     */
    private Integer isAdas;
}
