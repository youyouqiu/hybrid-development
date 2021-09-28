package com.zw.platform.domain.oilsubsidy.line;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author wanxing
 * @Title: 线路实体类
 * @date 2020/10/911:52
 */
@Data
public class LineDTO extends LineDO {

    /**
     * 方向
     */
    @NotNull(message = "上下行不能为空")
    @Valid
    private List<DirectionDTO> direction;

    /**
     * 组织名称
     */
    private String orgName;
}
