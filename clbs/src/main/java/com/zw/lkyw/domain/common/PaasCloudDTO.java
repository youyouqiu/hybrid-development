package com.zw.lkyw.domain.common;

import java.util.List;

import lombok.Data;

@Data
public class PaasCloudDTO<T> {
    private String message;
    private String code;
    private List<T> data;
}
