package com.zw.platform.util;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 公式计算结果实体
 * @Author Tianzhangxu
 * @Date 2020/6/17 10:34
 */
@Data
public class FormulaCalculatorResp implements Serializable {
    private static final long serialVersionUID = -179300124736600858L;

    /**
     * 计算结果
     */
    private Double resultValue;

    /**
     * 计算过程是否有误(包含题目识别失败，计算过程中出错)
     */
    private Boolean isSuccess;
}
