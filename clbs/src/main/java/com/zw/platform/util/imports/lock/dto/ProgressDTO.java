package com.zw.platform.util.imports.lock.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 进度条DTO
 *
 * @author Zhang Yanhui
 * @since 2020/9/14 17:16
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "进度条出参")
public class ProgressDTO {

    @ApiModelProperty("总长度")
    private Integer total;

    @ApiModelProperty("当前长度，不保证不超过总长")
    private Integer current;

    @ApiModelProperty("当前进度")
    private Double ratio;

    @ApiModelProperty("状态 0 进行中 1 完成 2 失败 3 等待中")
    private Integer status;

    @ApiModelProperty("当前阶段，0~N")
    private Integer stage;

    @ApiModelProperty("当前阶段名称（状态）")
    private String stageName;
}
