package com.zw.platform.domain.oil;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/12/6.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmHandle extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String positionalId; // 位置信息表ID

    private String personId;// 处理人ID

    @ExcelField(title = "监控对象")
    private String plateNumber;// 车牌号

    @ExcelField(title = "组织名称")
    private String assignmentName;// 组织名称

    @ExcelField(title = "所属企业")
    private String name;

    private String plateColor; // 车牌颜色

    @ExcelField(title = "车牌颜色")
    private String plateColorString;

    @ExcelField(title = "从业人员名称")
    private String professionalsName;// 从业人员名称

    private int alarmType;// 报警类型

    private int alarmSource;// 报警来源

    @ExcelField(title = "报警类型")
    private String description;// 描述

    @ExcelField(title = "严重程度")
    private String severityName;

    @ExcelField(title = "报警来源")
    private String alarmSourceString; // 报警来源

    private Long alarmStartTime;// 报警开始时间

    public Long getAlarmStartTime() {
        if (alarmStartTime != null) {
            alarmStartTime = DateUtil.getMillisecond(alarmStartTime);
        }
        return alarmStartTime;
    }

    private Long alarmEndTime;// 报警结束时间

    public Long getAlarmEndTime() {
        if (alarmEndTime != null) {
            alarmEndTime = DateUtil.getMillisecond(alarmEndTime);
        }
        return alarmEndTime;
    }

    @ExcelField(title = "报警开始速度（km/h）")
    private String speed;// 行驶速度

    /**
     * 行车记录仪速度
     */
    @ExcelField(title = "行车记录仪速度")
    private String recorderSpeed;

    /**
     * 道路类型
     * 1：高速路 2：都市高速路 3：国道 4：省道 5：县道 6：乡村道路 7：其他道路
     */
    private Integer roadType;

    @ExcelField(title = "道路类型")
    private String roadTypeStr;

    @ExcelField(title = "平台限速")
    private Double speedLimit;//限速

    @ExcelField(title = "路网限速")
    private Double roadNetSpeedLimit;//限速

    @ExcelField(title = "超速时长(S)")
    private Integer speedTime;//超速时长

    @ExcelField(title = "报警开始时间")
    private String startTime;// 报警开始时间

    @ExcelField(title = "报警结束时间")
    private String endTime;// 报警开始时间

    @ExcelField(title = "报警开始经度")
    private String alarmStartlongtitude;// 经度

    @ExcelField(title = "报警开始纬度")
    private String alarmStartlatitude;// 纬度

    @ExcelField(title = "报警结束经度")
    private String alarmEndlongtitude;// 经度

    @ExcelField(title = "报警结束纬度")
    private String alarmEndlatitude;// 纬度

    @ExcelField(title = "报警开始位置")
    private String alarmStartSpecificLocation;// 报警开始具体地址(目前只用于报警查询的导出功能,如其他地方要使用到,请删除注释)

    @ExcelField(title = "报警结束位置")
    private String alarmEndSpecificLocation;// 报警开始具体地址(目前只用于报警查询的导出功能,如其他地方要使用到,请删除注释)

    @ExcelField(title = "围栏类型")
    private String fenceType; // 围栏类型

    @ExcelField(title = "围栏名称")
    private String fenceName; // 围栏名称

    @ExcelField(title = "处理状态")
    private String alarmStatus;// 状态

    @ExcelField(title = "处理人")
    private String personName;// 处理人名称

    @ExcelField(title = "处理时间")
    private String handleTime;// 处理时间

    private int status;// 状态

    @ExcelField(title = "处理方式")
    private String handleType;// 处理类型
    // @ExcelField(title = "报警结束时间")

    private String alarmStartLocation;// 报警开始位置

    private String alarmEndLocation;// 报警结束位置

    private Integer alarmNumber;// 报警次数

    private Long duration;// 报警时长

    private int monitorType; // 监控对象类型

    private byte[] vehicleIdHbase;

    private String vehicleId;

    private String swiftNumber;

    private String deviceNumber;

    private String simcardNumber;

    private String deviceType;

    private String longtitude;// 经度

    private String latitude;// 纬度

    private int pushType;

    private Double severity;//严重程度

    public String getSeverityName() {
        if (calStandard == null || severity == null) {
            return "";
        }
        if (alarmType == 76 && calStandard == 2) {
            if (severity < 0.2) {
                return "一般严重";
            } else if (severity >= 0.2 && severity < 0.5) {
                return "比较严重";
            } else if (severity >= 0.5) {
                return "特别严重";
            }
        }
        return "";
    }

    private Double maxSpeed;//最高速度

    private String groupId;//企业id

    private byte[] groupIdHbase;//hbase中企业id

    private Integer calStandard; // 报警计算标准：0普通标准 1山西标准 2四川标准

    private Integer protocolType;// 协议类型

    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;
    /**
     * 主干4.1.1新增报表处理短信内容
     */
    private String dealOfMsg;
    /**
     * 主干4.1.1新增报表处理短信内容
     */
    private String sendOfMsg;



}
