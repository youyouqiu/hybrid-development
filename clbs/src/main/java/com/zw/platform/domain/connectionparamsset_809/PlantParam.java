package com.zw.platform.domain.connectionparamsset_809;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/2/21.
 * 809平台参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlantParam extends BaseFormBean implements Serializable, T809MsgBody {
    private static final long serialVersionUID = 1L;
    private String ip;//主链路ip地址
    private String ipBranch;//从链路ip地址
    private String port;//端口
    private String userName;//用户名
    private String password;//密码
    private Integer centerId;//接入码
    private String m;
    private String ia;
    private String ic;
    private String platformId;//平台id
    private String platformName;//平台名称
    private String versionFlag;//版本号
    private String permitId;//经营许可证号
    private String zoneDescription;//行政区号
    private String authorizeCode1;//归属地平台口令
    private String authorizeCode2;//跨域平台口令
    private String videoIp;//音视频ip地址
    private String videoPort;//音视频端口
    private Integer plateType;//平台类型
    private String editTitle;
    private Integer protocolType;//协议类型
    private String protocolTypeName;//协议类型名称
    private int branchServer;//（从链路服务端）:"未启动"
    private String branchServerName;//（从链路服务端）名称
    private int branchStatus;//（从链路状态）:"已断开"
    private String branchStatusName;//（从链路状态）名称
    private int mainClient;//（主链路客户端）:"已启动"
    private String mainClientName;//（主链路客户端）名称
    private int mainStatus;//（主链路状态）:"已断开"
    private String mainStatusName;//（主链路状态）名称
    private int serverStatus;//（是否开启服务）:"关闭"
    private String serverStatusName;//（是否开启服务）名称
    /**
     * 是否开启过滤 0:关闭; 1:开启
     */
    private Integer dataFilterStatus;
    private Integer verifyCode;//校验码
    private String groupId;//所属企业id
    private Integer groupProperty;//企业属性
    private String groupName;//企业名称
    private String groupPropertyName;//企业属性名称
    private Integer mappingFlag;//是否设置809报警映射，默认空 未设置，1 设置
    /**
     * 车辆id
     */
    private String vehicleId;
}
