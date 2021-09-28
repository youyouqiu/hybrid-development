package com.zw.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel("返回结果")
@Getter
public class ResultBean<T> {
    @ApiModelProperty("是否成功")
    private boolean success;

    @ApiModelProperty("描述")
    private String msg;

    @ApiModelProperty("数据")
    private T data;

    public ResultBean() {}

    public ResultBean(boolean success, String msg) {
        this(success, msg, null);
    }

    public ResultBean(boolean success, String msg, T data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }
}
