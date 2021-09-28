package com.zw.adas.domain.riskManagement.form;

import com.zw.adas.domain.common.AdasRiskStatus;
import com.zw.adas.domain.common.AdasRiskType;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by PengFeng on 2017/8/23 9:14
 */
@EqualsAndHashCode(callSuper = false)
public class AdasRiskDisposeRecordForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 监控对象
     */
    @Getter
    @Setter
    @ExcelField(title = "监控对象", mergedRegion = true)
    private String brand;

    /**
     * 风险编号
     */
    @Getter
    @Setter
    @ExcelField(title = "风险编号", mergedRegion = true)
    private String riskNumber;

    /**
     * 驾驶员
     */
    @Getter
    @Setter
    @ExcelField(title = "驾驶员", mergedRegion = true)
    private String driver;

    /**
     * 驾驶证号
     */
    @Getter
    @Setter
    @ExcelField(title = "驾驶证号", mergedRegion = true)
    private String driverNo;

    /**
     * 所属企业
     */
    @Getter
    @Setter
    @ExcelField(title = "所属企业", mergedRegion = true)
    private String groupName;

    /**
     * 风险类型
     */
    @Getter
    @Setter
    @ExcelField(title = "风险类型", mergedRegion = true)
    private String riskType;

    /**
     * 风险等级
     */
    @Getter
    @Setter
    @ExcelField(title = "风险等级", mergedRegion = true)
    private String riskLevel;

    /**
     * 风险的速度
     */
    @Getter
    @Setter
    @ExcelField(title = "速度", mergedRegion = true)
    private String speed;

    /**
     * 事件编号
     */
    @Getter
    @Setter
    @ExcelField(title = "事件编号")
    private String eventNumber;

    /**
     * 风险事件
     */
    @Getter
    @Setter
    @ExcelField(title = "报警事件")
    private String riskEvent;

    /**
     * 事件时间
     */
    @Getter
    @Setter
    @ExcelField(title = "事件时间")
    private String eventTime;

    /**
     * 预警时间
     */
    @Getter
    @ExcelField(title = "预警时间", mergedRegion = true)
    private String warTime;

    /**
     * 预警位置
     */
    @Getter
    @Setter
    @ExcelField(title = "预警位置", mergedRegion = true)
    private String address;

    @Getter
    @Setter
    private String formattedAddress;

    /**
     * 通过时间进行排序的字段
     */
    @Getter
    @Setter
    private long orderTime;
    /**
     * 状态
     */
    @Getter
    @Setter
    @ExcelField(title = "处理状态", mergedRegion = true)
    private String status;

    /**
     * 处理人姓名
     */
    @Getter
    @Setter
    @ExcelField(title = "处理人", mergedRegion = true)
    private String dealUser;

    /**
     * 归档时间
     */
    @Getter
    @Setter
    @ExcelField(title = "处理时间", mergedRegion = true)
    private String fileTime;

    /**
     * 预警天气
     */
    @Getter
    @Setter
    @ExcelField(title = "天气情况", mergedRegion = true)
    private String weather;

    /**
     * 处理时间
     */
    @Getter
    @Setter
    //  @ExcelField(title = "处理时间", mergedRegion = true)
    private String dealTime;

    /**
     * id
     */
    @Getter
    @Setter
    private String id;

    /**
     * id
     */
    @Getter
    @Setter
    private byte[] idByte;

    /**
     * 所属企业ldapUUID
     */
    @Getter
    @Setter
    private String groupId;

    /**
     * 岗位
     */
    @Getter
    @Setter
    // @ExcelField(title = "岗位", mergedRegion = true)
    private String job;

    /**
     * 回访次数
     */
    @Getter
    @Setter
    // @ExcelField(title = "回访次数", mergedRegion = true)
    private String visitTime;

    /**
     * 风控结果
     */
    @Getter
    @Setter
    // @ExcelField(title = "风控结果", mergedRegion = true)
    private String riskResult;

    /**
     * 经度，提供表格逆地理编码
     */
    @Getter
    @Setter
    private String longtitude;

    /**
     * 纬度，提供表格逆地理编码
     */
    @Getter
    @Setter
    private String latitude;

    @Getter
    @Setter
    private String vehicleId;

    @Getter
    @Setter
    private String mediaName;

    @Getter
    @Setter
    private String mediaUrlNew;

    @Getter
    @Setter
    private String mediaId;

    @Getter
    @Setter
    private String driverIds;

    @Getter
    @Setter
    private String riskId;

    @Getter
    @Setter
    private String riskEventId;

    /**
     * 视频终端证据
     */
    @Getter
    @Setter
    private Byte videoFlag;

    /**
     * 图片终端证据
     */
    @Getter
    @Setter
    private Byte picFlag;

    /**
     * 是否有多媒体文件
     */
    @Getter
    @Setter
    private Boolean hasMedia;

    /**
     * 是否有终端视频文件
     */
    @Getter
    @Setter
    private Boolean hasVideo;

    /**
     * 是否有终端图片文件
     */
    @Getter
    @Setter
    private Boolean hasPic;

    @Getter
    @Setter
    private int protocolType;

    /**
     * 处理类型
     */
    @Getter
    @Setter
    private String handleType;

    public AdasRiskDisposeRecordForm() {

    }

    public void setWarTime(String warTime) {
        if (StringUtil.isNullOrBlank(warTime) || !StringUtils.isNumeric(warTime)) {
            this.warTime = warTime;
        } else {
            this.warTime = DateUtil.getDateToString(new Date(Long.parseLong(warTime)), "yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 转换多媒体文件标志
     */
    public void assembleMediaFlag() {

        hasPic = picFlag != null && picFlag == 1;
        hasVideo = videoFlag != null && videoFlag == 1;
        hasMedia = hasPic || hasVideo;
    }

    /**
     * 手动调用设置风险类型名称方法
     */
    private void setRiskTypeName() {
        this.riskType = AdasRiskType.getRiskType(riskType);
    }

    /**
     * 手动调用设置风险等级名称方法
     */
    private void setRiskLevelName(AdasCommonHelper commonHelper) {
        this.riskLevel = commonHelper.geRiskLevel(riskLevel);
    }

    public void transFormData(AdasCommonHelper commonHelper) {
        setRiskTypeName();
        setRiskLevelName(commonHelper);
        status = AdasRiskStatus.getRiskStatus(status);
    }
}