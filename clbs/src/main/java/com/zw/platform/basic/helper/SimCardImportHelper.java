package com.zw.platform.basic.helper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.dto.imports.SimCardImportDTO;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.platform.util.excel.annotation.ExcelImportHelper;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/9 9:33
 */
public class SimCardImportHelper implements ExcelImportHelper<SimCardDO, SimCardImportDTO> {
    /**
     * 用来存放最终校验的结果
     */
    private boolean verified = false;

    private SimCardNewDao simCardNewDao;

    private Set<String> allSimCardNumber;

    private List<SimCardImportDTO> importDataList;

    /**
     * 组织名称对应的第一条数据
     */
    private Map<String, String> orgMap = new HashMap<>();

    private static final String REGEX = "^[A-Z0-9]+$";

    private static final Pattern SIM_CARD_CHECKER = Pattern.compile("^[0-9a-zA-Z]{7,20}$");

    private ImportExcel importExcel;
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final Set<String> operatorSet = ImmutableSet.of("中国移动", "中国联通", "中国电信");
    private static final Map<String, Integer> isStartMap = ImmutableMap.of("启用", 1, "停用", 0);

    public SimCardImportHelper(SimCardNewDao simCardNewDao, ImportExcel importExcel) {
        this.simCardNewDao = simCardNewDao;
        this.importExcel = importExcel;
    }

    @Override
    public void validate(Map<String, String> orgNameMap) throws BusinessException {
        validateDataSize(importDataList);
        init(orgNameMap);
        for (int i = 0, n = importDataList.size(); i < n; i++) {
            SimCardImportDTO simCard = importDataList.get(i);
            if (simCard != null) {

                if (StringUtils.isNotBlank(simCard.getErrorMsg())) {
                    continue;
                }
                if (allSimCardNumber.contains(simCard.getSimCardNumber())) {
                    simCard.setErrorMsg("终端手机号已存在");
                    continue;
                }
                if (null != simCard.getImsi() && simCard.getImsi().length() >= 50) {
                    simCard.setErrorMsg("imsi长度应小于等于50");
                    continue;
                }
                if (null != simCard.getIccid() && simCard.getIccid().length() != 20) {
                    simCard.setErrorMsg("iccid长度应为20");
                    continue;
                }
                if (null != simCard.getIccid() && !Pattern.matches(REGEX, simCard.getIccid())) {
                    simCard.setErrorMsg("iccid只能是大写字母和数字");
                    continue;
                }
                if (null != simCard.getImei() && simCard.getImei().length() > 20) {
                    simCard.setErrorMsg("imei长度应小于20");
                    continue;
                }

                //所属企业
                String org = simCard.getOrgName();
                String orgId = orgMap.get(org);
                if (orgId == null) {
                    simCard.setErrorMsg("所属企业无权限");
                    continue;
                }

                if (StringUtils.isNotBlank(simCard.getSimFlow()) && !Pattern
                    .matches("^[0-9]*$", simCard.getSimFlow())) {
                    simCard.setErrorMsg("套餐流量格式不正确");
                    continue;
                }
                // 修正系数
                if (StringUtils.isNotBlank(simCard.getCorrectionCoefficient())) {
                    if (Pattern.matches("^[1-9]\\d*$", simCard.getCorrectionCoefficient())) {
                        BigDecimal correctionCoefficient = new BigDecimal(simCard.getCorrectionCoefficient());
                        if (correctionCoefficient.compareTo(new BigDecimal("200")) > 0
                            || correctionCoefficient.compareTo(new BigDecimal("1")) < 0) {
                            simCard.setErrorMsg("修正系数值必须在1~200之间");
                            continue;
                        }
                    } else {
                        simCard.setErrorMsg("修正系数值必须为正整数");
                        continue;
                    }
                }
                // 预警系数
                if (StringUtils.isNotBlank(simCard.getForewarningCoefficient())) {
                    if (Pattern.matches("^[1-9]\\d*$", simCard.getForewarningCoefficient())) {
                        BigDecimal forewarningCoefficient = new BigDecimal(simCard.getForewarningCoefficient());
                        if (forewarningCoefficient.compareTo(new BigDecimal("200")) > 0
                            || forewarningCoefficient.compareTo(new BigDecimal("1")) < 0) {
                            simCard.setErrorMsg("预警系数值必须在1~200之间");
                            continue;
                        }
                    } else {
                        simCard.setErrorMsg("预警系数值必须为正整数");
                        continue;
                    }
                }
                // 小时流量阈值
                if (StringUtils.isNotBlank(simCard.getHourThresholdValue())) {
                    if (Pattern.matches("^[+]?(\\d+)$|^[+]?(\\d+\\.\\d+)$", simCard.getHourThresholdValue())) {
                        BigDecimal hourThresholdValue = new BigDecimal(simCard.getHourThresholdValue());
                        if (hourThresholdValue.compareTo(new BigDecimal("6553")) > 0) {
                            simCard.setErrorMsg("小时流量阈值必须在0~6553之间");
                            continue;
                        }
                    } else {
                        simCard.setErrorMsg("小时流量阈值必须为正数");
                        continue;
                    }
                }
                // 日流量阈值
                if (StringUtils.isNotBlank(simCard.getDayThresholdValue())) {
                    if (Pattern.matches("^[+]?(\\d+)$|^[+]?(\\d+\\.\\d+)$", simCard.getDayThresholdValue())) {
                        BigDecimal dayThresholdValue = new BigDecimal(simCard.getDayThresholdValue());
                        if (dayThresholdValue.compareTo(new BigDecimal("429496729")) > 0) {
                            simCard.setErrorMsg("日流量阈值必须在0~429496729之间");
                            continue;
                        }
                    } else {
                        simCard.setErrorMsg("日流量阈值必须为正数");
                        continue;
                    }
                }
                // 月流量阈值
                if (StringUtils.isNotBlank(simCard.getMonthThresholdValue())) {
                    if (Pattern.matches("^[+]?(\\d+)$|^[+]?(\\d+\\.\\d+)$", simCard.getMonthThresholdValue())) {
                        BigDecimal monthThresholdValue = new BigDecimal(simCard.getMonthThresholdValue());
                        if (monthThresholdValue.compareTo(new BigDecimal("429496729")) > 0) {
                            simCard.setErrorMsg("月流量阈值必须在0~429496729之间");
                            continue;
                        }
                    } else {
                        simCard.setErrorMsg("月流量阈值必须为正数");
                        continue;
                    }
                }
                Matcher matcher = SIM_CARD_CHECKER.matcher(simCard.getSimCardNumber());
                if (!matcher.matches()) {
                    // 不匹配的终端手机号的格式
                    simCard.setErrorMsg("终端手机号必须是7-20位的数字或字母");
                    continue;
                }

                // 校验激活日期不能大于到期时间
                if (simCard.getOpenCardTime() != null && simCard.getEndTime() != null) {
                    Date openTime = DateUtil.getStringToDate(simCard.getOpenCardTime(), DATE_FORMAT);
                    if (Objects.isNull(openTime)) {
                        simCard.setErrorMsg("激活日期错误");
                        continue;
                    }
                    Date endTime = DateUtil.getStringToDate(simCard.getEndTime(), DATE_FORMAT);
                    if (Objects.isNull(openTime)) {
                        simCard.setErrorMsg("到期日期错误");
                        continue;
                    }

                    if (endTime.before(openTime)) {
                        simCard.setErrorMsg("激活日期不能大于到期时间");
                        continue;
                    }
                }

                // 真实SIM卡号
                String realId = simCard.getRealId();
                if (StringUtils.isNotBlank(realId)) {
                    if (!realId.trim().matches("^[0-9]{7,20}$")) {
                        simCard.setErrorMsg("真实SIM卡号必须是7-20位整数");
                        continue;
                    }
                }

            }
        }
        List<String> errors = importDataList.stream().map(SimCardImportDTO::getErrorMsg).filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
        verified = errors.isEmpty();
    }

    @Override
    public List<SimCardDO> getFinalData() {
        /**
         * 组装最终插入数据库的实体
         */
        List<SimCardDO> simCardDOList = new ArrayList<>();

        //验证不通过，则不进行导入数据组装
        if (!verified) {
            return simCardDOList;
        }
        for (SimCardImportDTO data : importDataList) {
            //初始化导入的实体信息
            initImportData(data);
            //构建导入的do对象
            SimCardDO importData = SimCardDO.getImportData(data, orgMap, isStartMap.get(data.getIsStarts()));
            simCardDOList.add(importData);
        }
        return simCardDOList;
    }

    @Override
    public List<SimCardImportDTO> getExcelData() {
        return importDataList;
    }

    @Override
    public void init(Class<SimCardImportDTO> cls) throws InstantiationException, IllegalAccessException {
        importDataList = importExcel.getDataListNew(cls);
    }

    @Override
    public boolean getValidateResult() {
        return verified;
    }

    private void initImportData(SimCardImportDTO data) {
        String operator = Converter.toBlank(data.getOperator());
        // 不符合规范的字段
        if (operator.length() > 50 || !operatorSet.contains(operator)) {
            data.setOperator("");
        }
        String simFlow = Converter.toBlank(data.getSimFlow());
        if (StringUtils.isNotBlank(simFlow)) {
            if (!StringUtils.isNumeric(simFlow) || simFlow.length() > 20) {
                data.setSimFlow("");
            }
        }

    }

    /**
     * 初始化校验相关信息
     * @param orgNameMap
     */
    private void init(Map<String, String> orgNameMap) {
        orgMap = orgNameMap;
        allSimCardNumber = simCardNewDao.getAllSimCardNumber();
    }
}
