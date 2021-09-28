package com.zw.platform.domain.riskManagement.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by PengFeng on 2017/8/24  18:02
 */
@EqualsAndHashCode(callSuper = false)
public class RiskDisposeRecordQuery extends BaseQueryBean {
    private static Map<String, String> typeMap = new HashMap<String, String>();

    private static Map<String, String> resultMap = new HashMap<String, String>();

    private static Map<String, String> statusMap = new HashMap<String, String>();

    private static Map<String, String> riskLevelMap = Maps.newHashMap();

    private static Map<String, String> evidenceTypeMap = Maps.newHashMap();

    static {
        typeMap.put("疑似疲劳", "1");
        typeMap.put("注意力分散", "2");
        typeMap.put("违规异常", "3");
        typeMap.put("碰撞危险", "4");
        resultMap.put("接受督导高敏", "1");
        resultMap.put("接受督导低敏", "2");
        resultMap.put("事故发生", "3");
        statusMap.put("待处理", "1");
        statusMap.put("处理中", "3");
        statusMap.put("跟踪中", "5");
        statusMap.put("待回访", "2");
        statusMap.put("回访中", "4");
        statusMap.put("已归档", "6");
        statusMap.put("已处理", "6");
        statusMap.put("未处理", "1");

        /*riskLevelMap.put("一般（低）", "1");
        riskLevelMap.put("一般（中）", "2");
        riskLevelMap.put("一般（高）", "3");
        riskLevelMap.put("较重（低）", "4");
        riskLevelMap.put("较重（中）", "5");
        riskLevelMap.put("较重（高）", "6");
        riskLevelMap.put("严重（低）", "7");
        riskLevelMap.put("严重（中）", "8");
        riskLevelMap.put("严重（高）", "9");
        riskLevelMap.put("特重（低）", "10");
        riskLevelMap.put("特重（中）", "11");
        riskLevelMap.put("特重（高）", "12");
        */
        evidenceTypeMap.put("终端图片", "1");
        evidenceTypeMap.put("终端视频", "2");
        evidenceTypeMap.put("风控音频", "3");
        evidenceTypeMap.put("风控视频", "4");

    }

    @Getter
    @Setter
    private String eventIdStrs;

    @Getter
    @Setter
    private List<byte[]> eventIds;

    @Getter
    @Setter
    private String riskIdStrs;

    @Getter
    @Setter
    private List<byte[]> riskIds;

    @Getter
    @Setter
    private String riskIdStr;

    @Getter
    @Setter
    private byte[] riskId;

    @Getter
    @Setter
    private String vehicleIds;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private String endTime;

    @Getter
    @Setter
    private String riskNumber;

    @Getter
    private String riskType;

    @Getter
    private String riskLevel;

    @Getter
    @Setter
    private String brand;

    @Getter
    @Setter
    private String driver;

    @Getter
    private String status;

    @Getter
    @Setter
    private String dealUser;

    @Getter
    private String visitTime;

    @Getter
    private String riskResult;

    @Getter
    private String evidenceType;

    @Getter
    private String riskEvent;

    @Setter
    @Getter
    private String[] deleteIds;

    @Setter
    @Getter
    private Object[] searchAfter;

    @Setter
    @Getter
    private List<String> excludeIds = Lists.newLinkedList();

    public void setEvidenceType(String evidenceType) {

        if (!StringUtil.isNullOrEmpty(evidenceType)) {
            this.evidenceType = evidenceTypeMap.get(evidenceType);
        }
    }

    public void setRiskEvent(String riskEvent) {
        if (StringUtil.isNullOrBlank(riskEvent) || "所有".equals(riskEvent)) {
            this.riskEvent = null;
        } else {
            this.riskEvent = riskEvent;
        }
    }

    public void setRiskType(String riskType) {
        if (StringUtil.isNullOrBlank(riskType) || "所有".equals(riskType)) {
            this.riskType = null;
        } else {
            if (riskType.contains("+")) {
                String[] riskTypes = riskType.split("\\+");
                riskType = "";
                for (int i = 0; i < riskTypes.length; i++) {
                    if (!"".equals(riskType)) {
                        riskType = riskType + "," + typeMap.get(riskTypes[i]);
                    } else {
                        riskType = typeMap.get(riskTypes[i]);
                    }
                }
                //执行排序
                String[] str = riskType.split(",");
                Arrays.sort(str);
                this.riskType = StringUtils.join(str, ",");
            } else {
                this.riskType = typeMap.get(riskType);
            }
        }
    }

    public void setStatus(String status) {
        if (StringUtil.isNullOrBlank(status) || "所有".equals(status)) {
            this.status = null;
        } else {
            this.status = statusMap.get(status);
        }
    }

    public void setVisitTime(String visitTime) {
        if (StringUtil.isNullOrBlank(visitTime) || "所有".equals(visitTime)) {
            this.visitTime = null;
        } else {
            this.visitTime = visitTime;
        }
    }

    public void setRiskResult(String riskResult) {
        if (StringUtil.isNullOrBlank(riskResult) || "所有".equals(riskResult)) {
            this.riskResult = null;
        } else {
            this.riskResult = resultMap.get(riskResult);
        }
    }

    public void setRiskLevel(String riskLevel) {
        if (StringUtil.isNullOrBlank(riskLevel) || "所有".equals(riskLevel)) {
            this.riskLevel = null;
        } else {
            this.riskLevel = riskLevel;
        }
    }
}
